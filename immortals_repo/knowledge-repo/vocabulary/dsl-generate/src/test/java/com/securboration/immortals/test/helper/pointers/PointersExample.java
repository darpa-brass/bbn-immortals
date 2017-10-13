package com.securboration.immortals.test.helper.pointers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import com.securboration.immortals.helpers.ImmortalsPointerHelper;

public class PointersExample {
    
    public static void main(String[] args) throws IOException{
        
        final byte[] bytecode = 
                getThisClassBytes();
        
        {
            System.out.printf("illustrating the creation of pointers:\n");
            
            System.out.printf(
                "\ta pointer to f1 using wildcards: %s\n", 
                ImmortalsPointerHelper.pointerForMethod(
                    bytecode, 
                    "f1", 
                    (String[])null
                    )
                );
            
            System.out.printf(
                "\ta fully qualified pointer to f1: %s\n", 
                ImmortalsPointerHelper.pointerForMethod(
                    bytecode, 
                    "f1", 
                    "int"
                    )
                );
            
            System.out.printf(
                "\ta fully qualified pointer to f1: %s\n", 
                ImmortalsPointerHelper.pointerForMethod(
                    bytecode, 
                    "f1", 
                    "int","float","long","double","boolean","java.lang.String","int[][][][][]"
                    )
                );
            
            System.out.printf(
                "\ta pointer to getAsmModelOfClass using wildcards: %s\n", 
                ImmortalsPointerHelper.pointerForMethod(
                    bytecode, 
                    "getAsmModelOfClass", 
                    (String[])null
                    )
                );
            
            try{
                ImmortalsPointerHelper.pointerForMethod(
                    bytecode, 
                    "q", 
                    (String[])null
                    );
            } catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        
    }
    
    private static byte[] getThisClassBytes() throws IOException {
        final String thisClassName = 
                PointersExample.class.getName().replace(".", "/")+".class";
        
        final InputStream thisClassStream = 
                PointersExample.class.getClassLoader().getResourceAsStream(
                    thisClassName
                    );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.copy(thisClassStream, os);
        
        return os.toByteArray();
    }
    
    private static ClassNode getAsmModelOfClass(final byte[] bytecode) throws IOException {
        
        ClassReader cr = new ClassReader(new ByteArrayInputStream(bytecode));
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        
        return cn;
    }
    
    private static void f0(String x){}
    
    private static void f1(
            int x
            ){};
    
    private static void f1(
            int x,
            float y ,
            long z,
            double p ,
            boolean q, 
            String s, 
            int[][][][][] r
            ){};

}
