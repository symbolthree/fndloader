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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/DownloadOADoneQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 2/06/17 3:37a $
 * $Revision: 3 $
******************************************************************************/



package symbolthree.oracle.fndload.oa;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class DownloadOADoneQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/DownloadOADoneQuestion.java 3     2/06/17 3:37a Christopher Ho $";
    private String            oaDocType = null;
    private ArrayList<Choice> choices   = new ArrayList<Choice>();

    static final Logger logger = LogManager.getLogger(DownloadOADoneQuestion.class.getName());
    
    public DownloadOADoneQuestion() {
        oaDocType = Answer.getInstance().getA("DownloadOAType");
    }

    @Override
    public String getQuestion() {
        if (Instances.getInstance().isNLSMode()) {
            return "OA Framework translation is exported successfully.";
        }

        if (oaDocType.equals("C")) {
            return "OA Framework personalization is exported successfully.";
        }

        if (oaDocType.equals("P")) {
            return "OA Framework page/region definition is exported successfully.";
        }

        return super.getQuestion();
    }

    public String getExplanation() {
        String str = "";

        if (Instances.getInstance().isNLSMode()) {
            str =     "OA Page/Region   :" + Answer.getInstance().getA("DownloadOASelect") + "\n" 
                    + "Translation Lang :"
                    + Answer.getInstance().getB(OA_NLS_LANG) + "\n" 
                    + "Output Location  : "
                    + Answer.getInstance().getB("OA_ROOT_DIR");
        } else {
            if (oaDocType.equals("C")) {
                str = "Customized Page  : " + Answer.getInstance().getA("DownloadOASelect") + "\n"
                    + "Output Location  : " + Answer.getInstance().getB("OA_ROOT_DIR") + "\n"
                    + "# file exported  : " + Answer.getInstance().getB("NO_OF_CUSTOMIZATION");
            } else if (oaDocType.equals("P")) {
                str = "OA Page/Region   : " + Answer.getInstance().getA("DownloadOASelect") + "\n"
                    + "Output Location  : " + Answer.getInstance().getB("OA_ROOT_DIR");
            }
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

