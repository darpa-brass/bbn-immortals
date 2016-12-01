package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.property.Property;

/**
 * A criterion based on the presence, absence, or configuration of a property
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A criterion based on the presence, absence, or configuration of a" +
    " property  @author jstaples ")
public class PropertyCriterion extends CriterionStatement {

    /**
     * The property criterion statement
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The property criterion statement")
    private PropertyCriterionType criterion;
    
    /**
     * The property
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment("The property")
    private Property property;
    
    public PropertyCriterionType getCriterion() {
        return criterion;
    }
    
    public void setCriterion(PropertyCriterionType criterion) {
        this.criterion = criterion;
    }

    
    public Property getProperty() {
        return property;
    }

    
    public void setProperty(Property property) {
        this.property = property;
    }
    
    
}
