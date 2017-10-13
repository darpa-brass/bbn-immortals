package com.securboration.immortals.ontology.identifier;

import com.securboration.immortals.ontology.pojos.markup.Ignore;

@Ignore
public interface HasUuid {
    
    /**
     * 
     * @return a globally unique ID that identifies a concept over time
     */
    public String getUuid();
    
    /**
     * Sets a globally unique ID that identifies a concept over time
     */
    public void setUuid(String uuid);

}
