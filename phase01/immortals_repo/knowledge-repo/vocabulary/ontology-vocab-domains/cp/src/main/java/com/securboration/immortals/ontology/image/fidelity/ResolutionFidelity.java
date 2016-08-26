package com.securboration.immortals.ontology.image.fidelity;

import com.securboration.immortals.ontology.functionality.dataproperties.ImageFidelity;

public class ResolutionFidelity extends ImageFidelity {
    
    private int width;
    
    private int height;

    public ResolutionFidelity(){}

    public ResolutionFidelity(int width, int height) {
        super();
        this.width = width;
        this.height = height;
    }

    
    public int getWidth() {
        return width;
    }

    
    public void setWidth(int width) {
        this.width = width;
    }

    
    public int getHeight() {
        return height;
    }

    
    public void setHeight(int height) {
        this.height = height;
    }

}
