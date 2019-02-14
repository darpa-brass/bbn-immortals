package com.securboration.immortals.ontology.resources.gps;

import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.resources.RadioChannel;
import com.securboration.immortals.ontology.resources.gps.SpectrumKeying;

/**
 * Models a GPS receiver
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class L5 extends RadioChannel{
    public L5(){
        ChannelHelper.setFrequency(
            this, 
            "L5", 
            1176.45e6,
            SpectrumKeying.BPSK
            );
    }
}
