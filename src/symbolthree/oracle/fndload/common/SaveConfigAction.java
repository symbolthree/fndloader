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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/SaveConfigAction.java $
 * $Author: Christopher Ho $
 * $Date: 1/24/17 9:51a $
 * $Revision: 3 $
******************************************************************************/


package symbolthree.oracle.fndload.common;

import symbolthree.oracle.fndload.Instances;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SaveConfigAction extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/SaveConfigAction.java 3     1/24/17 9:51a Christopher Ho $";
    static final Logger logger = LogManager.getLogger(SaveConfigAction.class.getName());
    
    @Override
    public void execute() {
        Instances.getInstance().saveInstanceInfo();
        Instances.getInstance().getFileDirectory(LDT_DIRECTORY);
        Instances.getInstance().saveDirectory(LDT_DIRECTORY);
        Instances.getInstance().getFileDirectory(OA_ROOT_DIR);
        Instances.getInstance().saveDirectory(OA_ROOT_DIR);
        Instances.getInstance().getFileDirectory(XDO_DIRECTORY);
        Instances.getInstance().saveDirectory(XDO_DIRECTORY);
        Instances.getInstance().saveTNSAdminDirectory();
        
        String flag = Answer.getInstance().getA("LCTVersionDiff");
        
        logger.debug("LCTVersionDiff Answer = " + flag);
        if (flag != null && flag.equals(IGNORE_ALWAYS)) {
            String sid        = Answer.getInstance().getA("SelectInstance");
            String module     = Answer.getInstance().getA("DownloadModule");
            String objectName = Answer.getInstance().getA("DownloadObject");
            Instances.getInstance().setCheckLCTVer(sid, module, objectName, false);
        }

        Instances.getInstance().saveInstancesFile();
    }

    @Override
    public String nextAction() {

        /*
         * String releaseName = Answer.getInstance().getB(RELEASE_NAME);
         * if (releaseName.equals(RELEASE_11i)) return "FNDLOAD11Action";
         * if (releaseName.equals(RELEASE_12)) return "FNDLOAD12Action";
         * return super.nextAction();
         */
        String  opMode = Answer.getInstance().getA("OperationMode");
        boolean isNLS  = Instances.getInstance().isNLSMode();
        String  fnd    = Answer.getInstance().getA("DownloadFilter");

        if (fnd == null) {
            fnd = Answer.getInstance().getA("UploadObject");
        }

        String oa = Answer.getInstance().getA("DownloadOASelect");

        if (oa == null) {
            oa = Answer.getInstance().getA("UploadOAObj");
        }

        String xdo = Answer.getInstance().getA("XDOResultLOB");

        if (xdo == null) {
            xdo = Answer.getInstance().getA("UploadXDOFile");
        }

        logger.debug("FND=" + fnd + " OA=" + oa + " XDO=" + xdo);

        if (fnd != null) {
            return "FNDLOADERAction";
        }

        if (oa != null) {
            if (opMode.equals("DOWNLOAD")) {
                return "OAExporter";
            } else if (opMode.equals("UPLOAD")) {
                return isNLS
                       ? "OAXLIFFImporter"
                       : "OAImporter";
            }
        }

        if (xdo != null) {
            return "XDOLoader";
        }

        return super.nextAction();
    }
}

