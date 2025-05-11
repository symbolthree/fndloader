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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/OAXLIFFImporterAction.java $
 * $Author: Christopher Ho $
 * $Date: 2/06/17 3:37a $
 * $Revision: 2 $
******************************************************************************/

package symbolthree.oracle.fndload.oa;

import oracle.adf.mds.tools.util.CommandLineProcessor;
import oracle.adf.mds.tools.util.ConnectUtils;
import oracle.adf.mds.util.CommandLineArgs;
import oracle.jdbc.driver.OracleDriver;

//~--- non-JDK imports --------------------------------------------------------

import oracle.jrad.tools.trans.imp.XLIFFImporter;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERActionBase;

import org.apache.commons.io.FileUtils;

import symbolthree.flower.Answer;
import symbolthree.flower.Helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OAXLIFFImporterAction extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/OAXLIFFImporterAction.java 2     2/06/17 3:37a Christopher Ho $";
    private String oaFile    = null;
    private File   oaRootDir = null;

    static final Logger logger = LogManager.getLogger(OAXLIFFImporterAction.class.getName());
    
    public OAXLIFFImporterAction() {
        oaRootDir = new File(Instances.getInstance().getFileDirectory(OA_ROOT_DIR));
        oaFile    = Answer.getInstance().getA("UploadOAObj");
    }

    @Override
    public void execute() {
        if (oaFile.equals("ALL")) {
            List<File>     xmlFiles = (List<File>) FileUtils.listFiles(oaRootDir, new String[] { "XLF", "xlf" }, true);
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
         *       java oracle.jrad.tools.trans.imp.XLIFFImporter
         * -username APPS
         * -password <APPS password>
         * -dbconnection (DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=<hostname>)PORT=<port number>))(CONNECT_DATA=(SID=<your SID>)))
         * <path>/<NLS>/<page_name>.xlf
         */
        args.add("-username");
        args.add("apps");
        args.add("-password");
        args.add(Instances.getInstance().getPassword(Answer.getInstance().getA("SelectInstance")));
        args.add("-dbconnection");
        args.add(Answer.getInstance().getB(TNS_INSTANCE_PARAMS));
        args.add(_file);
        logger.debug(Helper.printArray(args));
        logger.debug("start XLIFFImporter...");
        //XLIFFImporter.main(Helper.listToArray(args));
        
        try {
	        String str = "username*|password*|dbconnection*|help|platform";
	        CommandLineProcessor processor = new CommandLineProcessor(Helper.listToArray(args), true, str);
	        CommandLineArgs cmdLineArgs = processor.processArgs();
	        
	        DriverManager.registerDriver(new OracleDriver());
	        Connection localConnection = DriverManager.getConnection(ConnectUtils.getConnectString(cmdLineArgs));
	        
	        boolean bool = cmdLineArgs.getValue("checklang", "true").equalsIgnoreCase("true");
	        
	        XLIFFImporter localXLIFFImporter = new XLIFFImporter(cmdLineArgs, localConnection, bool);
	        localXLIFFImporter.importDocument(cmdLineArgs.getValueByPosition(1));
	        
	        localXLIFFImporter.close();
	        localConnection.close();                
	        
	        logger.debug("XLIFFImporter done");
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
