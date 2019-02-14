package com.securboration.immortals.bcad.dataflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import com.securboration.immortals.bcas.block.BasicBlock;

public class StackSimulator{
        
        private final MethodNode mn;
        
        private final Map<AbstractInsnNode,Frame<BasicValue>> stateBeforeInstruction = new HashMap<>();
        private final Map<AbstractInsnNode,Frame<BasicValue>> stateAfterInstruction = new HashMap<>();
        
        public StackSimulator(ClassLoader cl,ClassNode cn,MethodNode mn) throws AnalyzerException{
            this.mn = mn;
            
            Type superType = 
                    cn.superName == null ? null : Type.getObjectType(cn.superName);
            
            List<Type> interfaces = new ArrayList<Type>();
            for (Iterator<String> i = cn.interfaces.iterator(); i.hasNext();) {
                interfaces.add(Type.getObjectType(i.next()));
            }
            
            SimpleVerifier verifier = new SimpleVerifier(
                Type.getObjectType(cn.name), 
                superType, 
                interfaces,
                (cn.access & Opcodes.ACC_INTERFACE) != 0
                );
            
            verifier.setClassLoader(cl);
            
            Analyzer<BasicValue> analyzer = new Analyzer<>(verifier);
            
            Frame<BasicValue>[] frames = analyzer.analyze(cn.name, mn);
            
            InsnList instructions = mn.instructions;
            
            BasicBlock bbg = BasicBlock.decompose(mn);
            
            //build state before
            for(int i=0;i<instructions.size();i++){
                final AbstractInsnNode instruction = instructions.get(i);
                final Frame<BasicValue> frame = frames[i];
                
                stateBeforeInstruction.put(instruction, frame);
            }
            
            for(int i=0;i<instructions.size();i++){
                final AbstractInsnNode instruction = instructions.get(i);
                
                final Frame<BasicValue> frameAfter = 
                        getFrameAfter(
                            instruction,
                            bbg,
                            stateBeforeInstruction
                            );
                
                stateAfterInstruction.put(instruction, frameAfter);
            }
            
            
            
        }
        
        public Frame<BasicValue> getFrameBefore(AbstractInsnNode i){
            return stateBeforeInstruction.get(i);
        }
        public Frame<BasicValue> getFrameAfter(AbstractInsnNode i){
            return stateAfterInstruction.get(i);
        }
        
        private List<AbstractInsnNode> getSuccessors(
                AbstractInsnNode instruction, 
                BasicBlock bbg
                ){
            BasicBlock block = bbg.getBlock(instruction);
            
            if(block == null){
                return null;
            }
            
            if(!block.isLastInBlock(instruction)){
                return Arrays.asList(instruction.getNext());
            }
            
            List<AbstractInsnNode> instructions = new ArrayList<>();
            
            for(BasicBlock successor:block.getSuccessors()){
                instructions.add(successor.getBlockInstructions().get(0));
            }
            
            return instructions;
        }
        
        private Frame<BasicValue> getFrameAfter(
                final AbstractInsnNode instruction, 
                final BasicBlock bbg, 
                final Map<AbstractInsnNode,Frame<BasicValue>> frames
                ){
            List<Frame<BasicValue>> successorFrames = new ArrayList<>();
            
            List<AbstractInsnNode> successors = getSuccessors(instruction,bbg);
            
            if(successors == null || successors.size() == 0){
                return null;
            }
            
            for(AbstractInsnNode successor:successors){
                successorFrames.add(frames.get(successor));
            }
            
            Set<String> stringForms = new HashSet<>();
            for(Frame<BasicValue> successorFrame:successorFrames){
                stringForms.add(successorFrame.toString());
            }
            
            //TODO
//            if(stringForms.size() > 1){
//                throw new RuntimeException(
//                    "inconsistent frames for successor instructions:" + stringForms + " for instruction " + MethodPrinter.print(mn,instruction)
//                    );
//            } else if(stringForms.size() == 0){
//                throw new RuntimeException(
//                    "no successor frame located for " + 
//                            MethodPrinter.print(mn,instruction)
//                    );
//            }
            
            return successorFrames.get(0);
        }
        
//        public void simulate(){
//            BasicBlock root = BasicBlock.decompose(mn);
//            for(AbstractInsnNode i:mn.instructions.toArray()){
//                root.getSuccessors();
//            }
//            
//        }
        
    }