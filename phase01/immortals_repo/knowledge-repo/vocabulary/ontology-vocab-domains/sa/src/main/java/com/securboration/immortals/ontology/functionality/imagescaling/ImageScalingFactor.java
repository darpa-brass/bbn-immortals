package com.securboration.immortals.ontology.functionality.imagescaling;

import com.securboration.immortals.ontology.property.Property;

/**
 * A top-level abstraction of a desired image size
 * 
 * @author Securboration
 *
 */
public class ImageScalingFactor extends Property {
    private double scalingFactor;

    
    public double getScalingFactor() {
        return scalingFactor;
    }

    
    public void setScalingFactor(double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }
}
