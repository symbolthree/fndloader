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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/CheckCredentialAction.java $
 * $Author: Christopher Ho $
 * $Date: 11/14/16 1:27a $
 * $Revision: 2 $
******************************************************************************/



package symbolthree.oracle.fndload.common;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Helper;
import symbolthree.flower.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.sql.SQLException;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.Instances;

public class CheckCredentialAction extends FNDLOADERActionBase {
	
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/CheckCredentialAction.java 2     11/14/16 1:27a Christopher Ho $";
    private Message rtnMsg = null;
    static final Logger logger = LogManager.getLogger(CheckCredentialAction.class.getName());

    
    @Override
    public void execute() {
        System.out.println("Checking database connection and credentials...");
        logger.debug("CheckCredentialAction - execute");
        
        String sid       = Answer.getInstance().getA("SelectInstance");
        String tnsParams = Answer.getInstance().getB(TNS_INSTANCE_PARAMS);
        String jdbcurl   = "jdbc:oracle:thin:@" + tnsParams;
        String username  = "APPS";
        String password  = null;

        password = Answer.getInstance().getA("DatabaseCredential");

        if (password == null) {
            logger.debug("password is not entered from user");
            password = Instances.getInstance().getPassword(sid);
        }

        if (password == null) {
            logger.debug("no password found from instances.xml");        	
            Answer.getInstance().putB(ASK_PASSWORD, "TRUE");

            return;
        }

        try {
            logger.debug("connection JDBC URL: " + jdbcurl);
            DBConnection.getInstance(jdbcurl, username, password).getConnection();
            Answer.getInstance().putA("DatabaseCredential", password);
        } catch (SQLException sqle) {
            logger.catching(sqle);

            int errCode = sqle.getErrorCode();

            if (errCode == 1017) {    // invalid password
                rtnMsg = new Message(Message.ERROR, "Invalid password");
            } else {
                rtnMsg = new Message(Message.ERROR, sqle.getLocalizedMessage());
                Answer.getInstance().putB(PASSWORD_ATTEMPT, "99");
            }

            setKey(true);

            return;
        }

        setKey(false);
    }

    @Override
    public Message actionMessage() {
        return rtnMsg;
    }

    private void setKey(boolean flag) {
        int attempt = getNoOfAttempt();

        logger.debug("password attempt:" + attempt);
        Answer.getInstance().putB(PASSWORD_ATTEMPT, String.valueOf(attempt + 1));
        Answer.getInstance().putB(ASK_PASSWORD, flag
                ? "TRUE"
                : "FALSE");
    }

    @Override
    public String nextAction() {
        if (getNoOfAttempt() <= 2) {
            return "DatabaseCredential";
        } else {

            // clear all settings
            Answer.getInstance().putA("DatabaseCredential", null);
            Answer.getInstance().putA("SelectInstance", null);
            Answer.getInstance().putB(PASSWORD_ATTEMPT, "0");
            Answer.getInstance().putB(ASK_PASSWORD, "TRUE");

            return "SelectInstance";
        }
    }

    private int getNoOfAttempt() {
        return Helper.getInt(Answer.getInstance().getB(PASSWORD_ATTEMPT), 0);
    }
}
