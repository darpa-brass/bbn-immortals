package com.securboration.immortals.analysis.semantic;

public interface SemanticExtractable {
    
    /**
     * 
     * @return the URI for this concept
     */
    public String getUri();
    
    /**
     * 
     * @return a semantic model of concepts related to this one
     */
    public TripleModel toSemanticForm();
    
}
