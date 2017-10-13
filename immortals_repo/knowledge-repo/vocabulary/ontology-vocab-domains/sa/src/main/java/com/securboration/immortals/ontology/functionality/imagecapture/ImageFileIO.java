package com.securboration.immortals.ontology.functionality.imagecapture;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * Read/write an image from/to a file
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class ImageFileIO extends Functionality {
    
    public ImageFileIO() {
        this.setFunctionalityId("ImageCapture");
        this.setFunctionalAspects(new FunctionalAspect[]{
                new AspectReadImage(),
                new AspectWriteImage()
        });
    }
    
    
    
    

}
