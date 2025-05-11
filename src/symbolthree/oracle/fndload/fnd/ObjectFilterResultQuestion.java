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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/fnd/ObjectFilterResultQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.fnd;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;
import symbolthree.flower.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.sql.*;

import java.util.ArrayList;

import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class ObjectFilterResultQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/fnd/ObjectFilterResultQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private String  desc      = null;
    private String  filter    = null;
    private String  msg       = null;
    private int     noOfFound = 0;
    private boolean isWFLoad;
    static final Logger logger = LogManager.getLogger(ObjectFilterResultQuestion.class.getName());    

    @Override
    public boolean enterQuestion() {
        try {
            isWFLoad = Instances.getInstance().isWFLoad();

            String name = Answer.getInstance().getA("DownloadObject");
            String itemCountQuery;

            filter = Answer.getInstance().getA("DownloadFilter");

            if (isWFLoad) {
                itemCountQuery = Config.getInstance().getWFItemCount();
                desc           = "Wokflow Item Type";
            } else {
                itemCountQuery = Config.getInstance().getValidateSQL(name);
                desc           = Config.getInstance().getKeyDescByName(name);
            }

            logger.debug("itemCountQuery: " + itemCountQuery);

            Connection        conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps   = conn.prepareStatement(itemCountQuery);

            ps.setString(1, filter);

            ResultSet rs = ps.executeQuery();

            rs.next();
            noOfFound = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (Exception e) {
            msg = e.getLocalizedMessage();
            logger.catching(e);

            return false;
        }

        return true;
    }

    @Override
    public Message enterQuestionMsg(boolean flag) {
        if (!flag) {
            return new Message(Message.ERROR, msg);
        } else {
            return null;
        }
    }

    @Override
    public ArrayList<Choice> choices() {
        ArrayList<Choice> al = new ArrayList<Choice>();

        if (noOfFound > 0) {
            al.add(new Choice("N", "Next"));
            al.add(new Choice("SH", "Show selected"));
        }

        al.add(new Choice("SA", "Search again"));

        return al;
    }

    @Override
    public String getQuestion() {
        String str = noOfFound + " " + desc + "(s) found in instance " + Answer.getInstance().getA("SelectInstance")
                     + ".";

        return str;
    }

    @Override
    public String getExplanation() {
        return "Filter: " + Answer.getInstance().getA("DownloadFilter");
    }

    @Override
    public String lastAction() {
        return "DownloadObject";
    }

    @Override
    public boolean leaveQuestion() {
        String ans = Answer.getInstance().getA(this);

        if (Instances.getInstance().isWFLoad() && ans.equals("N") && (noOfFound != 1)) {
            msg = "You must select only 1 Workflow Item Type to download";

            return false;
        }

        return true;
    }

    @Override
    public String nextAction() {
        String ans = Answer.getInstance().getA(this);

        if (ans.equals("N")) {
            return "OutputLDT";
        }

        if (ans.equals("SA")) {
            return "DownloadFilter";
        }

        if (ans.equals("SH")) {
            Answer.getInstance().putB(OBJECT_TOTAL_COUNT, String.valueOf(noOfFound));

            return "ShowFilterResult";
        }

        return null;
    }

    @Override
    public Message leaveQuestionMsg(boolean flag) {
        if (!flag) {
            return new Message(Message.ERROR, msg);
        } else {
            return null;
        }
    }
}
