package com.securboration.immortals.ontology.resources.gps;

import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.resources.RadioChannel;
import com.securboration.immortals.ontology.resources.gps.SpectrumKeying;

/**
 * Models a GPS channel
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class L1_CA extends RadioChannel{
    public L1_CA(){
        ChannelHelper.setFrequency(
            this, 
            "L1_CA", 
            1575.42e6,
            SpectrumKeying.MBOC
            );
    }
}
