package com.securboration.immortals.bcad.transformers;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.bcad.instrument.IBytecodeTransformerClass;
import com.securboration.immortals.bcad.instrument.IBytecodeTransformerMethod;

/**
 * Inserts "about to invoke" messages just before an invoke instruction
 * 
 * @author jstaples
 *
 */
public class SelectiveTransformer implements IBytecodeTransformerClass {
    
    public static interface ITransformMethod{
        public boolean transformMethod(
                String classHash, 
                String methodHash,
                ClassNode cn, 
                MethodNode mn
                );
    }
    
    private Set<String> prefixes = new HashSet<>();
    private final ITransformMethod transformer;
    
    public SelectiveTransformer(ITransformMethod transformer){
        this.transformer = transformer;
    }
    
    public void addPrefix(final String prefix){
        prefixes.add(prefix);
    }
    
    private boolean isMatch(final String name){
        
        final String dotName = name.replace("/", ".");
        final String slashName = name.replace(".", "/");
        
        for(String prefix:prefixes){
            if(dotName.startsWith(prefix)){
                return true;
            }
            
            if(slashName.startsWith(prefix)){
                return true;
            }
        }
        
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
                    ClassNode cn, 
                    MethodNode mn
                    ) {
                
                final String name = cn.name + " " + mn.name + " " + mn.desc;
                
                if(!isMatch(name)){
                    return false;
                }
                
                return transformer.transformMethod(
                    classHash, 
                    methodHash, 
                    cn, 
                    mn
                    );
            }
            
        };
    }
    
    

}
