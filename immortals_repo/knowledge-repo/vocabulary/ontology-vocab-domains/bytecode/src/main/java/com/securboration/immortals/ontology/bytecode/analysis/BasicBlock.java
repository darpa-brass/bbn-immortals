package com.securboration.immortals.ontology.bytecode.analysis;

import java.util.ArrayList;
import java.util.List;

import com.securboration.immortals.ontology.core.HumanReadable;
import com.securboration.immortals.ontology.core.Identifiable;

/**
 * A basic block is a control flow abstraction that takes the form of a sequence
 * of sequential instructions within a method body where:
 * <ol>
 * <li>Only the first instruction in a basic block may be the target of a jump
 * </li>
 * <li>Only the last instruction in a basic block may be a jump</li>
 * </ol>
 * 
 * @author jstaples
 *
 */
public class BasicBlock implements HumanReadable, Identifiable {
    
    private String humanReadable;
    
    /**
     * An identity for this block unique within the method from which it was 
     * derived
     */
    private String id;
    
    /**
     * The indices of the instructions in the basic block
     */
    private final List<Integer> instructions = new ArrayList<>();
    
    /**
     * The predecessors of this basic block
     */
    private final List<BasicBlock> predecessors = new ArrayList<>();
    
    /**
     * The successors of this basic block
     */
    private final List<BasicBlock> successors = new ArrayList<>();

    @Override
    public String getHumanReadableDesc() {
        return humanReadable;
    }

    
    public String getHumanReadable() {
        return humanReadable;
    }

    
    public void setHumanReadable(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    
    public List<Integer> getInstructions() {
        return instructions;
    }

    
    public List<BasicBlock> getPredecessors() {
        return predecessors;
    }

    
    public List<BasicBlock> getSuccessors() {
        return successors;
    }


    
    @Override
    public String getId() {
        return id;
    }


    
    public void setId(String id) {
        this.id = id;
    }

}
