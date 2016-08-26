package com.securboration.immortals.deployment.pojos;

import java.util.Collection;
import java.util.HashSet;

/**
 * Everything directly derived from the GME JSON will extend this type. Includes
 * various fields useful for posterior forensics.
 * 
 * @author jstaples
 *
 */
public abstract class ModelDerivedArtifact {

    /**
     * A human readable name for this thing
     */
    private String name;

    /**
     * Some documentation for this thing
     */
    private String comments;
    
    /**
     * A uuid in the JSON.  Allows us to reverse engineer and sanity check the 
     * POJO instantiation process
     */
    private String uuid;

    /**
     * A list of relevant node UUIDs for this thing
     */
    private final Collection<String> relevantNodeUuids = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Collection<String> getRelevantNodeUuids() {
        return relevantNodeUuids;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
