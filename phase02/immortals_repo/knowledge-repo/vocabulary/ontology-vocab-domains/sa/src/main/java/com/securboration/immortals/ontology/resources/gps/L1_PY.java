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
public class L1_PY extends RadioChannel{
    public L1_PY(){
        ChannelHelper.setFrequency(
            this, 
            "L1_PY", 
            1575.42e6,
            SpectrumKeying.MBOC,
            
            HighAccuracyProperty.class,JammerResistantProperty.class,TrustedProperty.class
            );
    }
}
