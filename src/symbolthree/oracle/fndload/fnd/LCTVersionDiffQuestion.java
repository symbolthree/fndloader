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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/LCTVersionDiffQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 1/24/17 9:51a $
 * $Revision: 4 $
******************************************************************************/

package symbolthree.oracle.fndload.fnd;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class LCTVersionDiffQuestion extends FNDLOADERQuestion {

	public static final String RCS_ID =
            "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/LCTVersionDiffQuestion.java 4     1/24/17 9:51a Christopher Ho $";
	static final Logger logger = LogManager.getLogger(DownloadFilterQuestion.class.getName());	
	
    @Override
    public boolean enterQuestion() {
    	// check whether this file is ignored for version diff
    	// TODO 
        return true;
    }

    @Override
    public String getQuestion() {
    	String text = "The versions of configuration file in local and server are different: \r\n" +
                      "File Name      : " + Answer.getInstance().getB(LCT_FILE) + "\r\n" +
                      "Local Version  : " + Answer.getInstance().getB(LCT_LOCAL_VER) + "\r\n" +
                      "Server Version : " + Answer.getInstance().getB(LCT_SERVER_VER) + "\r\n";                 
    	return text;
    }

    @Override
    public String getExplanation() {
        return "Version difference could cause error during operation. " + 
        	   "You can obtain this file from server and replace the local file.";
    }
    
    @Override
    public ArrayList<Choice> choices() {
    	ArrayList<Choice> choices = new ArrayList<Choice>();
    	choices.add(new Choice(IGNORE_ONCE, "Ignore warning this time"));
    	choices.add(new Choice(IGNORE_ALWAYS, "Always ignore warning for this config file"));
    	return choices;
    }    

    @Override
    public boolean isMultipleChoices() {
        return true;
    }


    @Override
    public String nextAction() {
        if (Answer.getInstance().getA(this).equals(IGNORE_ONCE)) {
            return "DownloadFilter";
        }
        
        if (Answer.getInstance().getA(this).equals(IGNORE_ALWAYS)) {
            return "DownloadFilter";
        }
        
        return "LCTVersionDiff";
    }
}
