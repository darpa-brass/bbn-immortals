package com.securboration.immortals.bcad.dataflow;

import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import com.securboration.immortals.bcas.block.BasicBlock;
import com.securboration.immortals.bcas.printer.MethodPrinter;

public class JimpleTransformer {
    
    public static void verify(
            ClassLoader cl,
            ClassNode cn,
            MethodNode mn
            ) throws AnalyzerException{
        BasicBlock b = BasicBlock.decompose(mn);
        
        InsnList instructions = mn.instructions;
        
        StackSimulator simulator = new StackSimulator(cl,cn,mn);
        
        System.out.printf("%s %s %s contains %d instructions\n", cn.name, mn.name, mn.desc, instructions.size());
        for(int i=0;i<instructions.size();i++){
            final AbstractInsnNode instruction = instructions.get(i);
            
            {//print frame before
                Frame<BasicValue> frame = simulator.getFrameBefore(instruction);
                
                if(frame != null){
                    System.out.printf("  @stack  (%d):", frame.getStackSize());
                    for(int j=0;j<frame.getStackSize();j++){
                        BasicValue v = frame.getStack(j);
                        System.out.printf("[%s]",v);
                    }
                    System.out.printf("\n");
                    System.out.printf("  @locals (%d):", frame.getLocals());
                    for(int j=0;j<frame.getLocals();j++){
                        BasicValue v = frame.getLocal(j);
                        System.out.printf("[%s]",v);
                    }
                    System.out.printf("\n");
                }
            }
            
            final BasicBlock block = b.getBlock(instruction);
            
            System.out.printf(
                "    i%-4d (block%4d%s) %s\n", 
                i,
                block == null ? -1 : block.getBlockId(),        
                block == null ? "" : block.isFirstInBlock(instruction) ? "F" : block.isLastInBlock(instruction) ? "L" : " ",
                MethodPrinter.print(mn,instruction)
                );
            
            if(block != null){
                final Set<Integer> predecessors = new TreeSet<>();
                final Set<Integer> successors = new TreeSet<>();
                
                for(BasicBlock p:block.getPredecessors()){
                    predecessors.add(p.getBlockId());
                }
                for(BasicBlock p:block.getSuccessors()){
                    successors.add(p.getBlockId());
                }
                
                if(block.isFirstInBlock(instruction)){
                    System.out.printf(
                        "        predecessors: %s\n",predecessors);
                }
                if(block.isLastInBlock(instruction)){
                    System.out.printf(
                        "        successors: %s\n",predecessors);
                }
            }
            
            {//print frame after
                Frame<BasicValue> frame = simulator.getFrameAfter(instruction);
                
                if(frame != null){
                    System.out.printf("  @stack  (%d):", frame.getStackSize());
                    for(int j=0;j<frame.getStackSize();j++){
                        BasicValue v = frame.getStack(j);
                        System.out.printf("[%s]",v);
                    }
                    System.out.printf("\n");
                    System.out.printf("  @locals (%d):", frame.getLocals());
                    for(int j=0;j<frame.getLocals();j++){
                        BasicValue v = frame.getLocal(j);
                        System.out.printf("[%s]",v);
                    }
                    System.out.printf("\n");
                }
            }
            
            System.out.println();
        }
    }
    
    

}
