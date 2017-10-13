package com.securboration.immortals.ontology.image.fidelity;

import com.securboration.immortals.ontology.functionality.dataproperties.ImageFidelity;

public class ColorFidelity extends ImageFidelity {
    
    private ColorChannel[] channels;

    public ColorFidelity(){}
    
    public ColorChannel[] getChannels() {
        return channels;
    }

    
    public void setChannels(ColorChannel[] channels) {
        this.channels = channels;
    }


    public ColorFidelity(ColorChannel...channels) {
        super();
        this.channels = channels;
    }

}
