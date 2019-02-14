package com.securboration.immortals.bcad.dataflow;

import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.bcas.block.BasicBlock;

public class DataflowAnalysisTypes {
    
    public static class DataflowAnalysisContext{
        private final DataflowGraph dfg;
        private final BasicBlock bbg;
        private final StackSimulator simulator;
        private final StackEmulator emulator;
        
        public DataflowAnalysisContext(
                MethodNode mn,
                StackSimulator simulator,
                BasicBlock bbg
                ){
            this.emulator = new StackEmulator(bbg);
            this.simulator = simulator;
            this.bbg = bbg;
            this.dfg = new DataflowGraph(mn);
        }
        
//        public DataflowAnalysisFrame acquireFrame(
//                AbstractInsnNode instruction
//                ){
//            if(instructionsToFrames.containsKey(instruction)){
//                throw new RuntimeException(
//                    "unhandled case: re-visited instruction"
//                    );
//                
////                return instructionsToFrames.get(instruction);
//            }
//            
//            DataflowAnalysisFrame frame = acquireFrameInternal(
//                instruction,
//                bbg
//                );
//            
//            instructionsToFrames.put(instruction, frame);
//            
//            return frame;
//        }
//        
//        private DataflowAnalysisFrame acquireFrameInternal(
//                AbstractInsnNode instruction,
//                BasicBlock bbg
//                ){
//            
//            Set<AbstractInsnNode> predecessors = getPredecessors(
//                instruction,
//                bbg
//                );
//            
//            if(predecessors.size() == 0){
//                //create a new frame
//                DataflowAnalysisFrame frame = new DataflowAnalysisFrame();
//                
//                return frame;
//            } else if(predecessors.size() == 1){
//                DataflowAnalysisFrame frame = 
//                        instructionsToFrames.get(
//                            predecessors.iterator().next()
//                            ).copy();
//                
//                return frame;
//            } else if(predecessors.size() > 1){
//                throw new RuntimeException("unhandled case:multiple predecessors");
//            } else {
//                throw new RuntimeException("unhandled case");
//            }
//            
//        }
        
        private Set<AbstractInsnNode> getPredecessors(
                AbstractInsnNode instruction,
                BasicBlock bbg
                ){
            Set<AbstractInsnNode> predecessors = new LinkedHashSet<>();
            
            final BasicBlock b = bbg.getBlock(instruction);
            
            if(!b.isFirstInBlock(instruction)){
                predecessors.add(instruction.getPrevious());
                return predecessors;
            }
            
            for(BasicBlock p:b.getPredecessors()){
                predecessors.add(p.getTail());
            }
            
            return predecessors;
        }

        
        public DataflowGraph getDfg() {
            return dfg;
        }

        
        public StackEmulator getEmulator() {
            return emulator;
        }
        
//        public FrameEntry createStackFrameEntry(
//                AbstractInsnNode instruction,
//                Type type
//                ){
//            return new FrameEntry(instruction,type);
//        }
    }
    
//    public static class DataflowAnalysisFrame{
//        
//        private final List<FrameEntry> stack = new ArrayList<>();
//        private final List<FrameEntry> locals = new ArrayList<>();
//        
//        public DataflowAnalysisFrame() {}
//        
//        public DataflowAnalysisFrame copy(){
//            DataflowAnalysisFrame copy = new DataflowAnalysisFrame();
//            
//            copy.stack.addAll(this.stack);
//            copy.locals.addAll(this.locals);
//            
//            return copy;
//        }
//        
//        public FrameEntry pop(){
//            final int size = stack.size();
//            
//            return stack.remove(size-1);
//        }
//        
//        public void push(FrameEntry e){
//            stack.add(e);
//        }
//        
//    }
//    
//    public static class FrameEntry{
//        
//        private final AbstractInsnNode producerInstruction;
//        private final Type entryType;
//        
//        public FrameEntry(
//                AbstractInsnNode producerInstruction, 
//                Type entryType
//                ) {
//            super();
//            this.producerInstruction = producerInstruction;
//            this.entryType = entryType;
//        }
//    }
    
//    public static class Action{
//        
//        
//        
//    }
//    
//    public static enum Verb{
//        PRODUCES,
//        CONSUMES
//        ;
//    }
//    
//    public static class Location{
//        
//    }

}
