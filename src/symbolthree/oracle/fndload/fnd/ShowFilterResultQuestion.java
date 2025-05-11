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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/fnd/ShowFilterResultQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/

package symbolthree.oracle.fndload.fnd;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.Helper;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.sql.*;

import java.util.ArrayList;

import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class ShowFilterResultQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/fnd/ShowFilterResultQuestion.java 1     11/06/16 1:12a Christopher Ho $";

    // private int            counter     = 0;
    private String    description = null;
    ArrayList<Choice> al          = new ArrayList<Choice>();
    int               OBJECT_NO_BY_PAGE;

    // private int            objectTotalCount;
    private boolean isWFLoad;

    static final Logger logger = LogManager.getLogger(ShowFilterResultQuestion.class.getName());    
    
    public ShowFilterResultQuestion() {
        OBJECT_NO_BY_PAGE = Helper.getChoiceSize();
    }

    @Override
    public boolean enterQuestion() {
        try {
            String op = Answer.getInstance().getA("OperationObject");

            if (op.equals("WF")) {
                isWFLoad = true;
            } else {
                isWFLoad = false;
            }

            // counter = Helper.getInt(Answer.getInstance().getB("OBJECT_PAGE_NO"), 0);
            String filter    = Answer.getInstance().getA("DownloadFilter");
            String name      = null;
            String resultSQL = null;

            if (isWFLoad) {
                resultSQL   = Config.getInstance().getWFFilterResultSQL(1, Helper.getChoiceMax());
                description = "Workflow Item Type";
            } else {
                name        = Answer.getInstance().getA("DownloadObject");
                resultSQL   = Config.getInstance().getFilterResultSQL(name, 1, Helper.getChoiceMax());
                description = Config.getInstance().getDescriptionByName(name);
            }

            // objectTotalCount = Helper.getInt(Answer.getInstance().getB("OBJECT_TOTAL_COUNT"), 0);
            logger.debug(resultSQL);

            Connection        conn     = DBConnection.getInstance().getConnection();
            PreparedStatement prepStmt = conn.prepareStatement(resultSQL);

            prepStmt.setString(1, filter);

            ResultSet rs = prepStmt.executeQuery();

            while (rs.next()) {
                if (isWFLoad) {
                    al.add(new Choice(rs.getString(1), rs.getString(1) + " - " + rs.getString(2)));
                } else {
                    al.add(new Choice(rs.getString(1), rs.getString(2)));
                }
            }
        } catch (Exception e) {
            logger.catching(e);
        }

        return true;
    }

    @Override
    public String getQuestion() {
        return "Selected " + description + ":";
    }

    @Override
    public String getExplanation() {
        StringBuffer sb = new StringBuffer();

        sb.append("Filter is " + Answer.getInstance().getA("DownloadFilter") + "\n");
        sb.append("Please select one of them to download.");

        return sb.toString();
    }

    @Override
    public ArrayList<Choice> choices() {
        return al;
    }

    @Override
    public String nextAction() {
        String str = Answer.getInstance().getA(this);

        Answer.getInstance().putA("DownloadFilter", str);

        return "FilterResult";
    }

    @Override
    public boolean lineWrap() {
        return false;
    }
}
