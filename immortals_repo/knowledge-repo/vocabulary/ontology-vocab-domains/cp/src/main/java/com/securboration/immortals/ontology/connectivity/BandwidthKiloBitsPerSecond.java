package com.securboration.immortals.ontology.connectivity;

import com.securboration.immortals.ontology.property.Property;

public class BandwidthKiloBitsPerSecond extends Property {
    
    private long kiloBytesPerSecond;

    
    public long getKiloBytesPerSecond() {
        return kiloBytesPerSecond;
    }
    
    public BandwidthKiloBitsPerSecond(){}

    
    public void setKiloBytesPerSecond(long kiloBytesPerSecond) {
        this.kiloBytesPerSecond = kiloBytesPerSecond;
    }


    public BandwidthKiloBitsPerSecond(long kiloBytesPerSecond) {
        super();
        this.kiloBytesPerSecond = kiloBytesPerSecond;
    }

}
