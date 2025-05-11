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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/DownloadXDOPathQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.xdo;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.ConsoleDirectoryBrowser;
import symbolthree.flower.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;

import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class DownloadXDOPathQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/DownloadXDOPathQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private String errMsg = null;

    static final Logger logger = LogManager.getLogger(DownloadXDOPathQuestion.class.getName());
    
    public DownloadXDOPathQuestion() {}

    @Override
    public String getQuestion() {
        return "Please verify the following information:";
    }

    @Override
    public String getExplanation() {
        String str = "";

        str = str + "Output Directory: " + Instances.getInstance().getFileDirectory(XDO_DIRECTORY);

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

        al.add(new Choice("NEXT", "Start download"));
        al.add(new Choice(CHANGE_DIRECTORY, "Change directory"));

        return al;
    }

    @Override
    public boolean isMultipleChoices() {
        if (Answer.getInstance().getB(XDO_ACTION) == null) {
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
        if ((Answer.getInstance().getB(XDO_ACTION) != null)
                && Answer.getInstance().getB(XDO_ACTION).equals(CHANGE_DIRECTORY)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean leaveQuestion() {
        String ans = Answer.getInstance().getA(this);

        if ((ans != null) &&!ans.equals("")) {
            if ((Answer.getInstance().getB(OA_ACTION) != null)
                    && Answer.getInstance().getB(OA_ACTION).equals(CHANGE_DIRECTORY)) {

                // validation
                File f = new File(ans);

                if (!f.exists() ||!f.isDirectory()) {
                    errMsg = "Invalid directory value.";
                    Answer.getInstance().putA(this, CHANGE_DIRECTORY);

                    return false;
                }

                Answer.getInstance().putB(XDO_DIRECTORY, ans);
                Answer.getInstance().putB(XDO_ACTION, null);
                Instances.getInstance().saveDirectory(XDO_DIRECTORY);
            }
        }

        return true;
    }

    @Override
    public Message leaveQuestionMsg(boolean flag) {
    	logger.debug("DownloadOAPath leaveQMsg:" + flag);

        if (!flag) {
            return new Message(Message.ERROR, errMsg);
        } else {
            return null;
        }
    }

    @Override
    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        logger.debug("DownloadOAPath Ans:" + ans);

        if (ans == null) {
            Answer.getInstance().putB(OA_ACTION, null);

            return "DownloadOAPath";
        }

        if (ans.equals(CHANGE_DIRECTORY)) {
            System.setProperty(ConsoleDirectoryBrowser.CURR_DIR, Answer.getInstance().getB(XDO_DIRECTORY));
            Answer.getInstance().putB(OA_ACTION, CHANGE_DIRECTORY);

            return "DownloadOAPath";
        } else if (ans.equals("NEXT")) {
            return "SaveConfig";
        } else {
            Answer.getInstance().putB(OA_ACTION, null);

            return "DownloadOAPath";
        }
    }

    @Override
    public String lastAction() {
        return "DownloadOASelect";
    }
}
