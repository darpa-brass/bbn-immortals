package com.securboration.immortals.ontology.functionality.dataproperties;

/**
 * An enumeration of image fidelity dimensions
 * 
 * @author Securboration
 *
 */
public enum ImageFidelityType {

    /**
     * Color depth of the image
     */
    COLOR_FIDELITY,
    
    /**
     * # of raw pixels in the image
     */
    PIXEL_FIDELITY,
    
    /**
     * Minimum size of features resolvable in the image
     */
    FEATURE_SIZE_FIDELITY,
    
    /**
     * The SNR of the image
     */
    NOISE_FIDELITY,
    
    /**
     * The overall quality of the image
     */
    QUALITY_FIDELITY
    
    ;
}
