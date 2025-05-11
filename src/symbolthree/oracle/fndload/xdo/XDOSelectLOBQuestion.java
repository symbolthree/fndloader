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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/xdo/XDOSelectLOBQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/22/16 10:57a $
 * $Revision: 2 $
******************************************************************************/


package symbolthree.oracle.fndload.xdo;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class XDOSelectLOBQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/xdo/XDOSelectLOBQuestion.java 2     11/22/16 10:57a Christopher Ho $";

    static final Logger logger = LogManager.getLogger(XDOSelectLOBQuestion.class.getName());
    
    public XDOSelectLOBQuestion() {}

    @Override
    public String getQuestion() {
        return "Please enter the Template Code or DataSource Code of the document you want to "
               + Answer.getInstance().getA("OperationMode") + " from instance "
               + Answer.getInstance().getA("SelectInstance") + ".\n";
    }

    @Override
    public String getExplanation() {
        return "Wildcard % is allowed.  Case is not sensitive.";
    }

    @Override
    public boolean isMultipleChoices() {
        return false;
    }

    @Override
    public int minTextInputLength() {
        return 3;
    }

    @Override
    public boolean leaveQuestion() {
        try {
            Connection        conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps   = conn.prepareStatement(getSQL());
            String            ans  = Answer.getInstance().getA(this).toUpperCase();

            ps.setString(1, ans);
            ps.setString(2, ans);
            logger.debug("SQL: " + getSQL());
            logger.debug("ANS: " + ans);

            ResultSet rs = ps.executeQuery();

            rs.next();

            int noOfRow = rs.getInt(1);

            rs.close();
            ps.close();
            logger.info("No. of template found: " + noOfRow);

            if (noOfRow == 0) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.catching(e);
        }

        return super.leaveQuestion();
    }

    @Override
    public Message leaveQuestionMsg(boolean flag) {
        if (!flag) {
            return new Message(Message.ERROR, "No XML Publisher template can be found");
        } else {
            return null;
        }
    }

    @Override
    public String nextAction() {
        return "XDOResultLOB";
    }

    @Override
    public String lastAction() {
        return "OAImporter";
    }

    private String getSQL() {
        return "  select count(*) "
        	   + "  from XDO_TEMPLATES_VL xt"
        	   + "     , XDO_DS_DEFINITIONS_VL xd "
               + "     , XDO_LOBS xl "
               + " where 1=1 "
               + "   and xl.lob_code in (xd.data_source_code, xt.template_code) "
               + "   and xl.application_short_name = xd.application_short_name "
               + "   and xd.data_source_code       = xt.data_source_code "
               + "   and xd.application_short_name = xt.ds_app_short_name "
               + "   and xl.lob_type               <> 'TEMPLATE' "
               + "   and (upper(xd.data_source_code) like ? or upper(xt.template_code) like ? )";
    }
}

