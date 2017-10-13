package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.resources.memory.MemoryResource;

/**
 * A logical organizational abstraction for disks
 * 
 * @author Securboration
 *
 */
public class Partition extends DiskResource {

    /**
     * A model of the memory for this object
     */
    private MemoryResource memoryModel;

    @Override
    public MemoryResource getMemoryModel() {
        return memoryModel;
    }

    @Override
    public void setMemoryModel(MemoryResource memoryModel) {
        this.memoryModel = memoryModel;
    }

}
