package com.securboration.immortals.bcad.instrument;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface IBytecodeTransformerMethod {
    
    /**
     * 
     * @param classHash
     * @param methodHash
     * @param className
     * @param methodName
     * @param methodDesc
     * @return true iff the method should be transformed
     */
    public default boolean shouldTransform(
            final String classHash, 
            final String methodHash,
            final String className,
            final String methodName,
            final String methodDesc
            ){
        return true;
    }
    
    public default boolean isSafeToTransform(MethodNode mn){
      //TODO: I believe it is possible to do away with this restriction but this
      //       hypothesis should be tested.
      
      //We ignore any methods with JSR/RET instructions because they aren't
      // supported by ASM's highly useful COMPUTE_FRAMES capability.  These 
      // instructions are typically generated by older Java compilers for
      // finally blocks.  Ultimately we may wish to remove this restriction, but
      // for now there is much more lower hanging fruit.
      
        if (mn.instructions == null) {
            return false;
        }

        if (mn.instructions.size() == 0) {
            return false;
        }

        for (AbstractInsnNode i : mn.instructions.toArray()) {
            if (i.getOpcode() == Opcodes.JSR) {
                return false;
            } else if (i.getOpcode() == Opcodes.RET) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * 
     * @param classHash
     * @param methodHash
     * @param methodOwner
     * @param method
     * @return true iff the owner or method were modified as a result of this
     *         transformation
     */
    public boolean transformMethod(
            final String classHash,
            final String methodHash,
            ClassNode methodOwner,
            MethodNode method
            );

}
