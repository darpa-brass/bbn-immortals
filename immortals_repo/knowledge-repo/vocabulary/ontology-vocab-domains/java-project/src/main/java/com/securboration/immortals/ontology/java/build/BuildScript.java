package com.securboration.immortals.ontology.java.build;

import com.securboration.immortals.ontology.identifier.HasUuid;

/**
 * Simplistic abstraction of a build script
 * 
 * @author jstaples
 *
 */
public class BuildScript implements HasUuid {

    /**
     * The textual content of the build script
     */
    private String buildScriptContents;

    /**
     * The hash of the build script
     */
    private String hash;

    /**
     * Uniquely identifies the concept over time
     */
    private String uuid;

    public BuildScript(String buildScriptContents, String hash, String uuid) {
		this.buildScriptContents = buildScriptContents;
		this.hash = hash;
		this.uuid = uuid;
	}
    
    public BuildScript(){
    	
    }

	public String getBuildScriptContents() {
        return buildScriptContents;
    }

    public void setBuildScriptContents(String buildScriptContents) {
        this.buildScriptContents = buildScriptContents;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
