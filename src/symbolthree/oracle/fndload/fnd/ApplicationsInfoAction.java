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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/ApplicationsInfoAction.java $
 * $Author: Christopher Ho $
 * $Date: 11/14/16 1:28a $
 * $Revision: 2 $
******************************************************************************/


package symbolthree.oracle.fndload.fnd;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.sql.*;

import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERActionBase;

public class ApplicationsInfoAction extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/ApplicationsInfoAction.java 2     11/14/16 1:28a Christopher Ho $";
    static final Logger logger = LogManager.getLogger(ApplicationsInfoAction.class.getName());
    
    @Override
    public boolean enterAction() {
        try {
            String sid         = Answer.getInstance().getA("SelectInstance");
            
            String releaseName = Instances.getInstance().getReleaseName(sid);
            
            if (!releaseName.equals("")) {

                // instance info already exists...no need to query from DB
                Answer.getInstance().putB(RELEASE_NAME, releaseName);
                logger.debug(RELEASE_NAME + "(instances file) = " + releaseName);
                Answer.getInstance().putB(NLS_LANG, Instances.getInstance().getNLSParam(sid, NLS_LANG));
                Answer.getInstance().putB(NLS_SORT, Instances.getInstance().getNLSParam(sid, NLS_SORT));
                Answer.getInstance().putB(NLS_DATE_FORMAT, Instances.getInstance().getNLSParam(sid, NLS_DATE_FORMAT));
                Answer.getInstance().putB(NLS_NUMERIC_CHARACTERS,
                                          Instances.getInstance().getNLSParam(sid, NLS_NUMERIC_CHARACTERS));

                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.catching(e);
            return true;
        }
    }

    @Override
    public void execute() {
        String    nls_lang               = null;
        String    nls_language           = null;
        String    nls_territory          = null;
        String    nls_sort               = null;
        String    nls_characterset       = null;
        String    nls_date_format        = null;
        String    nls_numeric_characters = null;
        String    sql                    = "";
        String    releaseName            = "";
        ResultSet rs;

        try {
            releaseName = Config.getInstance().getReleaseNameFromDB();
            
            Connection conn = DBConnection.getInstance().getConnection();

            Answer.getInstance().putB(RELEASE_NAME, releaseName);
            logger.debug(RELEASE_NAME + "(database) = " + releaseName);

            // NLS info
            sql = "SELECT PARAMETER, VALUE FROM NLS_DATABASE_PARAMETERS";
            rs  = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                if (rs.getString(1).equals("NLS_LANGUAGE")) {
                    nls_language = rs.getString(2);
                } else if (rs.getString(1).equals("NLS_TERRITORY")) {
                    nls_territory = rs.getString(2);
                } else if (rs.getString(1).equals("NLS_CHARACTERSET")) {
                    nls_characterset = rs.getString(2);
                } else if (rs.getString(1).equals(NLS_SORT)) {
                    nls_sort = rs.getString(2);
                } else if (rs.getString(1).equals(NLS_DATE_FORMAT)) {
                    nls_date_format = rs.getString(2);
                } else if (rs.getString(1).equals(NLS_NUMERIC_CHARACTERS)) {
                    nls_numeric_characters = rs.getString(2);
                }
            }

            rs.close();
            nls_lang = nls_language + "_" + nls_territory + "." + nls_characterset;
            Answer.getInstance().putB(NLS_LANG, nls_lang);
            Answer.getInstance().putB(NLS_SORT, nls_sort);
            Answer.getInstance().putB(NLS_DATE_FORMAT, nls_date_format);
            Answer.getInstance().putB(NLS_NUMERIC_CHARACTERS, nls_numeric_characters);
            logger.debug("NLS Parameters: [" + nls_lang + "] [" + nls_sort + "] [" + NLS_DATE_FORMAT + "] ["
                    + NLS_NUMERIC_CHARACTERS + "]");
            
            // APPS installed NLS
            sql = "SELECT COUNT(*) FROM FND_LANGUAGES WHERE INSTALLED_FLAG='I'";
            rs  = conn.createStatement().executeQuery(sql);
            rs.next();

            if (rs.getInt(1) > 0) {
                Answer.getInstance().putB(NLS_INSTALLED, "Y");
            } else {
                Answer.getInstance().putB(NLS_INSTALLED, "N");
            }

            rs.close();
        } catch (Exception e) {
            logger.catching(e);
        }
    }

    @Override
    public String nextAction() {
        return "CheckRegistry";
    }
}

