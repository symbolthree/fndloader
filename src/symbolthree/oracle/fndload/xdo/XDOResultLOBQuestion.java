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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/XDOResultLOBQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.xdo;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class XDOResultLOBQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/XDOResultLOBQuestion.java 1     11/06/16 1:12a Christopher Ho $";

    static final Logger logger = LogManager.getLogger(XDOResultLOBQuestion.class.getName());
    
    public XDOResultLOBQuestion() {}

    @Override
    public String getQuestion() {
        return "Please select a XDO document to " + Answer.getInstance().getA("OperationMode") + " from "
               + Answer.getInstance().getA("SelectInstance") + ".";
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        try {
            Connection        conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps   = conn.prepareStatement(getSQL());

            ps.setString(1, Answer.getInstance().getA("XDOSelectLOB").toUpperCase());
            ps.setString(2, Answer.getInstance().getA("XDOSelectLOB").toUpperCase());

            ResultSet rs = ps.executeQuery();
            String    key;
            String    option;

            while (rs.next()) {
                key    = rs.getString("LOB_ROW_ID");
                option = rs.getString("MEANING") + ": ";
                rs.getString("FILE_NAME");

                if (rs.getString("LOB_TYPE").equals("TEMPLATE_SOURCE")) {
                    option = option + rs.getString("TEMPLATE_NAME") + " - ";
                    option = option + rs.getString("FILE_NAME") + " (";
                    option = option + rs.getString("LANGUAGE") + ")";
                } else {
                    option = option + rs.getString("DATA_SOURCE_NAME") + " - ";
                    option = option + rs.getString("FILE_NAME");
                }

                al.add(new Choice(key, option));
            }

            rs.close();
            ps.close();
        } catch (Exception e) {
        	logger.catching(e);
        }

        return al;
    }

    private String getSQL() {
        return "  select distinct ROWIDTOCHAR(xl.rowid) LOB_ROW_ID,"
        	   + "       decode(xl.lob_type, 'TEMPLATE', xt.application_short_name || '.' || xt.template_code "
               + "              , 'TEMPLATE_SOURCE', xt.application_short_name || '.' || xt.template_code "
               + "              , null) TAMPLATE_CODE"
               + "      , decode(xl.lob_type, 'TEMPLATE', xt.template_name, 'TEMPLATE_SOURCE', xt.template_name, null) template_name "
               + "      , xd.application_short_name || '.' || xd.data_source_code data_source "
               + "      , xd.data_source_name"
               + "      , flv.meaning"
               + "      , xl.file_name "
               + "      , xl.lob_type"
               + "      , decode(xl.lob_type, 'TEMPLATE', xl.language || '-' || xl.territory, 'TEMPLATE_SOURCE', xl.language || '-' || xl.territory, null) language "
               + "   from XDO_TEMPLATES_VL xt"
               + "      , XDO_DS_DEFINITIONS_VL xd"
               + "      , XDO_LOBS xl "
               + "      , FND_LOOKUP_VALUES_VL FLV"
               + "  where 1=1 "
               + "    and xl.lob_code               in (xd.data_source_code, xt.template_code) "
               + "    and xl.application_short_name = xd.application_short_name         "
               + "    and xd.data_source_code       = xt.data_source_code "
               + "    and xd.application_short_name = xt.ds_app_short_name "
               + "    and flv.lookup_code           = xl.lob_type "
               + "    and flv.lookup_type           = 'XDO_LOB_TYPE' "
               + "    and xl.lob_type               <> 'TEMPLATE'"
               + "    and (upper(xd.data_source_code) like ? or upper(xt.template_code)like ?) order by 2";
    }

    @Override
    public boolean lineWrap() {
        return false;
    }

    @Override
    public String nextAction() {
        return "DownloadXDOPath";
    }

    @Override
    public String lastAction() {
        return "XDOSelectLOB";
    }
}

