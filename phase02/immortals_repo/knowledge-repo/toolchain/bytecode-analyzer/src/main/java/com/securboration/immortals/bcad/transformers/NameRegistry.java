package com.securboration.immortals.bcad.transformers;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.bcad.instrument.IBytecodeTransformerClass;
import com.securboration.immortals.bcad.instrument.IBytecodeTransformerMethod;

/**
 * Performs no transformation; instead, registers the hashes of classes and
 * methods so that they can later be easily converted into human readable form
 * 
 * @author jstaples
 *
 */
public class NameRegistry implements IBytecodeTransformerClass {
    
    private final Map<String,String> hashToName = new HashMap<>();

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
                
                hashToName.put(
                    methodHash, 
                    String.format("%s %s %s (hash=%s)", cn.name, mn.name, mn.desc, methodHash)
                    );
                
                return false;
            }
            
        };
    }

    @Override
    public boolean transformClass(
            String classHash, 
            ClassNode cn
            ) {
        hashToName.put(classHash,String.format("%s (hash=%s)",cn.name,classHash));
        
        return false;
    }
    
    public String getHumanReadableForm(String hash){
        if(!hashToName.containsKey(hash)){
            return "??? no human readable form stored for hash " + hash;
            
//            throw new RuntimeException(
//                "no human readable form stored for hash " + hash
//                );
        }
        
        return hashToName.get(hash);
    }
    
    

}
