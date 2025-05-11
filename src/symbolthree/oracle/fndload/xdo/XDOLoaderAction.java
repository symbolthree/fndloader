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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/XDOLoaderAction.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.xdo;

//~--- non-JDK imports --------------------------------------------------------

import oracle.apps.xdo.oa.util.XDOLoader;
import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.fnd.FNDLOADERAction;

import org.apache.commons.io.FileUtils;

import symbolthree.flower.Answer;
import symbolthree.flower.Helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

public class XDOLoaderAction extends FNDLOADERAction {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/XDOLoaderAction.java 1     11/06/16 1:12a Christopher Ho $";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HHmmss");
    
    static final Logger logger = LogManager.getLogger(XDOLoaderAction.class.getName());    

    public XDOLoaderAction() {}

    @Override
    public void execute() {
        ArrayList<String> args = new ArrayList<String>();

        /**
         * java oracle.apps.xdo.oa.util.XDOLoader DOWNLOAD | UPLOAD
         * -DB_USERNAME <db_username>
         * -DB_PASSWORD <db_password>
         * -JDBC_CONNECTION <jdbc_con_string>
         * -LOB_TYPE <lob_type>
         * -APPS_SHORT_NAME <application_short_name>
         * -LOB_CODE <lob_code>
         * -LANGUAGE <language>
         * -TERRITORY <territory>
         * -LOG_FILE <log file>
         * ~~ UPLOAD ~~
         * -XDO_FILE_TYPE <xdo_file_type>
         * -NLS_LANG <NLS_LANG>
         * -FILE_CONTENT_TYPE <file_content_type> (optional)
         * -FILE_NAME <file_name>
         * -OWNER <owner>  (optional)
         * -CUSTOM_MODE [FORCE|NOFORCE]
         * ~~ UPLOAD ~~
         */
        String opMode        = Answer.getInstance().getA("OperationMode");
        String lobType       = null;
        String language      = null;
        String territory     = null;
        String lobCode       = null;
        String appsShortName = null;
        String filename      = null;
        String fileType      = null;
        String xdoFileType   = null;

        try {
            String            rowid = null;
            Connection        conn  = DBConnection.getInstance().getConnection();
            PreparedStatement ps    = null;
            ResultSet         rs    = null;

            if (opMode.equals("DOWNLOAD")) {
                rowid = Answer.getInstance().getA("XDOResultLOB");
            } else if (opMode.equals("UPLOAD") && (Answer.getInstance().getA("UploadXDOConfirm") != null)) {
                rowid = Answer.getInstance().getA("UploadXDOConfirm");
            }

            if (rowid != null) {
                ps = conn.prepareStatement(getSQL());
                ps.setString(1, rowid);
                rs = ps.executeQuery();
                rs.next();
                lobType       = rs.getString("LOB_TYPE");
                appsShortName = rs.getString("APPLICATION_SHORT_NAME");
                lobCode       = rs.getString("LOB_CODE");
                filename      = rs.getString("FILE_NAME");
                fileType      = filename.toLowerCase().substring(filename.lastIndexOf(".") + 1);
                language      = rs.getString("LANGUAGE");
                territory     = rs.getString("TERRITORY");
                xdoFileType   = rs.getString("XDO_FILE_TYPE");
            }

            if (opMode.equals("UPLOAD") && (Answer.getInstance().getA("UploadXDOConfirm") == null)) {
                if (Answer.getInstance().getB("DATA_SOURCE_ROWID") != null) {
                    ps = conn.prepareStatement(getSQL("DATA_SOURCE"));
                    ps.setString(1, Answer.getInstance().getB("DATA_SOURCE_ROWID"));
                    rs = ps.executeQuery();
                    rs.next();
                    lobType       = Answer.getInstance().getA("UploadXDOType");
                    appsShortName = rs.getString("APPLICATION_SHORT_NAME");
                    lobCode       = rs.getString("DATA_SOURCE_CODE");
                    filename      = Answer.getInstance().getA("UploadXDOFile");
                    fileType      = filename.toLowerCase().substring(filename.lastIndexOf(".") + 1);
                    language      = "00";
                    territory     = "00";
                    xdoFileType   = fileType.toUpperCase();
                }

                if (Answer.getInstance().getB("TEMPLATE_ROWID") != null) {
                    ps = conn.prepareStatement(getSQL("TEMPLATE"));
                    ps.setString(1, Answer.getInstance().getB("TEMPLATE_ROWID"));
                    rs = ps.executeQuery();
                    rs.next();
                    lobType       = Answer.getInstance().getA("UploadXDOType");
                    appsShortName = rs.getString("APPLICATION_SHORT_NAME");
                    lobCode       = rs.getString("TEMPLATE_CODE");
                    filename      = Answer.getInstance().getA("UploadXDOFile");
                    fileType      = filename.toLowerCase().substring(filename.lastIndexOf(".") + 1);
                    language      = getSQLResult(conn, "LANGUAGE");
                    territory     = getSQLResult(conn, "TERRITORY");
                    xdoFileType   = fileType.toUpperCase();
                }
            }

            logger.debug("lobType=" + lobType);
            logger.debug("language=" + language);
            logger.debug("territory=" + territory);
            logger.debug("lobCode=" + lobCode);
            logger.debug("appsShortName=" + appsShortName);
            logger.debug("lobCode=" + lobCode);
            logger.debug("filename=" + filename);
            logger.debug("fileType=" + fileType);
            
            rs.close();
            ps.close();
            args.add(opMode);    // DOWNLOAD or UPLOAD
            args.add("-DB_USERNAME");
            args.add("APPS");
            args.add("-DB_PASSWORD");
            args.add(Instances.getInstance().getPassword(Answer.getInstance().getA("SelectInstance")));
            args.add("-JDBC_CONNECTION");
            args.add(Answer.getInstance().getB(TNS_INSTANCE_PARAMS));
            args.add("-LOB_TYPE");
            args.add(lobType);
            args.add("-APPS_SHORT_NAME");
            args.add(appsShortName);
            args.add("-LOB_CODE");
            args.add(lobCode);
            args.add("-LANGUAGE");
            args.add(language);
            args.add("-TERRITORY");
            args.add(territory);
            args.add("-LOG_FILE");

            String timestamp = sdf.format(Calendar.getInstance().getTime());

            File logDir = new File(FNDLOADER_APPLICATION_DIR, "log");
            
            if (! logDir.exists()) logDir.mkdirs();
            
            args.add(FNDLOADER_APPLICATION_DIR + File.separator + "log" + File.separator + "xdo" + timestamp
                     + ".log");
            
            if (opMode.equals("DOWNLOAD")) {
                args.add("-FILES_DIR");
                args.add(Instances.getInstance().getFileDirectory(XDO_DIRECTORY));
            }
            
            if (opMode.equals("UPLOAD")) {
                args.add("-XDO_FILE_TYPE");
                args.add(xdoFileType);

                // args.add("-NLS_LANG");
                // args.add();
                args.add("-FILE_NAME");
                args.add(Answer.getInstance().getA("UploadXDOFile"));
                args.add("-CUSTOM_MODE");
                args.add("FORCE");
            }

            logger.debug(maskPassword(Helper.printArray(args)));

            // XDOLoader.main(Helper.listToArray(args));

            XDOLoader xdoloader = new XDOLoader(Helper.listToArray(args));

            xdoloader.process();
            xdoloader.close();

            if (opMode.equals("DOWNLOAD")) {

                // when done move the file
                String suffix = "";

                if (!language.equals("00")) {
                    suffix = language;
                }

                if (!territory.equals("00")) {
                    suffix = suffix + "_" + territory;
                }

                if (!suffix.equals("")) {
                    suffix = "_" + suffix;
                }

                String outfileName = lobType + "_" + appsShortName + "_" + lobCode + suffix + "." + fileType;
                File   origOutput  = new File(Instances.getInstance().getFileDirectory(XDO_DIRECTORY), outfileName);
                File   finalOutput = new File(Instances.getInstance().getFileDirectory(XDO_DIRECTORY), filename);

                Answer.getInstance().putB(XDO_OUTPUT_FILE, finalOutput.getAbsolutePath());
                logger.debug("Move file " + origOutput.getAbsolutePath() + " to " + finalOutput.getAbsolutePath());
                FileUtils.deleteQuietly(finalOutput);
                FileUtils.moveFile(origOutput, finalOutput);
            }
        } catch (Exception e) {
            logger.catching(e);
        }
    }

    @Override
    public String nextAction() {
        return "XDOLoaderDone";
    }

    private String getSQL() {
        return "select LOB_TYPE, APPLICATION_SHORT_NAME, LOB_CODE, LANGUAGE, TERRITORY, FILE_NAME, XDO_FILE_TYPE "
        	 + "from XDO_LOBS where ROWID=CHARTOROWID(?)";
    }

    private String getSQLResult(Connection conn, String sqlType) throws Exception {
        String            rtnVal = "";
        PreparedStatement ps     = conn.prepareStatement(getSQL(sqlType));

        ps.setString(1, Answer.getInstance().getB(sqlType + "_ROWID"));

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            rtnVal = rs.getString(1);
        }

        rs.close();
        ps.close();

        return rtnVal;
    }

    private String getSQL(String sqlType) {
        String sql = null;

        if (sqlType.equals("DATA_SOURCE")) {
            sql = "SELECT APPLICATION_SHORT_NAME, DATA_SOURCE_CODE FROM XDO_DS_DEFINTIONS_VL"
                  + " WHERE ROWIDTOCHAR(ROWID)=?";
        } else if (sqlType.equals("TEMPLATE")) {
            sql = "SELECT APPLICATION_SHORT_NAME, TEMPLATE_CODE FROM XDO_TEMPLATES_VL"
                  + " WHERE ROWIDTOCHAR(ROWID)=?";
        } else if (sqlType.equals("TERRITORY")) {
            sql = "SELECT TERRITORY_CODE FROM FND_TERRITORIES_VL WHERE ROWIDTOCHAR(ROWID)=?";
        } else if (sqlType.equals("LANGUAGE")) {
            sql = "SELECT ISO_LANGUAGE_2 FROM FND_ISO_LANGUAGES_VL WHERE ROWIDTOCHAR(ROWID)=?";
        }

        return sql;
    }

    private String maskPassword(String str) {
        String          output = "";
        StringTokenizer st     = new StringTokenizer(str);

        while (st.hasMoreTokens()) {
            if (output.endsWith("-DB_PASSWORD")) {
                st.nextToken();
                output = output + " *****";
            } else {
                output = output + " " + st.nextToken();
            }
        }

        return output;
    }
}
