package com.securboration.immortals.ontology.resources.compute;

import com.securboration.immortals.ontology.resources.ComputeResource;
import com.securboration.immortals.ontology.resources.memory.MemoryResource;

/**
 * Simple abstraction of a specialized compute device for graphics processing.
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Simple abstraction of a specialized compute device for graphics" +
    " processing. @author jstaples ")
public class Gpu extends ComputeResource {
    
    /**
     * A model of the memory for this object
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A model of the memory for this object")
    private MemoryResource memoryModel;

    public MemoryResource getMemoryModel() {
        return memoryModel;
    }

    public void setMemoryModel(MemoryResource memoryModel) {
        this.memoryModel = memoryModel;
    }

}
