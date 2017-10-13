package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.resources.memory.MemoryResource;

/**
 * A logical volume may span multiple partitions
 * 
 * @author Securboration
 *
 */
public class Volume extends DiskResource {

    /**
     * A model of the memory for this object
     */
    private MemoryResource memoryModel;
    
    /**
     * The partitions the volume spans
     */
    private Partition[] partitionsSpanned;

    @Override
    public MemoryResource getMemoryModel() {
        return memoryModel;
    }

    @Override
    public void setMemoryModel(MemoryResource memoryModel) {
        this.memoryModel = memoryModel;
    }

    public Partition[] getPartitionsSpanned() {
        return partitionsSpanned;
    }

    public void setPartitionsSpanned(Partition[] partitionsSpanned) {
        this.partitionsSpanned = partitionsSpanned;
    }

}
