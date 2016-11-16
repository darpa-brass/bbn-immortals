package com.securboration.immortals.ontology.functionality.imagescaling;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * Resize an image
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class ImageResizer extends Functionality {
    
    public ImageResizer() {
        this.setFunctionalityId("ImageResizer");
        this.setFunctionalAspects(new FunctionalAspect[]{
                new ShrinkImage(),
                new EnlargeImage(),
        });
    }
    
    
    
    

}
