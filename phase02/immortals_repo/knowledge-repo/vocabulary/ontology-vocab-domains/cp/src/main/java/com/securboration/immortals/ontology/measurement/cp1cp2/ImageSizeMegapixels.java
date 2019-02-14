package com.securboration.immortals.ontology.measurement.cp1cp2;

import com.securboration.immortals.ontology.property.Property;

/**
 * The size of an image in megapixels
 * 
 * @author jstaples
 *
 */
public class ImageSizeMegapixels extends Property {
    
    private double numMegapixels;

    
    public double getNumMegapixels() {
        return numMegapixels;
    }

    
    public void setNumMegapixels(double numMegapixels) {
        this.numMegapixels = numMegapixels;
    }

}
