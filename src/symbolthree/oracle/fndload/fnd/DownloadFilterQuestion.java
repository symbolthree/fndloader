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
 * $Archive: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/fnd/DownloadFilterQuestion.java $
 * $Author: Christopher Ho $
 * $Date: 11/06/16 1:12a $
 * $Revision: 1 $
******************************************************************************/


package symbolthree.oracle.fndload.fnd;

import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERQuestion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

import symbolthree.flower.Answer;

public class DownloadFilterQuestion extends FNDLOADERQuestion {
    public static final String RCS_ID =
        "$Header: /TOOL/FNDLOADER_V3/src/symbolthree/oracle/fndload/fnd/DownloadFilterQuestion.java 1     11/06/16 1:12a Christopher Ho $";
    private boolean isNLSMode;
    private boolean isWFLoad;

	static final Logger logger = LogManager.getLogger(DownloadFilterQuestion.class.getName());
    
    @Override
    public boolean enterQuestion() {
        isNLSMode = Instances.getInstance().isNLSMode();
        isWFLoad  = Instances.getInstance().isWFLoad();

        return true;
    }

    @Override
    public String getQuestion() {
        String objectDesc    = null;
        String objectKeyDesc = null;
        String name          = Answer.getInstance().getA("DownloadObject");

        if (isWFLoad) {
            return "You want to download Workflow Item Type of :";
        }

        try {
            objectDesc = Config.getInstance().getDescriptionByName(name);

            // objectKey     = Config.getInstance().getKeyByName(name);
            objectKeyDesc = Config.getInstance().getKeyDescByName(name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isNLSMode) {
            return "You want to download object translation of " + objectDesc + " with " + objectKeyDesc + " of :";
            
        } else {
            return "You want to download " + objectDesc + " with " + objectKeyDesc + " of :";
        }
    }

    @Override
    public String getExplanation() {
        return "(Wildcard (%) is allowed. Case sensitive)";
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
    public String nextAction() {
        if (Answer.getInstance().getA(this) != "") {
            return "FilterResult";
        } else {
            return "DownloadFilter";
        }
    }
}
