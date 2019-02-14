package com.securboration.immortals.bcad.transformers;

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
 * Simple transformer that prints "entered method ..." whenever a method is
 * called
 * 
 * @author jstaples
 *
 */
public class JustInvokedTransformer implements IBytecodeTransformerClass {

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
                    ClassNode methodOwner, 
                    MethodNode method
                    ) {
                
                if(method.instructions == null){
                    return false;
                }
                
                if(method.instructions.size() == 0){
                    return false;
                }
                
                InsnList instructions = method.instructions;
                
                AbstractInsnNode insertionPoint = instructions.getFirst();
                
                final String message = String.format(
                    "just invoked %s %s %s", 
                    methodOwner.name, 
                    method.name, 
                    method.desc
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
                
                return true;
            }
            
        };
    }
    
    

}
