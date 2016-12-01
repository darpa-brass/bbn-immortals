package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.resources.gps.SpectrumKeying;

/**
 * A radio communication channel
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A radio communication channel  @author jstaples ")
public class RadioChannel extends CommunicationChannel {
    
    /**
     * The name of a channel
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The name of a channel")
    private String channelName;
    
    /**
     * The minimum frequency of a channel
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The minimum frequency of a channel")
    private double minFrequencyHertz;
    
    /**
     * The center frequency of a radio channel
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The center frequency of a radio channel")
    private double centerFrequencyHertz;
    
    /**
     * The max frequency of a radio channel
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The max frequency of a radio channel")
    private double maxFrequencyHertz;
    
    /**
     * The modulation strategy used on a radio channel (e.g., amplitude vs phase 
     * modulation)
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The modulation strategy used on a radio channel (e.g., amplitude" +
        " vs phase  modulation)")
    private SpectrumKeying modulationStrategy;
    
    /**
     * The properties of a radio channel
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The properties of a radio channel")
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
