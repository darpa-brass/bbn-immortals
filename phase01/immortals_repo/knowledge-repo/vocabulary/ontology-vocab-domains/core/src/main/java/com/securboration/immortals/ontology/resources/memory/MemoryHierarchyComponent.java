package com.securboration.immortals.ontology.resources.memory;

/**
 * Memory is typically arranged in a hierarchy. E.g., CPU cache{L1, L2, L3},
 * physical memory, disk.
 * 
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Memory is typically arranged in a hierarchy. E.g., CPU cache{L1, L2," +
    " L3}, physical memory, disk.   @author jstaples ")
public class MemoryHierarchyComponent extends MemoryResource {

    /**
     * 
     * The next memory in the hierarchy, if one exists.
     * 
     * E.g., if this node is L3 cache, the next level memory would be physical
     * memory
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        " The next memory in the hierarchy, if one exists.  E.g., if this" +
        " node is L3 cache, the next level memory would be physical memory")
    private MemoryResource nextLevel;

    public MemoryResource getNextLevel() {
        return nextLevel;
    }

    public void setNextLevel(MemoryResource nextLevel) {
        this.nextLevel = nextLevel;
    }
}
