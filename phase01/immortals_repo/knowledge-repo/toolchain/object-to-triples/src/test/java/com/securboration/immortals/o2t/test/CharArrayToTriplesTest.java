package com.securboration.immortals.o2t.test;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

public class CharArrayToTriplesTest {
    
    @Test
    public void testObjectToTriplesLists() throws IOException, NoSuchFieldException, SecurityException{
        testObjectToTriples(
            "asm Type",
            Type.getType(this.getClass())
            );
        
        testObjectToTriples(
            "object containing bytes",
            new TestObjectContainingBytes()
            );
        
        testObjectToTriples(
            "nested array",
            new TestNestedArray()
            );
        
        testObjectToTriples(
            "heavily nested array",
            new TestNestedArray3d()
            );
    }
    
    private static class TestObjectContainingBytes{
        private byte[] bytes = new byte[]{-128,0,127};
    }
    
    private static class TestNestedArray{
        private Object[] test = {
                new int[] {1,2,3},
                new byte[] {1,2,3},
                new char[] {1,2,3}
        };
    }
    
    private static class TestNestedArray3d{
        private Object[] test = {
                new int[] {7,8,9},
                new Object[]{
                        new byte[] {4,5,6},
                        new char[] {'a','b','c'}
                        },
                new char[] {'d','e',0}
        };
        
        private Object test2 = 9999;
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
    
    private static void testObjectToTriples(
            String tag,
            Object o
            ) throws IOException, NoSuchFieldException, SecurityException{
        
        final ObjectToTriplesConfiguration config = getConfig();
        
        config.getIgnoredFields().add(TestNestedArray3d.class.getDeclaredField("test2"));
        
        printDividerStart(tag);
        
        Model m = 
                ObjectToTriples.convert(config, o);
        
        System.out.println(
                OntologyHelper.serializeModel(
                        m, 
                        "Turtle",
                        false
                        ));
        
        printDividerEnd(tag);
    }
    
    private static ObjectToTriplesConfiguration getConfig(){
        ObjectToTriplesConfiguration c = new ObjectToTriplesConfiguration("r1.0.0");
        
        c.setTargetNamespace("http://securboration.com/immortals/ontology/r1.0.0/test");
        
        return c;
    }
    
    
    
    
    
}
