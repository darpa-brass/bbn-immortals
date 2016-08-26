package com.securboration.immortals.ontology.image.fidelity;

import com.securboration.immortals.ontology.constraint.BinaryComparisonOperatorType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class ImageSize1024x1024 extends ResolutionFidelity {
    
    public ImageSize1024x1024(){
        super(1024,1024);
        
        super.addQualitativeFidelityAssertion(
            BinaryComparisonOperatorType.GREATER_THAN_EXCLUSIVE,
            ImageSize512x512.class
            );
        
        super.addQualitativeFidelityAssertion(
            BinaryComparisonOperatorType.LESS_THAN_EXCLUSIVE,
            ImageSize4096x4096.class
            );
    }

}
