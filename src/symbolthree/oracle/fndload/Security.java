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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/Security.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/

package symbolthree.oracle.fndload;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.lang.reflect.Method;

public class Security implements Constants {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/Security.java 1     11/06/16 1:12a Christopher Ho $";
    private static Security security      = null;
    private String          decryptMethod = "";
    private String          encryptMethod = "";
    private String          securityClass = "";
    static final Logger logger = LogManager.getLogger(Security.class.getName());
    
    protected Security() {
		Class[] params = new Class[3];

        params[0] = String.class;
        params[1] = Integer.TYPE;
        params[2] = String.class;

        try {
            Class<?> secClass = Class.forName("oracle.apps.fnd.security.AolSecurity");

            secClass.getDeclaredMethod("encrypt", params);
            securityClass = "oracle.apps.fnd.security.AolSecurity";
            encryptMethod = "encrypt";
            decryptMethod = "decrypt";
        } catch (Exception e) {
            logger.debug("oracle.apps.fnd.security.AolSecurity not found or incorrect method");
        }

        if (securityClass.equals("")) {
            try {
                Class<?> secClass = Class.forName("oracle.apps.fnd.security.Security");

                secClass.getDeclaredMethod("encrypt", params);
                securityClass = "oracle.apps.fnd.security.Security";
                encryptMethod = "encrypt";
                decryptMethod = "decrypt";
            } catch (Exception e) {
                logger.debug("oracle.apps.fnd.security.Security (server) not found or incorrect method");
            }
        }

        if (securityClass.equals("")) {
            try {
                Class<?> secClass = Class.forName("oracle.apps.fnd.security.Security");

                secClass.getDeclaredMethod("f", params);
                securityClass = "oracle.apps.fnd.security.Security";
                encryptMethod = "f";
                decryptMethod = "k";
            } catch (Exception e) {
                logger.debug("oracle.apps.fnd.security.Security (fndext) not found or incorrect method");
            }
        }
    }

    public static Security getInstance() {
        if (security == null) {
            security = new Security();
        }

        return security;
    }

    protected String encryptPwd(String str) {
        String pwd = str;

        try {
            Class<?>   secClass = Class.forName(securityClass);
            Object  secObj   = secClass.newInstance();
            Class[] params   = new Class[3];

            params[0] = String.class;
            params[1] = Integer.TYPE;
            params[2] = String.class;

            Object[] objs   = { PASSWORD_SEED, new Integer(ENCRYPTED_PASSWORD_LENGTH), str };
            Method   method = secClass.getDeclaredMethod(encryptMethod, params);

            pwd = (String) method.invoke(secObj, objs);
        } catch (Exception e) {
            logger.catching(e);
        }

        return pwd;
    }

    protected String decryptPwd(String hash) {
        String pwd = hash;

        logger.debug("securityClass=" + securityClass);
        
        try {
            Class<?>   secClass = Class.forName(securityClass);
            Object  secObj   = secClass.newInstance();
            Class[] params   = new Class[2];

            params[0] = String.class;
            params[1] = String.class;

            Object[] objs   = { PASSWORD_SEED, hash };
            Method   method = secClass.getDeclaredMethod(decryptMethod, params);

            pwd = (String) method.invoke(secObj, objs);
        } catch (Exception e) {
            logger.catching(e);
        }

        return pwd;
    }
}
