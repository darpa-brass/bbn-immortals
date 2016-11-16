package com.securboration.immortals.ontology.cp;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.ordering.ExplicitNumericOrderingMechanism;
import com.securboration.immortals.ontology.property.impact.PropertyConstraint;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Describes the functionalities being performed by the software \n" +
        "(ie, its intent with regard to core functionalities).  \n\n" +
        "Note that for now we're modeling intent as a set of constraints\n" +
        "that bind to an abstraction of functionality (eg LocationProvider).\n"+
        "Eventually, we'll want to bind constraints to an actual point of \n" +
        "use, defined like \"Writes SA_Data to Disk\"."
        )
    )
public class FunctionalitySpec extends SoftwareSpec {
    
    //                      this should be FunctionalityPointOfUse
    //  [SENDS] [SADATA] [NETWORK]
    private Class<? extends Functionality> functionalityPerformed;

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
    
}
