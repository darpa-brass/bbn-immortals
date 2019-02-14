package com.securboration.immortals.pojoapi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

/**
 * 
 * @author Clayton
 *
 */
public class Triplifier {
    
    
    
    /**
     * Serialize a POJO into triples
     * 
     * @param objectToSerialize
     *            an object from the POJO API
     * @param outputFilePath
     *            the path to an output file where the resultant triples will be
     *            stored (as a Turtle document)
     * @throws IOException
     *             if something goes wrong
     */
    public static void serializeToTriples( 
            Object objectToSerialize,
            String outputFilePath
            ) throws IOException{
        final String serializedForm = process(objectToSerialize);
        
        final File outfile = new File(outputFilePath);
        
        FileUtils.writeStringToFile(outfile, serializedForm);
    }
    
    /**
     * Process an Object in its entirety
     * @param o
     * @return
     * @throws IOException
     */
    private static String process(Object o) throws IOException{
    	return triplify(o);
    }
    
    private static String triplify(Object o) throws IOException{
    	ObjectToTriplesConfiguration o2tc = 
    	        new ObjectToTriplesConfiguration(Config.IMMORTALS_VERSION);
    	
    	Model m = ModelFactory.createDefaultModel();
    	
    	addToModel(o2tc,m,o);
    	
		String rs = OntologyHelper.serializeModel(m, "Turtle", false);
		return rs;
    }
    
    private static void addToModel(
            ObjectToTriplesConfiguration config, 
            Model m, 
            Object o
            ){
        if(o.getClass().isArray()){
            //recurse into naked array entries
            for(int i=0;i<Array.getLength(o);i++){
                addToModel(config,m,Array.get(o, i));
            }
        } else if(o instanceof Collection){
            //recurse into naked Collection entries
            for(Object entry:(Collection<?>)o){
                addToModel(config,m,entry);
            }
        } else {
            //treat it like a non-collection object
            m.add(ObjectToTriples.convert(config, o));
        }
    }
}

