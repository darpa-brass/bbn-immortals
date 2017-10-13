package com.securboration.immortals.ontology.image.fidelity;

import com.securboration.immortals.ontology.constraint.BinaryComparisonOperatorType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class ImageSize4096x4096 extends ResolutionFidelity {
    
    public ImageSize4096x4096(){
        super(4096,4096);
        
        super.addQualitativeFidelityAssertion(
            BinaryComparisonOperatorType.GREATER_THAN_EXCLUSIVE,
            ImageSize1024x1024.class,
            ImageSize512x512.class
            );
    }

}
