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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/oa/OAImporter13Action.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.oa;

//~--- non-JDK imports --------------------------------------------------------

import oracle.jrad.tools.xml.importer.XMLImporter;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERActionBase;

import org.apache.commons.io.FileUtils;

import symbolthree.flower.Answer;
import symbolthree.flower.Helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class OAImporter13Action extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/oa/OAImporter13Action.java 1     11/06/16 1:12a Christopher Ho $";
    private String oaFile    = null;
    private File   oaRootDir = null;

    static final Logger logger = LogManager.getLogger(OAImporter13Action.class.getName());
    
    public OAImporter13Action() {
        oaRootDir = new File(Instances.getInstance().getFileDirectory(OA_ROOT_DIR));
        oaFile    = Answer.getInstance().getA("UploadOAObj");
    }

    @Override
    public void execute() {
        if (oaFile.equals("ALL")) {
            List<File>     xmlFiles = (List<File>) FileUtils.listFiles(oaRootDir, new String[] { "XML", "xml" }, true);
            Iterator<File> itr      = xmlFiles.iterator();

            while (itr.hasNext()) {
                File file = itr.next();

                doImport(file.getAbsolutePath());
            }
        } else {
            doImport(oaFile);
        }
    }

    private void doImport(String _file) {
        String docPath = getDocPath(_file);
        String docName = getDocName(_file);

        logger.info("docPath=" + docPath + ", docName=" + docName);

        ArrayList<String> args = new ArrayList<String>();

        /**
         *       java oracle.jrad.tools.xml.importer.XMLImporter
         *       <full_path_of_file_or_directory_to_import>
         *       -username <username>
         *       -password <password>
         *       -dbconnection <database>
         *       [-userId <userID>]
         *       -rootdir <root_dir>
         *       [-rootPackage <root_pkg_dir>]
         *       [-validate]
         *       [-includeSubpackages]
         *       [-jdk13]
         *       [-mmddir <MMD_dir>]
         *       [-displayOnly]
         */
        args.add(_file);
        args.add("-rootdir");
        args.add(oaRootDir.getAbsolutePath());
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

        logger.debug(maskPassword(Helper.printArray(args)));
        XMLImporter.main(Helper.listToArray(args));
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
