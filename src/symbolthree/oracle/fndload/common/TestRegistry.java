package symbolthree.oracle.fndload.common;

import symbolthree.oracle.fndload.Config;

public class TestRegistry {

	String releaseName = "12.0.0";
	int jvmBit = WinRegistry.KEY_WOW64_32KEY;
	
	public TestRegistry() {
    	//String bitVal = System.getProperty("sun.arch.data.model");		
    	if (Config.winArch().equals("32")) jvmBit = WinRegistry.KEY_WOW64_32KEY;
    	if (Config.winArch().equals("64")) jvmBit = WinRegistry.KEY_WOW64_64KEY;		
	}

	public static void main(String[] args) {
		TestRegistry t = new TestRegistry();
		t.run();
		//t.setKeys();
	}
	
	private void run() {
    	
    	String rootKey = "SOFTWARE\\ORACLE\\APPLICATIONS\\" + releaseName;
    	try {
	    	String regValue = WinRegistry.readString(
	    			WinRegistry.HKEY_LOCAL_MACHINE, rootKey, "APPL_CONFIG", jvmBit);
	
	        if (regValue == null || regValue.equals("")) {
	        	WinRegistry.createKey(WinRegistry.HKEY_LOCAL_MACHINE, rootKey);
	        	WinRegistry.writeStringValue(
	        			WinRegistry.HKEY_LOCAL_MACHINE, rootKey, "APPL_CONFIG", "SYMPLIK", jvmBit);
	        	System.out.println("Registry keys created");
	        } else {
	            System.out.println("Registry keys exist");  
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
	}
	
  private void setKeys() {
  	String keyRoot = "SOFTWARE\\ORACLE\\APPLICATIONS\\" + releaseName + "\\SYMPLIK";
  	try {
  		WinRegistry.createKey(WinRegistry.HKEY_LOCAL_MACHINE, keyRoot);
  		
	  	WinRegistry.writeStringValue(
	  			WinRegistry.HKEY_LOCAL_MACHINE, keyRoot, "NLS_LANG", "XXXX", jvmBit);
	
	  	WinRegistry.writeStringValue(
	  			WinRegistry.HKEY_LOCAL_MACHINE,	keyRoot, "NLS_SORT", "YYYYY", jvmBit);
	
	  	WinRegistry.writeStringValue(
	  			WinRegistry.HKEY_LOCAL_MACHINE,	keyRoot, "NLS_NUMERIC_CHARACTERS", "ZZZZ", jvmBit);
	
	  	WinRegistry.writeStringValue(
	  			WinRegistry.HKEY_LOCAL_MACHINE,	keyRoot, "NLS_DATE_FORMAT", "QQQQ", jvmBit);
   	  
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }
}
