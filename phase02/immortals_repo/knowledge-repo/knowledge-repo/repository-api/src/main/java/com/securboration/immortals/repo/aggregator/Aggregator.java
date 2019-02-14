package com.securboration.immortals.repo.aggregator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.securboration.immortals.ontology.pojos.markup.Ignore;

@Ignore
public class Aggregator {
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        
        Map<String[],String> map = new HashMap<>();
        
        {
            final String base = 
                    "C:/Users/Securboration/Desktop/code/" +
                    "immortals/trunk/knowledge-repo/vocabulary/" +
                    "ontology-generate/target/classes/ontology/"
                    ;
            
            map.put(new String[]{
                    base+"immortals_bytecode.ttl",
                    base+"immortals_core.ttl",
                    base+"immortals_cp.ttl",
                    base+"immortals_sa.ttl",
                }, 
                "target/aggregated/vocabulary.ttl"
                );
            
            map.put(new String[]{
                    base+"immortals_scratchpad.ttl",
                }, 
                "target/aggregated/gme-interchange-example.ttl"
                );
            
            map.put(new String[]{
                    base+"immortals_bytecode.ttl",
                    base+"immortals_core.ttl",
                    base+"immortals_cp.ttl",
                    base+"immortals_sa.ttl",
                    
                    base+"immortals_scratchpad.ttl",
                }, 
                "target/aggregated/gme-interchange-example-with-vocab.ttl"
                );
        }
        
        
        for(String[] set:map.keySet()){
            File outfile = new File(map.get(set));
            
            Model m = aggregate(set);
            
            
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            m.write(o, "TURTLE");
            
            FileUtils.writeStringToFile(outfile, o.toString());
        }
        
        System.out.println("done.");
    }
    
    private static Model aggregate(String[] paths) throws FileNotFoundException, IOException{
        Model m = ModelFactory.createDefaultModel();
        
        for(String path:paths){
            try(FileInputStream f = new FileInputStream(path)){
                m.read(f, null, "TURTLE");
            }
        }
        
        return m;
    }

}
