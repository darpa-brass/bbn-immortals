package mySoot.bbnAnalysisTools;

import java.io.IOException;
import java.util.*;

import com.securboration.immortals.ontology.measurement.CodeUnitPointer;

import mySoot.AnalyzerMain;
import mySoot.util.Log;
import soot.*;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class DFUResourceAnalysisTransformer extends SceneTransformer
{
	public CallGraph callgraph;
	
	// This data structure holds the class information along with all its methods. We use this information for DFU function identification.
	private LinkedHashMap<SootMethod, SootClass> methodToClassMapping = new LinkedHashMap<SootMethod, SootClass>();
	
	// This data structure holds the mapping between dfu and resources that are dependent on it
	private LinkedHashMap<String, LinkedList<String>> dfuToResourceMapping = new LinkedHashMap<String, LinkedList<String>>();
	//
	public LinkedHashMap<String, LinkedList<String>> methodToResourceMapping = new LinkedHashMap<String, LinkedList<String>>();
	
	
	public LinkedList<Edge> partialCallGraph = new LinkedList<Edge>();
	
	// lowest level method that calls resource usage APIs
	private ArrayList<SootMethod> lowestMethodCallees = new ArrayList<SootMethod>();
	
	// this contains all the caller methods that directly or indirectly call resource usage APIs
	private ArrayList<SootMethod> callers = new ArrayList<SootMethod>();
	
	private final String CALL_GRAPH_DOT_FILE = AnalyzerMain.CALL_GRAPH_DOT;
	private final String PARTIAL_CALL_GRAPH = AnalyzerMain.PARTIAL_CALL_GRAPH;
	private final String RUI_OUTPUT =  AnalyzerMain.RUI_OUTPUT;
	
	
	// CodeUnitPointer list for triple generation
	// mapping between class+method signature and its CodeUnitPointer
	public LinkedHashMap<String, CodeUnitPointer> cupList = new LinkedHashMap<String, CodeUnitPointer>();
	
	public ParseConfigFiles prif = null;
	
	@Override
	protected void internalTransform(String arg0, @SuppressWarnings("rawtypes") Map arg1) 
	{
		this.callgraph = Scene.v().getCallGraph();
		
		// parse several files in CONFIG directory. 
		// 1. dynamic analysis result files
		// 2. resource indicator file
		prif = new ParseConfigFiles(AnalyzerMain.CONFIG_FILE_DIR);
		
		System.err.println("Dumping call graph and partial call graph based on starting and ending points...\n");
		OutputFileInit();
		//OutputCallGraphToDot();
		//OutputPartialCallGraphToDot();
	
		System.err.println("Collecting class and method mapping info...");
		System.err.println("\ttotal class number: " + Scene.v().getClasses().size() + "\n");
		
		// go through each class to collect class and method mapping
		// In the meanwhile, find lowest level methods that call resource usage APIs
		Iterator<SootClass> itClass = Scene.v().getClasses().iterator();
		while(itClass.hasNext())
		{
			SootClass clazz = itClass.next();
			if(!clazz.isConcrete())
				continue;
			
			if(clazz.isJavaLibraryClass())
				continue;
			
			List<SootMethod> methods = clazz.getMethods();

			for(SootMethod method: methods)
			{
				if(!method.isConcrete() || (method.getSource() == null))
					continue;
				
				//generate all CodeUnitPointer for every method and class
				String sig = method.getSignature();
				try {
					if(ParseConfigFiles.isDFU(clazz.getName()) != -1) {
						Class<?> c = Class.forName(clazz.getName());
						String m = method.getName();
						CodeUnitPointer f1 = TripleGeneration.getPointer(c, m);
						cupList.put(sig, f1);
					}
				} 
				catch (IOException | ClassNotFoundException e) {
					System.err.println("\n\n" + sig);
					System.err.println("error during creating CodeUnitPointer: " + e.getMessage() + "\n\n");
				}
				
				
				// add class to method mapping information
				if(methodToClassMapping.containsKey(method))
				{
					System.err.println("Method: " + method.getSignature() + " occurred twice during DFU resource usage anlaysis!!");
				}
				else
				{
					methodToClassMapping.put(method, clazz);
				}

				
				System.out.println("\n\nMETHOD: " + method.getSignature());
				Body b = method.retrieveActiveBody();
				{
					//System.out.println("it has active body!");
					// search statement that invokes resource utilization APIs such as java.io.FileOutputStream: void write(byte[])
					// For every unit in the body, search invocations.
					Iterator<Unit> iter = b.getUnits().iterator();
					while(iter.hasNext())
					{
						Stmt s = (Stmt)iter.next();
						
						// extract the calling relationship as well as check the resource usage API call
						// check only if the statement is definition or invoke
						if(s instanceof DefinitionStmt)
						{
							Value rhs = ((DefinitionStmt) s).getRightOp();
							if(rhs instanceof InvokeExpr)
							{
								String signature = ((InvokeExpr) rhs).getMethod().getSignature();					
								findResourceDependence(prif, signature, clazz, method);
								if(ResourceUsageAPIs.isCallGraphSink(signature))
									lowestMethodCallees.add(method);
							}
						}
						else if(s instanceof InvokeStmt)
						{
							String signature = s.getInvokeExpr().getMethod().getSignature();
							findResourceDependence(prif, signature, clazz, method);
							if(ResourceUsageAPIs.isCallGraphSink(signature))
								lowestMethodCallees.add(method);
						}
					}
				}
			}
		}
		
		findCallSource();
		OutputDFUResourceUsages(prif);
		TripleGeneration.generateTriples();
	}
	
	
	private void findResourceDependence(ParseConfigFiles prif, String signature, SootClass clazz, SootMethod method)
	{
		// go through all the resource indicator and analysis mapping 
		// to find if any DFU is dependent on hardware sources
		for(String resource: prif.parsedResourceIndicatorInfo.keySet())
		{
			Pair<String, Integer> indicatorAnalysisPair = prif.parsedResourceIndicatorInfo.get(resource);
			
			// if the sig contains indicator, that means it's dependent on the 'resource'
			if(signature.contains(indicatorAnalysisPair.getFirst()))
			{
				// if current class is a DFU, then we go ahead adding this information and resource it depends on into 'dfuToResourceMapping'
				int idx = ParseConfigFiles.isDFU(clazz.getName());
				if(idx != -1)
				{
					String dfu = ResourceUsageAPIs.DFUs.get(idx);
					if(dfuToResourceMapping.containsKey(dfu))
					{
						LinkedList<String> resources = dfuToResourceMapping.get(dfu);
						if(!resources.contains(resource))
							resources.add(resource);
					}
					else
					{
						LinkedList<String> resources = new LinkedList<String>();
						resources.add(resource);
						dfuToResourceMapping.put(dfu, resources);
					}

				}
				
				
				// add method resource dependency. Now our granularity is at method level other than DFU level
				String sig = method.getSignature();
				if(methodToResourceMapping.containsKey(sig))
				{
					LinkedList<String> resources = dfuToResourceMapping.get(sig);
					if(!resources.contains(resource))
						resources.add(resource);
				}
				else
				{
					LinkedList<String> resources = new LinkedList<String>();
					resources.add(resource);
					methodToResourceMapping.put(sig, resources);
				}
				System.err.println("Method: " + method + " within " + clazz + " is dependent on: " + resource);

				
				return;
			}//end if
		}//end for
	}
	
	

	private void findCallSource()
	{
		// recursively find the call source
		LinkedList<SootMethod> callees = new LinkedList<SootMethod>();
		callees.addAll(lowestMethodCallees);
		callers.addAll(lowestMethodCallees);
				
		while(!callees.isEmpty())
		{
			SootMethod method = callees.removeFirst();
			Iterator<Edge> itEdge = this.callgraph.edgesInto(method);
			while(itEdge.hasNext())
			{
				Edge edge = itEdge.next();
				SootMethod src = (SootMethod) edge.getSrc();

				// we keep track of caller unless 
				// 1. source and target are the same
				// 2. caller has been explored
				// 3. caller is within android packages list android.support.v4/v7 and com.google.android
				if(!src.equals(method) 
						&& !callers.contains(src) 
						&& !src.getSignature().startsWith("<android.support")
						&& !src.getSignature().startsWith("<com.google.android"))
				{
					callees.add(src);
					callers.add(src);
					System.out.println("method: " + src.getSignature() + " calls method: " + method.getSignature());
				}
			}
		}//end while
	}
	
	
	
	private void OutputDFUResourceUsages(ParseConfigFiles prif) 
	{
		for(int i = 0; i < ResourceUsageAPIs.DFUs.size(); i++)
		{
			String dfu = ResourceUsageAPIs.DFUs.get(i);
			LinkedList<String> funcs = prif.dfuToDfuFuncMapping.get(dfu);
			
			Log.dumpln(RUI_OUTPUT,"DFU: " + dfu);
			
			// output per DFU usages
			if(dfuToResourceMapping.containsKey(dfu))
			{
				LinkedList<String> resources = dfuToResourceMapping.get(dfu);
				for(String res: resources)
				{
					Log.dumpln(RUI_OUTPUT, "\t dependent on hardware resource: " + res);
				}
			}
			
			// output per function usages
			if(!funcs.isEmpty())
			{
				for(String func: funcs)
				{
					Log.dump(RUI_OUTPUT, "\tfunction: " + func);
					int[] nums = prif.dynamicResults.get(func);
					Log.dump(RUI_OUTPUT, "\t" + ParseConfigFiles.CPU_SEPARATOR + nums[ParseConfigFiles.CPU_INDEX]);
					Log.dump(RUI_OUTPUT, "\t" + ParseConfigFiles.MEMORY_SEPARATOR + nums[ParseConfigFiles.MEMORY_INDEX]);
					Log.dumpln(RUI_OUTPUT);
				}
			}
			
			Log.dumpln(RUI_OUTPUT);
		}
	}
	
	

	private void OutputFileInit()
	{
		Log.init(CALL_GRAPH_DOT_FILE);
		Log.init(PARTIAL_CALL_GRAPH);
		Log.init(RUI_OUTPUT);
	}
	
	// output the partial call graphs to a dot file based on start/end info
//	private void OutputPartialCallGraphToDot()
//	{
//		//int num = 0;
//		LinkedList<Edge> previous = new LinkedList<Edge>();
//		LinkedHashMap<SootMethod, Boolean> visited = new LinkedHashMap<SootMethod, Boolean>();
//		
//		LinkedList<String> startingPoints = ResourceUsageAPIs.partialCallGraphPoints.getFirst();
//		LinkedList<String> endingPoints = ResourceUsageAPIs.partialCallGraphPoints.getSecond();
//		
//		for(String start : startingPoints)
//		{
//			SootMethod sp = null, ep = null;
//			
//			for(String end: endingPoints) {
//				// find starting SootMethod
//				Iterator<Edge> itEdge = this.callgraph.iterator();
//				while(itEdge.hasNext())
//				{
//					Edge curEdge = itEdge.next();
//					SootMethod src = (SootMethod) curEdge.getSrc();
//					SootMethod tgt = (SootMethod) curEdge.getTgt();
//					
//					if((sp == null) && src.getSignature().contains(start))
//					{
//						sp = src;
//					}
//					
//					if((ep == null) && tgt.getSignature().contains(end))
//					{
//						ep = tgt;
//					}
//					
//					if(sp != null && ep != null)
//						break;
//				}
//				
//				if(sp != null && ep != null) {
//					allPaths(visited, previous, sp, ep);
//					System.err.println("Find both " + start + " and  " + end + " as starting and ending points in call graph!");
//				}			
//			}
//		}
//	}
	

	// explore all paths between sp and ep
	private void allPaths(LinkedHashMap<SootMethod, Boolean> visited, LinkedList<Edge> previous, SootMethod sp, SootMethod ep) 
	{
		visited.put(sp, true);
		
		if(sp.equals(ep))
		{
			//printPaths(previous);
			storePaths(previous);
			return;
		}
		
		Iterator<Edge> it = this.callgraph.edgesOutOf(sp);
		while(it.hasNext())
		{
			Edge cur = it.next();
			SootMethod tgt = (SootMethod) cur.getTgt();
			if(visited.containsKey(tgt))
				continue;
			
			if(!previous.contains(tgt) 
					&& !tgt.getSignature().startsWith("<android.support")
					&& !tgt.getSignature().startsWith("<com.google.android"))
			{
				previous.add(cur);
				allPaths(visited, previous, tgt, ep);
			}
		}
	}


	private void storePaths(LinkedList<Edge> previous) {
		while(!previous.isEmpty())
		{
			Edge tmp = previous.removeFirst();
			
			if(!partialCallGraph.contains(tmp))
				partialCallGraph.add(tmp);
		}
	}


	// output the partial call graph we got from source and sink
	private void printPaths(LinkedList<Edge> previous)
	{
		Log.dumpln(PARTIAL_CALL_GRAPH, "digraph G {");
		while(!previous.isEmpty())
		{
			Edge tmp = previous.removeFirst();

			SootMethod src = (SootMethod) tmp.getSrc();
			SootMethod tgt = (SootMethod) tmp.getTgt();
			Log.dumpln(PARTIAL_CALL_GRAPH, src.getSignature() + " -> " + tgt.getSignature());
		}
		Log.dumpln(PARTIAL_CALL_GRAPH, "}\n");
	}


	// output the whole call graph to a dot file
//	private void OutputCallGraphToDot() 
//	{
//		//HashMap<SootMethod, Boolean> nodes = new HashMap<SootMethod, Boolean>();
//		//ArrayList<Edge> edges = new ArrayList<Edge>();
//		Log.dumpln(CALL_GRAPH_DOT_FILE, "digraph G {");
//		
//		Iterator<Edge> itEdge = this.callgraph.iterator();
//		while(itEdge.hasNext())
//		{
//			Edge edge = itEdge.next();
//			SootMethod src = (SootMethod) edge.getSrc();
//			SootMethod tgt = (SootMethod) edge.getTgt();
////			
////			if(!nodes.containsKey(src))
////				nodes.put(src, true);
////			if(!nodes.containsKey(tgt))
////				nodes.put(tgt, true);
////			
////			edges.add(edge);
//			
//			Log.dumpln(CALL_GRAPH_DOT_FILE, "    " + "\"" + src.getSignature() + "\" -> " + "\"" + tgt.getSignature() + "\";");
//		}
//		Log.dumpln(CALL_GRAPH_DOT_FILE, "}\n");	
//	}
	
}