package com.securboration.immortals.instantiation.annotationparser.bytecode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.instantiation.bytecode.printing.MethodPrinter;

public class BytecodeHelper {
    
    public static String hash(byte[] data){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ClassNode getClassNode(byte[] bytecode){
        ClassReader cr = new ClassReader(bytecode);
        ClassNode cn = new ClassNode();

        cr.accept(cn, 0);// 0 = Don't expand frames or compute stack/local
                         // mappings
        
        return cn;
    }
    
    public static String getMethodBytecode(MethodNode mn){
        
        if(mn.instructions == null){
            return null;
        } else if(mn.instructions.size() == 0){
            return null;
        }
        
        return MethodPrinter.print(mn);
    }
    
    public static MethodNode getDeclaredMethod(
            ClassNode cn, 
            String methodName, 
            String methodDesc
            ){
        List<MethodNode> methods = new ArrayList<>();
        if(cn.methods != null){
            methods.addAll(cn.methods);
        }
        
        for(MethodNode mn:methods){
            if(mn.name.equals(methodName) && mn.desc.equals(methodDesc)){
                return mn;
            }
        }
        
        throw new RuntimeException(
            "no method found in " + cn.name + " with name " + methodName + 
            " and desc " + methodDesc
            );
    }
    
    @SuppressWarnings("unchecked")//erasure :/
    public static <T> List<T> getInstructionsOfType(
            Collection<AbstractInsnNode> instructions, 
            final int sort
            ){
        List<T> matches = new ArrayList<>();
        
        for(AbstractInsnNode i:instructions){
            if(i.getType() == sort){
                matches.add((T)i);
            }
        }
        
        return matches;
    }
    
    

}
