package com.securboration.immortals.ontology.cp;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.ordering.ExplicitNumericOrderingMechanism;
import com.securboration.immortals.ontology.property.impact.PropertyConstraint;

/**
 * Describes the functionalities being performed by the software
 * (ie, its intent with regard to core functionalities).
 * Note that for now we're modeling intent as a set of constraints
 * that bind to an abstraction of functionality (eg LocationProvider).
 * Eventually, we'll want to bind constraints to an actual point of
 * use, defined like "Writes SA_Data to Disk".
 * 
 * @author Securboration
 */
public class FunctionalitySpec extends SoftwareSpec {
    
    //                      this should be FunctionalityPointOfUse
    //  [SENDS] [SADATA] [NETWORK]
    private Class<? extends Functionality> functionalityPerformed;
    
    private Class<? extends FunctionalAspect> functionalityProvided;

    private PropertyConstraint[] propertyConstraint;

    
    public Class<? extends Functionality> getFunctionalityPerformed() {
        return functionalityPerformed;
    }

    
    public void setFunctionalityPerformed(
            Class<? extends Functionality> functionalityPerformed) {
        this.functionalityPerformed = functionalityPerformed;
    }

    
    public PropertyConstraint[] getPropertyConstraint() {
        return propertyConstraint;
    }

    
    public void setPropertyConstraint(PropertyConstraint[] propertyConstraint) {
        this.propertyConstraint = propertyConstraint;
    }

    public FunctionalitySpec(){}

    public FunctionalitySpec(
            Class<? extends Functionality> functionalityPerformed,
            PropertyConstraint...propertyConstraint) {
        super();
        this.functionalityPerformed = functionalityPerformed;
        this.propertyConstraint = propertyConstraint;
        
        if(propertyConstraint.length > 1){
            int counter = 0;
            for(PropertyConstraint p:propertyConstraint){
                ExplicitNumericOrderingMechanism n = 
                        new ExplicitNumericOrderingMechanism();
                n.setPrecedence(counter++);
                p.setPrecedenceOfConstraint(n);
            }
        }
    }


    
    public Class<? extends FunctionalAspect> getFunctionalityProvided() {
        return functionalityProvided;
    }


    
    public void setFunctionalityProvided(
            Class<? extends FunctionalAspect> functionalityProvided) {
        this.functionalityProvided = functionalityProvided;
    }
    
}
