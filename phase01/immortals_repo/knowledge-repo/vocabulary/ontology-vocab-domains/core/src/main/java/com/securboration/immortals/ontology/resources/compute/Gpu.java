package com.securboration.immortals.ontology.resources.compute;

import com.securboration.immortals.ontology.resources.ComputeResource;
import com.securboration.immortals.ontology.resources.memory.MemoryResource;

/**
 * Simple abstraction of a specialized compute device for graphics processing.
 * @author Securboration
 *
 */
public class Gpu extends ComputeResource {
    
    /**
     * A model of the memory for this object
     */
    private MemoryResource memoryModel;

    public MemoryResource getMemoryModel() {
        return memoryModel;
    }

    public void setMemoryModel(MemoryResource memoryModel) {
        this.memoryModel = memoryModel;
    }

}
