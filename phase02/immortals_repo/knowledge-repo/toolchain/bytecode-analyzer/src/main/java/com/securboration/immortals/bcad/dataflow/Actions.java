package com.securboration.immortals.bcad.dataflow;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;

public class Actions {
    
    private final AbstractInsnNode instruction;
    
    private final List<Action> actionsPerformedByInstruction = new ArrayList<>();
    
    public Actions(AbstractInsnNode instruction){
        this.instruction = instruction;
    }
    
    public AbstractInsnNode getBlock() {
        return instruction;
    }

    
    public AbstractInsnNode getInstruction() {
        return instruction;
    }

    
    public List<Action> getActionsPerformedByInstruction() {
        return actionsPerformedByInstruction;
    }

}
