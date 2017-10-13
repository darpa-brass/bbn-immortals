package com.securboration.immortals.ontology.java.source;

/**
 * Model of a build mechanism for a Java project. Specifically, describes how to
 * build source code to create a bytecode artifact. For example, a Maven build
 * vs a Gradle build vs an Ant build vs a command line build. For now, this is a
 * TODO because we're only
 * 
 * @author Securboration
 *
 */
public class BuildMechanism {
	private String buildMechanism;

	public String getBuildMechanism() {
		return buildMechanism;
	}

	public void setBuildMechanism(String buildMechanism) {
		this.buildMechanism = buildMechanism;
	}
	
	public BuildMechanism(){
		
	}
	

    

}
