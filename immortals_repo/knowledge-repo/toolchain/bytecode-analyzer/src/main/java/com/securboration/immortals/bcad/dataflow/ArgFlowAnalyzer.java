package com.securboration.immortals.bcad.dataflow;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.bcad.dataflow.DataflowHelper.ParameterSpec;
import com.securboration.immortals.bcas.block.BasicBlock;

public class ArgFlowAnalyzer {
    
    private final BasicBlock bbg;
    private final MethodNode mn;
    private final Method methodDefinition;
    
    public ArgFlowAnalyzer(
            Method m,
            MethodNode mn,
            BasicBlock bbg
            ) {
        this.methodDefinition = m;
        this.bbg = bbg;
        this.mn = mn;
        
        analyze();
    }
    
    private static LabelNode[] getScope(Parameter p, MethodNode m){
        
        if(m.localVariables == null){
            return null;
        }
        
        for(LocalVariableNode l:m.localVariables){
            if(l.name.equals(p.getName())){
                
                return new LabelNode[]{
                        l.start,
                        l.end
                };
            }
        }
        
        return null;
    }
    
    private void analyze(){
        ParameterSpec[] spec = DataflowHelper.getArgumentTypes(methodDefinition);
    }

}
