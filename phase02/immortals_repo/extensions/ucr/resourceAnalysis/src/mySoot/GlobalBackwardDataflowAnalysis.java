package mySoot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import mySoot.util.*;

import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;
import soot.jimple.*;
import soot.*;
import soot.tagkit.*;

public class GlobalBackwardDataflowAnalysis extends SceneTransformer {

	private CallGraph cg;
	private CallGraph callgraph;
	public static CallGraph call_graph;
	private static PointsToAnalysis pta;
	
	private LinkedHashMap<SootMethod, APIGraph> apiGraphs;
	private LinkedHashMap<SootMethod, APIGraph> globalApiGraphs;
	private LinkedHashMap<SootMethod, APIGraph> asyncGlobalApiGraphs;
	private LinkedHashMap<SootMethod, APIGraph> implicitAsyncGlobalApiGraphs;
	private LinkedHashMap<SootMethod, APIGraph> interActivityGraphs;
	
//	private String LOG;
//	private String API_LOCAL_LOG;
//	private String API_GLOBAL_LOG;
//	private String DDG_LOCAL_DOT;
//	private String DDG_GLOBAL_DOT;
//	private String DDG_GLOBAL_SUCCINCT_DOT;
//	private String MAL_FEATURE_LOG;
//	private String BENIGN_FEATURE_LOG;
	private String ACTIVITY_LOG;

	static LinkedHashMap<String, TaintTag> taintTagMap = new LinkedHashMap<String, TaintTag>();
	static LinkedHashMap<String, TaintTag> extraDefTagMap = new LinkedHashMap<String, TaintTag>();
	static LinkedHashMap<TaintTag, String> taintTagReverseMap = new LinkedHashMap<TaintTag, String>();
	static LinkedHashMap<TaintTag, String> extraDefTagReverseMap = new LinkedHashMap<TaintTag, String>();

	static TaintTag generalTaintTag = new TaintTag(1, "generalTaintTag");
	static TaintTag instrumentationTag = new TaintTag(2);
	static TaintTag generalExtraDefTag = new TaintTag(3, "generalExtraDefTag");
	static TaintTag wrapperBeginTag = new TaintTag(4);
	static TaintTag wrapperEndTag = new TaintTag(5);
	static TaintTag invokeWrapperTag = new TaintTag(6);
	static TaintTag beforeWrapperTag = new TaintTag(7);
	static TaintTag afterWrapperTag = new TaintTag(8);
	static TaintTag initTaintTag = new TaintTag(9);
	static TaintTag checkTaintTag = new TaintTag(10);
	static TaintTag referenceTag = new TaintTag(11, "referenceTag");
	static TaintTag initialLocalTag = new TaintTag(12, "initialLocalTag");

	static TaintTag isWrapperTag = new TaintTag(126);
	static TaintTag wrapperMethodTag = new TaintTag(127);
	static TaintTag referenceRelatedTag = new TaintTag(128);
	static TaintTag taintStaticTag = new TaintTag(129);
	static TaintTag equivTag = new TaintTag(130);

	static TaintTag debuggingTag = new TaintTag(1023, "debuggingTag");
	
	static TaintTag functionPreservingTag = new TaintTag(1024, "functionPreservingTag");
	
	public static TaintTag API_TAG = new TaintTag(0xffff, "API_TAG");
	public static TaintTag STRING_CONST_TAG = new TaintTag(0xfffe, "STRING_CONST_TAG");

	private static LinkedHashMap<SootField, Vector<Integer>> usedStaticFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();
	private static LinkedHashMap<SootField, Vector<Integer>> usedInstanceFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();
	//private static int sourceCount = 0;
	private  int sinkCount = 0;
	private  int nodeCount = 0;
	private  int apiNodeCount = 0;

	private static LinkedHashMap<SootMethod, Vector<Integer>> sMethodsWithSources = new LinkedHashMap<SootMethod, Vector<Integer>>();

	private static List<SootField> taintedFieldsInCallee = new ArrayList<SootField>();
	private static List<SootField> taintedFieldsInCaller = new ArrayList<SootField>();
	private static List<String> sinks = new ArrayList<String>();
	private static List<String> srcs = new ArrayList<String>();
	private static LinkedHashMap<SootMethod, LinkedHashMap<Stmt, List<Stmt>>> methodToEquiv 
	= new LinkedHashMap<SootMethod, LinkedHashMap<Stmt, List<Stmt>>>();

	private static Stack<SootMethod> callString = new Stack<SootMethod>();

	private static LinkedHashMap<String, List<Integer>> propagationHistory 
	= new LinkedHashMap<String, List<Integer>>();

	public static LinkedHashMap<String, LinkedHashMap<String, List<String>>> methodToSourceToVar
	= new LinkedHashMap<String, LinkedHashMap<String, List<String>>>();

	public static LinkedHashMap<String, LinkedHashMap<String, List<String>>> classToSourceToField
	= new LinkedHashMap<String, LinkedHashMap<String, List<String>>>();

	public static LinkedHashMap<String, List<String>> methodToTaintVar = new LinkedHashMap<String, List<String>>();
	public static LinkedHashMap<String, List<String>> classToTaintField = new LinkedHashMap<String, List<String>>();
	
	//private List<Edge> edgeList = new ArrayList<Edge>();
	
	private LinkedHashMap<SootMethod, List<APIGraphNode>> methodToDDGMap = new LinkedHashMap<SootMethod, List<APIGraphNode>>();
	
	public List<APIGraphNode> apiDDGGraph = new ArrayList<APIGraphNode>();
	private LinkedHashMap<Stmt, APIGraphNode> stmtToNodeMap = new LinkedHashMap<Stmt, APIGraphNode>();
	
	private LinkedHashMap<SootField, List<Stmt>> fieldToUsesMap = new LinkedHashMap<SootField, List<Stmt>>();
	private LinkedHashMap<SootField, List<Stmt>> fieldToDefsMap = new LinkedHashMap<SootField, List<Stmt>>();
	
	
	//private static List<Stmt> recursiveCallsites = new ArrayList<Stmt>();
	
//	private static AndroidPermissionMap apm = new AndroidPermissionMap();
			
	public GlobalBackwardDataflowAnalysis(){
	}


	protected void internalTransform(String string, @SuppressWarnings("rawtypes") Map map) {
		this.cg = Scene.v().getCallGraph();
		this.callgraph = cg;
		call_graph = cg;
		
		//pta = Scene.v().getPointsToAnalysis();
		
		//annotateAPIs();
		
		doDataFlowAnalysis();
		
		
		if(AnalyzerMain.dataFlowForSQLAnalysis == false)
		{
			Set<Stmt> sKeySet = this.stmtToNodeMap.keySet();
			Iterator<Stmt> sIter = sKeySet.iterator();
			while(sIter.hasNext()){
				Stmt s = sIter.next();
				APIGraphNode node = this.stmtToNodeMap.get(s);
				this.apiDDGGraph.add(node);
			}
		}
		
		clearDataStructures();
		
		//RemoveNonImportantNodes();
		
		//detectRecursiveCallsites(Scene.v().getEntryPoints(), recursiveCallsites);

		//ClassificationFeatureExtraction.buildEntryPointToMethodsMap(Scene.v().getEntryPoints(),true);
		/*
		if(AnalyzerMain.CLASSPATH.contains("malware")){
			extractFeatures(this.apiDDGGraph, MAL_FEATURE_LOG);
		}else if(AnalyzerMain.CLASSPATH.contains("benign")){
			extractFeatures(this.apiDDGGraph, BENIGN_FEATURE_LOG);
		}
		*/
		//cfe = new ClassificationFeatureExtraction(this.apiDDGGraph);
		//cfe.FeatureExtraction();
		
//		if(AnalyzerMain.slicingForDiff == false)
//		{
//			dumpDDGEdgeListToDot(this.apiDDGGraph, DDG_GLOBAL_DOT, false);
//			AnalyzerMain.dumpDDGtoStructure(this.apiDDGGraph, false);
//		}
		
	}
	
//	private void RemoveNonImportantNodes() 
//	{
//		if(AnalyzerMain.slicingForDiff == false)
//		{
//			removeNonAPIorConstNode(apiDDGGraph);
//			removeNonModeledNode(apiDDGGraph);
//		}
//		
//		List<String> classFilter = new ArrayList<String>();
//		
//		classFilter.add("java.");
//		classFilter.add("javax.");
//		classFilter.add("org.");
//		
//		List<String> classPreserveSet = new ArrayList<String>();
//		classPreserveSet.add("java.lang.Runtime");
//		classPreserveSet.add("java.lang.reflect.Method");
//		classPreserveSet.add("java.lang.ClassLoader");
//		
//		if(AnalyzerMain.slicingForDiff == true)
//		{
//			classPreserveSet.add("java.lang.String");
//			classPreserveSet.add("java.lang.StringBuffer");
//			classPreserveSet.add("java.lang.StringBuilder");
//			classPreserveSet.add("java.util.Date");
//			classPreserveSet.add("java.util.Calendar");
//		}
//		
//		classPreserveSet.add("java.io.");
//		classPreserveSet.add("org.apache.http.client.");
//		classPreserveSet.add("org.apache.http.impl.client.");
//		classPreserveSet.add("java.net.");
//		
//		removeSpecificAPINode(apiDDGGraph, classFilter, classPreserveSet);
//		
//		List<String> packageFilter = new ArrayList<String>();		
//		for(String adsPackage : MyConstants.AdLibs){
//			packageFilter.add(adsPackage);
//		}
//		
//		removeSpecificPackage(apiDDGGraph, packageFilter);
//	}
//
//	public static void removeNonModeledNode(List<APIGraphNode> apiDDGGraph) 
//	{
//		List<APIGraphNode> toRemove = new ArrayList<APIGraphNode>();
//		
//		for(APIGraphNode node: apiDDGGraph)
//		{
//			Stmt s = node.getStmt();
//
//			if(s==null)
//			{
//				System.err.println("Error: statement is null!");
//			}
//			
//			if(!s.containsInvokeExpr())
//			{
//				toRemove.add(node);
//			}
//			else
//			{
//				boolean b_remove = true;
//				
//				String apiSig = s.getInvokeExpr().getMethod().getSignature();
//				apiSig = apiSig.replace('<', '(');
//				apiSig = apiSig.replace('>', ')');
//				for(String api: MyConstants.ModeledAPIList)
//				{
//					if(apiSig.equals(api))
//					{
//						b_remove = false;
//						break;
//					}
//				}
//				
//				if(b_remove)
//				{
//					toRemove.add(node);
//				}
//			}
//		}//end for each node in graph
//		
//		for(APIGraphNode node : toRemove){		
//			deleteNode(apiDDGGraph, node);
//		}
//	}

	private void clearDataStructures()
	{
		propagationHistory.clear();
		usedStaticFieldMap.clear();
		usedInstanceFieldMap.clear();
		sMethodsWithSources.clear();
		taintedFieldsInCallee.clear();
		taintedFieldsInCaller.clear();
		sinks.clear();
		srcs.clear();
		methodToEquiv.clear();
		callString.clear();
		propagationHistory.clear();
		methodToSourceToVar.clear();
		classToSourceToField.clear();
		methodToTaintVar.clear();
		classToTaintField.clear();
		methodToDDGMap.clear();
		stmtToNodeMap.clear();
		fieldToUsesMap.clear();
		fieldToDefsMap.clear();
	}
	

	private void doDataFlowAnalysis() 
	{		
		System.err.println("starting backward dataflow analysis...");
		//LinkedHashMap<SplitTag, SootMethod> tagToSplitEntry = computeSplitsAndAddTags();

		Set<String> sinkKey = AnalyzerMain.sourcesLocationMap.keySet();
		Iterator<String> sinkIter = sinkKey.iterator();
		while(sinkIter.hasNext()){

			String flowSink = sinkIter.next();
			
			LinkedHashMap<String, String> entryPointsString = AnalyzerMain.sourcesLocationMap.get(flowSink);
			String sinkSig = "";
			
			TaintTag taintTag = taintTagMap.get(flowSink);
			TaintTag extraDefTag = extraDefTagMap.get(flowSink);
			if(MyConstants.DEBUG_INFO)
			{
				System.out.println("dataflow analysis");
				System.out.println("loading function summaries");
			}
			

			List<SootMethod> entryPoints = new ArrayList<SootMethod>();

			LinkedHashMap<SootField, Vector<Integer>> instanceFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();
			LinkedHashMap<SootField, Vector<Integer>> staticFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();

			List<SootField> taintedFields = new ArrayList<SootField>();

			
			//set entry points for dataflow
			Set<String> keySet = entryPointsString.keySet();
			//this stmtMethod stores the method signature in which the flowsink(condition) locates
			String stmtMethod = "";
			
			Iterator<String> keyIterator = keySet.iterator();
			while (keyIterator.hasNext()) {
				String mClass = keyIterator.next();
				String method = entryPointsString.get(mClass);
				
				if(sinkSig.isEmpty())
				{
					sinkSig = flowSink + "|" + mClass;
				}
				
				stmtMethod = mClass.substring(mClass.indexOf("|") + 1);
				mClass = mClass.substring(0, mClass.indexOf("|"));
				
				SootClass entryClass = Scene.v().loadClassAndSupport(mClass);

				if(MyConstants.DEBUG_INFO){
					System.out.println("loading method " + method + " from " + entryClass);
				}
				SootMethod entryMethod = entryClass.getMethod(method);
				entryMethod.setDeclaringClass(entryClass);
				entryPoints.add(entryMethod);
			}

			Queue<SootMethod> worklist = new LinkedList<SootMethod>();
			List<SootMethod> fullWorklist = new LinkedList<SootMethod>();

			worklist.addAll(entryPoints);
			fullWorklist.addAll(entryPoints);

			//If dataflow reaches a "Identity" statement, we put caller name into sourceMethods. Further we track the dataflow in caller, starting from such function call.
			List<SootMethod> sourceMethods = new ArrayList<SootMethod>();

			boolean breakAnalysis = false;
			
			//dataflow analysis phase one
			while(!worklist.isEmpty()){
				
				if(MyConstants.TO_CONSIDER_LIMIT){
					if(apiNodeCount > MyConstants.MAX_APINODES_CONSIDERED)
					{
						//nodeCount = 0;
						apiNodeCount = 0;
						breakAnalysis = true;
						break;
					}
				}
				
				SootMethod sMethod = worklist.remove();
				
				if(!methodToDDGMap.containsKey(sMethod)){
					List<APIGraphNode> apiGraph = new ArrayList<APIGraphNode>();
					methodToDDGMap.put(sMethod, apiGraph);
				}
				
				
				//setup equivalent table
				boolean hasEquivTable = false;
				LinkedHashMap<Stmt, List<Stmt>> equivTable = null;

				if(methodToEquiv.containsKey(sMethod)){
					hasEquivTable = true;
					equivTable = methodToEquiv.get(sMethod);
				}

				if(MyConstants.DEBUG_INFO){
					System.out.println();
					System.out.println("analyzing method:" + sMethod.getSignature());
				}

				JimpleBody body = (JimpleBody) sMethod.retrieveActiveBody();
				ExceptionalUnitGraph eug = new ExceptionalUnitGraph(body);

				MyReachingDefinition mrd = new MyReachingDefinition(eug);

				Stack<UseWithScope> usesStack = new Stack<UseWithScope>();
				//Vector<UseWithScope> taintedRefUses = new Vector<UseWithScope>();

				//hashmap to record all analyzed definitions. key is definition, object is the scope(s).
				LinkedHashMap<Stmt, Vector<Stmt>> uses = new LinkedHashMap<Stmt, Vector<Stmt>>();

				Stmt sink = null;

				//identify "sources" in current method
				{
					Iterator<Unit> it = body.getUnits().iterator();
					while (it.hasNext()) {
						Stmt s = (Stmt) it.next();
						
						//added for BBN project. do data-flow analysis for all the variables in sink statement
						if(s.toString().equals(flowSink) && sMethod.getSignature().equals(stmtMethod))
						{
							if(MyConstants.DEBUG_SQL)
								System.err.println("SINK FOUND: " + s);
							UseWithScope sWS = new UseWithScope(s,s);
							if(!uses.containsKey(s))
							{
								uses.put(s, new Vector<Stmt>());
								usesStack.push(sWS);
								
								if(MyConstants.DEBUG_INFO)
									System.out.println("use stack doesn't contain " + sWS.dump() + ". Push it.");
								sink = s;
							}
						}
					}
				}

				while(!usesStack.isEmpty()){

					UseWithScope useWS = usesStack.pop();
					if(MyConstants.DEBUG_INFO)
						System.out.println("POP from use stack: " + useWS.dump());

					/*
					if(hasEquivTable){

						if(MyConstants.DEBUG_INFO)
							System.out.println(sMethod + "has equivTable: " + equivTable);
						if(equivTable.containsKey(useWS.getUse())){

							List<Stmt> equivs = equivTable.get(useWS.getUse());

							if(MyConstants.DEBUG_INFO)
								System.out.println("EQUIV found: " + useWS.getUse() + "|" + equivs);

							for(Stmt equiv : equivs){
								UseWithScope equivWS = new UseWithScope(equiv);
								if (!uses.containsKey(equiv)) {
									uses.put(equiv, new Vector<Stmt>());
									usesStack.push(equivWS);
									if(MyConstants.DEBUG_INFO)
										System.out.println("use stack doesn't contain " + equivWS.dump() + ". Push it.");
								}
							}
						}
					}
					*/

					//use-def analysis
					Stmt s = useWS.getUse();
					Stmt sScope = useWS.getScopeEnd();
					
						
					APIGraphNode sNode = CreateOrGetExistingNode(s, sMethod);

					if(!methodToDDGMap.get(sMethod).contains(sNode)){
						methodToDDGMap.get(sMethod).add(sNode);
					}
					
					if (s instanceof InvokeStmt) {
						
						if(s.getInvokeExpr().getMethod().getSignature().equals(flowSink)
								|| sourceMethods.contains(s.getInvokeExpr().getMethod()))
						{
							List<ValueBox> usesBoxes = s.getUseBoxes();
							Iterator usesIter = usesBoxes.iterator();
							while(usesIter.hasNext()){
								ValueBox usesBox = (ValueBox)usesIter.next();
								if(usesBox.getValue() instanceof Local){
									List<Unit> defs = mrd.getDefsOfAt((Local)usesBox.getValue(), s);
									for(Unit def : defs){
										
										APIGraphNode defNode = CreateOrGetExistingNode((Stmt)def, sMethod);;

										sNode.addPred(defNode);
										defNode.addSucc(sNode);
										if(!methodToDDGMap.get(sMethod).contains(defNode)){
											methodToDDGMap.get(sMethod).add(defNode);
										}
																				
										UseWithScope defofuseWS = new UseWithScope((Stmt)def, s);
										if(!uses.containsKey((Stmt)def)){
											Vector<Stmt> scopes = new Vector<Stmt>();
											scopes.add(s);
											uses.put((Stmt)def, scopes);
											usesStack.push(defofuseWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("use stack: " + defofuseWS.dump() + ". Push it.");
										}else{
											Vector<Stmt> scopes = uses.get((Stmt)def);
											if(!scopes.contains(s)){
												scopes.add(s);
												usesStack.push(defofuseWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("use stack: " + defofuseWS.dump() + ". Push it.");
											}
										}
									}
								}
							}
						}
					}
					//added for TPL project
					else if(s.toString().equals(flowSink) && sMethod.getSignature().equals(stmtMethod))
					{
						//System.err.println("location2: " + s);
						Iterator usesIter = s.getUseBoxes().iterator();
						while(usesIter.hasNext()){
							ValueBox usesBox = (ValueBox)usesIter.next();
							System.err.println("use box: " + usesBox.toString());
							if(usesBox.getValue() instanceof Local){
								List<Unit> defs = mrd.getDefsOfAt((Local)usesBox.getValue(), s);
								for(Unit def : defs){
									System.err.println("def: " + def.toString());
									APIGraphNode defNode = CreateOrGetExistingNode((Stmt)def, sMethod);;

									sNode.addPred(defNode);
									defNode.addSucc(sNode);
									if(!methodToDDGMap.get(sMethod).contains(defNode)){
										methodToDDGMap.get(sMethod).add(defNode);
									}
																			
									UseWithScope defofuseWS = new UseWithScope((Stmt)def, s);
									if(!uses.containsKey((Stmt)def)){
										Vector<Stmt> scopes = new Vector<Stmt>();
										scopes.add(s);
										uses.put((Stmt)def, scopes);
										usesStack.push(defofuseWS);
										if(MyConstants.DEBUG_INFO)
											System.out.println("use stack: " + defofuseWS.dump() + ". Push it.");
									}else{
										Vector<Stmt> scopes = uses.get((Stmt)def);
										if(!scopes.contains(s)){
											scopes.add(s);
											usesStack.push(defofuseWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("use stack: " + defofuseWS.dump() + ". Push it.");
										}
									}
								}
							}
						}
					}
					else
					{
						boolean isInvoke = false;

						Iterator iUse = s.getUseBoxes().iterator();
						while (iUse.hasNext()) {
							ValueBox vB = (ValueBox) iUse.next();
							if (vB.getValue() instanceof InvokeExpr) {
								isInvoke = true;
							}
						}

						//rhs is invoke, lhs is ret
						if (isInvoke) {

							if(s.getInvokeExpr().getMethod().getSignature().equals(flowSink)
									|| sourceMethods.contains(s.getInvokeExpr().getMethod())){

								List<ValueBox> usesBoxes = s.getUseBoxes();
								Iterator usesIter = usesBoxes.iterator();
								while(usesIter.hasNext()){
									ValueBox usesBox = (ValueBox)usesIter.next();
									if(usesBox.getValue() instanceof Local){
										List<Unit> defs = mrd.getDefsOfAt((Local)usesBox.getValue(), s);
										for(Unit def : defs){
											
											APIGraphNode defNode = CreateOrGetExistingNode((Stmt)def, sMethod);;

											sNode.addPred(defNode);
											defNode.addSucc(sNode);
											if(!methodToDDGMap.get(sMethod).contains(defNode)){
												methodToDDGMap.get(sMethod).add(defNode);
											}

											UseWithScope defofuseWS = new UseWithScope((Stmt)def, s);
											if(!uses.containsKey((Stmt)def)){
												Vector<Stmt> scopes = new Vector<Stmt>();
												scopes.add(s);
												uses.put((Stmt)def, scopes);
												usesStack.push(defofuseWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("use stack: " + defofuseWS.dump() + ". Push it.");
											}else{
												Vector<Stmt> scopes = uses.get((Stmt)def);
												if(!scopes.contains(s)){
													scopes.add(s);
													usesStack.push(defofuseWS);
													if(MyConstants.DEBUG_INFO)
														System.out.println("use stack: " + defofuseWS.dump() + ". Push it.");
												}
											}
										}
									}
								}
							}
							else {
								if(s instanceof DefinitionStmt){
									Value lhs = ((DefinitionStmt) s).getLeftOp();

									if(MyConstants.CONSIDER_REDEFINE && lhs.getType() instanceof RefLikeType){

										if(MyConstants.DEBUG_INFO)
											System.out.println("looking for redefine:" + s);

										Iterator itForRedefine = body.getUnits().iterator();
										while (itForRedefine.hasNext()) {
											Stmt stmt = (Stmt) itForRedefine.next();

											if(!isInScope(eug, stmt, sScope)){
												if(MyConstants.DEBUG_INFO)
													System.out.println(stmt + " is NOT in scope[<--" + sScope + "]");
												continue;
											}

											// see if 'stmt' contains any local that is defined at 's'
											boolean isStmtUsingS = false;
											List<ValueBox> useBoxesofStmt = stmt.getUseBoxes();
											for(ValueBox useBox : useBoxesofStmt){
												if(useBox.getValue() instanceof Local){
													if(mrd.getDefsOfAt((Local)(useBox.getValue()), stmt).contains(s)){
														isStmtUsingS = true;
														break;
													}
												}
											}

											if(isStmtUsingS){
												if(MyConstants.DEBUG_INFO)
													System.out.println(stmt + " IS using " + s);
												
												if(stmt.containsInvokeExpr()){
													if(!stmt.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
														AddTags(stmt, API_TAG);
													}
												}					
												
												if(stmt instanceof DefinitionStmt){
													//if(!stmt.containsInvokeExpr()){
														boolean usesConstant = false;
														List<ValueBox> checkConstUseBoxes = stmt.getUseBoxes();
														for(ValueBox ccVB : checkConstUseBoxes){
															if(ccVB.getValue() instanceof StringConstant){
																if(!((StringConstant)ccVB.getValue()).value.equals("")){
																	usesConstant = true;
																	break;
																}
															}
														}					
														if(usesConstant){
															AddTags(stmt, STRING_CONST_TAG);	
														}
													//}
												}

												APIGraphNode stmtNode = CreateOrGetExistingNode(stmt, sMethod);;

												if(!methodToDDGMap.get(sMethod).contains(stmtNode)){
													methodToDDGMap.get(sMethod).add(stmtNode);
												}

												APIGraphNode sScopeNode = CreateOrGetExistingNode(sScope, sMethod);;

												if(!methodToDDGMap.get(sMethod).contains(sScopeNode)){
													methodToDDGMap.get(sMethod).add(sScopeNode);
												}

												sNode.removeSucc(sScopeNode);
												sScopeNode.removePred(sNode);

												sNode.addSucc(stmtNode);
												stmtNode.addPred(sNode);

												stmtNode.addSucc(sScopeNode);
												sScopeNode.addPred(stmtNode);

												if(stmt instanceof InvokeStmt){

													Vector<Integer> taintVector = new Vector<Integer>();

													// go through each defBox within 's' to see if it is a parameter of the invokeStmt
													// If so, add the arg into taintVector
													Iterator defIt2 = s.getDefBoxes().iterator();
													while (defIt2.hasNext()) {
														ValueBox vbox2 = (ValueBox) defIt2.next();
														if (vbox2.getValue() instanceof Local) {
															// System.out.println(vbox2.getValue());
															InvokeExpr invokeEx = stmt.getInvokeExpr();
															int argCount = invokeEx.getArgCount();
															for (int i = 0; i < argCount; i++) {
																if (invokeEx.getArg(i) == vbox2.getValue()) {
																	taintVector.add(new Integer(i));
																}												
															}

															//for instance invoke, consider this reference too.
															if(invokeEx instanceof InstanceInvokeExpr){
																if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

																	/*
																//special invoke doesn't count
																if(invokeEx instanceof SpecialInvokeExpr){
																	if(rhs instanceof NewExpr){
																		continue;
																	}																	
																}
																	 */
																	if(MyConstants.TO_TAINT_THIS_OBJECT)
																		taintVector.add(new Integer(MyConstants.thisObject));
																}
															}
														}
													} 

													Iterator targets = null;
													if(stmt.getInvokeExpr().getMethod().isConcrete()){
														if(MyConstants.DEBUG_INFO)
															System.out.println(stmt + " calls CONCRETE method: " + stmt.getInvokeExpr().getMethod());
														List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
														defaultTargets.add(stmt.getInvokeExpr().getMethod());
														targets = defaultTargets.iterator();
													}else{	
														if(MyConstants.DEBUG_INFO)
															System.out.println(stmt + " calls NON-CONCRETE method: " + stmt.getInvokeExpr().getMethod());
														targets = new Targets(this.cg.edgesOutOf(stmt));										

														if(!targets.hasNext()){
															if(MyConstants.DEBUG_INFO)
																System.out.println(stmt + " does NOT have a target. add a DEFAULT one");
															List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
															defaultTargets.add(stmt.getInvokeExpr().getMethod());
															targets = defaultTargets.iterator();
														}
													}
													
													if(targets==null){
														continue;
													}

													while (targets.hasNext()) {
														SootMethod target = (SootMethod) targets.next();

														boolean noNewTaint = true;
														if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+stmt.toString())){														
															noNewTaint = false;
															List<Integer> sinks = new ArrayList<Integer>();
															sinks.addAll(taintVector);
															propagationHistory.put(sMethod.getSignature()+"|"+stmt.toString(), sinks);
														}else{
															List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+stmt.toString());

															for(Integer taint : taintVector){
																if(!sinks.contains(taint)){
																	noNewTaint = false;
																	sinks.add(taint);
																}
															}														
														}

														if(noNewTaint){
															break;
														}	

														if(MyConstants.DEBUG_INFO){
															System.out.println("PROPAGATING from METHOD: " + sMethod);
															System.out.println("PROPAGATING from STATEMENT: " + stmt);
														}
														taintedFieldsInCaller.addAll(taintedFields);
														Vector<Integer> tainted = propagate(target, taintVector, flowSink, stmt, sMethod);
														for(SootField sf : taintedFieldsInCallee){
															if(!taintedFields.contains(sf)){
																taintedFields.add(sf);
															}
														}
														taintedFieldsInCallee.clear();

														if(MyConstants.DEBUG_INFO){
															System.out.println(stmt + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
														}
														if ((tainted!=null) && (!tainted.isEmpty())) {
															AddTags(stmt, functionPreservingTag);

															for(Integer i : tainted){
																int index = i.intValue();

																if(index == MyConstants.thisObject){
																	if(stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
																		Value taintedThisRef = ((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase();

																		if(taintedThisRef instanceof Local){
																			List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, stmt);

																			for(Unit defn : defs0){

																				APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;
																	
																				stmtNode.addPred(defNode);
																				defNode.addSucc(stmtNode);
																				if(!methodToDDGMap.get(sMethod).contains(defNode)){
																					methodToDDGMap.get(sMethod).add(defNode);
																				}

																				UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																				if(!uses.containsKey(defn)){
																					Vector<Stmt> scopes = new Vector<Stmt>();
																					scopes.add(stmt);
																					uses.put((Stmt)defn, scopes);
																					usesStack.push(defnWS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																				}else if(!(uses.get(defn).contains(stmt))){
																					uses.get(defn).add(stmt);
																					usesStack.push(defnWS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																				}
																			}
																		}

																	}														 

																}else if(index >= 0){

																	Value taintedArg = stmt.getInvokeExpr().getArg(index);

																	if(taintedArg instanceof Local){
																		List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, stmt);

																		for(Unit defn : defs0){

																			APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																			stmtNode.addPred(defNode);
																			defNode.addSucc(stmtNode);
																			if(!methodToDDGMap.get(sMethod).contains(defNode)){
																				methodToDDGMap.get(sMethod).add(defNode);
																			}

																			UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																			if(!uses.containsKey(defn)){
																				Vector<Stmt> scopes = new Vector<Stmt>();
																				scopes.add(stmt);
																				uses.put((Stmt)defn, scopes);
																				usesStack.push(defnWS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																			}else if(!(uses.get(defn).contains(stmt))){
																				uses.get(defn).add(stmt);
																				usesStack.push(defnWS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																			}
																		}
																	}
																}
															}
														}

													}


												}else if(stmt instanceof DefinitionStmt){

													Value rhsInvoke = ((DefinitionStmt) stmt).getRightOp();
													if(rhsInvoke instanceof InvokeExpr){

														Vector<Integer> taintVector = new Vector<Integer>();

														Iterator defIt2 = s.getDefBoxes().iterator();
														while (defIt2.hasNext()) {
															ValueBox vbox2 = (ValueBox) defIt2.next();
															if (vbox2.getValue() instanceof Local) {
																// System.out.println(vbox2.getValue());
																InvokeExpr invokeEx = stmt.getInvokeExpr();
																int argCount = invokeEx.getArgCount();
																for (int i = 0; i < argCount; i++) {
																	if (invokeEx.getArg(i) == vbox2.getValue()) {
																		taintVector.add(new Integer(i));
																	}												
																}

																//for instance invoke, consider this reference too.
																if(invokeEx instanceof InstanceInvokeExpr){
																	if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

																		/*
																	//special invoke doesn't count
																	if(invokeEx instanceof SpecialInvokeExpr){
																		if(rhs instanceof NewExpr){
																			continue;
																		}																	
																	}
																		 */
																		if(MyConstants.TO_TAINT_THIS_OBJECT)
																			taintVector.add(new Integer(MyConstants.thisObject));
																	}
																}
															}
														}

														Iterator targets = null;
														if(stmt.getInvokeExpr().getMethod().isConcrete()){
															if(MyConstants.DEBUG_INFO)
																System.out.println(stmt + " calls CONCRETE method: " + stmt.getInvokeExpr().getMethod());
															List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
															defaultTargets.add(stmt.getInvokeExpr().getMethod());
															targets = defaultTargets.iterator();
														}else{	
															if(MyConstants.DEBUG_INFO)
																System.out.println(stmt + " calls NON-CONCRETE method: " + stmt.getInvokeExpr().getMethod());
															targets = new Targets(this.cg.edgesOutOf(stmt));										

															if(!targets.hasNext()){
																if(MyConstants.DEBUG_INFO)
																	System.out.println(stmt + " does NOT have a target. add a DEFAULT one");
																List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
																defaultTargets.add(stmt.getInvokeExpr().getMethod());
																targets = defaultTargets.iterator();
															}
														}
														
														if(targets==null){
															continue;
														}

														while (targets.hasNext()) {
															SootMethod target = (SootMethod) targets.next();

															boolean noNewTaint = true;
															if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+stmt.toString())){														
																noNewTaint = false;
																List<Integer> sinks = new ArrayList<Integer>();
																sinks.addAll(taintVector);
																propagationHistory.put(sMethod.getSignature()+"|"+stmt.toString(), sinks);
															}else{
																List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+stmt.toString());

																for(Integer taint : taintVector){
																	if(!sinks.contains(taint)){
																		noNewTaint = false;
																		sinks.add(taint);
																	}
																}														
															}

															if(noNewTaint){
																break;
															}	

															if(MyConstants.DEBUG_INFO){
																System.out.println("PROPAGATING from METHOD: " + sMethod);
																System.out.println("PROPAGATING from STATEMENT: " + stmt);
															}
															taintedFieldsInCaller.addAll(taintedFields);
															Vector<Integer> tainted = propagate(target, taintVector, flowSink, stmt, sMethod);
															for(SootField sf : taintedFieldsInCallee){
																if(!taintedFields.contains(sf)){
																	taintedFields.add(sf);
																}
															}
															taintedFieldsInCallee.clear();

															if(MyConstants.DEBUG_INFO){
																System.out.println(stmt + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
															}
															if ((tainted!=null) && (!tainted.isEmpty())) {

																AddTags(stmt, functionPreservingTag);

																for(Integer i : tainted){
																	int index = i.intValue();

																	if(index == MyConstants.thisObject){
																		if(stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
																			Value taintedThisRef = ((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase();

																			if(taintedThisRef instanceof Local){
																				List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, stmt);

																				for(Unit defn : defs0){

																					APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																					stmtNode.addPred(defNode);
																					defNode.addSucc(stmtNode);
																					if(!methodToDDGMap.get(sMethod).contains(defNode)){
																						methodToDDGMap.get(sMethod).add(defNode);
																					}

																					UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																					if(!uses.containsKey(defn)){
																						Vector<Stmt> scopes = new Vector<Stmt>();
																						scopes.add(stmt);
																						uses.put((Stmt)defn, scopes);
																						usesStack.push(defnWS);
																						if(MyConstants.DEBUG_INFO)
																							System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																					}else if(!(uses.get(defn).contains(stmt))){
																						uses.get(defn).add(stmt);
																						usesStack.push(defnWS);
																						if(MyConstants.DEBUG_INFO)
																							System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																					}
																				}
																			}

																		}														 

																	}else if(index >= 0){

																		Value taintedArg = stmt.getInvokeExpr().getArg(index);

																		if(taintedArg instanceof Local){
																			List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, stmt);

																			for(Unit defn : defs0){

																				APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																				stmtNode.addPred(defNode);
																				defNode.addSucc(stmtNode);
																				if(!methodToDDGMap.get(sMethod).contains(defNode)){
																					methodToDDGMap.get(sMethod).add(defNode);
																				}

																				UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																				if(!uses.containsKey(defn)){
																					Vector<Stmt> scopes = new Vector<Stmt>();
																					scopes.add(stmt);
																					uses.put((Stmt)defn, scopes);
																					usesStack.push(defnWS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																				}else if(!(uses.get(defn).contains(stmt))){
																					uses.get(defn).add(stmt);
																					usesStack.push(defnWS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																				}
																			}
																		}
																	}
																}
															}

														}
													}
												}										

											}//isStmtUsingS
										}
									}//if(lhs.getType() instanceof RefLikeType){
								}//end if(s instanceof DefinitionStmt)

								Vector<Integer> taintVector = new Vector<Integer>();
								taintVector.add(new Integer(MyConstants.returnValue));
								
								Iterator targets = null;
								if(s.getInvokeExpr().getMethod().isConcrete()){
									if(MyConstants.DEBUG_INFO)
										System.out.println(s + " calls CONCRETE method: " + s.getInvokeExpr().getMethod());
									List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
									defaultTargets.add(s.getInvokeExpr().getMethod());
									targets = defaultTargets.iterator();
								}else{
									if(MyConstants.DEBUG_INFO)
										System.out.println(s + " calls NON-CONCRETE method: " + s.getInvokeExpr().getMethod());
									targets = new Targets(this.cg.edgesOutOf(s));

									if(!targets.hasNext()){
										if(MyConstants.DEBUG_INFO)
											System.out.println(s + " does NOT have a target. add a DEFAULT one");
										List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
										defaultTargets.add(s.getInvokeExpr().getMethod());
										targets = defaultTargets.iterator();
									}
								}
								
								if(targets==null){
									continue;
								}

								while (targets.hasNext()) {
									SootMethod target = (SootMethod) targets.next();

									boolean noNewTaint = true;
									if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+s.toString())){														
										noNewTaint = false;
										List<Integer> sinks = new ArrayList<Integer>();
										sinks.addAll(taintVector);
										propagationHistory.put(sMethod.getSignature()+"|"+s.toString(), sinks);
									}else{
										List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+s.toString());

										for(Integer taint : taintVector){
											if(!sinks.contains(taint)){
												noNewTaint = false;
												sinks.add(taint);
											}
										}														
									}

									if(noNewTaint){
										break;
									}	

									if(MyConstants.DEBUG_INFO){
										System.out.println("PROPAGATING from METHOD: " + sMethod);
										System.out.println("PROPAGATING from STATEMENT: " + s);
									}
									taintedFieldsInCaller.addAll(taintedFields);
									Vector<Integer> tainted = propagate(target, taintVector, flowSink, s, sMethod);
									for(SootField sf : taintedFieldsInCallee){
										if(!taintedFields.contains(sf)){
											taintedFields.add(sf);
										}
									}
									taintedFieldsInCallee.clear();

									if(MyConstants.DEBUG_INFO){
										System.out.println(s + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
									}
									if ((tainted!=null) && (!tainted.isEmpty())) {

										AddTags(s, functionPreservingTag);

										for(Integer i : tainted){
											int index = i.intValue();

											if(index == MyConstants.thisObject){
												if(s.getInvokeExpr() instanceof InstanceInvokeExpr){
													Value taintedThisRef = ((InstanceInvokeExpr)s.getInvokeExpr()).getBase();

													if(taintedThisRef instanceof Local){
														List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, s);

														for(Unit defn : defs0){

															APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

															sNode.addPred(defNode);
															defNode.addSucc(sNode);
															if(!methodToDDGMap.get(sMethod).contains(defNode)){
																methodToDDGMap.get(sMethod).add(defNode);
															}

															UseWithScope defnWS = new UseWithScope((Stmt)defn, s);
															if(!uses.containsKey(defn)){
																Vector<Stmt> scopes = new Vector<Stmt>();
																scopes.add(s);
																uses.put((Stmt)defn, scopes);
																usesStack.push(defnWS);
																if(MyConstants.DEBUG_INFO)
																	System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
															}else if(!(uses.get(defn).contains(s))){
																uses.get(defn).add(s);
																usesStack.push(defnWS);
																if(MyConstants.DEBUG_INFO)
																	System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
															}
														}
													}

												}														 

											}else if(index >= 0){

												Value taintedArg = s.getInvokeExpr().getArg(index);

												if(taintedArg instanceof Local){
													List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, s);

													for(Unit defn : defs0){

														APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

														sNode.addPred(defNode);
														defNode.addSucc(sNode);
														if(!methodToDDGMap.get(sMethod).contains(defNode)){
															methodToDDGMap.get(sMethod).add(defNode);
														}

														UseWithScope defnWS = new UseWithScope((Stmt)defn, s);
														if(!uses.containsKey(defn)){
															Vector<Stmt> scopes = new Vector<Stmt>();
															scopes.add(s);
															uses.put((Stmt)defn, scopes);
															usesStack.push(defnWS);
															if(MyConstants.DEBUG_INFO)
																System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
														}else if(!(uses.get(defn).contains(s))){
															uses.get(defn).add(s);
															usesStack.push(defnWS);
															if(MyConstants.DEBUG_INFO)
																System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
														}
													}
												}
											}
										}
									}

								}
								// invokes.add(s);

							}
						}

						//pure definiton statement:
						else {
							
							if(s instanceof DefinitionStmt){
								Value rhs = ((DefinitionStmt) s).getRightOp();
								Value lhs = ((DefinitionStmt) s).getLeftOp();	
								
								//if lhs is a reference
								if(MyConstants.CONSIDER_REDEFINE && lhs.getType() instanceof RefLikeType){											
									
									if(MyConstants.DEBUG_INFO)
										System.out.println("looking for redefine:" + s);
									
									Iterator itForRedefine = body.getUnits().iterator();
									while (itForRedefine.hasNext()) {
										Stmt stmt = (Stmt) itForRedefine.next();
										
										if(!isInScope(eug, stmt, sScope)){
											if(MyConstants.DEBUG_INFO)
												System.out.println(stmt + " is NOT in scope[<--" + sScope + "]");
											continue;
										}
										
										boolean isStmtUsingS = false;
										List<ValueBox> useBoxesofStmt = stmt.getUseBoxes();
										for(ValueBox useBox : useBoxesofStmt){
											if(useBox.getValue() instanceof Local){
												if(mrd.getDefsOfAt((Local)(useBox.getValue()), stmt).contains(s)){
													isStmtUsingS = true;
													break;
												}
											}
										}
										
										if(isStmtUsingS){
											if(MyConstants.DEBUG_INFO)
												System.out.println(stmt + " IS using " + s);
											
											if(stmt.containsInvokeExpr()){
												if(!stmt.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
													AddTags(stmt, API_TAG);
												}
											}					
											
											if(stmt instanceof DefinitionStmt){
												//if(!stmt.containsInvokeExpr()){
													boolean usesConstant = false;
													List<ValueBox> checkConstUseBoxes = stmt.getUseBoxes();
													for(ValueBox ccVB : checkConstUseBoxes){
														if(ccVB.getValue() instanceof StringConstant){
															if(!((StringConstant)ccVB.getValue()).value.equals("")){
																usesConstant = true;
																break;
															}
														}
													}					
													if(usesConstant){
														AddTags(stmt, STRING_CONST_TAG);
													}
												//}
											}
											
											APIGraphNode stmtNode = CreateOrGetExistingNode(stmt, sMethod);
											
											if(!methodToDDGMap.get(sMethod).contains(stmtNode)){
												methodToDDGMap.get(sMethod).add(stmtNode);
											}
											
											APIGraphNode sScopeNode = CreateOrGetExistingNode(sScope, sMethod);;

											if(!methodToDDGMap.get(sMethod).contains(sScopeNode)){
												methodToDDGMap.get(sMethod).add(sScopeNode);
											}
											
											sNode.removeSucc(sScopeNode);
											sScopeNode.removePred(sNode);
											
											sNode.addSucc(stmtNode);
											stmtNode.addPred(sNode);
											
											stmtNode.addSucc(sScopeNode);
											sScopeNode.addPred(stmtNode);
											
											if(stmt instanceof InvokeStmt){
												
												Vector<Integer> taintVector = new Vector<Integer>();

												Iterator defIt2 = s.getDefBoxes().iterator();
												while (defIt2.hasNext()) {
													ValueBox vbox2 = (ValueBox) defIt2.next();
													if (vbox2.getValue() instanceof Local) {
														// System.out.println(vbox2.getValue());
														InvokeExpr invokeEx = stmt.getInvokeExpr();
														int argCount = invokeEx.getArgCount();
														for (int i = 0; i < argCount; i++) {
															if (invokeEx.getArg(i) == vbox2.getValue()) {
																taintVector.add(new Integer(i));
															}												
														}

														//for instance invoke, consider this reference too.
														if(invokeEx instanceof InstanceInvokeExpr){
															if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

																/*
																//special invoke doesn't count
																if(invokeEx instanceof SpecialInvokeExpr){
																	if(rhs instanceof NewExpr){
																		continue;
																	}																	
																}
																*/
																if(MyConstants.TO_TAINT_THIS_OBJECT)
																	taintVector.add(new Integer(MyConstants.thisObject));
															}
														}
													}
												}
												
												Iterator targets = null;
												if(stmt.getInvokeExpr().getMethod().isConcrete()){
													if(MyConstants.DEBUG_INFO)
														System.out.println(stmt + " calls CONCRETE method: " + stmt.getInvokeExpr().getMethod());
													List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
													defaultTargets.add(stmt.getInvokeExpr().getMethod());
													targets = defaultTargets.iterator();
												}else{
													if(MyConstants.DEBUG_INFO)
														System.out.println(stmt + " calls NON-CONCRETE method: " + stmt.getInvokeExpr().getMethod());
													targets = new Targets(this.cg.edgesOutOf(stmt));										

													if(!targets.hasNext()){
														if(MyConstants.DEBUG_INFO)
															System.out.println(stmt + " does NOT have a target. add a DEFAULT one");
														List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
														defaultTargets.add(stmt.getInvokeExpr().getMethod());
														targets = defaultTargets.iterator();
													}
												}
												
												if(targets==null){
													continue;
												}

												while (targets.hasNext()) {
													SootMethod target = (SootMethod) targets.next();

													boolean noNewTaint = true;
													if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+stmt.toString())){														
														noNewTaint = false;
														List<Integer> sinks = new ArrayList<Integer>();
														sinks.addAll(taintVector);
														propagationHistory.put(sMethod.getSignature()+"|"+stmt.toString(), sinks);
													}else{
														List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+stmt.toString());

														for(Integer taint : taintVector){
															if(!sinks.contains(taint)){
																noNewTaint = false;
																sinks.add(taint);
															}
														}
													}

													if(noNewTaint){
														break;
													}	

													if(MyConstants.DEBUG_INFO){
														System.out.println("PROPAGATING from METHOD: " + sMethod);
														System.out.println("PROPAGATING from STATEMENT: " + stmt);
													}
													taintedFieldsInCaller.addAll(taintedFields);
													Vector<Integer> tainted = propagate(target, taintVector, flowSink, stmt, sMethod);
													for(SootField sf : taintedFieldsInCallee){
														if(!taintedFields.contains(sf)){
															taintedFields.add(sf);
														}
													}
													taintedFieldsInCallee.clear();

													if(MyConstants.DEBUG_INFO){
														System.out.println(stmt + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
													}
													if ((tainted!=null) && (!tainted.isEmpty())) {
														AddTags(stmt, functionPreservingTag);

														for(Integer i : tainted){
															int index = i.intValue();

															if(index == MyConstants.thisObject){
																if(stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
																	Value taintedThisRef = ((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase();

																	if(taintedThisRef instanceof Local){
																		List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, stmt);

																		for(Unit defn : defs0){
																			
																			APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);
																			stmtNode.addPred(defNode);
																			defNode.addSucc(stmtNode);
																			if(!methodToDDGMap.get(sMethod).contains(defNode)){
																				methodToDDGMap.get(sMethod).add(defNode);
																			}

																			UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																			if(!uses.containsKey(defn)){
																				Vector<Stmt> scopes = new Vector<Stmt>();
																				scopes.add(stmt);
																				uses.put((Stmt)defn, scopes);
																				usesStack.push(defnWS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																			}else if(!(uses.get(defn).contains(stmt))){
																				uses.get(defn).add(stmt);
																				usesStack.push(defnWS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																			}
																		}
																	}
																													
																}														 

															}else if(index >= 0){

																Value taintedArg = stmt.getInvokeExpr().getArg(index);
																
																if(taintedArg instanceof Local){
																	List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, stmt);

																	for(Unit defn : defs0){
																		
																		APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;
																		stmtNode.addPred(defNode);
																		defNode.addSucc(stmtNode);
																		if(!methodToDDGMap.get(sMethod).contains(defNode)){
																			methodToDDGMap.get(sMethod).add(defNode);
																		}

																		UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																		if(!uses.containsKey(defn)){
																			Vector<Stmt> scopes = new Vector<Stmt>();
																			scopes.add(stmt);
																			uses.put((Stmt)defn, scopes);
																			usesStack.push(defnWS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																		}else if(!(uses.get(defn).contains(stmt))){
																			uses.get(defn).add(stmt);
																			usesStack.push(defnWS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																		}
																	}
																}
															}
														}
													}

												}
												
												
											}else if(stmt instanceof DefinitionStmt){
												
												Value rhsInvoke = ((DefinitionStmt) stmt).getRightOp();
												Value right = ((DefinitionStmt) stmt).getRightOp();												
												
												if(rhsInvoke instanceof InvokeExpr){
													
													Vector<Integer> taintVector = new Vector<Integer>();
													
													Iterator defIt2 = s.getDefBoxes().iterator();
													while (defIt2.hasNext()) {
														ValueBox vbox2 = (ValueBox) defIt2.next();
														if (vbox2.getValue() instanceof Local) {
															// System.out.println(vbox2.getValue());
															InvokeExpr invokeEx = stmt.getInvokeExpr();
															int argCount = invokeEx.getArgCount();
															for (int i = 0; i < argCount; i++) {
																if (invokeEx.getArg(i) == vbox2.getValue()) {
																	taintVector.add(new Integer(i));
																}												
															}

															//for instance invoke, consider this reference too.
															if(invokeEx instanceof InstanceInvokeExpr){
																if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

																	/*
																	//special invoke doesn't count
																	if(invokeEx instanceof SpecialInvokeExpr){
																		if(rhs instanceof NewExpr){
																			continue;
																		}																	
																	}
																	*/
																	if(MyConstants.TO_TAINT_THIS_OBJECT)
																		taintVector.add(new Integer(MyConstants.thisObject));
																}
															}
														}
													}
													
													Iterator targets = null;
													if(stmt.getInvokeExpr().getMethod().isConcrete()){
														if(MyConstants.DEBUG_INFO)
															System.out.println(stmt + " calls CONCRETE method: " + stmt.getInvokeExpr().getMethod());
														List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
														defaultTargets.add(stmt.getInvokeExpr().getMethod());
														targets = defaultTargets.iterator();
													}else{	
														if(MyConstants.DEBUG_INFO)
															System.out.println(stmt + " calls NON-CONCRETE method: " + stmt.getInvokeExpr().getMethod());
														targets = new Targets(this.cg.edgesOutOf(stmt));										

														if(!targets.hasNext()){
															if(MyConstants.DEBUG_INFO)
																System.out.println(stmt + " does NOT have a target. add a DEFAULT one");
															List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
															defaultTargets.add(stmt.getInvokeExpr().getMethod());
															targets = defaultTargets.iterator();
														}
													}
													
													if(targets==null){
														continue;
													}

													while (targets.hasNext()) {
														SootMethod target = (SootMethod) targets.next();

														boolean noNewTaint = true;
														if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+stmt.toString())){														
															noNewTaint = false;
															List<Integer> sinks = new ArrayList<Integer>();
															sinks.addAll(taintVector);
															propagationHistory.put(sMethod.getSignature()+"|"+stmt.toString(), sinks);
														}else{
															List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+stmt.toString());

															for(Integer taint : taintVector){
																if(!sinks.contains(taint)){
																	noNewTaint = false;
																	sinks.add(taint);
																}
															}														
														}

														if(noNewTaint){
															break;
														}	

														if(MyConstants.DEBUG_INFO){
															System.out.println("PROPAGATING from METHOD: " + sMethod);
															System.out.println("PROPAGATING from STATEMENT: " + stmt);
														}
														taintedFieldsInCaller.addAll(taintedFields);
														Vector<Integer> tainted = propagate(target, taintVector, flowSink, stmt, sMethod);
														for(SootField sf : taintedFieldsInCallee){
															if(!taintedFields.contains(sf)){
																taintedFields.add(sf);
															}
														}
														taintedFieldsInCallee.clear();

														if(MyConstants.DEBUG_INFO){
															System.out.println(stmt + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
														}
														if ((tainted!=null) && (!tainted.isEmpty())) {
															AddTags(stmt, functionPreservingTag);

															for(Integer i : tainted){
																int index = i.intValue();

																if(index == MyConstants.thisObject){
																	if(stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
																		Value taintedThisRef = ((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase();

																		if(taintedThisRef instanceof Local){
																			List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, stmt);

																			for(Unit defn : defs0){
																				
																				APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;
																				stmtNode.addPred(defNode);
																				defNode.addSucc(stmtNode);
																				if(!methodToDDGMap.get(sMethod).contains(defNode)){
																					methodToDDGMap.get(sMethod).add(defNode);
																				}

																				UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																				if(!uses.containsKey(defn)){
																					Vector<Stmt> scopes = new Vector<Stmt>();
																					scopes.add(stmt);
																					uses.put((Stmt)defn, scopes);
																					usesStack.push(defnWS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																				}else if(!(uses.get(defn).contains(stmt))){
																					uses.get(defn).add(stmt);
																					usesStack.push(defnWS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																				}
																			}
																		}
																														
																	}														 

																}else if(index >= 0){

																	Value taintedArg = stmt.getInvokeExpr().getArg(index);
																	
																	if(taintedArg instanceof Local){
																		List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, stmt);

																		for(Unit defn : defs0){
																			
																			APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;
																			stmtNode.addPred(defNode);
																			defNode.addSucc(stmtNode);
																			if(!methodToDDGMap.get(sMethod).contains(defNode)){
																				methodToDDGMap.get(sMethod).add(defNode);
																			}

																			UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																			if(!uses.containsKey(defn)){
																				Vector<Stmt> scopes = new Vector<Stmt>();
																				scopes.add(stmt);
																				uses.put((Stmt)defn, scopes);
																				usesStack.push(defnWS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																			}else if(!(uses.get(defn).contains(stmt))){
																				uses.get(defn).add(stmt);
																				usesStack.push(defnWS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																			}
																		}
																	}
																}
															}
														}

													}
												}else{
													Value left = ((DefinitionStmt) stmt).getLeftOp();													
													Iterator it = left.getUseBoxes().iterator();
													while(it.hasNext()){
														Value leftValue = ((ValueBox)it.next()).getValue();
														if(leftValue instanceof Local){
															List<Unit> defsOfLeftValue = mrd.getDefsOfAt((Local)leftValue, stmt);
															
														}
													}
												}
											}										
											
										}//isStmtUsingS
									}
								}//if(lhs.getType() instanceof RefLikeType){
								
								//rhs is parameter ref
								if(rhs instanceof ParameterRef){
									if(MyConstants.DEBUG_INFO){
										System.out.println("returning to caller...");
									}
									if(MyConstants.DEBUG_INFO)
										System.out.println("return to caller from: " + sMethod + " | " + s);
									
									Chain<SootClass> classes = Scene.v().getClasses();
									Iterator<SootClass> classes_iter = classes.iterator();
									while (classes_iter.hasNext()) {
										SootClass soot_class = classes_iter.next();

										if (soot_class.isApplicationClass() == false) {
											continue;
										}

										if(soot_class.isPhantom()){
											continue;
										}

										List<SootMethod> methods = soot_class.getMethods();
										for (SootMethod method : methods) {
											
											if(!method.isConcrete()){
												continue;
											}
											
											JimpleBody callerBody = (JimpleBody)method.retrieveActiveBody();
											Iterator callerIter = callerBody.getUnits().iterator();
											while(callerIter.hasNext()){
												Stmt callerStmt = (Stmt)callerIter.next();
												if(callerStmt.containsInvokeExpr()){
													SootMethod calleeMethod = callerStmt.getInvokeExpr().getMethod();
													if(calleeMethod.isConcrete()){
														if(calleeMethod.equals(sMethod)){
															
															APIGraphNode callerStmtNode = CreateOrGetExistingNode(callerStmt, method);;
															sNode.addPred(callerStmtNode);
															callerStmtNode.addSucc(sNode);
															if(!methodToDDGMap.containsKey(method)){
																List<APIGraphNode> ddg = new ArrayList<APIGraphNode>();
																ddg.add(callerStmtNode);
																methodToDDGMap.put(method, ddg);
															}else{
																if(!methodToDDGMap.get(method).contains(callerStmtNode)){
																	methodToDDGMap.get(method).add(callerStmtNode);
																}
															}
															
															if(!fullWorklist.contains(method)){
																worklist.add(method);
																fullWorklist.add(method);																																
															}
															
															// YUE: disable global for now
															if(false) {
																if(!sourceMethods.contains(sMethod)){
																	if(MyConstants.DEBUG_INFO)
																		System.out.println("adding sourceMethod: " + sMethod);
																	sourceMethods.add(sMethod);
																}
															}
														}
													}else{
														Iterator targets = new Targets(cg.edgesOutOf(callerStmt));													

														while (targets.hasNext()) {
															SootMethod target = (SootMethod) targets.next();
															//System.out.println(method + " may call " + target);
															if(target.equals(sMethod)){
																
																APIGraphNode callerStmtNode = CreateOrGetExistingNode(callerStmt, method);;
																sNode.addPred(callerStmtNode);
																callerStmtNode.addSucc(sNode);
																if(!methodToDDGMap.containsKey(method)){
																	List<APIGraphNode> ddg = new ArrayList<APIGraphNode>();
																	ddg.add(callerStmtNode);
																	methodToDDGMap.put(method, ddg);
																}else{
																	if(!methodToDDGMap.get(method).contains(callerStmtNode)){
																		methodToDDGMap.get(method).add(callerStmtNode);
																	}
																}
																
																if(!fullWorklist.contains(method)){
																	worklist.add(method);
																	fullWorklist.add(method);																																	
																}
																
																//Yue: disable global for now
																if(false) {
																	if(!sourceMethods.contains(sMethod)){
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("adding sourceMethod: " + sMethod);
																		sourceMethods.add(sMethod);
																	}
																}
															}														
														}
													}
												}
											}
											
											/*
											Iterator targets = new Targets(cg.edgesOutOf(method));													

											while (targets.hasNext()) {
												SootMethod target = (SootMethod) targets.next();
												//System.out.println(method + " may call " + target);
												if(MyConstants.DEBUG_INFO)
													System.out.println("comparing " + target + " with " + sMethod);
												if(target.equals(sMethod)){
													if(!fullWorklist.contains(method)){
														worklist.add(method);
														fullWorklist.add(method);
														if(!sourceMethods.contains(method)){
															if(MyConstants.DEBUG_INFO)
																System.out.println("adding sourceMethod: " + method);
															sourceMethods.add(method);
														}																
													}
												}														
											}
											*/
										}
									}
								}else if(rhs instanceof InstanceFieldRef){
									if(MyConstants.TO_TAINT_INSTANCE_FIELD){
										if(!taintedFields.contains(((InstanceFieldRef)rhs).getField())){
											if(MyConstants.DEBUG_INFO)
												System.out.println("adding new field as source: " + ((InstanceFieldRef)rhs).getField() + " from: " + s);
											taintedFields.add(((InstanceFieldRef)rhs).getField());
												
											/*
											SootField fieldKey = ((InstanceFieldRef)rhs).getField();
											if(fieldToUsesMap.containsKey(fieldKey)){
												List<Stmt> fieldUses = fieldToUsesMap.get(fieldKey);
												if(!fieldUses.contains(s)){
													fieldUses.add(s);
												}
											}else{
												List<Stmt> fieldUses = new ArrayList<Stmt>();
												fieldToUsesMap.put(fieldKey, fieldUses);
												fieldUses.add(s);
											}
											*/
										}
										
										SootField fieldKey = ((InstanceFieldRef)rhs).getField();
										if(fieldToUsesMap.containsKey(fieldKey)){
											List<Stmt> fieldUses = fieldToUsesMap.get(fieldKey);
											if(!fieldUses.contains(s)){
												fieldUses.add(s);
											}
										}else{
											List<Stmt> fieldUses = new ArrayList<Stmt>();
											fieldToUsesMap.put(fieldKey, fieldUses);
											fieldUses.add(s);
										}
									}
								}else if(rhs instanceof StaticFieldRef){
									if(MyConstants.TO_TAINT_STATIC_FIELD){
										if(!taintedFields.contains(((StaticFieldRef)rhs).getField())){
											if(MyConstants.DEBUG_INFO)
												System.out.println("adding new field as source: " + ((StaticFieldRef)rhs).getField() + " from: " + s);
											taintedFields.add(((StaticFieldRef)rhs).getField());
											
											/*
											SootField fieldKey = ((StaticFieldRef)rhs).getField();
											if(fieldToUsesMap.containsKey(fieldKey)){
												List<Stmt> fieldUses = fieldToUsesMap.get(fieldKey);
												if(!fieldUses.contains(s)){
													fieldUses.add(s);
												}
											}else{
												List<Stmt> fieldUses = new ArrayList<Stmt>();
												fieldToUsesMap.put(fieldKey, fieldUses);
												fieldUses.add(s);
											}
											*/
										}
										
										SootField fieldKey = ((StaticFieldRef)rhs).getField();
										if(fieldToUsesMap.containsKey(fieldKey)){
											List<Stmt> fieldUses = fieldToUsesMap.get(fieldKey);
											if(!fieldUses.contains(s)){
												fieldUses.add(s);
											}
										}else{
											List<Stmt> fieldUses = new ArrayList<Stmt>();
											fieldToUsesMap.put(fieldKey, fieldUses);
											fieldUses.add(s);
										}
									}
								}
								
								Iterator<ValueBox> sUseIter = s.getUseBoxes().iterator();
								while(sUseIter.hasNext()){
									Value v = sUseIter.next().getValue();
									if(v instanceof Local){
										
										List<Unit> defs = mrd.getDefsOfAt((Local)v, s);
										
										for(Unit defn : defs){
											
											APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;
											sNode.addPred(defNode);
											defNode.addSucc(sNode);
											if(!methodToDDGMap.get(sMethod).contains(defNode)){
												methodToDDGMap.get(sMethod).add(defNode);
											}
											
											UseWithScope defnWS = new UseWithScope((Stmt)defn, s);
											if(!uses.containsKey(defn)){
												Vector<Stmt> scopes = new Vector<Stmt>();
												scopes.add(s);
												uses.put((Stmt)defn, scopes);
												usesStack.push(defnWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
											}else if(!(uses.get(defn).contains(s))){
												uses.get(defn).add(s);
												usesStack.push(defnWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
											}
										}
									}
								}
								
								
							}//if(s instanceof DefinitionStmt){
									
						}
					}
				}//while(!usesStack.isEmpty()){

				Iterator i = uses.keySet().iterator();
				while (i.hasNext()) {
					Stmt s = (Stmt)i.next();
					//System.out.print(s + "|");

					AddTags(s, generalTaintTag);
					AddTags(s, taintTag);
					AddTags(s, functionPreservingTag);

					Iterator usesIt = s.getUseBoxes().iterator();
					while (usesIt.hasNext()) {
						ValueBox vbox = (ValueBox) usesIt.next();
						if (vbox.getValue() instanceof Local) {
							Local l = (Local) vbox.getValue();

							Iterator rDefsIt = mrd.getDefsOfAt(l, s).iterator();
							while (rDefsIt.hasNext()) {
								Stmt next = (Stmt) rDefsIt.next();
								if(!next.getTags().contains(taintTag)){
									AddTags(next, generalTaintTag);
									AddTags(next, extraDefTag);
								}
							}
						}
					}
					//System.out.println(s.getTags());
				}
				// System.out.println();

				if(MyConstants.DEBUG_INFO){
					System.out.println();
					System.out.println("method:" + sMethod.getSignature());
					System.out.println("dataflow for " + sink + ":");
				}
				Iterator printIt = body.getUnits().iterator();
				while(printIt.hasNext()){
					Stmt s = (Stmt)printIt.next();
					if(s.getTags().contains(taintTag) || s.getTags().contains(extraDefTag)){

						if(MyConstants.DEBUG_INFO){
							if(MyConstants.DEBUG_INFO){
								//System.out.println(s + "|" + s.getTags());
								dumpTaggedStmt(s);
							}
						}

						Vector<Integer> labels = new Vector<Integer>();

						for(Tag tag : s.getTags()){
							if(taintTagReverseMap.containsKey(tag)){
								Integer label = new Integer(((TaintTag)tag).getLabel());
								if(!labels.contains(label)){
									labels.add(label);
								}
							}else if(extraDefTagReverseMap.containsKey(tag)){
								Integer label = new Integer(((TaintTag)tag).getLabel());
								if(!labels.contains(label)){
									labels.add(label);
								}
							}								
						}

						List<ValueBox> vbs = s.getUseAndDefBoxes();
						Iterator iter = vbs.iterator();
						while(iter.hasNext()){
							ValueBox vb = (ValueBox)iter.next();
							if(vb.getValue() instanceof InstanceFieldRef){
								SootField instanceField = ((InstanceFieldRef)vb.getValue()).getField();

								if(instanceField.getDeclaringClass().isApplicationClass() == false){
									continue;
								}

								if(instanceField.getDeclaringClass().isPhantom()){
									continue;
								}

								//if(!instanceFields.contains(instanceField)){
								//	instanceFields.add(instanceField);
								//}
								////							
								if(!instanceFieldMap.containsKey(instanceField)){						

									Vector<Integer> taintSources = new Vector<Integer>();
									taintSources.addAll(labels);							
									instanceFieldMap.put(instanceField, taintSources);

								}else{

									Vector<Integer> taintSources = instanceFieldMap.get(instanceField);
									for(Integer label : labels){
										if(!taintSources.contains(label)){
											taintSources.add(label);
										}
									}
								}
								////

								LinkedHashMap<String, List<String>> taintSourceToField = new LinkedHashMap<String, List<String>>();
								List<String> fieldList = new ArrayList<String>();
								if(fieldList.contains(instanceField.getSignature())){
									fieldList.add(instanceField.getSignature());
								}								
								taintSourceToField.put(flowSink, fieldList);
								classToSourceToField.put(sMethod.getDeclaringClass().getName(), taintSourceToField);

							}else if(vb.getValue() instanceof StaticFieldRef){
								SootField staticField = ((StaticFieldRef)vb.getValue()).getField();

								if(staticField.getDeclaringClass().isApplicationClass() == false){
									continue;
								}

								if(staticField.getDeclaringClass().isPhantom()){
									continue;
								}

								//if(!staticFields.contains(staticField)){
								//	staticFields.add(staticField);
								//}
								///
								if(!staticFieldMap.containsKey(staticField)){						

									Vector<Integer> taintSources = new Vector<Integer>();
									taintSources.addAll(labels);
									staticFieldMap.put(staticField, taintSources);

								}else{

									Vector<Integer> taintSources = staticFieldMap.get(staticField);
									for(Integer label : labels){
										if(!taintSources.contains(label)){
											taintSources.add(label);
										}
									}
								}
								///

								LinkedHashMap<String, List<String>> taintSourceToField = new LinkedHashMap<String, List<String>>();
								List<String> fieldList = new ArrayList<String>();
								if(fieldList.contains(staticField.getSignature())){
									fieldList.add(staticField.getSignature());
								}								
								taintSourceToField.put(flowSink, fieldList);
								classToSourceToField.put(sMethod.getDeclaringClass().getName(), taintSourceToField);

							}else if(vb.getValue() instanceof Local){

								String varName = ((Local)vb.getValue()).getName();								
								LinkedHashMap<String, List<String>> taintSourceToVar = new LinkedHashMap<String, List<String>>();
								List<String> varList = new ArrayList<String>();
								if(varList.contains(varName)){
									varList.add(varName);
								}								
								taintSourceToVar.put(flowSink, varList);
								methodToSourceToVar.put(sMethod.getSignature(), taintSourceToVar);
							}
						}
					}
				}

				if(MyConstants.DEBUG_INFO){
					System.out.println("end dataflow for " + sink + "\n");
				}

			}//while(!worklist.isEmpty())

			if(breakAnalysis)
				break;

			//doDataFlowAnalysis phase two:
			//iteratively performs data propagation for tainted static fields
			Queue<SootField> fWorklist = new LinkedList<SootField>();
			fWorklist.addAll(taintedFields);
			while(!fWorklist.isEmpty())
			{
				if(MyConstants.TO_CONSIDER_LIMIT){
					if(apiNodeCount > MyConstants.MAX_APINODES_CONSIDERED){
						//nodeCount = 0;
						apiNodeCount = 0;
						break;
					}
				}
				SootField taintedField = fWorklist.remove();
				
				if(!fieldToUsesMap.containsKey(taintedField)){
					System.out.println("ERROR: definitions of a field " + taintedField + " is not recorded!");
					continue;
				}
				List<Stmt> fieldUsesForPTA = fieldToUsesMap.get(taintedField);

				if(MyConstants.DEBUG_INFO){
					System.out.println("NEW SOURCE: TAINTED FIELD " + taintedField);
				}

				entryPoints = new ArrayList<SootMethod>();
				//instanceFields = new ArrayList<SootField>();
				//staticFields = new ArrayList<SootField>();

				Chain<SootClass> classes = Scene.v().getClasses();
				Iterator<SootClass> classes_iter = classes.iterator();
				while (classes_iter.hasNext()) {
					SootClass soot_class = classes_iter.next();

					if (soot_class.isApplicationClass() == false) {
						continue;
					}

					if(soot_class.isPhantom()){
						continue;
					}

					if(MyConstants.DEBUG_INFO){
						System.out.println("looking for define of field in " + soot_class.getName() + "..." );
					}
					//System.out.println("package name: " + soot_class.getPackageName());

					List<SootMethod> methods = soot_class.getMethods();
					for (SootMethod method : methods) {

						if(!method.isConcrete()){
							continue;
						}
						
						/*
						boolean isInSameSplit = false;
						for(Tag tag : method.getTags()){
							if(tag.getName().equals("SplitTag")){

								SplitTag splitTag = (SplitTag)tag;
								if(currentSplits.contains(splitTag)){
									isInSameSplit = true;
									SootMethod splitEntry = tagToSplitEntry.get(splitTag);
									System.out.println(method + ": Split_" + splitTag.value + "[" + splitEntry + "]");
									break;
								}								
							}
						}
						
						if(!isInSameSplit){
							continue;
						}

						 */
						JimpleBody body = (JimpleBody) method.retrieveActiveBody();
						Iterator it = body.getUnits().iterator();

						while (it.hasNext()) {
							Stmt s = (Stmt) it.next();
							if(s instanceof DefinitionStmt){
								Value lhs = ((DefinitionStmt) s).getLeftOp();
								if(lhs instanceof StaticFieldRef){
									if(((StaticFieldRef) lhs).getField().equals(taintedField)){
										entryPoints.add(method);
									}
								}

								else if(lhs instanceof InstanceFieldRef){

									if(((InstanceFieldRef) lhs).getField().equals(taintedField)){
										entryPoints.add(method);
									}						

								}
							}
						}
					}
				}

				worklist = new LinkedList<SootMethod>();
				fullWorklist = new LinkedList<SootMethod>();

				worklist.addAll(entryPoints);
				fullWorklist.addAll(entryPoints);

				sourceMethods = new ArrayList<SootMethod>();

				while(!worklist.isEmpty()){

					SootMethod sMethod = worklist.remove();		
					
					if(!methodToDDGMap.containsKey(sMethod)){
						List<APIGraphNode> apiGraph = new ArrayList<APIGraphNode>();
						methodToDDGMap.put(sMethod, apiGraph);
					}

					boolean hasEquivTable = false;
					LinkedHashMap<Stmt, List<Stmt>> equivTable = null;

					if(methodToEquiv.containsKey(sMethod)){
						hasEquivTable = true;
						equivTable = methodToEquiv.get(sMethod);
					}

					if(MyConstants.DEBUG_INFO){
						System.out.println();
						System.out.println("analyzing method:" + sMethod.getSignature());
					}

					JimpleBody body = (JimpleBody) sMethod.retrieveActiveBody();
					ExceptionalUnitGraph eug = new ExceptionalUnitGraph(body);

					MyReachingDefinition mrd = new MyReachingDefinition(eug);

					Stack<UseWithScope> usesStack = new Stack<UseWithScope>();
					Vector<UseWithScope> taintedRefUses = new Vector<UseWithScope>();

					LinkedHashMap<Stmt, Vector<Stmt>> uses = new LinkedHashMap<Stmt, Vector<Stmt>>();

					Stmt sink = null;
					{
						Iterator it = body.getUnits().iterator();
						while (it.hasNext()) {
							Stmt s = (Stmt) it.next();

							if(s instanceof DefinitionStmt){
								Value lhs = ((DefinitionStmt) s).getLeftOp();
								if(lhs instanceof StaticFieldRef){
									if(((StaticFieldRef) lhs).getField().equals(taintedField)){

										UseWithScope sWS = new UseWithScope(s, s);
										if(!uses.containsKey(s)){
											uses.put(s, new Vector<Stmt>());
											usesStack.push(sWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("use stack doesn't contain " + sWS.dump() + ". Push it.");
											sink = s;
											
											APIGraphNode sNode = CreateOrGetExistingNode(s, sMethod);;
											if(!methodToDDGMap.get(sMethod).contains(sNode)){
												methodToDDGMap.get(sMethod).add(sNode);
											}
											
											if(fieldToDefsMap.containsKey(taintedField)){
												List<Stmt> fieldDefs = fieldToDefsMap.get(taintedField);
												if(!fieldDefs.contains(s)){
													fieldDefs.add(s);
												}
											}else{
												List<Stmt> fieldDefs = new ArrayList<Stmt>();
												fieldDefs.add(s);
												fieldToDefsMap.put(taintedField, fieldDefs);
											}
											
											/*
											List<Stmt> fieldUses = fieldToUsesMap.get(((StaticFieldRef) lhs).getField());
											for(Stmt fieldUse : fieldUses){
												if(stmtToNodeMap.containsKey(fieldUse)){
													APIGraphNode fieldUseNode = stmtToNodeMap.get(fieldUse);
													fieldUseNode.addPred(sNode);
													sNode.addSucc(fieldUseNode);
												}
											}
											*/
										}
									}
								}

								else if(lhs instanceof InstanceFieldRef){
									if(((InstanceFieldRef) lhs).getField().equals(taintedField)){
										
										boolean ptsHasIntersection = false;
										PointsToSet ptsLhs = pta.reachingObjects((Local)((InstanceFieldRef)lhs).getBase());
										if(!ptsLhs.isEmpty()){
										
											for(Stmt fieldUseForPTA : fieldUsesForPTA){
												PointsToSet ptsToTest = pta.reachingObjects((Local)((InstanceFieldRef)((DefinitionStmt)fieldUseForPTA)
														.getRightOp()).getBase());
												if(ptsLhs.hasNonEmptyIntersection(ptsToTest)){
													ptsHasIntersection = true;
													break;
												}
											}
										}
										
										if(ptsHasIntersection || ptsLhs.isEmpty()){	

											UseWithScope sWS = new UseWithScope(s, s);
											if(!uses.containsKey(s)){
												uses.put(s, new Vector<Stmt>());
												usesStack.push(sWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("use stack doesn't contain " + sWS.dump() + ". Push it.");
												sink = s;
												
												APIGraphNode sNode = CreateOrGetExistingNode(s, sMethod);;
												if(!methodToDDGMap.get(sMethod).contains(sNode)){
													methodToDDGMap.get(sMethod).add(sNode);
												}
												
												if(fieldToDefsMap.containsKey(taintedField)){
													List<Stmt> fieldDefs = fieldToDefsMap.get(taintedField);
													if(!fieldDefs.contains(s)){
														fieldDefs.add(s);
													}
												}else{
													List<Stmt> fieldDefs = new ArrayList<Stmt>();
													fieldDefs.add(s);
													fieldToDefsMap.put(taintedField, fieldDefs);
												}
												/*
												List<Stmt> fieldUses = fieldToUsesMap.get(((InstanceFieldRef) lhs).getField());
												for(Stmt fieldUse : fieldUses){
													if(stmtToNodeMap.containsKey(fieldUse)){
														APIGraphNode fieldUseNode = stmtToNodeMap.get(fieldUse);
														fieldUseNode.addPred(sNode);
														sNode.addSucc(fieldUseNode);
													}
												}
												*/
											}
										
										}
									}
								}
							}

							Iterator useIt = s.getUseBoxes().iterator();
							while (useIt.hasNext()) {
								ValueBox vBox = (ValueBox) useIt.next();
								if (vBox.getValue() instanceof InvokeExpr) {
									if (sourceMethods.contains(((InvokeExpr) vBox.getValue()).getMethod())) {

										UseWithScope sWS = new UseWithScope(s, s);
										if(!uses.containsKey(s)){
											Vector<Stmt> scopes = new Vector<Stmt>();
											scopes.add(s);
											uses.put(s, scopes);
											usesStack.push(sWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("use stack doesn't contain " + sWS.dump() + ". Push it.");
											sink = s;
										}
									}
								}
							}
						}
					}

					while(!usesStack.isEmpty()){

						UseWithScope useWS = usesStack.pop();
						if(MyConstants.DEBUG_INFO)
							System.out.println("POP from use stack: " + useWS.dump());

						if(hasEquivTable){

							if(MyConstants.DEBUG_INFO)
								System.out.println(sMethod + "has equivTable: " + equivTable);
							if(equivTable.containsKey(useWS.getUse())){

								List<Stmt> equivs = equivTable.get(useWS.getUse());

								if(MyConstants.DEBUG_INFO)
									System.out.println("EQUIV found: " + useWS.getUse() + "|" + equivs);

								for(Stmt equiv : equivs){
									UseWithScope equivWS = new UseWithScope(equiv, equiv);
									if (!uses.containsKey(equiv)) {
										uses.put(equiv, new Vector<Stmt>());
										usesStack.push(equivWS);
										if(MyConstants.DEBUG_INFO)
											System.out.println("use stack doesn't contain " + equivWS.dump() + ". Push it.");
									}
								}
							}
						}

						//use-def analysis
						Stmt s = useWS.getUse();
						Stmt sScope = useWS.getScopeEnd();
						
						if(s.containsInvokeExpr()){
							if(!s.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
								AddTags(s, API_TAG);
							}
						}
						
						if(s instanceof DefinitionStmt){
							//if(!s.containsInvokeExpr()){
								boolean usesConstant = false;
								List<ValueBox> checkConstUseBoxes = s.getUseBoxes();
								for(ValueBox ccVB : checkConstUseBoxes){
									if(ccVB.getValue() instanceof StringConstant){
										if(!((StringConstant)ccVB.getValue()).value.equals("")){
											usesConstant = true;
											break;
										}
									}
								}					
								if(usesConstant){
									AddTags(s, STRING_CONST_TAG);
								}
							//}
						}
						
						APIGraphNode sNode = CreateOrGetExistingNode(s, sMethod);;
						if(!methodToDDGMap.get(sMethod).contains(sNode)){
							methodToDDGMap.get(sMethod).add(sNode);
						}

						// for BBN, we only care about the this object on the SQL query
						if(s.toString().equals(flowSink) && sMethod.getSignature().equals(stmtMethod))
						{
							List<ValueBox> usesBoxes = s.getUseBoxes();
							Iterator usesIter = usesBoxes.iterator();
							while(usesIter.hasNext()){
								ValueBox usesBox = (ValueBox)usesIter.next();
								if(usesBox.getValue() instanceof Local){
									List<Unit> defs = mrd.getDefsOfAt((Local)usesBox.getValue(), s);
									for(Unit def : defs){
										
										APIGraphNode defNode = CreateOrGetExistingNode((Stmt)def, sMethod);;

										sNode.addPred(defNode);
										defNode.addSucc(sNode);
										if(!methodToDDGMap.get(sMethod).contains(defNode)){
											methodToDDGMap.get(sMethod).add(defNode);
										}
																				
										UseWithScope defofuseWS = new UseWithScope((Stmt)def, s);
										if(!uses.containsKey((Stmt)def)){
											Vector<Stmt> scopes = new Vector<Stmt>();
											scopes.add(s);
											uses.put((Stmt)def, scopes);
											usesStack.push(defofuseWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("use stack: " + defofuseWS.dump() + ". Push it.");
										}else{
											Vector<Stmt> scopes = uses.get((Stmt)def);
											if(!scopes.contains(s)){
												scopes.add(s);
												usesStack.push(defofuseWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("use stack: " + defofuseWS.dump() + ". Push it.");
											}
										}
									}
								}
							}
						}
					}//while(!usesStack.isEmpty()){

					Iterator i = uses.keySet().iterator();
					while (i.hasNext()) {
						Stmt s = (Stmt)i.next();
						//System.out.print(s + "|");

						AddTags(s, generalTaintTag);
						AddTags(s, taintTag);
						AddTags(s, functionPreservingTag);

						Iterator usesIt = s.getUseBoxes().iterator();
						while (usesIt.hasNext()) {
							ValueBox vbox = (ValueBox) usesIt.next();
							if (vbox.getValue() instanceof Local) {
								Local l = (Local) vbox.getValue();

								Iterator rDefsIt = mrd.getDefsOfAt(l, s).iterator();
								while (rDefsIt.hasNext()) {
									Stmt next = (Stmt) rDefsIt.next();
									if(!next.getTags().contains(taintTag)){
										AddTags(s, generalExtraDefTag);
										AddTags(s, extraDefTag);
									}
								}
							}
						}
						//System.out.println(s.getTags());
					}
					// System.out.println();

					if(MyConstants.DEBUG_INFO){
						System.out.println();
						System.out.println("method:" + sMethod.getSignature());
						System.out.println("dataflow for " + sink + ":");
					}
					Iterator printIt = body.getUnits().iterator();
					while(printIt.hasNext()){
						Stmt s = (Stmt)printIt.next();
						if(s.getTags().contains(taintTag) || s.getTags().contains(extraDefTag)){

							if(MyConstants.DEBUG_INFO){
								if(MyConstants.DEBUG_INFO){
									//System.out.println(s + "|" + s.getTags());
									dumpTaggedStmt(s);
								}
							}

							Vector<Integer> labels = new Vector<Integer>();

							for(Tag tag : s.getTags()){
								if(taintTagReverseMap.containsKey(tag)){
									Integer label = new Integer(((TaintTag)tag).getLabel());
									if(!labels.contains(label)){
										labels.add(label);
									}
								}else if(extraDefTagReverseMap.containsKey(tag)){
									Integer label = new Integer(((TaintTag)tag).getLabel());
									if(!labels.contains(label)){
										labels.add(label);
									}
								}								
							}

							List<ValueBox> vbs = s.getUseAndDefBoxes();
							Iterator iter = vbs.iterator();
							while(iter.hasNext()){
								ValueBox vb = (ValueBox)iter.next();
								if(vb.getValue() instanceof InstanceFieldRef){
									SootField instanceField = ((InstanceFieldRef)vb.getValue()).getField();

									if(instanceField.getDeclaringClass().isApplicationClass() == false){
										continue;
									}

									if(instanceField.getDeclaringClass().isPhantom()){
										continue;
									}

									//if(!instanceFields.contains(instanceField)){
									//	instanceFields.add(instanceField);
									//}
									////							
									if(!instanceFieldMap.containsKey(instanceField)){						

										Vector<Integer> taintSources = new Vector<Integer>();
										taintSources.addAll(labels);							
										instanceFieldMap.put(instanceField, taintSources);

									}else{

										Vector<Integer> taintSources = instanceFieldMap.get(instanceField);
										for(Integer label : labels){
											if(!taintSources.contains(label)){
												taintSources.add(label);
											}
										}
									}
									////

									LinkedHashMap<String, List<String>> taintSourceToField = new LinkedHashMap<String, List<String>>();
									List<String> fieldList = new ArrayList<String>();
									if(fieldList.contains(instanceField.getSignature())){
										fieldList.add(instanceField.getSignature());
									}								
									taintSourceToField.put(flowSink, fieldList);
									classToSourceToField.put(sMethod.getDeclaringClass().getName(), taintSourceToField);

								}else if(vb.getValue() instanceof StaticFieldRef){
									SootField staticField = ((StaticFieldRef)vb.getValue()).getField();

									if(staticField.getDeclaringClass().isApplicationClass() == false){
										continue;
									}

									if(staticField.getDeclaringClass().isPhantom()){
										continue;
									}

									//if(!staticFields.contains(staticField)){
									//	staticFields.add(staticField);
									//}
									///
									if(!staticFieldMap.containsKey(staticField)){						

										Vector<Integer> taintSources = new Vector<Integer>();
										taintSources.addAll(labels);
										staticFieldMap.put(staticField, taintSources);

									}else{

										Vector<Integer> taintSources = staticFieldMap.get(staticField);
										for(Integer label : labels){
											if(!taintSources.contains(label)){
												taintSources.add(label);
											}
										}
									}
									///

									LinkedHashMap<String, List<String>> taintSourceToField = new LinkedHashMap<String, List<String>>();
									List<String> fieldList = new ArrayList<String>();
									if(fieldList.contains(staticField.getSignature())){
										fieldList.add(staticField.getSignature());
									}								
									taintSourceToField.put(flowSink, fieldList);
									classToSourceToField.put(sMethod.getDeclaringClass().getName(), taintSourceToField);

								}else if(vb.getValue() instanceof Local){

									String varName = ((Local)vb.getValue()).getName();								
									LinkedHashMap<String, List<String>> taintSourceToVar = new LinkedHashMap<String, List<String>>();
									List<String> varList = new ArrayList<String>();
									if(varList.contains(varName)){
										varList.add(varName);
									}								
									taintSourceToVar.put(flowSink, varList);
									methodToSourceToVar.put(sMethod.getSignature(), taintSourceToVar);
								}
							}
						}
					}

					if(MyConstants.DEBUG_INFO){
						System.out.println("end dataflow for " + sink + "\n");
					}

				}//while(!worklist.isEmpty())
					
			}//end while(!fWorklist.isEmpty())

			//taintedFields.addAll(taintedFields);
			
			Set<SootField> fieldSet = fieldToDefsMap.keySet();
			Iterator<SootField> fieldIter = fieldSet.iterator();
			while(fieldIter.hasNext()){
				SootField field = fieldIter.next();
				List<Stmt> fieldDefs = fieldToDefsMap.get(field);
				if(fieldToUsesMap.containsKey(field)){
					List<Stmt> fieldUses = fieldToUsesMap.get(field);
					for(Stmt fieldDef : fieldDefs){
						
						if(fieldDef instanceof DefinitionStmt){
							Value lhs = ((DefinitionStmt)fieldDef).getLeftOp();
							if(lhs instanceof StaticFieldRef){						
								APIGraphNode fieldDefNode = stmtToNodeMap.get(fieldDef);								
								for(Stmt fieldUse : fieldUses){
									APIGraphNode fieldUseNode = stmtToNodeMap.get(fieldUse);
									fieldDefNode.addSucc(fieldUseNode);
									fieldUseNode.addPred(fieldDefNode);
								}
							}else if(lhs instanceof InstanceFieldRef){
								Value defBase = ((InstanceFieldRef) lhs).getBase();
								APIGraphNode fieldDefNode = stmtToNodeMap.get(fieldDef);								
								for(Stmt fieldUse : fieldUses){
									if(fieldUse instanceof DefinitionStmt){
										Value rhs = ((DefinitionStmt) fieldUse).getRightOp();
										if(rhs instanceof InstanceFieldRef){
											
											Value useBase = ((InstanceFieldRef) rhs).getBase();
											if(pta.reachingObjects((Local)defBase).hasNonEmptyIntersection(pta.reachingObjects((Local)useBase))
													|| pta.reachingObjects((Local)defBase).isEmpty()){
									
												APIGraphNode fieldUseNode = stmtToNodeMap.get(fieldUse);
												fieldDefNode.addSucc(fieldUseNode);
												fieldUseNode.addPred(fieldDefNode);
											}
										}
									}
								}
							}
						}
					}
				}
			}

			Set<SootField> instanceKeySet = instanceFieldMap.keySet();
			Iterator<SootField> instanceIter = instanceKeySet.iterator();
			while(instanceIter.hasNext()){
				SootField f = instanceIter.next();
				Vector<Integer> newLabels = instanceFieldMap.get(f);

				if(usedInstanceFieldMap.containsKey(f)){
					Vector<Integer> oldLabels = usedInstanceFieldMap.get(f);
					for(Integer label : newLabels){
						if(!oldLabels.contains(label)){
							oldLabels.add(label);
						}
					}
				}else{
					Vector<Integer> labels = new Vector<Integer>();
					labels.addAll(newLabels);
					usedInstanceFieldMap.put(f, labels);
				}
			}

			Set<SootField> staticKeySet = staticFieldMap.keySet();
			Iterator<SootField> staticIter = staticKeySet.iterator();
			while(staticIter.hasNext()){
				SootField f = staticIter.next();
				Vector<Integer> newLabels = staticFieldMap.get(f);

				if(usedStaticFieldMap.containsKey(f)){
					Vector<Integer> oldLabels = usedStaticFieldMap.get(f);
					for(Integer label : newLabels){
						if(!oldLabels.contains(label)){
							oldLabels.add(label);
						}
					}
				}else{
					Vector<Integer> labels = new Vector<Integer>();
					labels.addAll(newLabels);
					usedStaticFieldMap.put(f, labels);
				}
			}
			
			/*
			Set<Stmt> sKeySet = this.stmtToNodeMap.keySet();
			Iterator<Stmt> sIter = sKeySet.iterator();
			while(sIter.hasNext()){
				Stmt s = sIter.next();
				APIGraphNode node = this.stmtToNodeMap.get(s);
				this.apiDDGGraph.add(node);
			}
			printDDGEdgeList(this.apiDDGGraph);
			*/
			
			//if this DFA is for SQL analysis, do the following
			if(AnalyzerMain.dataFlowForSQLAnalysis == true)
			{
				Set<Stmt> sKeySet = this.stmtToNodeMap.keySet();
				Iterator<Stmt> sIter = sKeySet.iterator();
				while(sIter.hasNext()){
					Stmt s = sIter.next();
					APIGraphNode node = this.stmtToNodeMap.get(s);
					this.apiDDGGraph.add(node);
				}
				
				//RemoveNonImportantNodes();
				//AnalyzerMain.AddConditionSigToDDGMapping(conditionSig, this.apiDDGGraph);
				if(MyConstants.DEBUG_SQL)
				{
					System.err.println("SQL query is: " + flowSink);
					printDDGEdgeList(this.apiDDGGraph);
				}
					
				apiDDGGraph.clear();
				stmtToNodeMap.clear();
				propagationHistory.clear();
				clearDataStructures();
			}
			

		}//end foreach(flowsink)	
	}
	
	private APIGraphNode CreateOrGetExistingNode(Stmt s, SootMethod sMethod)
	{
		APIGraphNode sNode = null;
		if(!stmtToNodeMap.containsKey(s)){
			sNode = new APIGraphNode(s, sMethod);
			nodeCount++;
			if(isAndroidAPICall(s)){
				apiNodeCount++;
			}
			stmtToNodeMap.put(s, sNode);						
		}else{
			sNode = stmtToNodeMap.get(s);
		}
		return sNode;
	}
	
	private boolean isAndroidAPICall(Stmt s){
		if(s.containsInvokeExpr()){
			if(!s.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
				
				if(s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("java.io.") ||
						s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("org.apache.http.client.") ||
						s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("org.apache.http.impl.client.") ||
						s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("java.net.")){
					return true;
					
				}else if(!s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("java.") &&
						!s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("javax.") &&
						!s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("org.")){
					return true;
				}
			}
		}
		return false;
	}
	
	
	private void AddTags(Stmt s, TaintTag tag)
	{
		if(!s.getTags().contains(tag)){
			s.addTag(tag);
		}
	}


	private Vector<Integer> propagate(SootMethod sMethod, Vector<Integer> taintIndexes, String flowSink, Stmt from, SootMethod fromMethod)
	{
		if(callString.contains(sMethod)){
			if(MyConstants.DEBUG_INFO)
				System.out.println("RECURSIVE call found, return null");
			return null;
		}
		
		if(callString.size() > MyConstants.DFA_DEPTH){
			if(MyConstants.DEBUG_INFO)
				System.out.println("exceeds defined DEPTH, return null");
			return null;
		}
		
		if(!methodToDDGMap.containsKey(sMethod)){
			List<APIGraphNode> apiGraph = new ArrayList<APIGraphNode>();
			methodToDDGMap.put(sMethod, apiGraph);
		}

		TaintTag taintTag = taintTagMap.get(flowSink);
		TaintTag extraDefTag = extraDefTagMap.get(flowSink);

		callString.push(sMethod);

		if(MyConstants.DEBUG_INFO){
			System.out.println("step into method: " + sMethod + "|taintIndexes: " + taintIndexes + "\n");
		}

		if(sMethod.getSignature().equals("<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>")){
			System.out.println("REFLECTION on path");			
		}else if(sMethod.isNative() && sMethod.getDeclaringClass().isApplicationClass()){
			System.out.println("NATIVE on path");			
		}

		LinkedHashMap<SootField, Vector<Integer>> instanceFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();
		LinkedHashMap<SootField, Vector<Integer>> staticFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();

		List<SootField> taintedFields = new ArrayList<SootField>();
		taintedFields.addAll(taintedFieldsInCaller);
		taintedFieldsInCaller.clear();

		Vector<Integer> taintResult = new Vector<Integer>();

		//function summaries would be inserted here
		if(sMethod.getDeclaringClass().isApplicationClass()==false || (!sMethod.isConcrete())){

			if(MyConstants.DEBUG_INFO)
				System.out.println(sMethod + " is not declared in an application class");

			if(MyConstants.DEBUG_INFO)
				System.out.println("CHECK if " + sMethod.getSignature() + " has a function summary?");
			LinkedHashMap<Integer, List<Integer>> result = AndroidFunctionSummary.lookupFunctionSummary(sMethod.getSignature());
			if(result==null){

				if(MyConstants.DEBUG_INFO)
					System.out.println(sMethod.getSignature() + " has NO function summary");
				if(sMethod.getReturnType().equals(VoidType.v())){
					
					if(MyConstants.DEBUG_INFO)
						System.out.println(sMethod.getSignature() + " has NO return value as well");
					callString.pop();
					return null;
					
				}else{
					
					if(MyConstants.DEBUG_INFO)
						System.out.println(sMethod.getSignature() + " luckily has a RETURN value");
					if(taintIndexes.contains(MyConstants.returnValue)){
						if(!sMethod.isStatic()){
							if(MyConstants.TO_TAINT_THIS_OBJECT)
								taintResult.add(new Integer(MyConstants.thisObject));
						}
						int paraCount = sMethod.getParameterCount();
						for(int count = 0; count < paraCount; count++){
							taintResult.add(new Integer(count));
						}
					}
					callString.pop();
					return taintResult;
				}

			}else{

				if(MyConstants.DEBUG_INFO)
					System.out.println(sMethod.getSignature() + " HAS function SUMMARY");
				Set<Integer> sources = result.keySet();
				Iterator iterSources = sources.iterator();
				while(iterSources.hasNext()){
					Integer source = (Integer)iterSources.next();
					List<Integer> dests = result.get(source);
					for(Integer dest:dests){
						if(taintIndexes.contains(dest)){						
							if(!taintResult.contains(source)){
								taintResult.add(source);
							}
						}
					}
				}

				if(MyConstants.DEBUG_INFO)
					System.out.println("function summary tells which ones are tainted: " + taintResult);
				callString.pop();
				return taintResult;
			}


		}

		//System.out.println("step into method: " + sMethod + "|taintIndexes: " + taintIndexes + "\n");

		boolean hasEquivTable = false;

		LinkedHashMap<Stmt, List<Stmt>> equivTable = null;
		if(methodToEquiv.containsKey(sMethod)){
			hasEquivTable = true;
			equivTable = methodToEquiv.get(sMethod);
		}

		Stmt sink = null;
		List<Local> localSinkVars = new ArrayList<Local>();
		LinkedHashMap<Stmt, Vector<Stmt>> uses = new LinkedHashMap<Stmt, Vector<Stmt>>();	
		Stack<UseWithScope> usesStack = new Stack<UseWithScope>();
		Vector<UseWithScope> taintedRefUses = new Vector<UseWithScope>();

		JimpleBody body = (JimpleBody) sMethod.retrieveActiveBody();

		ExceptionalUnitGraph eug = new ExceptionalUnitGraph(body);
		MyReachingDefinition mrd = new MyReachingDefinition(eug);
		
		{
			Iterator it = body.getUnits().iterator();
			while (it.hasNext()) {
				Stmt s = (Stmt) it.next();
				if (s instanceof IdentityStmt) {
					List<ValueBox> vBoxes = ((IdentityStmt) s).getUseBoxes();
					Iterator iBox = vBoxes.iterator();
					while (iBox.hasNext()) {
						ValueBox vBox = (ValueBox) iBox.next();
						if (vBox.getValue() instanceof ParameterRef) {

							if (taintIndexes.contains(new Integer(((ParameterRef) vBox.getValue()).getIndex()))) {
								Value lhs = ((IdentityStmt)s).getLeftOp();
								
								if(lhs instanceof Local){
									if(!localSinkVars.contains((Local)lhs)){
										localSinkVars.add((Local)lhs);
									}
								}
							}

						}else if(vBox.getValue() instanceof ThisRef){
							if(taintIndexes.contains(new Integer(MyConstants.thisObject))){

								Value lhs = ((IdentityStmt)s).getLeftOp();
								
								if(lhs instanceof Local){
									if(!localSinkVars.contains((Local)lhs)){
										localSinkVars.add((Local)lhs);
									}
								}
							}
						}
					}
				}else if(s instanceof ReturnStmt){					
					
					if(taintIndexes.contains(new Integer(MyConstants.returnValue))){
						
						APIGraphNode returnNode = CreateOrGetExistingNode(s, sMethod);;

						if(!methodToDDGMap.get(sMethod).contains(returnNode)){
							methodToDDGMap.get(sMethod).add(returnNode);
						}
						
						APIGraphNode fromNode = CreateOrGetExistingNode(from, fromMethod);;

						if(!methodToDDGMap.get(fromMethod).contains(fromNode)){
							methodToDDGMap.get(fromMethod).add(fromNode);
						}
						
						fromNode.addPred(returnNode);
						returnNode.addSucc(fromNode);
						
						Value op = ((ReturnStmt)s).getOp();
						if(op instanceof Local){
							UseWithScope sWS = new UseWithScope(s, s);
							if (!uses.containsKey(s)) {
								uses.put(s, new Vector<Stmt>());
								usesStack.push(sWS);
								if(MyConstants.DEBUG_INFO)
									System.out.println("use stack doesn't contain return stmt " + sWS.dump() + ". Push it.");
								sink = s;
							}
						}					
					}
				}
			}
					
			////FIX HERE!
			if(MyConstants.CONSIDER_REDEFINE && !localSinkVars.isEmpty()){
				for(Local localSinkVar : localSinkVars){
					Iterator it2 = body.getUnits().iterator();
					while (it2.hasNext()) {
						Stmt stmt = (Stmt) it2.next();
						
						if(stmt.containsInvokeExpr()){
							if(!stmt.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
								AddTags(stmt, API_TAG);

							}
						}
						
						if(stmt instanceof DefinitionStmt){
							//if(!stmt.containsInvokeExpr()){
								boolean usesConstant = false;
								List<ValueBox> checkConstUseBoxes = stmt.getUseBoxes();
								for(ValueBox ccVB : checkConstUseBoxes){
									if(ccVB.getValue() instanceof StringConstant){
										if(!((StringConstant)ccVB.getValue()).value.equals("")){
											usesConstant = true;
											break;
										}
									}
								}					
								if(usesConstant){
									AddTags(stmt, STRING_CONST_TAG);
								}
							//}
						}
						
						APIGraphNode stmtNode = CreateOrGetExistingNode(stmt, sMethod);;

						if(!methodToDDGMap.get(sMethod).contains(stmtNode)){
							methodToDDGMap.get(sMethod).add(stmtNode);
						}
												
						if(stmt instanceof InvokeStmt){
							
							Vector<Integer> taintVector = new Vector<Integer>();
							// System.out.println(vbox2.getValue());
							InvokeExpr invokeEx = stmt.getInvokeExpr();
							int argCount = invokeEx.getArgCount();
							for (int i = 0; i < argCount; i++) {
								if (invokeEx.getArg(i) == localSinkVar) {
									taintVector.add(new Integer(i));
								}												
							}
							
							//for instance invoke, consider this reference too.
							if(invokeEx instanceof InstanceInvokeExpr){
								if(((InstanceInvokeExpr) invokeEx).getBase() == localSinkVar){
									if(MyConstants.TO_TAINT_THIS_OBJECT)
										taintVector.add(new Integer(MyConstants.thisObject));
								}								
							}
							
							Iterator targets = null;
							if(stmt.getInvokeExpr().getMethod().isConcrete()){
								if(MyConstants.DEBUG_INFO)
									System.out.println(stmt + " calls CONCRETE method: " + stmt.getInvokeExpr().getMethod());
								List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
								defaultTargets.add(stmt.getInvokeExpr().getMethod());
								targets = defaultTargets.iterator();
							}else{	
								if(MyConstants.DEBUG_INFO)
									System.out.println(stmt + " calls NON-CONCRETE method: " + stmt.getInvokeExpr().getMethod());
								targets = new Targets(this.cg.edgesOutOf(stmt));										

								if(!targets.hasNext()){
									if(MyConstants.DEBUG_INFO)
										System.out.println(stmt + " does NOT have a target. add a DEFAULT one");
									List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
									defaultTargets.add(stmt.getInvokeExpr().getMethod());
									targets = defaultTargets.iterator();
								}
							}
							
							if(targets==null){
								continue;
							}

							while (targets.hasNext()) {
								SootMethod target = (SootMethod) targets.next();

								boolean noNewTaint = true;
								if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+stmt.toString())){														
									noNewTaint = false;
									List<Integer> sinks = new ArrayList<Integer>();
									sinks.addAll(taintVector);
									propagationHistory.put(sMethod.getSignature()+"|"+stmt.toString(), sinks);
								}else{
									List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+stmt.toString());

									for(Integer taint : taintVector){
										if(!sinks.contains(taint)){
											noNewTaint = false;
											sinks.add(taint);
										}
									}														
								}

								if(noNewTaint){
									break;
								}	

								if(MyConstants.DEBUG_INFO){
									System.out.println("PROPAGATING from METHOD: " + sMethod);
									System.out.println("PROPAGATING from STATEMENT: " + stmt);
								}
								taintedFieldsInCaller.addAll(taintedFields);
								Vector<Integer> tainted = propagate(target, taintVector, flowSink, stmt, sMethod);
								for(SootField sf : taintedFieldsInCallee){
									if(!taintedFields.contains(sf)){
										taintedFields.add(sf);
									}
								}
								taintedFieldsInCallee.clear();

								if(MyConstants.DEBUG_INFO){
									System.out.println(stmt + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
								}
								if ((tainted!=null) && (!tainted.isEmpty())) {
									AddTags(stmt, functionPreservingTag);

									for(Integer i : tainted){
										int index = i.intValue();

										if(index == MyConstants.thisObject){
											if(stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
												Value taintedThisRef = ((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase();

												if(taintedThisRef instanceof Local){
													List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, stmt);

													for(Unit defn : defs0){
														
														APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

														stmtNode.addPred(defNode);
														defNode.addSucc(stmtNode);
														
														if(!methodToDDGMap.get(sMethod).contains(defNode)){
															methodToDDGMap.get(sMethod).add(defNode);
														}

														UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
														if(!uses.containsKey(defn)){
															Vector<Stmt> scopes = new Vector<Stmt>();
															scopes.add(stmt);
															uses.put((Stmt)defn, scopes);
															usesStack.push(defnWS);
															if(MyConstants.DEBUG_INFO)
																System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
														}else if(!(uses.get(defn).contains(stmt))){
															uses.get(defn).add(stmt);
															usesStack.push(defnWS);
															if(MyConstants.DEBUG_INFO)
																System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
														}
													}
												}
																								
											}														 

										}else if(index >= 0){

											Value taintedArg = stmt.getInvokeExpr().getArg(index);
											
											if(taintedArg instanceof Local){
												List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, stmt);

												for(Unit defn : defs0){
													
													APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

													stmtNode.addPred(defNode);
													defNode.addSucc(stmtNode);
													
													if(!methodToDDGMap.get(sMethod).contains(defNode)){
														methodToDDGMap.get(sMethod).add(defNode);
													}

													UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
													if(!uses.containsKey(defn)){
														Vector<Stmt> scopes = new Vector<Stmt>();
														scopes.add(stmt);
														uses.put((Stmt)defn, scopes);
														usesStack.push(defnWS);
														if(MyConstants.DEBUG_INFO)
															System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
													}else if(!(uses.get(defn).contains(stmt))){
														uses.get(defn).add(stmt);
														usesStack.push(defnWS);
														if(MyConstants.DEBUG_INFO)
															System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
													}
												}
											}
										}
									}
								}
							}
						}else if(stmt instanceof DefinitionStmt){
							Value rhs = ((DefinitionStmt) stmt).getRightOp();
							if(rhs instanceof InvokeExpr){
							
								Vector<Integer> taintVector = new Vector<Integer>();
								// System.out.println(vbox2.getValue());
								InvokeExpr invokeEx = stmt.getInvokeExpr();
								int argCount = invokeEx.getArgCount();
								for (int i = 0; i < argCount; i++) {
									if (invokeEx.getArg(i) == localSinkVar) {
										taintVector.add(new Integer(i));
									}												
								}
								
								//for instance invoke, consider this reference too.
								if(invokeEx instanceof InstanceInvokeExpr){
									if(((InstanceInvokeExpr) invokeEx).getBase() == localSinkVar){
										if(MyConstants.TO_TAINT_THIS_OBJECT)
											taintVector.add(new Integer(MyConstants.thisObject));
									}								
								}
								
								Iterator targets = null;
								if(stmt.getInvokeExpr().getMethod().isConcrete()){
									if(MyConstants.DEBUG_INFO)
										System.out.println(stmt + " calls CONCRETE method: " + stmt.getInvokeExpr().getMethod());
									List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
									defaultTargets.add(stmt.getInvokeExpr().getMethod());
									targets = defaultTargets.iterator();
								}else{	
									if(MyConstants.DEBUG_INFO)
										System.out.println(stmt + " calls NON-CONCRETE method: " + stmt.getInvokeExpr().getMethod());
									targets = new Targets(this.cg.edgesOutOf(stmt));										

									if(!targets.hasNext()){
										if(MyConstants.DEBUG_INFO)
											System.out.println(stmt + " does NOT have a target. add a DEFAULT one");
										List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
										defaultTargets.add(stmt.getInvokeExpr().getMethod());
										targets = defaultTargets.iterator();
									}
								}
								
								if(targets==null){
									continue;
								}

								while (targets.hasNext()) {
									SootMethod target = (SootMethod) targets.next();

									boolean noNewTaint = true;
									if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+stmt.toString())){														
										noNewTaint = false;
										List<Integer> sinks = new ArrayList<Integer>();
										sinks.addAll(taintVector);
										propagationHistory.put(sMethod.getSignature()+"|"+stmt.toString(), sinks);
									}else{
										List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+stmt.toString());

										for(Integer taint : taintVector){
											if(!sinks.contains(taint)){
												noNewTaint = false;
												sinks.add(taint);
											}
										}														
									}

									if(noNewTaint){
										break;
									}	

									if(MyConstants.DEBUG_INFO){
										System.out.println("PROPAGATING from METHOD: " + sMethod);
										System.out.println("PROPAGATING from STATEMENT: " + stmt);
									}
									taintedFieldsInCaller.addAll(taintedFields);
									Vector<Integer> tainted = propagate(target, taintVector, flowSink, stmt, sMethod);
									for(SootField sf : taintedFieldsInCallee){
										if(!taintedFields.contains(sf)){
											taintedFields.add(sf);
										}
									}
									taintedFieldsInCallee.clear();

									if(MyConstants.DEBUG_INFO){
										System.out.println(stmt + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
									}
									if ((tainted!=null) && (!tainted.isEmpty())) {
										
										AddTags(stmt, functionPreservingTag);

										for(Integer i : tainted){
											int index = i.intValue();

											if(index == MyConstants.thisObject){
												if(stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
													Value taintedThisRef = ((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase();

													if(taintedThisRef instanceof Local){
														List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, stmt);

														for(Unit defn : defs0){
															
															APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

															stmtNode.addPred(defNode);
															defNode.addSucc(stmtNode);
															
															if(!methodToDDGMap.get(sMethod).contains(defNode)){
																methodToDDGMap.get(sMethod).add(defNode);
															}

															UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
															if(!uses.containsKey(defn)){
																Vector<Stmt> scopes = new Vector<Stmt>();
																scopes.add(stmt);
																uses.put((Stmt)defn, scopes);
																usesStack.push(defnWS);
																if(MyConstants.DEBUG_INFO)
																	System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
															}else if(!(uses.get(defn).contains(stmt))){
																uses.get(defn).add(stmt);
																usesStack.push(defnWS);
																if(MyConstants.DEBUG_INFO)
																	System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
															}
														}
													}
																									
												}														 

											}else if(index >= 0){

												Value taintedArg = stmt.getInvokeExpr().getArg(index);
												
												if(taintedArg instanceof Local){
													List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, stmt);

													for(Unit defn : defs0){
														
														APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

														stmtNode.addPred(defNode);
														defNode.addSucc(stmtNode);
														
														if(!methodToDDGMap.get(sMethod).contains(defNode)){
															methodToDDGMap.get(sMethod).add(defNode);
														}

														UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
														if(!uses.containsKey(defn)){
															Vector<Stmt> scopes = new Vector<Stmt>();
															scopes.add(stmt);
															uses.put((Stmt)defn, scopes);
															usesStack.push(defnWS);
															if(MyConstants.DEBUG_INFO)
																System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
														}else if(!(uses.get(defn).contains(stmt))){
															uses.get(defn).add(stmt);
															usesStack.push(defnWS);
															if(MyConstants.DEBUG_INFO)
																System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
														}
													}
												}
											}
										}
									}
								}
							}
							
						}
					}
				}
			}
		}

		if (uses.isEmpty()) {
			callString.pop();
			return null;
		}

		while(!usesStack.isEmpty()){

			UseWithScope useWS = usesStack.pop();
			if(MyConstants.DEBUG_INFO)
				System.out.println("POP from use stack: " + useWS.dump());

			if(hasEquivTable){

				if(MyConstants.DEBUG_INFO)
					System.out.println(sMethod + "has equivTable: " + equivTable);
				if(equivTable.containsKey(useWS.getUse())){

					List<Stmt> equivs = equivTable.get(useWS.getUse());

					if(MyConstants.DEBUG_INFO)
						System.out.println("EQUIV found: " + useWS.getUse() + "|" + equivs);

					for(Stmt equiv : equivs){
						UseWithScope equivWS = new UseWithScope(equiv, equiv);
						if (!uses.containsKey(equiv)) {
							uses.put(equiv, new Vector<Stmt>());
							usesStack.push(equivWS);
							if(MyConstants.DEBUG_INFO)
								System.out.println("use stack doesn't contain " + equivWS.dump() + ". Push it.");
						}
					}
				}
			}

			//use-def analysis
			Stmt s = useWS.getUse();
			Stmt sScope = useWS.getScopeEnd();
			
			if(s.containsInvokeExpr()){
				if(!s.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
					AddTags(s, API_TAG);
				}
			}
			
			if(s instanceof DefinitionStmt){
				//if(!s.containsInvokeExpr()){
					boolean usesConstant = false;
					List<ValueBox> checkConstUseBoxes = s.getUseBoxes();
					for(ValueBox ccVB : checkConstUseBoxes){
						if(ccVB.getValue() instanceof StringConstant){
							if(!((StringConstant)ccVB.getValue()).value.equals("")){
								usesConstant = true;
								break;
							}
						}
					}					
					if(usesConstant){
						AddTags(s, STRING_CONST_TAG);
					}
				//}
			}
			
			APIGraphNode sNode = CreateOrGetExistingNode(s, sMethod);;

			if(!methodToDDGMap.get(sMethod).contains(sNode)){
				methodToDDGMap.get(sMethod).add(sNode);
			}

			if (s instanceof InvokeStmt) {

				if(s.getInvokeExpr().getMethod().getSignature().equals(flowSink)){
					List<ValueBox> usesBoxes = s.getUseBoxes();
					Iterator usesIter = usesBoxes.iterator();
					while(usesIter.hasNext()){
						ValueBox usesBox = (ValueBox)usesIter.next();
						if(usesBox.getValue() instanceof Local){
							List<Unit> defs = mrd.getDefsOfAt((Local)usesBox.getValue(), s);
							for(Unit def : defs){
								
								APIGraphNode defNode = CreateOrGetExistingNode((Stmt)def, sMethod);;

								sNode.addPred(defNode);
								defNode.addSucc(sNode);
								if(!methodToDDGMap.get(sMethod).contains(defNode)){
									methodToDDGMap.get(sMethod).add(defNode);
								}
								
								UseWithScope defofuseWS = new UseWithScope((Stmt)def, s);
								if(!uses.containsKey((Stmt)def)){
									Vector<Stmt> scopes = new Vector<Stmt>();
									scopes.add(s);
									uses.put((Stmt)def, scopes);
									usesStack.push(defofuseWS);
								}else{
									Vector<Stmt> scopes = uses.get((Stmt)def);
									if(!scopes.contains(s)){
										scopes.add(s);
										usesStack.push(defofuseWS);
									}
								}
							}
						}
					}
				}											

			} else {

				/*
				boolean isInvoke = false;

				Iterator iUse = s.getUseBoxes().iterator();
				while (iUse.hasNext()) {
					ValueBox vB = (ValueBox) iUse.next();
					if (vB.getValue() instanceof InvokeExpr) {
						isInvoke = true;
					}
				}
				*/
				boolean isInvoke = s.containsInvokeExpr();

				//rhs is invoke, lhs is ret
				if (isInvoke) {

					if(s.getInvokeExpr().getMethod().getSignature().equals(flowSink)){

					}
					
					if(s instanceof DefinitionStmt){
						Value lhs = ((DefinitionStmt) s).getLeftOp();
						
						if(MyConstants.CONSIDER_REDEFINE && lhs.getType() instanceof RefLikeType){		
							
							if(MyConstants.DEBUG_INFO)
								System.out.println("looking for redefine:" + s);
							
							Iterator itForRedefine = body.getUnits().iterator();
							while (itForRedefine.hasNext()) {
								Stmt stmt = (Stmt) itForRedefine.next();
								
								if(!isInScope(eug, stmt, sScope)){
									if(MyConstants.DEBUG_INFO)
										System.out.println(stmt + " is NOT in scope[<--" + sScope + "]");
									continue;
								}
								
								boolean isStmtUsingS = false;
								List<ValueBox> useBoxesofStmt = stmt.getUseBoxes();
								for(ValueBox useBox : useBoxesofStmt){
									if(useBox.getValue() instanceof Local){
										if(mrd.getDefsOfAt((Local)(useBox.getValue()), stmt).contains(s)){
											isStmtUsingS = true;
											break;
										}
									}
								}
								
								if(isStmtUsingS){
									if(MyConstants.DEBUG_INFO)
										System.out.println(stmt + " IS using " + s);
									
									if(stmt.containsInvokeExpr()){
										if(!stmt.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
											AddTags(s, API_TAG);
										}
									}					
									
									if(stmt instanceof DefinitionStmt){
										//if(!stmt.containsInvokeExpr()){
											boolean usesConstant = false;
											List<ValueBox> checkConstUseBoxes = stmt.getUseBoxes();
											for(ValueBox ccVB : checkConstUseBoxes){
												if(ccVB.getValue() instanceof StringConstant){
													if(!((StringConstant)ccVB.getValue()).value.equals("")){
														usesConstant = true;
														break;
													}
												}
											}					
											if(usesConstant){
												AddTags(s, STRING_CONST_TAG);
											}
										//}
									}
									
									APIGraphNode stmtNode = CreateOrGetExistingNode(stmt, sMethod);;

									if(!methodToDDGMap.get(sMethod).contains(stmtNode)){
										methodToDDGMap.get(sMethod).add(stmtNode);
									}
									
									APIGraphNode sScopeNode = CreateOrGetExistingNode(sScope, sMethod);;

									if(!methodToDDGMap.get(sMethod).contains(sScopeNode)){
										methodToDDGMap.get(sMethod).add(sScopeNode);
									}
									
									sNode.removeSucc(sScopeNode);
									sScopeNode.removePred(sNode);
									
									sNode.addSucc(stmtNode);
									stmtNode.addPred(sNode);
									
									stmtNode.addSucc(sScopeNode);
									sScopeNode.addPred(stmtNode);
									
									if(stmt instanceof InvokeStmt){
										
										Vector<Integer> taintVector = new Vector<Integer>();

										Iterator defIt2 = s.getDefBoxes().iterator();
										while (defIt2.hasNext()) {
											ValueBox vbox2 = (ValueBox) defIt2.next();
											if (vbox2.getValue() instanceof Local) {
												// System.out.println(vbox2.getValue());
												InvokeExpr invokeEx = stmt.getInvokeExpr();
												int argCount = invokeEx.getArgCount();
												for (int i = 0; i < argCount; i++) {
													if (invokeEx.getArg(i) == vbox2.getValue()) {
														taintVector.add(new Integer(i));
													}												
												}

												//for instance invoke, consider this reference too.
												if(invokeEx instanceof InstanceInvokeExpr){
													if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

														/*
														//special invoke doesn't count
														if(invokeEx instanceof SpecialInvokeExpr){
															if(rhs instanceof NewExpr){
																continue;
															}																	
														}
														*/
														if(MyConstants.TO_TAINT_THIS_OBJECT)
															taintVector.add(new Integer(MyConstants.thisObject));
													}
												}
											}
										}
										
										Iterator targets = null;
										if(stmt.getInvokeExpr().getMethod().isConcrete()){
											if(MyConstants.DEBUG_INFO)
												System.out.println(stmt + " calls CONCRETE method: " + stmt.getInvokeExpr().getMethod());
											List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
											defaultTargets.add(stmt.getInvokeExpr().getMethod());
											targets = defaultTargets.iterator();
										}else{	
											if(MyConstants.DEBUG_INFO)
												System.out.println(stmt + " calls NON-CONCRETE method: " + stmt.getInvokeExpr().getMethod());
											targets = new Targets(this.cg.edgesOutOf(stmt));										

											if(!targets.hasNext()){
												if(MyConstants.DEBUG_INFO)
													System.out.println(stmt + " does NOT have a target. add a DEFAULT one");
												List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
												defaultTargets.add(stmt.getInvokeExpr().getMethod());
												targets = defaultTargets.iterator();
											}
										}
										
										if(targets==null){
											continue;
										}

										while (targets.hasNext()) {
											SootMethod target = (SootMethod) targets.next();

											boolean noNewTaint = true;
											if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+stmt.toString())){														
												noNewTaint = false;
												List<Integer> sinks = new ArrayList<Integer>();
												sinks.addAll(taintVector);
												propagationHistory.put(sMethod.getSignature()+"|"+stmt.toString(), sinks);
											}else{
												List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+stmt.toString());

												for(Integer taint : taintVector){
													if(!sinks.contains(taint)){
														noNewTaint = false;
														sinks.add(taint);
													}
												}														
											}

											if(noNewTaint){
												break;
											}	

											if(MyConstants.DEBUG_INFO){
												System.out.println("PROPAGATING from METHOD: " + sMethod);
												System.out.println("PROPAGATING from STATEMENT: " + stmt);
											}
											taintedFieldsInCaller.addAll(taintedFields);
											Vector<Integer> tainted = propagate(target, taintVector, flowSink, stmt, sMethod);
											for(SootField sf : taintedFieldsInCallee){
												if(!taintedFields.contains(sf)){
													taintedFields.add(sf);
												}
											}
											taintedFieldsInCallee.clear();

											if(MyConstants.DEBUG_INFO){
												System.out.println(stmt + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
											}
											if ((tainted!=null) && (!tainted.isEmpty())) {
												AddTags(s, functionPreservingTag);

												for(Integer i : tainted){
													int index = i.intValue();

													if(index == MyConstants.thisObject){
														if(stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
															Value taintedThisRef = ((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase();

															if(taintedThisRef instanceof Local){
																List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, stmt);

																for(Unit defn : defs0){
																	
																	APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																	stmtNode.addPred(defNode);
																	defNode.addSucc(stmtNode);
																	if(!methodToDDGMap.get(sMethod).contains(defNode)){
																		methodToDDGMap.get(sMethod).add(defNode);
																	}

																	UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																	if(!uses.containsKey(defn)){
																		Vector<Stmt> scopes = new Vector<Stmt>();
																		scopes.add(stmt);
																		uses.put((Stmt)defn, scopes);
																		usesStack.push(defnWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																	}else if(!(uses.get(defn).contains(stmt))){
																		uses.get(defn).add(stmt);
																		usesStack.push(defnWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																	}
																}
															}
																											
														}														 

													}else if(index >= 0){

														Value taintedArg = stmt.getInvokeExpr().getArg(index);
														
														if(taintedArg instanceof Local){
															List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, stmt);

															for(Unit defn : defs0){
																
																APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																stmtNode.addPred(defNode);
																defNode.addSucc(stmtNode);
																if(!methodToDDGMap.get(sMethod).contains(defNode)){
																	methodToDDGMap.get(sMethod).add(defNode);
																}

																UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																if(!uses.containsKey(defn)){
																	Vector<Stmt> scopes = new Vector<Stmt>();
																	scopes.add(stmt);
																	uses.put((Stmt)defn, scopes);
																	usesStack.push(defnWS);
																	if(MyConstants.DEBUG_INFO)
																		System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																}else if(!(uses.get(defn).contains(stmt))){
																	uses.get(defn).add(stmt);
																	usesStack.push(defnWS);
																	if(MyConstants.DEBUG_INFO)
																		System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																}
															}
														}
													}
												}
											}

										}
										
										
									}else if(stmt instanceof DefinitionStmt){
										
										Value rhsInvoke = ((DefinitionStmt) stmt).getRightOp();
										if(rhsInvoke instanceof InvokeExpr){	
											
											Vector<Integer> taintVector = new Vector<Integer>();
											
											Iterator defIt2 = s.getDefBoxes().iterator();
											while (defIt2.hasNext()) {
												ValueBox vbox2 = (ValueBox) defIt2.next();
												if (vbox2.getValue() instanceof Local) {
													// System.out.println(vbox2.getValue());
													InvokeExpr invokeEx = stmt.getInvokeExpr();
													int argCount = invokeEx.getArgCount();
													for (int i = 0; i < argCount; i++) {
														if (invokeEx.getArg(i) == vbox2.getValue()) {
															taintVector.add(new Integer(i));
														}												
													}

													//for instance invoke, consider this reference too.
													if(invokeEx instanceof InstanceInvokeExpr){
														if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

															/*
															//special invoke doesn't count
															if(invokeEx instanceof SpecialInvokeExpr){
																if(rhs instanceof NewExpr){
																	continue;
																}																	
															}
															*/
															if(MyConstants.TO_TAINT_THIS_OBJECT)
																taintVector.add(new Integer(MyConstants.thisObject));
														}
													}
												}
											}	
											
											Iterator targets = null;
											if(stmt.getInvokeExpr().getMethod().isConcrete()){
												if(MyConstants.DEBUG_INFO)
													System.out.println(stmt + " calls CONCRETE method: " + stmt.getInvokeExpr().getMethod());
												List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
												defaultTargets.add(stmt.getInvokeExpr().getMethod());
												targets = defaultTargets.iterator();
											}else{	
												if(MyConstants.DEBUG_INFO)
													System.out.println(stmt + " calls NON-CONCRETE method: " + stmt.getInvokeExpr().getMethod());
												targets = new Targets(this.cg.edgesOutOf(stmt));										

												if(!targets.hasNext()){
													if(MyConstants.DEBUG_INFO)
														System.out.println(stmt + " does NOT have a target. add a DEFAULT one");
													List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
													defaultTargets.add(stmt.getInvokeExpr().getMethod());
													targets = defaultTargets.iterator();
												}
											}
											
											if(targets==null){
												continue;
											}

											while (targets.hasNext()) {
												SootMethod target = (SootMethod) targets.next();

												boolean noNewTaint = true;
												if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+stmt.toString())){														
													noNewTaint = false;
													List<Integer> sinks = new ArrayList<Integer>();
													sinks.addAll(taintVector);
													propagationHistory.put(sMethod.getSignature()+"|"+stmt.toString(), sinks);
												}else{
													List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+stmt.toString());

													for(Integer taint : taintVector){
														if(!sinks.contains(taint)){
															noNewTaint = false;
															sinks.add(taint);
														}
													}														
												}

												if(noNewTaint){
													break;
												}	

												if(MyConstants.DEBUG_INFO){
													System.out.println("PROPAGATING from METHOD: " + sMethod);
													System.out.println("PROPAGATING from STATEMENT: " + stmt);
												}
												taintedFieldsInCaller.addAll(taintedFields);
												Vector<Integer> tainted = propagate(target, taintVector, flowSink, stmt, sMethod);
												for(SootField sf : taintedFieldsInCallee){
													if(!taintedFields.contains(sf)){
														taintedFields.add(sf);
													}
												}
												taintedFieldsInCallee.clear();

												if(MyConstants.DEBUG_INFO){
													System.out.println(stmt + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
												}
												if ((tainted!=null) && (!tainted.isEmpty())) {
													AddTags(s, functionPreservingTag);

													for(Integer i : tainted){
														int index = i.intValue();

														if(index == MyConstants.thisObject){
															if(stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
																Value taintedThisRef = ((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase();

																if(taintedThisRef instanceof Local){
																	List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, stmt);

																	for(Unit defn : defs0){
																		
																		APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																		stmtNode.addPred(defNode);
																		defNode.addSucc(stmtNode);
																		if(!methodToDDGMap.get(sMethod).contains(defNode)){
																			methodToDDGMap.get(sMethod).add(defNode);
																		}

																		UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																		if(!uses.containsKey(defn)){
																			Vector<Stmt> scopes = new Vector<Stmt>();
																			scopes.add(stmt);
																			uses.put((Stmt)defn, scopes);
																			usesStack.push(defnWS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																		}else if(!(uses.get(defn).contains(stmt))){
																			uses.get(defn).add(stmt);
																			usesStack.push(defnWS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																		}
																	}
																}
																												
															}														 

														}else if(index >= 0){

															Value taintedArg = stmt.getInvokeExpr().getArg(index);
															
															if(taintedArg instanceof Local){
																List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, stmt);

																for(Unit defn : defs0){
																	
																	APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																	stmtNode.addPred(defNode);
																	defNode.addSucc(stmtNode);
																	if(!methodToDDGMap.get(sMethod).contains(defNode)){
																		methodToDDGMap.get(sMethod).add(defNode);
																	}

																	UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																	if(!uses.containsKey(defn)){
																		Vector<Stmt> scopes = new Vector<Stmt>();
																		scopes.add(stmt);
																		uses.put((Stmt)defn, scopes);
																		usesStack.push(defnWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																	}else if(!(uses.get(defn).contains(stmt))){
																		uses.get(defn).add(stmt);
																		usesStack.push(defnWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																	}
																}
															}
														}
													}
												}

											}
										}
									}										
									
								}//isStmtUsingS
							}
						}//if(lhs.getType() instanceof RefLikeType){
					}
					

					Vector<Integer> taintVector = new Vector<Integer>();
					taintVector.add(new Integer(MyConstants.returnValue));

					Iterator targets = null;
					if(s.getInvokeExpr().getMethod().isConcrete()){
						if(MyConstants.DEBUG_INFO)
							System.out.println(s + " calls CONCRETE method: " + s.getInvokeExpr().getMethod());
						List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
						defaultTargets.add(s.getInvokeExpr().getMethod());
						targets = defaultTargets.iterator();
					}else{	
						if(MyConstants.DEBUG_INFO)
							System.out.println(s + " calls NON-CONCRETE method: " + s.getInvokeExpr().getMethod());
						targets = new Targets(this.cg.edgesOutOf(s));										

						if(!targets.hasNext()){
							if(MyConstants.DEBUG_INFO)
								System.out.println(s + " does NOT have a target. add a DEFAULT one");
							List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
							defaultTargets.add(s.getInvokeExpr().getMethod());
							targets = defaultTargets.iterator();
						}
					}
					
					if(targets==null){
						continue;
					}

					while (targets.hasNext()) {
						SootMethod target = (SootMethod) targets.next();

						boolean noNewTaint = true;
						if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+s.toString())){														
							noNewTaint = false;
							List<Integer> sinks = new ArrayList<Integer>();
							sinks.addAll(taintVector);
							propagationHistory.put(sMethod.getSignature()+"|"+s.toString(), sinks);
						}else{
							List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+s.toString());

							for(Integer taint : taintVector){
								if(!sinks.contains(taint)){
									noNewTaint = false;
									sinks.add(taint);
								}
							}														
						}

						if(noNewTaint){
							break;
						}	

						if(MyConstants.DEBUG_INFO){
							System.out.println("PROPAGATING from METHOD: " + sMethod);
							System.out.println("PROPAGATING from STATEMENT: " + s);
						}
						taintedFieldsInCaller.addAll(taintedFields);
						Vector<Integer> tainted = propagate(target, taintVector, flowSink, s, sMethod);
						for(SootField sf : taintedFieldsInCallee){
							if(!taintedFields.contains(sf)){
								taintedFields.add(sf);
							}
						}
						taintedFieldsInCallee.clear();

						if(MyConstants.DEBUG_INFO){
							System.out.println(s + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
						}
						if ((tainted!=null) && (!tainted.isEmpty())) {
							AddTags(s, functionPreservingTag);

							for(Integer i : tainted){
								int index = i.intValue();

								if(index == MyConstants.thisObject){
									if(s.getInvokeExpr() instanceof InstanceInvokeExpr){
										Value taintedThisRef = ((InstanceInvokeExpr)s.getInvokeExpr()).getBase();

										if(taintedThisRef instanceof Local){
											List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, s);

											for(Unit defn : defs0){
												
												APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

												sNode.addPred(defNode);
												defNode.addSucc(sNode);
												if(!methodToDDGMap.get(sMethod).contains(defNode)){
													methodToDDGMap.get(sMethod).add(defNode);
												}

												UseWithScope defnWS = new UseWithScope((Stmt)defn, s);
												if(!uses.containsKey(defn)){
													Vector<Stmt> scopes = new Vector<Stmt>();
													scopes.add(s);
													uses.put((Stmt)defn, scopes);
													usesStack.push(defnWS);
													if(MyConstants.DEBUG_INFO)
														System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
												}else if(!(uses.get(defn).contains(s))){
													uses.get(defn).add(s);
													usesStack.push(defnWS);
													if(MyConstants.DEBUG_INFO)
														System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
												}
											}
										}
																						
									}														 

								}else if(index >= 0){

									Value taintedArg = s.getInvokeExpr().getArg(index);
									
									if(taintedArg instanceof Local){
										List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, s);

										for(Unit defn : defs0){
											
											APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

											sNode.addPred(defNode);
											defNode.addSucc(sNode);
											if(!methodToDDGMap.get(sMethod).contains(defNode)){
												methodToDDGMap.get(sMethod).add(defNode);
											}

											UseWithScope defnWS = new UseWithScope((Stmt)defn, s);
											if(!uses.containsKey(defn)){
												Vector<Stmt> scopes = new Vector<Stmt>();
												scopes.add(s);
												uses.put((Stmt)defn, scopes);
												usesStack.push(defnWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
											}else if(!(uses.get(defn).contains(s))){
												uses.get(defn).add(s);
												usesStack.push(defnWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
											}
										}
									}
								}
							}
						}

					}
					// invokes.add(s);
				}

				//pure definiton statement:
				else {
					
					if(s instanceof DefinitionStmt){
						Value rhs = ((DefinitionStmt) s).getRightOp();
						Value lhs = ((DefinitionStmt) s).getLeftOp();	
						
						//if lhs is a reference
						if(MyConstants.CONSIDER_REDEFINE && lhs.getType() instanceof RefLikeType){		
							//look for implicit redefine. e.g., a reference is passed into a method and thus its value is changed in callee.
							if(MyConstants.DEBUG_INFO)
								System.out.println("looking for redefine:" + s);
							
							Iterator itForRedefine = body.getUnits().iterator();
							while (itForRedefine.hasNext()) {
								Stmt stmt = (Stmt) itForRedefine.next();
								
								if(!isInScope(eug, stmt, sScope)){
									if(MyConstants.DEBUG_INFO)
										System.out.println(stmt + " is NOT in scope[<--" + sScope + "]");
									continue;
								}
								
								boolean isStmtUsingS = false;
								List<ValueBox> useBoxesofStmt = stmt.getUseBoxes();
								for(ValueBox useBox : useBoxesofStmt){
									if(useBox.getValue() instanceof Local){
										if(mrd.getDefsOfAt((Local)(useBox.getValue()), stmt).contains(s)){
											isStmtUsingS = true;
											break;
										}
									}
								}
								
								if(isStmtUsingS){
									if(MyConstants.DEBUG_INFO)
										System.out.println(stmt + " IS using " + s);
									
									if(stmt.containsInvokeExpr()){
										if(!stmt.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
											AddTags(stmt, API_TAG);
										}
									}					
									
									if(stmt instanceof DefinitionStmt){
										//if(!stmt.containsInvokeExpr()){
											boolean usesConstant = false;
											List<ValueBox> checkConstUseBoxes = stmt.getUseBoxes();
											for(ValueBox ccVB : checkConstUseBoxes){
												if(ccVB.getValue() instanceof StringConstant){
													if(!((StringConstant)ccVB.getValue()).value.equals("")){
														usesConstant = true;
														break;
													}
												}
											}					
											if(usesConstant){
												AddTags(stmt, STRING_CONST_TAG);
											}
										//}
									}
									
									APIGraphNode stmtNode = CreateOrGetExistingNode(stmt, sMethod);;

									if(!methodToDDGMap.get(sMethod).contains(stmtNode)){
										methodToDDGMap.get(sMethod).add(stmtNode);
									}
									
									APIGraphNode sScopeNode = CreateOrGetExistingNode(sScope, sMethod);;

									if(!methodToDDGMap.get(sMethod).contains(sScopeNode)){
										methodToDDGMap.get(sMethod).add(sScopeNode);
									}
									
									sNode.removeSucc(sScopeNode);
									sScopeNode.removePred(sNode);
									
									sNode.addSucc(stmtNode);
									stmtNode.addPred(sNode);
									
									stmtNode.addSucc(sScopeNode);
									sScopeNode.addPred(stmtNode);
									
									if(stmt instanceof InvokeStmt){
										
										Vector<Integer> taintVector = new Vector<Integer>();

										Iterator defIt2 = s.getDefBoxes().iterator();
										while (defIt2.hasNext()) {
											ValueBox vbox2 = (ValueBox) defIt2.next();
											if (vbox2.getValue() instanceof Local) {
												// System.out.println(vbox2.getValue());
												InvokeExpr invokeEx = stmt.getInvokeExpr();
												int argCount = invokeEx.getArgCount();
												for (int i = 0; i < argCount; i++) {
													if (invokeEx.getArg(i) == vbox2.getValue()) {
														taintVector.add(new Integer(i));
													}												
												}

												//for instance invoke, consider this reference too.
												if(invokeEx instanceof InstanceInvokeExpr){
													if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

														/*
														//special invoke doesn't count
														if(invokeEx instanceof SpecialInvokeExpr){
															if(rhs instanceof NewExpr){
																continue;
															}																	
														}
														*/
														if(MyConstants.TO_TAINT_THIS_OBJECT)
															taintVector.add(new Integer(MyConstants.thisObject));
													}
												}
											}
										}
										
										Iterator targets = null;
										if(stmt.getInvokeExpr().getMethod().isConcrete()){
											if(MyConstants.DEBUG_INFO)
												System.out.println(stmt + " calls CONCRETE method: " + stmt.getInvokeExpr().getMethod());
											List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
											defaultTargets.add(stmt.getInvokeExpr().getMethod());
											targets = defaultTargets.iterator();
										}else{	
											if(MyConstants.DEBUG_INFO)
												System.out.println(stmt + " calls NON-CONCRETE method: " + stmt.getInvokeExpr().getMethod());
											targets = new Targets(this.cg.edgesOutOf(stmt));										

											if(!targets.hasNext()){
												if(MyConstants.DEBUG_INFO)
													System.out.println(stmt + " does NOT have a target. add a DEFAULT one");
												List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
												defaultTargets.add(stmt.getInvokeExpr().getMethod());
												targets = defaultTargets.iterator();
											}
										}
										
										if(targets==null){
											continue;
										}

										while (targets.hasNext()) {
											SootMethod target = (SootMethod) targets.next();

											boolean noNewTaint = true;
											if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+stmt.toString())){														
												noNewTaint = false;
												List<Integer> sinks = new ArrayList<Integer>();
												sinks.addAll(taintVector);
												propagationHistory.put(sMethod.getSignature()+"|"+stmt.toString(), sinks);
											}else{
												List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+stmt.toString());

												for(Integer taint : taintVector){
													if(!sinks.contains(taint)){
														noNewTaint = false;
														sinks.add(taint);
													}
												}														
											}

											if(noNewTaint){
												break;
											}	

											if(MyConstants.DEBUG_INFO){
												System.out.println("PROPAGATING from METHOD: " + sMethod);
												System.out.println("PROPAGATING from STATEMENT: " + stmt);
											}
											taintedFieldsInCaller.addAll(taintedFields);
											Vector<Integer> tainted = propagate(target, taintVector, flowSink, stmt, sMethod);
											for(SootField sf : taintedFieldsInCallee){
												if(!taintedFields.contains(sf)){
													taintedFields.add(sf);
												}
											}
											taintedFieldsInCallee.clear();

											if(MyConstants.DEBUG_INFO){
												System.out.println(stmt + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
											}
											if ((tainted!=null) && (!tainted.isEmpty())) {
												AddTags(stmt, functionPreservingTag);

												for(Integer i : tainted){
													int index = i.intValue();

													if(index == MyConstants.thisObject){
														if(stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
															Value taintedThisRef = ((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase();

															if(taintedThisRef instanceof Local){
																List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, stmt);

																for(Unit defn : defs0){
																	
																	APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																	stmtNode.addPred(defNode);
																	defNode.addSucc(stmtNode);
																	if(!methodToDDGMap.get(sMethod).contains(defNode)){
																		methodToDDGMap.get(sMethod).add(defNode);
																	}

																	UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																	if(!uses.containsKey(defn)){
																		Vector<Stmt> scopes = new Vector<Stmt>();
																		scopes.add(stmt);
																		uses.put((Stmt)defn, scopes);
																		usesStack.push(defnWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																	}else if(!(uses.get(defn).contains(stmt))){
																		uses.get(defn).add(stmt);
																		usesStack.push(defnWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																	}
																}
															}
																											
														}														 

													}else if(index >= 0){

														Value taintedArg = stmt.getInvokeExpr().getArg(index);
														
														if(taintedArg instanceof Local){
															List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, stmt);

															for(Unit defn : defs0){
																
																APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																stmtNode.addPred(defNode);
																defNode.addSucc(stmtNode);
																if(!methodToDDGMap.get(sMethod).contains(defNode)){
																	methodToDDGMap.get(sMethod).add(defNode);
																}

																UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																if(!uses.containsKey(defn)){
																	Vector<Stmt> scopes = new Vector<Stmt>();
																	scopes.add(stmt);
																	uses.put((Stmt)defn, scopes);
																	usesStack.push(defnWS);
																	if(MyConstants.DEBUG_INFO)
																		System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																}else if(!(uses.get(defn).contains(stmt))){
																	uses.get(defn).add(stmt);
																	usesStack.push(defnWS);
																	if(MyConstants.DEBUG_INFO)
																		System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																}
															}
														}
													}
												}
											}

										}
										
										
									}else if(stmt instanceof DefinitionStmt){
										
										Value rhsInvoke = ((DefinitionStmt) stmt).getRightOp();
										if(rhsInvoke instanceof InvokeExpr){	
											
											Vector<Integer> taintVector = new Vector<Integer>();
											
											Iterator defIt2 = s.getDefBoxes().iterator();
											while (defIt2.hasNext()) {
												ValueBox vbox2 = (ValueBox) defIt2.next();
												if (vbox2.getValue() instanceof Local) {
													// System.out.println(vbox2.getValue());
													InvokeExpr invokeEx = stmt.getInvokeExpr();
													int argCount = invokeEx.getArgCount();
													for (int i = 0; i < argCount; i++) {
														if (invokeEx.getArg(i) == vbox2.getValue()) {
															taintVector.add(new Integer(i));
														}												
													}

													//for instance invoke, consider this reference too.
													if(invokeEx instanceof InstanceInvokeExpr){
														if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

															/*
															//special invoke doesn't count
															if(invokeEx instanceof SpecialInvokeExpr){
																if(rhs instanceof NewExpr){
																	continue;
																}																	
															}
															*/
															if(MyConstants.TO_TAINT_THIS_OBJECT)
																taintVector.add(new Integer(MyConstants.thisObject));
														}
													}
												}
											}
											
											Iterator targets = null;
											if(stmt.getInvokeExpr().getMethod().isConcrete()){
												if(MyConstants.DEBUG_INFO)
													System.out.println(stmt + " calls CONCRETE method: " + stmt.getInvokeExpr().getMethod());
												List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
												defaultTargets.add(stmt.getInvokeExpr().getMethod());
												targets = defaultTargets.iterator();
											}else{	
												if(MyConstants.DEBUG_INFO)
													System.out.println(stmt + " calls NON-CONCRETE method: " + stmt.getInvokeExpr().getMethod());
												targets = new Targets(this.cg.edgesOutOf(stmt));										

												if(!targets.hasNext()){
													if(MyConstants.DEBUG_INFO)
														System.out.println(stmt + " does NOT have a target. add a DEFAULT one");
													List<SootMethod> defaultTargets = new ArrayList<SootMethod>();
													defaultTargets.add(stmt.getInvokeExpr().getMethod());
													targets = defaultTargets.iterator();
												}
											}
											
											if(targets==null){
												continue;
											}

											while (targets.hasNext()) {
												SootMethod target = (SootMethod) targets.next();

												boolean noNewTaint = true;
												if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+stmt.toString())){														
													noNewTaint = false;
													List<Integer> sinks = new ArrayList<Integer>();
													sinks.addAll(taintVector);
													propagationHistory.put(sMethod.getSignature()+"|"+stmt.toString(), sinks);
												}else{
													List<Integer> sinks = propagationHistory.get(sMethod.getSignature()+"|"+stmt.toString());

													for(Integer taint : taintVector){
														if(!sinks.contains(taint)){
															noNewTaint = false;
															sinks.add(taint);
														}
													}														
												}

												if(noNewTaint){
													break;
												}	

												if(MyConstants.DEBUG_INFO){
													System.out.println("PROPAGATING from METHOD: " + sMethod);
													System.out.println("PROPAGATING from STATEMENT: " + stmt);
												}
												taintedFieldsInCaller.addAll(taintedFields);
												Vector<Integer> tainted = propagate(target, taintVector, flowSink, stmt, sMethod);
												for(SootField sf : taintedFieldsInCallee){
													if(!taintedFields.contains(sf)){
														taintedFields.add(sf);
													}
												}
												taintedFieldsInCallee.clear();

												if(MyConstants.DEBUG_INFO){
													System.out.println(stmt + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
												}
												if ((tainted!=null) && (!tainted.isEmpty())) {
													
													AddTags(stmt, functionPreservingTag);

													for(Integer i : tainted){
														int index = i.intValue();

														if(index == MyConstants.thisObject){
															if(stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
																Value taintedThisRef = ((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase();

																if(taintedThisRef instanceof Local){
																	List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, stmt);

																	for(Unit defn : defs0){
																		
																		APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																		stmtNode.addPred(defNode);
																		defNode.addSucc(stmtNode);
																		if(!methodToDDGMap.get(sMethod).contains(defNode)){
																			methodToDDGMap.get(sMethod).add(defNode);
																		}

																		UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																		if(!uses.containsKey(defn)){
																			Vector<Stmt> scopes = new Vector<Stmt>();
																			scopes.add(stmt);
																			uses.put((Stmt)defn, scopes);
																			usesStack.push(defnWS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																		}else if(!(uses.get(defn).contains(stmt))){
																			uses.get(defn).add(stmt);
																			usesStack.push(defnWS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																		}
																	}
																}
																												
															}														 

														}else if(index >= 0){

															Value taintedArg = stmt.getInvokeExpr().getArg(index);
															
															if(taintedArg instanceof Local){
																List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, stmt);

																for(Unit defn : defs0){
																	
																	APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

																	stmtNode.addPred(defNode);
																	defNode.addSucc(stmtNode);
																	if(!methodToDDGMap.get(sMethod).contains(defNode)){
																		methodToDDGMap.get(sMethod).add(defNode);
																	}

																	UseWithScope defnWS = new UseWithScope((Stmt)defn, stmt);
																	if(!uses.containsKey(defn)){
																		Vector<Stmt> scopes = new Vector<Stmt>();
																		scopes.add(stmt);
																		uses.put((Stmt)defn, scopes);
																		usesStack.push(defnWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																	}else if(!(uses.get(defn).contains(stmt))){
																		uses.get(defn).add(stmt);
																		usesStack.push(defnWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
																	}
																}
															}
														}
													}
												}

											}
										}
									}										
									
								}//isStmtUsingS
							}
						}//if(lhs.getType() instanceof RefLikeType){
						
						if(rhs instanceof InstanceFieldRef){
							if(MyConstants.TO_TAINT_INSTANCE_FIELD){
								if(!taintedFields.contains(((InstanceFieldRef)rhs).getField())){
									if(MyConstants.DEBUG_INFO)
										System.out.println("adding new field as source: " + ((InstanceFieldRef)rhs).getField() + " from: " + s);
									taintedFields.add(((InstanceFieldRef)rhs).getField());
									
									/*
									SootField fieldKey = ((InstanceFieldRef)rhs).getField();
									if(fieldToUsesMap.containsKey(fieldKey)){
										List<Stmt> fieldUses = fieldToUsesMap.get(fieldKey);
										if(!fieldUses.contains(s)){
											fieldUses.add(s);
										}
									}else{
										List<Stmt> fieldUses = new ArrayList<Stmt>();
										fieldToUsesMap.put(fieldKey, fieldUses);
										fieldUses.add(s);
									}
									*/
								}
								
								SootField fieldKey = ((InstanceFieldRef)rhs).getField();
								if(fieldToUsesMap.containsKey(fieldKey)){
									List<Stmt> fieldUses = fieldToUsesMap.get(fieldKey);
									if(!fieldUses.contains(s)){
										fieldUses.add(s);
									}
								}else{
									List<Stmt> fieldUses = new ArrayList<Stmt>();
									fieldToUsesMap.put(fieldKey, fieldUses);
									fieldUses.add(s);
								}
							}
						}else if(rhs instanceof StaticFieldRef){
							if(MyConstants.TO_TAINT_STATIC_FIELD){
								if(!taintedFields.contains(((StaticFieldRef)rhs).getField())){
									if(MyConstants.DEBUG_INFO)
										System.out.println("adding new field as source: " + ((StaticFieldRef)rhs).getField() + " from: " + s);
									taintedFields.add(((StaticFieldRef)rhs).getField());
									
									/*
									SootField fieldKey = ((StaticFieldRef)rhs).getField();
									if(fieldToUsesMap.containsKey(fieldKey)){
										List<Stmt> fieldUses = fieldToUsesMap.get(fieldKey);
										if(!fieldUses.contains(s)){
											fieldUses.add(s);
										}
									}else{
										List<Stmt> fieldUses = new ArrayList<Stmt>();
										fieldToUsesMap.put(fieldKey, fieldUses);
										fieldUses.add(s);
									}
									*/
								}
								
								SootField fieldKey = ((StaticFieldRef)rhs).getField();
								if(fieldToUsesMap.containsKey(fieldKey)){
									List<Stmt> fieldUses = fieldToUsesMap.get(fieldKey);
									if(!fieldUses.contains(s)){
										fieldUses.add(s);
									}
								}else{
									List<Stmt> fieldUses = new ArrayList<Stmt>();
									fieldToUsesMap.put(fieldKey, fieldUses);
									fieldUses.add(s);
								}
							}
						}
						
						Iterator<ValueBox> sUseIter = s.getUseBoxes().iterator();
						while(sUseIter.hasNext()){
							Value v = sUseIter.next().getValue();
							if(v instanceof Local){
								
								List<Unit> defs = mrd.getDefsOfAt((Local)v, s);
								
								for(Unit defn : defs){
									
									APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

									sNode.addPred(defNode);
									defNode.addSucc(sNode);
									if(!methodToDDGMap.get(sMethod).contains(defNode)){
										methodToDDGMap.get(sMethod).add(defNode);
									}
									
									UseWithScope defnWS = new UseWithScope((Stmt)defn, s);
									if(!uses.containsKey(defn)){
										Vector<Stmt> scopes = new Vector<Stmt>();
										scopes.add(s);
										uses.put((Stmt)defn, scopes);
										usesStack.push(defnWS);
										if(MyConstants.DEBUG_INFO)
											System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
									}else if(!(uses.get(defn).contains(s))){
										uses.get(defn).add(s);
										usesStack.push(defnWS);
										if(MyConstants.DEBUG_INFO)
											System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
									}
								}
							}
						}						
						
					}//if(s instanceof DefinitionStmt){
					else if(s instanceof ReturnStmt){
						Value op = ((ReturnStmt)s).getOp();
						
						if(op instanceof Local){

							List<Unit> defs = mrd.getDefsOfAt((Local)op, s);

							for(Unit defn : defs){

								APIGraphNode defNode = CreateOrGetExistingNode((Stmt)defn, sMethod);;

								sNode.addPred(defNode);
								defNode.addSucc(sNode);
								if(!methodToDDGMap.get(sMethod).contains(defNode)){
									methodToDDGMap.get(sMethod).add(defNode);
								}

								UseWithScope defnWS = new UseWithScope((Stmt)defn, s);
								if(!uses.containsKey(defn)){
									Vector<Stmt> scopes = new Vector<Stmt>();
									scopes.add(s);
									uses.put((Stmt)defn, scopes);
									usesStack.push(defnWS);
									if(MyConstants.DEBUG_INFO)
										System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
								}else if(!(uses.get(defn).contains(s))){
									uses.get(defn).add(s);
									usesStack.push(defnWS);
									if(MyConstants.DEBUG_INFO)
										System.out.println("use stack doesn't contain " + defnWS.dump() + ". Push it.");
								}
							}

						}else if(op instanceof Constant){
							
						}
					}
							
				}
			}
		}//while(!usesStack.isEmpty()){


		//////////////////////////
		Iterator i = uses.keySet().iterator();
		while (i.hasNext()) {
			Stmt s = (Stmt) i.next();

			AddTags(s, generalTaintTag);
			AddTags(s, taintTag);
			AddTags(s, functionPreservingTag);

			Iterator usesIt = s.getUseBoxes().iterator();
			while (usesIt.hasNext()) {
				ValueBox vbox = (ValueBox) usesIt.next();
				if (vbox.getValue() instanceof Local) {
					Local l = (Local) vbox.getValue();

					Iterator rDefsIt = mrd.getDefsOfAt(l, s).iterator();
					while (rDefsIt.hasNext()) {
						Stmt next = (Stmt) rDefsIt.next();
						if(!next.getTags().contains(taintTag)){

							AddTags(next,generalExtraDefTag);
							AddTags(next,extraDefTag);
						}
					}
				}
			}

			if(s instanceof IdentityStmt){
				
				APIGraphNode idNode = CreateOrGetExistingNode(s, sMethod);;

				if(!methodToDDGMap.get(sMethod).contains(idNode)){
					methodToDDGMap.get(sMethod).add(idNode);
				}
				
				APIGraphNode fromNode = CreateOrGetExistingNode(from, fromMethod);;

				if(!methodToDDGMap.get(fromMethod).contains(fromNode)){
					methodToDDGMap.get(fromMethod).add(fromNode);
				}
				
				idNode.addPred(fromNode);
				fromNode.addSucc(idNode);

				Value rhsIdentity = ((IdentityStmt) s).getRightOp();
				if(rhsIdentity instanceof ThisRef){
					if(!taintIndexes.contains(new Integer(MyConstants.thisObject))){
						if(MyConstants.TO_TAINT_THIS_OBJECT)
							taintResult.add(new Integer(MyConstants.thisObject));
					}
				}else if(rhsIdentity instanceof ParameterRef){
					int index = ((ParameterRef)rhsIdentity).getIndex();
					if(!taintIndexes.contains(new Integer(index))){
						taintResult.add(new Integer(index));
					}
				}
			}		
		}

		if(MyConstants.DEBUG_INFO){
			System.out.println();
			System.out.println("method:" + sMethod.getSignature());
			System.out.println("dataflow for " + sink + ":");			
		}

		Iterator printIt = body.getUnits().iterator();
		while(printIt.hasNext()){
			Stmt s = (Stmt)printIt.next();
			if(s.getTags().contains(taintTag) || s.getTags().contains(extraDefTag)){

				if(MyConstants.DEBUG_INFO){
					//System.out.println(s + "|" + s.getTags());
					dumpTaggedStmt(s);
				}

				Vector<Integer> labels = new Vector<Integer>();

				for(Tag tag : s.getTags()){
					if(taintTagReverseMap.containsKey(tag)){
						Integer label = new Integer(((TaintTag)tag).getLabel());
						if(!labels.contains(label)){
							labels.add(label);
						}
					}else if(extraDefTagReverseMap.containsKey(tag)){
						Integer label = new Integer(((TaintTag)tag).getLabel());
						if(!labels.contains(label)){
							labels.add(label);
						}
					}								
				}

				List<ValueBox> vbs = s.getUseAndDefBoxes();
				Iterator iter = vbs.iterator();
				while(iter.hasNext()){
					ValueBox vb = (ValueBox)iter.next();
					if(vb.getValue() instanceof InstanceFieldRef){
						SootField instanceField = ((InstanceFieldRef)vb.getValue()).getField();

						if(instanceField.getDeclaringClass().isApplicationClass() == false){
							continue;
						}

						if(instanceField.getDeclaringClass().isPhantom()){
							continue;
						}

						//if(!instanceFields.contains(instanceField)){
						//	instanceFields.add(instanceField);
						//}

						////					
						if(!instanceFieldMap.containsKey(instanceField)){						

							Vector<Integer> taintSources = new Vector<Integer>();
							taintSources.addAll(labels);							
							instanceFieldMap.put(instanceField, taintSources);

						}else{

							Vector<Integer> taintSources = instanceFieldMap.get(instanceField);
							for(Integer label : labels){
								if(!taintSources.contains(label)){
									taintSources.add(label);
								}
							}
						}
						////

						LinkedHashMap<String, List<String>> taintSourceToField = new LinkedHashMap<String, List<String>>();
						List<String> fieldList = new ArrayList<String>();
						if(fieldList.contains(instanceField.getSignature())){
							fieldList.add(instanceField.getSignature());
						}								
						taintSourceToField.put(flowSink, fieldList);
						classToSourceToField.put(sMethod.getDeclaringClass().getName(), taintSourceToField);

					}else if(vb.getValue() instanceof StaticFieldRef){
						SootField staticField = ((StaticFieldRef)vb.getValue()).getField();

						if(staticField.getDeclaringClass().isApplicationClass() == false){
							continue;
						}

						if(staticField.getDeclaringClass().isPhantom()){
							continue;
						}

						//if(!staticFields.contains(staticField)){
						//	staticFields.add(staticField);
						//}

						///
						if(!staticFieldMap.containsKey(staticField)){						

							Vector<Integer> taintSources = new Vector<Integer>();
							taintSources.addAll(labels);
							staticFieldMap.put(staticField, taintSources);

						}else{

							Vector<Integer> taintSources = staticFieldMap.get(staticField);
							for(Integer label : labels){
								if(!taintSources.contains(label)){
									taintSources.add(label);
								}
							}
						}
						///

						LinkedHashMap<String, List<String>> taintSourceToField = new LinkedHashMap<String, List<String>>();
						List<String> fieldList = new ArrayList<String>();
						if(fieldList.contains(staticField.getSignature())){
							fieldList.add(staticField.getSignature());
						}								
						taintSourceToField.put(flowSink, fieldList);
						classToSourceToField.put(sMethod.getDeclaringClass().getName(), taintSourceToField);

					}else if(vb.getValue() instanceof Local){

						String varName = ((Local)vb.getValue()).getName();								
						LinkedHashMap<String, List<String>> taintSourceToVar = new LinkedHashMap<String, List<String>>();
						List<String> varList = new ArrayList<String>();
						if(varList.contains(varName)){
							varList.add(varName);
						}								
						taintSourceToVar.put(flowSink, varList);
						methodToSourceToVar.put(sMethod.getSignature(), taintSourceToVar);
					}
				}
			}
		}

		if(MyConstants.DEBUG_INFO){
			System.out.println("end dataflow for " + sink + "\n");
		}

		/////////////////////////
		taintedFieldsInCallee.addAll(taintedFields);

		Set<SootField> instanceKeySet = instanceFieldMap.keySet();
		Iterator<SootField> instanceIter = instanceKeySet.iterator();
		while(instanceIter.hasNext()){
			SootField f = instanceIter.next();
			Vector<Integer> newLabels = instanceFieldMap.get(f);

			if(usedInstanceFieldMap.containsKey(f)){
				Vector<Integer> oldLabels = usedInstanceFieldMap.get(f);
				for(Integer label : newLabels){
					if(!oldLabels.contains(label)){
						oldLabels.add(label);
					}
				}
			}else{
				Vector<Integer> labels = new Vector<Integer>();
				labels.addAll(newLabels);
				usedInstanceFieldMap.put(f, labels);
			}
		}

		Set<SootField> staticKeySet = staticFieldMap.keySet();
		Iterator<SootField> staticIter = staticKeySet.iterator();
		while(staticIter.hasNext()){
			SootField f = staticIter.next();
			Vector<Integer> newLabels = staticFieldMap.get(f);

			if(usedStaticFieldMap.containsKey(f)){
				Vector<Integer> oldLabels = usedStaticFieldMap.get(f);
				for(Integer label : newLabels){
					if(!oldLabels.contains(label)){
						oldLabels.add(label);
					}
				}
			}else{
				Vector<Integer> labels = new Vector<Integer>();
				labels.addAll(newLabels);
				usedStaticFieldMap.put(f, labels);
			}
		}

		callString.pop();

		return taintResult;
	}

	private boolean isInScope(ExceptionalUnitGraph eug, Stmt toTest, Stmt scopeEnd){
		
		if(scopeEnd == null){
			return true;
		}
		
		if(toTest == scopeEnd){
			return false;
		}

		Stack<Stmt> predecessors = new Stack<Stmt>();
		Vector<Stmt> traversedPreds = new Vector<Stmt>();

		predecessors.push(scopeEnd);
		traversedPreds.add(scopeEnd);

		while(!predecessors.isEmpty()){
			Stmt predecessor = predecessors.pop();
			
			if(predecessor == toTest){
				return true;
			}
			List<Unit> predsOfPredecessor = eug.getPredsOf(predecessor);
			for(Unit u : predsOfPredecessor){
				Stmt s = (Stmt)u;
				if(!traversedPreds.contains(s)){
					traversedPreds.add(s);
					predecessors.push(s);
				}
			}
		}

		return false;
	}
	
	private void buildActivityConnection(){
		
		LinkedHashMap<Stmt, List<String>> intentToActivity = new LinkedHashMap<Stmt, List<String>>();
		LinkedHashMap<Stmt, SootMethod> stmtToMethod = new LinkedHashMap<Stmt, SootMethod>();
		
		Chain<SootClass> classes = Scene.v().getClasses();
		Iterator<SootClass> classes_iter = classes.iterator();		
		
		while (classes_iter.hasNext()) {
			SootClass soot_class = classes_iter.next();

			if (soot_class.isApplicationClass() == false) {
				continue;
			}

			List<SootMethod> methods = soot_class.getMethods();
			
			
			for (SootMethod method : methods) {
				
				//System.out.println("building Activity connections for " + method);
				
				if(!method.isConcrete()){
					continue;
				}
								
				JimpleBody body = (JimpleBody) method.retrieveActiveBody();
				ExceptionalUnitGraph eug = new ExceptionalUnitGraph(body);       
		        MyReachingDefinition mrd = new MyReachingDefinition(eug);
				
				Iterator it = body.getUnits().iterator();
				while (it.hasNext()) {
					Stmt s = (Stmt) it.next();
					if(s instanceof InvokeStmt){
						InvokeExpr invoke = s.getInvokeExpr();
						SootMethod m = invoke.getMethod();
						if(!m.getDeclaringClass().isApplicationClass()){
							if(m.getSubSignature().equals("void startActivity(android.content.Intent)")
									|| m.getSubSignature().equals("void startActivity(android.content.Intent,android.os.Bundle)")
									|| m.getSubSignature().equals("void startActivityForResult(android.content.Intent,int)")
									|| m.getSubSignature().equals("void startActivityForResult(android.content.Intent,int,android.os.Bundle)")){
								
								
								
								Value intent = invoke.getArg(0);
								if(intent instanceof Local){
									List<Unit> defs = mrd.getDefsOfAt((Local)intent, s);
							
									Stack<Unit> preds = new Stack<Unit>();
									for(Unit u : eug.getPredsOf(s)){
										if(!(u instanceof IdentityStmt)){
											preds.push(u);											
										}
									}
									
									Stack<Unit> flow = new Stack<Unit>();
									while(!preds.isEmpty()){
										Unit pred = preds.pop();
										
										if(!flow.isEmpty()){
											if(!eug.getSuccsOf(pred).isEmpty()){
												List<Unit> succsOfPred = eug.getSuccsOf(pred);
												while(!succsOfPred.contains(flow.peek())){
													flow.pop();
													if(flow.isEmpty()){
														break;
													}
												}
											}else{
												flow.clear();
											}
										}
										
										
										if(flow.contains(pred)){
											System.out.println("LOOP found: " + pred + ", continue with the next one");
											continue;
										}	
												
										flow.push(pred);
										
										//System.out.println(s + "[" + pred);
										
										if(pred instanceof InvokeStmt){
											
											InvokeExpr invo = ((InvokeStmt) pred).getInvokeExpr();
											if(invo instanceof InstanceInvokeExpr){
												
												Local base = (Local)((InstanceInvokeExpr) invo).getBase();
												List<Unit> defsOfBase = mrd.getDefsOfAt(base, pred);
												if(intersect(defs, defsOfBase)){
													
													List<String> targets = new ArrayList<String>();
													
													SootMethod meth = invo.getMethod();
													if(meth.getSignature().equals(
															"<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>")){
														
														Value target = invo.getArg(1);
														
														List<String> concreteValues = new ArrayList<String>();
														String annotation = lookForConcreteValue((Stmt)pred, target, mrd, concreteValues);
														if(annotation.contains("C")){
															for(String concrete : concreteValues){
																if(!targets.contains(concrete)){
																	targets.add(concrete);					
																}
															}
														}
														
													}else if(meth.getSignature().equals(
															"<android.content.Intent: void <init>(java.lang.String,android.net.Uri,android.content.Context,java.lang.Class)>")){
														
														Value target = invo.getArg(3);
														
														List<String> concreteValues = new ArrayList<String>();
														String annotation = lookForConcreteValue((Stmt)pred, target, mrd, concreteValues);
														if(annotation.contains("C")){
															for(String concrete : concreteValues){
																if(!targets.contains(concrete)){
																	targets.add(concrete);					
																}
															}
														}
														
													}else if(meth.getSignature().equals(
															"<android.content.Intent: android.content.Intent setClass(android.content.Context,java.lang.Class)>")){
														
														Value target = invo.getArg(1);
														
														List<String> concreteValues = new ArrayList<String>();
														String annotation = lookForConcreteValue((Stmt)pred, target, mrd, concreteValues);
														if(annotation.contains("C")){
															for(String concrete : concreteValues){
																if(!targets.contains(concrete)){
																	targets.add(concrete);					
																}
															}
														}
													}else if(meth.getSignature().equals(
															"<android.content.Intent: android.content.Intent setClassName(android.content.Context,java.lang.String)>")){
														
														Value target = invo.getArg(1);
														
														List<String> concreteValues = new ArrayList<String>();
														String annotation = lookForConcreteValue((Stmt)pred, target, mrd, concreteValues);
														if(annotation.contains("C")){
															for(String concrete : concreteValues){
																if(!targets.contains(concrete)){
																	targets.add(concrete);					
																}
															}
														}
													}else if(meth.getSignature().equals(
															"<android.content.Intent: android.content.Intent setClassName(java.lang.String,java.lang.String)>")){
														
														Value target = invo.getArg(1);
														
														List<String> concreteValues = new ArrayList<String>();
														String annotation = lookForConcreteValue((Stmt)pred, target, mrd, concreteValues);
														if(annotation.contains("C")){
															for(String concrete : concreteValues){
																if(!targets.contains(concrete)){
																	targets.add(concrete);					
																}
															}
														}
													}
													
													/*
													for(String tar : targets){
														//System.out.println("Intent[" + method + "->" +  tar + "]@" + s);
														Log.dumpln(ACTIVITY_LOG, "Intent[" + method.getDeclaringClass() + "->" +  tar + "]");
														Log.dumpln(ACTIVITY_LOG, "\t@" + method + "{" + s + "}");
													}
													*/
													intentToActivity.put(s, targets);
													stmtToMethod.put(s, method);
												}
											}
											
											
											
										}
										
										for(Unit u : eug.getPredsOf(pred)){
											if(!(u instanceof IdentityStmt)){
												preds.push(u);												
											}
										}
									}
								}
								
							}
						}//end if(!m.getDeclaringClass().isApplicationClass()){
					}
				}//end while (it.hasNext()) {
			}//end for (SootMethod method : methods) {
			
		}//end while (classes_iter.hasNext()) {
		
		
		Set<Stmt> keySet = intentToActivity.keySet();
		Iterator<Stmt> iter = keySet.iterator();
		while(iter.hasNext()){
			Stmt stmt = iter.next();
			List<String> activities = intentToActivity.get(stmt);
			if(!activities.isEmpty()){
				SootMethod m = stmtToMethod.get(stmt);
				for(String tar : activities){
					tar = tar.substring((tar.indexOf('"')+1), tar.lastIndexOf('"'));
					tar = tar.replace('/', '.');
					Log.dumpln(ACTIVITY_LOG, "Intent[" + m.getDeclaringClass() + "->" +  tar + "]");
					//Log.dumpln(ACTIVITY_LOG, "\t@" + m + "{" + stmt + "}\n");
				}
			}
		}
	}
	
	//Activity.<init> -> onCreate() -> onStart() -> onResume() -> onDestroy()
	//View.<ini> -> onClick(),e.g.
	//AsyncTask.doInBackground() -> AsyncTask.onPostExecute()
	private void linkImplicitFlow(){
		
	}
	
	//Context.startActivity(Intent) -> Activity.onCreate()		//TODO
	//Context.startActivityForResult(Intent, int) -> Activity.onCreate()
	//Activity.setResult(int, Intent) -> Activity.onActivityResult(int, int, Intent)
	private void linkActivity(){
		
		interActivityGraphs = new LinkedHashMap<SootMethod, APIGraph>();
	}
	
	//link asynchronous callees to their callers (i.e. Thread.start() -> Thread.run(),
	//	AsyncTask.execute() -> AsyncTask.doInBackground(), 
	//	AsyncTask.onPostExecute() -> AsyncTask.execute(),  	//TODO	
	private void linkAsync(){
		
		asyncGlobalApiGraphs = new LinkedHashMap<SootMethod, APIGraph>();
		
		LinkedHashMap<SootMethod, APIGraph> nonThreadGraphs = new LinkedHashMap<SootMethod, APIGraph>();
		LinkedHashMap<SootMethod, APIGraph> threadGraphs = new LinkedHashMap<SootMethod, APIGraph>();
		
		Set<SootMethod> keySet = this.globalApiGraphs.keySet();
		Iterator<SootMethod> iter = keySet.iterator();
		while(iter.hasNext()){
			SootMethod entryPoint = iter.next();
			APIGraph graph = this.globalApiGraphs.get(entryPoint);
			boolean isAsync = false;
			if(entryPoint.getName().equals("run")){
				if(isThread(entryPoint.getDeclaringClass())){
					isAsync = true;
				}
			}else if(entryPoint.getName().equals("doInBackground")){
				if(isAsyncTask(entryPoint.getDeclaringClass())){
					isAsync = true;
				}
			}
			
			if(isAsync){
				threadGraphs.put(entryPoint, graph.clone());
			}else{
				nonThreadGraphs.put(entryPoint, graph.clone());
			}
		}
		
		List<SootMethod> calledThreads = new ArrayList<SootMethod>();
		
		Set<SootMethod> keySetNonThread = nonThreadGraphs.keySet();
		Iterator<SootMethod> iterNonThread = keySetNonThread.iterator();
		while(iterNonThread.hasNext()){
			SootMethod entryPoint = iterNonThread.next();
			APIGraph graph = nonThreadGraphs.get(entryPoint);			
			
			Stack<APIGraphNode> pendingAppendPoints = new Stack<APIGraphNode>();
			
			for(APIGraphNode node : graph.getAPIGraph()){
				SootMethod callsite = node.getStmt().getInvokeExpr().getMethod();
				if(callsite.getName().equals("start")){
					if(isThread(callsite.getDeclaringClass())){					
						pendingAppendPoints.push(node);
					}
				}else if(callsite.getName().equals("execute")){
					if(isAsyncTask(callsite.getDeclaringClass())){	
						pendingAppendPoints.push(node);
					}
				}
			}
				
			Stack<Stmt> callString = new Stack<Stmt>();
			
			while(!pendingAppendPoints.isEmpty()){
				APIGraphNode appendPoint = pendingAppendPoints.pop();
				
				if(!callString.isEmpty()){
					if(appendPoint.getCallsite()!=null){
						Stmt caller = appendPoint.getCallsite();
						while(!callString.peek().equals(caller)){
							callString.pop();
							if(callString.isEmpty()){
								break;
							}
						}
					}else{
						callString.clear();
					}
				}
				
				if(callString.contains(appendPoint.getStmt())){
					System.out.println("RECURSIVE call found: " + appendPoint.getStmt() + ", continue with the next one");
					continue;
				}	
						
				callString.push(appendPoint.getStmt());
				
				//System.out.println("Append at " + appendPoint.getStmt());
				
				APIGraph callee = null;
				List<APIGraph> callees = new ArrayList<APIGraph>();
				
				SootMethod callsite = appendPoint.getStmt().getInvokeExpr().getMethod();
				if(callsite.getName().equals("start")){
										
					Iterator targets = new Targets(callgraph.edgesOutOf(appendPoint.getStmt()));
					while (targets.hasNext()) {
						SootMethod target = (SootMethod) targets.next();
						if(target.getDeclaringClass().isApplicationClass()){							
							
							if(threadGraphs.containsKey(target)){
								
								if(!calledThreads.contains(target)){
									calledThreads.add(target);
								}
							
								callee = threadGraphs.get(target).clone();
								callee.setCallsite(appendPoint.getStmt());
								
								
								for(APIGraphNode node : callee.getAPIGraph()){
									SootMethod innerCallsite = node.getStmt().getInvokeExpr().getMethod();
									if(innerCallsite.getName().equals("start")){
										if(isThread(innerCallsite.getDeclaringClass())){
											pendingAppendPoints.push(node);
										}
									}else if(innerCallsite.getName().equals("execute")){
										if(isAsyncTask(innerCallsite.getDeclaringClass())){
											pendingAppendPoints.push(node);
										}
									}
								}
								
								callees.add(callee);
							}
						}
					}
				}else if(callsite.getName().equals("execute")){
					Set<SootMethod> keySetThread = threadGraphs.keySet();
					Iterator<SootMethod> iterThread = keySetThread.iterator();
					while(iterThread.hasNext()){
						SootMethod target = iterThread.next();
						if(target.getDeclaringClass().equals(callsite.getDeclaringClass())
								&& target.getName().equals("doInbackground")){
							
							if(!calledThreads.contains(target)){
								calledThreads.add(target);
							}
							
							callee = threadGraphs.get(target).clone();
							callee.setCallsite(appendPoint.getStmt());
							
							
							for(APIGraphNode node : callee.getAPIGraph()){
								SootMethod innerCallsite = node.getStmt().getInvokeExpr().getMethod();
								if(innerCallsite.getName().equals("start")){
									if(isThread(innerCallsite.getDeclaringClass())){
										pendingAppendPoints.push(node);
									}
								}else if(innerCallsite.getName().equals("execute")){
									if(isAsyncTask(innerCallsite.getDeclaringClass())){
										pendingAppendPoints.push(node);
									}
								}
							}
							
							callees.add(callee);
						}
					}
				}
			
				graph.append(appendPoint, callees);
				
			}			
			
		}
		
		for(SootMethod m : calledThreads){
			threadGraphs.remove(m);
		}
		
		asyncGlobalApiGraphs.putAll(nonThreadGraphs);
		asyncGlobalApiGraphs.putAll(threadGraphs);
	}
	
	private boolean isAsyncTask(SootClass clazz){
				
		while(clazz.hasSuperclass() && clazz.isApplicationClass()){
			
			clazz = clazz.getSuperclass();
						
		}
				
		if(clazz.getName().equals("android.os.AsyncTask")){
			return true;
		}
		
		return false;
	}
	
	private boolean isThread(SootClass clazz){
		
		while(clazz.hasSuperclass() && clazz.isApplicationClass()){
			
			for(SootClass interfaze : clazz.getInterfaces()){
				if(interfaze.getName().equals("java.lang.Runnable")){
					return true;
				}
			}
			
			clazz = clazz.getSuperclass();
						
		}
		
		for(SootClass interfaze : clazz.getInterfaces()){
			if(interfaze.getName().equals("java.lang.Runnable")){
				return true;
			}
		}
				
		return false;
	}
	
	private void mergeAPISubGraphs(){
		linkAsync();
		linkImplicitFlow();
		linkActivity();
	}
		
	private APIGraph buildAPISubGraph(SootMethod entryPoint){
		
		APIGraph apiSubGraph = apiGraphs.get(entryPoint).clone();
		
		Stack<APIGraphNode> callsites = new Stack<APIGraphNode>();
		for(APIGraphNode node : apiSubGraph.getAPIGraph()){
			if(node.getStmt().getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
				callsites.push(node);
			}
		}
		
		Stack<SootMethod> callString = new Stack<SootMethod>();
		
		while(!callsites.isEmpty()){
			APIGraphNode callsite = callsites.pop();
			
			SootMethod method = callsite.getStmt().getInvokeExpr().getMethod();
			
			
			//System.out.println("callstring: " + callString + "\n");
			if(callsite.getCallsite()!=null){
				SootMethod caller = callsite.getCallsite().getInvokeExpr().getMethod();
				while(!callString.peek().equals(caller)){
					callString.pop();
				}
			}else{
				callString.clear();
			}
			
			
			if(callString.contains(method)){
				System.out.println("RECURSIVE call found: " + method + ", continue with the next one");
				continue;
			}	
					
			callString.push(method);
			
						
			//System.out.println("callsite: " + callsite.getStmt() + " from: " + callsite.getCallsite());
					
			APIGraph callee = null;
			List<APIGraph> callees = new ArrayList<APIGraph>();
			//boolean hasCallee = false;
			if(!apiGraphs.containsKey(method)){
				//System.out.println("WARNING: method not found: " + method);
				//continue;
				Iterator targets = new Targets(callgraph.edgesOutOf(callsite.getStmt()));
				while(targets.hasNext()){
					SootMethod target = (SootMethod)targets.next();
					
					callee = apiGraphs.get(target).clone();
					callee.setCallsite(callsite.getStmt());
					
					
					for(APIGraphNode node : callee.getAPIGraph()){
						if(node.getStmt().getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
							callsites.push(node);
							//hasCallee = true;
						}
					}
					callees.add(callee);
				}				
			}else{			
				callee = apiGraphs.get(method).clone();
				callee.setCallsite(callsite.getStmt());
				
				for(APIGraphNode node : callee.getAPIGraph()){
					if(node.getStmt().getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
						callsites.push(node);
						//hasCallee = true;
					}
				}
				callees.add(callee);
			}
						
			apiSubGraph.inline(callsite, callees);
			
		}
		
		//remove recursive stubs, need to have a better solution, but for now just a removal
		List<APIGraphNode> recursiveCallers = new ArrayList<APIGraphNode>();
		for(APIGraphNode node : apiSubGraph.getAPIGraph()){
			if(node.getAnnotation().equals("")){
				recursiveCallers.add(node);
			}
		}
		
		for(APIGraphNode node : recursiveCallers){
			apiSubGraph.deleteNode(node);
		}
		
		return apiSubGraph;
	}
	

	private LinkedHashMap<Stmt, SootMethod> lookForCallsites(SootMethod callee, CallGraph callgraph){
		
		System.out.println("Looking for callsites...");
		LinkedHashMap<Stmt, SootMethod> callsites = new LinkedHashMap<Stmt, SootMethod>();
		
		Chain<SootClass> classes = Scene.v().getClasses();
		Iterator<SootClass> classes_iter = classes.iterator();
		while (classes_iter.hasNext()) {
			SootClass soot_class = classes_iter.next();

			if (soot_class.isApplicationClass() == false) {
				continue;
			}

			List<SootMethod> methods = soot_class.getMethods();
			
			
			for (SootMethod method : methods) {	
				if(!method.isConcrete()){
					continue;
				}
				
				Iterator targets = new Targets(callgraph.edgesOutOf(method));
				while(targets.hasNext()){
					SootMethod target = (SootMethod)targets.next();
					if(target.equals(callee)){
						
						JimpleBody body = (JimpleBody)method.getActiveBody();
						
						Iterator it = body.getUnits().iterator();
						while (it.hasNext()) {
							Stmt s = (Stmt) it.next();
							boolean isInvoke = false;
							if(s instanceof InvokeStmt){
								isInvoke = true;
								
							}else if(s instanceof DefinitionStmt){
								if(((DefinitionStmt) s).getRightOp() instanceof InvokeExpr){
									isInvoke = true;
								}
							}
							
							if(isInvoke){				
								Iterator calltargets = new Targets(callgraph.edgesOutOf(s));
								while(calltargets.hasNext()){
									SootMethod calltarget = (SootMethod)calltargets.next();
									if(calltarget.equals(callee)){
										//System.out.println("target == callee:" + callee);
										if(!callsites.containsKey(s)){
											callsites.put(s, method);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		/*
		Iterator callers = new Targets(callgraph.edgesInto(callee));
		while (callers.hasNext()) {
			SootMethod caller = (SootMethod) callers.next();
			System.out.println("caller:" + caller + "|callee:" + callee);
			
			JimpleBody body = (JimpleBody)caller.getActiveBody();
			
			Iterator it = body.getUnits().iterator();
			while (it.hasNext()) {
				Stmt s = (Stmt) it.next();
				boolean isInvoke = false;
				if(s instanceof InvokeStmt){
					isInvoke = true;
					
				}else if(s instanceof DefinitionStmt){
					if(((DefinitionStmt) s).getRightOp() instanceof InvokeExpr){
						isInvoke = true;
					}
				}
				
				if(isInvoke){				
					Iterator targets = new Targets(callgraph.edgesOutOf(s));
					while(targets.hasNext()){
						SootMethod target = (SootMethod)targets.next();
						if(target.equals(callee)){
							System.out.println("target == callee:" + callee);
							if(!callsites.containsKey(s)){
								callsites.put(s, caller);
							}
						}
					}
				}
			}
		}
		*/		
		
		return callsites;
	}
	
	//return an annotation for a specific parameter
	//C: constant | value
	//P: parameter/this reference | type
	//SF: static field | field
	//IF: instance field | field
	//LO: local object | type
	//RA: return value of an API | method
	//RF: return value of a function call | method
	//RB: return value of a binary operation | type
	//E: exception | type
	private String lookForConcreteValue(Stmt s, Value var, MyReachingDefinition mrd, List<String> concreteValues){
		
		String annotation = "";
		
		if(var instanceof Constant){
			annotation = attach(annotation, "C");			
			if(!concreteValues.contains(var.toString())){
				concreteValues.add(var.toString());
			}			
			return annotation;			
		}
		
		if(var instanceof Local){
			
			Stack<Stmt> useStack = new Stack<Stmt>();
			useStack.push(s);
			
			Stack<Local> localStack = new Stack<Local>();
			localStack.push((Local)var);
			
			while(!useStack.isEmpty()){
				Stmt use = useStack.pop();
				Local local = localStack.pop();
				
				List<Unit> defs = mrd.getDefsOfAt(local, use);
				
				//System.out.println("Use:" + use + "[Local:" + local + "[Defs:" + defs);
				
				for(Unit def : defs){
					
					boolean isRecursiveDefine = false;
					List<ValueBox> useBox = def.getUseBoxes();
					for(ValueBox ub : useBox){
						Value u = ub.getValue();
						if(u instanceof Local){
							List<Unit> defsOfU = mrd.getDefsOfAt((Local)u, def);
							if(defsOfU.contains(use)){
								isRecursiveDefine = true;
							}
						}
					}
					
					//System.out.println("here");
					
					if(isRecursiveDefine){
						System.out.println("Recursive define!");
						continue;
					}
					
					if(def instanceof DefinitionStmt){
						Value rhs = ((DefinitionStmt) def).getRightOp();
						if(rhs instanceof Constant){							
							annotation = attach(annotation, "C");							
							if(!concreteValues.contains(rhs.toString())){
								concreteValues.add(rhs.toString());
							}							
						}else if(rhs instanceof Local){							
							useStack.push((Stmt)def);
							localStack.push((Local)rhs);							
						}else if(rhs instanceof UnopExpr){							
							Value op = ((UnopExpr) rhs).getOp();
							if(op instanceof Local){
								useStack.push((Stmt)def);
								localStack.push((Local)op);
							}
						}else if(rhs instanceof CastExpr){							
							Value op = ((CastExpr) rhs).getOp();
							if(op instanceof Local){
								useStack.push((Stmt)def);
								localStack.push((Local)op);
							}
						}else if(rhs instanceof ArrayRef){							
							Value base = ((ArrayRef) rhs).getBase();
							if(base instanceof Local){
								useStack.push((Stmt)def);
								localStack.push((Local)base);
							}
						}else if(rhs instanceof BinopExpr){
							annotation = attach(annotation, "RB");
						}else if(rhs instanceof ParameterRef){
							annotation = attach(annotation, "P");
						}else if(rhs instanceof ThisRef){
							annotation = attach(annotation, "P");
						}else if(rhs instanceof InstanceFieldRef){
							annotation = attach(annotation, "IF");
							if(!concreteValues.contains(((InstanceFieldRef) rhs).getField().toString())){
								concreteValues.add(((InstanceFieldRef) rhs).getField().toString());
							}
						}else if(rhs instanceof StaticFieldRef){
							annotation = attach(annotation, "SF");
							if(!concreteValues.contains(((StaticFieldRef) rhs).getField().toString())){
								concreteValues.add(((StaticFieldRef) rhs).getField().toString());
							}
						}else if(rhs instanceof NewExpr){
							annotation = attach(annotation, "LO");
						}else if(rhs instanceof NewArrayExpr){
							annotation = attach(annotation, "LO");
						}else if(rhs instanceof InvokeExpr){							
							SootMethod m = ((InvokeExpr) rhs).getMethod();
							SootClass c = m.getDeclaringClass();
							if(c.isApplicationClass()){
								annotation = attach(annotation, "RF");
								if(!concreteValues.contains(m.toString())){
									concreteValues.add(m.toString());
								}
							}else{
								annotation = attach(annotation, "RA");
								if(!concreteValues.contains(m.toString())){
									concreteValues.add(m.toString());
								}
							}
						}else if(rhs instanceof CaughtExceptionRef){
							annotation = attach(annotation, "E");
						}
					}//end if
				}//end for
				
				//System.out.println("end for");
			}//end while
			
			//System.out.println("end while");
		}else{
			System.err.println("Unhandled type: " + var.getClass());
			System.exit(-1);
		}		
		
		return annotation;
	}	

	//return an annotation for a specific parameter
	//C: constant | value
	//P: parameter/this reference | type
	//SF: static field | field
	//IF: instance field | field
	//LO: local object | type
	//RA: return value of an API | method
	//RF: return value of a function call | method
	//RB: return value of a binary operation | type
	//E: exception | type
	private String lookForConcreteValue(Stmt s, Value var, MyReachingDefinition mrd, List<String> concreteValues, SootMethod hostMethod, CallGraph callgraph){

		String annotation = "";

		if(var instanceof Constant){
			annotation = attach(annotation, "C");			
			if(!concreteValues.contains(var.toString())){
				concreteValues.add(var.toString());
			}			
			return annotation;			
		}

		if(var instanceof Local){

			Stack<Stmt> useStack = new Stack<Stmt>();
			useStack.push(s);

			Stack<Local> localStack = new Stack<Local>();
			localStack.push((Local)var);

			while(!useStack.isEmpty()){
				Stmt use = useStack.pop();
				Local local = localStack.pop();

				List<Unit> defs = mrd.getDefsOfAt(local, use);

				//System.out.println("Use:" + use + "[Local:" + local + "[Defs:" + defs);

				for(Unit def : defs){

					boolean isRecursiveDefine = false;
					List<ValueBox> useBox = def.getUseBoxes();
					for(ValueBox ub : useBox){
						Value u = ub.getValue();
						if(u instanceof Local){
							List<Unit> defsOfU = mrd.getDefsOfAt((Local)u, def);
							if(defsOfU.contains(use)){
								isRecursiveDefine = true;
							}
						}
					}

					//System.out.println("here");

					if(isRecursiveDefine){
						System.out.println("Recursive define!");
						continue;
					}

					if(def instanceof DefinitionStmt){
						Value rhs = ((DefinitionStmt) def).getRightOp();
						if(rhs instanceof Constant){							
							annotation = attach(annotation, "C");							
							if(!concreteValues.contains(rhs.toString())){
								concreteValues.add(rhs.toString());
							}							
						}else if(rhs instanceof Local){							
							useStack.push((Stmt)def);
							localStack.push((Local)rhs);							
						}else if(rhs instanceof UnopExpr){							
							Value op = ((UnopExpr) rhs).getOp();
							if(op instanceof Local){
								useStack.push((Stmt)def);
								localStack.push((Local)op);
							}
						}else if(rhs instanceof CastExpr){							
							Value op = ((CastExpr) rhs).getOp();
							if(op instanceof Local){
								useStack.push((Stmt)def);
								localStack.push((Local)op);
							}
						}else if(rhs instanceof ArrayRef){							
							Value base = ((ArrayRef) rhs).getBase();
							if(base instanceof Local){
								useStack.push((Stmt)def);
								localStack.push((Local)base);
							}
						}else if(rhs instanceof BinopExpr){
							annotation = attach(annotation, "RB");
						}else if(rhs instanceof ParameterRef){

							int index = ((ParameterRef)rhs).getIndex();
							LinkedHashMap<Stmt, SootMethod> callsites = lookForCallsites(hostMethod, callgraph);
														
							Set<Stmt> keySet = callsites.keySet();
							Iterator<Stmt> iter = keySet.iterator();
							while(iter.hasNext()){
								Stmt callsite = iter.next();
								InvokeExpr invoke = callsite.getInvokeExpr();
								Value v = invoke.getArg(index);
								
								SootMethod callerMeth = callsites.get(callsite);
								JimpleBody callerBody = (JimpleBody)callerMeth.getActiveBody();
								
								ExceptionalUnitGraph callerEug = new ExceptionalUnitGraph(callerBody);       
						        MyReachingDefinition callerMrd = new MyReachingDefinition(callerEug);
								
						        /*
						        System.out.println("annotation " + annotation);
						        System.out.println("concreteValues " + concreteValues);
						        System.out.println("callsite = " + callsite);
						        System.out.println("v = " + v);
						        */
						        annotation = attach(annotation, lookForConcreteValue(callsite, v, callerMrd, concreteValues));
						        /*
						        System.out.println("annotation " + annotation);
						        System.out.println("concreteValues " + concreteValues);
						        */
							}
							//annotation = attach(annotation, "P");
						}else if(rhs instanceof ThisRef){
							
							LinkedHashMap<Stmt, SootMethod> callsites = lookForCallsites(hostMethod, callgraph);
							
							Set<Stmt> keySet = callsites.keySet();
							Iterator<Stmt> iter = keySet.iterator();
							while(iter.hasNext()){
								Stmt callsite = iter.next();
								InvokeExpr invoke = callsite.getInvokeExpr();
								Value v = ((InstanceInvokeExpr)invoke).getBase();
								
								SootMethod callerMeth = callsites.get(callsite);
								JimpleBody callerBody = (JimpleBody)callerMeth.getActiveBody();
								
								ExceptionalUnitGraph callerEug = new ExceptionalUnitGraph(callerBody);       
						        MyReachingDefinition callerMrd = new MyReachingDefinition(callerEug);
								
						        annotation = attach(annotation, lookForConcreteValue(callsite, v, callerMrd, concreteValues));
							}
							//annotation = attach(annotation, "P");
						}else if(rhs instanceof InstanceFieldRef){
							annotation = attach(annotation, "IF");
							if(!concreteValues.contains(((InstanceFieldRef) rhs).getField().toString())){
								concreteValues.add(((InstanceFieldRef) rhs).getField().toString());
							}
						}else if(rhs instanceof StaticFieldRef){
							annotation = attach(annotation, "SF");
							if(!concreteValues.contains(((StaticFieldRef) rhs).getField().toString())){
								concreteValues.add(((StaticFieldRef) rhs).getField().toString());
							}
						}else if(rhs instanceof NewExpr){
							annotation = attach(annotation, "LO");
						}else if(rhs instanceof NewArrayExpr){
							annotation = attach(annotation, "LO");
						}else if(rhs instanceof InvokeExpr){							
							SootMethod m = ((InvokeExpr) rhs).getMethod();
							SootClass c = m.getDeclaringClass();
							if(c.isApplicationClass()){
								
								Iterator targets = new Targets(callgraph.edgesOutOf(def));
								while(targets.hasNext()){
									SootMethod target = (SootMethod)targets.next();
									
									JimpleBody targetBody = (JimpleBody)target.getActiveBody();
									
									ExceptionalUnitGraph targetEug = new ExceptionalUnitGraph(targetBody);       
							        MyReachingDefinition targetMrd = new MyReachingDefinition(targetEug);
							        
							        List<Stmt> returnStmts = lookForReturnStmt(target);
							        
							        for(Stmt ret : returnStmts){
							        	Value v = ((ReturnStmt)ret).getOp();
							        	annotation = attach(annotation, lookForConcreteValue(ret, v, targetMrd, concreteValues));
							        }
									
								}
								/*
								annotation = attach(annotation, "RF");
								if(!concreteValues.contains(m.toString())){
									concreteValues.add(m.toString());
								}
								*/
							}else{
								annotation = attach(annotation, "RA");
								if(!concreteValues.contains(m.toString())){
									concreteValues.add(m.toString());
								}
							}
						}else if(rhs instanceof CaughtExceptionRef){
							annotation = attach(annotation, "E");
						}
					}//end if
				}//end for

				//System.out.println("end for");
			}//end while

			//System.out.println("end while");
		}else{
			System.err.println("Unhandled type: " + var.getClass());
			System.exit(-1);
		}		

		return annotation;
	}
	
	private List<Stmt> lookForReturnStmt(SootMethod method){
		
		List<Stmt> returnStmts = new ArrayList<Stmt>();
		
		if(method.isConcrete()){
			JimpleBody body = (JimpleBody)method.getActiveBody();
			Iterator iter = body.getUnits().iterator();
			while(iter.hasNext()){
				Stmt s = (Stmt)iter.next();
				if(s instanceof ReturnStmt){
					if(!returnStmts.contains(s)){
						returnStmts.add(s);
					}
				}
			}
		}
		
		return returnStmts;
	}	
	
	private void annotateAPIs(){
		Chain<SootClass> classes = Scene.v().getClasses();
		Iterator<SootClass> classes_iter = classes.iterator();
		while (classes_iter.hasNext()) {
			SootClass soot_class = classes_iter.next();

			if (soot_class.isApplicationClass() == false) {
				continue;
			}

			List<SootMethod> methods = soot_class.getMethods();
			
			
			for (SootMethod method : methods) {	
				if(!method.isConcrete()){
					continue;
				}
				
				JimpleBody body = (JimpleBody) method.retrieveActiveBody();
				ExceptionalUnitGraph eug = new ExceptionalUnitGraph(body);
				MyReachingDefinition mrd = new MyReachingDefinition(eug);
				
				Iterator it = body.getUnits().iterator();
				while (it.hasNext()) {
					Stmt s = (Stmt) it.next();
					if(s.containsInvokeExpr()){
						InvokeExpr invoke = s.getInvokeExpr();
						SootMethod m = invoke.getMethod();
						SootClass c = m.getDeclaringClass();
						if(!c.isApplicationClass()){
							String parameters = "(";
							for(Value v : s.getInvokeExpr().getArgs()){
								if(v instanceof Constant){
									parameters += v.toString() + "{C},";
								}else{
									
									List<String> concreteValues = new ArrayList<String>();
									String annotation = lookForConcreteValue(s, v, mrd, concreteValues, method, this.callgraph);
									
									if(!concreteValues.isEmpty()){
										if(concreteValues.size()==1){
											parameters += concreteValues.get(0) + "{" + annotation + "},";
										}else{
											parameters += "Phi(";
											for(int i=0;i<concreteValues.size()-1;i++){
												parameters += concreteValues.get(i) + ",";
											}
											parameters += concreteValues.get(concreteValues.size()-1)
													+ ")" + "{" + annotation + "},";;
										}
									}else{
										parameters += v.getType().toString() + "{" + annotation + "},";
									}
									
								}
							}
							if(parameters.endsWith(",")){
								parameters = parameters.substring(0, (parameters.length()-1));
							}
							parameters += ")";
							
							APIFlowTag tag = new APIFlowTag(m.getSignature(), parameters);
							if(s.getTags().isEmpty()){
								s.addTag(tag);
							}
						}
					}
				}
			}
		}
	}

	
	private String attach(String annotation, String typeTag){
		
		String newAnnotation = annotation;
		
		if(!newAnnotation.contains(typeTag)){
			if(newAnnotation.isEmpty()){
				newAnnotation = typeTag;
			}else{
				newAnnotation += ":" + typeTag;
			}
		}
		
		return newAnnotation;
	}
	
	private boolean intersect(List<Unit> units1, List<Unit> units2){

		for(Unit u1 : units1){
			for(Unit u2 : units2){
				if(u1==u2){
					return true;
				}
			}
		}
		
		return false;
	}
	
	private LinkedHashMap<SplitTag, SootMethod> computeSplitsAndAddTags(){
		
		//Vector<TaintTag> splitTags = new Vector<TaintTag>();
		//LinkedHashMap<SootMethod, SplitTag> splitToTag = new LinkedHashMap<SootMethod, SplitTag>();
		LinkedHashMap<SplitTag, SootMethod> tagToSplitEntry = new LinkedHashMap<SplitTag, SootMethod>();
		
		LinkedHashMap<String, String> mClassToMethod = AnalyzerMain.entryPoints;
		List<SootMethod> entry_points = new ArrayList<SootMethod>();
		
		Set<String> keySet = mClassToMethod.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {

			String mClass = keyIterator.next();
			String method = mClassToMethod.get(mClass);

			//System.out.println("building entry points:" + mClass + "|" + method);
			
			mClass = mClass.substring(0, mClass.indexOf("|"));
			
			SootClass main_soot_class = Scene.v().loadClassAndSupport(mClass);
			SootMethod sMethod = main_soot_class.getMethod(method);
			sMethod.setDeclaringClass(main_soot_class);

			//System.out.println("entry point:" + method);

			entry_points.add(sMethod);
		}
		
		LinkedHashMap<SootMethod, List<SootMethod>> entryPointGroups = groupEntryPoints(entry_points);
		
		Set<SootMethod> entryPointSet = entryPointGroups.keySet();
		Iterator<SootMethod> entryPointIter = entryPointSet.iterator();
		int group = 0;
		while(entryPointIter.hasNext()){
			SootMethod entryPoint = entryPointIter.next();
			List<SootMethod> allCallees = entryPointGroups.get(entryPoint);
			
			SplitTag tag = new SplitTag(group);
			//splitTags.add(tag);
			//splitToTag.put(entryPoint, tag);
			tagToSplitEntry.put(tag, entryPoint);
			
			if(!entryPoint.getTags().contains(tag)){
				System.out.println("add split tag[" + tag.getName() + "," + tag.getLabel() + "] to entry method " + entryPoint);
				entryPoint.addTag(tag);
				tag.setSplitEntryPoint(entryPoint.getSignature());
			}
			if(!entryPoint.isConcrete()){
				continue;
			}
			JimpleBody entryBody = (JimpleBody)entryPoint.getActiveBody();
			Iterator iter = entryBody.getUnits().iterator();
			while(iter.hasNext()){
				Stmt s = (Stmt)iter.next();
				if(s.getTags().contains(tag)){
					s.addTag(tag);
				}
			}
			
			for(SootMethod callee : allCallees){
				if(!callee.getTags().contains(tag)){
					System.out.println("add split tag[" + tag.getName() + "," + tag.getLabel() + "] to callee method " + callee);
					callee.addTag(tag);
				}
				if(!callee.isConcrete()){
					continue;
				}
				JimpleBody calleeBody = (JimpleBody)callee.getActiveBody();
				Iterator iterCallee = calleeBody.getUnits().iterator();
				while(iterCallee.hasNext()){
					Stmt s = (Stmt)iterCallee.next();
					if(s.getTags().contains(tag)){
						s.addTag(tag);
					}
				}
			}
			
			group++;
		}
		
		return tagToSplitEntry;
	}
	
	private LinkedHashMap<SootMethod, List<SootMethod>> groupEntryPoints(List<SootMethod> entryPoints){
		
		//LinkedHashMap<SootMethod, Vector<Integer>> entryPointGroups = new LinkedHashMap<SootMethod, Vector<Integer>>();
		
		LinkedHashMap<SootMethod, List<SootMethod>> entryPointToCallees = new LinkedHashMap<SootMethod, List<SootMethod>>();
		
		/*
		int entryPointCount = 0;
		for(SootMethod entryPoint : entryPoints){			
			if(!entryPointGroups.containsKey(entryPoint)){
				Vector<Integer> groupNumbers = new Vector<Integer>();
				groupNumbers.add(new Integer(entryPointCount));
				entryPointGroups.put(entryPoint, groupNumbers);
				entryPointCount++;
			}
		}
		*/
		
		for(SootMethod entryPoint : entryPoints){
			if(!entryPointToCallees.containsKey(entryPoint)){
				//List<SootMethod> callees = new ArrayList<SootMethod>();
				List<SootMethod> callees = reachableMethods(entryPoint);
				entryPointToCallees.put(entryPoint, callees);
			}
		}
		
		linkAsync(entryPointToCallees);
		linkImplicitFlow(entryPointToCallees);
		
		return entryPointToCallees;
	}
	
	private List<SootMethod> reachableMethods(SootMethod entryPoint){
		
		Stack<SootMethod> callees = new Stack<SootMethod>();
		List<SootMethod> reachables = new ArrayList<SootMethod>();
		
		callees.push(entryPoint);
		reachables.add(entryPoint);
		while(!callees.isEmpty()){
			SootMethod callee = callees.pop();
						
			Iterator targets = new Targets(cg.edgesOutOf(callee));
			while (targets.hasNext()) {
				SootMethod target = (SootMethod) targets.next();
				if(!reachables.contains(target)){
					callees.push(target);
					reachables.add(target);
				}
			}
		}		
		
		return reachables;
	}
		
	private void linkImplicitFlow(LinkedHashMap<SootMethod, List<SootMethod>> entryPointToCallees){
		
		//List<SootMethod> implicitCallers = new ArrayList<SootMethod>();
		List<SootMethod> implicitCallees = new ArrayList<SootMethod>();
		Set<SootMethod> keySet = entryPointToCallees.keySet();
		Iterator<SootMethod> iter = keySet.iterator();
		while(iter.hasNext()){
			SootMethod meth = iter.next();
			if(meth.getSubSignature().equals("void onCreate(android.os.Bundle)")){
				/*
				if(!implicitCallers.contains(meth)){
					implicitCallers.add(meth);
				}				
				*/
			}else if(meth.getSubSignature().equals("void onStart()")){
				if(!implicitCallees.contains(meth)){
					implicitCallees.add(meth);
				}
			}else if(meth.getSubSignature().equals("void onRestart()")){
				if(!implicitCallees.contains(meth)){
					implicitCallees.add(meth);
				}
			}else if(meth.getSubSignature().equals("void onResume()")){
				if(!implicitCallees.contains(meth)){
					implicitCallees.add(meth);
				}
			}else if(meth.getSubSignature().equals("void onPause()")){
				if(!implicitCallees.contains(meth)){
					implicitCallees.add(meth);
				}
			}else if(meth.getSubSignature().equals("void onStop()")){
				if(!implicitCallees.contains(meth)){
					implicitCallees.add(meth);
				}
			}else if(meth.getSubSignature().equals("void onDestroy()")){
				if(!implicitCallees.contains(meth)){
					implicitCallees.add(meth);
				}
			}
		}
		
		for(SootMethod meth : implicitCallees){
			if(meth.getDeclaringClass().declaresMethod("void onCreate(android.os.Bundle)")){
				SootMethod onCreateMethod = meth.getDeclaringClass().getMethod("void onCreate(android.os.Bundle)");
				List<SootMethod> calleesOfOnCreate = entryPointToCallees.get(onCreateMethod);
				List<SootMethod> calleesOfImplicitCallee = entryPointToCallees.get(meth);
				for(SootMethod calleeOfImplicitCallee : calleesOfImplicitCallee){
					if(!calleesOfOnCreate.contains(calleeOfImplicitCallee)){
						calleesOfOnCreate.add(calleeOfImplicitCallee);
					}
				}
				entryPointToCallees.remove(meth);
			}
		}
	}
	
	private void linkAsync(LinkedHashMap<SootMethod, List<SootMethod>> entryPointToCallees){
		
		List<SootMethod> threadEntryPoints = new ArrayList<SootMethod>();
		LinkedHashMap<SootMethod, List<SootMethod>> calleeToCallers = new LinkedHashMap<SootMethod, List<SootMethod>>();
		
		Set<SootMethod> keySet = entryPointToCallees.keySet();
		Iterator<SootMethod> iter = keySet.iterator();
		while(iter.hasNext()){
			SootMethod entryPoint = iter.next();
			boolean isAsync = false;
			if(entryPoint.getName().equals("run")){
				if(isThread(entryPoint.getDeclaringClass())){
					isAsync = true;
				}
			}else if(entryPoint.getName().equals("doInBackground")){
				if(isAsyncTask(entryPoint.getDeclaringClass())){
					isAsync = true;
				}
			}
			
			if(isAsync){
				if(!threadEntryPoints.contains(entryPoint)){
					threadEntryPoints.add(entryPoint);
				}
			}
		}
		
		Set<SootMethod> entryPointSet = entryPointToCallees.keySet();
		Iterator<SootMethod> entryPointIter = entryPointSet.iterator();
		while(entryPointIter.hasNext()){
			SootMethod entryPoint = entryPointIter.next();
			
			Stack<SootMethod> callsites = new Stack<SootMethod>();
			List<SootMethod> history = new ArrayList<SootMethod>();
			callsites.push(entryPoint);
			history.add(entryPoint);
			
			while(!callsites.isEmpty()){
				SootMethod callsite = callsites.pop();
				JimpleBody body = (JimpleBody)callsite.getActiveBody();
				Iterator insnIter = body.getUnits().iterator();
				while(insnIter.hasNext()){
					Stmt insn = (Stmt)insnIter.next();
					if(insn.containsInvokeExpr()){
						InvokeExpr invoke = insn.getInvokeExpr();
						SootMethod calleeMethod = invoke.getMethod();						
						SootClass calleeClass = calleeMethod.getDeclaringClass();
						
						if(calleeMethod.getName().equals("start")){
							Iterator targets = new Targets(cg.edgesOutOf(insn));
							while (targets.hasNext()) {
								SootMethod target = (SootMethod) targets.next();
								if(target.getDeclaringClass().isApplicationClass()){									
									if(threadEntryPoints.contains(target)){
										/*
										if(callerToCallees.containsKey(entryPoint)){
											List<SootMethod> callees = callerToCallees.get(entryPoint);
											if(!callees.contains(target)){
												callees.add(target);
											}
										}else{
											List<SootMethod> callees = new ArrayList<SootMethod>();
											callees.add(target);
											callerToCallees.put(entryPoint, callees);
										}
										*/
										if(calleeToCallers.containsKey(target)){
											List<SootMethod> callers = calleeToCallers.get(target);
											if(!callers.contains(entryPoint)){
												callers.add(entryPoint);
											}
										}else{
											List<SootMethod> callers = new ArrayList<SootMethod>();
											callers.add(entryPoint);
											calleeToCallers.put(target, callers);
										}
									}
								}
							}
						}else if (calleeMethod.getName().equals("execute")){
							for(SootMethod target : threadEntryPoints){
								if(calleeClass.equals(target.getDeclaringClass()) && target.getName().equals("doInbackground")){
									if(threadEntryPoints.contains(target)){			
										/*
										if(callerToCallees.containsKey(entryPoint)){
											List<SootMethod> callees = callerToCallees.get(entryPoint);
											if(!callees.contains(target)){
												callees.add(target);
											}
										}else{
											List<SootMethod> callees = new ArrayList<SootMethod>();
											callees.add(target);
											callerToCallees.put(entryPoint, callees);
										}
										*/
										if(calleeToCallers.containsKey(target)){
											List<SootMethod> callers = calleeToCallers.get(target);
											if(!callers.contains(entryPoint)){
												callers.add(entryPoint);
											}
										}else{
											List<SootMethod> callers = new ArrayList<SootMethod>();
											callers.add(entryPoint);
											calleeToCallers.put(target, callers);
										}
									}
								}
							}
						}else if(calleeClass.isApplicationClass()){
							Iterator targets = new Targets(cg.edgesOutOf(insn));
							while (targets.hasNext()) {
								SootMethod target = (SootMethod) targets.next();
								if(!history.contains(target)){
									callsites.push(target);
									history.add(target);
								}
							}
						}
					}
				}
			}
		}//while(entryPointIter.hasNext()){
		
		for(SootMethod threadEntryPoint : threadEntryPoints){
			List<SootMethod> callers = calleeToCallers.get(threadEntryPoint);
			Stack<SootMethod> callersStack = new Stack<SootMethod>();
			List<SootMethod> allCallers = new ArrayList<SootMethod>();
			for(SootMethod caller : callers){
				callersStack.push(caller);
				allCallers.add(caller);
			}
			
			while(!callersStack.isEmpty()){
				SootMethod caller = callersStack.pop();
				
				if(threadEntryPoints.contains(caller)){
					List<SootMethod> callersOfCaller = calleeToCallers.get(caller);
					for(SootMethod callerOfCaller : callersOfCaller){
						if(!allCallers.contains(callerOfCaller)){
							callersStack.push(callerOfCaller);
							allCallers.add(callerOfCaller);
						}
					}
				}else{					
					List<SootMethod> callees = entryPointToCallees.get(caller);
					if(!callees.contains(threadEntryPoint)){
						callees.add(threadEntryPoint);
					}
				}
			}
		}
		
		for(SootMethod threadEntryPoint : threadEntryPoints){
			entryPointToCallees.remove(threadEntryPoint);
		}
	}
	/*
	private void addDataDependencyEdge(Stmt pred, Stmt succ){
		edgeList.add(new Edge(pred, succ, false, true));	
	}
	*/
	public static void addNode(List<APIGraphNode> apiGraph, APIGraphNode node){
		apiGraph.add(node);
	}
	
	public static void deleteNode(List<APIGraphNode> apiGraph, APIGraphNode node){
		adjustEdges(node);
		removeNode(apiGraph, node);		
	}

	private static void removeNode(List<APIGraphNode> apiGraph, APIGraphNode node){
		apiGraph.remove(node);
	}
	
	private static void adjustEdges(APIGraphNode node){
		Vector<APIGraphNode> predecessors = node.getPredecessors();
		Vector<APIGraphNode> successors = node.getSuccessors();
		
		for(APIGraphNode predNode : predecessors){
			
			predNode.removeSucc(node);
			
			for(APIGraphNode succ : successors){
				if(!succ.equals(node))
					predNode.addSucc(succ);
			}
		}
		
		for(APIGraphNode succNode : successors){
			
			succNode.removePred(node);
			
			for(APIGraphNode pred : predecessors){
				if(!pred.equals(node))
					succNode.addPred(pred);
			}
		}
	}
		
//	private static void removeNonAPIorConstNode(List<APIGraphNode> apiGraph){
//		
//		List<APIGraphNode> toRemove = new ArrayList<APIGraphNode>();
//		
//		for(APIGraphNode node : apiGraph){
//			
//			Stmt s = node.getStmt();
//			
//			if(s==null){
//				System.err.println("Error: statement is null!");
//				continue;
//			}
//			
//			//here we don't keep the statement which contains string constant. for BBN
//			if((!s.getTags().contains(API_TAG)))// && (!s.getTags().contains(STRING_CONST_TAG)))
//			{
//				if(!toRemove.contains(node)){
//					toRemove.add(node);
//				}
//			}
//			
//		}
//		
//		for(APIGraphNode node : toRemove){			
//			deleteNode(apiGraph, node);
//		}
//	}
//	
//	private static void removeSpecificPackage(List<APIGraphNode> apiGraph, List<String> packageFilter){
//		List<APIGraphNode> toRemove = new ArrayList<APIGraphNode>();
//		
//		for(APIGraphNode node : apiGraph){
//			
//			SootMethod hostMethod = node.getHostMethod();
//
//			boolean needsFiltering = false;
//			for(String filter : packageFilter){
//				if(hostMethod.toString().contains(filter)){
//					needsFiltering = true;
//					break;
//				}
//			}
//			if(needsFiltering){
//				if(!toRemove.contains(node)){
//					toRemove.add(node);
//				}
//			}
//		}
//		
//		for(APIGraphNode node : toRemove){			
//			deleteNode(apiGraph, node);
//		}
//	}
//	
//	private static void removeSpecificAPINode(List<APIGraphNode> apiGraph, List<String> classFilter, List<String> classPreserveSet){
//		
//		if(MyConstants.DEBUG_INFO)
//			System.out.println("removeSpecificAPINode: classFilter: " + classFilter);
//		List<APIGraphNode> toRemove = new ArrayList<APIGraphNode>();
//		
//		for(APIGraphNode node : apiGraph){
//			
//			Stmt s = node.getStmt();
//			
//			if(s==null){
//				System.err.println("\nError: statement is null!\n");
//				continue;
//			}
//			
//			//keep the statements which have string constant, disabled in BBN project
//			//if(s.getTags().contains(STRING_CONST_TAG))
//				//continue;
//			
//			if(s.getTags().contains(API_TAG))
//			{
//				if(MyConstants.DEBUG_INFO)
//					System.out.println("removeSpecificAPINode: " + s.getInvokeExpr().getMethod().getDeclaringClass());
//				
//				/*
//				if(classFilter.contains(s.getInvokeExpr().getMethod().getDeclaringClass().toString())){
//					if(MyConstants.DEBUG_INFO)
//						System.out.println("removeSpecificAPINode: classFilter contains " + s.getInvokeExpr().getMethod().getDeclaringClass());
//					if(!toRemove.contains(node)){
//						toRemove.add(node);
//					}
//				}
//				*/
//				boolean needsPreserving = false;
//				for(String preserve : classPreserveSet){
//					if(s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains(preserve)){
//						needsPreserving = true;
//						break;
//					}
//				}
//				
//				if(!needsPreserving){
//				
//					boolean needsFiltering = false;
//					for(String filter : classFilter){
//						if(s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains(filter)){
//							needsFiltering = true;
//							break;
//						}
//					}
//					if(needsFiltering){
//						if(!toRemove.contains(node)){
//							toRemove.add(node);
//						}
//					}
//				
//				}
//			}
//			
//		}
//		
//		for(APIGraphNode node : toRemove){			
//			deleteNode(apiGraph, node);
//		}
//		
//	}
	
	private void dumpTaggedStmt(Stmt s){
		System.out.print(s + "|[");
		int count = s.getTags().size();
		for(int i=0;i<count-1;i++){
			if(s.getTags().get(i) instanceof TaintTag){
				System.out.print(((TaintTag)s.getTags().get(i)).getSecondaryName() + ",");
			}
		}
		if(s.getTags().get(count-1) instanceof TaintTag){
			System.out.println(((TaintTag)s.getTags().get(count-1)).getSecondaryName() + "]");
		}
	}
	
	private static String labelEntryComponent(SootClass clazz){
		
		while(clazz.hasSuperclass() && clazz.isApplicationClass()){
			
			for(SootClass interfaze : clazz.getInterfaces()){
				if(interfaze.getName().equals("java.lang.Runnable")){
					return "Thread";
				}
			}
			
			clazz = clazz.getSuperclass();
						
		}
				
		if(clazz.getName().equals("android.os.AsyncTask")){
			return "AsyncTask";
		}

		//android.app.Service
		if(clazz.getName().equals("android.app.Service")){
			return "Service";
		}

		//android.app.Activity
		if(clazz.getName().equals("android.app.Activity")){
			return "Activity";
		}
		
		for(SootClass interfaze : clazz.getInterfaces()){
			if(interfaze.getName().equals("java.lang.Runnable")){
				return "Thread";
			}
		}		
		
		return clazz.getName();
	}
	
	
	private static boolean isThreadOrAsyncTask(SootClass clazz){
		
		while(clazz.hasSuperclass() && clazz.isApplicationClass()){
			
			for(SootClass interfaze : clazz.getInterfaces()){
				if(interfaze.getName().equals("java.lang.Runnable")){
					return true;
				}
			}
			
			clazz = clazz.getSuperclass();
						
		}
				
		if(clazz.getName().equals("android.os.AsyncTask")){
			return true;
		}
		
		for(SootClass interfaze : clazz.getInterfaces()){
			if(interfaze.getName().equals("java.lang.Runnable")){
				return true;
			}
		}		
		
		return false;
	}
	
	private static boolean isService(SootClass clazz){
		while(clazz.hasSuperclass() && clazz.isApplicationClass()){
			clazz = clazz.getSuperclass();
		}
		
		//android.app.Service
		if(clazz.getName().equals("android.app.Service")){
			return true;
		}
		
		return false;
	}
	
	private static boolean isActivity(SootClass clazz){
		while(clazz.hasSuperclass() && clazz.isApplicationClass()){
			clazz = clazz.getSuperclass();
		}
		
		//android.app.Activity
		if(clazz.getName().equals("android.app.Activity")){
			return true;
		}
		
		return false;
	}
	
	private static String findAndLabelEntryComponent(SootMethod hostMethod){
		List<String> labels = new ArrayList<String>();
		List<SootMethod> entryMethods = new ArrayList<SootMethod>();
		//Stack<SootMethod> callstring = new Stack<SootMethod>();
		findEntryMethodsForMethodCall(hostMethod, entryMethods);
		for(SootMethod entryMethod : entryMethods){
			SootClass c = entryMethod.getDeclaringClass();
			String label = labelEntryComponent(c);
			if(!labels.contains(label)){
				labels.add(label);
			}
		}	
		
		String ret = "{";
		
		if(labels.size()>0){
			for(int i=0;i<labels.size()-1;i++){
				ret += labels.get(i) + ",";
			}
			ret += labels.get(labels.size()-1) + "}: ";
		}else{
			ret += hostMethod.getName() + "}: ";
		}
		
		return ret;
	}
	
	private static List<String> findAndLabelEntryComponentWithLabelSet(SootMethod hostMethod){
		List<String> labels = new ArrayList<String>();
		List<SootMethod> entryMethods = new ArrayList<SootMethod>();
		//Stack<SootMethod> callstring = new Stack<SootMethod>();
		findEntryMethodsForMethodCall(hostMethod, entryMethods);
		for(SootMethod entryMethod : entryMethods){
			SootClass c = entryMethod.getDeclaringClass();
			String label = labelEntryComponent(c);
			if(!labels.contains(label)){
				labels.add(label);
			}
		}			
		return labels;
	}
		
	public static void dumpDDGEdgeListToDot(List<APIGraphNode> apiGraph, String log, boolean isSuccinct){
		
		/*
		if(apiGraph.size() < 2){
			return;
		}
		*/
		LinkedHashMap<APIGraphNode, Integer> nodeToIndex = new LinkedHashMap<APIGraphNode, Integer>();
		int index = 0;
		for(APIGraphNode node : apiGraph){
			nodeToIndex.put(node, new Integer(index));
			index++;
		}
		
		Log.dumpln(log, "digraph G {");
		
		for(APIGraphNode node : apiGraph){
			int pred = nodeToIndex.get(node).intValue();
			List<APIGraphNode> succNodes = node.getSuccessors();
			for(APIGraphNode succNode : succNodes){
				String nodeSig = "";
				String succNodeSig = "";
				
				if(isSuccinct){
					
					String label = findAndLabelEntryComponent(node.getHostMethod());
					
					if(node.getStmt().containsInvokeExpr()){
						nodeSig = label + node.getStmt().getInvokeExpr().getMethod().getSignature();
					}else{
						nodeSig = label + node.getStmt().toString();
					}
					
					//nodeSig = printWithAnnotation(node.getStmt());
				}else{
					nodeSig = node.getHostMethod() + ": " + node.getStmt().toString();
				}
				
				nodeSig = nodeSig.replace("\"", "'");
				
				if(isSuccinct){
					
					String label = findAndLabelEntryComponent(node.getHostMethod());
					
					if(succNode.getStmt().containsInvokeExpr()){
						succNodeSig = label + succNode.getStmt().getInvokeExpr().getMethod().getSignature();
					}else{
						succNodeSig = label + succNode.getStmt().toString();
					}
					
					//succNodeSig = printWithAnnotation(succNode.getStmt());
				}else{
					succNodeSig = succNode.getHostMethod() + ": " + succNode.getStmt().toString();
				}
				
				succNodeSig = succNodeSig.replace("\"", "'");
				
				//Log.dumpln(AnalyzerMain.API_LOCAL_DOT, "    " + "\"" + node.getStmt().getInvokeExpr().getMethod().getSignature() + "_" + pred
						//+ "\" -> " + "\"" + succNode.getStmt().getInvokeExpr().getMethod().getSignature() + "_" + nodeToIndex.get(succNode).intValue() + "\";");
				Log.dumpln(log, "    " + "\"" + nodeSig + "/" + pred
						+ "\" -> " + "\"" + succNodeSig + "/" + nodeToIndex.get(succNode).intValue() + "\";");
			}
		}
		
		Log.dumpln(log, "}\n");
	}
	
	
	public static void printDDGEdgeList(List<APIGraphNode> apiGraph)
	{
		LinkedHashMap<APIGraphNode, Integer> nodeToIndex = new LinkedHashMap<APIGraphNode, Integer>();
		int index = 0;
		for(APIGraphNode node : apiGraph){
			nodeToIndex.put(node, new Integer(index));
			index++;
		}
		
		System.err.println("digraph G {");
		
		for(APIGraphNode node : apiGraph){
			int pred = nodeToIndex.get(node).intValue();
			List<APIGraphNode> succNodes = node.getSuccessors();
			/*
			if(succNodes.isEmpty())
			{
				String nodeSig = node.getHostMethod() + ": " + node.getStmt().toString();
				nodeSig = nodeSig.replace("\"", "'");
				System.err.println("    " + "\"" + nodeSig + "/" + pred);
			}
			*/
			
			for(APIGraphNode succNode : succNodes)
			{
				String nodeSig = "";
				String succNodeSig = "";
				
				nodeSig = node.getHostMethod() + ": " + node.getStmt().toString();
				nodeSig = nodeSig.replace("\"", "'");
				
				succNodeSig = succNode.getHostMethod() + ": " + succNode.getStmt().toString();
				succNodeSig = succNodeSig.replace("\"", "'");
				
				System.err.println("    " + "\"" + nodeSig + "/" + pred
						+ "\" -> " + "\"" + succNodeSig + "/" + nodeToIndex.get(succNode).intValue() + "\";");
			}
		}
		
		System.err.println("}\n");
	}

	
	public static void extractFeatures(List<APIGraphNode> apiGraph, String log){
		/*
		Log.dumpln(log, "@relation app");
		
		Log.dumpln(log);
		
		Log.dumpln(log, "@attribute predperm {TRUE, FALSE}");
		Log.dumpln(log, "@attribute background {TRUE, FALSE}");
		Log.dumpln(log, "@attribute constparam {TRUE, FALSE}");
		Log.dumpln(log, "@attribute malicious {yes, no}");
		
		Log.dumpln(log);
		
		Log.dumpln(log, "@data");
		*/
				
		String[] attributes = new String[MyConstants.NUM_FEATURES];
		
		boolean isBackground = false;
		boolean isPredecessorPermissionDependent = false;
		boolean isParameterConstant = false;
		
		for(APIGraphNode node : apiGraph){
			
			if(node.getPredecessors().isEmpty()){
				List<String> labels = findAndLabelEntryComponentWithLabelSet(node.getHostMethod());
				if(labels.contains("Thread") || labels.contains("Service") || labels.contains("AsyncTask")){
					isBackground = true;
				}
			}
			
			if(!node.getSuccessors().isEmpty()){
				Stmt s = node.getStmt();
				if(s.containsInvokeExpr()){
					SootMethod m = s.getInvokeExpr().getMethod();
					String sig = m.getSignature();
					String apmSig = m.getDeclaringClass().toString() + "." + m.getName();// + sig.substring(sig.indexOf('('), sig.indexOf(')')+1);
					if(MyConstants.DEBUG_INFO)
						System.out.println("apmSig: " + apmSig);
//					if(apm.exists(apmSig)){
//						isPredecessorPermissionDependent = true;
//					}
				}
			}
			
			if(node.getSuccessors().isEmpty()){
				Stmt s = node.getStmt();
				if(s.containsInvokeExpr()){
					List<ValueBox> useBoxes = s.getUseBoxes();
					for(ValueBox vb : useBoxes){
						if(vb.getValue() instanceof StringConstant || vb.getValue() instanceof NumericConstant){
							isParameterConstant = true;
						}
					}
				}
			}
		}
		
		if(isPredecessorPermissionDependent){
			attributes[MyConstants.ATTR_PREDECESSOR_IS_PERMISSION_DEPENDENT] = "TRUE";
		}else{
			attributes[MyConstants.ATTR_PREDECESSOR_IS_PERMISSION_DEPENDENT] = "FALSE";
		}
		
		if(isBackground){
			attributes[MyConstants.ATTR_ENTRY_POINT_IS_IN_BACKGROUND] = "TRUE";
		}else{
			attributes[MyConstants.ATTR_ENTRY_POINT_IS_IN_BACKGROUND] = "FALSE";
		}		
		
		if(isParameterConstant){
			attributes[MyConstants.ATTR_PARAMETER_IS_CONSTANT] = "TRUE";
		}else{
			attributes[MyConstants.ATTR_PARAMETER_IS_CONSTANT] = "FALSE";
		}
		
		if(AnalyzerMain.CLASSPATH.contains("malware")){
			attributes[MyConstants.ATTR_IS_MALICIOUS] = "yes";
		}else{
			attributes[MyConstants.ATTR_IS_MALICIOUS] = "no";
		}
		
		Log.dumpln(log, attributes[MyConstants.ATTR_PREDECESSOR_IS_PERMISSION_DEPENDENT] + "," 
				+ attributes[MyConstants.ATTR_ENTRY_POINT_IS_IN_BACKGROUND] + "," 
				+ attributes[MyConstants.ATTR_PARAMETER_IS_CONSTANT] + ","
				+ attributes[MyConstants.ATTR_IS_MALICIOUS]);
	}
	
	private static String printWithAnnotation(Stmt s){
		
		if(!s.containsInvokeExpr()){
			return s.toString();
		}	
		
		InvokeExpr invoke = s.getInvokeExpr();
		SootMethod m = invoke.getMethod();
		SootClass c = m.getDeclaringClass();
		
		if(c.isApplicationClass()){
			return s.toString();
		}
		
		String annotatedSig = m.getName();
		
		List<Tag> tags = s.getTags();
		if(tags.isEmpty()){
			System.out.println("ERROR:No tag!");
			annotatedSig = m.getSignature();
		}else{
			Tag tag0 = tags.get(0);
			if(tag0 instanceof APIFlowTag){
				annotatedSig += ((APIFlowTag)(tags.get(0))).getAnnotation();
			}else{
				System.out.println("ERROR:No APIFlowTag! Annotation is missing!");
				annotatedSig = m.getSignature();
			}
		}
		
		return annotatedSig;
	}
	
	private static void findEntryMethodsForMethodCall(SootMethod call, List<SootMethod> entryMethods){
		Set<SootMethod> keySet = ClassificationFeatureExtraction.splitEntryPointToMethodsMap.keySet();
		Iterator<SootMethod> iter = keySet.iterator();
		while(iter.hasNext()){
			SootMethod entryPoint = iter.next();
			List<SootMethod> callees = ClassificationFeatureExtraction.splitEntryPointToMethodsMap.get(entryPoint);
			if(callees.contains(call)){
				if(!entryMethods.contains(entryPoint)){
					entryMethods.add(entryPoint);
				}
			}
		}
	}
	/*
	private static void findEntryMethodsForMethodCall(SootMethod call, List<SootMethod> entryMethods){
				
		Chain<SootClass> classes = Scene.v().getClasses();
		Iterator<SootClass> classes_iter = classes.iterator();
		while (classes_iter.hasNext()) {
			SootClass soot_class = classes_iter.next();

			if (soot_class.isApplicationClass() == false) {
				continue;
			}

			List<SootMethod> methods = soot_class.getMethods();			
			
			for (SootMethod method : methods) {	
				if(!method.isConcrete()){
					continue;
				}
				
				JimpleBody body = (JimpleBody) method.retrieveActiveBody();
				
				Iterator it = body.getUnits().iterator();
				while (it.hasNext()) {
					Stmt callerStmt = (Stmt) it.next();
					if(callerStmt.containsInvokeExpr()){
						
						if(recursiveCallsites.contains(callerStmt)){
							if(MyConstants.DEBUG_INFO)
								System.out.println("RECURSIVE call found: " + callerStmt + ", continue with the next one");
							continue;
						}
						
						SootMethod calleeMethod = callerStmt.getInvokeExpr().getMethod();
						if(calleeMethod.isConcrete()){
							if(calleeMethod.equals(call)){
								if(Scene.v().getEntryPoints().contains(method)){
									if(!entryMethods.contains(method)){
										entryMethods.add(method);
									}									
								}else{
									findEntryMethodsForMethodCall(method, entryMethods);
								}								
							}
						}else{
							Iterator targets = new Targets(call_graph.edgesOutOf(callerStmt));													

							while (targets.hasNext()) {
								SootMethod target = (SootMethod) targets.next();
								//System.out.println(method + " may call " + target);
								if(target.equals(call)){
									if(Scene.v().getEntryPoints().contains(method)){
										if(!entryMethods.contains(method)){
											entryMethods.add(method);
										}
									}else{
										findEntryMethodsForMethodCall(method, entryMethods);
									}								
								}														
							}
						} 
					}
				}
			}
		}
		
	}
	*/
	
	/*
	private static void detectRecursiveCallsites(List<SootMethod> entryPoints, List<Stmt> recursiveCallsites){
		//List<Stmt> recursiveCallsites = new ArrayList<Stmt>();
		for(SootMethod entryPoint : entryPoints){
			Stack<SootMethod> callstring = new Stack<SootMethod>();
			callstring.push(entryPoint);
			buildCallString(entryPoint, callstring, recursiveCallsites);
			callstring.pop();
		}
		
		//return recursiveCallsites;
	}
	*/
	
	
	
}
