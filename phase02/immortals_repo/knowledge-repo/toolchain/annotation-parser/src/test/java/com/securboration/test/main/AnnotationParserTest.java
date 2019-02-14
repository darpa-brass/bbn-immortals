package com.securboration.test.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.securboration.immortals.instantiation.annotationparser.traversal.AnnotationParser;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

public class AnnotationParserTest {
    
    public static void main(String[] args) throws IOException{
        ObjectToTriplesConfiguration c = new ObjectToTriplesConfiguration("r2.0.0");
        AnnotationParser p = new AnnotationParser(c);
        
        p.visitClass("hashCodeGoesHere", getBytecode(Example0_ParserStressTest.class));
        
        Model m = ModelFactory.createDefaultModel();
        for(Object o:c.getMapper().getObjectsToSerialize()){
            m.add(ObjectToTriples.convert(c, o));
        }
        
        System.out.println(OntologyHelper.serializeModel(m, "TURTLE", false));
    }
    
    private static byte[] getBytecode(Class<?> c) throws IOException{
        final String name = "/"+c.getName().replace(".", "/") + ".class";
        try(InputStream is = c.getResourceAsStream(name)){
            if(is == null){
                throw new RuntimeException("couldn't find .class for " + name);
            }
            
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            IOUtils.copy(is, b);
            
            return b.toByteArray();
        }
    }

}
