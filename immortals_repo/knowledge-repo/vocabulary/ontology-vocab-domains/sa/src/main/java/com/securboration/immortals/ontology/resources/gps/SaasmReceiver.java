package com.securboration.immortals.ontology.resources.gps;

import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.resources.gps.GpsReceiver;
import com.securboration.immortals.ontology.resources.gps.GpsSatelliteConstellation;

/**
 * Models a GPS receiver
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class SaasmReceiver extends GpsReceiver{
    public SaasmReceiver(){
        super.setConstellation(getConstellationInstance());
        super.setNumChannels(5);
        super.setReceivableSpectrum(
            new L1_C(),
            new L1_CA(),
            new L1_PY(),
            new L2_C(),
            new L2_CA(),
            new L2_PY(),
            new L5()
            );
    }
    
    private static GpsSatelliteConstellation getConstellationInstance(){
        GpsSatelliteConstellation c = new GpsSatelliteConstellation();
        
        c.setConstellationName("Block IIF");
        
        return c;
    }
}
