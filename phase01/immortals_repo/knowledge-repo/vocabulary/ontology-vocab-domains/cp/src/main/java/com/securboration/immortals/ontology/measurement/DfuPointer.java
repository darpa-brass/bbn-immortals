package com.securboration.immortals.ontology.measurement;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.uris.Uris.rdfs;

/**
 * A durable but indirect way to reference a DFU or an aspect of a DFU. 
 * Provide as much/little info as desired.
 * 
 * @author Securboration
 */
public class DfuPointer extends CodeUnitPointer {
    
    private Class<? extends Functionality> relevantFunctionality;
    private Class<? extends FunctionalAspect> relevantFunctionalAspect;
    
    public Class<? extends Functionality> getRelevantFunctionality() {
        return relevantFunctionality;
    }
    
    public void setRelevantFunctionality(
            Class<? extends Functionality> relevantFunctionality) {
        this.relevantFunctionality = relevantFunctionality;
    }
    
    public Class<? extends FunctionalAspect> getRelevantFunctionalAspect() {
        return relevantFunctionalAspect;
    }
    
    public void setRelevantFunctionalAspect(
            Class<? extends FunctionalAspect> relevantFunctionalAspect) {
        this.relevantFunctionalAspect = relevantFunctionalAspect;
    }
    
    

}
