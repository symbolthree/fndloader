package symbolthree.oracle.fndload.oa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

import oracle.jrad.tools.trans.imp.XLIFFImporter;
import oracle.jrad.tools.xml.exporter.XMLExporter;
import symbolthree.flower.Helper;
import oracle.adf.mds.util.CommandLineArgs;
import oracle.jdbc.driver.OracleDriver;
import oracle.adf.mds.tools.util.CommandLineProcessor;
import oracle.adf.mds.tools.util.ConnectUtils;

public class OAXMLTest {
    ArrayList<String> args = new ArrayList<String>();

    String exportDocName = "/oracle/apps/fnd/attributesets/CPAttributeSets/FNDCPPRINTER";
    String exportRootDir = "C:\\Users\\Administrator\\symbolthree\\fndloader\\output\\OA";
    String dbConnection = "(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=vis11win2k3.symplik.com)(PORT=1521))(CONNECT_DATA=(SID=VIS11)))";		
    String mmddir = "C:\\WORK\\FNDLOADER4\\CLIENT\\11.5.0\\OA_HTML\\jrad";
    
	public OAXMLTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		OAXMLTest t = new OAXMLTest();
		//t.doExport();
		t.doImport();
	}
	
	private void doExport() {
        
        args.add(exportDocName);
        args.add("-rootdir");
        args.add(exportRootDir);
        args.add("-username");
        args.add("apps");
        args.add("-password");
        args.add("apps");
        args.add("-dbconnection");
        args.add(dbConnection);
        args.add("-jdk13");
        args.add("-validate");
        args.add("-mmddir");
		args.add(mmddir);
		args.add("-translations");
		args.add("-language");
		args.add("zh-TW");
		
        XMLExporter.main(Helper.listToArray(args));
	}

	private void doImport() {
		String file = "C:\\Users\\Administrator\\symbolthree\\fndloader\\output\\OA\\zh-TW\\oracle\\apps\\fnd\\wf\\worklist\\webui\\AdvancWorklistRG.xlf";
        args.add("-username");
        args.add("apps");
        args.add("-password");
        args.add("apps");
        args.add("-dbconnection");
        args.add(dbConnection);
        args.add(file);
        System.out.println(Helper.printArray(args));
        
        try {
        String str = "username*|password*|dbconnection*|help|platform";
        CommandLineProcessor processor = new CommandLineProcessor(Helper.listToArray(args), true, str);
        CommandLineArgs args = processor.processArgs();
        
        DriverManager.registerDriver(new OracleDriver());
        Connection localConnection = DriverManager.getConnection(ConnectUtils.getConnectString(args));
        
        boolean bool = args.getValue("checklang", "true").equalsIgnoreCase("true");
        
        XLIFFImporter localXLIFFImporter = new XLIFFImporter(args, localConnection, bool);
        localXLIFFImporter.importDocument(args.getValueByPosition(1));
        
        localXLIFFImporter.close();
        localConnection.close();        
        
        //XLIFFImporter.main(Helper.listToArray(args));
        System.out.println("DONE!!");
        
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
}
