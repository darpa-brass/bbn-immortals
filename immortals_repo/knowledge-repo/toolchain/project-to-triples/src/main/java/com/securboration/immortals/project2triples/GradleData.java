package com.securboration.immortals.project2triples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradleData {
	
	public GradleData(){
		
	}
	
	public String testFunction(){
		return "HELLO";
	}
	
	/**
	 * Root location of the whole project
	 */
	private String baseDir = new String();
	
	/**
	 * Base location in the SVN for this project
	 */
	private String svnLocation = new String();
	
	/**
	 * Version of immortals
	 */
	private String immortalsVersion = new String();
	
	/**
	 * Path to the build file
	 */
	private String pathToBuildFile = new String();
	
	/**
	 * Has a list of each classpath listed by the plugin to that list of files
	 */
	private HashMap<String,ArrayList<String>> classpathNameToClasspathList = new HashMap<>();
	
	
	/**
	 * List of the .java files
	 */
	private ArrayList<String> sourceFilePaths = new ArrayList<String>();
	
	/**
	 * List of the .java test files
	 */
	private ArrayList<String> testSourceFilePaths = new ArrayList<String>();
	
	/**
	 * List of paths to this project's test .class files
	 */
	private ArrayList<String> testClassFilePaths = new ArrayList<String>();
	
	/**
	 * List of paths to this project's .class files
	 */
	private ArrayList<String> classFilePaths = new ArrayList<String>();
	
	/**
	 * The root directory of where class files are stored
	 */
	private String classFilesRoot = new String();
	
	/**
	 * Path to the final compiled project jar
	 */
	private String compiledProjectJarPath = new String();
	
	/**
	 * Name of the project
	 */
	private String compiledProjectName = new String();

	/**
	 * Gradle project properties
	 */
	private Map<String, ?> properties;

    /**
     * This project's group id
     */
	private String group;

    /**
     * This project's artifact id
     */
	private String artifact;

    /**
     * This project's version
     */
	private String version;

    /**
     * Additional source files that aren't found in the standard src/java/etc. directories
     */
	private List<String> additionalSources;

	public String getCompiledProjectJarPath() {
		return compiledProjectJarPath;
	}

	public void setCompiledProjectJarPath(String compiledProjectJarPath) {
		this.compiledProjectJarPath = compiledProjectJarPath;
	}

	public String getCompiledProjectName() {
		return compiledProjectName;
	}

	public void setCompiledProjectName(String compiledProjectName) {
		this.compiledProjectName = compiledProjectName;
	}

	public ArrayList<String> getTestClassFilePaths() {
		return testClassFilePaths;
	}

	public void setTestClassFilePaths(ArrayList<String> testClassFilePaths) {
		this.testClassFilePaths = testClassFilePaths;
	}

	public ArrayList<String> getClassFilePaths() {
		return classFilePaths;
	}

	public void setClassFilePaths(ArrayList<String> projectClassFilePaths) {
		this.classFilePaths = projectClassFilePaths;
	}

	public String getClassFilesRoot() {
		return classFilesRoot;
	}
	
	public void setClassFilesRoot(String classFilesRoot) { this.classFilesRoot = classFilesRoot; }

	public void setImmortalsVersion(String immortalsVersion){
		this.immortalsVersion = immortalsVersion;
	}
	
	public String getImmortalsVersion(){
		return this.immortalsVersion;
	}

	public String getPathToBuildFile() {
		return pathToBuildFile;
	}

	public void setPathToBuildFile(String pathToBuildFile) {
		this.pathToBuildFile = pathToBuildFile;
	}

	public String getSvnLocation() {
		return svnLocation;
	}

	public void setSvnLocation(String svnLocation) {
		this.svnLocation = svnLocation;
	}

	public ArrayList<String> getSourceFilePaths() {
		return sourceFilePaths;
	}

	public void setSourceFilePaths(ArrayList<String> sourceFilePaths) {
		this.sourceFilePaths = sourceFilePaths;
	}

	public ArrayList<String> getTestSourceFilePaths() {
		return testSourceFilePaths;
	}

	public void setTestSourceFilePaths(ArrayList<String> testSourceFilePaths) {
		this.testSourceFilePaths = testSourceFilePaths;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public HashMap<String, ArrayList<String>> getClasspathNameToClasspathList() {
		return classpathNameToClasspathList;
	}

	public void setClasspathNameToClasspathList(HashMap<String, ArrayList<String>> classpathNameToClasspathList) {
		this.classpathNameToClasspathList = classpathNameToClasspathList;
	}
	
	public void setProperties(Map<String, ?> _properties) {
		properties = _properties;
	}
	
	public Object getProperty(String propName) {
		return properties.get(propName);
	}

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getAdditionalSources() {
        return additionalSources;
    }

    public void setAdditionalSources(List<String> additionalSources) {
        this.additionalSources = additionalSources;
    }
}
