package com.securboration.immortals.bcad.instrument;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Simple transformer that prints "entered method ..." whenever a method is
 * called
 * 
 * @author jstaples
 *
 */
public class HelloWorldTransformer implements IBytecodeTransformerClass {
    
    public static void print(String s){
        System.out.println(s);
    }

    @Override
    public boolean shouldFilterByName(String hash, String className) {
        return false;
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
                    ClassNode methodOwner, 
                    MethodNode method
                    ) {
                
                if(method.instructions == null){
                    return false;
                }
                
                if(method.instructions.size() == 0){
                    return false;
                }
                
                System.out.printf("transforming %s %s %s\n", methodOwner.name, method.name, method.desc);
                
                InsnList instructions = method.instructions;
                
                AbstractInsnNode insertionPoint = instructions.getFirst();
                
                instructions.insertBefore(
                    insertionPoint, 
                    new LdcInsnNode("$ HELLO $>  entered method " + methodOwner.name + " " + method.name + " " + method.desc + " " + classHash)
                    );
                instructions.insertBefore(
                    insertionPoint, 
                    new MethodInsnNode(
                        Opcodes.INVOKESTATIC, 
                        Type.getInternalName(HelloWorldTransformer.class), 
                        "print", 
                        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)),
                        false
                        )
                    );
                
                return true;
            }

            @Override
            public boolean shouldTransform(
                    String classHash, 
                    String methodHash,
                    String className, 
                    String methodName, 
                    String methodDesc
                    ) {
                return true;
            }
            
        };
    }

    @Override
    public boolean transformClass(String classHash, ClassNode cn) {
        return false;
    }
    
    

}
