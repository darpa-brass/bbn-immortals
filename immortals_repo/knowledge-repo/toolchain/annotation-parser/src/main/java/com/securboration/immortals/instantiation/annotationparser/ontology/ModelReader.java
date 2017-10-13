package com.securboration.immortals.instantiation.annotationparser.ontology;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.securboration.immortals.instantiation.annotationparser.bytecode.Console;

public class ModelReader {

    public static Model getAggregateModel(File modelDir) throws IOException{
        
        Console.log(
            "loading aggregate model from %s", 
            modelDir.getAbsolutePath()
            );
        
        Model model = ModelFactory.createDefaultModel();
        
        Collection<File> models = 
                FileUtils.listFiles(
                    modelDir, 
                    new String[]{"ttl"}, 
                    true
                    );
        
        for(File f:models){
            Console.log(
                "\tfound model %s", 
                f.getName()
                );
            model.add(getModel(FileUtils.readFileToByteArray(f)));
        }
        
        return model;
    }
    
    private static Model getModel(
            byte[] modelBytes
            ) throws IOException{
        
        Model model = ModelFactory.createDefaultModel();
        
        model.read(
                new ByteArrayInputStream(modelBytes),
                null,
                "TTL");
        
        return model;
    }
    
}
