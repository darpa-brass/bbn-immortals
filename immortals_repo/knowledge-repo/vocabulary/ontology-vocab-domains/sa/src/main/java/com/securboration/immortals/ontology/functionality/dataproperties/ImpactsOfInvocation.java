package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

/**
 * Abstraction of the impact of an invocation on some Property dimension
 * 
 * @author Securboration
 *
 */
@GenerateAnnotation
public class ImpactsOfInvocation extends DataProperty {
    
    private ImpactOfInvocation[] impacts;

    
    public ImpactOfInvocation[] getImpacts() {
        return impacts;
    }

    
    public void setImpacts(ImpactOfInvocation[] impacts) {
        this.impacts = impacts;
    }
    
    
    
}
