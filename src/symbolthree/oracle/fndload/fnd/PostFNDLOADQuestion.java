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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/PostFNDLOADQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 1/25/17 9:37a $
 * $Revision: 2 $
******************************************************************************/


package symbolthree.oracle.fndload.fnd;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.Helper;

//~--- JDK imports ------------------------------------------------------------



import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class PostFNDLOADQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/PostFNDLOADQuestion.java 2     1/25/17 9:37a Christopher Ho $";
    private int processResult;

    static final Logger logger = LogManager.getLogger(PostFNDLOADQuestion.class.getName());    
    
    @Override
    public boolean enterQuestion() {
        processResult = Helper.getInt(Answer.getInstance().getB(EXIT_VALUE), 0);

        if (processResult == 0) {
            File file = new File(System.getProperty("user.dir"), "startcp.out");

            if (file.exists()) {
                file.delete();
            }

            file = new File(System.getProperty("user.dir"), "startcp.log");

            if (file.exists()) {
                file.delete();
            }

            // add metadata to output LDT file (DOWNLOAD mode)
            String actionType = Answer.getInstance().getA("OperationMode");

            logger.debug("actionType = " + actionType);
            
            if (actionType.equals("DOWNLOAD") || actionType.equals("DOWNLOAD_NLS")
                    || actionType.equals("DOWNLOAD_WF")) {
                File ldtFile = new File(Answer.getInstance().getB("LDT_DIRECTORY"),
                                        Answer.getInstance().getB("LDT_FILE"));

                try {
                    File               tempfile = File.createTempFile("fndloader", ".tmp");
                    OutputStreamWriter out      = new OutputStreamWriter(new FileOutputStream(tempfile), "UTF-8");
                    BufferedReader     reader   =
                        new BufferedReader(new InputStreamReader(new FileInputStream(ldtFile), "UTF-8"));
                    SimpleDateFormat sdf        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String           timestamp  = sdf.format(Calendar.getInstance().getTime());
                    String           filter     = Answer.getInstance().getA("DownloadFilter");
                    String           module     = Answer.getInstance().getA("DownloadModule");
                    String           objectName = Answer.getInstance().getA("DownloadObject"); 

                    // WFLOAD does not use module and objectName
                    if (Instances.getInstance().isWFLoad()) {
                    	module     = "FND";
                    	objectName = "WORKFLOW";
                    }
                    
                    String           line       = LDT_TAG + " | " + timestamp +  
                    		                                " | " + module +
                    		                                " | " + objectName +
                    		                                " | " + filter;

                    out.write(line + "\n");

                    while ((line = reader.readLine()) != null) {
                        out.write(line + "\n");
                    }

                    out.flush();
                    out.close();
                    reader.close();
                    ldtFile.delete();
                    FileUtils.moveFile(tempfile, ldtFile);
                    //tempfile.renameTo(ldtFile);
                } catch (IOException ioe) {

                    // do nothing
                }
            }
        }

        return true;
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        al.add(new Choice("B", "Back to main menu"));

        return al;
    }

    @Override
    public String getQuestion() {
        String str;

        boolean isWF = Instances.getInstance().isWFLoad();
        String exec = isWF?"WFLOAD":"FNDLOAD"; 
        
        if (processResult == 0) {
            str = exec + " finished successfully.\n";
        } else {
            str = exec + " failed. Please review the log file.\n";
        }

        return str;
    }

    @Override
    public boolean lineWrap() {
        return false;
    }

    @Override
    public String getExplanation() {
        if (Answer.getInstance().getA("OperationMode").equals("DOWNLOAD") && (processResult == 0)) {
            File file = new File(Answer.getInstance().getB(LDT_DIRECTORY), Answer.getInstance().getB(LDT_FILE));

            return "LDT file created: \n" + file.getAbsolutePath();
        } else {
            return super.getExplanation();
        }
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

