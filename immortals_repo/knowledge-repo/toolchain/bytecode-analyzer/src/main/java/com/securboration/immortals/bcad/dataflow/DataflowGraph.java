package com.securboration.immortals.bcad.dataflow;

import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.bcas.printer.MethodPrinter;

public class DataflowGraph {
    
    private final MethodNode mn;
    private final Map<AbstractInsnNode,DataflowNode> map = new LinkedHashMap<>();
    
    public DataflowGraph(MethodNode mn){
        this.mn = mn;
    }
    
    public DataflowNode makeNode(AbstractInsnNode instruction){
        if(map.containsKey(instruction)){
            throw new RuntimeException("unhandled case");
        }
        
        DataflowNode n = new DataflowNode(instruction);
        
        map.put(instruction, n);
        
        return n;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        for(AbstractInsnNode instruction:map.keySet()){
            DataflowNode node = map.get(instruction);
            sb.append(
                String.format(
                    "i%d: %s \nhas %d actions:\n", 
                    mn.instructions.indexOf(instruction),
                    MethodPrinter.print(mn,instruction),
                    node.getActions().size()
                    )
                );
            for(Action a:node.getActions()){
                sb.append(String.format("\t%s\n", a.toString()));
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }

}
