package com.securboration.immortals.ontology.functionality.imageprocessor;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * Processes an image in some arbitrary manner
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class ImageProcessor extends Functionality {
    
    public ImageProcessor() {
        this.setFunctionalityId("ImageProcessor");
        this.setFunctionalAspects(new FunctionalAspect[]{
                new AspectImageProcessorCleanup(),
                new AspectImageProcessorInitialize(),
                new AspectImageProcessorProcessImage(),
        });
    }
    
    
    
    

}
