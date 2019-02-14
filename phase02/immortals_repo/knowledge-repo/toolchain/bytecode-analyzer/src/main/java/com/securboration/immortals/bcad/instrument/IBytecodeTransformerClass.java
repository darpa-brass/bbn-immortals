package com.securboration.immortals.bcad.instrument;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface IBytecodeTransformerClass {
    
    /**
     * 
     * @param className
     *            the name of a class we possibly want to transform
     * @param hash
     *            the hash of a class we possibly want to transform
     * @return true iff we should transform the indicated class
     */
    public default boolean shouldFilterByName(
            final String hash,
            final String className
            ){
        return false;
    }
    
    public default boolean isSafeToTransform(ClassNode cn){
        if(cn.methods == null){
            return true;
        }
        
        if((cn.access & Opcodes.ACC_ANNOTATION) > 0){
            //don't touch annotations
            return false;
        }
        
        //TODO: ignore exceptions and other corner cases
        
        for(MethodNode mn:cn.methods){
            if (mn.instructions == null) {
                continue;
            }

            for (AbstractInsnNode i : mn.instructions.toArray()) {
                if (i.getOpcode() == Opcodes.JSR) {
                    return false;
                } else if (i.getOpcode() == Opcodes.RET) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 
     * @param classHash
     * @param cn
     * @return a method transformer for modifying the entries in the method
     */
    public default IBytecodeTransformerMethod acquireMethodTransformer(
            final String classHash,
            ClassNode cn
            ){
        return new IBytecodeTransformerMethod(){

            @Override
            public boolean transformMethod(String classHash, String methodHash,
                    ClassNode methodOwner, MethodNode method) {
                return false;
            }
            
        };
    }
    
    /**
     * 
     * @param classHash
     * @param cn
     * @return true iff the class bytecode was modified by this call
     */
    public default boolean transformClass(
            final String classHash,
            ClassNode cn
            ){
        return false;
    }

}
