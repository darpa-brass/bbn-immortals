package com.securboration.immortals.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Helper class for generating pointers for classes, fields, and methods
 * 
 * @author jstaples
 *
 */
public class ImmortalsPointerHelper {
    
    private static ClassNode getAsmModelOfClass(
            final byte[] bytecode
            ) throws IOException {
        ClassReader cr = new ClassReader(new ByteArrayInputStream(bytecode));
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        return cn;
    }
    
    /**
     * Answer a pointer to the field with the given name in the given class.
     * Different from {@link #pointerForField(String, String)} in that it
     * actually verifies that the indicated field exists in the class.
     * 
     * @param bytecode
     *            the bytecode of the class containing the field
     * @param fieldName
     *            the name of a field to obtain a pointer to
     * @return the pointer to the first matching field found in the indicated
     *         class
     * @throws IOException
     *             if the class cannot be read from the provided bytecode
     */
    public static String pointerForField(
            byte[] bytecode, 
            String fieldName
            ) throws IOException{
        ClassNode cn = getAsmModelOfClass(bytecode);
        
        List<FieldNode> fields = new ArrayList<>();
        if(cn.fields != null){
            fields.addAll(cn.fields);
        }
        
        StringBuilder sb = new StringBuilder();
        for(FieldNode f:fields){
            if(f.name.equals(fieldName)){
                final String hash = hash(bytecode);
                return pointerForField(hash,f.name);
            }
            
            sb.append(
                String.format(
                    "[%s (a %s)]", 
                    f.name, 
                    f.desc
                    )
                );
        }
        
        throw new RuntimeException(
            "No field found in class " + cn.name + 
            " with name " + fieldName + ".  Did you mean one of: " + sb.toString() + 
            "? Also consider looking in the parent class: " + cn.superName
            );
    }
    
    /**
     * Answer a pointer to the method with the given name and parameter types in
     * the indicated class. Different from
     * {@link #pointerForMethod(String, String, String)} in that it actually
     * verifies that the indicated method exists in the class and does not
     * presuppose that you know the method descriptor <i>a priori</i>.
     * 
     * @param bytecode
     *            the bytecode of the class containing the method
     * @param methodName
     *            the name of the method. E.g., hashCode
     * @param argTypes
     *            the name of each argument type. Null can be used in place of
     *            this array, in which case the first method with the provided
     *            name will be matched. Null can also be provided in place of
     *            any parameter, in which case any parameter type will match at
     *            that position. <br>
     *            <br>
     *            E.g., String[][][] has a name of java.lang.String[][][]
     *            whereas String has a name of java.lang.String.
     * @return a pointer to the first matching method found in the indicated
     *         class
     * @throws IOException
     *             if the provided class bytecode cannot be read
     */
    public static String pointerForMethod(
            byte[] bytecode, 
            String methodName, 
            String...argTypes
            ) throws IOException{
        ClassNode cn = getAsmModelOfClass(bytecode);
        
        List<MethodNode> methods = new ArrayList<>();
        if(cn.methods != null){
            methods.addAll(cn.methods);
        }
        
        StringBuilder sb = new StringBuilder();
        for(MethodNode mn:methods){
            final Type[] mnArgTypes = Type.getArgumentTypes(mn.desc);
            
            {
                sb.append(String.format("\n\t%s: ", mn.name));
                
                if(mnArgTypes != null){
                    for(Type argType:mnArgTypes){
                        sb.append(String.format("%s ", argType.getClassName()));
                    }
                }
            }
            
            boolean match = false;
            
            if(!mn.name.equals(methodName)){
                continue;
            }
            
            if(argTypes == null){
                //null argTypes implies we should match on name only
                
                final String hash = hash(bytecode);
                return pointerForMethod(hash,mn.name,mn.desc);
            }
            
            if(mnArgTypes.length != argTypes.length){
                continue;
            }
            
            for(int i=0;i<mnArgTypes.length;i++){
                String mnType = mnArgTypes[i].getClassName();
                String argType = argTypes[i];
                
                if(argType == null || mnType.equals(argType)){
                    match = true;
                } else {
                    match = false;
                }
            }
            
            if(match){
                final String hash = hash(bytecode);
                return pointerForMethod(hash,mn.name,mn.desc);
            }
        }
        
        throw new RuntimeException(
            "no method found in class " + cn.name + 
            " with name " + methodName + " matches the provided args.  " +
            "Try one of: " + sb.toString() + 
            ". Also consider looking in the parent class: " + cn.superName
            );
    }
    
    
    /**
     * Answer a String that uniquely identifies the indicated class
     * 
     * @param bytecode
     *            the original class bytecode
     * @return a String that uniquely identifies the given class. Any
     *         modification to the class that alters its bytecode will result in
     *         the emission of a different pointer.
     */
    public static String pointerForClass(
            byte[] bytecode
            ){
        return hash(bytecode);
    }
    
    /**
     * Answer a String that uniquely identifies a field within a class
     * 
     * @param ownerClassPointer
     *            a pointer obtained via a previous call to
     *            {@link #pointerForClass(byte[])}
     * @param fieldName
     *            the name of the field within the class
     * @return a String that uniquely identifies the given field. Any
     *         modification to the class that alters its bytecode will result in
     *         the emission of a different pointer.
     */
    public static String pointerForField(
            final String ownerClassPointer, 
            final String fieldName
            ){
        return ownerClassPointer + "/fields/" + fieldName;
    }
    
    /**
     * 
     * @param ownerClassPointer
     *            a pointer obtained via a previous call to
     *            {@link #pointerForClass(byte[])}
     * @param methodName
     *            the name of the method
     * @param methodDesc
     *            the type descriptor (signature) of the method. See
     *            {@link org.objectweb.asm.Type#getMethodDescriptor(java.lang.reflect.Method)}
     *            and
     *            {@link org.objectweb.asm.Type#getMethodDescriptor(org.objectweb.asm.Type, org.objectweb.asm.Type...)}
     *            . E.g., a method with no arguments that returns a String has
     *            descriptor ()Ljava/lang/String;. The recommended mechanism to
     *            retrieve this value is using ASM:
     *            {@link org.objectweb.asm.tree.MethodNode#desc}
     * @return a String that uniquely identifies the given method. Any
     *         modification to the class that alters its bytecode will result in
     *         the emission of a different pointer.
     */
    public static String pointerForMethod(
            final String ownerClassPointer, 
            final String methodName, 
            final String methodDesc
            ){
        return ownerClassPointer + "/methods/" + methodName + methodDesc;
    }
    
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
    
    

}
