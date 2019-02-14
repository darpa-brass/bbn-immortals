package com.securboration.immortals.pojoapi.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.securboration.immortals.helpers.ImmortalsPointerHelper;
import com.securboration.immortals.ontology.measurement.CodeUnitPointer;

public class ExamplePointerHelper {
    
    public static CodeUnitPointer getPointer(
            Class<?> c,
            String methodName
            ) throws IOException{
        
        CodeUnitPointer p = new CodeUnitPointer();
        
        p.setClassName(c.getName());
        p.setMethodName(methodName);
        p.setPointerString(ImmortalsPointerHelper.pointerForMethod(
            getClassBytes(c), 
            methodName, 
            (String[])null)
            );
        
        return p;
    }
    
    /**
     * In real analysis, these would be retrieved from a JAR or directly from
     * the file system. In this test, we retrieve the bytes from the
     * classloader.
     * 
     * @param c
     * @return
     * @throws IOException
     */
    private static byte[] getClassBytes(Class<?> c) throws IOException {
        final String thisClassName = 
                c.getName().replace(".", "/")+".class";
        
        final InputStream thisClassStream = 
                c.getClassLoader().getResourceAsStream(
                    thisClassName
                    );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.copy(thisClassStream, os);
        
        return os.toByteArray();
    }

}
