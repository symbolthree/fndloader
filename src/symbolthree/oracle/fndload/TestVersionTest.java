package symbolthree.oracle.fndload;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class TestVersionTest {

	public TestVersionTest() {
	}
	
	public static void main(String[] args) {
		TestVersionTest t = new TestVersionTest();
		try {
			t.getVersionLocal("C:/WORK/FNDLOADER3/CLIENT/12.0.0/APPL_TOP/fnd/12.0.0/patch/115/import/afcppinf.lct");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private void getVersionLocal(String _file) throws Exception {
    	String ver = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(_file), "UTF8"));
		String str;
		while ((str = reader.readLine()) != null) {
		    if (str.indexOf("Header") > 0) break;
		}
        reader.close();
        
        // the pattern is -- $ Header: <filename> <version> <timestamp> <author> ship $ 
        str = str.substring(str.indexOf("Header"));
        ver = str.split(" ")[2];
        System.out.println(ver);
    }

}
