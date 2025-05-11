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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/UploadXDOLOVQuestion.java $
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

import java.util.ArrayList;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class UploadXDOLOVQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/xdo/UploadXDOLOVQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    String lovType = null;
    static final Logger logger = LogManager.getLogger(UploadXDOLOVQuestion.class.getName());
    
    public UploadXDOLOVQuestion() {
        lovType = Answer.getInstance().getA("UploadXDOSetting");
        logger.debug("lovType=" + lovType);
    }

    @Override
    public String getQuestion() {
        return "Please enter the " + lovType + " for file";
    }

    @Override
    public boolean isMultipleChoices() {
        String ans = Answer.getInstance().getA(this);

        if ((ans != null) &&!ans.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getExplanation() {
        String str = Answer.getInstance().getA("UploadXDOFile") + "\n\n";

        str = str + "Wildcard (%) allowed.  Case is not sensitive.";

        return str;
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        try {
            Connection        conn       = DBConnection.getInstance().getConnection();
            PreparedStatement ps         = conn.prepareStatement(getSQL());
            String            searchWord = Answer.getInstance().getA(this);

            ps.setString(1, searchWord.toUpperCase());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                al.add(new Choice(Helper.setLOVKey(rs.getString(1)), rs.getString(2) + " - " + rs.getString(3)));
            }
        } catch (Exception e) {
            logger.catching(e);
        }

        return al;
    }

    @Override
    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        if (Helper.isLOVAnswer(ans)) {
            return "UploadXDOSetting";
        } else {
            return "UploadXDOLOV";
        }
    }

    @Override
    public String lastAction() {
        return "UploadXDOSetting";
    }

    @Override
    public boolean fixMultipleChoiceHeight() {
        return false;
    }

    private String getSQL() {
        String sql = null;

        if (lovType.equals("TEMPLATE")) {
            sql = "SELECT ROWIDTOCHAR(ROWID), APPLICATION_SHORT_NAME || '.' || TEMPLATE_CODE"
                  + "     , TEMPLATE_NAME FROM XDO_TEMPLATES_VL WHERE UPPER(TEMPLATE_CODE) LIKE ?"
                  + " ORDER BY 3";
        }

        if (lovType.equals("DATA_SOURCE")) {
            sql = "SELECT ROWIDTOCHAR(ROWID), APPLICATION_SHORT_NAME || '.' || DATA_SOURCE_CODE"
                  + "     , DATA_SOURCE_NAME FROM XDO_DS_DEFINITIONS_VL "
                  + " WHERE UPPER(DATA_SOURCE_CODE) LIKE ? ORDER BY 3";
        }

        if (lovType.equals("TERRITORY")) {
            sql = "SELECT ROWIDTOCHAR(ROWID) , TERRITORY_CODE, TERRITORY_SHORT_NAME "
                  + "  FROM FND_TERRITORIES_VL WHERE UPPER(TERRITORY_CODE) LIKE ? ORDER BY 3";
        }

        if (lovType.equals("LANGUAGE")) {
            sql = "SELECT ROWIDTOCHAR(ROWID), ISO_LANGUAGE_2, NAME "
                  + "  FROM FND_ISO_LANGUAGES_VL WHERE UPPER(ISO_LANGUAGE_2) LIKE ? ORDER BY 3";
        }

        return sql;
    }
}

