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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/OAExporter13Action.java $
 * $Author: Christopher Ho $
 * $Date: 2/06/17 3:37a $
 * $Revision: 3 $
******************************************************************************/

package symbolthree.oracle.fndload.oa;

//~--- non-JDK imports --------------------------------------------------------

import oracle.adf.mds.exception.MDSException;

import oracle.jrad.tools.xml.exporter.XMLExporter;
import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.FNDLOADERException;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERActionBase;
import symbolthree.flower.Answer;
import symbolthree.flower.Helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class OAExporter13Action extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/OAExporter13Action.java 3     2/06/17 3:37a Christopher Ho $";
    private Connection conn      = null;
    private String     oaDocType = null;
    static final Logger logger = LogManager.getLogger(OAExporter13Action.class.getName());
    
    public OAExporter13Action() {
        oaDocType = Answer.getInstance().getA("DownloadOAType");

        try {
            conn = DBConnection.getInstance().getConnection();
        } catch (FNDLOADERException f) {
            logger.catching(f);
            return;
        }
    }

    @Override
    public void execute() {
        MDSException.init("oracle.adf.mds.exception.MDSResources");

        String oaDoc = Answer.getInstance().getA("DownloadOASelect");

        // get doc path and doc name
        String docPath = getDocPath(oaDoc);
        String docName = getDocName(oaDoc);
        String rootDir = Answer.getInstance().getB(OA_ROOT_DIR);

        
        logger.info("DocPath=" + docPath);
        logger.info("DocName=" + docName);
        logger.info("rootDir=" + rootDir);

        if (!Instances.getInstance().isNLSMode()) {
            if (oaDocType.equals("C")) {
                String sql = " SELECT jdr_mds_internal.getDocumentName(path_docid)"
                           + "   FROM jdr_paths, jdr_attributes"
                           + "  WHERE path_docid = att_comp_docid"
                           + "    AND path_name  = ? AND att_comp_seq = 0"
                           + "    AND att_name   = 'customizes'"
                           + "    AND att_value  = ?";
                int counter = 0;

                try {
                    PreparedStatement ps = conn.prepareStatement(sql);

                    ps.setString(1, docName);
                    ps.setString(2, oaDoc);

                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        String customization = rs.getString(1);

                        doExport(customization, rootDir);
                        counter++;
                    }
                } catch (SQLException sqle) {
                    logger.catching(sqle);
                } catch (MDSException me) {
                	logger.catching(me);
                }

                Answer.getInstance().putB("NO_OF_CUSTOMIZATION", counter);
            } else if (oaDocType.equals("P")) {
                try {
                    doExport(oaDoc, rootDir);
                } catch (MDSException me) {
                	logger.catching(me);
                }
            }
        } else {    // NLS mode
            try {
                doExport(oaDoc, rootDir);
            } catch (MDSException me) {
            	logger.catching(me);
            }
        }
    }

    @Override
    public String nextAction() {
        return "DownloadOADone";
    }

    private void doExport(String exportDocName, String exportRootDir) throws MDSException {
        ArrayList<String> args = new ArrayList<String>();

        /**
         *  java oracle.jrad.tools.xml.exporter.XMLExporter
         *   <Package_or_Document_Name>
         *   -rootdir <output_dir>
         *   -username <username>
         *   -password <password>
         *   -dbconnection <database>
         *   [-mmddir <MMD_dir>]
         *       [-includeSubpackages]
         *       [-displayOnly]
         *       [-jdk13]
         *       [-validate]
         *       [-translations]
         *       [-language <language>]
         *       [-dbdrvFile <dbdrv_file>]
         */
        args.add(exportDocName);
        args.add("-rootdir");
        args.add(exportRootDir);
        args.add("-username");
        args.add("apps");
        args.add("-password");
        args.add(Instances.getInstance().getPassword(Answer.getInstance().getA("SelectInstance")));
        args.add("-dbconnection");
        args.add(Answer.getInstance().getB(TNS_INSTANCE_PARAMS));
        args.add("-jdk13");
        args.add("-validate");
        args.add("-mmddir");

        // standalone vs server mode
        if (Answer.getInstance().getB(APP_MODE).equals(SERVER_MODE)) {
            args.add(System.getenv("OA_HTML") + File.separator + "jrad");
        } else {
            String OA_HTML = System.getProperty("user.dir") + File.separator + "CLIENT" + File.separator
                             + Answer.getInstance().getB(RELEASE_NAME) + File.separator + "OA_HTML" + File.separator
                             + "jrad";

            args.add(OA_HTML);
        }

        if (Instances.getInstance().isNLSMode()) {
            args.add("-translations");
            args.add("-language");
            args.add(Answer.getInstance().getB(OA_NLS_LANG));
        }

        logger.debug(maskPassword(Helper.printArray(args)));
        
        try {
        	
          logger.debug("start XMLExporter...");        	
          XMLExporter.main(Helper.listToArray(args));
          logger.debug("XMLExporter done");
        } catch (Exception e) {
        	logger.catching(e);
        }
    }

    private String getDocPath(String oaDoc) {
        return oaDoc.substring(0, oaDoc.lastIndexOf("/"));
    }

    private String getDocName(String oaDoc) {
        return oaDoc.substring(oaDoc.lastIndexOf("/") + 1);
    }

    private String maskPassword(String str) {
        String          output = "";
        StringTokenizer st     = new StringTokenizer(str);

        while (st.hasMoreTokens()) {
            if (output.endsWith("-password")) {
                st.nextToken();
                output = output + " *****";
            } else {
                output = output + " " + st.nextToken();
            }
        }

        return output;
    }
}
