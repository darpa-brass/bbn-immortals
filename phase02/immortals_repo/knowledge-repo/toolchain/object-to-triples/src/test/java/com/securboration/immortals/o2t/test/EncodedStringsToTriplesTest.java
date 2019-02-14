package com.securboration.immortals.o2t.test;

import java.io.IOException;
import java.util.Base64;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

public class EncodedStringsToTriplesTest {
    
    @Test
    public void testObjectToTriplesLists() throws IOException{
        
        System.out.println(
            Base64.getEncoder().encodeToString(
                "\u0000\u0001\u0002\u0006".getBytes()
                )
            );
        
        testObjectToTriples(
            "nice object",
            new NiceObject()
            );
        
        testObjectToTriples(
            "nasty object",
            new TestObjectContainingNestedList()
            );
    }
    
    private static class NiceObject{
        private String field1 = "0006";
    }
    
    /*
     * Escape out X character
     * 
     * Scheme:
     *  X -> $ctrl_000X$
     * $ctrl_000X$ -> \$ctrl_000X$
     * 
     * Inputs:
     *   abcde
     *   abcXe
     *   XbcdX
     *   QQQQX
     *   XQQQX
     *   
     * X -> $ctrl_000X$
     *  
     */
    private static class TestObjectContainingNestedList{
        private String field1 = "\u0000\u0001\u0002\u0006";
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
                        "RDF/XML",
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
