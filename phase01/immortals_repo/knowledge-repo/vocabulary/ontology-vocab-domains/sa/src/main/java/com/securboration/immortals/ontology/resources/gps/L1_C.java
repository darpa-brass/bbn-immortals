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
public class L1_C extends RadioChannel{
    public L1_C(){
        ChannelHelper.setFrequency(
            this, 
            "L1_C", 
            1575.42e6,
            SpectrumKeying.MBOC
            );
    }
}
