package com.securboration.immortals.ontology.resources.compute;

import com.securboration.immortals.ontology.resources.ComputeResource;
import com.securboration.immortals.ontology.resources.memory.MemoryResource;

/**
 * Simple abstraction of a general-purpose compute device
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Simple abstraction of a general-purpose compute device  @author" +
    " jstaples ")
public class Cpu extends ComputeResource {

    /**
     * The number of cores actually available for independent execution
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The number of cores actually available for independent execution")
    private int numCoresPhysical;

    /**
     * The number of cores that appear to be available for independent
     * execution.
     * 
     * E.g., hyperthreading can map multiple logical threads onto a single
     * physical compute resource
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The number of cores that appear to be available for independent" +
        " execution.  E.g., hyperthreading can map multiple logical threads" +
        " onto a single physical compute resource")
    private int numCoresLogical;

    /**
     * A model of the memory for this object
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A model of the memory for this object")
    private MemoryResource memoryModel;
    
    @com.securboration.immortals.ontology.annotations.RdfsComment(
            "The instruction set(s) supported by this processor")
    private InstructionSet[] instructionSetArchitectureSupport;

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

    
    public InstructionSet[] getInstructionSetArchitectureSupport() {
        return instructionSetArchitectureSupport;
    }

    
    public void setInstructionSetArchitectureSupport(
            InstructionSet[] instructionSetArchitectureSupport) {
        this.instructionSetArchitectureSupport = instructionSetArchitectureSupport;
    }

}
