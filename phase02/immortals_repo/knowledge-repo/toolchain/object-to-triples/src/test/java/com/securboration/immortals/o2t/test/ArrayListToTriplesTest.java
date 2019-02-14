package com.securboration.immortals.o2t.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

public class ArrayListToTriplesTest {
    
    @Test
    public void testObjectToTriplesLists() throws IOException{
        testObjectToTriples(
            "simple list",
            new TestObjectContainingList()
            );
        
        testObjectToTriples(
            "nested list",
            new TestObjectContainingNestedList()
            );
    }
    
    private static class TestObjectContainingList{
        private List<Object> list = Arrays.asList("a",'b',new byte[]{7,8,9,0});
    }
    
    private static class TestObjectContainingNestedList{
        private List<Object> list = Arrays.asList("a",'b',Arrays.asList(3,4,"c","d",Arrays.asList("x",'y',"z")));
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
            ) throws IOException{
        
        final ObjectToTriplesConfiguration config = getConfig();
        
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
