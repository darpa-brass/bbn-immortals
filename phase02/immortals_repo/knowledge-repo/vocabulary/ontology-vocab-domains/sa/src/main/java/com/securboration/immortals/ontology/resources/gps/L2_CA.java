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
public class L2_CA extends RadioChannel{
    public L2_CA(){
        ChannelHelper.setFrequency(
            this, 
            "L2_CA", 
            1227.60e6,
            SpectrumKeying.BPSK
            );
    }
}
