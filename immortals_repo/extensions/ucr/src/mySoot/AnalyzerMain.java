package mySoot;

import mySoot.util.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import mySoot.bbnAnalysisTools.DFUResourceAnalysisTransformer;
//import mySoot.bbnAnalysisTools.ParseResourceIndicatorFile;
import mySoot.bbnAnalysisTools.ResourceUsageAPIs;
import mySoot.bbnAnalysisTools.SQLQueryIdentification;
import soot.*;
import soot.jimple.Stmt;

public class AnalyzerMain {

	public static String CLASSPATH;
	public static String DEX_FULLPATH;
	//public static String ANDROID;	
	public static String OUTPUT;
	public static String LIB_DIR;
	public static String API_PERMISSION_MAP;
	public static String API_PERMISSION_MAP_PSCOUT;
	
	public static String LOG;
	public static String API_LOCAL_LOG;
	public static String API_LOCAL_DOT;
	public static String API_GLOBAL_LOG;
	public static String API_GLOBAL_DOT;
	public static String DDG_GLOBAL_DOT;
	public static String DDG_GLOBAL_SUCCINCT_DOT;

	

	public static String OUTPUT_DIR;
	
	public static String CONFIG_FILE_DIR;
	public static String VULNERABILITY_TEST_RESULT;
	
	//added for BBN project
	public static boolean dataFlowForSQLAnalysis;
	public static String CALL_GRAPH_DOT;
	public static String PARTIAL_CALL_GRAPH;
	public static String RUI_OUTPUT;
	
	public static String THIRD_PARTY_LIBS;
	public static String RT;


	
	public static String ANDROID_23;
	public static String ANDROID_24;
	

	
	public static String INFO_SOURCE;
	public static String INFO_SINK;
	
	/** added for BBN */
	public static String SINK_METHOD;
	public static String SOURCE_CLASS;
	
	public static String APPNAME;
	public static int EFFECTIVE_GRAPH_SIZE_LIMIT = 3;
	
	public static FindUncalledMethodsTransformer findUncalledMethodsTransformer;
	// public static FindUncalledOverridingMethodsTransformer findUncalledOverridingMethodsTransformer;
	// public static FindEntryPointsTransformer findEntryPointsTransformer;
	public static APISubGraphTransformer apiSubGraphTransformer;
	public static GlobalAPISubGraphTransformer globalAPISubGraphTransformer;
	public static GlobalBackwardDataflowAnalysis globalBackwardDataflowAnalysis;
	public static GlobalForwardDataflowAnalysis globalForwardDataflowAnalysis;
//	private FlowSourceTransformer flowSourceTransformer;
	public static FlowSinkTransformer flowSinkTransformer;
	public static RecursionDetectionTransformer recursionDetectionTransformer;
	public static PointsToAnalysisTransformer pointsToAnalysisTransformer;
	public static ReadEpiccResult rer;
	
	// ADDED for BBN project
	//private ClassHierarchyAnalysisTransformer classHierarchyAnalysisTransformer;
	public static DFUResourceAnalysisTransformer dfuResourceAnalysisTransformer;
	
	public static LinkedHashMap<String, String> entryPoints = new LinkedHashMap<String, String>();
	public static List<SootMethod> appUncalledMethods;
	public static List<SootMethod> appMethods;
	public static List<SootMethod> appCalledMethods;
	
	public static LinkedHashMap<String, LinkedHashMap<String, String>> sourcesLocationMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
	public static LinkedHashMap<String, Integer> sources;
	public static LinkedHashMap<String, Integer> sinks;
	
	//these two data structures are used to contain the merged graph info for both forward and backward analysis
	//nodeStruct: first string is the node signature, second LinkedList<String> is the attributes.
	//edgeStruct: first string is the source node signature, second string is the destination node signature.
	//reversedEdgeStruct: first string is the destination node signature, second string is the source node signature.
	public static LinkedHashMap<String, LinkedList<String>> nodeStruct = new LinkedHashMap<String, LinkedList<String>>();
	public static LinkedHashMap<String, LinkedList<String>> edgeStruct = new LinkedHashMap<String, LinkedList<String>>();
	public static LinkedHashMap<String, LinkedList<String>> reversedEdgeStruct = new LinkedHashMap<String, LinkedList<String>>();
	public static int API_LEVEL = 0;
	
	public static List<String> unloadedClasses = new ArrayList<String>();
	
	public static void main(String[] args){
		
		String arg1 = args[0];
		DEX_FULLPATH = args[1];
		
		
		if(args.length > 1)
			API_LEVEL =  Integer.parseInt(args[2]);
		
		System.err.println("API level in soot: " + API_LEVEL);
		
		CLASSPATH = arg1 + "/";
		String filename = arg1.substring(arg1.lastIndexOf('/')+1);
		OUTPUT = "output" + "_" + filename;
		
		System.err.println("class path: " + CLASSPATH);
		System.err.println("File name: " + filename);
		File directory = new File(".");
		String pwd = "";
		try{
			pwd = directory.getAbsolutePath();
		}catch(Exception e){
			System.out.println(e.getMessage());
			System.err.println("java -Xss4m -Xmx1024m -cp $JASMIN:$SOOTCLASS:$POLYGLOT:$ANDROID:. mySoot.AnalyzerMain $CURR_DIR/apks/$FILENAME > $OUTPUT");
			System.exit(-1);
		}
		
		APPNAME = filename;
		OUTPUT_DIR = pwd + "/../output/";
		LIB_DIR = pwd + "/../sdk/platforms/default/";
		LOG = OUTPUT_DIR + filename + ".log";
//		LOG_DOT = OUTPUT_DIR + filename + ".log.dot";
		API_LOCAL_LOG = OUTPUT_DIR + filename + ".api.local.log";
		API_LOCAL_DOT = OUTPUT_DIR + filename + ".api.dot";
		API_GLOBAL_LOG = OUTPUT_DIR + filename + ".api.global.log";
		API_GLOBAL_DOT = OUTPUT_DIR + filename + ".api.global.dot";
//		DDG_LOCAL_DOT = OUTPUT_DIR + filename + ".ddg.local.dot";
		DDG_GLOBAL_DOT = OUTPUT_DIR + filename + ".ddg.global.dot";
		DDG_GLOBAL_SUCCINCT_DOT = OUTPUT_DIR + filename + ".ddg.global.succinct.dot";
//		DDG_GLOBAL_CXL = OUTPUT_DIR + filename + ".ddg.global.cxl";
//		VPT_CXL_1 = OUTPUT_DIR + ".vpt.1.cxl";
//		VPT_CXL_2 = OUTPUT_DIR + ".vpt.2.cxl";
		
		// added for BBN project
		CALL_GRAPH_DOT = OUTPUT_DIR + filename + ".call.graph.dot";
		PARTIAL_CALL_GRAPH = OUTPUT_DIR + filename + ".path";
		RUI_OUTPUT = OUTPUT_DIR + filename + ".resourceUsageOutput";
		
		CONFIG_FILE_DIR = pwd + "/../config/";
		
		Log.init(LOG);
		Log.init(API_LOCAL_LOG);
		Log.init(API_GLOBAL_LOG);
		Log.init(API_GLOBAL_DOT);
		Log.init(DDG_GLOBAL_DOT);
		Log.init(DDG_GLOBAL_SUCCINCT_DOT);
		
		
		SensitiveAPICategory.init();
		
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss aaa z");
		Log.dumpln(LOG, "Analysis starts at " + (sdf.format(cal.getTime())));
				
//		ANNOTATIONS = LIB_DIR + MyConstants.ANNOTATION;
//		APPCOMPAT_V7 = LIB_DIR + MyConstants.APPCOMPAT_V7;
//		DOM4J = LIB_DIR + MyConstants.DOM4J;
//		GSON = LIB_DIR + MyConstants.GSON;
//		IMMORTALS_CORE = LIB_DIR + MyConstants.IMMORTALS_CORE;
//		PLAY_SERVICES_BASE = LIB_DIR + MyConstants.PLAY_SERVICES_BASE;
//		PLAY_SERVICES_BASEMENT = LIB_DIR + MyConstants.PLAY_SERVICES_BASEMENT;
//		PLAY_SERVICES_MAPS = LIB_DIR + MyConstants.PLAY_SERVICES_MAPS;
//		RT = LIB_DIR + MyConstants.JAVA;
//		SUPPORT_ANNOTATIONS = LIB_DIR + MyConstants.SUPPORT_ANNOTATIONS;
//		SUPPORT_V4 = LIB_DIR + MyConstants.SUPPORT_V4;
//		MAIL = LIB_DIR + MyConstants.MAIL;
//		JDOM = LIB_DIR + MyConstants.JDOM;
//		XOM = LIB_DIR + MyConstants.XOM;
//		ASM_ALL = LIB_DIR + MyConstants.ASM_ALL;
//		GUAVA = LIB_DIR + MyConstants.GUAVA;
//		XERCESIMPL = LIB_DIR + MyConstants.XERCESIMPL;
		RT = LIB_DIR + MyConstants.JAVA;
		THIRD_PARTY_LIBS = LIB_DIR + MyConstants.THIRD_PARTY_LIBS;		
		
		ANDROID_24 = LIB_DIR + MyConstants.ANDROID_24;
		ANDROID_23 = LIB_DIR + MyConstants.ANDROID_23;
		
		
		System.out.println("processing package " + filename + " under " + CLASSPATH + ", output directory:" + OUTPUT);
		
		AnalyzerMain analyzer = new AnalyzerMain();
		analyzer.run();
	}

	
	private void run()
	{
		AndroidSourceSinkSummary.buildSourceAndSinkSummary();
		AndroidFunctionSummary.buildFunctionSummary();
	 
		// get epicc analysis result from the result file so as to enable ICC data-flow
		//GetEpiccResult();
		
		if(MyConstants.ENTRY_POINT_DISCOVERY) {
			System.err.println("\n\nEntry point discovery in progress...");
			entryPointDiscovery();
		}
		
		
//		if(MyConstants.GLOBAL_API_GRPAH)
//		{
//			System.err.println("Start building Global API graph...");
//			buildGlobalAPISubGraph();
//			G.reset();
//		}
		
		if(MyConstants.SQL_ANALYSIS) {
			System.err.println("\nStart analysing SQL queries...");
			TransformerInvokers.sqlResourceIdentification();
			dataFlowForSQLAnalysis = true;
			sqlQueryAnalysis();
			dataFlowForSQLAnalysis = false;
			G.reset();
		}
		
		
		if(MyConstants.DFU_RESOURCE_ANALYSIS)
		{
			System.err.println("\nAnalyzing DFU resource usages...");
			//this setup() function has to be the first one to get called
			ResourceUsageAPIs.setup();
			// perform resource analysis including analysis xx
			TransformerInvokers.dfuResourceAnalysis();
			G.reset();
		}
		
		//G.reset();
		//doAnnotationInsertion();
			
		
		System.err.println("Analysis completed");
	}
	
	private void entryPointDiscovery() {
		TransformerInvokers.findUncalledMethods();
		
		appMethods = findUncalledMethodsTransformer.getAppMethods();
		appCalledMethods = findUncalledMethodsTransformer.getCalledMethods();
		appUncalledMethods = new ArrayList<SootMethod>();
		for(SootMethod appMethod : appMethods){
			if(!appCalledMethods.contains(appMethod)){
				if(!appUncalledMethods.contains(appMethod)){
					appUncalledMethods.add(appMethod);
					String key = appMethod.getDeclaringClass().getName() + "|" + appMethod.getSignature();
					String value = appMethod.getSubSignature();
					entryPoints.put(key, value);
				}
			}
		}
		System.out.println(appUncalledMethods.size() + " ENTRY POINT Methods:");
		for(SootMethod appUncalledMethod : appUncalledMethods){
			System.out.println(appUncalledMethod.getSignature());
		}
		
		
		G.reset();
		
//		TransformerInvokers.findUncalledOverridingMethods();
//		uncalledOverridingFrameworkMethods = findUncalledOverridingMethodsTransformer.getUncalledOverridingFrameworkMethods();
//		
//		System.out.println(uncalledOverridingFrameworkMethods.size() + " Uncalled Overriding Framework Methods:");
//		for(String override : uncalledOverridingFrameworkMethods){
//			System.out.println(override);
//		}
//		G.reset();
//		
//		
//		TransformerInvokers.simpleOptimization();
//		G.reset();
//		
//		entryPoints = findUncalledOverridingMethodsTransformer.getClassToMethod();
	}

	/**
	 * This function performs annotation insertion based on data-flow analysis
	 */
	@SuppressWarnings("unused")
	private void doAnnotationInsertion() 
	{
		AnnotationInsertion ai = new AnnotationInsertion();
		ArrayList<SootMethod> annotatedMethod = new ArrayList<SootMethod>();
		
		// for each method in the class, we insert annotation
		for (APIGraphNode node: globalForwardDataflowAnalysis.apiDDGGraph)
		{
			SootMethod method = node.getHostMethod();
			if(annotatedMethod.contains(method))
				continue;
			else
				annotatedMethod.add(method);
	
			
			String classStr = method.getDeclaringClass().getName();
			String fileName = CLASSPATH + classStr.replace('.', '/') + ".class";
			
			//System.out.println("Insert annotation into: " + method.getName() + " in " + fileName + "\n");
			//System.out.println("output dir: " + OUTPUT_DIR);
			ai.setInfo(fileName, "GPS Required", method.getName(), OUTPUT_DIR);
			ai.performInsertion();
		}
	}

	@SuppressWarnings("unused")
	private void GetEpiccResult() 
	{
		System.err.println("Getting epicc result from " + OUTPUT_DIR + "epiccResult");
		rer = new ReadEpiccResult(OUTPUT_DIR + "epiccResult");
		rer.DumpIntentValues();
	}
	

	
	/**
	 * This funciton performs backward dataflow for each SQL query statement
	 */
	private void sqlQueryAnalysis() {
		
		for(Stmt sqlQuerySig : SQLQueryIdentification.sqlQueryStmtToMethodMapping.keySet()) {
			SINK_METHOD = SQLQueryIdentification.sqlQueryStmtToMethodMapping.get(sqlQuerySig).toString();
			INFO_SINK = sqlQuerySig.toString();
			
			G.reset();
			TransformerInvokers.locateSinks();
			G.reset();
			
			if(flowSinkTransformer.sinkSig != "") {
				if(MyConstants.DEBUG_SQL)
					System.err.println("Performing dataflow on sinkSig: " + INFO_SINK + "|" + flowSinkTransformer.sinkSig + "\n\n");
				
				sourcesLocationMap.put(INFO_SINK, flowSinkTransformer.getClassToMethod());
				TransformerInvokers.doGlobalBackwardDataflowAnalysis();
				sourcesLocationMap.clear();
			}
		}
	}
	

	
}
