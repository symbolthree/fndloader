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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/LDTInfoQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 2/06/17 3:37a $
 * $Revision: 5 $
******************************************************************************/

package symbolthree.oracle.fndload.fnd;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.Helper;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class LDTInfoQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/LDTInfoQuestion.java 5     2/06/17 3:37a Christopher Ho $";
    
    private String sid           = null;
    private String  creationDate = null;
    private String  fileName     = null;
    private String  language     = null;
    private String  objectDesc   = null;
    private String  objectFilter = null;
    private String  objectType   = null;
    private String  release      = null;
    private String  sourceDB     = null;
    private String  module       = null;
    private String  controlFileName = null;
    private String  controlFileVer  = null;
    private String localCtrlVer     = null;
    private String serverCtrlVer    = null;
    private boolean isNLSMode;
    private boolean isWFLoad;
    private boolean checkLCTVer     = true;
    private boolean nlsLangMatched  = true;
    
    private static String NEXT           = "NEXT";

    static final Logger logger = LogManager.getLogger(LDTInfoQuestion.class.getName());
    
    @Override
    public String getQuestion() {
    	
        String str = "This " + (isWFLoad
                                         ? "WFT"
                                         : "LDT") + " file will be uploaded to instance "
                                             + sid + ".\n";

        return str;
    }

    @Override
    public String getExplanation() {
    	
    	logger.debug("language=" + language);
    	
        File   file = new File(fileName);
        String str = null;
        
        try {
        if (!isWFLoad) {        	
          String controlFile   = Config.getInstance().getLocalControlFile(objectType);
          localCtrlVer  = Config.getInstance().getLocalLCTVer(controlFile);
          serverCtrlVer = Config.getInstance().getServerLCTVer(controlFile,  module);
        
          logger.debug("LDT = [" + controlFileVer + "]");
          logger.debug("local = [" + localCtrlVer + "]");
          logger.debug("server = [" + serverCtrlVer + "]");
        }
        
        str =       "Directory        : " + file.getParent() + "\n";
        str = str + "File             : " + file.getName() + "\n";

        str = str + "Object Type      : " + objectDesc + "\n"; 
        str = str + "DB / Ver / Lang  : " + sourceDB + " / " + ((release == null)
                ? "N/A"
                : release) + " / " + language + "\n";
        
        if ((objectFilter != null) && (creationDate != null)) {
            str = str + "Filter / Date    : " + objectFilter + " / " + creationDate + "\n";
        }
        
        if (!isWFLoad) {         
          str = str + "Config File      : " + controlFileName + "\n";;
          str = str + "Cfg File Version : (LDT)    " + controlFileVer + "\n"
        	  	    + "                 : (Local)  " + localCtrlVer + "\n"
                    + "                 : (Server) " + serverCtrlVer + "\n";
        
          if (! localCtrlVer.equals(serverCtrlVer)) {
        	  str = str + "WARNING ! Inconsistent configuration file versions !";
          }
        }

        if (isNLSMode && !isWFLoad) {
            String selectLang = Answer.getInstance().getA("SelectNLSLang");

            if (!selectLang.equals(language)) {
                str = str + "\n\n" + Answer.getInstance().getB(NLS_TRANSLATION)
                      + " translation LDT file is required. \nUnable to proceed.";
            }
        }

        if (Answer.getInstance().getA("OperationMode").equals("UPLOAD") && isNLSMode) {
            if (language.equals("US")) {
                str = str + "\nThis is not translation LDT file ! Unable to proceed.";
            }
            String selectedLang = Answer.getInstance().getA("SelectNLSLang");
            if (selectedLang.equals(language)) {
            	nlsLangMatched = false;
            	str = str + "\nNLS languages between LDT file and the one you selected are not matched.";
            }
        }
        
        } catch (Exception e) {
        	logger.catching(e);
        }

        return str;
    }

    @Override
    public boolean enterQuestion() {
        isWFLoad  = Instances.getInstance().isWFLoad();
        isNLSMode = Instances.getInstance().isNLSMode();
        logger.debug("isWFLoad=" + isWFLoad);
        logger.debug("isNLSMode=" + isNLSMode);

        sid       = Answer.getInstance().getA("SelectInstance");
        
        if (isWFLoad) checkLCTVer = false;
    	
        // read file info
        try {
            module    = Config.getInstance().getModuleByName(objectType);  // this is for DOWNLOAD
            
            fileName = Answer.getInstance().getA("UploadObject");
            logger.debug("Selected file=" + fileName);
        	
            BufferedReader br   = new BufferedReader(new FileReader(fileName));
            String         line = null;
            String         LDRCONFIG = null;
            
            while ((line = br.readLine()) != null) {
                if (line.startsWith(LDT_TAG)) {
                    StringTokenizer st = new StringTokenizer(line, "|");

                    try {
	                    st.nextToken();
	                    creationDate = st.nextToken().trim();
	                    st.nextToken();
	                    st.nextToken();
	                    objectFilter = st.nextToken().trim();
                    } catch (Exception e) {
                    	logger.debug(line + " - is not current format");
                    }
                    
                }

                if (line.startsWith("LANGUAGE")) {
                    language = line.substring(12, line.length() - 1);
                }

                if (line.startsWith("LDRCONFIG")) {
                	LDRCONFIG = line.substring(13, line.length() - 1);
                }

                if (line.startsWith("#Source") || line.startsWith("# Source")) {
                    sourceDB = line.substring(17, line.length()).trim();
                }

                if (line.startsWith("#RELEASE_NAME")) {
                    release = line.substring(14, line.length());
                }

                if (line.startsWith("DEFINE")) {
                    objectType = line.substring(7, line.length());

                    break;
                }
            }

            if (isWFLoad) {
                objectDesc = "Workflow Item Type";
            } else {
                controlFileName = LDRCONFIG.split(" ", 2)[0];
                controlFileVer  = LDRCONFIG.split(" ", 2)[1];
                objectDesc      = Config.getInstance().getDescriptionByName(objectType);
            }

            br.close();
            
            if (Answer.getInstance().getA(this) != null) {
            	if (Answer.getInstance().getA(this).equals("A") || Answer.getInstance().getA(this).equals("B")) {
                     	checkLCTVer = false;
                }
                checkLCTVer = Instances.getInstance().checkLCTVer(sid, module, objectType);
            }
            
            
        } catch (Exception e) {
            logger.catching(e);
        }

        return true;
    }

    @Override
    public ArrayList<Choice> choices() {
    	
        ArrayList<Choice> ht = new ArrayList<Choice>();
        
    	String ans = Helper.nullStr(Answer.getInstance().getA(this)); 
    	
    	if (!isNLSMode) {    	
    	  if (ans.equals(IGNORE_ONCE) || ans.equals(IGNORE_ALWAYS))  {
              ht.add(new Choice(NEXT, "Upload this file"));    		
    	  }
    	}
    	
        if (checkLCTVer && ! localCtrlVer.equals(serverCtrlVer)) {
    	  ht.add(new Choice(IGNORE_ONCE, "Ignore warning this time"));
    	  ht.add(new Choice(IGNORE_ALWAYS, "Always ignore warning for this config file"));
        
        } else {        
        
	        if (isNLSMode && nlsLangMatched) {
                ht.add(new Choice(NEXT, "Upload this file"));
	        }
	
	        if (!isNLSMode) {
	            if (language.equals("US") || language.equals("AMERICAN")) {
	                ht.add(new Choice(NEXT, "Upload this file"));
	            }
	        }
        }
        
        return ht;
    }

    @Override
    public String lastAction() {
        return "UploadObject";
    }

    @Override
    public String nextAction() {
    	String ans = Answer.getInstance().getA(this);
        if (ans.equals(NEXT)) {
        	// this DownloadObject and module are used in SaveConfigAction and FNDLOAD action
            Answer.getInstance().putA("DownloadObject", objectType);
            Answer.getInstance().putA("DownloadModule", module);
        	
            logger.debug("DownloadObject=" + objectType);

            return "ApplicationsInfo";
        
        } else {
        	
          Answer.getInstance().putA("LCTVersionDiff", ans);
          return "LDTInfo";
          
        }
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

