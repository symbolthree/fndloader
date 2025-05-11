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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/UploadXDOFileQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.xdo;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.FileUtils;

import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;
import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class UploadXDOFileQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/UploadXDOFileQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private String rtnMsg = "";

    static final Logger logger = LogManager.getLogger(UploadXDOFileQuestion.class.getName());
    
    public UploadXDOFileQuestion() {}

    @Override
    public ArrayList<Choice> choices() {
        String            dir      = Instances.getInstance().getFileDirectory(XDO_DIRECTORY);
        ArrayList<Choice> al       = new ArrayList<Choice>();
        String[]          fileExts = new String[] {
            "RTF", "rtf", "XSL", "xsl", "XML", "xml", "XSD", "xsd", "PDF", "pdf"
        };

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
        return "Select a XML Publisher file to upload to instance " + Answer.getInstance().getA("SelectInstance") + ":";
    }

    @Override
    public String getExplanation() {
        return "Current Directory: " + Instances.getInstance().getFileDirectory(XDO_DIRECTORY) + "\n"
               + "Select your file :";
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
        if ((Answer.getInstance().getB(XDO_ACTION) != null)
                && Answer.getInstance().getB(XDO_ACTION).equals(SELECT_FILE)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean leaveQuestion() {
        if ((Answer.getInstance().getB(XDO_ACTION) != null)
                && Answer.getInstance().getB(XDO_ACTION).equals(SELECT_FILE)) {
            String ans = Answer.getInstance().getA(this);

            if ((ans != null) &&!ans.equals("")) {
                File file = new File(ans);

                if (!file.exists()) {
                    Answer.getInstance().putB(XDO_ACTION, null);
                    Answer.getInstance().putA(this, SELECT_FILE);
                    rtnMsg = "Invalid file: " + file.getAbsolutePath();

                    return false;
                } else {
                    String dir = file.getParent();

                    logger.debug("XDO Directory:" + dir);
                    Answer.getInstance().putB(XDO_DIRECTORY, dir);
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
        return "XDOObject";
    }

    @Override
    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        if ((ans == null) || ans.equals("")) {
            Answer.getInstance().putB(XDO_ACTION, null);

            return "UploadXDOFile";
        }

        if (ans.equals(SELECT_FILE)) {
            Answer.getInstance().putB(XDO_ACTION, SELECT_FILE);

            return "UploadXDOFile";
        }

        Answer.getInstance().putB(XDO_ACTION, null);

        return "UploadXDOMatch";
    }

    @Override
    public boolean lineWrap() {
        return false;
    }
}

