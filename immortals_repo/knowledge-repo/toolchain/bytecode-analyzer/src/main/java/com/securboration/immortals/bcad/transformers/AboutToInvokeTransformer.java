package com.securboration.immortals.bcad.transformers;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.bcad.instrument.IBytecodeTransformerClass;
import com.securboration.immortals.bcad.instrument.IBytecodeTransformerMethod;
import com.securboration.immortals.bcad.runtime.MessagePrinter;

/**
 * Inserts "about to invoke" messages just before an invoke instruction
 * 
 * @author jstaples
 *
 */
public class AboutToInvokeTransformer implements IBytecodeTransformerClass {
    
    //TODO: handle invokedynamic
    
    private final AnalysisScopeDefinition scope;
    
    public AboutToInvokeTransformer(AnalysisScopeDefinition scope){
        this.scope = scope;
    }
    
    public interface AnalysisScopeDefinition{
        public boolean isInScope(String className);
    }
    
    private static List<MethodInsnNode> getMethodInsnNodes(MethodNode mn){
        List<MethodInsnNode> invokes = new ArrayList<>();
        
        for(AbstractInsnNode i:mn.instructions.toArray()){
            if(i.getType() != AbstractInsnNode.METHOD_INSN){
                continue;
            }
            
            invokes.add((MethodInsnNode)i);
        }
        
        return invokes;
    }

    @Override
    public IBytecodeTransformerMethod acquireMethodTransformer(
            String classHash,
            ClassNode cn
            ) {
        return new IBytecodeTransformerMethod(){

            @Override
            public boolean transformMethod(
                    String classHash, 
                    String methodHash,
                    ClassNode cn, 
                    MethodNode mn
                    ) {
                
                if(mn.instructions == null){
                    return false;
                }
                
                if(mn.instructions.size() == 0){
                    return false;
                }
                
                InsnList instructions = mn.instructions;
                
                List<MethodInsnNode> insertionPoints = getMethodInsnNodes(mn);
                
                //insert "about to invoke" instructions
                for(MethodInsnNode insertionPoint:insertionPoints){
                    final String name = insertionPoint.owner.replace("/",".");
                    if(scope.isInScope(name)){
                        continue;
                    }
                    
                    
                  final String message = String.format(
                      "about to %s %s %s %s", 
                      BytecodePrintHelper.getStringForm(insertionPoint.getOpcode()),
                      insertionPoint.owner,insertionPoint.name,insertionPoint.desc
                      );
                  
                  instructions.insertBefore(
                      insertionPoint, 
                      new LdcInsnNode(message)
                      );
                  instructions.insertBefore(
                      insertionPoint, 
                      new MethodInsnNode(
                          Opcodes.INVOKESTATIC, 
                          Type.getInternalName(MessagePrinter.class), 
                          "print", 
                          Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)),
                          false
                          )
                      );
                }
                
                //insert "returned from invoke" instructions
                for(MethodInsnNode insertionPoint:insertionPoints){
                    final String name = insertionPoint.owner.replace("/",".");
                    if(scope.isInScope(name)){
                        continue;
                    }
                    
                    final String message = String.format(
                        "returned from %s %s %s %s", 
                        BytecodePrintHelper.getStringForm(insertionPoint.getOpcode()),
                        insertionPoint.owner,insertionPoint.name,insertionPoint.desc
                        );
                  
                    instructions.insert(
                        insertionPoint, 
                        new MethodInsnNode(
                            Opcodes.INVOKESTATIC, 
                            Type.getInternalName(MessagePrinter.class), 
                            "print", 
                            Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)),
                            false
                            )
                        );
                    instructions.insert(
                        insertionPoint, 
                        new LdcInsnNode(message)
                        );
                }
                
                
                
                return true;
            }
            
        };
    }
    
    

}
