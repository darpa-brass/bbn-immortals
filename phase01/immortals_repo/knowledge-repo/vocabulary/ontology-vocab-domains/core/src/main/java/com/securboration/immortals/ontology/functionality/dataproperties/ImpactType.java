package com.securboration.immortals.ontology.functionality.dataproperties;

/**
 * An enumeration of impact types. E.g., some condition may degrade performance
 * whereas another would improve it.
 * 
 * Eventually this will be broken out into several more fine-grained
 * enumerations but for now keeping everything in one place for simplicity
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An enumeration of impact types. E.g., some condition may degrade" +
    " performance whereas another would improve it.  Eventually this will" +
    " be broken out into several more fine-grained enumerations but for now" +
    " keeping everything in one place for simplicity  @author jstaples ")
public enum ImpactType {
    
    IMPROVES_SIGNIFICANTLY,
    IMPROVES,
    
    DOES_NOT_AFFECT,
    
    DEGRADES,
    DEGRADES_SIGNIFICANTLY,
    
    OPTIMIZES,
    
    REPAIRS,
    DESTROYS,
    
    INCREASES,
    DECREASES,
    
    MAXIMIZES,
    MINIMIZES,
    
    ADDS,
    REMOVES,
    
    ;
}
