package com.securboration.immortals.o2t.test;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

public class ConceptInstanceToTriplesTest {
    
    @Test
    public void testObjectToTriplesLists() throws IOException{
        testObjectToTriples(
            "test concept",
            new TestConceptWrapper()
            );
    }
    
    public static class TestConceptWrapper{
        private TestConcept c1 = new TestConcept();
        private TestConcept c2 = new TestConcept();
        private TestConcept c3 = new TestConcept();
    }
    
    @ConceptInstance
    public static class TestConcept{
        private int x;
        private int y;
        
        public TestConcept(){
            this.x = 1;
            this.y = 2;
        }
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
