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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/SelectInstanceQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 1/24/17 9:51a $
 * $Revision: 3 $
******************************************************************************/

package symbolthree.oracle.fndload.common;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import symbolthree.oracle.fndload.*;

public class SelectInstanceQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/SelectInstanceQuestion.java 3     1/24/17 9:51a Christopher Ho $";
    private Hashtable<String, String> allTNSEntries     = null;
    private String                    appMode           = null;
    private String                    rtnMsg            = null;
    private String                    tnsnamesFile      = null;
    private boolean                   tnsnamesFileFound = true;
    private ArrayList<Choice>         al                = new ArrayList<Choice>();

    static final Logger logger = LogManager.getLogger(SelectInstanceQuestion.class.getName());    
    
    public SelectInstanceQuestion() {
        appMode = Answer.getInstance().getB(APP_MODE);
    }

    @Override
    public boolean enterQuestion() {
     	try {
  	      DBConnection.getInstance().disconnect();
  	    } catch (Exception e) {
  		// do nothing
  		  logger.info(e.getMessage());
		}
  	
        Answer.getInstance().putA(this, null);
        Answer.getInstance().putA("DatabaseCredential", null);

        try {
            allTNSEntries = Instances.getInstance().getAllTNSEntries();

            Enumeration<String> keys = allTNSEntries.keys();
            Vector<String>      v    = new Vector<String>();

            while (keys.hasMoreElements()) {
                String instanceName = keys.nextElement();

                v.add(instanceName);
            }

            Collections.sort(v);

            for (int i = 0; i < v.size(); i++) {
                al.add(new Choice(v.get(i), v.get(i)));
            }

            tnsnamesFile = Instances.getInstance().getTNSNameFile();
        } catch (FNDLOADERException e) {
            rtnMsg            = e.getLocalizedMessage();
            tnsnamesFileFound = false;
        }

        if (appMode.equals(CLIENT_MODE)) {
            return true;
        } else if (appMode.equals(SERVER_MODE)) {

            // 11i = TWO_TASK, R12 = DB_NAME
            logger.debug("Looking up database SID...");

            String instanceName = null;

            instanceName = System.getenv().get("TWO_TASK");

            if ((instanceName == null) || instanceName.equals("")) {
                instanceName = System.getenv().get("DB_NAME");
            }

            logger.debug("database SID=" + instanceName);            
            Answer.getInstance().putA(this, instanceName);

            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getQuestion() {
        if (tnsnamesFileFound) {
            return "Select a database instance:";
        } else {
            return rtnMsg + "\n";
        }
    }

    @Override
    public String getExplanation() {
        if (tnsnamesFileFound) {
            return "\nTNSNAMES: " + tnsnamesFile;
        } else {
            return "Please do ONE of the following to resolve this issue:\n" 
                  + "- Set system variable ORACLE_HOME, where tnsnames.ora is located in %ORACLE_HOME%\\NETWORK\\ADMIN\n"
                  + "- Set system variable TNS_ADMIN to a directory which contains tnsnames.ora\n" 
                  + "- Put tnsnames.ora under directory " + System.getProperty("user.dir");
        }
    }

    @Override
    public ArrayList<Choice> choices() {
        return al;
    }

    @Override
    public String nextAction() {
        String instanceName = null;

        if (tnsnamesFileFound) {
            instanceName = Answer.getInstance().getA(this);

            String instanceParams = allTNSEntries.get(instanceName);

            // remove the leading "([INSTANCE NAME]=" and trailing ")"
            instanceParams = instanceParams.substring(instanceName.length() + 2, instanceParams.length() - 1);
            Answer.getInstance().putB(TNS_INSTANCE_PARAMS, instanceParams);
            logger.debug("TNS INSTANCE = " + instanceName);
            logger.debug("TNS PARAMS   = " + instanceParams);
            
            return "CheckCredential";
        } else {
            return null;
        }
    }

    @Override
    public String getTitle() {
        return "FNDLOADER " + Config.getInstance().getVersion() + " [" + Answer.getInstance().getB(APP_MODE)
               + "]";
    }

    @Override
    public boolean lineWrap() {
        return false;
    }

    @Override
    public boolean showProgress() {
        return true;
    }
}
