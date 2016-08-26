package com.securboration.immortals.ontology.image.fidelity;

import com.securboration.immortals.ontology.constraint.BinaryComparisonOperatorType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class Monochrome extends ColorFidelity {
    
    public Monochrome(){
        super(
            new ColorChannel(1,ColorType.MONOCHROME)
            );
        
        super.addQualitativeFidelityAssertion(
            BinaryComparisonOperatorType.LESS_THAN_EXCLUSIVE,
            Greyscale8.class,
            Rgb24.class
            );
    }

}
