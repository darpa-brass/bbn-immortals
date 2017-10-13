package com.securboration.immortals.bcad.dataflow;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;

public class DataflowNode {
    
    private final AbstractInsnNode instruction;
    private final List<Action> actions = new ArrayList<>();
    
    public DataflowNode(AbstractInsnNode instruction) {
        super();
        this.instruction = instruction;
    }

    
    public AbstractInsnNode getInstruction() {
        return instruction;
    }

    
    public List<Action> getActions() {
        return actions;
    }

}
