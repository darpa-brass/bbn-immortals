package com.securboration.immortals.ontology.image.fidelity;

import com.securboration.immortals.ontology.constraint.BinaryComparisonOperatorType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class Rgb24 extends ColorFidelity {
    
    public Rgb24(){
        super(
            new ColorChannel(24,ColorType.RED),
            new ColorChannel(24,ColorType.GREEN),
            new ColorChannel(24,ColorType.BLUE)
            );
        
        super.addQualitativeFidelityAssertion(
            BinaryComparisonOperatorType.GREATER_THAN_EXCLUSIVE,
            Greyscale8.class,
            Monochrome.class
            );
    }

}
