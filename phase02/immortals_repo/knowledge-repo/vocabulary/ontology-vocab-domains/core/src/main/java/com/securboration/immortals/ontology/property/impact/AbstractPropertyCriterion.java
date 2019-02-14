package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.property.Property;

/**
 * Models a criterion that operates on an abstract property 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Models a criterion that operates on an abstract property  @author" +
    " jstaples ")
public class AbstractPropertyCriterion extends CriterionStatement {
    
    /**
     * The type of criterion operator to apply to the abstract property
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The type of criterion operator to apply to the abstract property")
    private PropertyCriterionType criterion;
    
    /**
     * The abstract property to which a criterion operator is applied
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The abstract property to which a criterion operator is applied")
    private Class<? extends Property> property;
    
    public PropertyCriterionType getCriterion() {
        return criterion;
    }
    
    public void setCriterion(PropertyCriterionType criterion) {
        this.criterion = criterion;
    }
    
    public Class<? extends Property> getProperty() {
        return property;
    }
    
    public void setProperty(Class<? extends Property> property) {
        this.property = property;
    }
    
    
}
