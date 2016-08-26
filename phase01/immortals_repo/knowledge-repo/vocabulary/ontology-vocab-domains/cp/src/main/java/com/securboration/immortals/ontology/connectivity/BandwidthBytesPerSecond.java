package com.securboration.immortals.ontology.connectivity;

import com.securboration.immortals.ontology.property.Property;

public class BandwidthBytesPerSecond extends Property {
    
    private long bytesPerSecond;

    
    public long getBytesPerSecond() {
        return bytesPerSecond;
    }

    
    public void setBytesPerSecond(long bytesPerSecond) {
        this.bytesPerSecond = bytesPerSecond;
    }


    public BandwidthBytesPerSecond(long bytesPerSecond) {
        super();
        this.bytesPerSecond = bytesPerSecond;
    }

}
