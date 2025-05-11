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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/DownloadOAObjectQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 2/06/17 3:37a $
 * $Revision: 3 $
******************************************************************************/

package symbolthree.oracle.fndload.oa;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class DownloadOAObjectQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/DownloadOAObjectQuestion.java 3     2/06/17 3:37a Christopher Ho $";
    private String  oaDocType  = null;
    private boolean oaObjExist = false;
    static final Logger logger = LogManager.getLogger(DownloadOAObjectQuestion.class.getName());
    
    // private boolean moreThanOneSelected = false;

    public DownloadOAObjectQuestion() {
        oaDocType = Answer.getInstance().getA("DownloadOAType");
    }

    @Override
    public String getQuestion() {
        String _type;

        if (Instances.getInstance().isNLSMode()) {
            _type = Answer.getInstance().getB(NLS_TRANSLATION) + " Translation";
        } else {
            _type = (oaDocType.equals("C"))
                    ? "Personalization"
                    : "Page /Region Definition";
        }

        return "You are going to download OA Framework " + _type + " from instance "
               + Answer.getInstance().getA("SelectInstance")
               + ". Please enter the Page / Region name (e.g. SalAdminPG).";
    }

    @Override
    public String getExplanation() {
        return "\nNo need to enter the document path. Wildcard (%) is accepted. Case is not sensitive.";
    }

    @Override
    public boolean isFreeTextInput() {
        return true;
    }

    @Override
    public int minTextInputLength() {
        return 3;
    }

    @Override
    public boolean isMultipleChoices() {
        return false;
    }

    public String lastAction() {
        return "DownloadOAObj";
    }

    @Override
    public boolean leaveQuestion() {
        String _object = Answer.getInstance().getA(this);

        _object = _object.toUpperCase();

        try {
            Connection        conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps   = conn.prepareStatement(getSQL());

            ps.setString(1, _object);

            ResultSet rs = ps.executeQuery();

            rs.next();

            int i = rs.getInt(1);

            rs.close();
            ps.close();

            if (i > 0) {
                oaObjExist = true;
            }

            logger.debug("No of related OA objects found:" + i);
        } catch (Exception e) {
            logger.catching(e);
        }

        return oaObjExist;
    }

    @Override
    public Message leaveQuestionMsg(boolean flag) {
    	
    	logger.debug("leaveQuestion flag = " + flag);
    	
        Message message = null;

        if (!flag) {
            if (oaDocType.equals("C")) {
                message = new Message(Message.ERROR, "No personalization found or invalid page name.");
            } else if (oaDocType.equals("P")) {
                message = new Message(Message.ERROR, "No page or region can be found.");
            }
        }

        return message;
    }

    public String nextAction() {
        if (oaObjExist) {
            return "DownloadOASelect";
        }

        if (!oaObjExist) {
            return "DownloadOAObj";
        }

        return "DownloadOAObj";
    }

    private String getSQL() {
        String sql1 =   "select count(*) "
        		      + "  from jdr_paths jp "
        		      + "  where jp.path_type ='DOCUMENT'"
                      + "    and upper(jp.path_name) like ?" 
        		      + "    and not exists ("
                      + "      select 1 from jdr_attributes ja "
                      + "       where ja.att_comp_docid = jp.path_docid"
                      + "         and ja.att_name       = 'customizes')";
        
        String sql2 =   "SELECT count(*) "
        		      + "  FROM jdr_paths jp "
        		      + "     , jdr_attributes ja "
                      + " WHERE jp.path_docid       = ja.att_comp_docid"
                      + "   AND upper(jp.path_name) like ? "
                      + "   AND ja.att_comp_seq     = 0 " 
                      + "   AND ja.att_name         = 'customizes' ";
        
        String sql3 =   "select count(*)" 
        		      + "  from jdr_paths jp" 
        		      + "  where jp.path_type ='DOCUMENT'"
                      + "    and upper(jp.path_name) like ?"
                      + "    and not exists ("
                      + "       select 1 from jdr_attributes ja "
                      + "        where ja.att_comp_docid = jp.path_docid"
                      + "          and ja.att_name       = 'customizes')"
                      + "    and exists ("
                      + "       select 1 from jdr_attributes_trans jat"
                      + "        where jat.atl_comp_docid=jp.path_docid"
                      + "          and jat.atl_lang='"
                      + Answer.getInstance().getB(OA_NLS_LANG) + "')";

        if (Instances.getInstance().isNLSMode()) {
            return sql3;
        } else {
            if (oaDocType.equals("P")) {
                return sql1;
            }

            if (oaDocType.equals("C")) {
                return sql2;
            }
        }

        return null;
    }
}

