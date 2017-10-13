package com.securboration.immortals.o2t.entrypoint;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;

public class Main {
    
    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException{
        
        ObjectToTriplesConfiguration config = 
                new ObjectToTriplesConfiguration();
        
        
        ObjectToTriples.convert(config,new Test1());
        
//        test(new Test1());
//        
//        test(getTestModel());
//        
//        test(null);
//        
//        test(Arrays.asList("1",2,'3',4d,new Object()));
//        
//        test(new Test4());
        
    }
    
    private static Model getTestModel(){
        org.apache.jena.rdf.model.impl.ModelCom m = 
                (org.apache.jena.rdf.model.impl.ModelCom)ModelFactory.createDefaultModel();
        
//        System.out.printf("%s\n", m.getClass().getName());
        
        Resource r1 = m.getResource("http://securboration.com/immortals#Test1");
        
        Resource r2 = m.getResource("http://securboration.com/immortals#Test2");
        
        r1.addProperty(RDFS.subClassOf,r2);
        r2.addProperty(RDFS.subClassOf,r1);
        
        return m;
    }
    
    private static class Test1{
        String stringField = "test";
        
        Object[] arrayField = {
                1,
                "two",
                "two",
                '3',
                4d,
                new Test3("testObject",new Test2("another test object"))
        };
    }
    
    private static class Test4 extends Test1{
        String anotherField = "anothervalue";
        
        String stringField = "this field overrides parent";
        
        String f = super.stringField;
    }
    
    private static class Test2{
        final String name;
        
        private Test2(String name){
            this.name = name;
        }
    }
    
    private static class Test3{
        
        final String name;
        final Test2 parent;
        final Test2 other;
        final Test3 other2 = null;
        final Object s;
        
        private Test3(String name,Test2 parent){
            this.name = name;
            this.parent = parent;
            this.other = parent;
            this.s = this;
        }
    }

}
