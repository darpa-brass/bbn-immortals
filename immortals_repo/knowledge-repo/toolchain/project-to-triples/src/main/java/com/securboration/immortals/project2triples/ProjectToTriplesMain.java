package com.securboration.immortals.project2triples;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import com.securboration.immortals.instantiation.annotationparser.bytecode.BytecodeHelper;
import com.securboration.immortals.instantiation.annotationparser.traversal.AnnotationParser;
import com.securboration.immortals.instantiation.annotationparser.traversal.JarTraverser;
import com.securboration.immortals.ontology.bytecode.*;


import com.securboration.immortals.ontology.java.compiler.NamedClasspath;

import com.securboration.immortals.semanticweaver.ObjectMapper;

import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

public class ProjectToTriplesMain {

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

	public static void main(String[] args) {
		System.out.println("testing");
	}
	
	public String testFunction(){
		return "Hello, world!";
	}
	
	public String gradleDataToTriples(GradleData gd, GradleTaskHelper taskHelper, ArrayList<String> includedLibs) throws Exception{
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
		Path ppom = Paths.get(gd.getPathToBuildFile());
		BuildScript buildScript;
		if (ppom.toFile().exists() || ppom.toString().equals("")){
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
		Set<String> elementPathList = new HashSet<>();
		NamedClasspath gpc;
		
		JarArtifact jal;
        String completeAnalysis = String.valueOf(gd.getProperty("completeAnalysis"));
		
        if (!Boolean.parseBoolean(completeAnalysis)) {

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
        
        ArrayList<String> customAnnotClasses = new ArrayList<>();
        customAnnotClasses.addAll(gd.getClasspathNameToClasspathList().get("compile"));
        customAnnotClasses.addAll(getPackagePaths(gd.getClassFilePaths()));
        
        String[] neededClassPaths = new String[customAnnotClasses.size()];
        int i = 0;
        for (String classPath : customAnnotClasses) {
            neededClassPaths[i] = classPath;
            i++;
        }
        
		// For each classpath...
		for (String key : gd.getClasspathNameToClasspathList().keySet()){
			ArrayList<String> jarPath = gd.getClasspathNameToClasspathList().get(key);
			// Debugging statement
			gpc = new NamedClasspath();
			// Add all methods on this classpath to cache
			for (String path : new HashSet<>(gd.getClassFilePaths())) {
				ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration(gd.getImmortalsVersion());
				AnnotationParser annotationParser = new AnnotationParser(config, neededClassPaths);
				
				CompiledJavaSourceFile sourceFile = processClassFileFromString(path, taskHelper, String.valueOf(gd.getProperty("vcsAnchor")), gd);
				
				if (elementPathList.add(path)) {
					compiledSourcesHash.add(sourceFile.getHash());
					Model classModel = ObjectToTriples.convert(o2tc, sourceFile);
					String serialModel = OntologyHelper.serializeModel(classModel, "Turtle", false);
					recordCPElement(serialModel, taskHelper.getResultsDir() + gd.getCompiledProjectName()
							+ SOURCES_DIRECTORY + sourceFile.getName() + RDFFormat.TURTLE.ext);

					byte[] bytecode = FileUtils.readFileToByteArray(new File(path));
					annotationParser.visitClass(BytecodeHelper.hash(bytecode), bytecode);
					annotationParser.visitMethods(BytecodeHelper.hash(bytecode), bytecode);
					//Serialize each dfu element for each class file
					serialModel = serializeCPElement(config);
					if (!serialModel.equals("")) {
						recordCPElement(serialModel, taskHelper.getResultsDir() + gd.getCompiledProjectName()
								+ DFUS_DIRECTORY + sourceFile.getName() + "-DFU" + RDFFormat.TURTLE.ext);
					}
				}
			}
			for (String path : jarPath) {
				ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration(gd.getImmortalsVersion());
				AnnotationParser annotationParser = new AnnotationParser(config, neededClassPaths);
				// Check if we have already processed this file
				if (elementPathList.add(path)) {
					// Single class file on path
					if (path.endsWith(".class")) {
						// Analyze class file element
						ClasspathElement element = JarIngestor.ingest(new File(path), sf);
						// If it's brand new, serialize and output
						if (gpc.addElementHashValue(element.getHash())) {
							taskHelper.getPw().println("New file, adding to master list : " + path);

							//Serialize Rdf model and output for each class file
							Model classModel = ObjectToTriples.convert(o2tc, element);
							String serialModel = OntologyHelper.serializeModel(classModel, "Turtle", false);
							recordCPElement(serialModel, taskHelper.getResultsDir()
									 + "/" + key + CLASS_ARTIFACTS_DIRECTORY + element.getName() + RDFFormat.TURTLE.ext);

							// Traverse class for dfu's
							byte[] bytecode = FileUtils.readFileToByteArray(new File(path));
							annotationParser.visitClass(BytecodeHelper.hash(bytecode), bytecode);
							annotationParser.visitMethods(BytecodeHelper.hash(bytecode), bytecode);
							//Serialize each dfu element for each class file
							serialModel = serializeCPElement(config);
							if (!serialModel.equals("")) {
								recordCPElement(serialModel, taskHelper.getResultsDir()
										+ DFUS_DIRECTORY + element.getName() + "-DFU" + RDFFormat.TURTLE.ext);
							}
						}
						// Jar file(s) on path
					} else {
						
						// Analyze jar artifact
						jal = analyzeJar(path, (includedLibs != null && includedLibs.stream()
                                .noneMatch(lib -> path.contains(lib)) ? false : true));
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
                                
                                String jarURI = o2tc.getNamingContext().getNameForObject(jal);
                                addToCache(jal.getHash(), jarURI, gd.getCompiledProjectName(), taskHelper);
                                
                                String serialModel = OntologyHelper.serializeModel(jarArtModel, "Turtle", false);
                                recordCPElement(serialModel, taskHelper.getResultsDir() + gd.getCompiledProjectName()
                                        + JARS_DIRECTORY + jal.getName() + RDFFormat.TURTLE.ext);

                                // Traverse jar for DFU's
                                JarTraverser.traverseJar(new File(path), annotationParser, annotationParser);
                                // Serialize DFU elements and output for each jar
                                serialModel = serializeCPElement(config);
                                if (!serialModel.equals("")) {
                                    recordCPElement(serialModel, taskHelper.getResultsDir() + gd.getCompiledProjectName()
                                            + DFUS_DIRECTORY + jal.getName() + "DFU" + RDFFormat.TURTLE.ext);
                                }
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
					/*
					 * Even if the artifact is not brand new, we want to record its call graph info
					 *  because it might contain different information, depending on what other artifacts 
					 *  are on the path with it.
 					 */
				}
			}
			gpc.setClasspathName(key);
			gradlePaths.add(gpc);
			NamedClasspath dummyClasspath = new NamedClasspath();
			ObjectToTriples.convert(o2tc, gpc);
			o2tc.getNamingContext().setNameForObject(dummyClasspath, o2tc.getNamingContext().getNameForObject(gpc));
		}
		
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
		Model m = ObjectToTriples.convert(o2tc, x);
		return OntologyHelper.serializeModel(m, "Turtle", false);
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
	private static void recordCPElement(String serialMod, String path) throws IOException {
		File mod2Rdf = new File(path);
		mod2Rdf.getParentFile().mkdirs();
		mod2Rdf.createNewFile();

        try (FileWriter fileWriter = new FileWriter(mod2Rdf)) {
            fileWriter.write(serialMod);
            fileWriter.flush();
        }
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
		List<String> list = javaSources.stream().map(source -> {
            try {
                return getPath(source);
            } catch (IOException e) {
                taskHelper.getPw().println(e.getLocalizedMessage());
            }
            return null;
        }).filter(source -> source.contains(className)).collect(Collectors.toList());
		
		String sourceFilePath = null;
		if (!list.isEmpty()) {
		    sourceFilePath = list.get(0);
        }
        
		if (!vcsAnchor.equals("null") && sourceFilePath != null) {
            String fileRemoteURL = sourceFilePath.substring(sourceFilePath.indexOf(vcsAnchor) + vcsAnchor.length() + 1);
            VcsCoordinate vcsInfo = new VcsCoordinate();
            vcsInfo.setVersion("TEMP"); //TODO
            vcsInfo.setVersionControlUrl("https://dsl-external.bbn.com/svn/immortals/trunk" + fileRemoteURL); //TODO
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
		return ji.processClass(hash, cn);
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
