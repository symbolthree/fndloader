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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/CheckRegistryAction.java $
 * $Author: Christopher Ho $
 * $Date: 11/14/16 1:28a $
 * $Revision: 2 $
******************************************************************************/


package symbolthree.oracle.fndload.fnd;

import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.common.FNDLOADERActionBase;

import org.apache.logging.log4j.Logger;

import com.sun.jna.platform.win32.WinReg;

import org.apache.logging.log4j.LogManager;

//~--- non-JDK imports --------------------------------------------------------

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;

import symbolthree.flower.Answer;

public class CheckRegistryAction extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/CheckRegistryAction.java 2     11/14/16 1:28a Christopher Ho $";
    private String  nls_date_format        = null;
    private String  nls_lang               = null;
    private String  nls_numeric_characters = null;
    private String  nls_sort               = null;
    private String  releaseName            = null;
	private String  rootKey                = null;
	private WinReg.HKEY HKEA               = WinReg.HKEY_LOCAL_MACHINE;
	
	
    static final Logger logger = LogManager.getLogger(CheckRegistryAction.class.getName());
    
    @Override
    public boolean enterAction() {
        if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") >= 0) {

        	releaseName            = Answer.getInstance().getB(RELEASE_NAME);
            nls_lang               = Answer.getInstance().getB(NLS_LANG);
            nls_sort               = Answer.getInstance().getB(NLS_SORT);
            nls_date_format        = Answer.getInstance().getB(NLS_DATE_FORMAT);
            nls_numeric_characters = Answer.getInstance().getB(NLS_NUMERIC_CHARACTERS);

            String _release = Config.releasePath(releaseName);
            
            // 32bit os + 32 bit jre
    		if (Config.winArch().equals("32"))  rootKey = "SOFTWARE\\ORACLE\\APPLICATIONS\\" + _release;
    		
    		if (Config.winArch().equals("64") && System.getProperty("os.arch").equals("x86")) {
    			rootKey = "SOFTWARE\\ORACLE\\APPLICATIONS\\" + _release;            
    		}

    		if (Config.winArch().equals("64") && System.getProperty("os.arch").equals("amd64")) {
    			rootKey = "SOFTWARE\\Wow6432Node\\ORACLE\\APPLICATIONS\\" + _release;            
    		}
           
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void execute() {
      try {
       	if (! checkKeyExists()) {
       		createRegistry();
       		setNLSValues();
       	}
      } catch (Win32Exception e) {
    	  logger.catching(e);
      }
    }

    
    private boolean checkKeyExists() throws Win32Exception {
    	boolean boo = false;
    	
    	boo = Advapi32Util.registryKeyExists(HKEA, rootKey);
    	
    	if (! boo) {
    		logger.debug("Registry Key not exist - " + rootKey);
    		return false;
    	}
    	
    	boo = Advapi32Util.registryValueExists(HKEA, rootKey, "APPL_CONFIG");

    	if (! boo) {
    		logger.debug("Registry Key Value not exist - " + rootKey + " , " + "APPL_CONFIG");
    		return false;
    	}
    	
    	String str = Advapi32Util.registryGetStringValue(HKEA, rootKey, "APPL_CONFIG");
    	if (str != null && str.equals(APPL_CONFIG_WIN_KEY)) {
    		logger.debug("Registry Key Value exists and correct");    		
    		return true;
    	} else {
  		    logger.debug("Registry Key Value exists but  incorrect");    		
    		return false;
    	}
    }

    private void createRegistry() throws Win32Exception {
		Advapi32Util.registryCreateKey(HKEA, rootKey + "\\" + APPL_CONFIG_WIN_KEY);
		Advapi32Util.registrySetStringValue(HKEA, rootKey, "APPL_CONFIG", APPL_CONFIG_WIN_KEY);
        logger.debug("Registry key created");
    }    
    
    private void setNLSValues() throws Win32Exception {
    	
    	String subKey = rootKey + "\\" + APPL_CONFIG_WIN_KEY;
    	
		Advapi32Util.registryCreateKey(HKEA, subKey);
		
    	Advapi32Util.registrySetStringValue(HKEA, subKey, "NLS_LANG", nls_lang);
    	Advapi32Util.registrySetStringValue(HKEA, subKey, "NLS_SORT", nls_sort);
    	Advapi32Util.registrySetStringValue(HKEA, subKey, "NLS_NUMERIC_CHARACTERS", nls_numeric_characters);
    	Advapi32Util.registrySetStringValue(HKEA, subKey, "NLS_DATE_FORMAT", nls_date_format);

        logger.debug("Registry keys saved");
    }

    @Override
    public String nextAction() {
        return "SaveConfig";
    	//return "CheckLCTVerAction";
    }
}
