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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/common/OperationObjectQuestion.java $
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
import java.sql.ResultSet;

import java.util.ArrayList;

import symbolthree.oracle.fndload.DBConnection;

public class OperationObjectQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/common/OperationObjectQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private boolean nlsPatched = false;
    static final Logger logger = LogManager.getLogger(OperationObjectQuestion.class.getName());
    
    public OperationObjectQuestion() {}

    @Override
    public String getQuestion() {
        return "What object you want to " + Answer.getInstance().getA("OperationMode") + " for instance "
               + Answer.getInstance().getA("SelectInstance") + " ?";
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        al.add(new Choice("FND", "FNDLOAD Object"));
        al.add(new Choice("OA", "OA Framework Object"));
        al.add(new Choice("WF", "Workflow"));
        al.add(new Choice("XDO", "XML Publisher"));

        if (nlsPatched) {
            al.add(new Choice("NLS", "NLS Translation"));
        }

        return al;
    }

    @Override
    public boolean enterQuestion() {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            String     sql  = "SELECT COUNT(*) FROM FND_LANGUAGES WHERE INSTALLED_FLAG='I'";
            ResultSet  rs   = conn.createStatement().executeQuery(sql);

            rs.next();

            int i = rs.getInt(1);

            if (i > 0) {
                nlsPatched = true;
            }

            rs.close();
        } catch (Exception e) {
         	logger.catching(e);
        }

        return true;
    }

/*
    @Override
    public boolean leaveQuestion() {
        if (Answer.getInstance().getA(this).equals("XDO")) {
                return false;
        } else {
                return true;
        }
    }
*/
    @Override
    public Message leaveQuestionMsg(boolean flag) {
        if (!flag) {
            return new Message(Message.ERROR, "Sorry This feature has not implemented yet.");
        } else {
            return null;
        }
    }

    @Override
    public String lastAction() {
        return "SelectInstance";
    }

    @Override
    public String nextAction() {
        String _mode   = Answer.getInstance().getA("OperationMode");
        String _object = Answer.getInstance().getA(this);

        if (_mode.equals("DOWNLOAD")) {
            if (_object.equals("FND")) {
                return "DownloadModule";
            }

            if (_object.equals("OA")) {
                return "DownloadOAType";
            }

            if (_object.equals("WF")) {
                return "DownloadFilter";
            }

            if (_object.equals("XDO")) {
                return "XDOObject";
            }

            if (_object.equals("NLS")) {
                return "SelectNLSLang";
            }
        }

        if (_mode.equals("UPLOAD")) {
            if (_object.equals("FND")) {
                return "UploadObject";
            }

            if (_object.equals("OA")) {
                return "UploadOAPath";
            }

            if (_object.equals("WF")) {
                return "UploadObject";
            }

            if (_object.equals("XDO")) {
                return "XDOObject";
            }

            if (_object.equals("NLS")) {
                return "SelectNLSLang";
            }
        }

        return "OperationMode";
    }
}

