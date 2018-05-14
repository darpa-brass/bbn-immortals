package mySoot.bbnAnalysisTools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import mySoot.AnalyzerMain;
import mySoot.util.Log;

public class ParseConfigFiles
{
	public static String RESOURCE_INDICATOR_FILE = AnalyzerMain.CONFIG_FILE_DIR + "ResourceIndicatorFile";
	public static String CPU_USAGE_FILE = AnalyzerMain.CONFIG_FILE_DIR + "analysisResult_cpu.log";
	public static String MEMORY_USAGE_FILE = AnalyzerMain.CONFIG_FILE_DIR + "analysisResult_memory.log";
	public static String THIRD_PARTY_USAGE_FILE = AnalyzerMain.CONFIG_FILE_DIR + "analysisResult_thirdPartyLibs.log";
	
	// newly generated files for DFU functions and non-DFU functions
	public static String CPU_USAGE_DFU = AnalyzerMain.OUTPUT_DIR + "analysisResult_cpu_dfu.log";
	public static String CPU_USAGE_NONDFU = AnalyzerMain.OUTPUT_DIR + "analysisResult_cpu_nondfu.log";
	public static String MEMORY_USAGE_DFU = AnalyzerMain.OUTPUT_DIR + "analysisResult_memory_dfu.log";
	public static String MEMORY_USAGE_NONDFU = AnalyzerMain.OUTPUT_DIR + "analysisResult_memory_nondfu.log";
	public static String THIRD_PARTY_USAGE_DFU = AnalyzerMain.OUTPUT_DIR + "analysisResult_thirdPartyLibs_dfu.log";
	public static String THIRD_PARTY_USAGE_NONDFU = AnalyzerMain.OUTPUT_DIR + "analysisResult_thirdPartyLibs_nondfu.log";
	
	
	public static String CPU_SEPARATOR =  " instruction count: ";
	public static String MEMORY_SEPARATOR =  " memory consumption: ";
	
	public static String RESOURCE_INDICATOR_SEPARATOR = "#";

	// ========================================================================================================================
	public static int CPU_INDEX = 0;
	public static int MEMORY_INDEX = 1;
	public static int MAX_INDEX = 1;
	
	// output data structures
	// dynamicResults: <function name, LinkedList<String> for max number for CPU and memory>
	// LinkedList<String>: [0] for CPU, [1] for memory
	public LinkedHashMap<String, LinkedList<String>> dfuToDfuFuncMapping = new LinkedHashMap<String, LinkedList<String>>();
	public LinkedHashMap<String, int[]> dynamicResults = new LinkedHashMap<String, int[]>();
	
	
	public static int FORWARD = 1;
	public static int CONTROL_FLOW = 2;
	// 
	public LinkedHashMap<String,Pair<String, Integer>> parsedResourceIndicatorInfo =
			new LinkedHashMap<String, Pair<String, Integer>>();
	// ========================================================================================================================
	
	
	// main function for this class
	public ParseConfigFiles(String file)
	{
		try
		{
			setupDfuToDfuFuncMapping();
			parseResourceIndicatorFile();
			parseCpuUsageFile();
			parseMemoryUsageFile();
		}
		catch (IOException e) 
		{
			System.out.println(e.getMessage());
		}
	}
	
	
	
	//setup the data structure of 'dfuToDfuFuncMapping'
	private void setupDfuToDfuFuncMapping() 
	{
		for(String dfu: ResourceUsageAPIs.DFUs)
		{
			LinkedList<String> funcs = new LinkedList<String>();
			dfuToDfuFuncMapping.put(dfu, funcs);
		}
	}


	// this function reads from ResourceIndicatorFile file from Configuration directory to get sources and sinks
	private void parseResourceIndicatorFile() throws IOException
	{
		System.err.println("parsing resource indicator file: " + RESOURCE_INDICATOR_FILE);
		BufferedReader br = new BufferedReader(new FileReader(RESOURCE_INDICATOR_FILE));
		String line;
	    while((line = br.readLine()) != null) 
	    {
	    	String[] strs = line.split(RESOURCE_INDICATOR_SEPARATOR);
	    	
	    	String resourceName = strs[0];
	    	String indicator = strs[1];
	    	String analysis = strs[2];
	    	
	    	if(analysis.equals("FW")) //forward data-flow analysis
	    	{
	    		Pair<String, Integer> resourceAnalysisMapping = new Pair<String, Integer>(indicator, FORWARD);
	    		parsedResourceIndicatorInfo.put(resourceName, resourceAnalysisMapping);
	    	}
	    	else if(analysis.equals("CF")) //control-flow analysis
	    	{
	    		Pair<String, Integer> resourceAnalysisMapping = new Pair<String, Integer>(indicator, CONTROL_FLOW);
	    		parsedResourceIndicatorInfo.put(resourceName, resourceAnalysisMapping);
	    	}
	    	else
	    	{
	    		System.err.println("Error parsing resource indicator file");
	    		break;
	    	}
	    }
	    br.close();
	}
	
	
	// this function reads from cpu usage file and split into two files based on DFU function list
	private void parseCpuUsageFile() throws IOException
	{
		Log.init(CPU_USAGE_DFU);
		Log.init(CPU_USAGE_NONDFU);

		System.err.println("parsing CPU usage file: " + CPU_USAGE_FILE);
		BufferedReader br = new BufferedReader(new FileReader(CPU_USAGE_FILE));
		String line;
		while((line = br.readLine()) != null) 
	    {
	    	if(line.contains(CPU_SEPARATOR))
	    	{
	    		int idx = isDFU(line);
		    	if(idx != -1)
	    			Log.dumpln(CPU_USAGE_DFU, line);
	    		else
	    			Log.dumpln(CPU_USAGE_NONDFU, line);
	    		
	    		String[] strs = line.split(CPU_SEPARATOR);
	    		String func = strs[0];
	    		int num = Integer.parseInt(strs[1]);
	    		
	    		if(dynamicResults.containsKey(func))
	    		{
	    			int currentNum = dynamicResults.get(func)[CPU_INDEX];
	    			if(currentNum < num)
	    				dynamicResults.get(func)[CPU_INDEX] = num;
	    		}
	    		else
    			{
    				int[] nums = new int[MAX_INDEX + 1];
    				nums[CPU_INDEX] = num;
    				dynamicResults.put(func, nums);
    			}
	    		
	    		if(idx != -1)
	    		{
	    			LinkedList<String> funcs = dfuToDfuFuncMapping.get(ResourceUsageAPIs.DFUs.get(idx));
	    			if(!funcs.contains(func))
	    				funcs.add(func);
	    		}
	    	}//end if
	    }//end while
	    br.close();
	}
	
	
	// this function reads from cpu usage file and split into two files based on DFU function list
	private void parseMemoryUsageFile() throws IOException
	{
		Log.init(MEMORY_USAGE_DFU);
		Log.init(MEMORY_USAGE_NONDFU);

		System.err.println("parsing memory usage file: " + MEMORY_USAGE_FILE);
		BufferedReader br = new BufferedReader(new FileReader(MEMORY_USAGE_FILE));
		String line;
		while((line = br.readLine()) != null) 
		{
		    if(line.contains(MEMORY_SEPARATOR))
		    {
		    	int idx = isDFU(line);
		    	if(idx != -1)
		    		Log.dumpln(MEMORY_USAGE_DFU, line);		
	    		else
	    			Log.dumpln(MEMORY_USAGE_NONDFU, line);
		    	
		    	String[] strs = line.split(MEMORY_SEPARATOR);
		    	String func = strs[0];
	    		int num = Integer.parseInt(strs[1]);
	    		
	    		if(dynamicResults.containsKey(func))
	    		{
	    			int currentNum = dynamicResults.get(func)[MEMORY_INDEX];
	    			if(currentNum < num)
	    				dynamicResults.get(func)[MEMORY_INDEX] = num;
	    		}
    			else
    			{
    				int[] nums = new int[MAX_INDEX + 1];
    				nums[MEMORY_INDEX] = num;
    				dynamicResults.put(func, nums);
    			}
	    		
	    		if(idx != -1)
	    		{
	    			LinkedList<String> funcs = dfuToDfuFuncMapping.get(ResourceUsageAPIs.DFUs.get(idx));
	    			if(!funcs.contains(func))
	    				funcs.add(func);
	    		}
		    }//end if
		}//end while
		br.close();
	}
	
	
	// check if given string contains DFU string
	public static int isDFU(String line)
	{
		for(int i = 0; i < ResourceUsageAPIs.DFUs.size(); i++)
		{
			if(line.contains(ResourceUsageAPIs.DFUs.get(i)))
				return i;
		}
		return -1;
	}
}