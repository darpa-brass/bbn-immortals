package com.securboration.immortals.o2t.test;

import java.io.IOException;
import java.util.Arrays;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;

import com.securboration.immortals.j2t.analysis.JavaToOwl;
import com.securboration.immortals.j2t.analysis.JavaToTriplesConfiguration;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.pojos.markup.PojoProperty;

public class VocabularyTest {
    
    @Test
    public void testObjectToTriplesLists() throws IOException, ClassNotFoundException{
        
        JavaToTriplesConfiguration c = 
                new JavaToTriplesConfiguration("r2.0.0");
        
        JavaToOwl converter = new JavaToOwl(c);
        Model m = converter.analyze(Arrays.asList(Example.class));
        
        System.out.println(OntologyHelper.serializeModel(m, "TURTLE", false));
        
        ObjectToTriplesConfiguration o2t = 
                new ObjectToTriplesConfiguration("r2.0.0");
        
        Example e = new Example();
        e.humanReadableForm = "blah";
        e.numberField = -1234;
        
        Example ee = new Example();
        ee.humanReadableForm = "blah blah blah";
        
        e.exampleee = ee;
        
        System.out.println(
            OntologyHelper.serializeModel(
                ObjectToTriples.convert(o2t, e), 
                "TURTLE", 
                false
                )
            );
    }

    public static class Example implements HumanReadable, HasNumber, HasExample, Comparable<Example>{

        private String humanReadableForm;

        private int numberField;
        
        private Example exampleee;

        @Override
        public String getHumanReadableDesc() {
            return humanReadableForm;
        }

        @Override
        public int hasFeaturedNumberField() {
            return numberField;
        }

        @Override
        public long hasLongForm() {
//            if(humanReadableForm == null){
//                return -1;
//            }
            return numberField;
        }

        @Override
        public Example getExample() {
            return exampleee;
        }

        @Override
        public int compareTo(Example o) {
            throw new RuntimeException("intentional");
        }

    }

    @PojoProperty
    public interface HumanReadable {
        public String getHumanReadableDesc();
    }
    
    @PojoProperty
    public interface HasExample {
        public Example getExample();
    }
    
    @PojoProperty
    public interface HasNumber {
        
        public int hasFeaturedNumberField();
        public long hasLongForm();
        
    }
    
    
    
    
    
}
