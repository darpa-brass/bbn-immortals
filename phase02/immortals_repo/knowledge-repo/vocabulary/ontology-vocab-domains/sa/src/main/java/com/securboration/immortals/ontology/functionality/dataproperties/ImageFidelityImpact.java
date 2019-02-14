package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;

/**
 * A binding of fidelity dimensions to impacts on those dimensions
 * 
 * @author Securboration
 *
 */
public class ImageFidelityImpact extends DataProperty {

    /**
     * The affected dimensions
     */
    private ImageFidelityType[] fidelityDimensions;
    
    /**
     * The fidelity impacts on those dimensions
     */
    private ImpactType[] fidelityImpacts;

    
    public ImageFidelityType[] getFidelityDimensions() {
        return fidelityDimensions;
    }

    
    public void setFidelityDimensions(ImageFidelityType[] fidelityDimensions) {
        this.fidelityDimensions = fidelityDimensions;
    }


    
    public ImpactType[] getFidelityImpacts() {
        return fidelityImpacts;
    }


    
    public void setFidelityImpacts(ImpactType[] fidelityImpacts) {
        this.fidelityImpacts = fidelityImpacts;
    }
    
}
