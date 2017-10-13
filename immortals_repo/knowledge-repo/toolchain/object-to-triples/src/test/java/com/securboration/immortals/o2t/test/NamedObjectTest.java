package com.securboration.immortals.o2t.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.o2t.test.NamedObjectTest.TestVocabulary.People;
import com.securboration.immortals.o2t.test.NamedObjectTest.TestVocabulary.Person;

public class NamedObjectTest {
    
    @Test
    public void testNaming() throws IOException{
        
        final ObjectToTriplesConfiguration config = getConfig();
        
        Person david = getPerson("david","d");
        
        config.getNamingContext().setNameForObject(david, "anExternalUri");
        
        List<Person> people = Arrays.asList(
            getPerson("alice","a"),
            getPerson("bob","b"),
            getPerson("charles","c"),
            david
            );
        
        testObjectToTriples(
                "people",
                config,
                getPeople(people)
                );
        
        for(Person p:people){
            System.out.printf("%s has uri %s\n", p.firstName, config.getNamingContext().getNameForObject(p));
        }
        
    }
    
    private static Person getPerson(
            String first,
            String last
            ){
        Person person = new Person();
        person.firstName = first;
        person.lastName = last;
        return person;
    }
    
    private static People getPeople(
            List<Person> people
            ){
        People p = new People();
        p.people = people.toArray(new Person[]{});
        return p;
        
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
            final ObjectToTriplesConfiguration config,
            Object o
            ) throws IOException{
        
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
    
    static class TestVocabulary{
        static class Person{
            private String firstName;
            private String lastName;
        }
        
        static class  People{
            private Person[] people;
        }
    }
    
    
    
}
