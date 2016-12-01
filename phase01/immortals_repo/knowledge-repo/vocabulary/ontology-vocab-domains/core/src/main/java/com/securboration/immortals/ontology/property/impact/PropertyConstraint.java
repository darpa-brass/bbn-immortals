package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.MultiplicityType;
import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.ordering.ExplicitNumericOrderingMechanism;
import com.securboration.immortals.ontology.property.Property;

/**
 * Describes a constraint on a property
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Describes a constraint on a property  @author jstaples ")
public class PropertyConstraint {
    
    /**
     * Human readable description
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Human readable description")
    private String humanReadableForm;
    
    /**
     * The criterion for the constraint
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The criterion for the constraint")
    private PropertyCriterionType constraintCriterion;
    
    /**
     * The multiplicity of the constraint (e.g., does the criterion apply to 
     * all properties or just one)
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The multiplicity of the constraint (e.g., does the criterion apply" +
        " to  all properties or just one)")
    private MultiplicityType constraintMultiplicity;
    
    /**
     * The constrained properties
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The constrained properties")
    private Property[] constrainedProperty;
    
    /**
     * The precedence of this constraint
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The precedence of this constraint")
    private ExplicitNumericOrderingMechanism precedenceOfConstraint;
    
    
    public MultiplicityType getConstraintMultiplicity() {
        return constraintMultiplicity;
    }
    
    public void setConstraintMultiplicity(
            MultiplicityType constraintMultiplicity) {
        this.constraintMultiplicity = constraintMultiplicity;
    }
    
    public PropertyCriterionType getConstraintCriterion() {
        return constraintCriterion;
    }
    
    public void setConstraintCriterion(PropertyCriterionType constraintType) {
        this.constraintCriterion = constraintType;
    }
    
    public Property[] getConstrainedProperty() {
        return constrainedProperty;
    }
    
    public void setConstrainedProperty(Property[] constrainedProperty) {
        this.constrainedProperty = constrainedProperty;
    }
    
    public PropertyConstraint(){}

    public PropertyConstraint(
            String humanReadableForm,
            MultiplicityType constraintMultiplicity,
            PropertyCriterionType constraintType,
            Property...constrainedProperty
            ) {
        super();
        this.humanReadableForm = humanReadableForm;
        this.constraintMultiplicity = constraintMultiplicity;
        this.constraintCriterion = constraintType;
        this.constrainedProperty = constrainedProperty;
    }

    
    public String getHumanReadableForm() {
        return humanReadableForm;
    }

    
    public void setHumanReadableForm(String humanReadableForm) {
        this.humanReadableForm = humanReadableForm;
    }

    
    public ExplicitNumericOrderingMechanism getPrecedenceOfConstraint() {
        return precedenceOfConstraint;
    }

    
    public void setPrecedenceOfConstraint(
            ExplicitNumericOrderingMechanism precedenceOfConstraint) {
        this.precedenceOfConstraint = precedenceOfConstraint;
    }
    

}
