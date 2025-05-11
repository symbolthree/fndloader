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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/DatabaseCredentialQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/14/16 1:27a $
 * $Revision: 2 $
******************************************************************************/

package symbolthree.oracle.fndload.common;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Helper;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class DatabaseCredentialQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/DatabaseCredentialQuestion.java 2     11/14/16 1:27a Christopher Ho $";
    private boolean askPassword;
    static final Logger logger = LogManager.getLogger(DatabaseCredentialQuestion.class.getName());
    
    @Override
    public boolean enterQuestion() {
        askPassword = Helper.getBoolean(Answer.getInstance().getB(ASK_PASSWORD), true);
        logger.debug("Password required = " + askPassword);
        return askPassword;
    }

    @Override
    public String getQuestion() {
        return "Please enter the APPS password for instance " + Answer.getInstance().getA("SelectInstance") + ":";
    }

    @Override
    public boolean isPasswordInput() {
        return true;
    }

    @Override
    public boolean isMultipleChoices() {
        return false;
    }

    @Override
    public String nextAction() {
        if (askPassword) {
            return "CheckCredential";
        } else {
            Answer.getInstance().putB(PASSWORD_ATTEMPT, "0");

            //return "OperationMode";
            return "CheckClientFile";
        }
    }

    @Override
    public boolean showProgress() {
        return true;
    }
}

