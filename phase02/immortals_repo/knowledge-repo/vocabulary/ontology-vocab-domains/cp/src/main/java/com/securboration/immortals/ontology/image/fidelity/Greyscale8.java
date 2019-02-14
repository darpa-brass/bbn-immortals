package com.securboration.immortals.ontology.image.fidelity;

import com.securboration.immortals.ontology.constraint.BinaryComparisonOperatorType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class Greyscale8 extends ColorFidelity {
    
    public Greyscale8(){
        super(
            new ColorChannel(8,ColorType.GREYSCALE)
            );
        
        super.addQualitativeFidelityAssertion(
            BinaryComparisonOperatorType.GREATER_THAN_EXCLUSIVE,
            Monochrome.class
            );
        super.addQualitativeFidelityAssertion(
            BinaryComparisonOperatorType.LESS_THAN_EXCLUSIVE,
            Rgb24.class
            );
    }

}
