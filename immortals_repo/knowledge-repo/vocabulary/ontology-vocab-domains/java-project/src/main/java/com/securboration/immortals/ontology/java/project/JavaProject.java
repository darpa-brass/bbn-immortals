package com.securboration.immortals.ontology.java.project;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifact;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.identifier.HasUuid;
import com.securboration.immortals.ontology.java.build.BuildScript;
import com.securboration.immortals.ontology.java.compiler.*;
import com.securboration.immortals.ontology.java.source.CompiledJavaSourceFile;
import com.securboration.immortals.ontology.java.source.SourceCodeRepo;
import com.securboration.immortals.ontology.java.vcs.VcsCoordinate;

/**
 * A Java Project includes the abstractions for building and executing a Java
 * software application
 * 
 * @author jstaples
 *
 */
public class JavaProject implements HasUuid {
    
    /**
     * Uniquely identifies the project
     */
    private String uuid;
    
    /**
     * The build script for the project
     */
    private BuildScript buildScript;
    
    private String[] compiledSourceHash;
    
    /**
     * The compiled test code in this project
     */
    private CompiledJavaSourceFile[] compiledTestSource;
    
    /**
     * A list 
     */
    private NamedClasspath[] classpaths;
    
    /**
     * The result of compiling this Java project. For now, assumed to be a
     * JAR. TODO: could be something other than a JAR--for example, AAR, EAR,
     * naked classes (unpackaged), etc.
     */
    private BytecodeArtifact compiledSoftware;
    
    /**
     * This project's coordinate, e.g. group:artifact:version
     */
    private BytecodeArtifactCoordinate coordinate;
    
    private VcsCoordinate vcsCoordinate;
    
    private SourceCodeRepo sourceCodeRepo;

    public String[] getCompiledSourceHash() {
        return compiledSourceHash;
    }

    public void setCompiledSourceHash(String[] compiledSourceHash) {
        this.compiledSourceHash = compiledSourceHash;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public BuildScript getBuildScript() {
        return buildScript;
    }

    
    public void setBuildScript(BuildScript buildScript) {
        this.buildScript = buildScript;
    }
    
    public CompiledJavaSourceFile[] getCompiledTestSource() {
        return compiledTestSource;
    }

    
    public void setCompiledTestSource(CompiledJavaSourceFile[] compiledTestSource) {
        this.compiledTestSource = compiledTestSource;
    }

    
    public BytecodeArtifact getCompiledSoftware() {
        return compiledSoftware;
    }

    
    public void setCompiledSoftware(BytecodeArtifact compiledSoftware) {
        this.compiledSoftware = compiledSoftware;
    }
    
	public NamedClasspath[] getClasspaths() {
		return classpaths;
	}

	public void setClasspaths(NamedClasspath[] classpaths) {
		this.classpaths = classpaths;
	}


    public BytecodeArtifactCoordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(BytecodeArtifactCoordinate coordinate) {
        this.coordinate = coordinate;
    }

    public SourceCodeRepo getSourceCodeRepo() {
        return sourceCodeRepo;
    }

    public void setSourceCodeRepo(SourceCodeRepo sourceCodeRepo) {
        this.sourceCodeRepo = sourceCodeRepo;
    }

    public VcsCoordinate getVcsCoordinate() {
        return vcsCoordinate;
    }

    public void setVcsCoordinate(VcsCoordinate vcsCoordinate) {
        this.vcsCoordinate = vcsCoordinate;
    }
}
