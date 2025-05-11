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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/oa/OAImporter11Action.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.oa;

//~--- non-JDK imports --------------------------------------------------------

import oracle.adf.mds.tools.xml.importer.java118.Java118XMLImporter;
import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.FNDLOADERException;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERActionBase;

import org.apache.commons.io.FileUtils;

import symbolthree.flower.Answer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;

import java.sql.Connection;

import java.util.Iterator;
import java.util.List;

public class OAImporter11Action extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/oa/OAImporter11Action.java 1     11/06/16 1:12a Christopher Ho $";
    private Connection conn      = null;
    private String     oaFile    = null;
    private File       oaRootDir = null;

    static final Logger logger = LogManager.getLogger(OAImporter11Action.class.getName());
    
    public OAImporter11Action() {
        oaRootDir = new File(Instances.getInstance().getFileDirectory(OA_ROOT_DIR));
        oaFile    = Answer.getInstance().getA("UploadOAObj");

        try {
            conn = DBConnection.getInstance().getConnection();
        } catch (FNDLOADERException e) {
            logger.catching(e);
        }
    }

    @Override
    public void execute() {
        if (oaFile.equals("ALL")) {
            List<File> xmlFiles = (List<File>) FileUtils.listFiles(oaRootDir, new String[] { "XML", "xml", "XLF",
                    "xlf" }, true);
            Iterator<File> itr = xmlFiles.iterator();

            while (itr.hasNext()) {
                File file = itr.next();

                doImport(file.getAbsolutePath());
            }
        } else {
            doImport(oaFile);
        }
    }

    private void doImport(String _file) {
        String docPath = getDocPath(oaFile);
        String docName = getDocName(oaFile);

        logger.info("docPath=" + docPath + ", docName=" + docName);

        Java118XMLImporter importer = new Java118XMLImporter();

        try {
            FileInputStream fis = new FileInputStream(new File(oaFile));

            importer.saveDocument(fis, docPath + "/" + docName, "INTERNAL", true, conn);
        } catch (Exception e) {
        	logger.catching(e);
        }
    }

    private String getDocPath(String fullPath) {
        File   file    = new File(fullPath);
        String fullDir = file.getParent();

        fullDir = fullDir.replace(File.separator, "/");

        return "/" + fullDir.substring(oaRootDir.getAbsolutePath().length() + 1);
    }

    private String getDocName(String fullPath) {
        File   file     = new File(fullPath);
        String fileName = file.getName();

        return fileName.substring(0, fileName.length() - 4);
    }

    @Override
    public String nextAction() {
        return "UploadOADone";
    }
}
