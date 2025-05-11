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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/common/ServerClientModeAction.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/

package symbolthree.oracle.fndload.common;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------


import java.io.File;
import java.util.Map;

import symbolthree.oracle.fndload.FNDLOADERException;
import symbolthree.oracle.fndload.Instances;

public class ServerClientModeAction extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/common/ServerClientModeAction.java 1     11/06/16 1:12a Christopher Ho $";
    private Message msg = null;
    static final Logger logger = LogManager.getLogger(ServerClientModeAction.class.getName());
    
    @Override
    public void execute() {

        // if APPL_TOP and BIN directories exist, this is client mode
        String currDir = System.getProperty("user.dir");
        File   file    = new File(currDir, "CLIENT");

        if (file.exists() && file.isDirectory()) {
            Answer.getInstance().putB(APP_MODE, CLIENT_MODE);
        } else {
            Answer.getInstance().putB(APP_MODE, SERVER_MODE);
        }
    }

    @Override
    public Message actionMessage() {
        String appMode = Answer.getInstance().getB(APP_MODE);

        logger.debug("APP_MODE=" + appMode);
        
        try {
            if (appMode.equals(SERVER_MODE)) {
                Map<String, String>   env = System.getenv();
                String s   = (String) env.get("CONTEXT_NAME");

                if ((s == null) || s.equals("")) {
                    msg = new Message(
                        Message.ERROR,
                        "CONTEXT_NAME system variable is not found. Make sure you run this program in APPL_TOP environment.");
                } else {

                    // get the database SID and forward to password check
                    String sid = s.split("_")[0];

                    Instances.getInstance().getAllTNSEntries();
                    Answer.getInstance().putA("SelectInstance", sid);
                }
            }
        } catch (FNDLOADERException fe) {
            msg = new Message(Message.ERROR, fe.getMessage());
        }

        return msg;
    }

    @Override
    public String nextAction() {
        if (msg != null) {
            System.exit(1);
        }

        return "SelectInstance";
    }
}
