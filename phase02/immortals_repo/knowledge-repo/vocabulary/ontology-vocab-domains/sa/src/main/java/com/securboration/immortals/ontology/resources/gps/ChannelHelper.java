package com.securboration.immortals.ontology.resources.gps;

import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.resources.RadioChannel;
import com.securboration.immortals.ontology.resources.gps.SpectrumKeying;
import com.securboration.immortals.ontology.resources.gps.properties.HighAccuracyProperty;
import com.securboration.immortals.ontology.resources.gps.properties.JammerResistantProperty;
import com.securboration.immortals.ontology.resources.gps.properties.TrustedProperty;

@Ignore
public class ChannelHelper {
    
    @SafeVarargs
    public static void setFrequency(
            RadioChannel r,
            String name,
            double center,
            SpectrumKeying k,
            Class<? extends Property>...properties
            ){
//        r.setMinFrequencyHertz(min);
//        r.setMaxFrequencyHertz(max);
//        r.setCenterFrequencyHertz((min+max)/2);
        r.setCenterFrequencyHertz(center);
        
        r.setChannelName(name);
        r.setModulationStrategy(k);
        r.setChannelProperties(properties);
    }
    
    public static Property getAccurateProperty(){
        return new HighAccuracyProperty();
    }
    
    public static Property getTrustedLocationProperty(){
        return new TrustedProperty();
    }
    
    public static Property getJammerResistantProperty(){
        return new JammerResistantProperty();
    }
    
    

}
