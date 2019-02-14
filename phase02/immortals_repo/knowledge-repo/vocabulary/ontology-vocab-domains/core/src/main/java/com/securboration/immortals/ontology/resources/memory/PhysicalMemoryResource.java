package com.securboration.immortals.ontology.resources.memory;

/**
 * The physical memory of a system. I.e., the actual installed memory hardware.
 * 
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "The physical memory of a system. I.e., the actual installed memory" +
    " hardware.   @author jstaples ")
public class PhysicalMemoryResource extends MemoryResource {
    
    /**
     * The kind of memory for a memory resource
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The kind of memory for a memory resource")
    private MemoryType memoryType;

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(MemoryType memoryType) {
        this.memoryType = memoryType;
    }

}
