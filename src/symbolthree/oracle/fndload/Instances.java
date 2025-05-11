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
 * $Archive: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/Instances.java $
 * $Author: Christopher Ho $
 * $Date: 1/24/17 9:50a $
 * $Revision: 2 $
******************************************************************************/

package symbolthree.oracle.fndload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import oracle.net.nl.NLException;
import oracle.net.nl.NLParamParser;

import org.apache.commons.io.FileUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import symbolthree.flower.Answer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Instances implements Constants {
  public static final String RCS_ID                     =
    "$Header: /TOOL/FNDLOADER_V4/src/symbolthree/oracle/fndload/Instances.java 2     1/24/17 9:50a Christopher Ho $";
  private static Document    document    = null;
  private static Instances   myInstances = null;
  private File               instancesFile;
 
  private XPathFactory  xpfac       = XPathFactory.instance();
  private static String ATTRIBUTE_NODE = "A";
  private static String ELEMENT_NODE   = "E";  
  private String tnsnamesFile          = null;
  
  static final Logger logger = LogManager.getLogger(Instances.class.getName());
  
  protected Instances() {
  	System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
  	 
      try {
          SAXBuilder builder = new SAXBuilder();

          instancesFile = new File(FNDLOADER_APPLICATION_DIR, INSTANCES_FILENAME);
          
          if (!instancesFile.exists()) {
            // create a new INSTANCES.XML
            Element root = new Element("FNDLOADER");
            document = new Document(root);
            
            //String userDir = System.getProperty("user.dir");
            Element ldtDir = new Element(LDT_DIRECTORY);
            ldtDir.setText(FNDLOADER_APPLICATION_DIR + File.separator + "output" + File.separator + "LDT");
            
            Element oaRootDir = new Element(OA_ROOT_DIR);
            oaRootDir.setText(FNDLOADER_APPLICATION_DIR + File.separator + "output" + File.separator + "OA");
            
            Element xdoDir = new Element(XDO_DIRECTORY);
            xdoDir.setText(FNDLOADER_APPLICATION_DIR + File.separator + "output" + File.separator + "XDO");
            
            Element tnsAdmin = new Element("TNS_ADMIN");
            //tnsAdmin.setText(System.getProperty("user.dir"));
            root.addContent(ldtDir);
            root.addContent(oaRootDir);
            root.addContent(xdoDir);
            root.addContent(tnsAdmin);
            
          } else {
            document = builder.build(instancesFile);
          }
      } catch (JDOMException je) {
          logger.catching(je);
          
      } catch (IOException ioe) {
          logger.catching(ioe);;
      }
  }

  public static Instances getInstance() {
      if (myInstances == null) {
          myInstances = new Instances();
      }

      return myInstances;
  }

  public void saveTNSAdminDirectory() {
      String dir = System.getProperty("oracle.net.tns_admin");
      XPathExpression<Element> xp = xpfac.compile("/FNDLOADER/TNS_ADMIN", Filters.element());
      Element ele = xp.evaluateFirst(document);
      ele.setText(dir);
  }
  
  public void saveInstancesFile() {
      try {
          XMLOutputter outputter = new XMLOutputter();

          outputter.setFormat(Format.getPrettyFormat());
          outputter.output(document, new FileOutputStream(instancesFile));
      } catch (IOException e) {
          logger.error("Unable to save instances file " + instancesFile.getAbsolutePath());
      }
  }

  public String getNLSParam(String sid, String NLSParam) {
      String param = getSingleXPathValue("/FNDLOADER/INSTANCE[@SID='" + sid + "']/" + NLSParam, ELEMENT_NODE);
      return param==null?"":param;
  }
  
  
  public void saveDirectory(String _type) {
      String dir = Answer.getInstance().getB(_type);
      XPathExpression<Element> xp = xpfac.compile("/FNDLOADER/" + _type, Filters.element());
      Element ele = xp.evaluateFirst(document);
      ele.setText(dir);	  
  }
  
  /*
  public void saveLDTDirectory() {
      String dir = Answer.getInstance().getB(LDT_DIRECTORY);
      XPathExpression<Element> xp = xpfac.compile("/FNDLOADER/LDT_DIRECTORY", Filters.element());
      Element ele = xp.evaluateFirst(document);
      ele.setText(dir);
  }

  public void saveOARootDir() {
      String dir = Answer.getInstance().getB(OA_ROOT_DIR);
      XPathExpression<Element> xp = xpfac.compile("/FNDLOADER/OA_ROOT_DIR", Filters.element());
      Element ele = xp.evaluateFirst(document);
      ele.setText(dir);
  }
  */
  
  public void saveInstanceInfo() {
      String instance    = Answer.getInstance().getA("SelectInstance");
      String password    = Answer.getInstance().getA("DatabaseCredential");
      String releaseName = Answer.getInstance().getB(RELEASE_NAME);

      XPathExpression<Element> xp = xpfac.compile("/FNDLOADER/INSTANCE[@SID='" + instance + "']", Filters.element());

      if (xp.evaluate(document).size() == 0) {
          logger.debug("Instance node not defined for " + instance);

          Element newEle = new Element("INSTANCE");

          newEle.setAttribute("SID", instance);

          Element ele1 = new Element(RELEASE_NAME);

          ele1.setText(releaseName);

          Element ele2 = new Element("PASSWORD");

          ele2.setText(Security.getInstance().encryptPwd(password));

          Element ele3 = new Element(NLS_LANG);

          ele3.setText(Answer.getInstance().getB(NLS_LANG));

          Element ele4 = new Element(NLS_SORT);

          ele4.setText(Answer.getInstance().getB(NLS_SORT));

          Element ele5 = new Element(NLS_NUMERIC_CHARACTERS);

          ele5.setText(Answer.getInstance().getB(NLS_NUMERIC_CHARACTERS));

          Element ele6 = new Element(NLS_DATE_FORMAT);

          ele6.setText(Answer.getInstance().getB(NLS_DATE_FORMAT));
          newEle.addContent(ele1);
          newEle.addContent(ele2);
          newEle.addContent(ele3);
          newEle.addContent(ele4);
          newEle.addContent(ele5);
          newEle.addContent(ele6);

          xp = xpfac.compile("/FNDLOADER", Filters.element());
          Element root = xp.evaluateFirst(document);
          root.addContent(newEle);
          
      } else {
    	  
          XPathExpression<Element> xp2 = xpfac.compile("/FNDLOADER/INSTANCE[@SID='" + instance + "']/PASSWORD", Filters.element());
          
          Element ele             = xp2.evaluateFirst(document);
          String  oldEncryptedPwd = ele.getValue();

          if (!Security.getInstance().decryptPwd(oldEncryptedPwd).equals(password)) {
              ele.setText(Security.getInstance().encryptPwd(password));
          }
      }
  }

  

  public String getPassword(String sid) {
      XPathExpression<Element> xp = xpfac.compile("/FNDLOADER/INSTANCE[@SID='" + sid + "']/PASSWORD", Filters.element());    	  

      Element ele = xp.evaluateFirst(document);
      String  pwd = null;

      if (ele != null) {
          pwd = ele.getValue();
      } else {
        return null;
      }

      if ((pwd != null) && (pwd.length() == ENCRYPTED_PASSWORD_LENGTH)) {
          return Security.getInstance().decryptPwd(pwd);
      } else {
          return pwd;
      }
  }
  
  public String getFileDirectory(String _type) {
	try {
	    String dir = Answer.getInstance().getB(_type);
	    if (dir != null && !dir.equals("")) {
		    logger.debug(_type + "(from Answer)=" + dir);
	        return dir;
	    }
	
	    boolean checked = false;
	
	    dir   = this.getSingleXPathValue("//" + _type, ELEMENT_NODE);
	    logger.debug("//" + _type + "=" + dir);
	    
	    if (dir == null || dir.equals("")) {
		    logger.debug(_type + " has not set yet, set to default");
		    
		    if (_type.equals(LDT_DIRECTORY)) {
		    	dir = FNDLOADER_APPLICATION_DIR + File.separator + "output" + File.separator + "LDT";	    	
		    } else if (_type.equals(OA_ROOT_DIR)) {
		    	dir = FNDLOADER_APPLICATION_DIR + File.separator + "output" + File.separator + "OA";
		    } else if (_type.equals(XDO_DIRECTORY)){
		    	dir = FNDLOADER_APPLICATION_DIR + File.separator + "output" + File.separator + "XDO";
		    } else {
		    	dir = FNDLOADER_APPLICATION_DIR + File.separator + "output";
		    }
	    }
	    
	    File test = new File(dir);
	
	    if (test.exists() && test.isDirectory()) {
	        dir     = test.getAbsolutePath();
		    logger.debug("Use directory " + dir);
	        checked = true;
	    }
	
	    if (!checked) {
		  logger.debug("Create directory " + test.getAbsolutePath());
	      FileUtils.forceMkdir(test);
	    }
	
	     Answer.getInstance().putB(_type, dir);
	     return dir;
	} catch (Exception e) {
		return System.getProperty("user.dir");
	}
  }
  
  public String getReleaseName(String sid) throws Exception {
    String name = this.getSingleXPathValue("/FNDLOADER/INSTANCE[@SID='" + sid + "']/RELEASE_NAME", ELEMENT_NODE);
    return name==null?"":name;
  }
  
  
  public Hashtable<String, String> getAllTNSEntries() throws FNDLOADERException {
	  String appMode = Answer.getInstance().getB(APP_MODE);
	  
	  if (appMode.equals(CLIENT_MODE)) {
		  return getClientModeAllTNSEntries();
	  } else if (appMode.equals(SERVER_MODE)) {
		  return getServerModeAllTNSEntries();
	  }
	  return null;
  }
  
  
  private Hashtable<String, String> getServerModeAllTNSEntries() throws FNDLOADERException {
	Hashtable<String, String> ht      = new Hashtable<String, String>();	  
    String dirName    = "";
    String tnsNameLoc = "";

    dirName = System.getenv().get("TNS_ADMIN");
    logger.debug("TNS_ADMIN=" + dirName);
    File file;    
    if (dirName != null) {
    	file = new File(dirName, TNSNAMES);
    	if (file.exists()) {
          tnsnamesFile = file.getAbsolutePath();
          tnsNameLoc   = dirName;
          logger.debug(file.getAbsolutePath() + " exists");
        } else {
          throw new FNDLOADERException(file.getAbsolutePath() + " does not exist");
        }
    	
    } else {
    	throw new FNDLOADERException("System variable TNS_ADMIN is not defined");
    }

    System.setProperty("oracle.net.tns_admin", tnsNameLoc);
    logger.debug("Reading " + tnsnamesFile + "...");

    NLParamParser parser = null;

    try {
      parser = new NLParamParser(tnsnamesFile);
    } catch (NLException nle) {
        throw new FNDLOADERException("Error in reading " + tnsnamesFile);
    } catch (IOException ioe) {
        // should not throw in here
    }

    String[] allEntries = parser.getNLPAllElements();
    String[] allNames = parser.getNLPAllNames();

    for (int i = 0; i < allNames.length; i++) {
        ht.put(allNames[i], allEntries[i]);
    }

    return ht;
  }
  
  @SuppressWarnings("rawtypes")
  private Hashtable<String, String> getClientModeAllTNSEntries() throws FNDLOADERException {
    Hashtable<String, String> ht      = new Hashtable<String, String>();
    Map                       envVars = null;
    String                    sep     = System.getProperty("file.separator");

    try {
        envVars = System.getenv();
    } catch (Exception ioe) {
        throw new FNDLOADERException("Unable to get environmnent variables.");
    }

    /*
     *  get the directory which has tnsnames.ora from
     * (1) Config file //TNS_ADMIN, if not present
     * (2) System variable TNS_NAME (in server mode), if not present
     * (3) %ORACLE_HOME%\network\admin\, if not present
     * (4) current directory
     */
    tnsnamesFile      = null;
    String dirName    = "";
    String tnsNameLoc = "";

    File file;
    
    dirName = getSingleXPathValue("//TNS_ADMIN", ELEMENT_NODE);
    logger.debug("//TNS_ADMIN=" + dirName);
    
    if ((dirName != null) &&!dirName.equals("")) {
        file = new File(dirName, TNSNAMES);
        if (file.exists()) {
            tnsnamesFile = file.getAbsolutePath();
            tnsNameLoc   = dirName;                
            logger.debug(file.getAbsolutePath() + " exists");
        } else {
            logger.debug(file.getAbsolutePath() + " does not exist");
        }
    }
    
    dirName = (String) envVars.get("TNS_ADMIN");
    logger.debug("system variable TNS_ADMIN=" + dirName);
    
    if (dirName != null) {
    	file = new File(dirName, TNSNAMES);
    	if (file.exists()) {
          tnsnamesFile = file.getAbsolutePath();
          tnsNameLoc   = dirName;
          logger.debug(file.getAbsolutePath() + " exists");
        } else {
          logger.debug(file.getAbsolutePath() + " does not exist");        	
      }
    }

	if (tnsnamesFile == null) {
	    String oracleHome = (String) envVars.get("ORACLE_HOME");
	    logger.debug("ORACLE_HOME=" + oracleHome);
	    
	    if (oracleHome != null) {
    	  dirName = oracleHome + sep + "NETWORK" + sep + "ADMIN";
          file = new File(dirName, TNSNAMES);    	
          if (file.exists()) {
              tnsnamesFile = file.getAbsolutePath();
              tnsNameLoc   = dirName;
              logger.debug(file.getAbsolutePath() + " exists");
          } else {
            logger.debug(file.getAbsolutePath() + " does not exist");
          }
	    }
    }
	
	// try lowercase path
	if (tnsnamesFile == null) {
	    String oracleHome = (String) envVars.get("ORACLE_HOME");
	    logger.debug("ORACLE_HOME=" + oracleHome);
	    if (oracleHome != null) {
    	  dirName = oracleHome + sep + "network" + sep + "admin";
          file = new File(dirName, TNSNAMES);    	
          if (file.exists()) {
              tnsnamesFile = file.getAbsolutePath();
              tnsNameLoc   = dirName;
              logger.debug(file.getAbsolutePath() + " exists");
          } else {
            logger.debug(file.getAbsolutePath() + " does not exist");
          }
	    }
    }
	
	
    if (tnsnamesFile == null) {
    	dirName = System.getProperty("user.dir");
        file = new File(dirName, TNSNAMES);    	
        if (file.exists()) {
            tnsnamesFile = file.getAbsolutePath();
            tnsNameLoc   = dirName;            
            logger.debug(file.getAbsolutePath() + " exists");
        } else {
            logger.debug(file.getAbsolutePath() + " does not exist");
        }
    }

    if (tnsnamesFile == null) {
        throw new FNDLOADERException("Unable to find " + TNSNAMES);
    }

    System.setProperty("oracle.net.tns_admin", tnsNameLoc);
    logger.debug("Reading " + tnsnamesFile + "...");

    NLParamParser parser = null;

    try {
      parser = new NLParamParser(tnsnamesFile);
    } catch (NLException nle) {
    	String errMsg = "Error in reading " + tnsnamesFile + "\n";
    	errMsg = errMsg + "Please verify the format is correct";
        throw new FNDLOADERException(errMsg);
    } catch (IOException ioe) {
        // should not throw in here
    }

    String[] allEntries = parser.getNLPAllElements();
    String[] allNames = parser.getNLPAllNames();

    for (int i = 0; i < allNames.length; i++) {
        ht.put(allNames[i], allEntries[i]);
    }

    return ht;
  }
  
  
  public String getTNSNameFile() {
	  return this.tnsnamesFile;
  }

  public boolean isWFLoad() {
	String obj = Answer.getInstance().getA("OperationObject");
	if (obj.equals("WF")) {
  	  return true;
    } else {
      return false;
    }
  }      
  
  public boolean isNLSMode() {
	String obj = Answer.getInstance().getA("OperationObject");
	if (obj.equals("NLS")) {
	  return true;
	} else {
	  return false; 
	}
  }
  
  
  private String getSingleXPathValue(String xpath, String nodeType) {
  	String rtnVal = null;
  	
  	if (nodeType.equals(ATTRIBUTE_NODE)) {
        XPathExpression<Attribute> xp = xpfac.compile(xpath, Filters.attribute());
        Attribute attr = xp.evaluateFirst(document);
        if (attr != null) rtnVal = attr.getValue();
  	} 
  	if (nodeType.equals(ELEMENT_NODE)) {
          XPathExpression<Element> xp = xpfac.compile(xpath, Filters.element());
          Element ele = xp.evaluateFirst(document);
          if (ele != null) rtnVal = ele.getValue();
    	}
  	return rtnVal;
  } 
  
  public boolean checkLCTVer(String _sid, String _module, String _name) {
	  boolean rtnVal = true;  // default
	  
	  String param = getSingleXPathValue(
			        "/FNDLOADER/INSTANCE[@SID='" + _sid + "']" + 
                    "/CHECK_LCT_VER[@MODULE='" + _module + "' and @NAME='" + _name + "']", ELEMENT_NODE);
	  if (param != null) {
		  rtnVal = Boolean.parseBoolean(param);
	  }
	  
	  return rtnVal;
  }
  
  public void setCheckLCTVer(String _sid, String _module, String _name, boolean _flag) {
	  
      String xpath = "/FNDLOADER/INSTANCE[@SID='" + _sid + "']" + 
                     "/CHECK_LCT_VER[@MODULE='" + _module + "' and @NAME='" + _name + "']";
      
      logger.debug("xpath=" + xpath);
	  XPathExpression<Element> xp = xpfac.compile(xpath, Filters.element());
	  Element ele = xp.evaluateFirst(document);
	  
	  if (ele != null) {
	      logger.debug("node exists - set value");
		  ele.setText(Boolean.toString(_flag).toUpperCase());
	  } else {
	      logger.debug("node not exists - create node");
		  xpath = "/FNDLOADER/INSTANCE[@SID='" + _sid + "']";
		  xp = xpfac.compile(xpath, Filters.element());
		  
		  Element parentNode = xp.evaluateFirst(document);
		  
		  logger.debug("parentNode exists - " + parentNode==null?"No":"Yes");
		  logger.debug(parentNode.getText());
		  
		  Element newEle = new Element(CHECK_LCT_VER);
		  newEle.setAttribute("MODULE", _module);
		  newEle.setAttribute("NAME", _name);
		  newEle.setText(Boolean.toString(_flag).toUpperCase());
		  parentNode.addContent(newEle);
	  }
  }
  
  
}
