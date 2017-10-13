package com.securboration.immortals.ontology.functionality.imagescaling;

import com.securboration.immortals.ontology.functionality.datatype.DataProperty;

/**
 * The # of pixels in an image
 * 
 * @author Securboration
 *
 */
public class NumberOfPixels extends DataProperty {
    
    private int widthPixels;
    private int heightPixels;
    private int totalPixels;
    
    public int getWidthPixels() {
        return widthPixels;
    }
    
    public void setWidthPixels(int widthPixels) {
        this.widthPixels = widthPixels;
    }
    
    public int getHeightPixels() {
        return heightPixels;
    }
    
    public void setHeightPixels(int heightPixels) {
        this.heightPixels = heightPixels;
    }
    
    public int getTotalPixels() {
        return totalPixels;
    }
    
    public void setTotalPixels(int totalPixels) {
        this.totalPixels = totalPixels;
    }
    
}
