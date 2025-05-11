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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/DownloadOAQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/17/16 10:48a $
 * $Revision: 2 $
******************************************************************************/

package symbolthree.oracle.fndload.oa;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Choice;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;

import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class DownloadOAQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/DownloadOAQuestion.java 2     11/17/16 10:48a Christopher Ho $";
    static final Logger logger = LogManager.getLogger(DownloadOAQuestion.class.getName());
    
    public DownloadOAQuestion() {}

    @Override
    public String getQuestion() {
        return "Which type of OA Framework component you want to download?";
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        al.add(new Choice("C", "Personalization"));
        al.add(new Choice("P", "Page/Region Definition"));

        // al.add(new Choice("A",  "Attributeset"));
        // al.add(new Choice("T",  "Translation"));

        return al;
    }

    @Override
    public boolean enterQuestion() {
        return true;
    }

    @Override
    public String lastAction() {
        return "OperationObject";
    }

    @Override
    public String nextAction() {

        // String ans = Answer.getInstance().getA(this);
        return "DownloadOAObj";
    }
}
