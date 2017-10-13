package com.securboration.immortals.bcad.dataflow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.securboration.immortals.bcad.dataflow.DataflowHelper.LocalVariableSpec;
import com.securboration.immortals.bcad.dataflow.DataflowHelper.ParameterSpec;
import com.securboration.immortals.bcad.transformers.helpers.OpcodeHelper;
import com.securboration.immortals.bcas.block.BasicBlock;
import com.securboration.immortals.instantiation.bytecode.printing.MethodPrinter;

public class LocalVars {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalVars.class);
    
    private final MethodNode mn;
    private final BasicBlock bbg;
    private final ReachabilityAnalysis reachability;
    private final List<LocalVariableSpec> localSpecs = new ArrayList<>();
    
    public LocalVariableSpec getParameterSpec(int offset){
        for(LocalVariableSpec l:localSpecs){
            if(l.getLocalIndex() == offset && l.isArgument()){
                return l;
            }
        }
        
//        throw new RuntimeException("no local variable table entry found for local index " + offset);
        
        return null;
    }
    
    public LocalVariableSpec getLocal(VarInsnNode instruction){
        return getLocal(instruction.var,instruction);
    }
    
    public LocalVariableSpec getLocal(IincInsnNode instruction){
        return getLocal(instruction.var,instruction);
    }
    
    private List<LocalVariableSpec> getCandidatesByIndex(
            final int index
            ){
        List<LocalVariableSpec> candidates = new ArrayList<>();
        for(LocalVariableSpec l:localSpecs){
            if(l.getLocalIndex() != index){
                continue;
            }
            
            candidates.add(l);
        }
        return candidates;
    }
    
    private List<LocalVariableSpec> getCandidatesByScope(
            final AbstractInsnNode instruction,
            final int index,
            List<LocalVariableSpec> initialCandidates
            ){
        List<LocalVariableSpec> candidates = new ArrayList<>();
        
//        System.out.printf(
//            "winnowing %d candidates for %d @i%d: %s\n", 
//            initialCandidates.size(), 
//            initialCandidates.get(0).getLocalIndex(),
//            mn.instructions.indexOf(instruction),
//            MethodPrinter.print(mn,instruction)
//            );//TODO
        
        for(LocalVariableSpec candidate:initialCandidates){
            if(isLocalInScope(instruction,index,candidate)){
                candidates.add(candidate);
            }
        }
        
        return candidates;
    }
    
//    private AbstractInsnNode getNextLabel(AbstractInsnNode start){
//        
//        List<AbstractInsnNode> instructions = 
//                bbg.getBlock(start).getBlockInstructions();
//        for(int i=instructions.indexOf(start);i<instructions.size();i++){
//            AbstractInsnNode instruction = instructions.get(i);
//            
//            if(instruction.getType() == AbstractInsnNode.LABEL){
//                return instruction;
//            }
//        }
//        
//        throw new RuntimeException(
//            "no successor label found for i" + mn.instructions.indexOf(start) + 
//            ": " + MethodPrinter.print(mn,start)
//            );
//    }
//    
//    private boolean isLocalInScope2(
//            final AbstractInsnNode instruction,
//            final int index, 
//            LocalVariableSpec candidate
//            ){
//        return reachability.isWaypoint(
//            candidate.getScopeBegin(), 
//            getNextLabel(instruction), 
//            candidate.getScopeEnd()
//            );
//    }
    

    
    private AbstractInsnNode getNextLabel(AbstractInsnNode start){
        
        AbstractInsnNode i = start;
        boolean stop = false;
        while(!stop){
            if(i.getType() == AbstractInsnNode.LABEL){
                return i;
            } else {
                i = i.getNext();
                
                if(i == null){
                    stop = true;
                }
            }
        }
        
        throw new RuntimeException(
            "no successor label found for i" + mn.instructions.indexOf(start) + 
            ": " + MethodPrinter.print(mn,start)
            );
    }
    
    private boolean isLocalInScope(
            final AbstractInsnNode instruction,
            final int index, 
            LocalVariableSpec candidate
            ){
        final AbstractInsnNode start = candidate.getScopeBegin();
        final AbstractInsnNode end = candidate.getScopeEnd();
        final AbstractInsnNode label = getNextLabel(instruction);
        
        final int instructionIndex = mn.instructions.indexOf(label);
        final int beginIndex = mn.instructions.indexOf(start);
        final int endIndex = mn.instructions.indexOf(end);
        
        final boolean afterBegin = instructionIndex >= beginIndex;
        final boolean beforeEnd = instructionIndex <= endIndex;
        
        if(false){
            System.out.printf("next label @ %d\n", mn.instructions.indexOf(label));
            
            System.out.printf(
                "[%d,%d] aka [%s, %s] ? %s %s\n", 
                beginIndex,endIndex,
                MethodPrinter.print(mn,start),
                MethodPrinter.print(mn,end),
                afterBegin && beforeEnd ? "pass":"fail",
                reachability.isWaypoint(start, label, instruction)
    //            ,isLocalInScope2(instruction,index,candidate)
                );//TODO
        }
        
        return afterBegin && beforeEnd;
    }
    
    private LocalVariableSpec getLocal(
            final int index, 
            final AbstractInsnNode instruction
            ){
        List<LocalVariableSpec> candidates = getCandidatesByIndex(index);
        
        if(candidates.size() == 1){
//            System.out.printf("var %d at instruction%d holds %s\n", index, mn.instructions.indexOf(instruction), candidates.get(0));//TODO
            return candidates.get(0);
        }
        
        candidates = getCandidatesByScope(instruction,index,candidates);
        
        if(candidates.size() == 1){
//            System.out.printf("var %d at instruction%d holds %s\n", index, mn.instructions.indexOf(instruction), candidates.get(0));//TODO
            return candidates.get(0);
        }
        
        return null;
    }
    
//    private LocalVariableSpec getLocal(
//            final int index, 
//            final AbstractInsnNode instruction
//            ){
//        List<LocalVariableSpec> initialCandidates = getCandidatesByIndex(index);
//        List<LocalVariableSpec> candidates = new ArrayList<>(initialCandidates);
//        
//        if(initialCandidates.size() > 1){
//            candidates.clear();
//            candidates.addAll(
//                getCandidatesByScope(instruction,index,initialCandidates)
//                );
//        }
//        
//        if(candidates.size() > 1){
//            throw new RuntimeException(
//                "need to use further winnow down the " + 
//                candidates.size() + 
//                " candidates for @" + index + 
//                " at i" + mn.instructions.indexOf(instruction) + ": " + 
//                MethodPrinter.print(mn,instruction)
//                );
//        }
//        
//        if(candidates.size() == 0){
////            int localIndex, String variableName,
////            String typeDesc, Class<?> semanticType, LabelNode scopeBegin,
////            LabelNode scopeEnd, boolean isArgument
//            
//            //TODO: something weird is happening with tableswitch/ASM
//            
//            final String message = 
//                    "found " + candidates.size() + 
//                    " after winnowing (" + initialCandidates.size() + 
//                    " before winnowing) " + 
//                    " and expected exactly 1 @ " + index +
//                    " at i" + mn.instructions.indexOf(instruction) + ": " + 
//                    MethodPrinter.print(mn,instruction);
//            
//            logger.warn(message);
//            
//            if(initialCandidates.size() > 0){
//                LocalVariableSpec candidate = initialCandidates.get(0);
//                logger.warn("falling back to " + ((Object)candidate).toString());
//                
//                return initialCandidates.get(0);
//            }
//            
//            logger.warn("introducing a new virtual local variable");
//            LocalVariableSpec spec = new LocalVariableSpec(
//                index,
//                "unknown",
//                null,
//                null,
//                mn.instructions.getFirst(),
//                mn.instructions.getLast(),
//                false
//                );
//            
//            localSpecs.add(spec);
//            
//            return spec;
//        }
//        
//        if(candidates.size() != 1){
//            throw new RuntimeException(
//                "found " + candidates.size() + 
//                " but expected exactly 1 @ " + index +
//                " at i" + mn.instructions.indexOf(instruction) + ": " + 
//                MethodPrinter.print(mn,instruction)
//                );
//        }
//        
//        System.out.printf("using %s for i%d: %s\n", candidates.get(0),mn.instructions.indexOf(instruction),MethodPrinter.print(mn,instruction));
//        
//        return candidates.get(0);
//    }


    public static LocalVars analyze(
            Method m,
            MethodNode mn,
            BasicBlock bbg,
            ReachabilityAnalysis reachability
            ){
        return new LocalVars(mn,bbg,reachability,getParameterVars(m,mn));
    }
    
    public boolean isLocalMappingComplete(){
        
        {//check parameters
            int index = 0;
            if(!OpcodeHelper.isAnyFlagSet(mn.access, Opcodes.ACC_STATIC)){
                LocalVariableSpec spec = getParameterSpec(index);
                
                if(spec == null){
                    return false;
                }
                
                index++;
            }
            
            for(Type argType:Type.getMethodType(mn.desc).getArgumentTypes()){
                LocalVariableSpec spec = getParameterSpec(index);
                
                if(spec == null){
                    return false;
                }
                
                index += argType.getSize();
            }
        }
        
        //check var instructions
        for(AbstractInsnNode i:mn.instructions.toArray()){
            
            if(i.getType() == AbstractInsnNode.VAR_INSN){
                VarInsnNode v = (VarInsnNode)i;
                
                LocalVariableSpec spec = getLocal(v);
                
                if(spec == null){
                    return false;
                }
            } else if(i.getType() == AbstractInsnNode.IINC_INSN){
                IincInsnNode v = (IincInsnNode)i;
                
                LocalVariableSpec spec = getLocal(v);
                
                if(spec == null){
                    return false;
                }
            }
        }
        
        return true;
    }

    private LocalVars(
            MethodNode mn, 
            BasicBlock bbg,
            ReachabilityAnalysis reachability,
            LocalVariableSpec[] localSpecs
            ) {
        super();
        this.mn = mn;
        this.bbg = bbg;
        this.reachability = reachability;
        this.localSpecs.addAll(Arrays.asList(localSpecs));
        
        if(false){
            for(LocalVariableSpec local:localSpecs){
                System.out.printf("\t%s\n", local.print(mn));//TODO
            }
        }
    }
    
    private static LocalVariableSpec[] getParameterVars(Method m,MethodNode mn){
        
        Map<Integer,ParameterSpec> parameterMapping = new HashMap<>();
        {
            //get the stack offset, which is 1 for non-static methods
            int currentOffset = (mn.access & Opcodes.ACC_STATIC) > 0 ? 0 : 1;
            for(ParameterSpec param:DataflowHelper.getArgumentTypes(m)){
                parameterMapping.put(currentOffset, param);
                
                currentOffset += Type.getType(param.getJavaTypeDesc()).getSize();
            }
        }
        
        List<LocalVariableSpec> locals = new ArrayList<>();
        
        for(LocalVariableNode local:mn.localVariables){
            ParameterSpec correspondingParameterSpec = 
                    parameterMapping.get(local.index);
            
            LocalVariableSpec localSpec = new LocalVariableSpec(
                local.index,
                local.name,
                local.desc,
                correspondingParameterSpec == null ? null : correspondingParameterSpec.getSemanticType(),
                local.start,
                local.end,
                local.name.equals("this") || correspondingParameterSpec != null
                );
            
            locals.add(localSpec);
        }
        
        return locals.toArray(new LocalVariableSpec[]{});
    }

}
