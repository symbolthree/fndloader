/******************************************************************************
 *
 * ≡≡ FNDLOADER ≡≡
 * Copyright (C) 2009-2020 Christopher Ho
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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/XDOObjectQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.xdo;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;

import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class XDOObjectQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/XDOObjectQuestion.java 1     11/06/16 1:12a Christopher Ho $";

    public XDOObjectQuestion() {}

    @Override
    public String getQuestion() {
        return "Which XML Publisher Object you want to " + Answer.getInstance().getA("OperationMode")
               + " from instance " + Answer.getInstance().getA("SelectInstance") + " ?";
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        al.add(new Choice("XDO_DEF", "Data Source and Template Definition (LDT file)"));
        al.add(new Choice("XDO_LOB", "Data Source or Template Document (XML, PDF, RTF)"));

        return al;
    }

    @Override
    public String nextAction() {
        String ans    = Answer.getInstance().getA(this);
        String opMode = Answer.getInstance().getA("OperationMode");

        if (opMode.equals("DOWNLOAD") && ans.equals("XDO_DEF")) {
            Answer.getInstance().putA("DownloadModule", "XDO");
            Answer.getInstance().putA("DownloadObject", "XDO_DS_DEFINITIONS");

            return "DownloadFilter";
        }

        if (opMode.equals("DOWNLOAD") && ans.equals("XDO_LOB")) {
            return "XDOSelectLOB";
        }

        if (opMode.equals("UPLOAD") && ans.equals("XDO_DEF")) {
            return "UploadObject";
        }

        if (opMode.equals("UPLOAD") && ans.equals("XDO_LOB")) {
            return "UploadXDOFile";
        }

        return super.nextAction();
    }

    @Override
    public String lastAction() {
        return "OperationObject";
    }
}

