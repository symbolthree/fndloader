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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/oa/OAExporter11Action.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.oa;

//~--- non-JDK imports --------------------------------------------------------

import oracle.adf.mds.exception.MDSException;
import oracle.adf.mds.tools.util.FileUtils;
import oracle.adf.mds.tools.xml.exporter.java118.Java118XMLExporter;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.FNDLOADERException;
import symbolthree.oracle.fndload.common.FNDLOADERActionBase;
import symbolthree.flower.Answer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class OAExporter11Action extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/oa/OAExporter11Action.java 1     11/06/16 1:12a Christopher Ho $";
    private Connection conn      = null;
    private String     oaDocType = null;

    static final Logger logger = LogManager.getLogger(OAExporter11Action.class.getName());
    
    public OAExporter11Action() {
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
        

        if (oaDocType.equals("C")) {
            String sql = " SELECT jdr_mds_internal.getDocumentName(path_docid) "
            		   + "   FROM jdr_paths"
            		   + "      , jdr_attributes"
                       + "  WHERE path_docid   = att_comp_docid"
                       + "    AND path_name    = ?"
                       + "    AND att_comp_seq = 0"
                       + "    AND att_name     = 'customizes'"
                       + "    AND att_value    = ?";
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
    }

    @Override
    public String nextAction() {
        return "DownloadOADone";
    }

    private void doExport(String exportDocName, String exportRootDir) throws MDSException {
        Java118XMLExporter exporter  = new Java118XMLExporter();
        boolean            createDir = true;
        String             fileName  = null;

        try {
            fileName = exportDocName.replace('/', File.separatorChar);

            // Helper.log(Helper.LOG_DEBUG, sUserMsgResource.getMessage("EXPORT_PROGRESS_MSG", exportDocName));

            if (!fileName.endsWith(".xml")) {
                fileName = fileName + ".xml";
            }

            fileName = exportRootDir + fileName;

            File outputFile;

            if (createDir) {
                outputFile = FileUtils.createDirectory(fileName);
            } else {
                logger.debug("FileName2=" + fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1));
                
                outputFile = new File(exportRootDir, fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1));
            }

            logger.debug("Output file is " + outputFile.getAbsolutePath());

            FileOutputStream fos = new FileOutputStream(outputFile);

            exporter.getDocument(exportDocName, fos, conn);
            fos.close();
        } catch (IOException ex) {
            throw new MDSException(null, "WRITE_ERROR", new Object[] { fileName });
        }
    }

    private String getDocPath(String oaDoc) {
        return oaDoc.substring(0, oaDoc.lastIndexOf("/"));
    }

    private String getDocName(String oaDoc) {
        return oaDoc.substring(oaDoc.lastIndexOf("/") + 1);
    }
}
