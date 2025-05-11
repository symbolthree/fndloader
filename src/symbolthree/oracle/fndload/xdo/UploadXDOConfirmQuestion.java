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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/xdo/UploadXDOConfirmQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/22/16 10:57a $
 * $Revision: 2 $
******************************************************************************/


package symbolthree.oracle.fndload.xdo;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.FilenameUtils;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;
import symbolthree.flower.Answer;
import symbolthree.flower.Choice;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.util.ArrayList;
import java.util.Properties;

public class UploadXDOConfirmQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/xdo/UploadXDOConfirmQuestion.java 2     11/22/16 10:57a Christopher Ho $";
    private Properties parameters = new Properties();

    static final Logger logger = LogManager.getLogger(UploadXDOConfirmQuestion.class.getName());
    
    public UploadXDOConfirmQuestion() {}

    @Override
    public boolean enterQuestion() {
        try {
            String            file     = Answer.getInstance().getA("UploadXDOFile");
            String            fileName = FilenameUtils.getName(file);
            Connection        conn     = DBConnection.getInstance().getConnection();
            PreparedStatement ps       = conn.prepareStatement(getSQL());

            ps.setString(1, fileName.toUpperCase());

            ResultSet         rs  = ps.executeQuery();
            ResultSetMetaData rmd = rs.getMetaData();

            rs.next();

            for (int i = 1; i <= rmd.getColumnCount(); i++) {
                parameters.put(rmd.getColumnName(i), rs.getString(i));
            }

            rs.close();
            ps.close();
        } catch (Exception e) {
            logger.catching(e);
        }

        return true;
    }

    @Override
    public String getQuestion() {
        return "Please verify the following:\n";
    }

    @Override
    public String getExplanation() {
    	
    	File ldtFile = new File(Answer.getInstance().getA("UploadXDOFile"));
    	
        String str =  "Directory       : " + ldtFile.getParent() + "\n" +
        		      "Filename        : " + ldtFile.getName() + "\n" + 
                      "LOB Type        : " + parameters.get("MEANING") + "\n";

        if (parameters.get("LOB_TYPE").equals("TEMPLATE_SOURCE")) {
          str = str + "Lang / Terr     : " + 
                        parameters.get("LANGUAGE") + " / " + 
                        parameters.get("TERRITORY") + "\n" + 
            		  "Template Code   : " + parameters.get("LOB_CODE") + "\n" + 
                      "Template Name   : " + parameters.get("TEMPLATE_NAME") + "\n";
        }

        str = str +   "Data Source Code: " + parameters.get("DATA_SOURCE_CODE") + "\n" + 
                      "Data Source Name: " + parameters.get("DATA_SOURCE_NAME");

        return str;
    }

    @Override
    public boolean fixMultipleChoiceHeight() {
        return false;
    }

    @Override
    public boolean lineWrap() {
        return false;
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        al.add(new Choice((String) parameters.get("LOB_ROW_ID"), "Confirm and upload"));
        al.add(new Choice("CHANGE", "Change settings"));

        return al;
    }

    @Override
    public String lastAction() {
        return "UploadXDOFile";
    }

    @Override
    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        if (ans.equals("CHANGE")) {
            Answer.getInstance().putA(this, null);

            return "UploadXDOType";
        } else {
            return "SaveConfig";
        }
    }

    private String getSQL() {
        return "select ROWIDTOCHAR(xl.rowid) LOB_ROW_ID                        "
               + "     , xl.lob_type                                             "
               + "     , xl.lob_code                                             "
               + "     , xl.application_short_name                               "
               + "     , flv.meaning                                             "
               + "     , xl.language                                             "
               + "     , xl.territory                                            "
               + "     , xt.template_name                                        "
               + "     , xd.data_source_name                                     "
               + "     , xt.data_source_code                                     "
               + "  from XDO_TEMPLATES_VL xt                                     "
               + "     , XDO_DS_DEFINITIONS_VL xd                                "
               + "     , XDO_LOBS xl                                             "
               + "     , FND_LOOKUP_VALUES_VL flv                                "
               + " where 1=1                                                     "
               + "   and xd.data_source_code       = xt.data_source_code         "
               + "   and xd.application_short_name = xt.ds_app_short_name        "
               + "   and xl.lob_code in (xd.data_source_code, xt.template_code)  "
               + "   and flv.lookup_code           = xl.lob_type                 "
               + "   and xl.application_short_name = xd.application_short_name   "
               + "   and upper(xl.file_name)       = ?                           ";
    }
}

