package com.securboration.immortals.ontology.functionality.compression;

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
public class Compressor extends Functionality {
    
    public Compressor() {
        this.setFunctionalityId("ImageProcessor");
        this.setFunctionalAspects(new FunctionalAspect[]{
                new AspectInflate(),
                new AspectDeflate(),
        });
    }
    
    
    
    

}
