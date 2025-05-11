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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/UploadObjectQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 2/06/17 3:37a $
 * $Revision: 2 $
******************************************************************************/

package symbolthree.oracle.fndload.common;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.FileUtils;

import symbolthree.oracle.fndload.Instances;
import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.Helper;
import symbolthree.flower.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class UploadObjectQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/UploadObjectQuestion.java 2     2/06/17 3:37a Christopher Ho $";
    private String  rtnMsg = "";
    private boolean isNLSMode;
    private boolean isWFLoad;
    static final Logger logger = LogManager.getLogger(UploadObjectQuestion.class.getName());

    @Override
    public boolean enterQuestion() {
        isWFLoad  = Instances.getInstance().isWFLoad();
        isNLSMode = Instances.getInstance().isNLSMode();
        logger.debug("isWFLoad=" + isWFLoad + ", " + "isNLSMode=" + isNLSMode);
        return true;
    }

    @Override
    public ArrayList<Choice> choices() {
        String            dir      = Instances.getInstance().getFileDirectory(LDT_DIRECTORY);
        ArrayList<Choice> al       = new ArrayList<Choice>();
        String[]          fileExts = null;

        if (isWFLoad) {
            fileExts = new String[] { "WFT", "wft" };
        } else {
            fileExts = new String[] { "LDT", "ldt" };
        }

        al.add(new Choice("SELECT_FILE", "Select a file in other directory"));

        if (dir != null) {
            File       dirFile = new File(dir);
            List<File> files   = (List<File>) FileUtils.listFiles(dirFile, fileExts, false);

            for (int i = 0; i < files.size(); i++) {
                File f = files.get(i);

                al.add(new Choice(f.getAbsolutePath(), f.getName()));
            }
        }

        return al;
    }

    @Override
    public String getQuestion() {
        if (isNLSMode) {
            return "Select a " + Answer.getInstance().getB(NLS_TRANSLATION)
                   + " translation LDT file to upload to instance " + Answer.getInstance().getA("SelectInstance") + ":";
        } else {
            return "Select a " + (isWFLoad
                                  ? "WFT"
                                  : "LDT") + " file to upload to instance "
                                           + Answer.getInstance().getA("SelectInstance") + ":";
        }
    }

    @Override
    public String getExplanation() {
        return "Current Directory: " + Instances.getInstance().getFileDirectory(LDT_DIRECTORY) + "\n"
               + "Select your file :";
    }

    public boolean isDirectoryInput() {

        // A directory dialog box will be shown to help user to select a directory
        return false;
    }

    @Override
    public String fileExtension() {
        return isWFLoad
               ? "WFT"
               : "LDT";
    }

    @Override
    public boolean isFileInput() {
        String ans = Answer.getInstance().getA(this);

        if ((ans != null) && ans.equals("SELECT_FILE")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isMultipleChoices() {
        String ans = Answer.getInstance().getA(this);

        if ((ans != null) && ans.equals("SELECT_FILE")) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean needAnswer() {
        if ((Answer.getInstance().getB(LDT_ACTION) != null)
                && Answer.getInstance().getB(LDT_ACTION).equals(SELECT_FILE)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean leaveQuestion() {
        if ((Answer.getInstance().getB(LDT_ACTION) != null)
                && Answer.getInstance().getB(LDT_ACTION).equals(SELECT_FILE)) {
            String ans = Answer.getInstance().getA(this);

            if ((ans != null) &&!ans.equals("")) {
                File file = new File(ans);

                if (!Helper.getExtension(file).equalsIgnoreCase((isWFLoad
                        ? "WFT"
                        : "LDT")) ||!file.exists()) {
                    Answer.getInstance().putB(LDT_ACTION, null);
                    Answer.getInstance().putA(this, SELECT_FILE);
                    rtnMsg = "Invalid file: " + file.getAbsolutePath();

                    return false;
                } else {
                    String dir = file.getParent();

                    logger.debug("LDT Directory:" + dir);
                    Answer.getInstance().putB(LDT_DIRECTORY, dir);
                }
            }
        }

        return true;
    }

    @Override
    public Message leaveQuestionMsg(boolean flag) {
        if (!flag) {
            return new Message(Message.ERROR, rtnMsg);
        } else {
            return null;
        }
    }

    @Override
    public String lastAction() {
        return "OperationMode";
    }

    @Override
    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        if ((ans == null) || ans.equals("")) {
            Answer.getInstance().putB(LDT_ACTION, null);

            return "UploadObject";
        }

        if (ans.equals(SELECT_FILE)) {
            Answer.getInstance().putB(LDT_ACTION, SELECT_FILE);

            return "UploadObject";
        }

        Answer.getInstance().putB(LDT_ACTION, null);

        return "LDTInfo";
        //return "CheckLCTVerAction";
    }

    @Override
    public boolean lineWrap() {
        return false;
    }
}

