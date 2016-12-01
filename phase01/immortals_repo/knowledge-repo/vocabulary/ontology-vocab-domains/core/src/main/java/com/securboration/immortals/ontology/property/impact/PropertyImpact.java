package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.PropertyImpactType;
import com.securboration.immortals.ontology.property.Property;

/**
 * Models an impact on a property
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Models an impact on a property  @author jstaples ")
public class PropertyImpact extends ImpactStatement {
    
    /**
     * The impact type
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The impact type")
    private PropertyImpactType impactOnProperty;
    
    /**
     * The impacted property
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The impacted property")
    private Class<? extends Property> impactedProperty;
    
    public PropertyImpactType getImpactOnProperty() {
        return impactOnProperty;
    }
    
    public void setImpactOnProperty(PropertyImpactType impactOnProperty) {
        this.impactOnProperty = impactOnProperty;
    }
    
    public Class<? extends Property> getImpactedProperty() {
        return impactedProperty;
    }
    
    public void setImpactedProperty(Class<? extends Property> impactedProperty) {
        this.impactedProperty = impactedProperty;
    }

}
