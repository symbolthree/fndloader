package symbolthree.oracle.fndload.common;

import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

import symbolthree.oracle.fndload.Config;
import symbolthree.oracle.fndload.Constants;

public class TestRegistry2 {

	String releaseName = "12.0.0";
	String rootKey = null;
	WinReg.HKEY HKEA = WinReg.HKEY_LOCAL_MACHINE;
	
	public TestRegistry2() {
	}

	public static void main(String[] args) {
		TestRegistry2 t = new TestRegistry2();
		System.out.println("java runtime arch: " +  System.getProperty("os.arch"));
    	String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
    	String arch = System.getenv("PROCESSOR_ARCHITECTURE");    	
    	String realArch = arch.endsWith("64")
    	                  || wow64Arch != null && wow64Arch.endsWith("64")
    	                      ? "64" : "32";     
    	System.out.println("os arch " + realArch);
		t.readKey();
		//t.createKey();
	}

	private void readKey() {
		if (Config.winArch().equals("32"))  rootKey="SOFTWARE\\ORACLE\\APPLICATIONS\\" + releaseName;
		if (Config.winArch().equals("64"))  rootKey="SOFTWARE\\Wow6432Node\\ORACLE\\APPLICATIONS\\" + releaseName;
		
    	boolean boo = false;
    	
    	boo = Advapi32Util.registryKeyExists(HKEA, rootKey);
    	
    	if (! boo) {
    		System.out.println("Registry Key not exist - " + rootKey);
    		return;
    	} 
    	
    	boo = Advapi32Util.registryValueExists(HKEA, rootKey, "APPL_CONFIG");

    	if (! boo) {
    		System.out.println("Registry Key Value not exist - " + rootKey + " , " + "APPL_CONFIG");
    		return;    		
    	}
    	
    	String str = Advapi32Util.registryGetStringValue(HKEA, rootKey, "APPL_CONFIG");
    	if (str != null && str.equals(Constants.APPL_CONFIG_WIN_KEY)) {
    		System.out.println("Registry Key Value exists and correct");
    		return;    		
    		
    	} else {
    		System.out.println("Registry Key Value exists but  incorrect");
    		return;    		
    	}
	}
	
	private void createKey() {

		if (Config.winArch().equals("32"))  rootKey="SOFTWARE\\ORACLE\\APPLICATIONS\\" + releaseName;
		if (Config.winArch().equals("64"))  rootKey="SOFTWARE\\Wow6432Node\\ORACLE\\APPLICATIONS\\" + releaseName;
		
		rootKey="SOFTWARE\\ORACLE\\APPLICATIONS\\" + releaseName;
		
	    System.out.println(rootKey);		
	    
	    boolean boo = Advapi32Util.registryKeyExists(HKEA, rootKey);
	    
	    System.out.println(boo);
	    
		Advapi32Util.registryCreateKey(HKEA, rootKey + "\\SYMBOLTHREE");
		Advapi32Util.registrySetStringValue(HKEA, rootKey, "APPL_CONFIG", "SYMBOLTHREE");
		
		Advapi32Util.registrySetStringValue(HKEA, rootKey + "\\SYMBOLTHREE", "NLS_LANG" ,"AMERICAN_AMERICA.UTF8");
		Advapi32Util.registrySetStringValue(HKEA, rootKey + "\\SYMBOLTHREE", "NLS_SORT" ,"BINARY");
		Advapi32Util.registrySetStringValue(HKEA, rootKey + "\\SYMBOLTHREE", "NLS_NUMERIC_CHARACTERS" ,".,");
		Advapi32Util.registrySetStringValue(HKEA, rootKey + "\\SYMBOLTHREE", "NLS_DATE_FORMAT" ,"DD-MON-RR");
		
	}
	
}
