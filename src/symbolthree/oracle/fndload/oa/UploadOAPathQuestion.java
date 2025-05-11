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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/oa/UploadOAPathQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.oa;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.FileUtils;

import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;
import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.ConsoleDirectoryBrowser;
import symbolthree.flower.Message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class UploadOAPathQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/oa/UploadOAPathQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private String errMsg = null;
    
    static final Logger logger = LogManager.getLogger(UploadOAPathQuestion.class.getName());
    
    public UploadOAPathQuestion() {}

    @Override
    public String getQuestion() {
        return "Please select the OA document root path";
    }

    @Override
    public String getExplanation() {
        String str = "OA Root Dir: " + Instances.getInstance().getFileDirectory(OA_ROOT_DIR);
        String ans = Answer.getInstance().getA(this);

        if ((ans != null) && ans.equals(CHANGE_DIRECTORY)) {
            return str + "\nPlease select a directory";
        } else {
            return str;
        }
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        al.add(new Choice("NEXT", "Select OA files to upload"));
        al.add(new Choice(CHANGE_DIRECTORY, "Change directory"));

        return al;
    }

    @Override
    public boolean isMultipleChoices() {
        if (Answer.getInstance().getB(OA_ACTION) == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean needAnswer() {
        if ((Answer.getInstance().getA(this) != null) && Answer.getInstance().getA(this).equals(CHANGE_DIRECTORY)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isDirectoryInput() {
        if ((Answer.getInstance().getB(OA_ACTION) != null)
                && Answer.getInstance().getB(OA_ACTION).equals(CHANGE_DIRECTORY)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean leaveQuestion() {
        String  ans            = Answer.getInstance().getA(this);
        String  oaAction       = Answer.getInstance().getB(OA_ACTION);
        boolean needValidation = false;
        File    oaRootDir      = null;

        if ((ans != null) &&!ans.equals("")) {
            if ((oaAction != null) && oaAction.equals(CHANGE_DIRECTORY)) {
                oaRootDir      = new File(ans);
                needValidation = true;
            }

            if ((oaAction == null) && ans.equals("NEXT")) {
                oaRootDir      = new File(Answer.getInstance().getB(OA_ROOT_DIR));
                needValidation = true;
            }
        }

        // validation
        if (needValidation) {
            if (!oaRootDir.exists() ||!oaRootDir.isDirectory()) {
                errMsg = "Invalid directory value.";
                Answer.getInstance().putA(this, CHANGE_DIRECTORY);

                return false;
            }

            // check any xml file
            List<File> xmlFiles = (List<File>) FileUtils.listFiles(oaRootDir, new String[] { "XML", "xml" }, true);

            logger.info("XML found: " + xmlFiles.size());

            if (xmlFiles.size() == 0) {
                errMsg = "No XML can be found under this directory";
                Answer.getInstance().putA(this, CHANGE_DIRECTORY);

                return false;
            }

            Answer.getInstance().putB(OA_ROOT_DIR, oaRootDir.getAbsolutePath());
            Answer.getInstance().putB(OA_ACTION, null);
            Instances.getInstance().saveDirectory(OA_ROOT_DIR);
        }

        return true;
    }

    @Override
    public Message leaveQuestionMsg(boolean flag) {
        logger.debug("UploadOAPath leaveQMsg:" + flag);

        if (!flag) {
            return new Message(Message.ERROR, errMsg);
        } else {
            return null;
        }
    }

    @Override
    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        logger.debug("UploadOAPath Ans:" + ans);

        if (ans == null) {
            Answer.getInstance().putB(OA_ACTION, null);

            return "UploadOAPath";
        }

        if (ans.equals(CHANGE_DIRECTORY)) {
            System.setProperty(ConsoleDirectoryBrowser.CURR_DIR, Answer.getInstance().getB(OA_ROOT_DIR));
            Answer.getInstance().putB(OA_ACTION, CHANGE_DIRECTORY);

            return "UploadOAPath";
        } else if (ans.equals("NEXT")) {
            return "UploadOAObj";
        } else {
            Answer.getInstance().putB(OA_ACTION, null);

            return "UploadOAPath";
        }
    }

    @Override
    public String lastAction() {
        return "DownloadOASelect";
    }
}
