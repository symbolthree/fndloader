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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/UploadXDOSettingQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.xdo;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.Helper;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class UploadXDOSettingQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/UploadXDOSettingQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private String                    currentAns = null;
    private String                    lobType    = null;
    private String                    lovAns     = null;
    private Hashtable<String, String> ht         = new Hashtable<String, String>();

    static final Logger logger = LogManager.getLogger(UploadXDOSettingQuestion.class.getName());    
    
    public UploadXDOSettingQuestion() {
        currentAns = Answer.getInstance().getA(this);
        lobType    = Answer.getInstance().getA("UploadXDOType");
        lovAns     = Answer.getInstance().getA("UploadXDOLOV");
    }

    @Override
    public boolean enterQuestion() {
    	
    	logger.debug("Entering UploadXDOSetting...");
    	logger.debug("currentAns(LOV type)=" + currentAns);
    	logger.debug("isLOVAnswer=" + Helper.isLOVAnswer(lovAns));

        if (Helper.isLOVAnswer(lovAns)) {
            try {
                String lovType = Answer.getInstance().getA(this);

                if (lovType.equals("TEMPLATE") || lovType.equals("LANGUAGE") || lovType.equals("TERRITORY")) {
                    fetchValues("TEMPLATE");
                    fetchValues("LANGUAGE");
                    fetchValues("TERRITORY");
                }

                if (lovType.equals("DATA_SOURCE")) {
                    fetchValues("DATA_SOURCE");
                }
            } catch (Exception e) {
                logger.catching(e);
            }
        }

        // reset LOV values
        Answer.getInstance().putA("UploadXDOLOV", null);

        return true;
    }

    @Override
    public String getQuestion() {
    	logger.debug("Entering getQuestion...");

        return "Please provide the following settings for this file";
    }

    @Override
    public String getExplanation() {
    	logger.debug("Entering getExplanation...");

        String str = "File: " + Answer.getInstance().getA("UploadXDOFile") + "\n";

        str = str + "Type: " + lobType + "\n";

        if (lobType.equals("TEMPLATE_SOURCE") || lobType.equals("TEMPLATE")) {
            str = str + "Template Code : " + getValue("TEMPLATE_CODE") + "\n";
            str = str + "Template Name : " + getValue("TEMPLATE_NAME") + "\n";
            str = str + "Language      : " + getValue("LANGUAGE") + "\n";
            str = str + "Territory     : " + getValue("TERRITORY") + "\n";
        }

        str = str + "Data Source Code: " + getValue("DATA_SOURCE_CODE") + "\n";
        str = str + "Data Source Name: " + getValue("DATA_SOURCE_NAME") + "\n";

        return str;
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al            = new ArrayList<Choice>();
        boolean           uploadAllowed = false;

        if (lobType.equals("TEMPLATE_SOURCE") || lobType.equals("TEMPLATE")) {
            al.add(new Choice("TEMPLATE", "Select Template"));
            al.add(new Choice("LANGUAGE", "Select Language"));
            al.add(new Choice("TERRITORY", "Select Territory"));

            if ((Answer.getInstance().getB("TEMPLATE_ROWID") != null)
                    && (Answer.getInstance().getB("LANGUAGE_ROWID") != null)) {
                uploadAllowed = true;
            }
        } else {
            al.add(new Choice("DATA_SOURCE", "Select Data Source"));

            if (Answer.getInstance().getB("DATA_SOURCE_ROWID") != null) {
                uploadAllowed = true;
            }
        }

        if (uploadAllowed) {
            al.add(new Choice("UPLOAD", "Confirm and Upload"));
        }

        return al;
    }

    @Override
    public boolean lineWrap() {
        return false;
    }

    @Override
    public boolean fixMultipleChoiceHeight() {
        return false;
    }

    @Override
    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        if (ans.equals("UPLOAD")) {
            return "SaveConfig";
        } else {
            return "UploadXDOLOV";
        }
    }

    @Override
    public String lastAction() {
        return "UploadXDOType";
    }

    private void fetchValues(String lovType) throws Exception {
        Connection        conn  = DBConnection.getInstance().getConnection();
        PreparedStatement ps    = conn.prepareStatement(getSQL(lovType));
        String            rowid = Answer.getInstance().getB(lovType + "_ROWID");
        String            key   = null;

        if ((rowid == null) || rowid.equals("")) {
            key = Helper.getLOVKey(lovAns);
        } else {
            key = rowid;
        }

        ps.setString(1, key);

        ResultSet         rs       = ps.executeQuery();
        ResultSetMetaData metadata = rs.getMetaData();

        while (rs.next()) {
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                ht.put(metadata.getColumnName(i), rs.getString(i));
            }
        }

        rs.close();
        ps.close();
        Answer.getInstance().putB(currentAns + "_ROWID", Helper.getLOVKey(lovAns));
    }

    private String getValue(String key) {
        String s = ht.get(key);

        return (s == null)
               ? ""
               : s;
    }

    private String getSQL(String lovType) {
        String sql = null;

        if (lovType.equals("TEMPLATE")) {
            sql = "select xt.template_name"
                  + "     , xt.application_short_name || '.' || xt.template_code template_code"
                  + "     , xd.data_source_name"
                  + "     , xt.ds_app_short_name || '.' || xd.data_source_code data_source_code"
                  + "  from xdo_templates_vl xt" + "     , xdo_ds_definitions_vl xd" + " where 1=1 "
                  + "   and ROWIDTOCHAR(xt.rowid) = ?" + "   and xt.data_source_code   = xd.data_source_code"
                  + "   and xt.ds_app_short_name  = xd.application_short_name";
        } else if (lovType.equals("DATA_SOURCE")) {
            sql = "select xd.data_source_name" + "     , xd.data_source_code" + "     , application_short_name"
                  + "  from xdo_ds_definitions_vl xd" + " where 1=1 " + "   and ROWIDTOCHAR(rowid)  = ?";
        } else if (lovType.equals("TERRITORY")) {
            sql = "SELECT TERRITORY_SHORT_NAME || ' (' || TERRITORY_CODE || ')' TERRITORY"
                  + "  FROM FND_TERRITORIES_VL " + " WHERE ROWIDTOCHAR(rowid)  = ?";
        } else if (lovType.equals("LANGUAGE")) {
            sql = "SELECT NAME || ' (' || ISO_LANGUAGE_2 || ')' LANGUAGE" + "  FROM FND_ISO_LANGUAGES_VL "
                  + " WHERE ROWIDTOCHAR(rowid)  = ?";
        }

        return sql;
    }
}

