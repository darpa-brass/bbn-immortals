package com.securboration.immortals.ontology.bytecode;


/**
 * A coordinate that identifies a bytecode artifact
 * 
 * @author Securboration
 *
 */
public class BytecodeArtifactCoordinate {

    /**
     * A group identifier
     */
    private String groupId;
    
    /**
     * An artifact identifier
     */
    private String artifactId;
    
    /**
     * A version identifier
     */
    private String version;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString()
    {
        return "[" + groupId + "] " + artifactId + " : " + version;
    }
}
