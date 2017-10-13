package com.securboration.immortals.ontology.resources.gps;

import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.resources.RadioChannel;
import com.securboration.immortals.ontology.resources.gps.SpectrumKeying;
import com.securboration.immortals.ontology.resources.gps.properties.HighAccuracyProperty;
import com.securboration.immortals.ontology.resources.gps.properties.JammerResistantProperty;
import com.securboration.immortals.ontology.resources.gps.properties.TrustedProperty;

/**
 * Models a GPS channel
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class L2_PY extends RadioChannel{
    public L2_PY(){
        ChannelHelper.setFrequency(
            this, 
            "L2_PY", 
            1227.60e6,
            SpectrumKeying.BPSK,
            
            HighAccuracyProperty.class,JammerResistantProperty.class,TrustedProperty.class
            );
    }
}
