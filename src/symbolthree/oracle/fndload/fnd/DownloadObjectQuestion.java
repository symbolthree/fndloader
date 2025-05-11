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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/fnd/DownloadObjectQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/

package symbolthree.oracle.fndload.fnd;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.Question;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.*;

import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class DownloadObjectQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/fnd/DownloadObjectQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private boolean isNLSMode;
    static final Logger logger = LogManager.getLogger(DownloadObjectQuestion.class.getName());
    
    public DownloadObjectQuestion() {}

    @Override
    public boolean enterQuestion() {
        isNLSMode = Instances.getInstance().isNLSMode();

        // clear re-enter cache value
        Answer.getInstance().putB(LDT_FILE, null);

        return true;
    }

    @Override
    public String getQuestion() {
        if (isNLSMode) {
            return "Which object of " + Answer.getInstance().getB(NLS_TRANSLATION)
                   + " translation you want to download from " + Answer.getInstance().getA("SelectInstance") + " ?";
        }

        return "Which object you want to download from " + Answer.getInstance().getA("SelectInstance") + " ?";
    }

    @Override
    public ArrayList<Choice> choices() {
        try {
            return Config.getInstance().getAllObjectType(Answer.getInstance().getA("DownloadModule"), isNLSMode);
        } catch (Exception e) {
            logger.catching(e);

            return null;
        }
    }

    @Override
    public String sortChoicesBy() {
        return Question.SORT_BY_DESC;
    }

    @Override
    public String lastAction() {
        ArrayList<Choice> moduleChoices;

        try {
            moduleChoices = Config.getInstance().getAllModules(Instances.getInstance().isNLSMode());
            logger.debug("Choice Size=" + moduleChoices.size());

            if (moduleChoices.size() == 1) {
                return "OperationMode";
            } else {
                return "DownloadModule";
            }
        } catch (Exception e) {
            logger.catching(e);

            return "OperationMode";
        }
    }

    @Override
    public String nextAction() {
        //return "DownloadFilter";
    	return "CheckLCTVerAction";
    }
}

