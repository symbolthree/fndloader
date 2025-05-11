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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/UploadXDOMatchAction.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.xdo;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.FilenameUtils;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.common.FNDLOADERActionBase;
import symbolthree.flower.Answer;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class UploadXDOMatchAction extends FNDLOADERActionBase {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/UploadXDOMatchAction.java 1     11/06/16 1:12a Christopher Ho $";
    private int lobMatched = 0;
    static final Logger logger = LogManager.getLogger(UploadXDOMatchAction.class.getName());
    
    @Override
    public void execute() {
        try {
            String file     = Answer.getInstance().getA("UploadXDOFile");
            String fileName = FilenameUtils.getName(file);

            logger.debug("Filename checking: " + fileName);

            Connection        conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps   = conn.prepareStatement(getCheckSQL());

            ps.setString(1, fileName.toUpperCase());

            ResultSet rs = ps.executeQuery();

            rs.next();
            lobMatched = rs.getInt(1);
            logger.debug("Filename matched:" + lobMatched);
            rs.close();
            ps.close();
        } catch (Exception e) {
            logger.catching(e);
        }
    }

    @Override
    public String nextAction() {
        if (lobMatched == 1) {
            return "UploadXDOConfirm";
        } else {
            return "UploadXDOType";
        }
    }

    private String getCheckSQL() {
        return "select count(*) from (" + "select distinct xl.lob_code, xl.lob_type" + "  from XDO_TEMPLATES_VL xt"
               + "     , XDO_DS_DEFINITIONS_VL xd" + "     , XDO_LOBS xl" + " where 1=1"
               + "   and xd.data_source_code       = xt.data_source_code"
               + "   and xd.application_short_name = xt.ds_app_short_name"
               + "   and xl.lob_code in (xd.data_source_code, xt.template_code)"
               + "   and xl.application_short_name = xd.application_short_name"
               + "   and upper(xl.file_name)       = ?)";
    }
}

