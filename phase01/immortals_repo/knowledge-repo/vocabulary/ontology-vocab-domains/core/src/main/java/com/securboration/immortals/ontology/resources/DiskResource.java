package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.resources.memory.MemoryResource;

/**
 * A disk-based resource. Note that this may include non-disk devices that
 * follow the disk storage paradigm.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A disk-based resource. Note that this may include non-disk devices" +
    " that follow the disk storage paradigm.  @author jstaples ")
public class DiskResource extends IOResource {

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
