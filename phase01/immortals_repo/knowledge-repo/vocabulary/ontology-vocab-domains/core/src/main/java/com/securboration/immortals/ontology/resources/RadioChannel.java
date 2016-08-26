package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.resources.gps.SpectrumKeying;

/**
 * A radio communication channel
 * 
 * @author Securboration
 *
 */
public class RadioChannel extends CommunicationChannel {
    
    private String channelName;
    private double minFrequencyHertz;
    private double centerFrequencyHertz;
    private double maxFrequencyHertz;
    
    private SpectrumKeying modulationStrategy;
    
    private Class<? extends Property>[] channelProperties;

    
    public String getChannelName() {
        return channelName;
    }

    
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    
    public double getMinFrequencyHertz() {
        return minFrequencyHertz;
    }

    
    public void setMinFrequencyHertz(double minFrequencyHertz) {
        this.minFrequencyHertz = minFrequencyHertz;
    }

    
    public double getCenterFrequencyHertz() {
        return centerFrequencyHertz;
    }

    
    public void setCenterFrequencyHertz(double centerFrequencyHertz) {
        this.centerFrequencyHertz = centerFrequencyHertz;
    }

    
    public double getMaxFrequencyHertz() {
        return maxFrequencyHertz;
    }

    
    public void setMaxFrequencyHertz(double maxFrequencyHertz) {
        this.maxFrequencyHertz = maxFrequencyHertz;
    }

    
    public Class<? extends Property>[] getChannelProperties() {
        return channelProperties;
    }

    
    public void setChannelProperties(
            Class<? extends Property>[] channelProperties) {
        this.channelProperties = channelProperties;
    }


    
    public SpectrumKeying getModulationStrategy() {
        return modulationStrategy;
    }


    
    public void setModulationStrategy(SpectrumKeying modulationStrategy) {
        this.modulationStrategy = modulationStrategy;
    }
}
