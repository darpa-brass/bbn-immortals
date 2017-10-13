package com.securboration.immortals.bcad.transformers;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.bcad.instrument.IBytecodeTransformerClass;
import com.securboration.immortals.bcad.instrument.IBytecodeTransformerMethod;

/**
 * Simple transformer that adds the hash value of the class as a static constant
 * 
 * @author jstaples
 *
 */
public class AddHashFieldTransformer implements IBytecodeTransformerClass {

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
                return false;
            }
            
        };
    }

    @Override
    public boolean transformClass(String classHash, ClassNode cn) {
        return false;
    }
    
    

}
