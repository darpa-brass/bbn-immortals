package com.securboration.immortals.ontology.resources.memory;

/**
 * The physical memory of a system. I.e., the actual installed memory hardware.
 * 
 * 
 * @author Securboration
 *
 */
public class PhysicalMemoryResource extends MemoryResource {
    
    /**
     * The kind of memory this is
     */
    private MemoryType memoryType;

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(MemoryType memoryType) {
        this.memoryType = memoryType;
    }

}
