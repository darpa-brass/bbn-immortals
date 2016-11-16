package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.functionality.datatype.DataProperty;

/**
 * The size of a data structure in physical memory
 * 
 * @author Securboration
 *
 */
public class MemoryFootprint extends DataProperty {
    
    private int sizeInBytes;

    
    public int getSizeInBytes() {
        return sizeInBytes;
    }

    
    public void setSizeInBytes(int sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

}
