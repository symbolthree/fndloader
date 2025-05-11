/******************************************************************************
 *
 * ≡≡ FNDLOADER ≡≡
 * Copyright (C) 2009-2016 Christopher Ho
 * All Rights Reserved, symbolthree.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * E-mail: christopher.ho@symbolthree.com
 *
 * ================================================
 *
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/common/SelectNLSLangQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/

package symbolthree.oracle.fndload.common;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;

import symbolthree.oracle.fndload.DBConnection;

public class SelectNLSLangQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/common/SelectNLSLangQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private String    msg = null;
    ArrayList<Choice> al  = new ArrayList<Choice>();

    static final Logger logger = LogManager.getLogger(SelectNLSLangQuestion.class.getName());
    
    public SelectNLSLangQuestion() {}

    @Override
    public boolean enterQuestion() {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            String     sql  = "SELECT a.LANGUAGE_CODE" + "    ,  b.DESCRIPTION" + "  FROM FND_LANGUAGES a,"
                              + "       FND_LANGUAGES_TL b" + " WHERE a.INSTALLED_FLAG = 'I'"
                              + "   AND a.LANGUAGE_CODE  = b.LANGUAGE_CODE" + "   AND b.LANGUAGE='US'"
                              + " ORDER BY b.DESCRIPTION";
            ResultSet rs = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                al.add(new Choice(rs.getString(1), rs.getString(2)));
            }

            rs.close();
        } catch (Exception e) {
            msg = e.getLocalizedMessage();
            logger.catching(e);
        }

        return true;
    }

    @Override
    public Message enterQuestionMsg(boolean flag) {
        if (!flag) {
            return new Message(Message.ERROR, msg);
        } else {
            return null;
        }
    }

    @Override
    public ArrayList<Choice> choices() {
        return al;
    }

    @Override
    public String getQuestion() {
        if (Answer.getInstance().getA("OperationMode").equals("DOWNLOAD")) {
            return "Which NLS translation you want to download?";
        }

        if (Answer.getInstance().getA("OperationMode").equals("UPLOAD")) {
            return "Which NLS translation you want to upload?";
        }

        return super.getQuestion();
    }

    @Override
    public boolean leaveQuestion() {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            String     sql  = "SELECT b.DESCRIPTION" + "     , a.NLS_LANGUAGE" + "     , a.NLS_TERRITORY"
                              + "     , lower(ISO_LANGUAGE) || '-' || ISO_TERRITORY" + " FROM  FND_LANGUAGES a,"
                              + "       FND_LANGUAGES_TL b" + " WHERE a.INSTALLED_FLAG = 'I'"
                              + "   AND a.LANGUAGE_CODE  = ?" + "   AND a.LANGUAGE_CODE  = b.LANGUAGE_CODE"
                              + "   AND b.LANGUAGE='US'" + " ORDER BY b.DESCRIPTION";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, Answer.getInstance().getA(this));

            ResultSet rs = pstmt.executeQuery();

            rs.next();
            Answer.getInstance().putB(NLS_TRANSLATION, rs.getString(1));
            Answer.getInstance().putB(OA_NLS_LANG, rs.getString(4));

            String nlsLang = rs.getString(2) + "_" + rs.getString(3) + ".UTF8";

            Answer.getInstance().putB(NLS_SESSION_LANG, nlsLang);
            logger.debug("NLS_SESSION_LANG : " + nlsLang);
            rs.close();
        } catch (Exception e) {
            logger.catching(e);
        }

        return true;
    }

    @Override
    public String lastAction() {
        return "OperationMode";
    }

    @Override
    public String nextAction() {
        if (Answer.getInstance().getA(this) != "") {
            if (Answer.getInstance().getA("OperationMode").equals("DOWNLOAD")) {
                return "DownloadModule";
            }

            // if (Answer.getInstance().getA("OperationMode").equals("UPLOAD")) return "UploadObject";
            if (Answer.getInstance().getA("OperationMode").equals("UPLOAD")) {
                return "UploadNLSObj";
            }
        }

        return "SelectNLSLang";
    }
}
