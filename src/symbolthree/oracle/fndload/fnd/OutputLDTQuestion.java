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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/OutputLDTQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 1/24/17 9:51a $
 * $Revision: 3 $
******************************************************************************/


package symbolthree.oracle.fndload.fnd;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.ConsoleDirectoryBrowser;
import symbolthree.flower.ConsoleFileBrowser;
import symbolthree.flower.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;

import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class OutputLDTQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/OutputLDTQuestion.java 3     1/24/17 9:51a Christopher Ho $";
    private String  errMsg = null;
    private boolean isNLSMode;
    private boolean isWFLoad;
    static final Logger logger = LogManager.getLogger(OutputLDTQuestion.class.getName());    

    @Override
    public boolean enterQuestion() {
        isNLSMode = Instances.getInstance().isNLSMode();
        isWFLoad  = Instances.getInstance().isWFLoad();

        try {
            if (Answer.getInstance().getB(LDT_FILE) == null) {
                SimpleDateFormat sdf          = new SimpleDateFormat("yyMMdd-HHmmss");
                String           timestamp    = sdf.format(Calendar.getInstance().getTime());
                String           instanceName = Answer.getInstance().getA("SelectInstance");
                String           name         = Answer.getInstance().getA("DownloadObject");
                String           key          = null;

                if (isWFLoad) {
                    //key = "WORKFLOW_ITEM_TYPE";
                	key = "WORKFLOW";
                } else {
                    //key = Config.getInstance().getKeyByName(name);
                	key = name;
                }

                String outputFile = instanceName + "_" + key + "_" + timestamp + (isWFLoad
                        ? ".wft"
                        : ".ldt");

                if (isNLSMode) {
                    String nls = Answer.getInstance().getA("SelectNLSLang");

                    outputFile = instanceName + "_" + key + "_" + timestamp + "_" + nls + ".ldt";
                }

                Answer.getInstance().putB(LDT_FILE, outputFile);
            }
        } catch (Exception e) {
            logger.catching(e);
        }

        logger.debug(Answer.getInstance().getB(LDT_ACTION));
        logger.debug(Answer.getInstance().getA(this));
        
        return true;
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        al.add(new Choice("N", "Start download"));
        al.add(new Choice(CHANGE_DIRECTORY, "Change directory"));
        al.add(new Choice(CHANGE_FILE, "Change filename"));

        return al;
    }

    @Override
    public String getQuestion() {
        return "A " + (isWFLoad
                       ? "WFT"
                       : "LDT") + " file will be created for your selection:\n";
    }

    @Override
    public String getExplanation() {
        String str = "Directory : " + Instances.getInstance().getFileDirectory(LDT_DIRECTORY) + "\n" + 
                     "Filename  : " + Answer.getInstance().getB(LDT_FILE);
        String ans = Answer.getInstance().getA(this);

        if ((ans != null) && ans.equals(CHANGE_FILE)) {
            str = str + "\n\n" + "Please enter a new filename";
        }

        if ((ans != null) && ans.equals(CHANGE_DIRECTORY)) {
            str = str + "\n\n" + "Please select a directory";
        }

        return str;
    }

    @Override
    public boolean isMultipleChoices() {
        if (Answer.getInstance().getB(LDT_ACTION) == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean needAnswer() {
        if ((Answer.getInstance().getA(this) != null)
                && (Answer.getInstance().getA(this).equals(CHANGE_FILE)
                    || Answer.getInstance().getA(this).equals(CHANGE_DIRECTORY))) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isDirectoryInput() {
        if ((Answer.getInstance().getB(LDT_ACTION) != null)
                && Answer.getInstance().getB(LDT_ACTION).equals(CHANGE_DIRECTORY)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean leaveQuestion() {
        String ans = Answer.getInstance().getA(this);

        if ((ans != null) &&!ans.equals("")) {
            if ((Answer.getInstance().getB(LDT_ACTION) != null)
                    && Answer.getInstance().getB(LDT_ACTION).equals(CHANGE_DIRECTORY)) {

                // validation
                File f = new File(ans);

                if (!f.exists() ||!f.isDirectory()) {
                    errMsg = "Invalid directory value.";
                    Answer.getInstance().putA(this, CHANGE_DIRECTORY);

                    return false;
                }

                Answer.getInstance().putB(LDT_DIRECTORY, ans);
                Answer.getInstance().putB(LDT_ACTION, null);
                Instances.getInstance().saveDirectory(LDT_DIRECTORY);
            } else if ((Answer.getInstance().getB(LDT_ACTION) != null)
                       && Answer.getInstance().getB(LDT_ACTION).equals(CHANGE_FILE)) {
                if (isWFLoad) {
                    if (!ans.toUpperCase().endsWith(".WFT")) {
                        ans = ans + ".wft";
                    }
                } else {
                    if (!ans.toUpperCase().endsWith(".LDT")) {
                        ans = ans + ".ldt";
                    }
                }

                Answer.getInstance().putB(LDT_FILE, ans);
                Answer.getInstance().putB(LDT_ACTION, null);
            }
        }

        return true;
    }

    @Override
    public String lastAction() {
        return "DownloadObject";
    }

    @Override
    public Message leaveQuestionMsg(boolean flag) {
        if (!flag) {
            return new Message(Message.ERROR, errMsg);
        } else {
            return null;
        }
    }

    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        if (ans == null) {
            Answer.getInstance().putB(LDT_ACTION, null);

            return "OutputLDT";
        }

        if (ans.equals(CHANGE_DIRECTORY)) {
            System.setProperty(ConsoleDirectoryBrowser.CURR_DIR, Answer.getInstance().getB(LDT_DIRECTORY));
            Answer.getInstance().putB(LDT_ACTION, CHANGE_DIRECTORY);

            return "OutputLDT";
        } else if (ans.equals(CHANGE_FILE)) {
            System.setProperty(ConsoleFileBrowser.CURR_DIR, Answer.getInstance().getB(LDT_DIRECTORY));
            Answer.getInstance().putB(LDT_ACTION, CHANGE_FILE);

            return "OutputLDT";
        } else if (ans.equals("N")) {
            return "ApplicationsInfo";
        } else {
            Answer.getInstance().putB(LDT_ACTION, null);

            return "OutputLDT";
        }
    }

    @Override
    public boolean lineWrap() {
        return false;
    }

    @Override
    public boolean showProgress() {
        return true;
    }
}
