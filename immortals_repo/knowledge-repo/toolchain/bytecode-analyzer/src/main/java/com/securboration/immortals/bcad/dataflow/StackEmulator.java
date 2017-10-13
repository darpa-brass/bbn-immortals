package com.securboration.immortals.bcad.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import com.securboration.immortals.bcad.dataflow.DataflowHelper.LocalVariableSpec;
import com.securboration.immortals.bcad.dataflow.value.JavaValue;
import com.securboration.immortals.bcas.block.BasicBlock;

public class StackEmulator {
    
    private final BasicBlock bbg;
    private final boolean useSingleEmulator;
    
    public StackEmulator(BasicBlock bbg){
        this(bbg,false);
    }
    
    public StackEmulator(BasicBlock bbg, final boolean useSingleEmulator){
        this.bbg = bbg;
        this.useSingleEmulator = useSingleEmulator;
    }
    
    
    
    public static class Entry{
        private final int id;
        private final AbstractInsnNode producerInstruction;
        private final Type type;
        
        private final JavaValue value;
        
        public Entry cloneWithNewType(Type newType){
            return new Entry(id,producerInstruction,newType,value);
        }
        
        private Entry(
                int id, 
                AbstractInsnNode producerInstruction,
                Type type,
                JavaValue value
                ) {
            this.id = id;
            this.producerInstruction = producerInstruction;
            this.type = type;
            this.value = value;
        }
        
        @Override
        public String toString(){
            return String.format(
                "%d producer=%s type=%s", id, producerInstruction, type
                );
        }

        
        public int getId() {
            return id;
        }

        
        public AbstractInsnNode getProducerInstruction() {
            return producerInstruction;
        }

        
        public Type getType() {
            return type;
        }

        
        public JavaValue getValue() {
            return value;
        }
        
    }
    
    private final List<Entry> currentStack = new ArrayList<>();
    
    private final Map<Integer,JavaValue> currentLocals = new HashMap<>();
    
    private AtomicInteger stackCounter = new AtomicInteger(0);
    
    private Map<AbstractInsnNode,List<Entry>> stackStatesAfter = new HashMap<>();
    
    private Map<AbstractInsnNode,StackEmulator> emulators = new HashMap<>();
    
    private final Map<LocalVariableSpec,JavaValue> parametersToInitialValues = new HashMap<>();
    
    /**
     * 
     * @return a full copy of this object that can be safely modified
     */
    private StackEmulator branch(){
        if(useSingleEmulator){
            return this;
        }
        
        {
            System.out.println("======================== branch ===================");//TODO
            System.out.printf("copying %d stack entries\n", this.currentStack.size());
            System.out.printf("copying %d locals\n", this.currentLocals.size());
            for(int l:currentLocals.keySet()){
                System.out.printf("\t%s -> %s\n", l, currentLocals.get(l));//TODO
            }
        }//branching needs to be forward-oriented rather than retrospective
        
        StackEmulator copy = new StackEmulator(this.bbg);
        
        copy.currentStack.addAll(this.currentStack);
        
        copy.stackCounter = this.stackCounter;
        copy.stackStatesAfter = this.stackStatesAfter;
        copy.emulators = this.emulators;
        copy.currentLocals.putAll(this.currentLocals);
        copy.parametersToInitialValues.putAll(this.parametersToInitialValues);
        
        return copy;
    }
    
    private StackEmulator acquire(AbstractInsnNode instruction){
        if(useSingleEmulator){
            return this;
        }
        
        if(emulators.containsKey(instruction)){
            return emulators.get(instruction);
        }
        
        BasicBlock block = bbg.getBlock(instruction);
        
        if(block == null){
            return this;
        }
        
        if(block.isFirstInBlock(instruction)){
            
            final boolean isEntrypoint = 
                    (block.getPredecessors().size() == 0) 
                    || (
                    block == block.getRoot())
                    ;
            
            if(isEntrypoint){
                //entry point
                emulators.put(instruction, this);
                return this;
            }
            
            Set<BasicBlock> predecessors = block.getPredecessors();
            
            //TODO: there can be multiple possible matches, we should make 
            // sure they are consistent
            
            //it's the start of a non-entry basic block
            List<StackEmulator> branches = new ArrayList<>();
            for(BasicBlock predecessor:predecessors){
                AbstractInsnNode predecessorTail = predecessor.getTail();
                
                if(emulators.containsKey(predecessorTail)){
                    StackEmulator predecessorEmulator = 
                            emulators.get(predecessorTail);
                    
                    branches.add(predecessorEmulator.branch());
                }
            }
            
            if(branches.size() > 0){
                //TODO: merge them together?
                StackEmulator branch = branches.get(0);
                emulators.put(instruction, branch);
                
                System.out.printf("found %d predecessors and %d emulators\n", predecessors.size(), branches.size());//TODO
                
                return branch;
            }
            
            
            throw new RuntimeException(
                "visited instructions in incorrect order, traversal should " +
                "follow control flow"
                );
        }
        
        AbstractInsnNode predecessor = instruction.getPrevious();
        
        if(!emulators.containsKey(predecessor)){
            throw new RuntimeException(
                "instructions visited in incorrect order, traversal should " +
                "follow control flow"
                );
        }
        
        StackEmulator emulator = emulators.get(predecessor);
        
        emulators.put(instruction, emulator);
        
        return emulator;
    }
    
    public Entry peek(AbstractInsnNode instruction){
        final int size = acquire(instruction).currentStack.size();
        
        return acquire(instruction).currentStack.get(size-1);
    }
    
    public Entry produceAndPushOntoStack(
            AbstractInsnNode instruction,
            Type type,
            JavaValue value
            ){
        StackEmulator emulator = acquire(instruction);
        return emulator.produceAndPushOntoStackInternal(
            instruction, 
            type, 
            value
            );
    }
    
    private Entry produceAndPushOntoStackInternal(
            AbstractInsnNode instruction,
            Type type,
            JavaValue value
            ){
        Entry e = new Entry(
            stackCounter.getAndIncrement(),
            instruction,
            type,
            value
            );
        
        currentStack.add(e);
        
        return e;
    }
    
    public void recordState(AbstractInsnNode instruction){
        StackEmulator emulator = acquire(instruction);
        
        emulator.recordStateInternal(instruction);
    }
    
    private void recordStateInternal(AbstractInsnNode instruction){
        List<Entry> stack = new ArrayList<>(currentStack);
        
        stackStatesAfter.put(instruction, stack);
    }
    
    public void pushEntry(AbstractInsnNode instruction,Entry e){
        StackEmulator emulator = acquire(instruction);
        emulator.pushEntryInternal(e);
    }
    
    private void pushEntryInternal(Entry e){
        currentStack.add(e);
    }
    
    public Entry popEntry(AbstractInsnNode instruction){
        StackEmulator emulator = acquire(instruction);
        return emulator.popEntryInternal();
    }
    
    private Entry popEntryInternal(){
        return currentStack.remove(currentStack.size()-1);
    }
    
    public Map<AbstractInsnNode, List<Entry>> getStackStatesAfter() {
        return stackStatesAfter;
    }
    
    public void storeLocal(
            final int local, 
            final JavaValue value
            ){
//        System.out.printf("storing value \"%s\" in \"%s\"\n", value, local);//TODO
        
        currentLocals.put(local, value);
    }
    
    public JavaValue getLocal(final int local){
//        System.out.printf("value in \"%s\" is \"%s\"\n", local, currentLocals.get(local));//TODO
        
        return currentLocals.get(local);
    }

    
    public Map<LocalVariableSpec, JavaValue> getParametersToInitialValues() {
        return parametersToInitialValues;
    }

}
