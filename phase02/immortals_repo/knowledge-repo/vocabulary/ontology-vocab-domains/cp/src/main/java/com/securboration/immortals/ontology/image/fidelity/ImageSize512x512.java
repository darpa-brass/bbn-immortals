package com.securboration.immortals.ontology.image.fidelity;

import com.securboration.immortals.ontology.constraint.BinaryComparisonOperatorType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class ImageSize512x512 extends ResolutionFidelity {
    
    public ImageSize512x512(){
        super(512,512);
        
        super.addQualitativeFidelityAssertion(
            BinaryComparisonOperatorType.LESS_THAN_EXCLUSIVE,
            ImageSize1024x1024.class,
            ImageSize4096x4096.class
            );
    }

}
