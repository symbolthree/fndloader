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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/UploadNLSObjectQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 2/06/17 3:37a $
 * $Revision: 2 $
******************************************************************************/

package symbolthree.oracle.fndload.common;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.Message;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;

public class UploadNLSObjectQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/UploadNLSObjectQuestion.java 2     2/06/17 3:37a Christopher Ho $";

    public UploadNLSObjectQuestion() {}

    @Override
    public String getQuestion() {
        return "Which translation object you want to upload to instance " + Answer.getInstance().getA("SelectInstance")
               + " ?";
    }

    @Override
    public String getExplanation() {
        return "Selected Language: " + Answer.getInstance().getB(NLS_TRANSLATION);
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        al.add(new Choice("FND", "FNDLOAD Object"));
        al.add(new Choice("OA", "OA Framework Object"));
        //al.add(new Choice("XDO", "XML Publisher"));

        return al;
    }

    @Override
    public boolean leaveQuestion() {
        if (Answer.getInstance().getA(this).equals("XDO")) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Message leaveQuestionMsg(boolean flag) {
        if (!flag) {
            return new Message(Message.ERROR, "Sorry This feature has not implemented yet.");
        } else {
            return null;
        }
    }

    @Override
    public String lastAction() {
        return "SelectNLSLang";
    }

    @Override
    public String nextAction() {
        String _object = Answer.getInstance().getA(this);

        if (_object.equals("FND")) {
            return "UploadObject";
        }

        if (_object.equals("OA")) {
            return "UploadOAObj";
        }

        return super.nextAction();
    }
}
