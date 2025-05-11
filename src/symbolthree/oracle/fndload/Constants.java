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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/Constants.java $
 * $Author: Christopher Ho $
 * $Date: 1/24/17 9:51a $
 * $Revision: 3 $
******************************************************************************/



package symbolthree.oracle.fndload;

import java.io.File;

public interface Constants {

    // internal properties
    public static final String APP_MODE                  = "APP_MODE";
    public static final String CHANGE_DIRECTORY          = "CHANGE_DIRECTORY";
    public static final String CHANGE_FILE               = "CHANGE_FILE";
    public static final String CLIENT_MODE               = "Standalone Version";
    public static final String FNDLOADER_APPLICATION_DIR = System.getProperty("user.home") + File.separator + "symbolthree" + File.separator + "fndloader";
    public static final String CONFIG_FILENAME           = "FNDLOADER.xml";
    public static final int    ENCRYPTED_PASSWORD_LENGTH = 100;
    public static final String PASSWORD_ATTEMPT          = "PASSWORD_ATTEMPT";
    public static final String ASK_PASSWORD              = "ASK_PASSWORD";    
    
    public static final String APPL_CONFIG_WIN_KEY       = "SYMBOLTHREE";
    
    public static final String EXIT_VALUE                = "EXIT_VALUE";
    public static final String INSTANCES_FILENAME        = "INSTANCES.xml";
    public static final String LAST_PAGE                 = "LAST_PAGE";
    public static final String LDT_ACTION                = "LDT_ACTION";
    public static final String LDT_DIRECTORY             = "LDT_DIRECTORY";
    public static final String LDT_FILE                  = "LDT_FILE";
    public static final String LDT_TAG                   = "#FNDLOADER";
    
    public static final int    MAX_PASSWORD_ATTEMPT      = 3;
    public static final String NEXT_PAGE                 = "NEXT_PAGE";
    public static final String NLS_DATE_FORMAT           = "NLS_DATE_FORMAT";
    public static final String NLS_INSTALLED             = "NLS_INSTALLED";
    public static final String NLS_LANG                  = "NLS_LANG";
    public static final String NLS_NUMERIC_CHARACTERS    = "NLS_NUMERIC_CHARACTERS";
    public static final String NLS_SESSION_LANG          = "NLS_SESSION_LANG";
    public static final String NLS_SORT                  = "NLS_SORT";
    public static final String NLS_TRANSLATION           = "NLS_TRANSLATION";
    
    // OA Framework
    public static final String OA_ACTION          = "OA_ACTION";
    public static final String OA_NLS_LANG        = "OA_NLS_LANG";
    public static final String OA_ROOT_DIR        = "OA_ROOT_DIR";
    public static final String OBJECT_TOTAL_COUNT = "OBJECT_TOTAL_COUNT";
    public static final String PASSWORD_SEED      = "SYMBOLTHREE";
    public static final String PROGRAM_VERSION    = "4.0";

    // internal values
    public static final String RELEASE_11i         = "11.5.0";
    public static final String RELEASE_121         = "12.1.0";
    public static final String RELEASE_122         = "12.2.0";
    public static final String RELEASE_NAME        = "RELEASE_NAME";
    public static final String SELECT_FILE         = "SELECT_FILE";
    public static final String SERVER_MODE         = "Server Addon Version";
    public static final String TNSNAMES            = "tnsnames.ora";
    public static final String TNS_INSTANCE_PARAMS = "TNS_INSTANCE_PARAMS";

    // Version Checking
    public static final String LCT_LOCAL_VER       = "LCT_LOCAL_VER";
    public static final String LCT_SERVER_VER      = "LCT_SERVER_VER";
    public static final String LCT_FILE            = "LCT_FILE";
    public static final String CHECK_LCT_VER       = "CHECK_LCT_VER";
    
    public static final String IGNORE_ONCE         = "IGNORE_ONCE";
    public static final String IGNORE_ALWAYS       = "IGNORE_ALWAYS";    
    
    // XDO
    public static final String XDO_ACTION      = "XDO_ACTION";
    public static final String XDO_DIRECTORY   = "XDO_DIRECTORY";
    public static final String XDO_OUTPUT_FILE = "XDO_OUTPUT_FILE";
}


