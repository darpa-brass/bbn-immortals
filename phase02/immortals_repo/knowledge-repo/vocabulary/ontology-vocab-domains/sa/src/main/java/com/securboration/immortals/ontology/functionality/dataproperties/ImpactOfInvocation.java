package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;
import com.securboration.immortals.ontology.property.Property;

/**
 * Abstraction of the impact of an invocation on some Property dimension
 * 
 * @author Securboration
 *
 */
@GenerateAnnotation
public class ImpactOfInvocation extends DataProperty {
    
    private ImpactType impactOfInvocation;
    private Class<? extends Property>[] impactedProperties;
    
    public ImpactType getImpactOfInvocation() {
        return impactOfInvocation;
    }
    
    public void setImpactOfInvocation(ImpactType impactOfInvocation) {
        this.impactOfInvocation = impactOfInvocation;
    }
    
    public ImpactOfInvocation(){}

    
    public Class<? extends Property>[] getImpactedProperties() {
        return impactedProperties;
    }

    
    public void setImpactedProperties(
            Class<? extends Property>[] impactedProperties) {
        this.impactedProperties = impactedProperties;
    }
    
    
    
}
