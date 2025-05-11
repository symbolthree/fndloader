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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/oa/UploadOAObjectQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.oa;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.FileUtils;

import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;
import symbolthree.flower.Choice;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UploadOAObjectQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/oa/UploadOAObjectQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private File oaRootDir = null;

    public UploadOAObjectQuestion() {
        oaRootDir = new File(Instances.getInstance().getFileDirectory(OA_ROOT_DIR));
    }

    @Override
    public String getQuestion() {
        return "Please select [1] to upload ALL OA framework files, or choose a particular file to upload.";
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        al.add(new Choice("ALL", "All Files"));

        List<File> xmlFiles = null;

        if (Instances.getInstance().isNLSMode()) {
            xmlFiles = (List<File>) FileUtils.listFiles(oaRootDir, new String[] { "XLF", "xlf" }, true);
        } else {
            xmlFiles = (List<File>) FileUtils.listFiles(oaRootDir, new String[] { "XML", "xml" }, true);
        }

        Iterator<File> itr = xmlFiles.iterator();

        while (itr.hasNext()) {
            File   _file     = itr.next();
            String _filePath = _file.getAbsolutePath();

            al.add(new Choice(_filePath, _filePath));
        }

        return al;
    }

    @Override
    public String nextAction() {
        return "ApplicationsInfo";
    }

    @Override
    public boolean lineWrap() {
        return false;
    }

    @Override
    public String lastAction() {
        return "UploadOAPath";
    }
}
