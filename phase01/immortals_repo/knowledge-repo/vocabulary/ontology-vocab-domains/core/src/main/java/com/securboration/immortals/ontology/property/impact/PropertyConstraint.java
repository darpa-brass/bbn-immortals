package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.MultiplicityType;
import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.ordering.ExplicitNumericOrderingMechanism;
import com.securboration.immortals.ontology.property.Property;

public class PropertyConstraint {
    
    private String humanReadableForm;
    
    private PropertyCriterionType constraintCriterion;
    private MultiplicityType constraintMultiplicity;
    private Property[] constrainedProperty;
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
