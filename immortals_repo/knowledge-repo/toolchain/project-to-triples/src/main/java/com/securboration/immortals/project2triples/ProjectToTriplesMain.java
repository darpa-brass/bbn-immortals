package com.securboration.immortals.project2triples;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

import com.securboration.immortals.bca.BytecodeAnalyzer;
import com.securboration.immortals.instantiation.annotationparser.bytecode.BytecodeHelper;
import com.securboration.immortals.instantiation.annotationparser.traversal.AnnotationParser;
import com.securboration.immortals.instantiation.annotationparser.traversal.JarTraverser;
import com.securboration.immortals.o2t.UniqueMethodCall;
import com.securboration.immortals.ontology.*;
import com.securboration.immortals.ontology.bytecode.*;
import com.securboration.immortals.ontology.bytecode.analysis.Instruction;
import com.securboration.immortals.ontology.bytecode.analysis.MethodCall;


import com.securboration.immortals.ontology.java.compiler.NamedClasspath;

import com.securboration.immortals.semanticweaver.ObjectMapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import com.securboration.immortals.instantiation.bytecode.JarIngestor;
import com.securboration.immortals.instantiation.bytecode.SourceFinder;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.java.build.BuildScript;
import com.securboration.immortals.ontology.java.project.JavaProject;
import com.securboration.immortals.ontology.java.source.CompiledJavaSourceFile;
import com.securboration.immortals.ontology.java.vcs.VcsCoordinate;
import org.xeustechnologies.jcl.JarClassLoader;

public class ProjectToTriplesMain {

	private static final boolean ignoreBinaries = true;
	private ObjectToTriplesConfiguration o2tc;
	private SourceFinder sf;

	private enum RDFFormat {
		TURTLE(".ttl"),
		XML(".xml");
		
		private String ext;
		
		RDFFormat(String ext) {

			this.ext = ext;
		}
		public String ext() {
			return ext;
		}
		
	}

	public static void main(String[] args) throws Exception{
		System.out.println("testing");
	}
	
	public String testFunction(){
		return "Hello, world!";
	}
	
	public String gradleDataToTriples(GradleData gd) throws Exception{
		String uuid = getUUID();
		o2tc = new ObjectToTriplesConfiguration(gd.getImmortalsVersion());
		//o2tc = new ObjectToTriplesConfiguration("r2.0.0"); //TODO hard coded
		JavaProject x = new JavaProject();
		//Set base directory
		String basedir = gd.getBaseDir();
		String plugindir = basedir + "/krgp/";
		
		//SourceFinder (the .java files)
		String svnLocation = gd.getSvnLocation();
		HashSet<String> dirSet = new HashSet<>();
		for (String src : gd.getSourceFilePaths()){
			detectDirectory(src, dirSet);
		}
		for (String src : gd.getTestSourceFilePaths()){
			detectDirectory(src, dirSet);
		}
		String[] sourceFiles = new String[dirSet.size()];
		int counter = 0;
		for (String dir : dirSet){
			sourceFiles[counter] = dir;
			counter++;
		}
		if (svnLocation == null || svnLocation.equals("")){
			svnLocation = "[Not supplied]";
			sf = new SourceFinder(basedir, svnLocation,sourceFiles);
		}
		else{
			sf = new SourceFinder(basedir, svnLocation,sourceFiles);
		}

		//buildScript
		Path ppom = Paths.get(gd.getPathToBuildFile());
		BuildScript buildScript;
		if (!Files.exists(ppom) || ppom.toString().equals("")){
			buildScript = new BuildScript(null,null,null);
		}
		else{
			byte[] pom = Files.readAllBytes(ppom);
			String buildScriptContents = new String(pom);
			String buildScriptHash = hash(pom);
			buildScript = new BuildScript(buildScriptContents,buildScriptHash,uuid);
		}
		
		// Initialize data structures that will be used across classpaths
		ArrayList<NamedClasspath> gradlePaths = new ArrayList<>();
		Set<String> compiledSourcesHash = new HashSet<>();
		HashMap<String, UniqueMethodCall> methodCalls;
		Set<String> elementPathList = new HashSet<>();
		NamedClasspath gpc;
		CallGraph callGraph;
		
		StaticCallGraph staticCallGraph;
		DynamicCallGraph dynamicCallGraph = new DynamicCallGraph();
		JarArtifact jal;
        String completeAnalysis = String.valueOf(gd.getProperty("completeAnalysis"));
		
        if (!Boolean.parseBoolean(completeAnalysis)) {

            HashSet<String> defaultClassPaths = new HashSet<>();
            defaultClassPaths.add(COMPILE_CLASSPATH_NAME);
            // plugin takes too long right now, need to optimize before analyzing any more classpaths
            //defaultClassPaths.add(RUNTIME_CLASSPATH_NAME);
            defaultClassPaths.add(TEST_COMPILE_CLASSPATH_NAME);
            //defaultClassPaths.add(TEST_RUNTIME_CLASSPATH_NAME);

            HashMap<String, ArrayList<String>> tempClasspath = new HashMap<>();
            tempClasspath.putAll(gd.getClasspathNameToClasspathList());
            for (String key : tempClasspath.keySet()) {
                if (!defaultClassPaths.contains(key)) {
                    gd.getClasspathNameToClasspathList().remove(key);
                }
            }
        }
		HashSet<String> classFilesWithTestDependencies = new HashSet<>();
        classFilesWithTestDependencies.addAll(gd.getClasspathNameToClasspathList().get(TEST_COMPILE_CLASSPATH_NAME));
		classFilesWithTestDependencies.addAll(gd.getClasspathNameToClasspathList().get(COMPILE_CLASSPATH_NAME));
		classFilesWithTestDependencies.addAll(gd.getClassFilePaths());
		
		// For each classpath...
		for (String key : gd.getClasspathNameToClasspathList().keySet()){
			ArrayList<FunctionalityCheck> functionalityChecks = new ArrayList<>();
			ArrayList<FunctionalityExemplarDriver> functionalityDrivers = new ArrayList<>();
			ArrayList<String> jarPath = gd.getClasspathNameToClasspathList().get(key);
			callGraph = new CallGraph();
			staticCallGraph = new StaticCallGraph();
			methodCalls = new HashMap<>();
			gpc = new NamedClasspath();
			// Add all methods on this classpath to cache
			// TODO Create config option for creating call graph
			if (false) {
				methodCalls.putAll(addMethodsToCache(jarPath, plugindir
						+ "/" + key + CLASS_ARTIFACTS_DIRECTORY));
				methodCalls.putAll(addMethodsToCache(gd.getClassFilePaths(), plugindir
						+ "/" + key + CLASS_ARTIFACTS_DIRECTORY));
			}
			for (String path : new HashSet<>(gd.getClassFilePaths())) {
				ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration(gd.getImmortalsVersion());
				AnnotationParser annotationParser = new AnnotationParser(config);
				
				CompiledJavaSourceFile sourceFile = processClassFileFromString(path, methodCalls);
				
				//TODO same as above...
				if (false) {
					for (ClassArtifact art : sourceFile.getCorrespondingClass()) {
						constructCPElementGraph(art, methodCalls, staticCallGraph, dynamicCallGraph,
								functionalityChecks, classFilesWithTestDependencies, functionalityDrivers);
					}
				}
				if (elementPathList.add(path)) {
					compiledSourcesHash.add(sourceFile.getHash());
					Model classModel = ObjectToTriples.convert(o2tc, sourceFile);
					String serialModel = OntologyHelper.serializeModel(classModel, "Turtle", false);
					recordCPElement(serialModel, plugindir
							+ SOURCES_DIRECTORY + sourceFile.getName() + RDFFormat.TURTLE.ext);

					byte[] bytecode = FileUtils.readFileToByteArray(new File(path));
					annotationParser.visitClass(BytecodeHelper.hash(bytecode), bytecode);
					//Serialize each dfu element for each class file
					serialModel = serializeCPElement(config);
					if (!serialModel.equals("")) {
						recordCPElement(serialModel, plugindir
								+ DFUS_DIRECTORY + sourceFile.getName() + "-DFU" + RDFFormat.TURTLE.ext);
					}
				}
			}
			for (String path : jarPath) {
				ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration(gd.getImmortalsVersion());
				AnnotationParser annotationParser = new AnnotationParser(config);
				// Check if we have already processed this file
				if (elementPathList.add(path)) {
					// Single class file on path
					if (path.endsWith(".class")) {
						// Analyze class file element
						ClasspathElement element = JarIngestor.ingest(new File(path), sf, methodCalls);
						// If it's brand new, serialize and output
						if (gpc.addElementHashValue(element.getHash())) {
							System.out.println("New file, adding to master list : " + path);

							//Serialize Rdf model and output for each class file
							Model classModel = ObjectToTriples.convert(o2tc, element);
							String serialModel = OntologyHelper.serializeModel(classModel, "Turtle", false);
							recordCPElement(serialModel, plugindir
									 + "/" + key + CLASS_ARTIFACTS_DIRECTORY + element.getName() + RDFFormat.TURTLE.ext);

							// Traverse class for dfu's
							byte[] bytecode = FileUtils.readFileToByteArray(new File(path));
							annotationParser.visitClass(BytecodeHelper.hash(bytecode), bytecode);
							//Serialize each dfu element for each class file
							serialModel = serializeCPElement(config);
							if (!serialModel.equals("")) {
								recordCPElement(serialModel, plugindir
										+ DFUS_DIRECTORY + element.getName() + "-DFU" + RDFFormat.TURTLE.ext);
							}
							
							// Record call graph info
							if (element instanceof ClassArtifact) {
								constructCPElementGraph((ClassArtifact) element, methodCalls, staticCallGraph);
							}
						}
						// Jar file(s) on path
					} else {
						
						// Analyze jar artifact
						jal = analyzeJar(path, methodCalls);
						
						// If it's brand new, serialize and output
						if (gpc.addElementHashValue(jal.getHash())) {
							System.out.println("New file, adding to master list : " + path);
							
							Model jarArtModel = ObjectToTriples.convert(o2tc, jal);
							String serialModel = OntologyHelper.serializeModel(jarArtModel, "Turtle", false);
							recordCPElement(serialModel, plugindir
									+ JARS_DIRECTORY + jal.getName() + RDFFormat.TURTLE.ext);

							// Traverse jar for DFU's
							JarTraverser.traverseJar(new File(path), annotationParser);
							// Serialize DFU elements and output for each jar
							serialModel = serializeCPElement(config);
							if (!serialModel.equals("")) {
								recordCPElement(serialModel, plugindir
										+ DFUS_DIRECTORY + jal.getName() + "DFU" + RDFFormat.TURTLE.ext);
							}
							// Record call graph info
							constructJarGraph(jal, methodCalls, staticCallGraph);
						}
					}
					/*
					 * Even if the artifact is not brand new, we want to record its call graph info
					 *  because it might contain different information, depending on what other artifacts 
					 *  are on the path with it.
 					 */
				} else {
					//TODO config option for call graph
					if (false) {
						System.out.println("Recurring file has already been processed : " + path);
						if (path.endsWith(".class")) {
							ClasspathElement element = JarIngestor.ingest(new File(path), sf, methodCalls);
							gpc.addElementHashValue(element.getHash());
							if (element instanceof ClassArtifact) {
								constructCPElementGraph((ClassArtifact) element, methodCalls, staticCallGraph);
							}
						} else {
							jal = analyzeJar(path, methodCalls);
							gpc.addElementHashValue(jal.getHash());
							constructJarGraph(jal, methodCalls, staticCallGraph);
						}
					}
				}
			}
			// Serialize static analysis call graph info
			callGraph.setStaticCallGraph(staticCallGraph);
			callGraph.setDynamicCallGraph(dynamicCallGraph);
			gpc.setClasspathName(key);
			gradlePaths.add(gpc);
			NamedClasspath dummyClasspath = new NamedClasspath();
			ObjectToTriples.convert(o2tc, gpc);
			o2tc.getNamingContext().setNameForObject(dummyClasspath, o2tc.getNamingContext().getNameForObject(gpc));
			// TODO config option for call graph
			if (false) {
				callGraph.setClasspath(dummyClasspath);
				Model graphModel = ObjectToTriples.convert(o2tc, callGraph);
				String serializedGraphModel = OntologyHelper.serializeModel(graphModel, "TURTLE", false);
				recordCPElement(serializedGraphModel, plugindir
						+ ANALYSIS_DIRECTORY + key + "/" + gpc.getClasspathName() + "-analysis" + RDFFormat.TURTLE.ext);

				CallGraph dummyGraph = new CallGraph();
				o2tc.getNamingContext().setNameForObject(dummyGraph, o2tc.getNamingContext().getNameForObject(callGraph));
				for (FunctionalityCheck check : functionalityChecks) {
					for (FunctionalityTestRun testRun : check.getFunctionalityTestRuns()) {
						testRun.setCallGraph(dummyGraph);
					}

					check.setClasspath(dummyClasspath);
					Model m = ObjectToTriples.convert(o2tc, check);
					String serializedModel = OntologyHelper.serializeModel(m, "TURTLE", false);
					recordCPElement(serializedModel, plugindir + ANALYSIS_DIRECTORY + key
							+ CHECKS_DIRECTORY + check.getMethodPointer().replace("/", "X")
							+ "-FunctCheck" + RDFFormat.TURTLE.ext);
				}

				for (FunctionalityExemplarDriver driver : functionalityDrivers) {
					Model m = ObjectToTriples.convert(o2tc, driver);
					String serializedModel = OntologyHelper.serializeModel(m, "TURTLE", false);
					recordCPElement(serializedModel, plugindir + ANALYSIS_DIRECTORY + key
							+ DRIVERS_DIRECTORY + driver.getTemplateTaught().replace(" ", "_")
							+ "-FunctDriver" + RDFFormat.TURTLE.ext);
				}
			}
		}
		
		// Record and serialize the overall, JavaProject ontology info
		NamedClasspath[] gpcs = new NamedClasspath[gradlePaths.size()];
		gradlePaths.toArray(gpcs);
		x.setClasspaths(gpcs);
		
		// TODO update this with new method call cache system
		String jarPath = gd.getCompiledProjectJarPath();
		if (jarPath != null && !jarPath.equals("")){
			JarArtifact cs = analyzeJar(jarPath);
			x.setCompiledSoftware(cs);
		}
		
		x.setCompiledSourceHash(compiledSourcesHash.toArray(new String[compiledSourcesHash.size()]));
		x.setUuid(uuid);
		x.setBuildScript(buildScript);
		Model m = ObjectToTriples.convert(o2tc, x);
		return OntologyHelper.serializeModel(m, "Turtle", false);
	}

	/** Updates a call graph with the given method edge info
	 * @param callerHash - identifier for the method caller
	 * @param calledHash - identifier for the method called
	 * @param graph - Call graph that will be updated
	 * @param note - Optional note describing issues with the update
	 */
	private void updateGraph(String callerHash, String calledHash, StaticCallGraph graph, Optional<String> note, int order) {
		StaticCallGraphEdge edge = new StaticCallGraphEdge();
		edge.setCallerHash(callerHash);
		edge.setCalledHash(calledHash);
		edge.setOrder(order);
		note.ifPresent(edge::setNote);
		StaticCallGraphEdge[] edges = graph.getStaticCallGraphEdges();
		List<StaticCallGraphEdge> edgeList;
		if (edges == null) {
			edgeList = new ArrayList<>();
		} else {
			edgeList = Arrays.asList(edges);
			edgeList = new ArrayList<>(edgeList);
		}
		edgeList.add(edge);
		graph.setStaticCallGraphEdges(edgeList.toArray(new StaticCallGraphEdge[edgeList.size()]));
	}

	/** Test jar methods against current cache and build the resulting call graph 
	 * @param jal - Jar artifact that will be analyzed for call graph edges
	 * @param methodCalls - Current cache to test against
	 * @param graph - graph that will have constructed edges added to
	 */
	private void constructJarGraph(JarArtifact jal, HashMap<String, UniqueMethodCall> methodCalls, StaticCallGraph graph) {
		for (ClasspathElement element : jal.getJarContents()) {
			if (element instanceof ClassArtifact) {
				AClass aClass = ((ClassArtifact) element).getClassModel();
				for (AMethod method : aClass.getMethods()) {
					int methodCallOrder = 1;
					for (Instruction instruction : method.getInterestingInstructions()) {
						if (instruction instanceof MethodCall) {
							MethodCall methodCall = (MethodCall) instruction;
							Optional<String> note = Optional.empty();
							if (methodCall.getInvocationType().equals(InvocationType.getType(INTERFACE_OP_CODE))) {
								note = Optional.of(INTERFACE_METHOD_CALLED_NOTE);
							}
							UniqueMethodCall testAgainstCache = new UniqueMethodCall(methodCall.getCalledMethodName(),
									methodCall.getCalledMethodDesc(), methodCall.getOwner());
							boolean foundInCache = false;
							for (Map.Entry<String, UniqueMethodCall> entry : methodCalls.entrySet()) {
								if (entry.getValue().equals(testAgainstCache)) {
									updateGraph(method.getBytecodePointer(), entry.getKey(), graph, note, methodCallOrder);
									methodCallOrder++;
									foundInCache = true;
								}
							}
							
							if (!foundInCache) {
								note = note.map(s -> Optional.of(INTERFACE_METHOD_CALLED_NOTE + AMBIGUOUS_METHOD_CALLED_NOTE)).orElseGet(() ->
										Optional.of(AMBIGUOUS_METHOD_CALLED_NOTE));
								updateGraph(method.getBytecodePointer(), constructAmbiguousEdgePointer(methodCall),
										graph, note, methodCallOrder);
								methodCallOrder++;
							}
						}
					}
				}
			}
		}
	}

	/** Test classpath artifact methods against current cache and build the resulting call graph 
	 * @param artifact - Artifact that will be analyzed for call graph edges
	 * @param methodCalls - Current cache to test against
	 * @param graph - graph that will have constructed edges added to
	 */
	private void constructCPElementGraph(ClassArtifact artifact, HashMap<String, UniqueMethodCall> methodCalls, StaticCallGraph graph) {
		AClass aClass = artifact.getClassModel();
		for (AMethod method : aClass.getMethods()) {
			int methodCallOrder = 1;
			for (Instruction instruction : method.getInterestingInstructions()) {
				if (instruction instanceof MethodCall) {
					MethodCall methodCall = (MethodCall) instruction;
					UniqueMethodCall testAgainstCache = new UniqueMethodCall(methodCall.getCalledMethodName(),
							methodCall.getCalledMethodDesc(), methodCall.getOwner());
					boolean foundInCache = false;
					for (Map.Entry<String, UniqueMethodCall> entry : methodCalls.entrySet()) {
						if (entry.getValue().equals(testAgainstCache)) {
							updateGraph(method.getBytecodePointer(), entry.getKey(), graph, Optional.empty(), methodCallOrder);
							methodCallOrder++;
							foundInCache = true;
						}
					}

					if (!foundInCache) {
						updateGraph(method.getBytecodePointer(), constructAmbiguousEdgePointer(methodCall),
								graph, Optional.of(AMBIGUOUS_METHOD_CALLED_NOTE), methodCallOrder);
						methodCallOrder++;
					}
				}
			}
		}
	}

	/** Test source file methods against current cache and build the resulting call graph. Additionally,
	 *  if any of the methods should contain a @FunctionalityTest annotation, invoke and record the results
	 *  in the ontology.
	 * @param artifact - Artifact that will be analyzed for call graph edges
	 * @param methodCalls - Current cache to test against
	 * @param staticCallGraph - graph that will have constructed edges added to
	 * @param testDependencyDir - directory that contains needed dependencies to invoke test method
	 */
	private void constructCPElementGraph(ClassArtifact artifact, HashMap<String, UniqueMethodCall> methodCalls,
										 StaticCallGraph staticCallGraph, DynamicCallGraph dynamicCallGraph,
										 ArrayList<FunctionalityCheck> checks, HashSet<String> testDependencyDir,
										 ArrayList<FunctionalityExemplarDriver> drivers) {
		AClass aClass = artifact.getClassModel();
		FunctionalityExemplarDriver driver;
		Stack<String> driverTemplate;

		for (AMethod aMethod : aClass.getMethods()) {
			int methodCallOrder = 1;
			driverTemplate = new Stack<>();
			driver = new FunctionalityExemplarDriver();
			AnAnnotation[] annotations = aMethod.getAnnotations();
			if (annotations != null) {
				for (AnAnnotation anAnnotation : annotations) {
					if (anAnnotation.getAnnotationClassName().equals(FUNCTIONALITY_TEST)) {
						FunctionalityTestRun testRun = BytecodeAnalyzer.invokeTestMethod(
								aClass.getClassName().replace("/", "."),
								testDependencyDir, aMethod.getMethodName(), dynamicCallGraph);
						if (testRun == null) {
							System.out.println("Unable to invoke FunctionalityTest-annotated method: " +
									aMethod.getMethodName() + ", with class name: " + aClass.getClassName());
							testRun = new FunctionalityTestRun();
							testRun.setSuccess(false);
						}
						AnnotationKeyValuePair[] functionalityType = anAnnotation.getKeyValuePairs();
						FunctionalityCheck check = new FunctionalityCheck();
						
						check.setMethodPointer(aMethod.getBytecodePointer());
						check.setType(functionalityType[0].getValue());
						FunctionalityTestRun[] functionalityTestRuns = { testRun };
						check.setFunctionalityTestRuns(functionalityTestRuns);
						checks.add(check);
					} else if (anAnnotation.getAnnotationClassName().equals(EXEMPLAR_DRIVER)) {
						String template = anAnnotation.getKeyValuePairs()[0].getValue();
						driverTemplate.push(template);
					}
				} 
			}
			for (Instruction instruction : aMethod.getInterestingInstructions()) {
				if (instruction instanceof MethodCall) {
					MethodCall methodCall = (MethodCall) instruction;
					UniqueMethodCall testAgainstCache = new UniqueMethodCall(methodCall.getCalledMethodName(),
							methodCall.getCalledMethodDesc(), methodCall.getOwner());
					boolean foundInCache = false;
					for (Map.Entry<String, UniqueMethodCall> entry : methodCalls.entrySet()) {
						if (entry.getValue().equals(testAgainstCache)) {
							updateGraph(aMethod.getBytecodePointer(), entry.getKey(), staticCallGraph, Optional.empty(), methodCallOrder);
							methodCallOrder++;
							foundInCache = true;
							if (!driverTemplate.isEmpty()) {
								String[] currentInstructions = driver.getInstructions();

								List<String> instructionList;
								if (currentInstructions == null) {
									instructionList = new ArrayList<>();
								} else {
									instructionList = Arrays.asList(currentInstructions);
									instructionList = new ArrayList<>(instructionList);
								}
								instructionList.add(entry.getKey());
								driver.setTemplateTaught(driverTemplate.peek());
								driver.setInstructions(instructionList.toArray(new String[instructionList.size()]));
							}
						}
					}

					if (!foundInCache) {
						updateGraph(aMethod.getBytecodePointer(), constructAmbiguousEdgePointer(methodCall),
								staticCallGraph, Optional.of(AMBIGUOUS_METHOD_CALLED_NOTE), methodCallOrder);
						methodCallOrder++;
						if (!driverTemplate.isEmpty()) {
							driver.setNote("WARNING: Unable to locate one or more method source/destination(s), attempting" +
									" to invoke the given sequence will likely result in unpredictable behavior.");
						}
					}
				}
			}
			if (driver.getNote() != null) {
				drivers.add(driver);
			}
		}
	}

	/** Analyze paths for methods and build up cache
	 * @param paths - Paths to analyze for method calls
	 * @return - Mapping of method pointer to wrapper object UniqueMethodCall, containing method name, method description,
	 * and owner name.
	 * @throws NoSuchAlgorithmException - if the algorithm used to create our object's hash's is not available 
	 */
	private HashMap<String, UniqueMethodCall> addMethodsToCache(ArrayList<String> paths, String classArtifactPath) throws NoSuchAlgorithmException {
		HashMap<String, UniqueMethodCall> methodCalls = new HashMap<>();
		JarArtifact jal;
		JarClassLoader jarClassLoader;
		Set<String> superClasses;
		for (String path : paths) {
			superClasses = new HashSet<>();
			jarClassLoader = new JarClassLoader();
			jarClassLoader.add(path);
			if (path.endsWith(".class")) {
				try {
					AClass aClass = analyzeClass(FileUtils.readFileToByteArray(new File(path)));
					for (AMethod method : aClass.getMethods()) {
						methodCalls.put(aClass.getBytecodePointer() + "/methods/" + method.getMethodName() + method.getMethodDesc(),
								new UniqueMethodCall(method.getMethodName(), method.getMethodDesc(), aClass.getClassName()));
					}
				} catch (IOException exc) {
					System.out.println("Unable to read file with path: " + path);
				}
			} else {
				try {
					jal = analyzeJar(path);
				} catch (IOException exc) {
					System.out.println("Unable to read file with path: " + path);
					continue;
				}
				for (ClasspathElement element : jal.getJarContents()) {
					if (element instanceof ClassArtifact) {
						AClass aClass = ((ClassArtifact) element).getClassModel();
						for (AMethod method : aClass.getMethods()) {
							methodCalls.put(aClass.getBytecodePointer() + "/methods/" + method.getMethodName() + method.getMethodDesc(), new UniqueMethodCall(method.getMethodName(), method.getMethodDesc(), aClass.getClassName()));
							Class clazz;
							try {
								clazz = jarClassLoader.loadClass(aClass.getClassName().replace("/", ".")).getSuperclass();
							} catch (ClassNotFoundException exc) {
								System.out.println("Unable to find superclass for class: " + aClass.getClassName());
								continue;
							} catch (IllegalAccessError exc) {
								System.out.println("Encountered classpath error when instantiating superclass of class: " + aClass.getClassName());
								continue;
							} catch (NoClassDefFoundError exc) {
								System.out.println("Could not find class definition for superclass of class: " + aClass.getClassName());
								continue;
							}
							if (clazz == null) {
								continue;
							}
							try {
								String clazzName = clazz.getCanonicalName();
								if (clazzName == null) {
									//anonymous class
									continue;
								}
							} catch (NoClassDefFoundError exc) {
								// anonymous inner class
								continue;
							}
							InputStream in = jarClassLoader.getResourceAsStream(clazz.getCanonicalName().replace(".", "/") + ".class");
							if (in == null) {
									continue;
							}

							AClass superClass;
							try {
								superClass = analyzeClass(IOUtils.toByteArray(in));
							} catch (IOException exc) {
								System.out.println("Unable to read superclass of class: " + aClass.getClassName());
								continue;
							}
							
							while (!superClass.getClassName().equals("java/lang/Object")) {
								for (AMethod superMethod : superClass.getMethods()) {
									if (superMethod.getMethodName().equals(method.getMethodName()) && superMethod.getMethodDesc().equals(method.getMethodDesc())) {
										methodCalls.put(superClass.getBytecodePointer() + "/methods/" + superMethod.getMethodName() + superMethod.getMethodDesc(), new UniqueMethodCall(superMethod.getMethodName(), superMethod.getMethodDesc(), superClass.getClassName()));
										if (superClasses.add(superClass.getClassName())) {
											Model m = ObjectToTriples.convert(o2tc, superClass);
											try {
												String serializedModel = OntologyHelper.serializeModel(m, "TURTLE", false);
												recordCPElement(serializedModel, classArtifactPath + superClass.getClassName().replace("/", ".") + ".ttl");
											} catch (IOException exc) {
												exc.printStackTrace();
											}
										}
									}
								}
								try {
									clazz = jarClassLoader.loadClass(superClass.getClassName().replace("/", ".")).getSuperclass();
									InputStream stream = jarClassLoader.getResourceAsStream(clazz.getCanonicalName().replace(".", "/") + ".class");
									if (stream == null) {
										System.out.println("Classloader can not see class with name: " + clazz.getCanonicalName());
										break;
									}
									superClass = analyzeClass(IOUtils.toByteArray(jarClassLoader.getResourceAsStream(clazz.getCanonicalName().replace(".", "/") + ".class")));
								} catch (ClassNotFoundException exc) {
									System.out.println("Unable to find superclass of class: " + aClass.getClassName());
								} catch (IOException exc) {
									System.out.println("Unable to read superclass of class: " + aClass.getClassName());
								}
							}
						}
					}
				}
			}
		}
		return methodCalls;
	}
	
	// Serialize classpath element, passing an o2tc object and returning a string representation
	private String serializeCPElement(ObjectToTriplesConfiguration config) throws IOException {

		Model dfuModels = ModelFactory.createDefaultModel();
		ObjectMapper mapper = config.getMapper();
		String serialModel = "";

		if (mapper.getObjectsToSerialize().size() != 0) {

			for (Object o : mapper.getObjectsToSerialize()) {
				Model dfuModel = ObjectToTriples.convert(config, o);
				dfuModels.add(dfuModel);
			}
			OntologyHelper.addAutogenerationMetadata(
					config,
					dfuModels,
					config.getTargetNamespace(),
					config.getOutputFile()
			);
			serialModel =
					OntologyHelper.serializeModel(
							dfuModels,
							"Turtle",
							config.isValidateOntology()
					);
		}
		return serialModel;
	}

	/** Output the given model to directory
	 * @param serialMod - serialization of rdf model
	 * @param path - path to which the model will be written to
	 * @throws IOException - writing the model is unsuccessful
	 */
	private static void recordCPElement(String serialMod, String path) throws IOException {
		File mod2Rdf = new File(path);
		mod2Rdf.getParentFile().mkdirs();
		mod2Rdf.createNewFile();

		FileWriter fileWriter = new FileWriter(mod2Rdf);
		fileWriter.write(serialMod);
		fileWriter.flush();
		fileWriter.close();
	}
	
	private void print(Object o){
		System.out.println(o.toString());
	}
	
	private void printList(ArrayList<String> a){
		a.stream().forEach(System.out::println);
	}
	
	private <T> void printArray(T[] ar){
		Stream<T> s = Arrays.stream(ar);
		s.forEach(System.out::println);
	}
	
	private void detectDirectory(String src, Set<String> dirSet){
		int i = src.lastIndexOf(File.separator);
		if (i != -1){
			src = src.substring(0, i);
		}
		if (!dirSet.contains(src)){
			dirSet.add(src);
		}
	}

	private String getUUID() {
		return UUID.randomUUID().toString();
	}
	
	private CompiledJavaSourceFile[] processClassFileStrings(ArrayList<String> classes, HashMap<String, UniqueMethodCall> pointers) throws Exception{
		CompiledJavaSourceFile[] compiledSourceFiles = new CompiledJavaSourceFile[classes.size()];
		for (int x = 0; x < classes.size(); x++){
			compiledSourceFiles[x] = processClassFileFromString(classes.get(x), pointers);
		}
		return compiledSourceFiles;
	}
	
	private CompiledJavaSourceFile processClassFileFromString(String path, HashMap<String, UniqueMethodCall> pointers) throws Exception {
		Path p = Paths.get(path);
		byte[] binaryForm = Files.readAllBytes(p);
		String hash = hash(binaryForm);
		String name = p.getFileName().toString();
		VcsCoordinate vcsInfo = new VcsCoordinate();
		vcsInfo.setVersion("TEMP"); //TODO
		vcsInfo.setVersionControlUrl("www.TEMP.com"); //TODO
		String sourceEncoding = "UTF-8"; //TODO guessing
		
		AClass ac = analyzeClass(binaryForm, pointers);
		AClass[] inneracs = ac.getInnerClasses();
		
		ClassArtifact[] cas;
		ClassArtifact builder;
		if (inneracs != null){
			cas = new ClassArtifact[inneracs.length+1];
			for (int counter = 0; counter < inneracs.length; counter++){
				AClass worker = inneracs[counter];
				builder = new ClassArtifact();
				builder.setClassModel(worker);
				builder.setName(worker.getClassName());
				cas[counter+1] = builder;
				//builder.setBinaryForm(worker.getBytecodePointer()); //TODO maybe always leave off
				//builder.setHash(hash); //TODO
			}
		}
		else{
			cas = new ClassArtifact[1];
		}
		builder = new ClassArtifact();
		builder.setClassModel(ac);
		builder.setHash(hash);
		builder.setName(name);
		cas[0] = builder;
		
		CompiledJavaSourceFile c = new CompiledJavaSourceFile();
		if (ignoreBinaries){
			c.setBinaryForm(new byte[]{0});
		}
		else{
			c.setBinaryForm(binaryForm);
		}
		c.setCorrespondingClass(cas);
		c.setHash(hash);
		c.setName(name);
		c.setSourceEncoding(sourceEncoding);
		c.setVcsInfo(vcsInfo);
		return c;
	}
	
	private AClass analyzeClass(byte[] cb, HashMap<String, UniqueMethodCall> pointers) throws Exception{
		//getClassNode code
		ClassReader cr = new ClassReader(cb);
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		String hash = hash(cb);
		
		//processClass code
		JarIngestor ji = new JarIngestor(sf);
		return ji.processClass(hash, cn, pointers);
	}

	private AClass analyzeClass(byte[] cb) throws NoSuchAlgorithmException {
		//getClassNode code
		ClassReader cr = new ClassReader(cb);
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		String hash = hash(cb);

		//processClass code
		JarIngestor ji = new JarIngestor(sf);
		return ji.processClass(hash, cn);
	}
	
	private JarArtifact analyzeJar(String jarFile) throws IOException{
		Path p = Paths.get(jarFile);
		byte[] jar = Files.readAllBytes(p);
		String name = p.getFileName().toString();
		BytecodeArtifactCoordinate bac;
		try{
			bac = JarIngestor.getCoordinate(p.toFile());
		}
		catch (Exception e){
			bac = new BytecodeArtifactCoordinate();
			bac.setArtifactId("[Error]");
			bac.setGroupId("[Error]");
			bac.setVersion("[Error]");
		}
		return JarIngestor.ingest(jar, name, bac.getGroupId(), bac.getArtifactId(), bac.getVersion(), sf);
	}

	private JarArtifact analyzeJar(String jarFile, HashMap<String, UniqueMethodCall> pointers) throws IOException{
		Path p = Paths.get(jarFile);
		byte[] jar = Files.readAllBytes(p);
		String name = p.getFileName().toString();
		BytecodeArtifactCoordinate bac;
		try{
			bac = JarIngestor.getCoordinate(p.toFile());
		}
		catch (Exception e){
			bac = new BytecodeArtifactCoordinate();
			bac.setArtifactId("[Error]");
			bac.setGroupId("[Error]");
			bac.setVersion("[Error]");
		}
		return JarIngestor.ingest(jar, name, bac.getGroupId(), bac.getArtifactId(), bac.getVersion(), sf, pointers);
	}
	
	private static String hash(byte[] in) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(in);
		return Base64.getEncoder().encodeToString(md.digest());
	}
	
	private static String constructAmbiguousEdgePointer(MethodCall methodCall) {
		String result = "non-platform-call.";
		result += (methodCall.getOwner().replace("/", "."));
		result += methodCall.getCalledMethodName();
		result += methodCall.getCalledMethodDesc();
		
		return result;
	}

	public static final String FUNCTIONALITY_TEST = "mil/darpa/immortals/annotation/dsl/ontology/FunctionalityTest";
	public static final String EXEMPLAR_DRIVER = "mil/darpa/immortals/annotation/dsl/ontology/FunctionalityExemplarDriver";
	
	private final static String AMBIGUOUS_METHOD_CALLED_NOTE = "The owner of the method being called cannot be identified through static analysis. ";
	private final static String INTERFACE_METHOD_CALLED_NOTE = "The owner of the method is an interface. This may result in incomplete analysis. ";
	private final static int INTERFACE_OP_CODE = 185;
	
	private final static String COMPILE_CLASSPATH_NAME = "compile";
	private final static String RUNTIME_CLASSPATH_NAME = "runtime";
	private final static String TEST_COMPILE_CLASSPATH_NAME = "testCompile";
	private final static String TEST_RUNTIME_CLASSPATH_NAME = "testRuntime";
	private final static String CLASS_ARTIFACTS_DIRECTORY = "/classes/";
	private final static String ANALYSIS_DIRECTORY = "/analysis/";
	private final static String SOURCES_DIRECTORY = "/sources/";
	private final static String JARS_DIRECTORY = "/jars/";
	private final static String DFUS_DIRECTORY = "/dfus/";
	private final static String CHECKS_DIRECTORY = "/checks/";
	private final static String DRIVERS_DIRECTORY = "/drivers/";
}
