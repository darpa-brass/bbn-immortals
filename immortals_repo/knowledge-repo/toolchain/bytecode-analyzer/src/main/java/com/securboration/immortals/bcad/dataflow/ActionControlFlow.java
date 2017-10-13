package com.securboration.immortals.bcad.dataflow;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.objectweb.asm.tree.AbstractInsnNode;

import com.securboration.immortals.bcad.dataflow.value.JavaValue;

public class ActionControlFlow extends Action{
    
    private final String uuid;
    private final String instructionString;
    private final List<JavaValue> valuesConsumed;
    private final List<AbstractInsnNode> successors;
    
    @Override
    public String toString() {
        return String.format(
            "CONTROL FLOW %s consumes %d values (%s) and has %d successors (%s)", 
            instructionString, 
            valuesConsumed.size(),valuesConsumed,
            successors.size(),successors
            );
    }

    public ActionControlFlow(
            String instructionString,
            JavaValue[] valuesConsumed,
            AbstractInsnNode[] successors
            ) {
        super();
        this.uuid = UUID.randomUUID().toString();
        this.instructionString = instructionString;
        this.valuesConsumed = Arrays.asList(valuesConsumed);
        this.successors = Arrays.asList(successors);
    }
    

}
