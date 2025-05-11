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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/Config.java $
 * $Author: Christopher Ho $
 * $Date: 2/06/17 3:37a $
 * $Revision: 4 $
******************************************************************************/



package symbolthree.oracle.fndload;

//~--- non-JDK imports --------------------------------------------------------

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import symbolthree.flower.Answer;
import symbolthree.flower.Choice;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class Config implements Constants {
    private static String      ATTRIBUTE_NODE = "A";
    private static String      ELEMENT_NODE   = "E";
    public static final String RCS_ID         =
        "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/Config.java 4     2/06/17 3:37a Christopher Ho $";
    private static Document document = null;
    private static Config   myConfig = null;
    private XPathFactory    xpfac    = XPathFactory.instance();
    private File            configFile;
    static final Logger logger = LogManager.getLogger(Config.class.getName());
    
    protected Config() {
        try {
            SAXBuilder builder = new SAXBuilder();
            configFile = new File(System.getProperty("user.dir") + File.separator + CONFIG_FILENAME);
            logger.debug("Using config file " + configFile.getCanonicalPath());
            document = builder.build(configFile);
        } catch (JDOMException je) {
            logger.catching(je);
        } catch (IOException ioe) {
        	logger.catching(ioe);
        }
    }

    public static Config getInstance() {
        if (myConfig == null) {
            myConfig = new Config();
        }

        return myConfig;
    }

    public String getVersion() {
        InputStream is   = Config.class.getResourceAsStream("/build.properties");
        Properties  prop = new Properties();

        try {
            prop.load(is);
            is.close();
        } catch (Exception e) {}

        return prop.getProperty("build.version", PROGRAM_VERSION) + " build " + prop.getProperty("build.number");
    }

    public ArrayList<Choice> getAllModules(boolean nls) throws Exception {
        String xpStr = "";

        if (nls) {
            xpStr = "/FNDLOADER/module/object[@translatable='Yes']/parent::module/@name";
        } else {

            // xpStr = "/FNDLOADER/module/object/parent::module/@name";
            xpStr = "/FNDLOADER/module[@name!='XDO']/@name";
        }

        XPathExpression<Attribute> xp         = xpfac.compile(xpStr, Filters.attribute());
        ArrayList<Choice>          al         = new ArrayList<Choice>();
        List<Attribute>            eles       = xp.evaluate(document);
        Iterator<Attribute>        itr        = eles.iterator();
        String                     moduleName = null;
        String                     moduleDesc = null;

        while (itr.hasNext()) {
            moduleName = itr.next().getValue();
            xp         = xpfac.compile("/FNDLOADER/module[@name='" + moduleName + "']/@description",
                                       Filters.attribute());
            moduleDesc = xp.evaluateFirst(document).getValue();
            al.add(new Choice(moduleName, ((moduleName.length() == 2)
                                           ? moduleName + " "
                                           : moduleName) + " - " + moduleDesc));
        }

        return al;
    }

    public ArrayList<Choice> getAllObjectType(String module, boolean nls) throws Exception {
        ArrayList<Choice> allChoices = new ArrayList<Choice>();
        String            xpStr      = "";

        if (nls) {
            xpStr = "/FNDLOADER/module[@name='" + module + "']/object[@translatable='Yes']/@name";
        } else {
            xpStr = "/FNDLOADER/module[@name='" + module + "']/object/@name";
        }

        XPathExpression<Attribute> xp    = xpfac.compile(xpStr, Filters.attribute());
        List<Attribute>            eles1 = xp.evaluate(document);
        Iterator<Attribute>        itr   = eles1.iterator();

        while (itr.hasNext()) {
            String name = ((Attribute) itr.next()).getValue();

            xp = xpfac.compile("//object[@name=\"" + name + "\"" + "]/@description", Filters.attribute());

            String description = xp.evaluateFirst(document).getValue();

            allChoices.add(new Choice(name, description));
        }

        return allChoices;
    }

    public String getDescriptionByName(String name) throws FNDLOADERException {
        return getSingleXPathValue("//object[@name='" + name + "']/@description", ATTRIBUTE_NODE);
    }
    
    public boolean getCheckVerByName(String name) throws FNDLOADERException {
        String checkVer = getSingleXPathValue("//object[@name='" + name + "']/controlFileName/@checkVer", ATTRIBUTE_NODE);
        if (checkVer.equalsIgnoreCase("YES")) {
        	return true;
        } else {
        	return false;
        }
    }

    public String getKeyByName(String name) throws FNDLOADERException {
        return getSingleXPathValue("//object[@name=\"" + name + "\"" + "]/key", ELEMENT_NODE);
    }

    public String getKeyColumnByName(String name) throws FNDLOADERException {
        return getSingleXPathValue("//object[@name=\"" + name + "\"" + "]/tableName/@keyColumn", ATTRIBUTE_NODE);
    }

    public String getKeyDescByName(String name) throws FNDLOADERException {
        return getSingleXPathValue("//object[@name=\"" + name + "\"" + "]/key/@description", ATTRIBUTE_NODE);
    }

    public String getDescByControlFileName(String controlFile) throws FNDLOADERException {
        return getSingleXPathValue("//object[controlFileName=\"" + controlFile + "\"]/@description", ATTRIBUTE_NODE);
    }

    public String getControlFileByName(String name, Properties vars) throws FNDLOADERException {
        String filePath = getSingleXPathValue("/FNDLOADER/module/object[@name='" + name
                              + "']/parent::module/@controlFilePath", ATTRIBUTE_NODE);
        String fileName = getSingleXPathValue("//object[@name='" + name + "']/controlFileName", ELEMENT_NODE);
        char   sep      = File.separatorChar;
        String fullName = filePath + sep + fileName;

        if (sep == '\\') {
            fullName = fullName.replace('/', sep);
        }

        fullName = replaceProperties(fullName, vars);

        // expand system variable
        return fullName;
    }

    public String getModuleByName(String name) throws FNDLOADERException {
    	return getSingleXPathValue("//object[@name=\"" + name + "\"" + "]/parent::module/@name", ATTRIBUTE_NODE);
    }
    
    public String getValidateSQL(String objName) throws FNDLOADERException {
        String tableName  = getSingleXPathValue("//object[@name='" + objName + "']/tableName", ELEMENT_NODE);
        String columnName = getSingleXPathValue("//object[@name='" + objName + "']/tableName/@keyColumn",
                                ATTRIBUTE_NODE);

        return "SELECT COUNT(DISTINCT " + columnName + ") FROM " + tableName + " WHERE " + columnName + " LIKE ?";
    }

    public String getWFItemCount() {
        return "SELECT COUNT(*) FROM WF_ITEM_TYPES_TL WHERE LANGUAGE='US' AND NAME LIKE ?";
    }

    public String getNameByControlFileName(String controlFile) throws Exception {
        return getSingleXPathValue("//object[controlFileName=\"" + controlFile + "\"]/@name", ATTRIBUTE_NODE);
    }

    
    public String getClientControlFileVer(String controlFile) {
    	return "";
    }

    public String getServerControlFileVer(String controlFile) {
    	return "";
/*
select c.version
  from AD_SNAPSHOT_FILES a
     , AD_FILES b
     , AD_FILE_VERSIONS c
     , AD_SNAPSHOTS d
 where 1 = 1 
   and a.file_id=b.file_id
   and a.file_id=c.file_id
   and c.file_version_id=a.file_version_id
   and d.snapshot_id=a.snapshot_id
   and d.snapshot_name='GLOBAL_VIEW'
   and b.filename='afcpprog.lct'  
*/
    }
    
    public String getWFFilterResultSQL(int _from, int _to) throws FNDLOADERException {
        String sql = "SELECT VIEW1.NAME, VIEW1.DISPLAY_NAME FROM ( "
                     + "       SELECT ROW_NUMBER() OVER (ORDER BY NAME) ROW_NO, NAME "
                     + "            , DISPLAY_NAME FROM WF_ITEM_TYPES_TL "
                     + "        WHERE LANGUAGE='US' AND NAME LIKE ? ORDER BY NAME) VIEW1 "
                     + "        WHERE VIEW1.ROW_NO BETWEEN " + _from + " AND " + _to;

        return sql;
    }

    public String getFilterResultSQL(String objName, int _from, int _to) throws FNDLOADERException {
        String tableName  = getSingleXPathValue("//object[@name='" + objName + "']/tableName", ELEMENT_NODE);
        String columnName = getSingleXPathValue("//object[@name='" + objName + "']/tableName/@keyColumn",
                                ATTRIBUTE_NODE);
        String additionalColumn = getSingleXPathValue("//object[@name='" + objName + "']/tableName/@additionalColumn",
                                      ATTRIBUTE_NODE);
        String sql;

        if (additionalColumn != null) {
            sql = "SELECT V2." + columnName + ", V2." + columnName + " || ' - ' || V2." + additionalColumn
                  + " FROM " + " (SELECT ROWNUM ROW_NO " + ", V1." + columnName + ", V1." + additionalColumn
                  + " FROM (  SELECT DISTINCT " + columnName + ", " + additionalColumn + " FROM " + tableName
                  + " WHERE " + columnName + " LIKE ?  " + " ORDER BY " + columnName + ") V1) V2 WHERE "
                  + " V2.ROW_NO BETWEEN " + _from + " AND " + _to;
        } else {
            sql = "SELECT V2." + columnName + ", V2." + columnName + " FROM (SELECT ROWNUM ROW_NO "
                  + ", V1." + columnName + " FROM (  SELECT DISTINCT " + columnName + " FROM " + tableName + " WHERE "
                  + columnName + " LIKE ?  " + " ORDER BY " + columnName + ") V1) V2 WHERE "
                  + " V2.ROW_NO BETWEEN " + _from + " AND " + _to;
        }

        return sql;
    }

    public static String replaceAll(String target, String from, String to) {

        // target is the original string
        // from   is the string to be replaced
        // to     is the string which will used to replace
        // returns a new String!
        int start = target.indexOf(from);

        if (start == -1) {
            return target;
        }

        int          lf          = from.length();
        char[]       targetChars = target.toCharArray();
        StringBuffer buffer      = new StringBuffer();
        int          copyFrom    = 0;

        while (start != -1) {
            buffer.append(targetChars, copyFrom, start - copyFrom);
            buffer.append(to);
            copyFrom = start + lf;
            start    = target.indexOf(from, copyFrom);
        }

        buffer.append(targetChars, copyFrom, targetChars.length - copyFrom);

        return buffer.toString();
    }

    public static String replaceProperties(String value, Properties keys) {
        if (value == null) {
            return null;
        }

        Vector<String> fragments    = new Vector<String>();
        Vector<String> propertyRefs = new Vector<String>();

        parsePropertyString(value, fragments, propertyRefs);

        StringBuffer        sb = new StringBuffer();
        Enumeration<String> i  = fragments.elements();
        Enumeration<String> j  = propertyRefs.elements();

        while (i.hasMoreElements()) {
            String fragment = (String) i.nextElement();

            if (fragment == null) {
                String propertyName = (String) j.nextElement();
                Object replacement  = null;

                // try to get it from the project or keys
                // Backward compatibility
                if (keys != null) {
                    replacement = keys.get(propertyName);
                }

                fragment = (replacement != null)
                           ? replacement.toString()
                           : "${" + propertyName + "}";
            }

            sb.append(fragment);
        }

        return sb.toString();
    }

    private static void parsePropertyString(String value, Vector<String> fragments, Vector<String> propertyRefs) {
        int prev = 0;
        int pos;

        // search for the next instance of $ from the 'prev' position
        while ((pos = value.indexOf("$", prev)) >= 0) {

            // if there was any text before this, add it as a fragment
            // TODO, this check could be modified to go if pos>prev;
            // seems like this current version could stick empty strings
            // into the list
            if (pos > 0) {
                fragments.addElement(value.substring(prev, pos));
            }

            // if we are at the end of the string, we tack on a $
            // then move past it
            if (pos == (value.length() - 1)) {
                fragments.addElement("$");
                prev = pos + 1;
            } else if (value.charAt(pos + 1) != '{') {

                // peek ahead to see if the next char is a property or not
                // not a property: insert the char as a literal

                /*
                 * fragments.addElement(value.substring(pos + 1, pos + 2));
                 * prev = pos + 2;
                 */
                if (value.charAt(pos + 1) == '$') {

                    // backwards compatibility two $ map to one mode
                    fragments.addElement("$");
                    prev = pos + 2;
                } else {

                    // new behaviour: $X maps to $X for all values of X!='$'
                    fragments.addElement(value.substring(pos, pos + 2));
                    prev = pos + 2;
                }
            } else {

                // property found, extract its name or bail on a typo
                int    endName      = value.indexOf('}', pos);
                String propertyName = value.substring(pos + 2, endName);

                fragments.addElement(null);
                propertyRefs.addElement(propertyName);
                prev = endName + 1;
            }
        }

        // no more $ signs found
        // if there is any tail to the file, append it
        if (prev < value.length()) {
            fragments.addElement(value.substring(prev));
        }
    }

    private String getSingleXPathValue(String xpath, String nodeType) {
        String rtnVal = null;

        if (nodeType.equals(ATTRIBUTE_NODE)) {
            XPathExpression<Attribute> xp   = xpfac.compile(xpath, Filters.attribute());
            Attribute                  attr = xp.evaluateFirst(document);

            if (attr != null) {
                rtnVal = attr.getValue();
            }
        }

        if (nodeType.equals(ELEMENT_NODE)) {
            XPathExpression<Element> xp  = xpfac.compile(xpath, Filters.element());
            Element                  ele = xp.evaluateFirst(document);

            if (ele != null) {
                rtnVal = ele.getValue();
            }
        }

        return rtnVal;
    }
    
    // TODO
    public String getLocalControlFile(String objectType) {
        Properties vars    = new Properties();
        String controlFile = null;
        try {
	        String sid      = Answer.getInstance().getA("SelectInstance");
	        String releaseName = getReleaseName(sid);
	        Map<String, String> systemEnv = System.getenv();    	
	        String applTop      = systemEnv.get("APPL_TOP");
	        
	        if ((applTop == null) || applTop.equals("")) {
	            applTop = System.getProperty("user.dir") + File.separator + "CLIENT" + File.separator + 
	            		  releaseName + File.separator + "APPL_TOP";
	        }
	        
	        logger.debug("APPL_TOP = " + applTop);
	        
	        vars.put("APPL_TOP", applTop);
	        vars.put(RELEASE_NAME, releasePath(releaseName));
	
	        controlFile = getControlFileByName(objectType, vars);
        } catch (Exception e) {
        	logger.catching(e);
        }
        return controlFile;
    }
    
    public String getServerLCTVer(String _file, String _module) throws Exception {
    	logger.debug("getServerLCTVer of module " + _module);
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
		        		"   --and b.subdir          = 'patch/115/import'\r\n" +
		        		"   and b.app_short_name  = NVL(?, b.app_short_name) \r\n" +
		                "   and b.filename        = ?";
        
        Connection conn = DBConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, _module);
        ps.setString(2, fileName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
        	ver = rs.getString(4);
        }
        rs.close();
        ps.close();
        return ver;
    }    
    
    
    
    public String getReleaseName(String sid) throws Exception {
    	String releaseName = null;

        releaseName = Instances.getInstance().getReleaseName(sid);        
        logger.debug("releaseName (from instance file) = " + releaseName);    	
    	
        if (releaseName == null || releaseName.equals("")) {
        	releaseName = getReleaseNameFromDB();
        }
        return releaseName;
    	
    }    
    

    public String getReleaseNameFromDB() throws Exception {
    	String releaseName = null;
    	
        Connection conn = DBConnection.getInstance().getConnection();
    	
        String sql = "SELECT RELEASE_NAME FROM FND_PRODUCT_GROUPS";
        ResultSet rs  = conn.createStatement().executeQuery(sql);

        while (rs.next()) {
            releaseName = rs.getString(1);
        }

        rs.close();
        
        if (releaseName.startsWith("12.1")) {
            releaseName = RELEASE_121;
        } else if (releaseName.startsWith("12.2")) {
        	releaseName = RELEASE_122;
        } else if (releaseName.startsWith("11.5")) {
            releaseName = RELEASE_11i;
        } else {
            releaseName = "UNKNOWN";
        }
        logger.debug("releaseName (from DB) = " + releaseName);
        return releaseName;
    }
    
    public String getLocalLCTVer(String _file) {
    	String ver = null;
    	try {
  		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(_file), "UTF8"));
		String str;
		while ((str = reader.readLine()) != null) {
		    if (str.indexOf("Header") > 0) break;
		}
        reader.close();
        
        // the pattern is -- $ Header: <filename> <version> <timestamp> <author> ship $ 
        str = str.substring(str.indexOf("Header"));
        ver = str.split(" ")[2];
    	} catch (Exception e) {
    		logger.catching(e);
    	}
        return ver;
    }
    
    public static String releasePath(String release) {
    	if (release.equals(RELEASE_11i)) return "11.5.0";
    	if (release.equals(RELEASE_121)) return "12.0.0";
    	if (release.equals(RELEASE_122)) return "12.0.0";    	
    	return release;
    }
    
    /*
     * return 32, 64 or null 
     */
    public static String winArch() {
    	String rtnVal = null;
        String osName = System.getProperty("os.name");
        logger.debug("os info: " + osName);
        if (osName.toUpperCase().startsWith("WIN")) {
        	String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        	String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        	rtnVal = arch.endsWith("64")
        	                  || wow64Arch != null && wow64Arch.endsWith("64")
        	                      ? "64" : "32";     
        	logger.debug("os arch " + rtnVal);
        }
        return rtnVal;
    }
}
