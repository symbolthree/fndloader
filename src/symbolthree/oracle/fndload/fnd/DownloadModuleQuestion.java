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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/fnd/DownloadModuleQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/

package symbolthree.oracle.fndload.fnd;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.*;

import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class DownloadModuleQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/fnd/DownloadModuleQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private ArrayList<Choice> choices;
    static final Logger logger = LogManager.getLogger(DownloadModuleQuestion.class.getName());
    
    @Override
    public boolean enterQuestion() {
        try {
            choices = Config.getInstance().getAllModules(isNLSMode());

            // add OA Framework for NLS
            if (Instances.getInstance().isNLSMode()) {
                choices.add(new Choice("OAF", "OA Framework Object"));
            }

            logger.debug("Choice Size=" + choices.size());
            
            if (choices.size() == 1) {
                String module = choices.get(0).getChoiceKey();

                Answer.getInstance().putA("DownloadModule", module);

                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.catching(e);

            return true;
        }
    }

    @Override
    public String getQuestion() {
        if (isNLSMode()) {
            return "Which module of object NLS translation you want to download from "
                   + Answer.getInstance().getA("SelectInstance") + " ?";
        } else {
            return "Which module of object you want to download from " + Answer.getInstance().getA("SelectInstance")
                   + " ?";
        }
    }

    @Override
    public ArrayList<Choice> choices() {
        return choices;
    }

    @Override
    public String sortChoicesBy() {
        return SORT_BY_KEY;
    }

    @Override
    public String lastAction() {
        return "OperationMode";
    }

    @Override
    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        if (ans.equals("OAF")) {
            return "DownloadOAObj";
        } else {
            return "DownloadObject";
        }
    }

    private boolean isNLSMode() {
        String op = Answer.getInstance().getA("OperationObject");

        if (op.equals("NLS")) {
            return true;
        }

        return false;
    }
}
