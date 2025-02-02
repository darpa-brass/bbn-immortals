package com.securboration.immortals.project2triples;

import static soot.SootClass.BODIES;
import static soot.SootClass.SIGNATURES;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import com.securboration.immortals.exampleDataflows.CPP_ESS_ARCH;
import com.securboration.immortals.exampleDataflows.ServerOrientedFormattedDataConstraint;
import com.securboration.immortals.ontology.java.project.AnalysisMetrics;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.utils.Pair;
import com.securboration.immortals.instantiation.bytecode.JarIngestor;
import com.securboration.immortals.instantiation.bytecode.SourceFinder;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.bytecode.AClass;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.bytecode.ClassArtifact;
import com.securboration.immortals.ontology.bytecode.ClasspathElement;
import com.securboration.immortals.ontology.bytecode.Dependency;
import com.securboration.immortals.ontology.bytecode.JarArtifact;
import com.securboration.immortals.ontology.java.android.AndroidApp;
import com.securboration.immortals.ontology.java.build.BuildScript;
import com.securboration.immortals.ontology.java.compiler.NamedClasspath;
import com.securboration.immortals.ontology.java.dfus.DfuModuleRepo;
import com.securboration.immortals.ontology.java.project.JavaProject;
import com.securboration.immortals.ontology.java.source.CompiledJavaSourceFile;
import com.securboration.immortals.ontology.java.source.SourceCodeRepo;
import com.securboration.immortals.ontology.java.vcs.VcsCoordinate;
import com.securboration.immortals.ontology.lang.ProgrammingLanguage;
import com.securboration.immortals.ontology.lang.SourceFile;
import com.securboration.immortals.semanticweaver.ObjectMapper;
import com.securboration.immortals.soot.JimpleClassMapping;
import com.securboration.immortals.utility.GradleTaskHelper;

import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.SootResolver;
import soot.SourceLocator;
import soot.options.Options;
import soot.tagkit.InnerClassTag;
import soot.tagkit.Tag;

public class ProjectToTriplesMain {

	private static final String ANDROID_VERSION = "21";
	private static final boolean IGNORE_BINARIES = true;
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

	public static void main(String[] args) throws IOException {


		//Scene.v().setSootClassPath(Scene.v().getSootClassPath() + File.pathSeparator + "C:/ImmortalsPhase3/knowledge-repo/cp/cp3.1/cp-ess-min/client/target/immortals-cp3.1-client-1.0.0.jar");
		//Scene.v().loadBasicClasses();
		//jimplfyClass(Scene.v().loadClassAndSupport("com.securboration.client.MessageListenerClient"));

		CPP_ESS_ARCH cpp_ess_arch = new CPP_ESS_ARCH();
		ServerOrientedFormattedDataConstraint.XsltImplementationStrategy xsltImplementationStrategy = new ServerOrientedFormattedDataConstraint.XsltImplementationStrategy();
		ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");
		Model m = ModelFactory.createDefaultModel();
		m.add(ObjectToTriples.convert(config, cpp_ess_arch));
		m.add(ObjectToTriples.convert(config, xsltImplementationStrategy));
		recordCPElement(OntologyHelper.serializeModel(m, "TTL", false),
				"C:/cp-ess-min-17-19/ess/ess/etc/arch.ttl");

	}

	public String testFunction(){
		return "Hello, world!";
	}

	public String gradleDataToTriples(GradleData gd, GradleTaskHelper taskHelper, ArrayList<String> includedLibs,
									  boolean completeAnalysis, String vcsAnchor) throws Exception {

		Long beginTime = System.currentTimeMillis();
		int bytecodeTriples = 0;
		String uuid = getUUID();
		o2tc = new ObjectToTriplesConfiguration("r2.0.0");
		JavaProject x = new JavaProject();
		//Set base directory
		String basedir = gd.getBaseDir();
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
		String buildFilePath = gd.getPathToBuildFile();
		Path ppom = Paths.get(buildFilePath);
		BuildScript buildScript;
		if (!ppom.toFile().exists() || ppom.toString().equals("")){
			buildScript = new BuildScript(null,null,null, null);
		}
		else{
			byte[] pom = Files.readAllBytes(ppom);
			String buildScriptContents = new String(pom);
			String buildScriptHash = hash(pom);
			String projectDir = buildFilePath.substring(0, buildFilePath.lastIndexOf(File.separator));
			buildScript = new BuildScript(buildScriptContents,buildScriptHash,uuid, projectDir);
		}

		// Initialize data structures that will be used across classpaths
		ArrayList<NamedClasspath> gradlePaths = new ArrayList<>();
		Set<String> compiledSourcesHash = new HashSet<>();
		Set<String> elementPathList = new HashSet<>();
		NamedClasspath gpc;

		JarArtifact jal;

		if (!completeAnalysis) {

			HashSet<String> defaultClassPaths = new HashSet<>();
			defaultClassPaths.add(COMPILE_CLASSPATH_NAME);

			HashMap<String, ArrayList<String>> tempClasspath = new HashMap<>();
			tempClasspath.putAll(gd.getClasspathNameToClasspathList());
			for (String key : tempClasspath.keySet()) {
				if (!defaultClassPaths.contains(key)) {
					gd.getClasspathNameToClasspathList().remove(key);
				}
			}
		}

		SourceCodeRepo sourceCodeRepo = new SourceCodeRepo();
		gd.getSourceFilePaths().addAll(gd.getAdditionalSources());
		SourceFile[] sourceFileArray = new SourceFile[gd.getSourceFilePaths().size()];

		int j = 0;
		for (String javaSourceFile : gd.getSourceFilePaths()) {
			SourceFile sourceFile = new SourceFile();
			ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
			programmingLanguage.setLanguageName("java");
			sourceFile.setLanguageModel(programmingLanguage);
			String source = FileUtils.readFileToString(new File(javaSourceFile));
			CompilationUnit compilationUnit = JavaParser.parse(source);
			Optional<PackageDeclaration> filePackageOption = compilationUnit.getPackageDeclaration();
			String fileSimpleName = javaSourceFile.substring(javaSourceFile.lastIndexOf(File.separatorChar) + 1);
			sourceFile.setFileName(fileSimpleName);
			fileSimpleName = fileSimpleName.substring(0, fileSimpleName.lastIndexOf(".java"));
			if (filePackageOption.isPresent()) {
				PackageDeclaration filePackage = filePackageOption.get();
				sourceFile.setFullyQualifiedName(filePackage.getNameAsString() + "." + fileSimpleName);
			}
			sourceFile.setSource(source);

			sourceFileArray[j] = sourceFile;
			j++;
		}

		sourceCodeRepo.setSourceFiles(sourceFileArray);

		// For each classpath...
		gd.getClassFilePaths().addAll(gd.getTestClassFilePaths());
		for (String key : gd.getClasspathNameToClasspathList().keySet()){
			ArrayList<String> jarPath = gd.getClasspathNameToClasspathList().get(key);
			// Debugging statement
			gpc = new NamedClasspath();
			// Add all methods on this classpath to cache
			for (String path : new HashSet<>(gd.getClassFilePaths())) {
				CompiledJavaSourceFile sourceFile = processClassFileFromString(path, taskHelper, basedir, gd);

				//TODO we should be going through this loop
				if (elementPathList.add(path)) {
					compiledSourcesHash.add(sourceFile.getHash());
					Model classModel = ObjectToTriples.convert(o2tc, sourceFile);
					bytecodeTriples+=classModel.getGraph().size();
					String serialModel = OntologyHelper.serializeModel(classModel, "Turtle", false);
					recordCPElement(serialModel, taskHelper.getResultsDir() + gd.getCompiledProjectName()
							+ SOURCES_DIRECTORY + sourceFile.getName() + RDFFormat.TURTLE.ext);
				}
			}
			for (String path : jarPath) {

				// Check if we have already processed this file
				if (elementPathList.add(path)) {
					// Single class file on path
					//TODO currently here... don't think we should be going through this loop , but the one above instead
					if (path.endsWith(".class")) {
						// Analyze class file element
						ClasspathElement element = JarIngestor.ingest(new File(path), sf);
						// If it's brand new, serialize and output
						if (gpc.addElementHashValue(element.getHash())) {
							taskHelper.getPw().println("New file, adding to master list : " + path);

							//Serialize Rdf model and output for each class file
							Model classModel = ObjectToTriples.convert(o2tc, element);
							bytecodeTriples+=classModel.getGraph().size();
							String serialModel = OntologyHelper.serializeModel(classModel, "Turtle", false);
							recordCPElement(serialModel, taskHelper.getResultsDir() + gd.getCompiledProjectName()
									+ "/" + key + CLASS_ARTIFACTS_DIRECTORY + element.getName() + RDFFormat.TURTLE.ext);
						}
						// Jar file(s) on path
					} else {

						if (path.endsWith("aar") && x.getAndroidApp() == null) {
							File aarFile = new File(path);
							while (!aarFile.getName().toLowerCase().contains("sdk")) {
								try {
									aarFile = aarFile.getParentFile();
								} catch (NullPointerException exc) {
									// aar file wasn't in sdk dir... try to get ANDROID_HOME
									String androidSDK = System.getenv("ANDROID_HOME");
									aarFile = new File(androidSDK);
									if (!aarFile.exists()) {
										System.err.println("ANDROID DEPENDENCIES DETECTED, BUT UNABLE TO FIND ANDROID BOOTSTRAP JAR..." +
												"MAKE SURE ANDROID_HOME ENVIRONMENT VARIABLE IS SET");
									}
								}
							}

							String[] sdkDirs = aarFile.list((current, name) ->
									new File(current, name).isDirectory());

							File uberAndroidJarsParent = null;
							for (String sdkDir : sdkDirs) {
								if (sdkDir.equals("platforms")) {
									uberAndroidJarsParent = new File(aarFile.getAbsolutePath()
											+ File.separator + sdkDir);
									break;
								}
							}

							Collection<File> platformJars = FileUtils.listFiles(uberAndroidJarsParent, new String[] {"jar"},
									true);
							List<File> uberAndroidJars = platformJars.stream().filter(uberAndroidJar -> uberAndroidJar.getName()
									.equals("android.jar")).collect(Collectors.toList());

							File uberAndroidJar = null;
							for (File possAndroidJar : uberAndroidJars) {
								if (possAndroidJar.getParent().contains(ANDROID_VERSION)) {
									uberAndroidJar = possAndroidJar;
									break;
								}
							}

							AndroidApp androidApp = new AndroidApp();
							androidApp.setPathToUberJar(uberAndroidJar.getAbsolutePath());
							x.setAndroidApp(androidApp);
						}

						// Analyze jar artifact
						jal = analyzeJar(path, (includedLibs == null || includedLibs.stream()
								.anyMatch(path::contains)));
						jal.setFileSystemPath(path);
						String cachedURI = null;

						if (taskHelper.isPreviousTaskDetected()) {
							cachedURI = checkCache(jal.getHash(), gd.getCompiledProjectName(), taskHelper);
						}

						// If it's brand new, serialize and output
						if (gpc.addElementHashValue(jal.getHash())) {
							taskHelper.getPw().println("New file, adding to master list : " + path);
							if (cachedURI == null) {

								Model jarArtModel = ObjectToTriples.convert(o2tc, jal);
								bytecodeTriples+= jarArtModel.getGraph().size();
								String jarURI = o2tc.getNamingContext().getNameForObject(jal);
								addToCache(jal.getHash(), jarURI, gd.getCompiledProjectName(), taskHelper);

								String serialModel = OntologyHelper.serializeModel(jarArtModel, "Turtle", false);
								recordCPElement(serialModel, taskHelper.getResultsDir() + gd.getCompiledProjectName()
										+ JARS_DIRECTORY + jal.getName() + RDFFormat.TURTLE.ext);

								// Record call graph info
							} else if(!cachedURI.equals("jarAlreadyBelongsToCurrentProject")){
								JarArtifact dummyJar = new JarArtifact();
								dummyJar.setName(jal.getName());
								o2tc.getNamingContext().setNameForObject(dummyJar, cachedURI);

								Model dummyJarModel = ObjectToTriples.convert(o2tc, dummyJar);
								recordCPElement(OntologyHelper.serializeModel(dummyJarModel, "TTL", false), taskHelper.getResultsDir()
										+ gd.getCompiledProjectName() + JARS_DIRECTORY + jal.getName() + RDFFormat.TURTLE.ext);
							}
						}
					}
				}
			}
			gpc.setClasspathName(key);
			gradlePaths.add(gpc);
			NamedClasspath dummyClasspath = new NamedClasspath();
			ObjectToTriples.convert(o2tc, gpc);
			o2tc.getNamingContext().setNameForObject(dummyClasspath, o2tc.getNamingContext().getNameForObject(gpc));
		}

		DfuModuleRepo dfuModuleRepo = new DfuModuleRepo();
		String repoPath = gd.getRepoPaths().get(0);
		dfuModuleRepo.setPathToRepo(repoPath);

		// Record and serialize the overall, JavaProject ontology info
		NamedClasspath[] gpcs = new NamedClasspath[gradlePaths.size()];
		gradlePaths.toArray(gpcs);
		x.setClasspaths(gpcs);

		// TODO update this with new method call cache system
		String jarPath = gd.getCompiledProjectJarPath();
		if (jarPath != null && !jarPath.equals("")){
			JarArtifact cs = analyzeJar(jarPath, (includedLibs != null && includedLibs.stream()
					.noneMatch(lib -> jarPath.contains(lib)) ? false : true));
			x.setCompiledSoftware(cs);
		}

		x.setCompiledSourceHash(compiledSourcesHash.toArray(new String[compiledSourcesHash.size()]));
		x.setUuid(uuid);
		x.setBuildScript(buildScript);
		x.setSourceCodeRepo(sourceCodeRepo);
		x.setDfuModuleRepo(dfuModuleRepo);

		Dependency[] dependencies = new Dependency[gd.getDependencies().size()];
		for (int i = 0; i < dependencies.length; i++) {
			File dependencyFile = gd.getDependencies().get(i);
			Dependency dependency = new Dependency();
			dependency.systemPath = dependencyFile.getAbsolutePath();
			dependency.classifier = dependencyFile.getName();
			dependencies[i] = dependency;
		}
		x.setDependencies(dependencies);

		BytecodeArtifactCoordinate projectCoordinate= new BytecodeArtifactCoordinate();
		projectCoordinate.setGroupId(gd.getGroup());
		projectCoordinate.setArtifactId(gd.getArtifact());
		projectCoordinate.setVersion(gd.getVersion());

		if (vcsAnchor != null && basedir != null) {
			//String fileRemoteURL = getPath(basedir.substring(basedir.indexOf(vcsAnchor) + vcsAnchor.length() + 1));
			VcsCoordinate vcsInfo = new VcsCoordinate();
			vcsInfo.setVersion(gd.getVersion()); //TODO
			vcsInfo.setVersionControlUrl(basedir); //TODO
			x.setVcsCoordinate(vcsInfo);
		} else {
			taskHelper.getPw().println("Unable to set remote URL and link to local file location, check you've set the correct parameters and/or there are no" +
					"package with file path conflicts.");
		}

		x.setCoordinate(projectCoordinate);

		Long endTime = System.currentTimeMillis() - beginTime;
		AnalysisMetrics analysisMetrics = new AnalysisMetrics();
		analysisMetrics.setBytecodeExec(endTime);
		bytecodeTriples+=ObjectToTriples.convert(o2tc, x).getGraph().size();
		analysisMetrics.setBytecodeTriples(bytecodeTriples);
		x.setAnalysisMetrics(analysisMetrics);

		Model m = ObjectToTriples.convert(o2tc, x);
		return OntologyHelper.serializeModel(m, "Turtle", false);
	}

	private void generateSemantics(CompiledJavaSourceFile sourceFile, List<JimpleClassMapping> jimpleClassMappings) throws IOException {

		for (ClassArtifact classArtifact : sourceFile.getCorrespondingClass()) {

			String className = classArtifact.getName();

			Optional<JimpleClassMapping> jimpleClassMappingOption = jimpleClassMappings.stream().filter(
					jCMO -> jCMO.getClassName().equals(className)).findAny();

			if (jimpleClassMappingOption.isPresent()) {
				JimpleClassMapping jimpleClassMapping = jimpleClassMappingOption.get();

				File jimpleFile = new File(jimpleClassMapping.getPathToJimple());

				if (jimpleFile.exists()) {
					String jimpleString = FileUtils.readFileToString(jimpleFile);
					String fields = jimpleString.substring(jimpleString.indexOf("{") + 1);
					fields = fields.substring(0, jimpleString.indexOf("{"));

				}
			}


		}

	}

	private List<JimpleClassMapping> generateJimples(SourceFile[] sourceFiles, String buildDir) throws IOException {

		List<JimpleClassMapping> jimpleClassMappings = new ArrayList<>();

		String classFilesDir = buildDir + File.separator + "classes" + File.separator + "main";

		String oldSootPath = Scene.v().getSootClassPath();
		Scene.v().setSootClassPath(oldSootPath + File.pathSeparator + classFilesDir);
		Scene.v().loadNecessaryClasses();

		for (SourceFile sourceFile : sourceFiles) {

			if (sourceFile.getFullyQualifiedName() == null) {
				continue;
			}

			SootClass sootClass = Scene.v().forceResolve(sourceFile.getFullyQualifiedName(), BODIES);
			if (!sootClass.isConcrete()) {
				SootResolver.v().reResolve(sootClass, SIGNATURES);
			}

			sootClass.setApplicationClass();

			List<Tag> classTags = sootClass.getTags();
			for (Tag classTag : classTags) {
				if (classTag.getName().equals("InnerClassTag")) {
					InnerClassTag innerClassTag = (InnerClassTag) classTag;
					SootClass innerClass;
					if (sootClass.isConcrete()) {
						innerClass = Scene.v().forceResolve(innerClassTag.getInnerClass().replace("/", "."),
								BODIES);
					} else {
						innerClass = Scene.v().forceResolve(innerClassTag.getInnerClass().replace("/", "."),
								SIGNATURES);
					}
					innerClass.setApplicationClass();
					jimpleClassMappings.add(jimplfyClass(innerClass));
				}
			}
			jimpleClassMappings.add(jimplfyClass(sootClass));
		}
		return jimpleClassMappings;
	}

	private static JimpleClassMapping jimplfyClass(SootClass sc) throws IOException {
		String fileName = SourceLocator.v().getFileNameFor(sc, Options.output_format_jimple);
		OutputStream streamOut = new FileOutputStream(fileName);
		PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
		try {
			Printer.v().printTo(sc, writerOut);
		} catch (RuntimeException exc) {
			exc.printStackTrace();
		}
		writerOut.flush();
		streamOut.close();

		JimpleClassMapping jimpleClassMapping = new JimpleClassMapping(new Pair<>(fileName, sc.getName()));
		System.out.println(fileName);
		return jimpleClassMapping;
	}

	private Set<String> getPackagePaths(ArrayList<String> classFilePaths) throws IOException {

		Set<String> packagePaths = new HashSet<>();

		for (String classFilePath : classFilePaths) {
			Path p = Paths.get(classFilePath);
			byte[] binaryForm = Files.readAllBytes(p);

			ClassReader cr = new ClassReader(binaryForm);
			ClassNode cn = new ClassNode();
			cr.accept(cn, 0);

			classFilePath = classFilePath.replace("\\", "/");
			packagePaths.add(classFilePath.substring(0, classFilePath.indexOf(cn.name)));
		}
		return packagePaths;
	}

	private String checkCache(String discoveredJarHash, String projectName, GradleTaskHelper taskHelper) throws IOException {
		JSONParser parser = new JSONParser();
		File cacheFile = new File(taskHelper.getResultsDir() + "/cache/");
		cacheFile = new File(cacheFile.getAbsolutePath() + "/cachedJars.json");
		JSONArray cachedJars;
		try {
			cachedJars = (JSONArray) parser.parse(new FileReader(cacheFile.getAbsolutePath()));
		} catch (ParseException parseException) {
			cachedJars = new JSONArray();
		}

		for (Object o : cachedJars) {
			JSONObject cachedJar = (JSONObject) o;

			String cachedJarHash = (String) cachedJar.get("jarBytecode");
			String cachedJarOwner = (String) cachedJar.get("projectOwner");

			if (cachedJarHash.equals(discoveredJarHash) && cachedJarOwner.equals(projectName)) {
				return "jarAlreadyBelongsToCurrentProject";
			} else if (cachedJarHash.equals(discoveredJarHash)) {
				return (String) cachedJar.get("jarURI");
			}
		}
		return null;
	}

	private void addToCache(String jarBytecode, String jarURI, String projectName, GradleTaskHelper taskHelper) throws IOException {
		File cacheFile = new File(taskHelper.getResultsDir() + "/cache/");
		cacheFile.mkdirs();
		cacheFile = new File(cacheFile.getAbsolutePath() + "/cachedJars.json");
		cacheFile.createNewFile();

		JSONParser parser = new JSONParser();
		JSONArray cachedJars;
		try {
			cachedJars = (JSONArray) parser.parse(new FileReader(cacheFile.getAbsolutePath()));
		} catch (ParseException parseException) {
			cachedJars = new JSONArray();
		}

		JSONObject newCachedJar = new JSONObject();
		newCachedJar.put("jarBytecode", jarBytecode);
		newCachedJar.put("jarURI", jarURI);
		newCachedJar.put("projectOwner", projectName);

		cachedJars.add(newCachedJar);

		try (FileWriter file = new FileWriter(cacheFile.getAbsolutePath())) {

			file.write(cachedJars.toJSONString());
			file.flush();

		} catch (IOException e) {
			taskHelper.getPw().println(e.getLocalizedMessage());
		}
	}

	// Serialize classpath element, passing an o2tc object and returning a string representation
	private String serializeCPElement(ObjectToTriplesConfiguration config) throws IOException {

		Model dfuModels = ModelFactory.createDefaultModel();
		ObjectMapper mapper = config.getMapper();
		String serialModel = "";

		if (!mapper.getObjectsToSerialize().isEmpty()) {

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
	public static void recordCPElement(String serialMod, String path) throws IOException {
	    FileUtils.writeStringToFile(
	        new File(path).getAbsoluteFile(), 
	        serialMod
	        );
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

	private CompiledJavaSourceFile processClassFileFromString(String path, GradleTaskHelper taskHelper, String vcsAnchor, GradleData gradleData) throws Exception {
		Path p = Paths.get(path);
		byte[] binaryForm = Files.readAllBytes(p);
		String hash = hash(binaryForm);
		String name = p.getFileName().toString();

		String sourceEncoding = "UTF-8"; //TODO guessing

		AClass ac = analyzeClass(binaryForm);
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
		if (IGNORE_BINARIES){
			c.setBinaryForm(new byte[]{0});
		}
		else{
			c.setBinaryForm(binaryForm);
		}

		String tempClassName = ac.getClassName();

		while(tempClassName.contains("$")) {
			tempClassName = tempClassName.substring(0, tempClassName.indexOf('$'));
		}

		final String className = tempClassName;

		ArrayList<String> javaSources = gradleData.getSourceFilePaths();

		String sourceFilePath = null;
		for (String javaSource : javaSources) {
			javaSource = javaSource.replace("\\", "/");
			if (javaSource.contains(className)) {
				sourceFilePath = javaSource;
			}
		}

		//TODO keep an eye on this for now...
		//List<String> list = javaSources.stream().map(source -> {
		//	try {
			//	return getPath(source);
		//	} catch (IOException e) {
			//	taskHelper.getPw().println(e.getLocalizedMessage());
		//	}
		//	return null;
	//	}).filter(source -> source.contains(className)).collect(Collectors.toList());


		//if (!list.isEmpty()) {
		//	sourceFilePath = list.get(0);
	//	}

		if (vcsAnchor != null && sourceFilePath != null) {
			// TODO Phasing out the remote repo path, all should be the path to the project's build file
			//String fileRemoteURL = sourceFilePath.substring(sourceFilePath.indexOf(vcsAnchor) + vcsAnchor.length() + 1);
			VcsCoordinate vcsInfo = new VcsCoordinate();
			vcsInfo.setVersion(gradleData.getVersion()); //TODO
			vcsInfo.setVersionControlUrl(vcsAnchor); //TODO
			c.setVcsInfo(vcsInfo);
			c.setAbsoluteFilePath(sourceFilePath);
		} else {
			taskHelper.getPw().println("Unable to set remote URL and link to local file location, check you've set the correct parameters and/or there are no" +
					"package with file path conflicts.");
		}

		c.setCorrespondingClass(cas);
		c.setHash(hash);
		c.setName(name);
		c.setSourceEncoding(sourceEncoding);
		return c;
	}

	private static String getPath(String path) throws IOException{
		return new File(path).toPath().toRealPath().toFile().getAbsolutePath().replace("\\", "/");
	}

	private AClass analyzeClass(byte[] cb) throws NoSuchAlgorithmException {
		//getClassNode code
		ClassReader cr = new ClassReader(cb);
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		String hash = hash(cb);

		//processClass code
		JarIngestor ji = new JarIngestor(sf);
		return ji.processClass(hash, cn, true);
	}

	private JarArtifact analyzeJar(String jarFile, boolean fullAnalysis) throws IOException{
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
		return JarIngestor.ingest(jar, name, bac.getGroupId(), bac.getArtifactId(), bac.getVersion(), sf, fullAnalysis);
	}

	private static String hash(byte[] in) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(in);
		return Base64.getEncoder().encodeToString(md.digest());
	}

	private static final String COMPILE_CLASSPATH_NAME = "compile";
	private static final String CLASS_ARTIFACTS_DIRECTORY = "/classes/";
	private static final String SOURCES_DIRECTORY = "/structures/sources/";
	private static final String JARS_DIRECTORY = "/structures/jars/";
	private static final String DFUS_DIRECTORY = "/structures/dfus/";
}
