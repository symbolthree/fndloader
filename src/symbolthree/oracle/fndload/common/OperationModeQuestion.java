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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/OperationModeQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/22/16 10:58a $
 * $Revision: 2 $
******************************************************************************/

package symbolthree.oracle.fndload.common;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.oracle.fndload.Config;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;

public class OperationModeQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/OperationModeQuestion.java 2     11/22/16 10:58a Christopher Ho $";

    public OperationModeQuestion() {}

    @Override
    public String getQuestion() {
        return "Which operation you want to do for instance " + Answer.getInstance().getA("SelectInstance") + " ?";
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        al.add(new Choice("DOWNLOAD", "Download Object"));
        al.add(new Choice("UPLOAD", "Upload Object"));

        return al;
    }

    @Override
    public String nextAction() {
        return "OperationObject";
    }

    @Override
    public String lastAction() {
        return "SelectInstance";
    }
    
    @Override
    public String getTitle() {
        return "FNDLOADER " + Config.getInstance().getVersion() + " [" + Answer.getInstance().getB(APP_MODE)
               + "] - " + Answer.getInstance().getA("SelectInstance");
    }
    
}

