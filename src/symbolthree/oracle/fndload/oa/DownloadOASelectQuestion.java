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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/DownloadOASelectQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/17/16 10:41a $
 * $Revision: 2 $
******************************************************************************/

package symbolthree.oracle.fndload.oa;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;

import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

public class DownloadOASelectQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/oa/DownloadOASelectQuestion.java 2     11/17/16 10:41a Christopher Ho $";
    private String            oaDocType = null;
    private ArrayList<Choice> choices   = new ArrayList<Choice>();
    static final Logger logger = LogManager.getLogger(DownloadOASelectQuestion.class.getName());
    
    public DownloadOASelectQuestion() {
        oaDocType = Answer.getInstance().getA("DownloadOAType");
    }

    @Override
    public String getQuestion() {
        if (Instances.getInstance().isNLSMode()) {
            return "Please choose the " + Answer.getInstance().getB(OA_NLS_LANG)
                   + " translation from a page, region, or \npersonalization to download from instance "
                   + Answer.getInstance().getA("SelectInstance") + " .\n";
        } else if (oaDocType.equals("C")) {
            return "Please choose the personalization of a page or region to download from instance "
                   + Answer.getInstance().getA("SelectInstance") + " .\n";
        } else if (oaDocType.equals("P")) {
            return "Please choose a page or region definition to download from instance "
                   + Answer.getInstance().getA("SelectInstance") + " .\n";
        }

        return super.getQuestion();
    }

    @Override
    public boolean enterQuestion() {
        try {
            String _page = Answer.getInstance().getA("DownloadOAObj");

            _page = _page.toUpperCase();

            Connection        conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps   = conn.prepareStatement(getSQL());

            ps.setString(1, _page);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                choices.add(new Choice(rs.getString(1), rs.getString(1)));
            }

            rs.close();
            ps.close();
        } catch (Exception e) {
            logger.catching(e);
        }

        return true;
    }

    @Override
    public ArrayList<Choice> choices() {
        return choices;
    }

    @Override
    public boolean lineWrap() {
        return false;
    }

    @Override
    public String lastAction() {
        return "DownloadOAObj";
    }

    @Override
    public String nextAction() {
        return "DownloadOAPath";
    }

    private String getSQL() {
        String sql1 = "SELECT distinct ja.att_value" 
                    + "  FROM jdr_paths jp" 
        		    + "     , jdr_attributes ja"
                    + " WHERE jp.path_docid       = ja.att_comp_docid"
                    + "   AND upper(jp.path_name) like ?"
                    + "   AND ja.att_comp_seq     = 0"
                    + "   AND ja.att_name         = 'customizes'"
                    + " ORDER BY ja.att_value";
        
        String sql2 = "select distinct jdr_mds_internal.getDocumentName(path_docid) doc_path"
        		    + "  from jdr_paths jp"
                    + " where jp.path_type ='DOCUMENT'"
                    + "   and upper(jp.path_name) like ?"
                    + "   and not exists ("
                    + "    select 1 from jdr_attributes ja"
                    + "     where ja.att_comp_docid = jp.path_docid"
                    + "       and ja.att_name       = 'customizes') order by 1";
        
        String sql3 = "select distinct jdr_mds_internal.getDocumentName(path_docid) doc_path "
        		    + "  from jdr_paths jp"
                    + " where jp.path_type ='DOCUMENT'"
                    + "   and upper(jp.path_name) like ?" 
                    + "   and exists ( "
                    + "     select 1 from jdr_attributes_trans jat"
                    + "      where jat.atl_comp_docid=jp.path_docid"
                    + "        and jat.atl_lang='"
                    + Answer.getInstance().getB(OA_NLS_LANG) + "') order by 1";

        if (Instances.getInstance().isNLSMode()) {
            return sql3;
        } else {
            if (oaDocType.equals("C")) {
                return sql1;
            }

            if (oaDocType.equals("P")) {
                return sql2;
            }
        }

        return null;
    }
}
