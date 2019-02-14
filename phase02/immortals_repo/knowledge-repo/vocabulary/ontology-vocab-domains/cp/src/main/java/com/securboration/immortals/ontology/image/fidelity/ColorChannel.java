package com.securboration.immortals.ontology.image.fidelity;


public class ColorChannel {
    
    private int bitDepth;
    
    private ColorType channelColor;

    
    public int getBitDepth() {
        return bitDepth;
    }
    
    public void setBitDepth(int bitDepth) {
        this.bitDepth = bitDepth;
    }
    
    public ColorType getChannelColor() {
        return channelColor;
    }


    
    public void setChannelColor(ColorType channelColor) {
        this.channelColor = channelColor;
    }


    public ColorChannel(){}
    
    public ColorChannel(int bitDepth, ColorType channelColor) {
        super();
        this.bitDepth = bitDepth;
        this.channelColor = channelColor;
    }

}
