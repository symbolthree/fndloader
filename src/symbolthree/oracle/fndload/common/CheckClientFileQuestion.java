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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/common/CheckClientFileQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/22/16 10:58a $
 * $Revision: 2 $
******************************************************************************/

package symbolthree.oracle.fndload.common;

import symbolthree.oracle.fndload.Config;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;

public class CheckClientFileQuestion extends FNDLOADERQuestion {

	private String releaseName = null;
	private boolean dirExist   = false;
	private static String downloadURL = "http://symbolthree.com/home/products/fndloader/";
	
    static final Logger logger = LogManager.getLogger(CheckClientFileQuestion.class.getName());
    
    
	public CheckClientFileQuestion() {
	}

    @Override
    public boolean enterQuestion() {
        String sid = Answer.getInstance().getA("SelectInstance");
        try {
			releaseName = Config.getInstance().getReleaseName(sid);
			
			File clientDir = new File(System.getProperty("user.dir") + File.separator + "CLIENT" + File.separator + releaseName);
			if (clientDir.isDirectory() && clientDir.exists()) {
				dirExist = true;
				return false;
			}
		} catch (Exception e) {
			logger.catching(e);
		}
    	return true;
    }	
    
    
    @Override
    public String getQuestion() {
    	return "Client file for EBS release " + releaseName + " does not exist.\n";
    }
    
    @Override
    public boolean isMultipleChoices() {
        return true;
    }   
    
    @Override
    public String getExplanation() {
    	return "Please download file " + releaseName + ".zip from \r" +
    			downloadURL + "\n\n" +
    			"Unzip this file to " + System.getProperty("user.dir") + File.separator + "CLIENT \n" +
    			"then restart the program.";
    }
    
    @Override
    public String nextAction() {
    	if (! dirExist) {
	    	if (Answer.getInstance().getA(this).equals("A")) {
	    		try {
					Desktop.getDesktop().browse(new URI(downloadURL));
				} catch (Exception e) {
					logger.catching(e);
				}
	    	}
    	} else {
          return "OperationMode";
    	}
    	return "CheckClientFile";
    }   
    
    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();
        al.add(new Choice("A", "Open web site for download"));

        return al;
    }    
}
