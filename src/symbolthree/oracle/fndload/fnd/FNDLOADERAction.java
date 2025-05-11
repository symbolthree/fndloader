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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/FNDLOADERAction.java $
 * $Author: Christopher Ho $
 * $Date: 7/06/17 11:18a $
 * $Revision: 5 $
******************************************************************************/


package symbolthree.oracle.fndload.fnd;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERActionBase;

public class FNDLOADERAction extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/FNDLOADERAction.java 5     7/06/17 11:18a Christopher Ho $";
    private String  currDir    = System.getProperty("user.dir");
    private String  errMsg     = null;
    private String  fileSep    = File.separator;
    private String  fndloadExe = "FNDLOADSO";
    private String  pathSep    = File.pathSeparator;
    private String  wfloadExe  = "WFLOAD";
    private boolean isNLSMode;
    private boolean isWFLoad;
    
    static final Logger logger = LogManager.getLogger(FNDLOADERAction.class.getName());    

    @Override
    public void execute() {
        isNLSMode = Instances.getInstance().isNLSMode();
        isWFLoad  = Instances.getInstance().isWFLoad();
        
        // construct command line
        String            opMode       = Answer.getInstance().getA("OperationMode");
        String            opObj        = Answer.getInstance().getA("OperationObject");
        String            instanceName = Answer.getInstance().getA("SelectInstance");
        String            releaseName  = Answer.getInstance().getB(RELEASE_NAME);
        String            credential   = "apps/" + Answer.getInstance().getA("DatabaseCredential") + "@" + instanceName;
        ArrayList<String> cmdArgs      = new ArrayList<String>();
        int               exitVal      = -1;

        try {
            Map<String, String> origSystemEnv = System.getenv();

            Hashtable<String, String> systemEnv = new Hashtable<String, String>();

            // fill the running env. variables with the orig. values, using variable name in uppercase
            Set<String>      envSet = origSystemEnv.keySet();
            Iterator<String> itr    = envSet.iterator();

            while (itr.hasNext()) {
                String envKey = itr.next();

                systemEnv.put(envKey.toUpperCase(), origSystemEnv.get(envKey));
            }

            // used by config file (variable substitution)
            
            String appMode = Answer.getInstance().getB(APP_MODE);
            
            String applTop = systemEnv.get("APPL_TOP");

            if ((applTop == null) || applTop.equals("")) {
            	// under client mode forcing to use program location, override any pre-defined values
            	if (appMode.equals(CLIENT_MODE)) {
                  applTop = System.getProperty("user.dir") + fileSep + "CLIENT" + fileSep + releaseName + fileSep
                          + "APPL_TOP";
            	}
            }

            String fndTop = systemEnv.get("FND_TOP");

            if ((fndTop == null) || fndTop.equals("")) {
            	// under client mode forcing to use program location, override any pre-defined values
            	if (appMode.equals(CLIENT_MODE)) {
                  fndTop = applTop + fileSep + "fnd" + fileSep + Config.releasePath(releaseName);
            	}
            }

            File file = new File(currDir, "CLIENT");

            // Standalone version
            if (file.exists() && file.isDirectory()) {

                // common system variables
                systemEnv.put("APPLMSG", "mesg");
                systemEnv.put("FND_TOP", fndTop);
                systemEnv.put("TNS_ADMIN", System.getProperty("oracle.net.tns_admin"));

                if (isNLSMode) {
                    systemEnv.put(NLS_LANG, Answer.getInstance().getB(NLS_SESSION_LANG));
                } else {
                    systemEnv.put(NLS_LANG, Answer.getInstance().getB(NLS_LANG));
                }

                systemEnv.put(NLS_DATE_FORMAT, Answer.getInstance().getB(NLS_DATE_FORMAT));
                systemEnv.put(NLS_NUMERIC_CHARACTERS, Answer.getInstance().getB(NLS_NUMERIC_CHARACTERS));
                systemEnv.put(NLS_SORT, Answer.getInstance().getB(NLS_SORT));

                String oracleHome = currDir + fileSep + "CLIENT" + fileSep + releaseName + fileSep + "ORACLE_HOME";

                systemEnv.put("ORACLE_HOME", oracleHome);

                if (releaseName.equals(RELEASE_121) || releaseName.equals(RELEASE_122)) {
                    systemEnv.put("ORA_NLS10", oracleHome + fileSep + "nls" + fileSep + "data" + fileSep + "9idata");
                }

                if (releaseName.equals(RELEASE_11i)) {
                    if (isWindows()) {
                        systemEnv.put("NLSRTL33", oracleHome + fileSep + "NLSRTL33");
                        systemEnv.put("ORA_NLS33", oracleHome + fileSep + "NLSRTL33" + fileSep + "DATA");
                    }

                    if (isUnix()) {
                        systemEnv.put("ORA_NLS",
                                      oracleHome + fileSep + "ocommon" + fileSep + "nls" + fileSep + "admin" + fileSep
                                      + "data");
                    }
                }

                // used by FNDLOAD for Windows
                if (isWindows()) {
                    String path = systemEnv.get("PATH");

                    path = currDir + fileSep + "CLIENT" + fileSep + releaseName + fileSep + "ORACLE_HOME" + fileSep
                           + "BIN" + pathSep + path;
                    systemEnv.put("PATH", path);
                }

                // used by FNDLOAD for Linux
                if (isUnix()) {
                    String ldLibPath = systemEnv.get("LD_LIBRARY_PATH");

                    ldLibPath = currDir + fileSep + "CLIENT" + fileSep + releaseName + fileSep + "ORACLE_HOME"
                                + fileSep + "BIN" + pathSep + ldLibPath;
                    systemEnv.put("LD_LIBRARY_PATH", ldLibPath);
                }
            }

            String name        = Answer.getInstance().getA("DownloadObject");
            String controlFile = Config.getInstance().getLocalControlFile(name);

            logger.debug("controlFile=" + controlFile);
            
            // show all systemEnv variables for debug
            envSet = systemEnv.keySet();
            itr    = envSet.iterator();

            logger.debug(">>> Environment Variables <<<");
            while (itr.hasNext()) {
                String envKey = itr.next();
                logger.debug(envKey + "=" + systemEnv.get(envKey));
            }

            cmdArgs.add(fndTop + fileSep + "bin" + fileSep + getExecProgram());

            // construct arguments
            cmdArgs.add(credential);
            
            /* FNDLOADSO does not need the 0 Y arguments, but WFLOAD need them */

            if (isWFLoad) {
              cmdArgs.add("0");
              cmdArgs.add("Y");
            }

            // if (actionType.equals("DOWNLOAD") || actionType.equals("DOWNLOAD_NLS") || actionType.equals("DOWNLOAD_WF")) cmdArgs.add("DOWNLOAD");
            if (opMode.equals("DOWNLOAD")) {
                cmdArgs.add("DOWNLOAD");
            }

            if (opMode.equals("UPLOAD")) {
                cmdArgs.add("UPLOAD");
            }

            if (!isWFLoad) {
                cmdArgs.add(controlFile);
            }

            // if (actionType.equals("UPLOAD") || actionType.equals("UPLOAD_NLS") || actionType.equals("UPLOAD_WF")) {
            if (opMode.equals("UPLOAD")) {
                cmdArgs.add(Answer.getInstance().getA("UploadObject"));
                if (!isWFLoad) cmdArgs.add("CUSTOM_MODE=FORCE");
            }

            //cmdArgs.add("FLEX_DEBUG_FLAG=Y");
            //cmdArgs.add("DETAIL_LOG=TRUE");
            
            // if (actionType.equals("UPLOAD_NLS")) {
            if (opMode.equals("UPLOAD") && opObj.equals("NLS")) {
                cmdArgs.add("-UPLOAD_MODE=NLS");
                cmdArgs.add("CUSTOM_MODE=FORCE");
                cmdArgs.add("WARNINGS=TRUE");
            }

            // if (actionType.equals("DOWNLOAD") || actionType.equals("DOWNLOAD_NLS") || actionType.equals("DOWNLOAD_WF")) {
            if (opMode.equals("DOWNLOAD")) {
                String outputFile = Answer.getInstance().getB("LDT_DIRECTORY") + fileSep
                                    + Answer.getInstance().getB("LDT_FILE");

                cmdArgs.add(outputFile);

                String keyValue = Answer.getInstance().getA("DownloadFilter");

                if (isWFLoad) {
                    cmdArgs.add(keyValue);
                } else {
                    cmdArgs.add(name);

                    String key = Config.getInstance().getKeyByName(name);

                    cmdArgs.add(key + "=" + keyValue);
                }
            }

            ProcessBuilder pb = new ProcessBuilder(cmdArgs);

            logger.debug(maskPassword(pb.command().toString()));
            
            Map<String, String> envVars = pb.environment();

            envVars.clear();

            /*            
            envSet = systemEnv.keySet();
            itr    = envSet.iterator();

            while (itr.hasNext()) {
                String envKey = itr.next();
                Helper.log(Helper.LOG_DEBUG, envKey + "=" + systemEnv.get(envKey));
            }
		    */

            envVars.putAll(systemEnv);

            // set log directory
            File logDir = new File(FNDLOADER_APPLICATION_DIR, "log");

            if (!logDir.exists() || !logDir.isDirectory()) {
                logDir.mkdirs();
            }

            // exec.setWorkingDirectory(logDir);
            pb.directory(logDir);
            logger.debug("log directory=" + logDir.getAbsolutePath());
            pb.redirectErrorStream(true);

            // Helper.log(Helper.LOG_DEBUG, "Executable=" + cl.getExecutable());
            // Helper.log(Helper.LOG_DEBUG, "Arguments=" + maskPassword(Arrays.toString(cl.getArguments())));
            // Helper.log(Helper.LOG_DEBUG, "Program start...");

            Process        shell  = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            String         line;

            while ((line = reader.readLine()) != null) {
                logger.info("FNDLOAD Output >> " + line);
            }

            exitVal = shell.waitFor();
            reader.close();
            shell.destroy();

            // exitVal = exec.execute(cl, systemEnv);
            // Helper.log(Helper.LOG_DEBUG, stdout.toString());
            logger.debug("Program done");
            
        } catch (Throwable e) {
            logger.catching(e);
            errMsg = e.getLocalizedMessage();
        }

        logger.debug("Exit value is " + exitVal);
        Answer.getInstance().putB(EXIT_VALUE, String.valueOf(exitVal));
    }

    @Override
    public boolean showSystemOutput() {
        return true;
    }

    @Override
    public Message actionMessage() {
        if (errMsg != null) {
            return new Message(Message.ERROR, errMsg);
        } else {
            return null;
        }
    }

    @Override
    public String nextAction() {
        return "PostFNDLOAD";
    }

    private String maskPassword(String line) {
        String          output = "";
        StringTokenizer st     = new StringTokenizer(line, " ");

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (token.indexOf("apps/") >= 0) {
                String sid = token.split("@")[1];

                output = output + " apps/*******@" + sid;
            } else {
                output = output + " " + token;
            }
        }

        return output;
    }

    private boolean isWindows() {
        if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") >= 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isUnix() {
        if ((System.getProperty("os.name").toUpperCase().indexOf("NIX") >= 0)
                || (System.getProperty("os.name").toUpperCase().indexOf("NUX") >= 0)) {
            return true;
        } else {
            return false;
        }
    }

    private String getExecProgram() {
        String execProgram = null;

        if (isWindows()) {
            execProgram = isWFLoad
                          ? (wfloadExe + ".exe")
                          : (fndloadExe + ".exe");
        }

        if (isUnix()) {
            execProgram = isWFLoad
                          ? wfloadExe
                          : fndloadExe;
        }

        return execProgram;
    }
    
}

