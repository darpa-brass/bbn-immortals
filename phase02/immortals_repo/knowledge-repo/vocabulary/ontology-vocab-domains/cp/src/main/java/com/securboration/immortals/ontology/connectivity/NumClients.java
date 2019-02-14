package com.securboration.immortals.ontology.connectivity;

import com.securboration.immortals.ontology.property.Property;

public class NumClients extends Property {
    
    private int numClients;

    
    public int getNumClients() {
        return numClients;
    }

    
    public void setNumClients(int maxClients) {
        this.numClients = maxClients;
    }


    public NumClients(int maxClients) {
        super();
        this.numClients = maxClients;
    }

}
