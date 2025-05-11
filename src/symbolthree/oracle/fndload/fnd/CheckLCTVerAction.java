/******************************************************************************
 *
 * ≡≡ FNDLOADER ≡≡ 
 * Copyright (C) 2009-2020 Christopher Ho
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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/CheckLCTVerAction.java $
 * $Author: Christopher Ho $
 * $Date: 2/06/17 3:37a $
 * $Revision: 4 $
******************************************************************************/

package symbolthree.oracle.fndload.fnd;

import symbolthree.flower.Answer;
import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.DBConnection;
import symbolthree.oracle.fndload.Instances;
import symbolthree.oracle.fndload.common.FNDLOADERActionBase;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;

/***************************************************************
 * If LCT server version is HIGHER than local version, then the LDT file created (based on local version)
 * usually good because of backward compatibility. 
 * 
 * If LCT server version is LOWER than local version, then the LDT file created (based on local version)
 * will probably contain extra information that is not compatible to the target server.
 * 
 * FNDLOADER always take the latest LCT version from Oracle.
 *
 ****************************************************************/

public class CheckLCTVerAction extends FNDLOADERActionBase {

	public static final String RCS_ID =
            "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/fnd/CheckLCTVerAction.java 4     2/06/17 3:37a Christopher Ho $";
    
    private String vLocal     = null;
    private String vServer    = null;
    private boolean checkVer  = false;
    private String objectName = null;
    private String sid        = null;
    private String module     = null;
    
	static final Logger logger = LogManager.getLogger(CheckLCTVerAction.class.getName());
    
	@Override
	public boolean enterAction() {
		
        sid        = Answer.getInstance().getA("SelectInstance");
        module     = Answer.getInstance().getA("DownloadModule");
        objectName = Answer.getInstance().getA("DownloadObject");        
        logger.debug(sid + "," + module + "," + objectName);
        checkVer = Instances.getInstance().checkLCTVer(sid, module, objectName);
        logger.debug("Check LCT Ver: " + checkVer);
	    return checkVer;	
	}
	
    @Override
    public void execute() {

        try {
        String controlFile = Config.getInstance().getLocalControlFile(objectName);
        logger.info("controlFile = " + controlFile);

        vLocal  = Config.getInstance().getLocalLCTVer(controlFile);
        vServer = getVersionFromDB(controlFile);
          
        logger.info("Local  version = " + vLocal);
        logger.info("Server version = " + vServer);
          
        Answer.getInstance().putB(LCT_LOCAL_VER, vLocal);
        Answer.getInstance().putB(LCT_SERVER_VER, vServer);
        Answer.getInstance().putB(LCT_FILE, (new File(controlFile)).getName());
        } catch (Exception e) {
        	logger.catching(e);
        }
    }
    
    private String getVersionFromDB(String _file) throws Exception {
        String ver = null;
      	File file  = new File(_file);
    	String fileName = file.getName();
    	
    	// TODO  -- lookup app name and add condition of app_short_name='FND' or others 
        String sql =    "select b.app_short_name\r\n" + 
		        		"     , b.subdir\r\n" + 
		        		"     , b.filename\r\n" + 
		        		"     , c.version\r\n" + 
		        		"     , a.file_type_flag\r\n" + 
		        		"     , a.last_patched_date\r\n" + 
		        		"  from AD_SNAPSHOT_FILES a\r\n" + 
		        		"     , AD_FILES b\r\n" + 
		        		"     , AD_FILE_VERSIONS c\r\n" + 
		        		"     , AD_SNAPSHOTS d\r\n" + 
		        		" where 1 = 1 \r\n" + 
		        		"   and a.file_id         = b.file_id\r\n" + 
		        		"   and a.file_id         = c.file_id\r\n" + 
		        		"   and c.file_version_id = a.file_version_id\r\n" + 
		        		"   and d.snapshot_id     = a.snapshot_id\r\n" + 
		        		"   and d.snapshot_name   = 'GLOBAL_VIEW'\r\n" +
		        		"   and NVL(a.file_type_flag, 'N') = 'N'\r\n" +
		        		"   --and b.subdir          = 'patch/115/import'\r\n" +
		        		"   and b.app_short_name  = ?\r\n" +
		                "   and b.filename        = ?";
        
        Connection conn = DBConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, module);
        ps.setString(2, fileName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
        	ver = rs.getString(4);
        }
        rs.close();
        ps.close();
        return ver;
    }    
    
    @Override
    public String nextAction() {
    	
    	String updownMode = Answer.getInstance().getA("OperationMode");
    	
    	if (updownMode.equals("DOWNLOAD")) {
      	  if (checkVer) {
            if (! vLocal.equals(vServer)) {
      	      return "LCTVersionDiff";        	
            } else {
              return "DownloadFilter";
            }
    	  } else {  
           return "DownloadFilter";
    	  }
    	}
    	
    	//if (updownMode.equals("UPLOAD")) {
    	//	return "LDTInfo";
    	//}
    	
    	return super.nextAction();
    }

}
