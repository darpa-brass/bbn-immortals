package com.securboration.immortals.ontology.resources.compute;

import com.securboration.immortals.ontology.resources.ComputeResource;
import com.securboration.immortals.ontology.resources.memory.MemoryResource;

/**
 * Simple abstraction of a general-purpose compute device
 * 
 * @author Securboration
 *
 */
public class Cpu extends ComputeResource {

    /**
     * The number of cores actually available for independent execution
     */
    private int numCoresPhysical;

    /**
     * The number of cores that appear to be available for independent
     * execution.
     * 
     * E.g., hyperthreading can map multiple logical threads onto a single
     * physical compute resource
     */
    private int numCoresLogical;

    /**
     * A model of the memory for this object
     */
    private MemoryResource memoryModel;

    public int getNumCoresPhysical() {
        return numCoresPhysical;
    }

    public void setNumCoresPhysical(int numCoresPhysical) {
        this.numCoresPhysical = numCoresPhysical;
    }

    public int getNumCoresLogical() {
        return numCoresLogical;
    }

    public void setNumCoresLogical(int numCoresLogical) {
        this.numCoresLogical = numCoresLogical;
    }

    public MemoryResource getMemoryModel() {
        return memoryModel;
    }

    public void setMemoryModel(MemoryResource memoryModel) {
        this.memoryModel = memoryModel;
    }

}
