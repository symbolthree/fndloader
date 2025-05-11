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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/XDOLoaderDoneQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/

package symbolthree.oracle.fndload.xdo;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;

//~--- JDK imports ------------------------------------------------------------


import java.io.File;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class XDOLoaderDoneQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/XDOLoaderDoneQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private ArrayList<Choice> choices = new ArrayList<Choice>();

    static final Logger logger = LogManager.getLogger(XDOLoaderDoneQuestion.class.getName());
    
    public XDOLoaderDoneQuestion() {}

    @Override
    public String getQuestion() {
        return "XDOLoader has " + Answer.getInstance().getA("OperationMode") + " your request file. \n";
    }

    public String getExplanation() {
        String str    = "";
        String opMode = Answer.getInstance().getA("OperationMode");

        if (opMode.equals("DOWNLOAD")) {
        	File f = new File(Answer.getInstance().getB(XDO_OUTPUT_FILE));
            str = "Output File     : " + f.getName() + "\n" 
                + "Output Location : " + Answer.getInstance().getB(XDO_DIRECTORY) + "\n";
        }

        return str;
    }

    @Override
    public boolean lineWrap() {
        return false;
    }

    @Override
    public ArrayList<Choice> choices() {
        choices.add(new Choice("B", "Back to main menu"));

        return choices;
    }

    @Override
    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        if (ans.equals("B")) {

            // clear all answers
            Answer.getInstance().clearAll();

            try {
                DBConnection.getInstance().disconnect();
            } catch (Exception e) {}

            return "ServerClientMode";
        }

        return super.nextAction();
    }
}

