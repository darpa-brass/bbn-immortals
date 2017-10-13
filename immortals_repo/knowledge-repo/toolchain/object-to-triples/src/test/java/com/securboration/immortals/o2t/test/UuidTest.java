package com.securboration.immortals.o2t.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import com.securboration.immortals.o2t.ObjectTranslatorImpl;
import com.securboration.immortals.o2t.analysis.ObjectNode;
import com.securboration.immortals.o2t.analysis.ObjectPrinter;
import com.securboration.immortals.o2t.analysis.ObjectUriGenerator;
import com.securboration.immortals.o2t.test.UuidTest.BytecodeAnalysisTests.BytecodeAnalysisTest1;
import com.securboration.immortals.o2t.test.UuidTest.BytecodeAnalysisTests.BytecodeAnalysisTest2;
import com.securboration.immortals.o2t.test.UuidTest.BytecodeAnalysisTests.BytecodeAnalysisTest3;

public class UuidTest {
    
    @Test
    public void testObjectToTriplesMaps() throws IOException, IllegalArgumentException, IllegalAccessException {
        
        final ObjectUriGenerator g = 
                new ObjectUriGenerator();
        
        final Object o1 = new O1(new O2("key","value",new byte[]{0x0A,0x0B,0x0C,0x0D},new int[]{1,2,3,4,5,6,7,8,9,0}));
        final Object o2 = new O1(new O2("key","value",new byte[]{0x0A,0x0B,0x0C,0x0D},new int[]{1,2,3,4,5,6,7,8,9,0}));
        final Object o3 = new O1(new O2("kex","value",new byte[]{0x0A,0x0B,0x0C,0x0D},new int[]{1,2,3,4,5,6,7,8,9,0}));
        final Object o4 = new O1(new O3("key","value",new byte[]{0x0A,0x0B,0x0C,0x0D},new int[]{1,2,3,4,5,6,7,8,9,0}));
        final Object o5 = new O1(new O2("kex","value",new byte[]{0x0A,0x0C,0x0B,0x0D},new int[]{1,2,3,4,5,6,7,8,9,0}));
        final Object o6 = new O1(new O2("key","value",new byte[]{0x0A,0x0B,0x0C,0x0D},new int[]{1,2,3,4,5,6,7,8,9,0}));
        final O1     o7 = new O1(new O2("key","value",new byte[]{0x0A,0x0B,0x0C,0x0D},new int[]{1,2,3,4,5,6,7,8,9,0}));{
            o7.otherField = Arrays.asList("1",'2',3);
        }
        final O1     o8 = new O1(new O2("key","value",new byte[]{0x0A,0x0B,0x0C,0x0D},new int[]{1,2,3,4,5,6,7,8,9,0}));{
            o8.otherField = Arrays.asList("1",'2',3);
        }
        final O1     o9 = new O1(new O2("key","value",new byte[]{0x0A,0x0B,0x0C,0x0D},new int[]{1,2,3,4,5,6,7,8,9,0}));{
            o9.otherField = Arrays.asList("1",'2',3f);
        }
        
        assertObjectHashes(g,o1,o1,true);//sanity check
        assertObjectHashes(g,o1,o2,true);//sanity check
        assertObjectHashes(g,o1,o3,false);//because the value of a field is different
        assertObjectHashes(g,o1,o4,false);//because the type of a field is different
        assertObjectHashes(g,o1,o5,false);//because array elements are out-of-order
        assertObjectHashes(g,o1,o6,true);//sanity check
        assertObjectHashes(g,o1,o7,false);//because o7 defines an extra field
        assertObjectHashes(g,o7,o8,true);//sanity check
        assertObjectHashes(g,o7,o9,false);//because o9 uses a float
        
        testBytecodeModels(g,BytecodeAnalysisTest1.class);
        testBytecodeModels(g,BytecodeAnalysisTest2.class);
        testBytecodeModels(g,BytecodeAnalysisTest3.class);
        
        System.out.println(String.class.getClass());
        System.out.println(InputStream.class.getClass().equals(Class.class));
    }
    
    private static void testBytecodeModels(ObjectUriGenerator g,Class<?> c) throws IOException, IllegalArgumentException, IllegalAccessException{
        assertObjectHashes(
            g,
            getTestClassModel(c),
            getTestClassModel(c),
            true
            );//sanity check
    }
    
    private static ClassNode getTestClassModel(Class<?> c) throws IOException{
        byte[] bytecode = getTestBytecode(c);
        
        ClassReader cr = new ClassReader(bytecode);
        ClassNode cn = new ClassNode();

        cr.accept(cn, 0);// 0 = Don't expand frames or compute stack/local
                         // mappings
        
        return cn;
    }
    
    private static byte[] getTestBytecode(Class<?> c) throws IOException{
        InputStream bytecodeStream = 
                c.getClassLoader().getResourceAsStream(
                    c.getName().replace(".", "/") + ".class"
                    );
        ByteArrayOutputStream bytecode = new ByteArrayOutputStream();
        IOUtils.copy(bytecodeStream, bytecode);
        
        return bytecode.toByteArray();
    }
    
    private static String getUuid(ObjectUriGenerator uriGenerator,Object o) throws IllegalArgumentException, IllegalAccessException{
        return uriGenerator.generateUuid(o);
    }
    
    private static void print(Object o) throws IllegalArgumentException, IllegalAccessException{
        ObjectTranslatorImpl translator = new ObjectTranslatorImpl();
        ObjectNode node = ObjectNode.build(translator, o);
        
        node.accept(ObjectPrinter.getPrinterVisitor());
    }
    
    private static void printDividerStart(String tag){
        System.out.println();
        for(int i=0;i<10;i++){
            System.out.printf("[%s] ",tag);
        }
        System.out.println();
    }
    
    private static void printDividerEnd(String tag){
        System.out.println();
        for(int i=0;i<10;i++){
            System.out.printf("[%s]^",tag);
        }
        System.out.println();
    }
    
    private static void assertObjectHashes(
            ObjectUriGenerator generator,
            Object o1,
            Object o2,
            boolean expectHashesEqual
            ) throws IllegalArgumentException, IllegalAccessException {
        
        final String expectedState = expectHashesEqual?"equal":"not equal";
        
        System.out.printf(
            "comparing hashes of %s and %s (expect %s)\n",
            o1,
            o2,
            expectedState
            );
        printDividerStart("o1");
        print(o1);
        printDividerEnd("o1");
        printDividerStart("o2");
        print(o2);
        printDividerEnd("o2");
        
        final String o1Hash = getUuid(generator,o1);
        final String o2Hash = getUuid(generator,o2);
        
        System.out.printf("hash(o1)=%s\nhash(o2)=%s\n", o1Hash, o2Hash);
        
        boolean shouldBeTrue = 
                expectHashesEqual ? o1Hash.equals(o2Hash) : !o1Hash.equals(o2Hash);
                
        Assert.assertTrue(
            String.format("hash(o1)=%s and hash(o2)=%s but expected %s",o1Hash,o2Hash,expectedState),
            shouldBeTrue
            );
    }
    
    
    /**
     * <b>WARNING</b>:don't touch this. it may appear unused but is actually
     * consumed during getTestBytecode
     * 
     * @author jstaples
     *
     */
    public static class BytecodeAnalysisTests{

        public static class BytecodeAnalysisTest1 {
            
        }
        
        public static class BytecodeAnalysisTest2 {

            private int x;

            private String y;

            private double z;
        }
        
        public static class BytecodeAnalysisTest3 extends BytecodeAnalysisTest2 {

            private short q;
            
            public void f(int x){
                if(x % 5 == 0){
                    return;
                }
                
                System.out.println(x);
            }
        }
    }
    
    private static class O1{
        private final O2 field;
        private Object otherField;

        public O1(O2 field) {
            super();
            this.field = field;
        }
    }
    
    private static class O2{
        private final String k;
        private final String v;
        private final byte[] binaryData;
        private final int[] numericalArray;
        public O2(String k, String v, byte[] binaryData, int[] numericalArray) {
            super();
            this.k = k;
            this.v = v;
            
            this.binaryData = binaryData;
            this.numericalArray = numericalArray;
        }
    }
    
    private static class O3 extends O2{

        public O3(String k, String v, byte[] binaryData, int[] numericalArray) {
            super(k, v, binaryData, numericalArray);
        }
        
    }
    
    
    
}
