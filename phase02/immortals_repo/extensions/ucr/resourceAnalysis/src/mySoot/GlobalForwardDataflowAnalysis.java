package mySoot;

import mySoot.util.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;
import soot.util.HashChain;
import soot.jimple.*;
import soot.*;
import soot.jimple.internal.*;
import soot.tagkit.*;

public class GlobalForwardDataflowAnalysis extends SceneTransformer {	

	private CallGraph cg;
	private CallGraph callgraph;
	public static CallGraph call_graph;
	private static PointsToAnalysis pta;
	private ClassificationFeatureExtraction cfe;

	private String DDG_GLOBAL_DOT;
	private String DDG_GLOBAL_SUCCINCT_DOT;

	static LinkedHashMap<String, TaintTag> taintTagMap = new LinkedHashMap<String, TaintTag>();
	static LinkedHashMap<String, TaintTag> extraDefTagMap = new LinkedHashMap<String, TaintTag>();
	static LinkedHashMap<TaintTag, String> taintTagReverseMap = new LinkedHashMap<TaintTag, String>();
	static LinkedHashMap<TaintTag, String> extraDefTagReverseMap = new LinkedHashMap<TaintTag, String>();

	static TaintTag generalTaintTag = new TaintTag(1);
	static TaintTag instrumentationTag = new TaintTag(2);
	static TaintTag generalExtraDefTag = new TaintTag(3);
	static TaintTag wrapperBeginTag = new TaintTag(4);
	static TaintTag wrapperEndTag = new TaintTag(5);
	static TaintTag invokeWrapperTag = new TaintTag(6);
	static TaintTag beforeWrapperTag = new TaintTag(7);
	static TaintTag afterWrapperTag = new TaintTag(8);
	static TaintTag initTaintTag = new TaintTag(9);
	static TaintTag checkTaintTag = new TaintTag(10);
	static TaintTag referenceTag = new TaintTag(11);
	static TaintTag initialLocalTag = new TaintTag(12);

	static TaintTag isWrapperTag = new TaintTag(126);
	static TaintTag wrapperMethodTag = new TaintTag(127);
	static TaintTag referenceRelatedTag = new TaintTag(128);
	static TaintTag taintStaticTag = new TaintTag(129);
	static TaintTag equivTag = new TaintTag(130);

	static TaintTag debuggingTag = new TaintTag(1023);

	static TaintTag functionPreservingTag = new TaintTag(1024, "functionPreservingTag");

	public static TaintTag API_TAG = new TaintTag(0xffff, "API_TAG");
	public static TaintTag STRING_CONST_TAG = new TaintTag(0xfffe, "STRING_CONST_TAG");

	private static LinkedHashMap<SootField, Vector<Integer>> usedStaticFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();
	private static LinkedHashMap<SootField, Vector<Integer>> usedInstanceFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();
	private static int sourceCount = 0;

	private static LinkedHashMap<SootMethod, Vector<Integer>> sMethodsWithSources = new LinkedHashMap<SootMethod, Vector<Integer>>();

	//private static List<SootField> taintedFields = new ArrayList<SootField>();
	private static List<SootField> taintedFieldsInCallee = new ArrayList<SootField>();
	private static List<SootField> taintedFieldsInCaller = new ArrayList<SootField>();
	
	//private static LinkedHashMap<SootField, PointsToSet> taintedFieldToPTSMapInCallee = new LinkedHashMap<SootField, PointsToSet>();
	//private static LinkedHashMap<SootField, PointsToSet> taintedFieldToPTSMapInCaller = new LinkedHashMap<SootField, PointsToSet>();
	
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

	private LinkedHashMap<SootMethod, List<APIGraphNode>> methodToDDGMap = new LinkedHashMap<SootMethod, List<APIGraphNode>>();

	public List<APIGraphNode> apiDDGGraph = new ArrayList<APIGraphNode>();
	private LinkedHashMap<Stmt, APIGraphNode> stmtToNodeMap = new LinkedHashMap<Stmt, APIGraphNode>();

	private LinkedHashMap<SootField, List<Stmt>> fieldToDefsMap = new LinkedHashMap<SootField, List<Stmt>>();
	private LinkedHashMap<SootField, List<Stmt>> fieldToUsesMap = new LinkedHashMap<SootField, List<Stmt>>();
	
	//private static LinkedHashMap<SootMethod, List<SootMethod>> entryPointToMethodsMap = new LinkedHashMap<SootMethod, List<SootMethod>>();
	//private static LinkedHashMap<SootMethod, List<SootMethod>> splitEntryPointToMethodsMap = new LinkedHashMap<SootMethod, List<SootMethod>>();
	private static LinkedHashMap<SplitTag, SootMethod> tagToSplitEntry = new LinkedHashMap<SplitTag, SootMethod>();
	
	public static int NODE_COUNT = 0;
	public static int API_NODE_COUNT = 0;

	public GlobalForwardDataflowAnalysis(){
		LinkedHashMap<String, Integer> sources = AnalyzerMain.sources;
		//LinkedHashMap<String, Integer> sinks = AnalyzerMain.sinks;

		Set<String> srcKeySet = sources.keySet();
		Iterator<String> srcIter = srcKeySet.iterator();
		while(srcIter.hasNext()){
			String src = srcIter.next();
			if(!taintTagMap.containsKey(src)){			
				TaintTag tag = new TaintTag(sources.get(src).intValue());
				taintTagMap.put(src, tag);
				taintTagReverseMap.put(tag, src);
			}
			if(!extraDefTagMap.containsKey(src)){			
				TaintTag tag = new TaintTag(sources.get(src).intValue());
				extraDefTagMap.put(src, tag);
				extraDefTagReverseMap.put(tag, src);
			}
			sourceCount++;
		}

		DDG_GLOBAL_DOT = AnalyzerMain.DDG_GLOBAL_DOT;
		DDG_GLOBAL_SUCCINCT_DOT = AnalyzerMain.DDG_GLOBAL_SUCCINCT_DOT;
	}


	@Override
	protected void internalTransform(String string, Map map) {
		
		this.cg = Scene.v().getCallGraph();
		this.callgraph = cg;
		call_graph = cg;
		
		System.out.println("call graph size: " + call_graph.size());
		
		pta = Scene.v().getPointsToAnalysis();
		
		//buildEntryPointToMethodsMap(Scene.v().getEntryPoints());
		//computeSplitsAndAddTags(Scene.v().getEntryPoints());

		doDataFlowAnalysis();

		Set<Stmt> sKeySet = this.stmtToNodeMap.keySet();
		Iterator<Stmt> sIter = sKeySet.iterator();
		while(sIter.hasNext()){
			Stmt s = sIter.next();
			APIGraphNode node = this.stmtToNodeMap.get(s);
			this.apiDDGGraph.add(node);
		}
		
		/*
		removeNonAPIorConstNode(this.apiDDGGraph);

		List<String> classFilter = new ArrayList<String>();
		//classFilter.add("java.lang.String");
		//classFilter.add("java.lang.StringBuilder");
		
		classFilter.add("java.");
		classFilter.add("javax.");
		classFilter.add("org.");

		List<String> classPreserveSet = new ArrayList<String>();
		classPreserveSet.add("java.lang.Runtime");
		classPreserveSet.add("java.lang.reflect.Method");
		classPreserveSet.add("java.lang.ClassLoader");
		classPreserveSet.add("java.lang.String");
		classPreserveSet.add("java.lang.StringBuilder");
		classPreserveSet.add("java.lang.StringBuffer");
		classPreserveSet.add("java.io.");
		classPreserveSet.add("org.apache.http.client.");
		classPreserveSet.add("org.apache.http.impl.client.");
		classPreserveSet.add("java.net.");

		removeSpecificAPINode(this.apiDDGGraph, classFilter, classPreserveSet);
		*/
		
		List<String> packageFilter = new ArrayList<String>();		
		for(String adsPackage : MyConstants.AdLibs){
			packageFilter.add(adsPackage);
		}
		removeSpecificPackage(this.apiDDGGraph, packageFilter);
		
		ClassificationFeatureExtraction.buildEntryPointToMethodsMap(Scene.v().getEntryPoints(), false);

		/*
		//detectRecursiveCallsites(Scene.v().getEntryPoints(), recursiveCallsites);

		cfe = new ClassificationFeatureExtraction(this.apiDDGGraph);
		cfe.FeatureExtraction();
		
		File directory = new File(".");
		String pwd = "";
		try{
			pwd = directory.getAbsolutePath();
		}catch(Exception e){
			System.out.println(e.getMessage());
			System.err.println("java -Xss4m -Xmx1024m -cp $JASMIN:$SOOTCLASS:$POLYGLOT:$ANDROID:. mySoot.AnalyzerMain $CURR_DIR/apks/$FILENAME > $OUTPUT");
			System.exit(-1);
		}
		
		String featureFile = pwd + "/../featureList";
		File featureSet = new File(featureFile);
		
		try 
		{
			if (!featureSet.exists()) 
			{
				featureSet.createNewFile();
			}
	
			FileWriter fw;
			fw = new FileWriter(featureSet.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);

			Set<String> features = ClassificationFeatureExtraction.features.keySet();
			for(String feature: features)
			{
				bw.write(feature);
				bw.write("\n" + ClassificationFeatureExtraction.features.get(feature));
				bw.write("\n\n");
			}
			
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		dumpDDGEdgeListToDot(this.apiDDGGraph, DDG_GLOBAL_DOT, false);
		//dumpDDGEdgeListToDot(this.apiDDGGraph, DDG_GLOBAL_SUCCINCT_DOT, true);
		//AnalyzerMain.dumpDDGtoStructure(this.apiDDGGraph, true);

	}


	private void doDataFlowAnalysis() {
		
		//AndroidFunctionSummary.buildFunctionSummary();

		Set<String> sourceKey = AnalyzerMain.sourcesLocationMap.keySet();
		Iterator<String> sourceIter = sourceKey.iterator();
		while(sourceIter.hasNext()){

			String leakSource = sourceIter.next();
			
			LinkedHashMap<String, String> entryPointsString = AnalyzerMain.sourcesLocationMap.get(leakSource);
			//System.err.println("xjtu leakSource: " + leakSource);
			//System.err.println("xjtu entryPointsString: " + entryPointsString);
			//System.err.println();
			
			TaintTag taintTag = taintTagMap.get(leakSource);
			TaintTag extraDefTag = extraDefTagMap.get(leakSource);

			if(MyConstants.DEBUG_INFO){
				System.out.println("dataflow analysis");

				System.out.println("loading function summaries");
			}
			

			List<SootMethod> entryPoints = new ArrayList<SootMethod>();
			//List<SootField> instanceFields = new ArrayList<SootField>();
			//List<SootField> staticFields = new ArrayList<SootField>();

			LinkedHashMap<SootField, Vector<Integer>> instanceFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();
			LinkedHashMap<SootField, Vector<Integer>> staticFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();

			List<SootField> taintedFields = new ArrayList<SootField>();
			//LinkedHashMap<SootField, PointsToSet> taintedFieldToPTSMap = new LinkedHashMap<SootField, PointsToSet>();

			//set entry points for dataflow
			Set<String> keySet = entryPointsString.keySet();
			Iterator<String> keyIterator = keySet.iterator();
			while (keyIterator.hasNext()) {
				String mClass = keyIterator.next();
				String method = entryPointsString.get(mClass);

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

			// If dataflow reaches a "return", we put caller name into sourceMethods. 
			// Further we track the dataflow in caller, starting from such function call.
			List<SootMethod> sourceMethods = new ArrayList<SootMethod>();

			boolean breakAnalysis = false;
			//dataflow analysis phase one
			while(!worklist.isEmpty()){
				
				if(MyConstants.TO_CONSIDER_LIMIT){
					if(API_NODE_COUNT > MyConstants.MAX_APINODES_CONSIDERED){
						//NODE_COUNT = 0;
						API_NODE_COUNT = 0;
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
				// System.out.println(sMethod.getSource());
				// System.out.println(sMethod.getBytecodeParms());

				JimpleBody body = (JimpleBody) sMethod.retrieveActiveBody();
				ExceptionalUnitGraph eug = new ExceptionalUnitGraph(body);

				/*
				if(CFG==true){
					System.out.println("BEGIN [CFG] - " + sMethod + "\n");
					System.out.println(eug.toString());
					System.out.println("END [CFG]");
				}
				 */

				MyReachingDefinition mrd = new MyReachingDefinition(eug);

				/*
				if(REACH_DEF==true){
					System.out.println("BEGIN [Reaching Definition Analysis] - " + sMethod + "\n");
					dumpReachingDefs(mrd, body);
					System.out.println("END [Reaching Definition Analysis]");
				}
				 */

				//Stack<Stmt> defsStack = new Stack<Stmt>();
				//Vector<Stmt> taintedRefDefs = new Vector<Stmt>();
				Stack<DefWithScope> defsStack = new Stack<DefWithScope>();
				Vector<DefWithScope> taintedRefDefs = new Vector<DefWithScope>();

				//Vector<Stmt> defs = new Vector<Stmt>();
				//Vector<DefWithScope> defs = new Vector<DefWithScope>();

				//hashmap to record all analyzed definitions. key is definition, object is the scope(s).
				LinkedHashMap<Stmt, Vector<Stmt>> defs = new LinkedHashMap<Stmt, Vector<Stmt>>();

				Stmt source = null;

				//identify "sources" in current method
				{
					//Step 1: Check if current method is a callback source
					if(sMethod.getSubSignature().equals(leakSource)
							&& sMethod.getDeclaringClass().implementsInterface(AndroidSourceSinkSummary.callbackSourceSubSignatureMap.get(leakSource)))
					{
						List<Integer> sourceIndexes = AndroidSourceSinkSummary.callbackSourceSubSummary.get(leakSource);
						
						Iterator iterIdentity = body.getUnits().iterator();
						while(iterIdentity.hasNext()){
							Stmt s = (Stmt)iterIdentity.next();
							if(s instanceof IdentityStmt){
								Value rhs = ((IdentityStmt) s).getRightOp();
								if(rhs instanceof ThisRef){
									if(sourceIndexes.contains(new Integer(MyConstants.thisObject))){
										DefWithScope sWS = new DefWithScope(s);
										if(!defs.containsKey(s)){
											defs.put(s, new Vector<Stmt>());
											defsStack.push(sWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
											source = s;
											
											APIGraphNode srcNode = null;
											if(!stmtToNodeMap.containsKey(s)){
												srcNode = new APIGraphNode(s, sMethod);
												stmtToNodeMap.put(s, srcNode);
												NODE_COUNT++;
												if(isAndroidAPICall(s)){
													API_NODE_COUNT++;
												}
											}else{
												srcNode = stmtToNodeMap.get(s);
											}
											if(!methodToDDGMap.get(sMethod).contains(srcNode)){
												methodToDDGMap.get(sMethod).add(srcNode);
											}
											
											APIGraphNode callbackNode = null;
											
							    			List<Type> paramTypes = sMethod.getParameterTypes();							    			
							    			List<Value> paramValues = new ArrayList<Value>();
							    			for(Type t : paramTypes){
							    							
							    				Value param = null;
							    				if(t instanceof RefLikeType){
							    					param = NullConstant.v();
							    				}else{
							    					if(t instanceof PrimType){
							    						if(t instanceof LongType){
							    							param = LongConstant.v(0);
							    						}else if(t instanceof DoubleType){
							    							param = DoubleConstant.v(0);
							    						}else if(t instanceof FloatType){
							    							param = FloatConstant.v(0);
							    						}else{
							    							param = IntConstant.v(0);
							    						}
							    					}else{
							    						System.out.println("ERROR: type not handled - " + t.toString());
							    					}
							    				}							    				
							    				paramValues.add(param);							    				
							    			}
							    			
							    			Local ref = Jimple.v().newLocal("SYSTEM", RefType.v(sMethod.getDeclaringClass()));
											Stmt callback = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(ref, sMethod.makeRef(), paramValues));
											
											AddTags(callback, API_TAG);
											
											if(!stmtToNodeMap.containsKey(callback)){
												callbackNode = new APIGraphNode((Stmt)callback, sMethod);
												stmtToNodeMap.put((Stmt)callback, callbackNode);
												NODE_COUNT++;
												if(isAndroidAPICall((Stmt)callback)){
													API_NODE_COUNT++;
												}
											}else{
												callbackNode = stmtToNodeMap.get((Stmt)callback);
											}
											if(!methodToDDGMap.get(sMethod).contains(callbackNode)){
												methodToDDGMap.get(sMethod).add(callbackNode);
											}
											
											callbackNode.addSucc(srcNode);
											srcNode.addPred(callbackNode);
										}
									}
								}else if(rhs instanceof ParameterRef){
									if(sourceIndexes.contains(new Integer(((ParameterRef)rhs).getIndex()))){
										DefWithScope sWS = new DefWithScope(s);
										if(!defs.containsKey(s)){
											defs.put(s, new Vector<Stmt>());
											defsStack.push(sWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
											source = s;
											
											APIGraphNode srcNode = null;
											if(!stmtToNodeMap.containsKey(s)){
												srcNode = new APIGraphNode(s, sMethod);
												stmtToNodeMap.put(s, srcNode);
												NODE_COUNT++;
												if(isAndroidAPICall(s)){
													API_NODE_COUNT++;
												}
											}else{
												srcNode = stmtToNodeMap.get(s);
											}
											if(!methodToDDGMap.get(sMethod).contains(srcNode)){
												methodToDDGMap.get(sMethod).add(srcNode);
											}
											
											APIGraphNode callbackNode = null;
											
							    			List<Type> paramTypes = sMethod.getParameterTypes();							    			
							    			List<Value> paramValues = new ArrayList<Value>();
							    			for(Type t : paramTypes){
							    							
							    				Value param = null;
							    				if(t instanceof RefLikeType){
							    					param = NullConstant.v();
							    				}else{
							    					if(t instanceof PrimType){
							    						if(t instanceof LongType){
							    							param = LongConstant.v(0);
							    						}else if(t instanceof DoubleType){
							    							param = DoubleConstant.v(0);
							    						}else if(t instanceof FloatType){
							    							param = FloatConstant.v(0);
							    						}else{
							    							param = IntConstant.v(0);
							    						}
							    					}else{
							    						System.out.println("ERROR: type not handled - " + t.toString());
							    					}
							    				}							    				
							    				paramValues.add(param);							    				
							    			}
							    			
							    			Local ref = Jimple.v().newLocal("SYSTEM", RefType.v(sMethod.getDeclaringClass()));
											Stmt callback = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(ref, sMethod.makeRef(), paramValues));
											
											AddTags(callback, API_TAG);
											
											if(!stmtToNodeMap.containsKey(callback)){
												callbackNode = new APIGraphNode((Stmt)callback, sMethod);
												stmtToNodeMap.put((Stmt)callback, callbackNode);
												NODE_COUNT++;
												if(isAndroidAPICall((Stmt)callback)){
													API_NODE_COUNT++;
												}
											}else{
												callbackNode = stmtToNodeMap.get((Stmt)callback);
											}
											if(!methodToDDGMap.get(sMethod).contains(callbackNode)){
												methodToDDGMap.get(sMethod).add(callbackNode);
											}
											
											callbackNode.addSucc(srcNode);
											srcNode.addPred(callbackNode);
										}
									}
								}
							}
						}					
					}						
					
					//Step 2: Check if current method contains function call to any source
					Iterator it = body.getUnits().iterator();
					while (it.hasNext()) {
						Stmt s = (Stmt) it.next();

//						if(s instanceof DefinitionStmt)
//						{
//							Value lhs = ((DefinitionStmt) s).getLeftOp();
//							
//							if(lhs instanceof Local)
//							{
//								System.err.println("statement: " + s.toString());
//								System.err.println("left op: " + lhs.toString());
//							}
//						}
						/*
						if(s instanceof DefinitionStmt){
							Value rhs = ((DefinitionStmt) s).getRightOp();
							if(rhs instanceof InstanceFieldRef){
								if(((InstanceFieldRef) rhs).getField().getSignature().equals(leakSource)){

									DefWithScope sWS = new DefWithScope(s);
									if(!defs.containsKey(s)){
										defs.put(s, new Vector<Stmt>());
										defsStack.push(sWS);
										if(MyConstants.DEBUG_INFO)
											System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
										source = s;
									}
								}
							}
						}
						*/

						Iterator useIt = s.getUseBoxes().iterator();
						while (useIt.hasNext()) {
							ValueBox vBox = (ValueBox) useIt.next();
							if (vBox.getValue() instanceof InvokeExpr) {
								SootMethod m = s.getInvokeExpr().getMethod();
								
								if (((InvokeExpr) vBox.getValue()).getMethod().getSignature().equals(leakSource)) {
									
									if(AndroidSourceSinkSummary.isNonCallbackSource(leakSource)){
										List<Integer> sourceIndexes = AndroidSourceSinkSummary.sourceSummary.get(leakSource);
										for(Integer sourceIndex : sourceIndexes){
											if(sourceIndex.intValue() == MyConstants.returnValue){
												DefWithScope sWS = new DefWithScope(s);
												if(!defs.containsKey(s)){
													defs.put(s, new Vector<Stmt>());
													defsStack.push(sWS);
													if(MyConstants.DEBUG_INFO)
														System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
													source = s;
												}
											}else if(sourceIndex.intValue() == MyConstants.thisObject){
												InvokeExpr invoke = s.getInvokeExpr();
												if(invoke instanceof VirtualInvokeExpr){
													Value base = ((VirtualInvokeExpr)invoke).getBase();
													if(base instanceof Local){
														List<Unit> defines = mrd.getDefsOfAt((Local)base, s);
														for(Unit define : defines){
															DefWithScope defineWS = new DefWithScope((Stmt)define,s);
															if(!defs.containsKey((Stmt)define)){
																Vector<Stmt> scopes = new Vector<Stmt>();
																scopes.add(s);
																defs.put((Stmt)define, scopes);
																defsStack.push(defineWS);
																if(MyConstants.DEBUG_INFO)
																	System.out.println("def stack doesn't contain " + defineWS.dump() + ". Push it.");
																source = s;
																
																AddTags(s, API_TAG);

																APIGraphNode srcNode = null;
																if(!stmtToNodeMap.containsKey(s)){
																	srcNode = new APIGraphNode(s, sMethod);
																	stmtToNodeMap.put(s, srcNode);
																	NODE_COUNT++;
																	if(isAndroidAPICall(s)){
																		API_NODE_COUNT++;
																	}
																}else{
																	srcNode = stmtToNodeMap.get(s);
																}
																if(!methodToDDGMap.get(sMethod).contains(srcNode)){
																	methodToDDGMap.get(sMethod).add(srcNode);
																}
																
																APIGraphNode defineNode = null;
																if(!stmtToNodeMap.containsKey(define)){
																	defineNode = new APIGraphNode((Stmt)define, sMethod);
																	stmtToNodeMap.put((Stmt)define, defineNode);
																	NODE_COUNT++;
																	if(isAndroidAPICall((Stmt)define)){
																		API_NODE_COUNT++;
																	}
																}else{
																	defineNode = stmtToNodeMap.get((Stmt)define);
																}
																if(!methodToDDGMap.get(sMethod).contains(defineNode)){
																	methodToDDGMap.get(sMethod).add(defineNode);
																}
																
																srcNode.addSucc(defineNode);
																defineNode.addPred(srcNode);
															}
														}
													}
												}
											}else if(sourceIndex.intValue() >= 0){
												
												if(s.getInvokeExpr().getArgCount()-1 < sourceIndex.intValue()){
													System.err.println("ERROR: method has less parameters. [" + s.getInvokeExpr().getMethod() + "]");
												}
												
												Value param = s.getInvokeExpr().getArg(sourceIndex.intValue());
												if(param instanceof Local){
													List<Unit> defines = mrd.getDefsOfAt((Local)param, s);
													for(Unit define : defines){
														DefWithScope defineWS = new DefWithScope((Stmt)define,s);
														if(!defs.containsKey((Stmt)define)){
															Vector<Stmt> scopes = new Vector<Stmt>();
															scopes.add(s);
															defs.put((Stmt)define, scopes);
															defsStack.push(defineWS);
															if(MyConstants.DEBUG_INFO)
																System.out.println("def stack doesn't contain " + defineWS.dump() + ". Push it.");
															source = s;
															
															AddTags(s, API_TAG);
															
															APIGraphNode srcNode = null;
															if(!stmtToNodeMap.containsKey(s)){
																srcNode = new APIGraphNode(s, sMethod);
																stmtToNodeMap.put(s, srcNode);
																NODE_COUNT++;
																if(isAndroidAPICall(s)){
																	API_NODE_COUNT++;
																}
															}else{
																srcNode = stmtToNodeMap.get(s);
															}
															if(!methodToDDGMap.get(sMethod).contains(srcNode)){
																methodToDDGMap.get(sMethod).add(srcNode);
															}
															
															APIGraphNode defineNode = null;
															if(!stmtToNodeMap.containsKey(define)){
																defineNode = new APIGraphNode((Stmt)define, sMethod);
																stmtToNodeMap.put((Stmt)define, defineNode);
																NODE_COUNT++;
																if(isAndroidAPICall((Stmt)define)){
																	API_NODE_COUNT++;
																}
															}else{
																defineNode = stmtToNodeMap.get((Stmt)define);
															}
															if(!methodToDDGMap.get(sMethod).contains(defineNode)){
																methodToDDGMap.get(sMethod).add(defineNode);
															}
															
															srcNode.addSucc(defineNode);
															defineNode.addPred(srcNode);
														}
													}
												}
											}
										}
									}
									
									/*
									if(m.getDeclaringClass().getName().equals("android.media.AudioRecord") && m.getName().equals("read")){
										Value firstParam = s.getInvokeExpr().getArg(0);
										if(firstParam instanceof Local){
											List<Unit> defines = mrd.getDefsOfAt((Local)firstParam, s);
											for(Unit define : defines){
												DefWithScope defineWS = new DefWithScope((Stmt)define);
												if(!defs.containsKey((Stmt)define)){
													Vector<Stmt> scopes = new Vector<Stmt>();
													scopes.add(s);
													defs.put((Stmt)define, scopes);
													defsStack.push(defineWS);
													if(MyConstants.DEBUG_INFO)
														System.out.println("def stack doesn't contain " + defineWS.dump() + ". Push it.");
													source = s;
												}
											}
										}
									}
									else if(m.getDeclaringClass().getName().equals("android.hardware.Camera.PictureCallback") && m.getName().equals("onPictureTaken")){
										Value firstParam = s.getInvokeExpr().getArg(0);
										if(firstParam instanceof Local){
											List<Unit> defines = mrd.getDefsOfAt((Local)firstParam, s);
											for(Unit define : defines){
												DefWithScope defineWS = new DefWithScope((Stmt)define);
												if(!defs.containsKey((Stmt)define)){
													Vector<Stmt> scopes = new Vector<Stmt>();
													scopes.add(s);
													defs.put((Stmt)define, scopes);
													defsStack.push(defineWS);
													if(MyConstants.DEBUG_INFO)
														System.out.println("def stack doesn't contain " + defineWS.dump() + ". Push it.");
													source = s;
												}
											}
										}
									}
									else
									{	
										DefWithScope sWS = new DefWithScope(s);
										if(!defs.containsKey(s)){
											defs.put(s, new Vector<Stmt>());
											defsStack.push(sWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
											source = s;
										}
									}
									*/

								}else if(sourceMethods.contains(((InvokeExpr) vBox.getValue()).getMethod())){
									
									DefWithScope sWS = new DefWithScope(s);
									if(!defs.containsKey(s)){
										defs.put(s, new Vector<Stmt>());
										defsStack.push(sWS);
										if(MyConstants.DEBUG_INFO)
											System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
										source = s;
									}
								}
							}
						}
					}
				}
 
				while(!defsStack.isEmpty()){
					
					if(MyConstants.TO_CONSIDER_LIMIT){
						if(NODE_COUNT > MyConstants.MAX_NODES_CONSIDERED || API_NODE_COUNT > MyConstants.MAX_APINODES_CONSIDERED){
							break;
						}
					}

					DefWithScope defWS = defsStack.pop();
					if(MyConstants.DEBUG_INFO)
						System.out.println("POP from def stack: " + defWS.dump());

					/*
					if(hasEquivTable){

						if(MyConstants.DEBUG_INFO)
							System.out.println(sMethod + "has equivTable: " + equivTable);
						if(equivTable.containsKey(defWS.getDef())){

							List<Stmt> equivs = equivTable.get(defWS.getDef());

							if(MyConstants.DEBUG_INFO)
								System.out.println("EQUIV found: " + defWS.getDef() + "|" + equivs);

							for(Stmt equiv : equivs){
								DefWithScope equivWS = new DefWithScope(equiv);
								if (!defs.containsKey(equiv)) {
									defs.put(equiv, new Vector<Stmt>());
									defsStack.push(equivWS);
									if(MyConstants.DEBUG_INFO)
										System.out.println("def stack doesn't contain " + equivWS.dump() + ". Push it.");
								}
							}
						}
					}
					 */

					//def-use analysis
					Stmt def = defWS.getDef();
					Stmt scope = defWS.getScopeBegin();

					if(def.containsInvokeExpr()){
						if(!def.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
							AddTags(def, API_TAG);
						}
					}					

					if(def instanceof DefinitionStmt){
						//if(!def.containsInvokeExpr()){
							boolean usesConstant = false;
							List<ValueBox> checkConstUseBoxes = def.getUseBoxes();
							for(ValueBox ccVB : checkConstUseBoxes){
								if(ccVB.getValue() instanceof StringConstant){
									if(!((StringConstant)ccVB.getValue()).value.equals("")){
										usesConstant = true;
										break;
									}
								}
							}					
							if(usesConstant){
								AddTags(def, STRING_CONST_TAG);
							}
						//}
					}

					APIGraphNode defNode = null;
					if(!stmtToNodeMap.containsKey(def)){
						defNode = new APIGraphNode(def, sMethod);
						stmtToNodeMap.put(def, defNode);
						NODE_COUNT++;
						if(isAndroidAPICall(def)){
							API_NODE_COUNT++;
						}
					}else{
						defNode = stmtToNodeMap.get(def);
					}
					if(!methodToDDGMap.get(sMethod).contains(defNode)){
						methodToDDGMap.get(sMethod).add(defNode);
					}

					Iterator it = body.getUnits().iterator();
					while (it.hasNext()) {
						Stmt s = (Stmt) it.next();

						if(defWS.getScopeBegin()!=null){
							if(!isInScope(eug, s, defWS.getScopeBegin())){
								if(MyConstants.DEBUG_INFO){
									System.out.println(s + " is NOT in the scope: " + defWS.getScopeBegin());
								}
								continue;
							}
						}

						// For every use box in that unit in sMethod and that use box is an instance of local
						Iterator usesIt = s.getUseBoxes().iterator();
						while (usesIt.hasNext()) {
							ValueBox vbox = (ValueBox) usesIt.next();
							if (vbox.getValue() instanceof Local) {
								Local l = (Local) vbox.getValue();
								//System.out.println("l: " + l);

								
								Iterator rDefsIt = mrd.getDefsOfAt(l, s).iterator();
								while (rDefsIt.hasNext()) {
									Stmt next = (Stmt) rDefsIt.next();
									//System.out.println("next: " + next);
									//System.out.println("defs: " + defs);

									// Get all the defs of the local that used in sMethod 
									// and if (the def == the def in defWS) then do analysis
									//if (defs.contains(next)) {
									if(next == defWS.getDef()){

										if (s instanceof InvokeStmt) {

											//to pass the index of tainted parameter. in fact one taint a time.
											Vector<Integer> taintVector = new Vector<Integer>();

											// Since s is an instance of invoke statement.
											// This loop is to add index of tainted parameter of locally defined variables
											Iterator defIt2 = next.getDefBoxes().iterator();
											while (defIt2.hasNext()) {
												ValueBox vbox2 = (ValueBox) defIt2.next();
												if (vbox2.getValue() instanceof Local) {
													// System.out.println(vbox2.getValue());
													InvokeExpr invokeEx = s.getInvokeExpr();
													int argCount = invokeEx.getArgCount();
													for (int i = 0; i < argCount; i++) {
														if (invokeEx.getArg(i) == vbox2.getValue()) {
															taintVector.add(new Integer(i));
														}												
													}

													//for instance invoke, consider this reference too.
													if(invokeEx instanceof InstanceInvokeExpr){
														if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

															//special invoke doesn't count
															if(invokeEx instanceof SpecialInvokeExpr){
																if(next instanceof DefinitionStmt){
																	Value rhs = ((DefinitionStmt) next).getRightOp();
																	if(rhs instanceof NewExpr){
																		continue;
																	}
																}
															}

															taintVector.add(new Integer(MyConstants.thisObject));
														}
													}
												}
											}//end of while

											if(taintVector.isEmpty()){

												if(MyConstants.DEBUG_INFO)
													System.out.println("No parameters: " + s);
												continue;
											}
											
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
											

											// Go through every method that could be reached from s
											while (targets.hasNext()) {
												SootMethod target = (SootMethod) targets.next();

												if(MyConstants.DEBUG_INFO){
													System.out.println("call target is " + target);
												}

												boolean noNewTaint = true;
												if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+s.toString())){
													noNewTaint = false;
													
													// put the taint sources into this 'sources' list
													List<Integer> sources = new ArrayList<Integer>();
													sources.addAll(taintVector);
													propagationHistory.put(sMethod.getSignature()+"|"+s.toString(), sources);
												}else{
													List<Integer> sources = propagationHistory.get(sMethod.getSignature()+"|"+s.toString());

													for(Integer taint : taintVector){
														if(!sources.contains(taint)){
															noNewTaint = false;
															sources.add(taint);
														}
													}

												}

												DefWithScope sWS = new DefWithScope(s);

												APIGraphNode sNode = null;
												if(!stmtToNodeMap.containsKey(s)){
													sNode = new APIGraphNode(s, sMethod);
													stmtToNodeMap.put(s, sNode);
													NODE_COUNT++;
													if(isAndroidAPICall(s)){
														API_NODE_COUNT++;
													}
												}else{
													sNode = stmtToNodeMap.get(s);
												}
												sNode.addPred(defNode);
												defNode.addSucc(sNode);
												if(!methodToDDGMap.get(sMethod).contains(sNode)){
													methodToDDGMap.get(sMethod).add(sNode);
												}												

												if (!defs.containsKey(s)) {
													defs.put(s, new Vector<Stmt>());
													defsStack.push(sWS);
													if(MyConstants.DEBUG_INFO)
														System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
												}else{
													if(noNewTaint){
														break;
													}
												}


												if(MyConstants.DEBUG_INFO){
													System.out.println("PROPAGATING from METHOD: " + sMethod);
													System.out.println("PROPAGATING from STATEMENT: " + s);
												}

												GlobalForwardDataflowAnalysis.taintedFieldsInCaller.addAll(taintedFields);
												//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCaller.putAll(taintedFieldToPTSMap);

												Vector<Integer> tainted = propagate(target, taintVector, leakSource, s, sMethod);
												
												for(SootField sf : GlobalForwardDataflowAnalysis.taintedFieldsInCallee){
													if(!taintedFields.contains(sf)){
														taintedFields.add(sf);
													}
												}
												/*
												Set<SootField> taintedFieldSet = GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.keySet();
												Iterator<SootField> taintedFieldIter = taintedFieldSet.iterator();
												while(taintedFieldIter.hasNext()){
													SootField tf = taintedFieldIter.next();
													if(!taintedFieldToPTSMap.containsKey(tf)){
														taintedFieldToPTSMap.put(tf, GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.get(tf));
													}
												}											
												*/
												GlobalForwardDataflowAnalysis.taintedFieldsInCallee.clear();
												//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.clear();

												if(MyConstants.DEBUG_INFO){
													System.out.println(s + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
												}
												
												// if propagation result is not empty
												if ((tainted!=null) && (!tainted.isEmpty())) {

													for(Integer i : tainted){
														int index = i.intValue();

														if(index == MyConstants.returnValue){
															if(s instanceof DefinitionStmt){
																Value taintedRet = ((DefinitionStmt) s).getLeftOp();
																if(taintedRet instanceof Local){

																	if (!defs.containsKey(s)) {

																		sNode.addPred(defNode);
																		defNode.addSucc(sNode);
																		if(!methodToDDGMap.get(sMethod).contains(sNode)){
																			methodToDDGMap.get(sMethod).add(sNode);
																		}

																		if(MyConstants.DEBUG_INFO)
																			System.out.println("adding def of return value:" + s);

																		defs.put(s, new Vector<Stmt>());
																		defsStack.push(sWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
																	}
																}
															}

														}else if(index == MyConstants.thisObject){
															if(s.getInvokeExpr() instanceof InstanceInvokeExpr){
																Value taintedThisRef = ((InstanceInvokeExpr)s.getInvokeExpr()).getBase();

																boolean hasDef = false;
																Stmt def0 = s;
																
																// If tainted this reference is an instance of local
																if(taintedThisRef instanceof Local){
																	List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, def0);
																	
																	// If this reference only has one definition
																	// then update stmtToNodeMap and add new definition into defsStack
																	if(defs0.size()==1){
																		def0 = (Stmt)defs0.get(0);
																		hasDef = true;

																		APIGraphNode def0Node = null;
																		if(!stmtToNodeMap.containsKey(def0)){
																			def0Node = new APIGraphNode(def0, sMethod);
																			stmtToNodeMap.put(def0, def0Node);
																			NODE_COUNT++;
																			if(isAndroidAPICall(def0)){
																				API_NODE_COUNT++;
																			}
																		}else{
																			def0Node = stmtToNodeMap.get(def0);
																		}
																		def0Node.addPred(sNode);
																		sNode.addSucc(def0Node);
																		if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																			methodToDDGMap.get(sMethod).add(def0Node);
																		}

																		DefWithScope def0WS = new DefWithScope(def0, s);
																		if(!defs.containsKey(def0)){
																			Vector<Stmt> scopes = new Vector<Stmt>();
																			scopes.add(s);
																			defs.put(def0, scopes);
																			defsStack.push(def0WS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																		}else if(!(defs.get(def0).contains(s))){
																			defs.get(def0).add(s);
																			defsStack.push(def0WS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																		}
																	}else{
																		//not very good solution :(
																		for(Unit defn : defs0){		

																			APIGraphNode defnNode = null;
																			if(!stmtToNodeMap.containsKey((Stmt)defn)){
																				defnNode = new APIGraphNode((Stmt)defn, sMethod);
																				stmtToNodeMap.put((Stmt)defn, defnNode);
																				NODE_COUNT++;
																				if(isAndroidAPICall((Stmt)defn)){
																					API_NODE_COUNT++;
																				}
																			}else{
																				defnNode = stmtToNodeMap.get((Stmt)defn);
																			}
																			defnNode.addPred(sNode);
																			sNode.addSucc(defnNode);
																			if(!methodToDDGMap.get(sMethod).contains(defnNode)){
																				methodToDDGMap.get(sMethod).add(defnNode);
																			}

																			DefWithScope defnWS = new DefWithScope((Stmt)defn, s);
																			if(!defs.containsKey(defn)){
																				Vector<Stmt> scopes = new Vector<Stmt>();
																				scopes.add(s);
																				defs.put((Stmt)defn, scopes);
																				defsStack.push(defnWS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																			}else if(!(defs.get(defn).contains(s))){
																				defs.get(defn).add(s);
																				defsStack.push(defnWS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																			}
																		}
																	}
																}//end of if(taintedThisRef is an instance of Local)

																													
															}														 
															
														} //end of if this object is tainted
														else if(index >= 0){

															Value taintedArg = s.getInvokeExpr().getArg(index);

															boolean hasDef = false;
															Stmt def0 = s;
															if(taintedArg instanceof Local && taintedArg.getType() instanceof RefLikeType){
																List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, def0);
																if(defs0.size()==1){
																	def0 = (Stmt)defs0.get(0);
																	hasDef = true;

																	APIGraphNode def0Node = null;
																	if(!stmtToNodeMap.containsKey(def0)){
																		def0Node = new APIGraphNode(def0, sMethod);
																		stmtToNodeMap.put(def0, def0Node);
																		NODE_COUNT++;
																		if(isAndroidAPICall(def0)){
																			API_NODE_COUNT++;
																		}
																	}else{
																		def0Node = stmtToNodeMap.get(def0);
																	}
																	def0Node.addPred(sNode);
																	sNode.addSucc(def0Node);
																	if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																		methodToDDGMap.get(sMethod).add(def0Node);
																	}

																	DefWithScope def0WS = new DefWithScope(def0, s);
																	if(!defs.containsKey(def0)){
																		Vector<Stmt> scopes = new Vector<Stmt>();
																		scopes.add(s);
																		defs.put(def0, scopes);
																		defsStack.push(def0WS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																	}else if(!(defs.get(def0).contains(s))){
																		defs.get(def0).add(s);
																		defsStack.push(def0WS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																	}
																}
															}
															
														}
													}
												}

											}

											// invokes.add(s);
										// s is not invoke statement
										} else {

											boolean isInvoke = false;

											Iterator iUse = s.getUseBoxes().iterator();
											while (iUse.hasNext()) {
												ValueBox vB = (ValueBox) iUse.next();
												if (vB.getValue() instanceof InvokeExpr) {
													isInvoke = true;
												}
											}

											if (isInvoke) {
												Vector<Integer> taintVector = new Vector<Integer>();

												Iterator defIt2 = next.getDefBoxes().iterator();
												while (defIt2.hasNext()) {
													ValueBox vbox2 = (ValueBox) defIt2.next();
													if (vbox2.getValue() instanceof Local) {

														InvokeExpr invokeEx = s.getInvokeExpr();
														int argCount = invokeEx.getArgCount();

														for (int i = 0; i < argCount; i++) {
															if (invokeEx.getArg(i) == vbox2.getValue()) {
																taintVector.add(new Integer(i));
															}
														}

														if(invokeEx instanceof InstanceInvokeExpr){
															if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

																if(invokeEx instanceof SpecialInvokeExpr){
																	if(next instanceof DefinitionStmt){
																		Value rhs = ((DefinitionStmt) next).getRightOp();
																		if(rhs instanceof NewExpr){
																			continue;
																		}
																	}
																}

																taintVector.add(new Integer(MyConstants.thisObject));
															}
														}

													}
												}

												if(taintVector.isEmpty()){
													if(MyConstants.DEBUG_INFO)
														System.out.println("No parameters: " + s);
													continue;												
												}

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

													if(MyConstants.DEBUG_INFO){
														System.out.println("call target is " + target);
													}
													
													boolean noNewTaint = true;
													if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+s.toString())){														
														noNewTaint = false;
														List<Integer> sources = new ArrayList<Integer>();
														sources.addAll(taintVector);
														propagationHistory.put(sMethod.getSignature()+"|"+s.toString(), sources);
													}else{
														List<Integer> sources = propagationHistory.get(sMethod.getSignature()+"|"+s.toString());

														for(Integer taint : taintVector){
															if(!sources.contains(taint)){
																noNewTaint = false;
																sources.add(taint);
															}
														}														
													}
													
													APIGraphNode sNode = null;
													if(!stmtToNodeMap.containsKey(s)){
														sNode = new APIGraphNode(s, sMethod);
														stmtToNodeMap.put(s, sNode);
														NODE_COUNT++;
														if(isAndroidAPICall(s)){
															API_NODE_COUNT++;
														}
													}else{
														sNode = stmtToNodeMap.get(s);
													}
													sNode.addPred(defNode);
													defNode.addSucc(sNode);
													if(!methodToDDGMap.get(sMethod).contains(sNode)){
														methodToDDGMap.get(sMethod).add(sNode);
													}

													DefWithScope sWS = new DefWithScope(s);
													if (!defs.containsKey(s)) {
														defs.put(s, new Vector<Stmt>());
														defsStack.push(sWS);
														if(MyConstants.DEBUG_INFO)
															System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
													}else{
														if(noNewTaint){
															break;
														}
													}	

													if(MyConstants.DEBUG_INFO){
														System.out.println("PROPAGATING from METHOD: " + sMethod);
														System.out.println("PROPAGATING from STATEMENT: " + s);
													}
													GlobalForwardDataflowAnalysis.taintedFieldsInCaller.addAll(taintedFields);
													//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCaller.putAll(taintedFieldToPTSMap);

													Vector<Integer> tainted = propagate(target, taintVector, leakSource, s, sMethod);
													
													for(SootField sf : GlobalForwardDataflowAnalysis.taintedFieldsInCallee){
														if(!taintedFields.contains(sf)){
															taintedFields.add(sf);
														}
													}
													/*
													Set<SootField> taintedFieldSet = GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.keySet();
													Iterator<SootField> taintedFieldIter = taintedFieldSet.iterator();
													while(taintedFieldIter.hasNext()){
														SootField tf = taintedFieldIter.next();
														if(!taintedFieldToPTSMap.containsKey(tf)){
															taintedFieldToPTSMap.put(tf, GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.get(tf));
														}
													}											
													*/
													GlobalForwardDataflowAnalysis.taintedFieldsInCallee.clear();
													//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.clear();

													if(MyConstants.DEBUG_INFO){
														System.out.println(s + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
													}
													if ((tainted!=null) && (!tainted.isEmpty())) {

														for(Integer i : tainted){
															int index = i.intValue();

															if(index == MyConstants.returnValue){
																if(s instanceof DefinitionStmt){
																	Value taintedRet = ((DefinitionStmt) s).getLeftOp();
																	if(taintedRet instanceof Local){
																		if (!defs.containsKey(s)) {
																			
																			sNode.addPred(defNode);
																			defNode.addSucc(sNode);
																			if(!methodToDDGMap.get(sMethod).contains(sNode)){
																				methodToDDGMap.get(sMethod).add(sNode);
																			}

																			if(MyConstants.DEBUG_INFO)
																				System.out.println("adding def of return value:" + s);

																			defs.put(s, new Vector<Stmt>());
																			//delta.add(s);
																			//System.out.println("def: " + s + " PROPAGATES");
																			defsStack.push(sWS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
																		}
																	}
																}

															}else if(index == MyConstants.thisObject){
																if(s.getInvokeExpr() instanceof InstanceInvokeExpr){
																	Value taintedThisRef = ((InstanceInvokeExpr)s.getInvokeExpr()).getBase();

																	boolean hasDef = false;
																	Stmt def0 = s;
																	if(taintedThisRef instanceof Local){
																		List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, def0);
																		if(defs0.size()==1){
																			def0 = (Stmt)defs0.get(0);
																			hasDef = true;
																			
																			APIGraphNode def0Node = null;
																			if(!stmtToNodeMap.containsKey(def0)){
																				def0Node = new APIGraphNode(def0, sMethod);
																				stmtToNodeMap.put(def0, def0Node);
																				NODE_COUNT++;
																				if(isAndroidAPICall(def0)){
																					API_NODE_COUNT++;
																				}
																			}else{
																				def0Node = stmtToNodeMap.get(def0);
																			}
																			def0Node.addPred(sNode);
																			sNode.addSucc(def0Node);
																			if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																				methodToDDGMap.get(sMethod).add(def0Node);
																			}

																			DefWithScope def0WS = new DefWithScope(def0, s);
																			if(!defs.containsKey(def0)){
																				Vector<Stmt> scopes = new Vector<Stmt>();
																				scopes.add(s);
																				defs.put(def0, scopes);
																				defsStack.push(def0WS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																			}else if(!(defs.get(def0).contains(s))){
																				defs.get(def0).add(s);
																				defsStack.push(def0WS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																			}
																		}else{
																			//not very good solution :(
																			for(Unit defn : defs0){
																				
																				APIGraphNode defnNode = null;
																				if(!stmtToNodeMap.containsKey((Stmt)defn)){
																					defnNode = new APIGraphNode((Stmt)defn, sMethod);
																					stmtToNodeMap.put((Stmt)defn, defnNode);
																					NODE_COUNT++;
																					if(isAndroidAPICall((Stmt)defn)){
																						API_NODE_COUNT++;
																					}
																				}else{
																					defnNode = stmtToNodeMap.get((Stmt)defn);
																				}
																				defnNode.addPred(sNode);
																				sNode.addSucc(defnNode);
																				if(!methodToDDGMap.get(sMethod).contains(defnNode)){
																					methodToDDGMap.get(sMethod).add(defnNode);
																				}

																				DefWithScope defnWS = new DefWithScope((Stmt)defn, s);
																				if(!defs.containsKey(defn)){
																					Vector<Stmt> scopes = new Vector<Stmt>();
																					scopes.add(s);
																					defs.put((Stmt)defn, scopes);
																					defsStack.push(defnWS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																				}else if(!(defs.get(defn).contains(s))){
																					defs.get(defn).add(s);
																					defsStack.push(defnWS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																				}
																			}
																		}
																	}
																												
																}														 

															}else if(index >= 0){

																Value taintedArg = s.getInvokeExpr().getArg(index);

																boolean hasDef = false;
																Stmt def0 = s;
																if(taintedArg instanceof Local && taintedArg.getType() instanceof RefLikeType){
																	List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, def0);
																	if(defs0.size()==1){
																		def0 = (Stmt)defs0.get(0);
																		hasDef = true;
																		
																		APIGraphNode def0Node = null;
																		if(!stmtToNodeMap.containsKey(def0)){
																			def0Node = new APIGraphNode(def0, sMethod);
																			stmtToNodeMap.put(def0, def0Node);
																			NODE_COUNT++;
																			if(isAndroidAPICall(def0)){
																				API_NODE_COUNT++;
																			}
																		}else{
																			def0Node = stmtToNodeMap.get(def0);
																		}
																		def0Node.addPred(sNode);
																		sNode.addSucc(def0Node);
																		if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																			methodToDDGMap.get(sMethod).add(def0Node);
																		}

																		DefWithScope def0WS = new DefWithScope(def0, s);
																		if(!defs.containsKey(def0)){
																			Vector<Stmt> scopes = new Vector<Stmt>();
																			scopes.add(s);
																			defs.put(def0, scopes);
																			defsStack.push(def0WS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																		}else if(!(defs.get(def0).contains(s))){
																			defs.get(def0).add(s);
																			defsStack.push(def0WS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																		}
																	}
																}

																
															}
														}
													}

												}
												// invokes.add(s);
											} else if(s instanceof ReturnStmt){

												if(MyConstants.DEBUG_INFO){
													System.out.println("returning to caller...");
												}
												if(MyConstants.DEBUG_INFO)
													System.out.println("return to caller from: " + sMethod + " | " + s);
												
												APIGraphNode sNode = null;
												if(!stmtToNodeMap.containsKey(s)){
													sNode = new APIGraphNode(s, sMethod);
													stmtToNodeMap.put(s, sNode);
													NODE_COUNT++;
													if(isAndroidAPICall(s)){
														API_NODE_COUNT++;
													}
												}else{
													sNode = stmtToNodeMap.get(s);
												}
												sNode.addPred(defNode);
												defNode.addSucc(sNode);
												if(!methodToDDGMap.get(sMethod).contains(sNode)){
													methodToDDGMap.get(sMethod).add(sNode);
												}

												DefWithScope sWS = new DefWithScope(s);
												if (!defs.containsKey(s)) {
													defs.put(s, new Vector<Stmt>());
													defsStack.push(sWS);
													if(MyConstants.DEBUG_INFO)
														System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
												}

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

													//ddg chain is broken here, need fix!
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
																		
																		APIGraphNode callerStmtNode = null;
																		if(!stmtToNodeMap.containsKey(callerStmt)){
																			callerStmtNode = new APIGraphNode(callerStmt, method);
																			stmtToNodeMap.put(callerStmt, callerStmtNode);
																			NODE_COUNT++;
																		}else{
																			callerStmtNode = stmtToNodeMap.get(callerStmt);
																		}
																		sNode.addSucc(callerStmtNode);
																		callerStmtNode.addPred(sNode);
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
																		
																		if(!sourceMethods.contains(sMethod)){
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("adding sourceMethod: " + sMethod);
																			sourceMethods.add(sMethod);
																		}
																	}
																}else{
																	Iterator targets = new Targets(cg.edgesOutOf(callerStmt));													

																	while (targets.hasNext()) {
																		SootMethod target = (SootMethod) targets.next();
																		//System.out.println(method + " may call " + target);
																		if(target.equals(sMethod)){
																			
																			APIGraphNode callerStmtNode = null;
																			if(!stmtToNodeMap.containsKey(callerStmt)){
																				callerStmtNode = new APIGraphNode(callerStmt, method);
																				stmtToNodeMap.put(callerStmt, callerStmtNode);
																				NODE_COUNT++;
																			}else{
																				callerStmtNode = stmtToNodeMap.get(callerStmt);
																			}
																			sNode.addSucc(callerStmtNode);
																			callerStmtNode.addPred(sNode);
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
														
														/*
														Iterator targets = new Targets(cg.edgesOutOf(method));													

														while (targets.hasNext()) {
															SootMethod target = (SootMethod) targets.next();
															//System.out.println(method + " may call " + target);
															if(target.equals(sMethod)){
																if(!fullWorklist.contains(method)){
																	worklist.add(method);
																	fullWorklist.add(method);
																	if(!sourceMethods.contains(sMethod)){
																		sourceMethods.add(sMethod);
																	}
																	
																	Iterator iterCall = method.retrieveActiveBody().getUnits().iterator();
																	while(iterCall.hasNext()){
																		Stmt call = (Stmt)iterCall.next();
																		if(call.containsInvokeExpr()){
																			Iterator tars = new Targets(cg.edgesOutOf(call));
																			while(tars.hasNext()){
																				SootMethod tar = (SootMethod)tars.next();
																				if(tar.equals(sMethod)){
																					APIGraphNode callNode = null;
																					if(!stmtToNodeMap.containsKey(call)){
																						callNode = new APIGraphNode(call, method);
																						stmtToNodeMap.put(call, callNode);
																						NODE_COUNT++;
																						if(isAndroidAPICall(call)){
																							API_NODE_COUNT++;
																						}
																					}else{
																						callNode = stmtToNodeMap.get(call);
																					}
																					callNode.addPred(sNode);
																					sNode.addSucc(callNode);
																					if(!methodToDDGMap.containsKey(method)){
																						List<APIGraphNode> ddg = new ArrayList<APIGraphNode>();
																						ddg.add(callNode);
																						methodToDDGMap.put(method, ddg);
																					}else{
																						if(!methodToDDGMap.get(method).contains(callNode)){
																							methodToDDGMap.get(method).add(callNode);
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
													}
												}
											}

											//pure definiton statement:
											else {
												//use of l appears on the left of assignment
												if(s instanceof DefinitionStmt){
													Value lhs = ((DefinitionStmt) s).getLeftOp();													
													
													if(lhs instanceof ArrayRef){
														Value base = ((ArrayRef) lhs).getBase();
														if(base == l){
															continue;
														}
													}else if(lhs instanceof InstanceFieldRef){
														Value base = ((InstanceFieldRef) lhs).getBase();
														if(base == l){
															continue;
														}
													}
												}

												DefWithScope sWS = new DefWithScope(s);											
												if (!defs.containsKey(s)) {
													
													APIGraphNode sNode = null;
													if(!stmtToNodeMap.containsKey(s)){
														sNode = new APIGraphNode(s, sMethod);
														stmtToNodeMap.put(s, sNode);
														NODE_COUNT++;
														if(isAndroidAPICall(s)){
															API_NODE_COUNT++;
														}
													}else{
														sNode = stmtToNodeMap.get(s);
													}
													sNode.addPred(defNode);
													defNode.addSucc(sNode);
													if(!methodToDDGMap.get(sMethod).contains(sNode)){
														methodToDDGMap.get(sMethod).add(sNode);
													}
													
													boolean isRhsInstanceRef = false;
													if(s instanceof DefinitionStmt){
														Value rhs = ((DefinitionStmt) s).getRightOp();
														if(rhs instanceof InstanceFieldRef){
															Value base = ((InstanceFieldRef) rhs).getBase();
															if(base == l){
																isRhsInstanceRef = true;
															}
														}
													}		
													
													if(!isRhsInstanceRef){
														defs.put(s, new Vector<Stmt>());
														defsStack.push(sWS);
														if(MyConstants.DEBUG_INFO)
															System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
													}

													if(s instanceof DefinitionStmt){
														Value lhs = ((DefinitionStmt) s).getLeftOp();

														if(lhs instanceof StaticFieldRef){
															if(MyConstants.TO_TAINT_STATIC_FIELD){
																if(!taintedFields.contains(s)){
																	taintedFields.add(((StaticFieldRef)lhs).getField());
																	
																	/*
																	SootField fieldKey = ((StaticFieldRef)lhs).getField();
																	if(fieldToDefsMap.containsKey(fieldKey)){
																		List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
																		if(!fieldDefs.contains(s)){
																			fieldDefs.add(s);
																		}
																	}else{
																		List<Stmt> fieldDefs = new ArrayList<Stmt>();
																		fieldDefs.add(s);
																		fieldToDefsMap.put(fieldKey, fieldDefs);																	
																	}
																	*/
																}
																
																SootField fieldKey = ((StaticFieldRef)lhs).getField();
																if(fieldToDefsMap.containsKey(fieldKey)){
																	List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
																	if(!fieldDefs.contains(s)){
																		fieldDefs.add(s);
																	}
																}else{
																	List<Stmt> fieldDefs = new ArrayList<Stmt>();
																	fieldDefs.add(s);
																	fieldToDefsMap.put(fieldKey, fieldDefs);																	
																}
															}
														}
													}												

													boolean hasDef = false;
													Stmt def0 = s;


													if(def0 instanceof DefinitionStmt){
														Value lhs = ((DefinitionStmt) def0).getLeftOp();
														

														if(lhs instanceof InstanceFieldRef){

															if(MyConstants.TO_TAINT_INSTANCE_FIELD){
																if(!taintedFields.contains(def0)){
																	taintedFields.add(((InstanceFieldRef)lhs).getField());
																	
																	/*
																	SootField fieldKey = ((InstanceFieldRef)lhs).getField();
																	if(fieldToDefsMap.containsKey(fieldKey)){
																		List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
																		if(!fieldDefs.contains(def0)){
																			fieldDefs.add(def0);
																		}
																	}else{
																		List<Stmt> fieldDefs = new ArrayList<Stmt>();
																		fieldDefs.add(def0);
																		fieldToDefsMap.put(fieldKey, fieldDefs);																	
																	}
																	*/
																}
																
																SootField fieldKey = ((InstanceFieldRef)lhs).getField();
																if(fieldToDefsMap.containsKey(fieldKey)){
																	List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
																	if(!fieldDefs.contains(def0)){
																		fieldDefs.add(def0);
																	}
																}else{
																	List<Stmt> fieldDefs = new ArrayList<Stmt>();
																	fieldDefs.add(def0);
																	fieldToDefsMap.put(fieldKey, fieldDefs);																	
																}
															}
															
															
														}else if(lhs instanceof ArrayRef){
															Value base = ((ArrayRef) lhs).getBase();
															if(base instanceof Local){
																List<Unit> defs0 = mrd.getDefsOfAt((Local)base, def0);
																if(defs0.size()==1){
																	def0 = (Stmt)defs0.get(0);
																	hasDef = true;
																	
																	APIGraphNode def0Node = null;
																	if(!stmtToNodeMap.containsKey(def0)){
																		def0Node = new APIGraphNode(def0, sMethod);
																		stmtToNodeMap.put(def0, def0Node);
																		NODE_COUNT++;
																		if(isAndroidAPICall(def0)){
																			API_NODE_COUNT++;
																		}
																	}else{
																		def0Node = stmtToNodeMap.get(def0);
																	}
																	def0Node.addPred(sNode);
																	sNode.addSucc(def0Node);
																	if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																		methodToDDGMap.get(sMethod).add(def0Node);
																	}

																	DefWithScope def0WS = new DefWithScope(def0, s);
																	if(!defs.containsKey(def0)){
																		Vector<Stmt> scopes = new Vector<Stmt>();
																		scopes.add(s);
																		defs.put(def0, scopes);
																		defsStack.push(def0WS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																	}else if(!(defs.get(def0).contains(s))){
																		defs.get(def0).add(s);
																		defsStack.push(def0WS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																	}
																}
															}
														}
													}

													
												}
											}//end else
										}
									}
								}

							}
						}
					}

				}// end while(!defsStack.isEmpty())

				Iterator i = defs.keySet().iterator();
				while (i.hasNext()) {
					Stmt s = (Stmt)i.next();
					//System.out.print(s + "|");
					AddTags(s, generalTaintTag);
					AddTags(s, taintTag);

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
					System.out.println("dataflow for " + source + ":");
				}
				Iterator printIt = body.getUnits().iterator();
				while(printIt.hasNext()){
					Stmt s = (Stmt)printIt.next();
					if(s.getTags().contains(taintTag) || s.getTags().contains(extraDefTag)){

						if(MyConstants.DEBUG_INFO){
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
								taintSourceToField.put(leakSource, fieldList);
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
								taintSourceToField.put(leakSource, fieldList);
								classToSourceToField.put(sMethod.getDeclaringClass().getName(), taintSourceToField);

							}else if(vb.getValue() instanceof Local){

								String varName = ((Local)vb.getValue()).getName();								
								LinkedHashMap<String, List<String>> taintSourceToVar = new LinkedHashMap<String, List<String>>();
								List<String> varList = new ArrayList<String>();
								if(varList.contains(varName)){
									varList.add(varName);
								}								
								taintSourceToVar.put(leakSource, varList);
								methodToSourceToVar.put(sMethod.getSignature(), taintSourceToVar);
							}
						}
					}
				}

				if(MyConstants.DEBUG_INFO){
					System.out.println("end dataflow for " + source + "\n");
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
					if(API_NODE_COUNT > MyConstants.MAX_APINODES_CONSIDERED){
						API_NODE_COUNT = 0;
						break;
					}
				}				
				
				SootField taintedField = fWorklist.remove();
				if(!fieldToDefsMap.containsKey(taintedField)){
					System.out.println("ERROR: definitions of a field " + taintedField + " is not recorded!");
					continue;
				}
				List<Stmt> fieldDefsForPTA = fieldToDefsMap.get(taintedField);

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
						System.out.println("looking for use of static field in " + soot_class.getName() + "..." );
					}
					//System.out.println("package name: " + soot_class.getPackageName());

					List<SootMethod> methods = soot_class.getMethods();
					for (SootMethod method : methods) {

						if(!method.isConcrete()){
							continue;
						}


						JimpleBody body = (JimpleBody) method.retrieveActiveBody();
						Iterator it = body.getUnits().iterator();

						while (it.hasNext()) {
							Stmt s = (Stmt) it.next();
							if(s instanceof DefinitionStmt){
								Value rhs = ((DefinitionStmt) s).getRightOp();
								if(rhs instanceof StaticFieldRef){
									if(((StaticFieldRef) rhs).getField().equals(taintedField)){
										entryPoints.add(method);
									}
								}

								else if(rhs instanceof InstanceFieldRef){

									//if(((InstanceFieldRef) rhs).getField().getType().equals(
									//		RefType.v("android.location.Location"))){
									if(((InstanceFieldRef) rhs).getField().equals(taintedField)){
										entryPoints.add(method);
									}
									//}									

								}
							}
						}


					}
				}



				//for(SootMethod sm : entryPoints){
				//	System.out.println("entry point sm: " + sm);
				//}


				//List<SootMethod> entryPoints = Scene.v().getEntryPoints();


				worklist = new LinkedList<SootMethod>();
				fullWorklist = new LinkedList<SootMethod>();

				worklist.addAll(entryPoints);
				fullWorklist.addAll(entryPoints);

				sourceMethods = new ArrayList<SootMethod>();


				//Iterator itMeth = entryPoints.iterator();


				while(!worklist.isEmpty()){
					
					if(MyConstants.TO_CONSIDER_LIMIT){
						if(NODE_COUNT > MyConstants.MAX_NODES_CONSIDERED || API_NODE_COUNT > MyConstants.MAX_APINODES_CONSIDERED){
							break;
						}
					}

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


					//SootMethod sMethod = (SootMethod) itMeth.next();

					if(MyConstants.DEBUG_INFO){
						System.out.println();
						System.out.println("analyzing method:" + sMethod.getSignature());
					}
					// System.out.println(sMethod.getSource());
					// System.out.println(sMethod.getBytecodeParms());

					JimpleBody body = (JimpleBody) sMethod.retrieveActiveBody();



					ExceptionalUnitGraph eug = new ExceptionalUnitGraph(body);
					
					MyReachingDefinition mrd = new MyReachingDefinition(eug);
					

					//Stack<Stmt> defsStack = new Stack<Stmt>();
					//Vector<Stmt> taintedRefDefs = new Vector<Stmt>();
					Stack<DefWithScope> defsStack = new Stack<DefWithScope>();
					Vector<DefWithScope> taintedRefDefs = new Vector<DefWithScope>();

					//Vector<Stmt> defs = new Vector<Stmt>();
					//Vector<DefWithScope> defs = new Vector<DefWithScope>();
					LinkedHashMap<Stmt, Vector<Stmt>> defs = new LinkedHashMap<Stmt, Vector<Stmt>>();

					Stmt source = null;
					{
						Iterator it = body.getUnits().iterator();
						while (it.hasNext()) {
							Stmt s = (Stmt) it.next();

							if(s instanceof DefinitionStmt){
								Value rhs = ((DefinitionStmt) s).getRightOp();
								if(rhs instanceof StaticFieldRef){
									if(((StaticFieldRef) rhs).getField().equals(taintedField)){

										DefWithScope sWS = new DefWithScope(s);
										if(!defs.containsKey(s)){
											defs.put(s, new Vector<Stmt>());
											defsStack.push(sWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
											source = s;
											
											APIGraphNode sNode = null;
											if(!stmtToNodeMap.containsKey(s)){
												sNode = new APIGraphNode(s, sMethod);
												stmtToNodeMap.put(s, sNode);
												NODE_COUNT++;
											}else{
												sNode = stmtToNodeMap.get(s);
											}
											if(!methodToDDGMap.get(sMethod).contains(sNode)){
												methodToDDGMap.get(sMethod).add(sNode);
											}
											
											if(fieldToUsesMap.containsKey(taintedField)){
												List<Stmt> fieldUses = fieldToUsesMap.get(taintedField);
												if(!fieldUses.contains(s)){
													fieldUses.add(s);
												}
											}else{
												List<Stmt> fieldUses = new ArrayList<Stmt>();
												fieldUses.add(s);
												fieldToUsesMap.put(taintedField, fieldUses);
											}
											/*
											List<Stmt> fieldDefs = fieldToDefsMap.get(((StaticFieldRef) rhs).getField());
											for(Stmt fieldDef : fieldDefs){
												if(stmtToNodeMap.containsKey(fieldDef)){
													APIGraphNode fieldDefNode = stmtToNodeMap.get(fieldDef);
													sNode.addPred(fieldDefNode);
													fieldDefNode.addSucc(sNode);
												}
											}
											*/
										}
									}
								}

								else if(rhs instanceof InstanceFieldRef){
									if(((InstanceFieldRef) rhs).getField().equals(taintedField)){
										
										boolean ptsHasIntersection = false;
										PointsToSet ptsRhs = pta.reachingObjects((Local)((InstanceFieldRef)rhs).getBase());
										if(!ptsRhs.isEmpty()){
										
											for(Stmt fieldDefForPTA : fieldDefsForPTA){
												PointsToSet ptsToTest = pta.reachingObjects((Local)((InstanceFieldRef)((DefinitionStmt)fieldDefForPTA)
														.getLeftOp()).getBase());
												if(ptsRhs.hasNonEmptyIntersection(ptsToTest)){
													ptsHasIntersection = true;
													break;
												}
											}
										}
										
										if(ptsHasIntersection || ptsRhs.isEmpty()){

											DefWithScope sWS = new DefWithScope(s);
											if(!defs.containsKey(s)){
												defs.put(s, new Vector<Stmt>());
												defsStack.push(sWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
												source = s;
												
												APIGraphNode sNode = null;
												if(!stmtToNodeMap.containsKey(s)){
													sNode = new APIGraphNode(s, sMethod);
													stmtToNodeMap.put(s, sNode);
													NODE_COUNT++;
												}else{
													sNode = stmtToNodeMap.get(s);
												}
												if(!methodToDDGMap.get(sMethod).contains(sNode)){
													methodToDDGMap.get(sMethod).add(sNode);
												}
												
												if(fieldToUsesMap.containsKey(taintedField)){
													List<Stmt> fieldUses = fieldToUsesMap.get(taintedField);
													if(!fieldUses.contains(s)){
														fieldUses.add(s);
													}
												}else{
													List<Stmt> fieldUses = new ArrayList<Stmt>();
													fieldUses.add(s);
													fieldToUsesMap.put(taintedField, fieldUses);
												}
												/*
												List<Stmt> fieldDefs = fieldToDefsMap.get(((InstanceFieldRef) rhs).getField());
												for(Stmt fieldDef : fieldDefs){
													if(stmtToNodeMap.containsKey(fieldDef)){
														APIGraphNode fieldDefNode = stmtToNodeMap.get(fieldDef);
														sNode.addPred(fieldDefNode);
														fieldDefNode.addSucc(sNode);
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

										DefWithScope sWS = new DefWithScope(s);
										if(!defs.containsKey(s)){
											defs.put(s, new Vector<Stmt>());
											defsStack.push(sWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
											source = s;
										}
									}
								}
							}
						}
					}

					while(!defsStack.isEmpty()){
						
						if(MyConstants.TO_CONSIDER_LIMIT){
							if(NODE_COUNT > MyConstants.MAX_NODES_CONSIDERED || API_NODE_COUNT > MyConstants.MAX_APINODES_CONSIDERED){
								break;
							}
						}

						DefWithScope defWS = defsStack.pop();
						if(MyConstants.DEBUG_INFO)
							System.out.println("POP from def stack: " + defWS.dump());
						
						Stmt def = defWS.getDef();
						Stmt scope = defWS.getScopeBegin();
						
						if(def.containsInvokeExpr()){
							if(!def.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
								AddTags(def, API_TAG);
							}
						}					
						
						if(def instanceof DefinitionStmt){
							//if(!def.containsInvokeExpr()){
								boolean usesConstant = false;
								List<ValueBox> checkConstUseBoxes = def.getUseBoxes();
								for(ValueBox ccVB : checkConstUseBoxes){
									if(ccVB.getValue() instanceof StringConstant){
										if(!((StringConstant)ccVB.getValue()).value.equals("")){
											usesConstant = true;
											break;
										}
									}
								}
								if(usesConstant){
									AddTags(def, STRING_CONST_TAG);
								}
							//}
						}
							
						APIGraphNode defNode = null;
						if(!stmtToNodeMap.containsKey(def)){
							defNode = new APIGraphNode(def, sMethod);
							stmtToNodeMap.put(def, defNode);	
							NODE_COUNT++;
							if(isAndroidAPICall(def)){
								API_NODE_COUNT++;
							}
						}else{
							defNode = stmtToNodeMap.get(def);
						}
						if(!methodToDDGMap.get(sMethod).contains(defNode)){
							methodToDDGMap.get(sMethod).add(defNode);
						}

						/*
						if(hasEquivTable){
							if(equivTable.containsKey(defWS.getDef())){

								if(MyConstants.DEBUG_INFO)
									System.out.println(sMethod + " has equivTable: " + equivTable);
								List<Stmt> equivs = equivTable.get(defWS.getDef());

								if(MyConstants.DEBUG_INFO)
									System.out.println("EQUIV found: " + defWS.getDef() + "|" + equivs);

								for(Stmt equiv : equivs){
									DefWithScope equivWS = new DefWithScope(equiv);
									if (!defs.containsKey(equiv)) {
										defs.put(equiv, new Vector<Stmt>());
										defsStack.push(equivWS);
										if(MyConstants.DEBUG_INFO)
											System.out.println("def stack doesn't contain " + equivWS.dump() + ". Push it.");
									}
								}
							}
						}
						*/

						Iterator it = body.getUnits().iterator();
						while (it.hasNext()) {
							Stmt s = (Stmt) it.next();
							
							if(defWS.getScopeBegin()!=null){
								if(!isInScope(eug, s, defWS.getScopeBegin())){
									if(MyConstants.DEBUG_INFO){
										System.out.println(s + " is NOT in the scope: " + defWS.getScopeBegin());
									}
									continue;
								}
							}

							Iterator usesIt = s.getUseBoxes().iterator();
							while (usesIt.hasNext()) {
								ValueBox vbox = (ValueBox) usesIt.next();
								if (vbox.getValue() instanceof Local) {
									Local l = (Local) vbox.getValue();

									Iterator rDefsIt = mrd.getDefsOfAt(l, s).iterator();
									while (rDefsIt.hasNext()) {
										Stmt next = (Stmt) rDefsIt.next();

										if(next == defWS.getDef()){

											if (s instanceof InvokeStmt) {
												Vector<Integer> taintVector = new Vector<Integer>();

												Iterator defIt2 = next.getDefBoxes().iterator();
												while (defIt2.hasNext()) {
													ValueBox vbox2 = (ValueBox) defIt2.next();
													if (vbox2.getValue() instanceof Local) {
														InvokeExpr invokeEx = s.getInvokeExpr();
														int argCount = invokeEx.getArgCount();
														for (int i = 0; i < argCount; i++) {
															if (invokeEx.getArg(i) == vbox2.getValue()) {
																taintVector.add(new Integer(i));
															}												
														}

														if(invokeEx instanceof InstanceInvokeExpr){
															if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

																if(invokeEx instanceof SpecialInvokeExpr){
																	if(next instanceof DefinitionStmt){
																		Value rhs = ((DefinitionStmt) next).getRightOp();
																		if(rhs instanceof NewExpr){
																			continue;
																		}
																	}
																}
																taintVector.add(new Integer(MyConstants.thisObject));
															}
														}
													}
												}

												if(taintVector.isEmpty()){

													if(MyConstants.DEBUG_INFO)
														System.out.println("No parameters: " + s);
													continue;
												}

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

													if(MyConstants.DEBUG_INFO){
														System.out.println("call target is " + target);
													}

													boolean noNewTaint = true;
													if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+s.toString())){														
														noNewTaint = false;
														List<Integer> sources = new ArrayList<Integer>();
														sources.addAll(taintVector);
														propagationHistory.put(sMethod.getSignature()+"|"+s.toString(), sources);
													}else{
														List<Integer> sources = propagationHistory.get(sMethod.getSignature()+"|"+s.toString());
														for(Integer taint : taintVector){
															if(!sources.contains(taint)){
																noNewTaint = false;
																sources.add(taint);
															}
														}														
													}
													
													APIGraphNode sNode = null;
													if(!stmtToNodeMap.containsKey(s)){
														sNode = new APIGraphNode(s, sMethod);
														stmtToNodeMap.put(s, sNode);
														NODE_COUNT++;
														if(isAndroidAPICall(s)){
															API_NODE_COUNT++;
														}
													}else{
														sNode = stmtToNodeMap.get(s);
													}
													sNode.addPred(defNode);
													defNode.addSucc(sNode);
													if(!methodToDDGMap.get(sMethod).contains(sNode)){
														methodToDDGMap.get(sMethod).add(sNode);
													}

													DefWithScope sWS = new DefWithScope(s);
													if (!defs.containsKey(s)) {
														defs.put(s, new Vector<Stmt>());
														defsStack.push(sWS);
														if(MyConstants.DEBUG_INFO)
															System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
													}else{
														if(noNewTaint){
															break;
														}
													}

													if(MyConstants.DEBUG_INFO){
														System.out.println("PROPAGATING from METHOD: " + sMethod);
														System.out.println("PROPAGATING from STATEMENT: " + s);
													}
													GlobalForwardDataflowAnalysis.taintedFieldsInCaller.addAll(taintedFields);
													//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCaller.putAll(taintedFieldToPTSMap);

													Vector<Integer> tainted = propagate(target, taintVector, leakSource, s, sMethod);
													
													for(SootField sf : GlobalForwardDataflowAnalysis.taintedFieldsInCallee){
														if(!taintedFields.contains(sf)){
															taintedFields.add(sf);
														}
													}
													/*
													Set<SootField> taintedFieldSet = GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.keySet();
													Iterator<SootField> taintedFieldIter = taintedFieldSet.iterator();
													while(taintedFieldIter.hasNext()){
														SootField tf = taintedFieldIter.next();
														if(!taintedFieldToPTSMap.containsKey(tf)){
															taintedFieldToPTSMap.put(tf, GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.get(tf));
														}
													}											
													*/
													GlobalForwardDataflowAnalysis.taintedFieldsInCallee.clear();
													//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.clear();

													if(MyConstants.DEBUG_INFO){
														System.out.println(s + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
													}
													if ((tainted!=null) && (!tainted.isEmpty())) {

														for(Integer i : tainted){
															int index = i.intValue();

															if(index == MyConstants.returnValue){
																if(s instanceof DefinitionStmt){
																	Value taintedRet = ((DefinitionStmt) s).getLeftOp();
																	if(taintedRet instanceof Local){

																		if (!defs.containsKey(s)) {
																			
																			sNode.addPred(defNode);
																			defNode.addSucc(sNode);
																			if(!methodToDDGMap.get(sMethod).contains(sNode)){
																				methodToDDGMap.get(sMethod).add(sNode);
																			}

																			if(MyConstants.DEBUG_INFO)
																				System.out.println("adding def of return value:" + s);

																			defs.put(s, new Vector<Stmt>());
																			defsStack.push(sWS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
																		}
																	}
																}

															}else if(index == MyConstants.thisObject){
																if(s.getInvokeExpr() instanceof InstanceInvokeExpr){
																	Value taintedThisRef = ((InstanceInvokeExpr)s.getInvokeExpr()).getBase();

																	boolean hasDef = false;
																	Stmt def0 = s;
																	if(taintedThisRef instanceof Local){
																		List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, def0);
																		if(defs0.size()==1){
																			def0 = (Stmt)defs0.get(0);
																			hasDef = true;
																			
																			APIGraphNode def0Node = null;
																			if(!stmtToNodeMap.containsKey(def0)){
																				def0Node = new APIGraphNode(def0, sMethod);
																				stmtToNodeMap.put(def0, def0Node);
																				NODE_COUNT++;
																				if(isAndroidAPICall(def0)){
																					API_NODE_COUNT++;
																				}
																			}else{
																				def0Node = stmtToNodeMap.get(def0);
																			}
																			def0Node.addPred(sNode);
																			sNode.addSucc(def0Node);
																			if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																				methodToDDGMap.get(sMethod).add(def0Node);
																			}

																			DefWithScope def0WS = new DefWithScope(def0, s);
																			if(!defs.containsKey(def0)){
																				Vector<Stmt> scopes = new Vector<Stmt>();
																				scopes.add(s);
																				defs.put(def0, scopes);
																				defsStack.push(def0WS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																			}else if(!(defs.get(def0).contains(s))){
																				defs.get(def0).add(s);
																				defsStack.push(def0WS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																			}
																		}else{
																			//not very good solution :(
																			for(Unit defn : defs0){
																				
																				APIGraphNode defnNode = null;
																				if(!stmtToNodeMap.containsKey((Stmt)defn)){
																					defnNode = new APIGraphNode((Stmt)defn, sMethod);
																					stmtToNodeMap.put((Stmt)defn, defnNode);
																					NODE_COUNT++;
																					if(isAndroidAPICall((Stmt)defn)){
																						API_NODE_COUNT++;
																					}
																				}else{
																					defnNode = stmtToNodeMap.get((Stmt)defn);
																				}
																				defnNode.addPred(sNode);
																				sNode.addSucc(defnNode);
																				if(!methodToDDGMap.get(sMethod).contains(defnNode)){
																					methodToDDGMap.get(sMethod).add(defnNode);
																				}

																				DefWithScope defnWS = new DefWithScope((Stmt)defn, s);
																				if(!defs.containsKey(defn)){
																					Vector<Stmt> scopes = new Vector<Stmt>();
																					scopes.add(s);
																					defs.put((Stmt)defn, scopes);
																					defsStack.push(defnWS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																				}else if(!(defs.get(defn).contains(s))){
																					defs.get(defn).add(s);
																					defsStack.push(defnWS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																				}
																			}
																		}
																	}

																															
																}														 

															}else if(index >= 0){

																Value taintedArg = s.getInvokeExpr().getArg(index);


																boolean hasDef = false;
																Stmt def0 = s;
																if(taintedArg instanceof Local && taintedArg.getType() instanceof RefLikeType){
																	List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, def0);
																	if(defs0.size()==1){
																		def0 = (Stmt)defs0.get(0);
																		hasDef = true;
																		
																		APIGraphNode def0Node = null;
																		if(!stmtToNodeMap.containsKey(def0)){
																			def0Node = new APIGraphNode(def0, sMethod);
																			stmtToNodeMap.put(def0, def0Node);
																			NODE_COUNT++;
																			if(isAndroidAPICall(def0)){
																				API_NODE_COUNT++;
																			}
																		}else{
																			def0Node = stmtToNodeMap.get(def0);
																		}
																		def0Node.addPred(sNode);
																		sNode.addSucc(def0Node);
																		if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																			methodToDDGMap.get(sMethod).add(def0Node);
																		}

																		DefWithScope def0WS = new DefWithScope(def0, s);
																		if(!defs.containsKey(def0)){
																			Vector<Stmt> scopes = new Vector<Stmt>();
																			scopes.add(s);
																			defs.put(def0, scopes);
																			defsStack.push(def0WS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																		}else if(!(defs.get(def0).contains(s))){
																			defs.get(def0).add(s);
																			defsStack.push(def0WS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																		}
																	}
																}

																
															}
														}
													}

												}

											} else {

												boolean isInvoke = false;

												Iterator iUse = s.getUseBoxes().iterator();
												while (iUse.hasNext()) {
													ValueBox vB = (ValueBox) iUse.next();
													if (vB.getValue() instanceof InvokeExpr) {
														isInvoke = true;
													}
												}

												if (isInvoke) {
													Vector<Integer> taintVector = new Vector<Integer>();

													Iterator defIt2 = next.getDefBoxes().iterator();
													while (defIt2.hasNext()) {
														ValueBox vbox2 = (ValueBox) defIt2.next();
														if (vbox2.getValue() instanceof Local) {
															InvokeExpr invokeEx = s.getInvokeExpr();
															int argCount = invokeEx.getArgCount();
															for (int i = 0; i < argCount; i++) {
																if (invokeEx.getArg(i) == vbox2.getValue()) {
																	taintVector.add(new Integer(i));
																}
															}

															if(invokeEx instanceof InstanceInvokeExpr){
																if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

																	if(invokeEx instanceof SpecialInvokeExpr){
																		if(next instanceof DefinitionStmt){
																			Value rhs = ((DefinitionStmt) next).getRightOp();
																			if(rhs instanceof NewExpr){
																				continue;
																			}
																		}
																	}
																	taintVector.add(new Integer(MyConstants.thisObject));
																}
															}

														}
													}

													if(taintVector.isEmpty()){
														if(MyConstants.DEBUG_INFO)
															System.out.println("No parameters: " + s);
														continue;
													}

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

														if(MyConstants.DEBUG_INFO){
															System.out.println("call target is " + target);
														}

														boolean noNewTaint = true;
														if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+s.toString())){															
															noNewTaint = false;
															List<Integer> sources = new ArrayList<Integer>();
															sources.addAll(taintVector);
															propagationHistory.put(sMethod.getSignature()+"|"+s.toString(), sources);
														}else{
															List<Integer> sources = propagationHistory.get(sMethod.getSignature()+"|"+s.toString());

															for(Integer taint : taintVector){
																if(!sources.contains(taint)){
																	noNewTaint = false;
																	sources.add(taint);
																}
															}

														}
														
														APIGraphNode sNode = null;
														if(!stmtToNodeMap.containsKey(s)){
															sNode = new APIGraphNode(s, sMethod);
															stmtToNodeMap.put(s, sNode);
															NODE_COUNT++;
															if(isAndroidAPICall(s)){
																API_NODE_COUNT++;
															}
														}else{
															sNode = stmtToNodeMap.get(s);
														}
														sNode.addPred(defNode);
														defNode.addSucc(sNode);
														if(!methodToDDGMap.get(sMethod).contains(sNode)){
															methodToDDGMap.get(sMethod).add(sNode);
														}

														DefWithScope sWS = new DefWithScope(s);
														if (!defs.containsKey(s)) {
															defs.put(s, new Vector<Stmt>());
															defsStack.push(sWS);
															if(MyConstants.DEBUG_INFO)
																System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
														}else{
															if(noNewTaint){
																break;
															}
														}

														if(MyConstants.DEBUG_INFO){
															System.out.println("PROPAGATING from METHOD: " + sMethod);
															System.out.println("PROPAGATING from STATEMENT: " + s);
														}
														GlobalForwardDataflowAnalysis.taintedFieldsInCaller.addAll(taintedFields);
														//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCaller.putAll(taintedFieldToPTSMap);

														Vector<Integer> tainted = propagate(target, taintVector, leakSource, s, sMethod);
														
														for(SootField sf : GlobalForwardDataflowAnalysis.taintedFieldsInCallee){
															if(!taintedFields.contains(sf)){
																taintedFields.add(sf);
															}
														}
														/*
														Set<SootField> taintedFieldSet = GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.keySet();
														Iterator<SootField> taintedFieldIter = taintedFieldSet.iterator();
														while(taintedFieldIter.hasNext()){
															SootField tf = taintedFieldIter.next();
															if(!taintedFieldToPTSMap.containsKey(tf)){
																taintedFieldToPTSMap.put(tf, GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.get(tf));
															}
														}											
														*/
														GlobalForwardDataflowAnalysis.taintedFieldsInCallee.clear();
														//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.clear();
														
														if(MyConstants.DEBUG_INFO){
															System.out.println(s + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
														}
														if ((tainted!=null) && (!tainted.isEmpty())) {

															for(Integer i : tainted){
																int index = i.intValue();

																if(index == MyConstants.returnValue){
																	if(s instanceof DefinitionStmt){
																		Value taintedRet = ((DefinitionStmt) s).getLeftOp();
																		if(taintedRet instanceof Local){

																			if (!defs.containsKey(s)) {
																				
																				sNode.addPred(defNode);
																				defNode.addSucc(sNode);
																				if(!methodToDDGMap.get(sMethod).contains(sNode)){
																					methodToDDGMap.get(sMethod).add(sNode);
																				}

																				if(MyConstants.DEBUG_INFO)
																					System.out.println("adding def of return value:" + s);

																				defs.put(s, new Vector<Stmt>());
																				defsStack.push(sWS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
																			}
																		}
																	}

																}else if(index == MyConstants.thisObject){
																	if(s.getInvokeExpr() instanceof InstanceInvokeExpr){
																		Value taintedThisRef = ((InstanceInvokeExpr)s.getInvokeExpr()).getBase();

																		boolean hasDef = false;
																		Stmt def0 = s;
																		if(taintedThisRef instanceof Local){
																			List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, def0);
																			if(defs0.size()==1){
																				def0 = (Stmt)defs0.get(0);
																				hasDef = true;
																				
																				APIGraphNode def0Node = null;
																				if(!stmtToNodeMap.containsKey(def0)){
																					def0Node = new APIGraphNode(def0, sMethod);
																					stmtToNodeMap.put(def0, def0Node);
																					NODE_COUNT++;
																					if(isAndroidAPICall(def0)){
																						API_NODE_COUNT++;
																					}
																				}else{
																					def0Node = stmtToNodeMap.get(def0);
																				}
																				def0Node.addPred(sNode);
																				sNode.addSucc(def0Node);
																				if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																					methodToDDGMap.get(sMethod).add(def0Node);
																				}

																				DefWithScope def0WS = new DefWithScope(def0, s);
																				if(!defs.containsKey(def0)){
																					Vector<Stmt> scopes = new Vector<Stmt>();
																					scopes.add(s);
																					defs.put(def0, scopes);
																					defsStack.push(def0WS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																				}else if(!(defs.get(def0).contains(s))){
																					defs.get(def0).add(s);
																					defsStack.push(def0WS);
																					if(MyConstants.DEBUG_INFO)
																						System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																				}
																			}else{
																				//not very good solution :(
																				for(Unit defn : defs0){
																					
																					APIGraphNode defnNode = null;
																					if(!stmtToNodeMap.containsKey((Stmt)defn)){
																						defnNode = new APIGraphNode((Stmt)defn, sMethod);
																						stmtToNodeMap.put((Stmt)defn, defnNode);
																						NODE_COUNT++;
																						if(isAndroidAPICall((Stmt)defn)){
																							API_NODE_COUNT++;
																						}
																					}else{
																						defnNode = stmtToNodeMap.get((Stmt)defn);
																					}
																					defnNode.addPred(sNode);
																					sNode.addSucc(defnNode);
																					if(!methodToDDGMap.get(sMethod).contains(defnNode)){
																						methodToDDGMap.get(sMethod).add(defnNode);
																					}

																					DefWithScope defnWS = new DefWithScope((Stmt)defn, s);
																					if(!defs.containsKey(defn)){
																						Vector<Stmt> scopes = new Vector<Stmt>();
																						scopes.add(s);
																						defs.put((Stmt)defn, scopes);
																						defsStack.push(defnWS);
																						if(MyConstants.DEBUG_INFO)
																							System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																					}else if(!(defs.get(defn).contains(s))){
																						defs.get(defn).add(s);
																						defsStack.push(defnWS);
																						if(MyConstants.DEBUG_INFO)
																							System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																					}
																				}
																			}
																		}
																															
																	}														 

																}else if(index >= 0){

																	Value taintedArg = s.getInvokeExpr().getArg(index);

																	boolean hasDef = false;
																	Stmt def0 = s;
																	if(taintedArg instanceof Local && taintedArg.getType() instanceof RefLikeType){
																		List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, def0);
																		if(defs0.size()==1){
																			def0 = (Stmt)defs0.get(0);
																			hasDef = true;
																			
																			APIGraphNode def0Node = null;
																			if(!stmtToNodeMap.containsKey(def0)){
																				def0Node = new APIGraphNode(def0, sMethod);
																				stmtToNodeMap.put(def0, def0Node);
																				NODE_COUNT++;
																				if(isAndroidAPICall(def0)){
																					API_NODE_COUNT++;
																				}
																			}else{
																				def0Node = stmtToNodeMap.get(def0);
																			}
																			def0Node.addPred(sNode);
																			sNode.addSucc(def0Node);
																			if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																				methodToDDGMap.get(sMethod).add(def0Node);
																			}

																			DefWithScope def0WS = new DefWithScope(def0, s);
																			if(!defs.containsKey(def0)){
																				Vector<Stmt> scopes = new Vector<Stmt>();
																				scopes.add(s);
																				defs.put(def0, scopes);
																				defsStack.push(def0WS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																			}else if(!(defs.get(def0).contains(s))){
																				defs.get(def0).add(s);
																				defsStack.push(def0WS);
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																			}
																		}
																	}
																	
																}
															}
														}

													}
													// invokes.add(s);
												} else if(s instanceof ReturnStmt){

													if(MyConstants.DEBUG_INFO){
														System.out.println("returning to caller...");
													}
													if(MyConstants.DEBUG_INFO)
														System.out.println("return to caller from: " + sMethod + " | " + s);
													
													APIGraphNode sNode = null;
													if(!stmtToNodeMap.containsKey(s)){
														sNode = new APIGraphNode(s, sMethod);
														stmtToNodeMap.put(s, sNode);
														NODE_COUNT++;
														if(isAndroidAPICall(s)){
															API_NODE_COUNT++;
														}
													}else{
														sNode = stmtToNodeMap.get(s);
													}
													sNode.addPred(defNode);
													defNode.addSucc(sNode);
													if(!methodToDDGMap.get(sMethod).contains(sNode)){
														methodToDDGMap.get(sMethod).add(sNode);
													}

													DefWithScope sWS = new DefWithScope(s);
													if (!defs.containsKey(s)) {
														defs.put(s, new Vector<Stmt>());
														defsStack.push(sWS);
														if(MyConstants.DEBUG_INFO)
															System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
													}

													Chain<SootClass> classes1 = Scene.v().getClasses();
													Iterator<SootClass> classes_iter1 = classes1.iterator();
													while (classes_iter1.hasNext()) {
														SootClass soot_class = classes_iter1.next();

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
																			
																			APIGraphNode callerStmtNode = null;
																			if(!stmtToNodeMap.containsKey(callerStmt)){
																				callerStmtNode = new APIGraphNode(callerStmt, method);
																				stmtToNodeMap.put(callerStmt, callerStmtNode);
																				NODE_COUNT++;
																			}else{
																				callerStmtNode = stmtToNodeMap.get(callerStmt);
																			}
																			sNode.addSucc(callerStmtNode);
																			callerStmtNode.addPred(sNode);
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
																			
																			if(!sourceMethods.contains(sMethod)){
																				if(MyConstants.DEBUG_INFO)
																					System.out.println("adding sourceMethod: " + sMethod);
																				sourceMethods.add(sMethod);
																			}
																		}
																	}else{
																		Iterator targets = new Targets(cg.edgesOutOf(callerStmt));													

																		while (targets.hasNext()) {
																			SootMethod target = (SootMethod) targets.next();
																			//System.out.println(method + " may call " + target);
																			if(target.equals(sMethod)){
																				
																				APIGraphNode callerStmtNode = null;
																				if(!stmtToNodeMap.containsKey(callerStmt)){
																					callerStmtNode = new APIGraphNode(callerStmt, method);
																					stmtToNodeMap.put(callerStmt, callerStmtNode);
																					NODE_COUNT++;
																				}else{
																					callerStmtNode = stmtToNodeMap.get(callerStmt);
																				}
																				sNode.addSucc(callerStmtNode);
																				callerStmtNode.addPred(sNode);
																				
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
															/*
															Iterator targets = new Targets(cg.edgesOutOf(method));													

															while (targets.hasNext()) {
																SootMethod target = (SootMethod) targets.next();
																//System.out.println(method + " may call " + target);
																if(target.equals(sMethod)){
																	if(!fullWorklist.contains(method)){
																		worklist.add(method);
																		fullWorklist.add(method);
																		if(!sourceMethods.contains(sMethod)){
																			sourceMethods.add(sMethod);
																		}	
																		
																		Iterator iterCall = method.retrieveActiveBody().getUnits().iterator();
																		while(iterCall.hasNext()){
																			Stmt call = (Stmt)iterCall.next();
																			if(call.containsInvokeExpr()){
																				Iterator tars = new Targets(cg.edgesOutOf(call));
																				while(tars.hasNext()){
																					SootMethod tar = (SootMethod)tars.next();
																					if(tar.equals(sMethod)){
																						APIGraphNode callNode = null;
																						if(!stmtToNodeMap.containsKey(call)){
																							callNode = new APIGraphNode(call, method);
																							stmtToNodeMap.put(call, callNode);
																							NODE_COUNT++;
																							if(isAndroidAPICall(call)){
																								API_NODE_COUNT++;
																							}
																						}else{
																							callNode = stmtToNodeMap.get(call);
																						}
																						callNode.addPred(sNode);
																						sNode.addSucc(callNode);
																						if(!methodToDDGMap.containsKey(method)){
																							List<APIGraphNode> ddg = new ArrayList<APIGraphNode>();
																							ddg.add(callNode);
																							methodToDDGMap.put(method, ddg);
																						}else{
																							if(!methodToDDGMap.get(method).contains(callNode)){
																								methodToDDGMap.get(method).add(callNode);
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
														}
													}
												}										

												else {
													if(s instanceof DefinitionStmt){
														Value lhs = ((DefinitionStmt) s).getLeftOp();
														if(lhs instanceof ArrayRef){
															Value base = ((ArrayRef) lhs).getBase();
															if(base == l){
																continue;
															}
														}else if(lhs instanceof InstanceFieldRef){
															Value base = ((InstanceFieldRef) lhs).getBase();
															if(base == l){
																continue;
															}
														}
													}
													
													APIGraphNode sNode = null;
													if(!stmtToNodeMap.containsKey(s)){
														sNode = new APIGraphNode(s, sMethod);
														stmtToNodeMap.put(s, sNode);
														NODE_COUNT++;
														if(isAndroidAPICall(s)){
															API_NODE_COUNT++;
														}
													}else{
														sNode = stmtToNodeMap.get(s);
													}
													sNode.addPred(defNode);
													defNode.addSucc(sNode);
													if(!methodToDDGMap.get(sMethod).contains(sNode)){
														methodToDDGMap.get(sMethod).add(sNode);
													}

													DefWithScope sWS = new DefWithScope(s);
													if (!defs.containsKey(s)) {
																											
														boolean isRhsInstanceRef = false;
														if(s instanceof DefinitionStmt){
															Value rhs = ((DefinitionStmt) s).getRightOp();
															if(rhs instanceof InstanceFieldRef){
																Value base = ((InstanceFieldRef) rhs).getBase();
																if(base == l){
																	isRhsInstanceRef = true;
																}
															}
														}		
														
														if(!isRhsInstanceRef){
															defs.put(s, new Vector<Stmt>());
															defsStack.push(sWS);
															if(MyConstants.DEBUG_INFO)
																System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
														}

														if(s instanceof DefinitionStmt){
															Value lhs = ((DefinitionStmt) s).getLeftOp();
															if(lhs instanceof StaticFieldRef){
																
																if(MyConstants.TO_TAINT_STATIC_FIELD){
																	if(!taintedFields.contains(((StaticFieldRef)lhs).getField())){
																		taintedFields.add(((StaticFieldRef)lhs).getField());
																		fWorklist.add(((StaticFieldRef)lhs).getField());
																		
																		/*
																		SootField fieldKey = ((StaticFieldRef)lhs).getField();
																		if(fieldToDefsMap.containsKey(fieldKey)){
																			List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
																			if(!fieldDefs.contains(s)){
																				fieldDefs.add(s);
																			}
																		}else{
																			List<Stmt> fieldDefs = new ArrayList<Stmt>();
																			fieldDefs.add(s);
																			fieldToDefsMap.put(fieldKey, fieldDefs);																	
																		}
																		*/
																	}
																	
																	SootField fieldKey = ((StaticFieldRef)lhs).getField();
																	if(fieldToDefsMap.containsKey(fieldKey)){
																		List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
																		if(!fieldDefs.contains(s)){
																			fieldDefs.add(s);
																		}
																	}else{
																		List<Stmt> fieldDefs = new ArrayList<Stmt>();
																		fieldDefs.add(s);
																		fieldToDefsMap.put(fieldKey, fieldDefs);																	
																	}
																}
															}
														}

														boolean hasDef = false;
														Stmt def0 = s;

														if(def0 instanceof DefinitionStmt){
															Value lhs = ((DefinitionStmt) def0).getLeftOp();

															if(lhs instanceof InstanceFieldRef){

																if(MyConstants.TO_TAINT_INSTANCE_FIELD){
																	if(!taintedFields.contains(((InstanceFieldRef)lhs).getField())){
																		taintedFields.add(((InstanceFieldRef)lhs).getField());
																		fWorklist.add(((InstanceFieldRef)lhs).getField());
																		
																		/*
																		SootField fieldKey = ((InstanceFieldRef)lhs).getField();
																		if(fieldToDefsMap.containsKey(fieldKey)){
																			List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
																			if(!fieldDefs.contains(def0)){
																				fieldDefs.add(def0);
																			}
																		}else{
																			List<Stmt> fieldDefs = new ArrayList<Stmt>();
																			fieldDefs.add(def0);
																			fieldToDefsMap.put(fieldKey, fieldDefs);																	
																		}
																		*/
																	}
																	
																	SootField fieldKey = ((InstanceFieldRef)lhs).getField();
																	if(fieldToDefsMap.containsKey(fieldKey)){
																		List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
																		if(!fieldDefs.contains(def0)){
																			fieldDefs.add(def0);
																		}
																	}else{
																		List<Stmt> fieldDefs = new ArrayList<Stmt>();
																		fieldDefs.add(def0);
																		fieldToDefsMap.put(fieldKey, fieldDefs);																	
																	}
																}
																
															}else if(lhs instanceof ArrayRef){
																Value base = ((ArrayRef) lhs).getBase();
																if(base instanceof Local){
																	List<Unit> defs0 = mrd.getDefsOfAt((Local)base, def0);
																	if(defs0.size()==1){
																		def0 = (Stmt)defs0.get(0);
																		hasDef = true;
																		
																		APIGraphNode def0Node = null;
																		if(!stmtToNodeMap.containsKey(def0)){
																			def0Node = new APIGraphNode(def0, sMethod);
																			stmtToNodeMap.put(def0, def0Node);
																			NODE_COUNT++;
																			if(isAndroidAPICall(def0)){
																				API_NODE_COUNT++;
																			}
																		}else{
																			def0Node = stmtToNodeMap.get(def0);
																		}
																		def0Node.addPred(sNode);
																		sNode.addSucc(def0Node);
																		if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																			methodToDDGMap.get(sMethod).add(def0Node);
																		}

																		DefWithScope def0WS = new DefWithScope(def0, s);
																		if(!defs.containsKey(def0)){
																			Vector<Stmt> scopes = new Vector<Stmt>();
																			scopes.add(s);
																			defs.put(def0, scopes);
																			defsStack.push(def0WS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																		}else if(!(defs.get(def0).contains(s))){
																			defs.get(def0).add(s);
																			defsStack.push(def0WS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																		}
																	}
																}
															}
														}

													}
												}//end else
											}
										}
									}

								}
							}
						}

					}// end while(!delta.isEmpty())


					Iterator i = defs.keySet().iterator();
					while (i.hasNext()) {
						Stmt s = (Stmt)i.next();
						AddTags(s, generalTaintTag);
						AddTags(s, taintTag);
						AddTags(s, taintStaticTag);
						//System.out.print(s + "|");

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
										AddTags(s, taintStaticTag);
									}else 
										AddTags(s, taintStaticTag);
								}
							}
						}

					}

					if(MyConstants.DEBUG_INFO){
						System.out.println();
						System.out.println("method:" + sMethod.getSignature());
						System.out.println("dataflow for " + source + ":");
					}

					Iterator printIt = body.getUnits().iterator();
					while(printIt.hasNext()){
						Stmt s = (Stmt)printIt.next();

						if((s.getTags().contains(taintTag) || s.getTags().contains(extraDefTag))
								&& s.getTags().contains(taintStaticTag)){

							if(MyConstants.DEBUG_INFO){
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
									taintSourceToField.put(leakSource, fieldList);
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
									taintSourceToField.put(leakSource, fieldList);
									classToSourceToField.put(sMethod.getDeclaringClass().getName(), taintSourceToField);

								}else if(vb.getValue() instanceof Local){

									String varName = ((Local)vb.getValue()).getName();								
									LinkedHashMap<String, List<String>> taintSourceToVar = new LinkedHashMap<String, List<String>>();
									List<String> varList = new ArrayList<String>();
									if(varList.contains(varName)){
										varList.add(varName);
									}								
									taintSourceToVar.put(leakSource, varList);
									methodToSourceToVar.put(sMethod.getSignature(), taintSourceToVar);
								}
							}
						}

					}

					if(MyConstants.DEBUG_INFO){
						System.out.println("end dataflow for " + source + "\n");
					}

				}//while(!worklist.isEmpty())


			}//end while(!fWorklist.isEmpty())

			
			/*
			for(SootField f : instanceFields){
				//addTaintField(f);
				if(!usedInstanceFields.contains(f)){
					usedInstanceFields.add(f);
				}
			}

			for(SootField f : staticFields){
				//addTaintField(f);
				if(!usedStaticFields.contains(f)){
					usedStaticFields.add(f);
				}
			}
			 */
			
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

		}
	}
	
	private void AddTags(Stmt s, TaintTag tag)
	{
		if(!s.getTags().contains(tag)){
			s.addTag(tag);
		}
	}

	private Vector<Integer> propagate(SootMethod sMethod, Vector<Integer> taintIndexes, String leakSource, Stmt from, SootMethod fromMethod){

		if(callString.contains(sMethod)){

			if(MyConstants.DEBUG_INFO)
				System.out.println("RECURSIVE call found, return null");
			return null;
		}
		
		if(!methodToDDGMap.containsKey(sMethod)){
			List<APIGraphNode> apiGraph = new ArrayList<APIGraphNode>();
			methodToDDGMap.put(sMethod, apiGraph);
		}

		TaintTag taintTag = taintTagMap.get(leakSource);
		TaintTag extraDefTag = extraDefTagMap.get(leakSource);

		callString.push(sMethod);			

		if(MyConstants.DEBUG_INFO){
			System.out.println("step into method: " + sMethod + "|taintIndexes: " + taintIndexes + "\n");
		}

		if(sMethod.getSignature().equals("<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>")){
			System.out.println("REFLECTION on path");			
		}else if(sMethod.isNative() && sMethod.getDeclaringClass().isApplicationClass()){
			System.out.println("NATIVE on path");			
		}

		/*
		if(!sMethod.isConcrete()){

			System.out.println("method is NOT CONCRETE!");
			callString.pop();
			return null;
		}
		 */

		//List<SootField> instanceFields = new ArrayList<SootField>();
		//List<SootField> staticFields = new ArrayList<SootField>();

		LinkedHashMap<SootField, Vector<Integer>> instanceFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();
		LinkedHashMap<SootField, Vector<Integer>> staticFieldMap = new LinkedHashMap<SootField, Vector<Integer>>();

		List<SootField> taintedFields = new ArrayList<SootField>();
		taintedFields.addAll(GlobalForwardDataflowAnalysis.taintedFieldsInCaller);
		GlobalForwardDataflowAnalysis.taintedFieldsInCaller.clear();

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
					taintResult.add(new Integer(MyConstants.returnValue));
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
					if(taintIndexes.contains(source)){
						for(Integer dest:dests){
							if(!taintResult.contains(dest)){
								taintResult.add(dest);
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

		Stmt source = null;
		//Vector<Stmt> defs = new Vector<Stmt>();
		//Stack<Stmt> defsStack = new Stack<Stmt>();

		//Vector<DefWithScope> defs = new Vector<DefWithScope>();
		LinkedHashMap<Stmt, Vector<Stmt>> defs = new LinkedHashMap<Stmt, Vector<Stmt>>();		

		Stack<DefWithScope> defsStack = new Stack<DefWithScope>();

		//Vector<Stmt> taintedRefDefs = new Vector<Stmt>();
		Vector<DefWithScope> taintedRefDefs = new Vector<DefWithScope>();

		JimpleBody body = (JimpleBody) sMethod.retrieveActiveBody();

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
								
								APIGraphNode parameterNode = null;
								if(!stmtToNodeMap.containsKey(s)){
									parameterNode = new APIGraphNode(s, sMethod);
									stmtToNodeMap.put(s, parameterNode);	
									NODE_COUNT++;
									if(isAndroidAPICall(s)){
										API_NODE_COUNT++;
									}
								}else{
									parameterNode = stmtToNodeMap.get(s);
								}
								if(!methodToDDGMap.get(sMethod).contains(parameterNode)){
									methodToDDGMap.get(sMethod).add(parameterNode);
								}
								
								APIGraphNode fromNode = null;
								if(!stmtToNodeMap.containsKey(from)){
									fromNode = new APIGraphNode(from, fromMethod);
									stmtToNodeMap.put(from, fromNode);
									NODE_COUNT++;
									if(isAndroidAPICall(from)){
										API_NODE_COUNT++;
									}
								}else{
									fromNode = stmtToNodeMap.get(from);
								}
								if(!methodToDDGMap.get(fromMethod).contains(fromNode)){
									methodToDDGMap.get(fromMethod).add(fromNode);
								}
								
								parameterNode.addPred(fromNode);
								fromNode.addSucc(parameterNode);

								DefWithScope sWS = new DefWithScope(s);
								if (!defs.containsKey(s)) {
									defs.put(s, new Vector<Stmt>());
									defsStack.push(sWS);
									if(MyConstants.DEBUG_INFO)
										System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
									source = s;
								}

							}

						}else if(vBox.getValue() instanceof ThisRef){
							if(taintIndexes.contains(new Integer(MyConstants.thisObject))){
								
								APIGraphNode thisNode = null;
								if(!stmtToNodeMap.containsKey(s)){
									thisNode = new APIGraphNode(s, sMethod);
									stmtToNodeMap.put(s, thisNode);		
									NODE_COUNT++;
									if(isAndroidAPICall(s)){
										API_NODE_COUNT++;
									}
								}else{
									thisNode = stmtToNodeMap.get(s);
								}
								if(!methodToDDGMap.get(sMethod).contains(thisNode)){
									methodToDDGMap.get(sMethod).add(thisNode);
								}
								
								APIGraphNode fromNode = null;
								if(!stmtToNodeMap.containsKey(from)){
									fromNode = new APIGraphNode(from, fromMethod);
									stmtToNodeMap.put(from, fromNode);	
									NODE_COUNT++;
									if(isAndroidAPICall(from)){
										API_NODE_COUNT++;
									}
								}else{
									fromNode = stmtToNodeMap.get(from);
								}
								if(!methodToDDGMap.get(fromMethod).contains(fromNode)){
									methodToDDGMap.get(fromMethod).add(fromNode);
								}
								
								thisNode.addPred(fromNode);
								fromNode.addSucc(thisNode);

								DefWithScope sWS = new DefWithScope(s);
								if (!defs.containsKey(s)) {
									defs.put(s, new Vector<Stmt>());
									defsStack.push(sWS);
									if(MyConstants.DEBUG_INFO)
										System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
									source = s;
								}
							}
						}
					}
				}
			}
		}

		if (defs.isEmpty()) {
			callString.pop();
			return null;
		}

		ExceptionalUnitGraph eug = new ExceptionalUnitGraph(body);

		/*
		if(CFG==true){
			System.out.println("BEGIN [CFG] - " + sMethod + "\n");
			System.out.println(eug.toString());
			System.out.println("END [CFG]");
		}
		 */

		MyReachingDefinition mrd = new MyReachingDefinition(eug);

		/*
		if(REACH_DEF){
			System.out.println("BEGIN [Reaching Definition Analysis] - " + sMethod + "\n");
			dumpReachingDefs(mrd, body);
			System.out.println("END [Reaching Definition Analysis]");
		}
		 */

		while(!defsStack.isEmpty()){

			DefWithScope defWS = defsStack.pop();
			if(MyConstants.DEBUG_INFO)
				System.out.println("POP from def stack: " + defWS.dump());
			
			Stmt def = defWS.getDef();
			Stmt scope = defWS.getScopeBegin();
			
			if(def.containsInvokeExpr()){
				if(!def.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
					AddTags(def, API_TAG);
				}
			}
			
			if(def instanceof DefinitionStmt){
				//if(!def.containsInvokeExpr()){
					boolean usesConstant = false;
					List<ValueBox> checkConstUseBoxes = def.getUseBoxes();
					for(ValueBox ccVB : checkConstUseBoxes){
						if(ccVB.getValue() instanceof StringConstant){
							if(!((StringConstant)ccVB.getValue()).value.equals("")){
								usesConstant = true;
								break;
							}
						}
					}					
					if(usesConstant){
						AddTags(def, STRING_CONST_TAG);
					}
				//}
			}
			
			APIGraphNode defNode = null;
			if(!stmtToNodeMap.containsKey(def)){
				defNode = new APIGraphNode(def, sMethod);
				stmtToNodeMap.put(def, defNode);
				NODE_COUNT++;
				if(isAndroidAPICall(def)){
					API_NODE_COUNT++;
				}
			}else{
				defNode = stmtToNodeMap.get(def);
			}
			if(!methodToDDGMap.get(sMethod).contains(defNode)){
				methodToDDGMap.get(sMethod).add(defNode);
			}

			/*
			if(hasEquivTable){

				if(equivTable.containsKey(defWS.getDef())){
					if(MyConstants.DEBUG_INFO)
						System.out.println(sMethod + "has equivTable: " + equivTable);
					List<Stmt> equivs = equivTable.get(defWS.getDef());

					if(MyConstants.DEBUG_INFO)
						System.out.println("EQUIV found: " + defWS.getDef() + "|" + equivs);

					for(Stmt equiv : equivs){
						DefWithScope equivWS = new DefWithScope(equiv);
						if (!defs.containsKey(equiv)) {
							defs.put(equiv, new Vector<Stmt>());
							defsStack.push(equivWS);
							if(MyConstants.DEBUG_INFO)
								System.out.println("def stack doesn't contain " + equivWS.dump() + ". Push it.");
						}
					}
				}
			}
			*/

			Iterator it = body.getUnits().iterator();
			while (it.hasNext()) {
				Stmt s = (Stmt) it.next();

				
				if(defWS.getScopeBegin()!=null){
					if(!isInScope(eug, s, defWS.getScopeBegin())){
						if(MyConstants.DEBUG_INFO){
							System.out.println(s + " is NOT in the scope: " + defWS.getScopeBegin());
						}
						continue;
					}
				}

				Iterator usesIt = s.getUseBoxes().iterator();
				while (usesIt.hasNext()) {
					ValueBox vbox = (ValueBox) usesIt.next();
					if (vbox.getValue() instanceof Local) {
						Local l = (Local) vbox.getValue();

						Iterator rDefsIt = mrd.getDefsOfAt(l, s).iterator();
						while (rDefsIt.hasNext()) {
							Stmt next = (Stmt) rDefsIt.next();

							if(next == defWS.getDef()){

								if (s instanceof InvokeStmt) {

									Vector<Integer> taintVector = new Vector<Integer>();

									Iterator defIt2 = next.getDefBoxes().iterator();
									while (defIt2.hasNext()) {
										ValueBox vbox2 = (ValueBox) defIt2.next();
										if (vbox2.getValue() instanceof Local) {
											InvokeExpr invokeEx = s.getInvokeExpr();
											int argCount = invokeEx.getArgCount();
											for (int i = 0; i < argCount; i++) {
												if (invokeEx.getArg(i) == vbox2.getValue()) {
													taintVector.add(new Integer(i));
												}
											}

											if(invokeEx instanceof InstanceInvokeExpr){

												if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

													if(invokeEx instanceof SpecialInvokeExpr){
														if(next instanceof DefinitionStmt){
															Value rhs = ((DefinitionStmt) next).getRightOp();
															if(rhs instanceof NewExpr){
																continue;
															}
														}
													}

													taintVector.add(new Integer(MyConstants.thisObject));
												}
											}
										}
									}


									if(taintVector.isEmpty()){

										if(MyConstants.DEBUG_INFO)
											System.out.println("No parameters: " + s);
										continue;
									}

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

										if(MyConstants.DEBUG_INFO){
											System.out.println("call target is " + target);
										}


										boolean noNewTaint = true;
										if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+s.toString())){

											noNewTaint = false;
											List<Integer> sources = new ArrayList<Integer>();
											sources.addAll(taintVector);
											propagationHistory.put(sMethod.getSignature()+"|"+s.toString(), sources);
										}else{
											List<Integer> sources = propagationHistory.get(sMethod.getSignature()+"|"+s.toString());

											for(Integer taint : taintVector){
												if(!sources.contains(taint)){
													noNewTaint = false;
													sources.add(taint);
												}
											}
										}
										
										APIGraphNode sNode = null;
										if(!stmtToNodeMap.containsKey(s)){
											sNode = new APIGraphNode(s, sMethod);
											stmtToNodeMap.put(s, sNode);	
											NODE_COUNT++;
											if(isAndroidAPICall(s)){
												API_NODE_COUNT++;
											}
										}else{
											sNode = stmtToNodeMap.get(s);
										}
										if(!methodToDDGMap.get(sMethod).contains(sNode)){
											methodToDDGMap.get(sMethod).add(sNode);
										}
										
										sNode.addPred(defNode);
										defNode.addSucc(sNode);

										DefWithScope sWS = new DefWithScope(s);
										if (!defs.containsKey(s)) {
											defs.put(s, new Vector<Stmt>());
											defsStack.push(sWS);
											if(MyConstants.DEBUG_INFO)
												System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
										}else{
											if(noNewTaint){
												break;
											}
										}

										if(MyConstants.DEBUG_INFO){
											System.out.println("PROPAGATING from METHOD: " + sMethod);
											System.out.println("PROPAGATING from STATEMENT: " + s);
										}

										GlobalForwardDataflowAnalysis.taintedFieldsInCaller.addAll(taintedFields);
										//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCaller.putAll(taintedFieldToPTSMap);

										Vector<Integer> tainted = propagate(target, taintVector, leakSource, s, sMethod);
										
										for(SootField sf : GlobalForwardDataflowAnalysis.taintedFieldsInCallee){
											if(!taintedFields.contains(sf)){
												taintedFields.add(sf);
											}
										}
										/*
										Set<SootField> taintedFieldSet = GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.keySet();
										Iterator<SootField> taintedFieldIter = taintedFieldSet.iterator();
										while(taintedFieldIter.hasNext()){
											SootField tf = taintedFieldIter.next();
											if(!taintedFieldToPTSMap.containsKey(tf)){
												taintedFieldToPTSMap.put(tf, GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.get(tf));
											}
										}											
										*/
										GlobalForwardDataflowAnalysis.taintedFieldsInCallee.clear();
										//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.clear();

										if(MyConstants.DEBUG_INFO){
											System.out.println(s + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
										}
										if ((tainted!=null) && (!tainted.isEmpty())) {

											for(Integer i : tainted){
												int index = i.intValue();

												if(index == MyConstants.returnValue){
													if(s instanceof DefinitionStmt){
														Value taintedRet = ((DefinitionStmt) s).getLeftOp();
														if(taintedRet instanceof Local){
															if (!defs.containsKey(s)) {
																
																if(!methodToDDGMap.get(sMethod).contains(sNode)){
																	methodToDDGMap.get(sMethod).add(sNode);
																}
																
																sNode.addPred(defNode);
																defNode.addSucc(sNode);

																if(MyConstants.DEBUG_INFO)
																	System.out.println("adding def of return value:" + s);

																defs.put(s, new Vector<Stmt>());
																defsStack.push(sWS);
																if(MyConstants.DEBUG_INFO)
																	System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
															}
														}
													}

												}else if(index == MyConstants.thisObject){
													if(s.getInvokeExpr() instanceof InstanceInvokeExpr){
														Value taintedThisRef = ((InstanceInvokeExpr)s.getInvokeExpr()).getBase();

														boolean hasDef = false;
														Stmt def0 = s;
														if(taintedThisRef instanceof Local){
															List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, def0);
															if(defs0.size()==1){
																def0 = (Stmt)defs0.get(0);
																hasDef = true;
																
																APIGraphNode def0Node = null;
																if(!stmtToNodeMap.containsKey(def0)){
																	def0Node = new APIGraphNode(def0, sMethod);
																	stmtToNodeMap.put(def0, def0Node);		
																	NODE_COUNT++;
																	if(isAndroidAPICall(def0)){
																		API_NODE_COUNT++;
																	}
																}else{
																	def0Node = stmtToNodeMap.get(def0);
																}
																if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																	methodToDDGMap.get(sMethod).add(def0Node);
																}
																
																def0Node.addPred(sNode);
																sNode.addSucc(def0Node);

																DefWithScope def0WS = new DefWithScope(def0, s);
																if(!defs.containsKey(def0)){
																	Vector<Stmt> scopes = new Vector<Stmt>();
																	scopes.add(s);
																	defs.put(def0, scopes);
																	defsStack.push(def0WS);
																	if(MyConstants.DEBUG_INFO)
																		System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																}else if(!(defs.get(def0).contains(s))){
																	defs.get(def0).add(s);
																	defsStack.push(def0WS);
																	if(MyConstants.DEBUG_INFO)
																		System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																}
															}else{
																//not very good solution :(
																for(Unit defn : defs0){
																	
																	APIGraphNode defnNode = null;
																	if(!stmtToNodeMap.containsKey((Stmt)defn)){
																		defnNode = new APIGraphNode((Stmt)defn, sMethod);
																		stmtToNodeMap.put((Stmt)defn, defnNode);
																		NODE_COUNT++;
																		if(isAndroidAPICall((Stmt)defn)){
																			API_NODE_COUNT++;
																		}
																	}else{
																		defnNode = stmtToNodeMap.get((Stmt)defn);
																	}
																	if(!methodToDDGMap.get(sMethod).contains(defnNode)){
																		methodToDDGMap.get(sMethod).add(defnNode);
																	}
																	
																	defnNode.addPred(sNode);
																	sNode.addSucc(defnNode);

																	DefWithScope defnWS = new DefWithScope((Stmt)defn, s);
																	if(!defs.containsKey(defn)){
																		Vector<Stmt> scopes = new Vector<Stmt>();
																		scopes.add(s);
																		defs.put((Stmt)defn, scopes);
																		defsStack.push(defnWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																	}else if(!(defs.get(defn).contains(s))){
																		defs.get(defn).add(s);
																		defsStack.push(defnWS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																	}
																}
															}
														}
													
													}														 

												}else if(index >= 0){

													Value taintedArg = s.getInvokeExpr().getArg(index);

													boolean hasDef = false;
													Stmt def0 = s;
													if(taintedArg instanceof Local && taintedArg.getType() instanceof RefLikeType){
														List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, def0);
														if(defs0.size()==1){
															def0 = (Stmt)defs0.get(0);
															hasDef = true;
															
															APIGraphNode def0Node = null;
															if(!stmtToNodeMap.containsKey(def0)){
																def0Node = new APIGraphNode(def0, sMethod);
																stmtToNodeMap.put(def0, def0Node);	
																NODE_COUNT++;
																if(isAndroidAPICall(def0)){
																	API_NODE_COUNT++;
																}
															}else{
																def0Node = stmtToNodeMap.get(def0);
															}
															if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																methodToDDGMap.get(sMethod).add(def0Node);
															}
															
															def0Node.addPred(sNode);
															sNode.addSucc(def0Node);

															DefWithScope def0WS = new DefWithScope(def0, s);
															if(!defs.containsKey(def0)){
																Vector<Stmt> scopes = new Vector<Stmt>();
																scopes.add(s);
																defs.put(def0, scopes);
																defsStack.push(def0WS);
																if(MyConstants.DEBUG_INFO)
																	System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
															}else if(!(defs.get(def0).contains(s))){
																defs.get(def0).add(s);
																defsStack.push(def0WS);
																if(MyConstants.DEBUG_INFO)
																	System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
															}
														}
													}
													
												}
											}
										}

									}


								} else {

									boolean isInvoke = false;

									Iterator iUse = s.getUseBoxes().iterator();
									while (iUse.hasNext()) {
										ValueBox vB = (ValueBox) iUse.next();
										if (vB.getValue() instanceof InvokeExpr) {
											isInvoke = true;
										}
									}

									if (isInvoke) {

										if(MyConstants.DEBUG_INFO)
											System.out.println("s is Invoke: " + s);

										Vector<Integer> taintVector = new Vector<Integer>();

										Iterator defIt2 = next.getDefBoxes().iterator();
										while (defIt2.hasNext()) {
											ValueBox vbox2 = (ValueBox) defIt2.next();
											if (vbox2.getValue() instanceof Local) {
												InvokeExpr invokeEx = s.getInvokeExpr();
												int argCount = invokeEx.getArgCount();
												for (int i = 0; i < argCount; i++) {
													if (invokeEx.getArg(i) == vbox2.getValue()) {
														taintVector.add(new Integer(i));
													}
												}

												if(invokeEx instanceof InstanceInvokeExpr){

													if(((InstanceInvokeExpr) invokeEx).getBase() == vbox2.getValue()){

														if(invokeEx instanceof SpecialInvokeExpr){
															if(next instanceof DefinitionStmt){
																Value rhs = ((DefinitionStmt) next).getRightOp();
																if(rhs instanceof NewExpr){
																	continue;
																}
															}
														}

														taintVector.add(new Integer(MyConstants.thisObject));
													}
												}
											}
										}



										if(taintVector.isEmpty()){
											if(MyConstants.DEBUG_INFO)
												System.out.println("No parameters: " + s);
											continue;
										}										

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
											if(MyConstants.DEBUG_INFO){
												System.out.println("call target is " + target);
											}

											boolean noNewTaint = true;
											if(!propagationHistory.containsKey(sMethod.getSignature()+"|"+s.toString())){

												noNewTaint = false;
												List<Integer> sources = new ArrayList<Integer>();
												sources.addAll(taintVector);
												propagationHistory.put(sMethod.getSignature()+"|"+s.toString(), sources);
											}else{
												List<Integer> sources = propagationHistory.get(sMethod.getSignature()+"|"+s.toString());

												for(Integer taint : taintVector){
													if(!sources.contains(taint)){
														noNewTaint = false;
														sources.add(taint);
													}
												}													
											}
											
											APIGraphNode sNode = null;
											if(!stmtToNodeMap.containsKey(s)){
												sNode = new APIGraphNode(s, sMethod);
												stmtToNodeMap.put(s, sNode);	
												NODE_COUNT++;
												if(isAndroidAPICall(s)){
													API_NODE_COUNT++;
												}
											}else{
												sNode = stmtToNodeMap.get(s);
											}
											if(!methodToDDGMap.get(sMethod).contains(sNode)){
												methodToDDGMap.get(sMethod).add(sNode);
											}
											
											sNode.addPred(defNode);
											defNode.addSucc(sNode);

											DefWithScope sWS = new DefWithScope(s);
											if (!defs.containsKey(s)) {
												defs.put(s, new Vector<Stmt>());
												defsStack.push(sWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
											}else{
												if(noNewTaint){
													break;
												}
											}

											if(MyConstants.DEBUG_INFO){
												System.out.println("PROPAGATING from METHOD: " + sMethod);
												System.out.println("PROPAGATING from STATEMENT: " + s);
											}
											GlobalForwardDataflowAnalysis.taintedFieldsInCaller.addAll(taintedFields);
											//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCaller.putAll(taintedFieldToPTSMap);

											Vector<Integer> tainted = propagate(target, taintVector, leakSource, s, sMethod);
											
											for(SootField sf : GlobalForwardDataflowAnalysis.taintedFieldsInCallee){
												if(!taintedFields.contains(sf)){
													taintedFields.add(sf);
												}
											}
											/*
											Set<SootField> taintedFieldSet = GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.keySet();
											Iterator<SootField> taintedFieldIter = taintedFieldSet.iterator();
											while(taintedFieldIter.hasNext()){
												SootField tf = taintedFieldIter.next();
												if(!taintedFieldToPTSMap.containsKey(tf)){
													taintedFieldToPTSMap.put(tf, GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.get(tf));
												}
											}											
											*/
											GlobalForwardDataflowAnalysis.taintedFieldsInCallee.clear();
											//GlobalForwardDataflowAnalysis.taintedFieldToPTSMapInCallee.clear();
											
											if(MyConstants.DEBUG_INFO){
												System.out.println(s + " |taint:" + taintVector + "| PROPAGATION result: " + tainted);
											}
											if ((tainted!=null) && (!tainted.isEmpty())) {

												for(Integer i : tainted){
													int index = i.intValue();

													if(index == MyConstants.returnValue){
														if(s instanceof DefinitionStmt){
															Value taintedRet = ((DefinitionStmt) s).getLeftOp();
															if(taintedRet instanceof Local){

																if (!defs.containsKey(s)) {
																	
																	if(!methodToDDGMap.get(sMethod).contains(sNode)){
																		methodToDDGMap.get(sMethod).add(sNode);
																	}
																	
																	sNode.addPred(defNode);
																	defNode.addSucc(sNode);

																	if(MyConstants.DEBUG_INFO)
																		System.out.println("adding def of return value:" + s);

																	defs.put(s, new Vector<Stmt>());
																	defsStack.push(sWS);
																	if(MyConstants.DEBUG_INFO)
																		System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
																}
															}
														}

													}else if(index == MyConstants.thisObject){
														if(s.getInvokeExpr() instanceof InstanceInvokeExpr){

															Value taintedThisRef = ((InstanceInvokeExpr)s.getInvokeExpr()).getBase();

															boolean hasDef = false;
															Stmt def0 = s;
															if(taintedThisRef instanceof Local){
																List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedThisRef, def0);
																if(defs0.size()==1){
																	def0 = (Stmt)defs0.get(0);
																	hasDef = true;
																	
																	APIGraphNode def0Node = null;
																	if(!stmtToNodeMap.containsKey(def0)){
																		def0Node = new APIGraphNode(def0, sMethod);
																		stmtToNodeMap.put(def0, def0Node);		
																		NODE_COUNT++;
																		if(isAndroidAPICall(def0)){
																			API_NODE_COUNT++;
																		}
																	}else{
																		def0Node = stmtToNodeMap.get(def0);
																	}
																	if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																		methodToDDGMap.get(sMethod).add(def0Node);
																	}
																	
																	def0Node.addPred(sNode);
																	sNode.addSucc(def0Node);

																	DefWithScope def0WS = new DefWithScope(def0, s);
																	if(!defs.containsKey(def0)){
																		Vector<Stmt> scopes = new Vector<Stmt>();
																		scopes.add(s);
																		defs.put(def0, scopes);
																		defsStack.push(def0WS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																	}else if(!(defs.get(def0).contains(s))){
																		defs.get(def0).add(s);
																		defsStack.push(def0WS);
																		if(MyConstants.DEBUG_INFO)
																			System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																	}
																}else{
																	//not very good solution :(
																	for(Unit defn : defs0){
																		
																		APIGraphNode defnNode = null;
																		if(!stmtToNodeMap.containsKey((Stmt)defn)){
																			defnNode = new APIGraphNode((Stmt)defn, sMethod);
																			stmtToNodeMap.put((Stmt)defn, defnNode);	
																			NODE_COUNT++;
																			if(isAndroidAPICall((Stmt)defn)){
																				API_NODE_COUNT++;
																			}
																		}else{
																			defnNode = stmtToNodeMap.get((Stmt)defn);
																		}
																		if(!methodToDDGMap.get(sMethod).contains(defnNode)){
																			methodToDDGMap.get(sMethod).add(defnNode);
																		}
																		
																		defnNode.addPred(sNode);
																		sNode.addSucc(defnNode);

																		DefWithScope defnWS = new DefWithScope((Stmt)defn, s);
																		if(!defs.containsKey(defn)){
																			Vector<Stmt> scopes = new Vector<Stmt>();
																			scopes.add(s);
																			defs.put((Stmt)defn, scopes);
																			defsStack.push(defnWS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																		}else if(!(defs.get(defn).contains(s))){
																			defs.get(defn).add(s);
																			defsStack.push(defnWS);
																			if(MyConstants.DEBUG_INFO)
																				System.out.println("def stack doesn't contain " + defnWS.dump() + ". Push it.");
																		}
																	}
																}
															}
																													
														}														 

													}else if(index >= 0){

														Value taintedArg = s.getInvokeExpr().getArg(index);

														boolean hasDef = false;
														Stmt def0 = s;
														if(taintedArg instanceof Local && taintedArg.getType() instanceof RefLikeType){
															List<Unit> defs0 = mrd.getDefsOfAt((Local)taintedArg, def0);
															if(defs0.size()==1){
																def0 = (Stmt)defs0.get(0);
																hasDef = true;
																
																APIGraphNode def0Node = null;
																if(!stmtToNodeMap.containsKey(def0)){
																	def0Node = new APIGraphNode(def0, sMethod);
																	stmtToNodeMap.put(def0, def0Node);		
																	NODE_COUNT++;
																	if(isAndroidAPICall(def0)){
																		API_NODE_COUNT++;
																	}
																}else{
																	def0Node = stmtToNodeMap.get(def0);
																}
																if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																	methodToDDGMap.get(sMethod).add(def0Node);
																}
																
																def0Node.addPred(sNode);
																sNode.addSucc(def0Node);

																DefWithScope def0WS = new DefWithScope(def0, s);
																if(!defs.containsKey(def0)){
																	Vector<Stmt> scopes = new Vector<Stmt>();
																	scopes.add(s);
																	defs.put(def0, scopes);
																	defsStack.push(def0WS);
																	if(MyConstants.DEBUG_INFO)
																		System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																}else if(!(defs.get(def0).contains(s))){
																	defs.get(def0).add(s);
																	defsStack.push(def0WS);
																	if(MyConstants.DEBUG_INFO)
																		System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
																}
															}
														}														
													}
												}
											}

										}

									} else {

										//if the use of l is located on the left-handed side of the assignment, ignore it
										if(s instanceof DefinitionStmt){
											Value lhs = ((DefinitionStmt) s).getLeftOp();
											if(lhs instanceof ArrayRef){
												Value base = ((ArrayRef) lhs).getBase();
												if(base == l){
													continue;
												}
											}else if(lhs instanceof InstanceFieldRef){
												Value base = ((InstanceFieldRef) lhs).getBase();
												if(base == l){
													continue;
												}
											}
										}
										
										APIGraphNode sNode = null;
										if(!stmtToNodeMap.containsKey(s)){
											sNode = new APIGraphNode(s, sMethod);
											stmtToNodeMap.put(s, sNode);		
											NODE_COUNT++;
											if(isAndroidAPICall(s)){
												API_NODE_COUNT++;
											}
										}else{
											sNode = stmtToNodeMap.get(s);
										}
										if(!methodToDDGMap.get(sMethod).contains(sNode)){
											methodToDDGMap.get(sMethod).add(sNode);
										}
										
										sNode.addPred(defNode);
										defNode.addSucc(sNode);
										
										DefWithScope sWS = new DefWithScope(s);
										if (!defs.containsKey(s)) {
																						
											boolean isRhsInstanceRef = false;
											if(s instanceof DefinitionStmt){
												Value rhs = ((DefinitionStmt) s).getRightOp();
												if(rhs instanceof InstanceFieldRef){
													Value base = ((InstanceFieldRef) rhs).getBase();
													if(base == l){
														isRhsInstanceRef = true;
													}
												}
											}		
											
											if(!isRhsInstanceRef){
												defs.put(s, new Vector<Stmt>());
												defsStack.push(sWS);
												if(MyConstants.DEBUG_INFO)
													System.out.println("def stack doesn't contain " + sWS.dump() + ". Push it.");
											}

											if(s instanceof DefinitionStmt){
												Value lhs = ((DefinitionStmt) s).getLeftOp();

												if(lhs instanceof StaticFieldRef){
													
													if(MyConstants.TO_TAINT_STATIC_FIELD){
														if(!taintedFields.contains(s)){
															taintedFields.add(((StaticFieldRef)lhs).getField());
															
															/*
															SootField fieldKey = ((StaticFieldRef)lhs).getField();
															if(fieldToDefsMap.containsKey(fieldKey)){
																List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
																if(!fieldDefs.contains(s)){
																	fieldDefs.add(s);
																}
															}else{
																List<Stmt> fieldDefs = new ArrayList<Stmt>();
																fieldDefs.add(s);
																fieldToDefsMap.put(fieldKey, fieldDefs);																	
															}
															*/
														}
														
														SootField fieldKey = ((StaticFieldRef)lhs).getField();
														if(fieldToDefsMap.containsKey(fieldKey)){
															List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
															if(!fieldDefs.contains(s)){
																fieldDefs.add(s);
															}
														}else{
															List<Stmt> fieldDefs = new ArrayList<Stmt>();
															fieldDefs.add(s);
															fieldToDefsMap.put(fieldKey, fieldDefs);																	
														}
													}
												}
											}


											boolean hasDef = false;
											Stmt def0 = s;


											if(def0 instanceof DefinitionStmt){
												Value lhs = ((DefinitionStmt) def0).getLeftOp();
												

												if(lhs instanceof InstanceFieldRef){

													if(MyConstants.TO_TAINT_INSTANCE_FIELD){
														if(!taintedFields.contains(def0)){
															taintedFields.add(((InstanceFieldRef)lhs).getField());
															
															/*
															SootField fieldKey = ((InstanceFieldRef)lhs).getField();
															if(fieldToDefsMap.containsKey(fieldKey)){
																List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
																if(!fieldDefs.contains(def0)){
																	fieldDefs.add(def0);
																}
															}else{
																List<Stmt> fieldDefs = new ArrayList<Stmt>();
																fieldDefs.add(def0);
																fieldToDefsMap.put(fieldKey, fieldDefs);																	
															}
															*/
														}
														
														SootField fieldKey = ((InstanceFieldRef)lhs).getField();
														if(fieldToDefsMap.containsKey(fieldKey)){
															List<Stmt> fieldDefs = fieldToDefsMap.get(fieldKey);
															if(!fieldDefs.contains(def0)){
																fieldDefs.add(def0);
															}
														}else{
															List<Stmt> fieldDefs = new ArrayList<Stmt>();
															fieldDefs.add(def0);
															fieldToDefsMap.put(fieldKey, fieldDefs);																	
														}
													}
													
												}else if(lhs instanceof ArrayRef){
													Value base = ((ArrayRef) lhs).getBase();
													if(base instanceof Local){
														List<Unit> defs0 = mrd.getDefsOfAt((Local)base, def0);
														if(defs0.size()==1){
															def0 = (Stmt)defs0.get(0);
															hasDef = true;
															
															APIGraphNode def0Node = null;
															if(!stmtToNodeMap.containsKey(def0)){
																def0Node = new APIGraphNode(def0, sMethod);
																stmtToNodeMap.put(def0, def0Node);		
																NODE_COUNT++;
																if(isAndroidAPICall(def0)){
																	API_NODE_COUNT++;
																}
															}else{
																def0Node = stmtToNodeMap.get(def0);
															}
															if(!methodToDDGMap.get(sMethod).contains(def0Node)){
																methodToDDGMap.get(sMethod).add(def0Node);
															}
															
															def0Node.addPred(sNode);
															sNode.addSucc(def0Node);

															DefWithScope def0WS = new DefWithScope(def0, s);
															if(!defs.containsKey(def0)){
																Vector<Stmt> scopes = new Vector<Stmt>();
																scopes.add(s);
																defs.put(def0, scopes);
																defsStack.push(def0WS);
																if(MyConstants.DEBUG_INFO)
																	System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
															}else if(!(defs.get(def0).contains(s))){
																defs.get(def0).add(s);
																defsStack.push(def0WS);
																if(MyConstants.DEBUG_INFO)
																	System.out.println("def stack doesn't contain " + def0WS.dump() + ". Push it.");
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
			}// end while(it.hasNext())

		}// end while(!delta.isEmpty())


		//////////////////////////
		Iterator i = defs.keySet().iterator();
		while (i.hasNext()) {
			Stmt s = (Stmt) i.next();
			AddTags(s, generalTaintTag);
			AddTags(s, taintTag);

			Iterator usesIt = s.getUseBoxes().iterator();
			while (usesIt.hasNext()) {
				ValueBox vbox = (ValueBox) usesIt.next();
				if (vbox.getValue() instanceof Local) {
					Local l = (Local) vbox.getValue();

					Iterator rDefsIt = mrd.getDefsOfAt(l, s).iterator();
					while (rDefsIt.hasNext()) {
						Stmt next = (Stmt) rDefsIt.next();
						if(!next.getTags().contains(taintTag)){
							AddTags(next, generalExtraDefTag);
							AddTags(next, extraDefTag);
						}
					}
				}
			}

			if (s instanceof ReturnStmt) {
				
				APIGraphNode returnNode = null;
				if(!stmtToNodeMap.containsKey(s)){
					returnNode = new APIGraphNode(s, sMethod);
					stmtToNodeMap.put(s, returnNode);	
					NODE_COUNT++;
					if(isAndroidAPICall(s)){
						API_NODE_COUNT++;
					}
				}else{
					returnNode = stmtToNodeMap.get(s);
				}
				if(!methodToDDGMap.get(sMethod).contains(returnNode)){
					methodToDDGMap.get(sMethod).add(returnNode);
				}
				
				APIGraphNode fromNode = null;
				if(!stmtToNodeMap.containsKey(from)){
					fromNode = new APIGraphNode(from, fromMethod);
					stmtToNodeMap.put(from, fromNode);		
					NODE_COUNT++;
					if(isAndroidAPICall(from)){
						API_NODE_COUNT++;
					}
				}else{
					fromNode = stmtToNodeMap.get(from);
				}
				if(!methodToDDGMap.get(fromMethod).contains(fromNode)){
					methodToDDGMap.get(fromMethod).add(fromNode);
				}
				
				fromNode.addPred(returnNode);
				returnNode.addSucc(fromNode);
				
				taintResult.add(new Integer(MyConstants.returnValue));	
				
			}else if(s instanceof IdentityStmt){
				
				APIGraphNode idNode = null;
				if(!stmtToNodeMap.containsKey(s)){
					idNode = new APIGraphNode(s, sMethod);
					stmtToNodeMap.put(s, idNode);	
					NODE_COUNT++;
					if(isAndroidAPICall(s)){
						API_NODE_COUNT++;
					}
				}else{
					idNode = stmtToNodeMap.get(s);
				}
				if(!methodToDDGMap.get(sMethod).contains(idNode)){
					methodToDDGMap.get(sMethod).add(idNode);
				}
				
				APIGraphNode fromNode = null;
				if(!stmtToNodeMap.containsKey(from)){
					fromNode = new APIGraphNode(from, fromMethod);
					stmtToNodeMap.put(from, fromNode);		
					NODE_COUNT++;
					if(isAndroidAPICall(from)){
						API_NODE_COUNT++;
					}
				}else{
					fromNode = stmtToNodeMap.get(from);
				}
				if(!methodToDDGMap.get(fromMethod).contains(fromNode)){
					methodToDDGMap.get(fromMethod).add(fromNode);
				}
				
				fromNode.addPred(idNode);
				idNode.addSucc(fromNode);

				Value rhsIdentity = ((IdentityStmt) s).getRightOp();
				if(rhsIdentity instanceof ThisRef){
					if(!taintIndexes.contains(new Integer(MyConstants.thisObject))){
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
			System.out.println("dataflow for " + source + ":");
		}

		Iterator printIt = body.getUnits().iterator();
		while(printIt.hasNext()){
			Stmt s = (Stmt)printIt.next();
			if(s.getTags().contains(taintTag) || s.getTags().contains(extraDefTag)){

				if(MyConstants.DEBUG_INFO){
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
						taintSourceToField.put(leakSource, fieldList);
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
						taintSourceToField.put(leakSource, fieldList);
						classToSourceToField.put(sMethod.getDeclaringClass().getName(), taintSourceToField);

					}else if(vb.getValue() instanceof Local){

						String varName = ((Local)vb.getValue()).getName();								
						LinkedHashMap<String, List<String>> taintSourceToVar = new LinkedHashMap<String, List<String>>();
						List<String> varList = new ArrayList<String>();
						if(varList.contains(varName)){
							varList.add(varName);
						}								
						taintSourceToVar.put(leakSource, varList);
						methodToSourceToVar.put(sMethod.getSignature(), taintSourceToVar);
					}
				}
			}
		}

		if(MyConstants.DEBUG_INFO){
			System.out.println("end dataflow for " + source + "\n");
		}

		/////////////////////////
		GlobalForwardDataflowAnalysis.taintedFieldsInCallee.addAll(taintedFields);

		/*
		for(SootField f : instanceFields){
			//addTaintField(f);
			if(!usedInstanceFields.contains(f)){
				usedInstanceFields.add(f);
			}
		}

		for(SootField f : staticFields){
			//addTaintField(f);
			if(!usedStaticFields.contains(f)){
				usedStaticFields.add(f);
			}
		}
		 */

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

	public CallGraph getCallGraph() {
		return this.cg;
	}

	private boolean isInScope(ExceptionalUnitGraph eug, Stmt toTest, Stmt scopeBegin){

		if(toTest == scopeBegin){
			return false;
		}

		Stack<Stmt> successors = new Stack<Stmt>();
		Vector<Stmt> traversedSuccs = new Vector<Stmt>();

		successors.push(scopeBegin);
		traversedSuccs.add(scopeBegin);

		while(!successors.isEmpty()){
			Stmt successor = successors.pop();
			if(successor == toTest){
				return true;
			}
			List<Unit> succsOfSuccessor = eug.getSuccsOf(successor);
			for(Unit u : succsOfSuccessor){
				Stmt s = (Stmt)u;
				if(!traversedSuccs.contains(s)){
					traversedSuccs.add(s);
					successors.push(s);
				}
			}
		}

		return false;
	}

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

	private static void removeNonAPIorConstNode(List<APIGraphNode> apiGraph){

		List<APIGraphNode> toRemove = new ArrayList<APIGraphNode>();

		for(APIGraphNode node : apiGraph){

			Stmt s = node.getStmt();
			
			if(s==null){
				System.err.println("Error: statement is null!");
			}
			
			if((!s.getTags().contains(API_TAG)) && (!s.getTags().contains(STRING_CONST_TAG)))
			{
				
				if(!toRemove.contains(node)){
					toRemove.add(node);
				}
			}

		}

		for(APIGraphNode node : toRemove){			
			deleteNode(apiGraph, node);
		}

	}

	private static void removeSpecificPackage(List<APIGraphNode> apiGraph, List<String> packageFilter){
		List<APIGraphNode> toRemove = new ArrayList<APIGraphNode>();
		
		for(APIGraphNode node : apiGraph){
			
			SootMethod hostMethod = node.getHostMethod();

			boolean needsFiltering = false;
			for(String filter : packageFilter){
				if(hostMethod.toString().contains(filter)){
					needsFiltering = true;
					break;
				}
			}
			if(needsFiltering){
				if(!toRemove.contains(node)){
					toRemove.add(node);
				}
			}
		}
		
		for(APIGraphNode node : toRemove){			
			deleteNode(apiGraph, node);
		}
	}

	private static void removeSpecificAPINode(List<APIGraphNode> apiGraph, List<String> classFilter, List<String> classPreserveSet){

		if(MyConstants.DEBUG_INFO)
			System.out.println("removeSpecificAPINode: classFilter: " + classFilter);
		List<APIGraphNode> toRemove = new ArrayList<APIGraphNode>();

		for(APIGraphNode node : apiGraph){

			Stmt s = node.getStmt();

			if(s==null){
				System.err.println("Error: statement is null!");
			}
			if(s.getTags().contains(STRING_CONST_TAG)){
				continue;
			}
			if((s.getTags().contains(API_TAG)))
			{
				if(MyConstants.DEBUG_INFO)
					System.out.println("removeSpecificAPINode: " + s.getInvokeExpr().getMethod().getDeclaringClass());

				/*
				if(classFilter.contains(s.getInvokeExpr().getMethod().getDeclaringClass().toString())){
					if(MyConstants.DEBUG_INFO)
						System.out.println("removeSpecificAPINode: classFilter contains " + s.getInvokeExpr().getMethod().getDeclaringClass());
					if(!toRemove.contains(node)){
						toRemove.add(node);
					}
				}
				 */
				boolean needsPreserving = false;
				for(String preserve : classPreserveSet){
					if(s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains(preserve)){
						needsPreserving = true;
						break;
					}
				}

				if(!needsPreserving){

					boolean needsFiltering = false;
					for(String filter : classFilter){
						if(s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains(filter)){
							needsFiltering = true;
							break;
						}
					}
					if(needsFiltering){
						if(!toRemove.contains(node)){
							toRemove.add(node);
						}
					}

				}
			}

		}

		for(APIGraphNode node : toRemove){			
			deleteNode(apiGraph, node);
		}

	}

	public static void findEntryMethodsForMethodCall(SootMethod call, List<SootMethod> entryMethods)
	{	
		for(SootMethod entryPoint: ClassificationFeatureExtraction.splitEntryPointToMethodsMap.keySet())
		{
			List<SootMethod> callees = ClassificationFeatureExtraction.splitEntryPointToMethodsMap.get(entryPoint);
			if(callees.contains(call))
			{
				if(!entryMethods.contains(entryPoint))
				{
					entryMethods.add(entryPoint);
				}
			}
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

	private boolean isAPICall(Stmt s){
		if(s.containsInvokeExpr()){
			if(!s.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass()){
				return true;
			}
		}
		return false;
	}
	
	private boolean isAndroidAPICall(Stmt s){
		if(s.containsInvokeExpr())
		{
			if(!s.getInvokeExpr().getMethod().getDeclaringClass().isApplicationClass())
			{
				if(s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("java.io.") ||
						s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("org.apache.http.client.") ||
						s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("org.apache.http.impl.client.") ||
						s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("java.net."))
				{
					return true;
				}
				else if(!s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("java.") &&
						!s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("javax.") &&
						!s.getInvokeExpr().getMethod().getDeclaringClass().toString().contains("org."))
				{
					return true;
				}
			}
		}
		return false;
	}
	
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
	
	private static void computeSplitsAndAddTags(List<SootMethod> entry_points){
		
		LinkedHashMap<SootMethod, List<SootMethod>> entryPointGroups = ClassificationFeatureExtraction.splitEntryPointToMethodsMap;
		
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
				entryPoint.addTag(tag);
				tag.setSplitEntryPoint(entryPoint.getSignature());
				System.out.println("add split tag[" + tag.getName() + "," + tag.getLabel() + "] to entry method " + tag.getSplitEntryPoint());
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
		
	}

	
	
}
