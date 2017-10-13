package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.functionality.datatype.DataProperty;

/**
 * A binding of fidelity dimensions to impacts on those dimensions
 * 
 * @author Securboration
 *
 */
public class ImageFidelityImpacts extends DataProperty {

    /**
     * The affected dimensions
     */
    private ImageFidelityImpact[] imageFidelityImpacts;

    
    public ImageFidelityImpact[] getImageFidelityImpacts() {
        return imageFidelityImpacts;
    }

    
    public void setImageFidelityImpacts(
            ImageFidelityImpact[] imageFidelityImpacts) {
        this.imageFidelityImpacts = imageFidelityImpacts;
    }
    
}
