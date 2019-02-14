package com.securboration.immortals.j2t.aggregator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.securboration.immortals.o2t.ontology.OntologyHelper;

/**
 * Used to combine multiple triple files into a single uber file
 * 
 * 
 * @author jstaples
 *
 */
public class VocabularyAggregator {
    
    public static void main(String[] args) throws IOException{
        if(args.length < 2){
            throw new IllegalArgumentException(
                "arg1*:   language (e.g., TURTLE)\n" +
                "arg2*:   output file path (e.g., aggregated.ttl)\n" +
                "arg3...: paths to ontology files or dirs containing ontology files\n"
                );
        }
        
        //arg1: language
        //arg2: output file
        //arg2...N 
        
        Model model = ModelFactory.createDefaultModel();
        
        final String lang = args[0];
        final File outputPath = new File(args[1]);
        
        for(int i=2;i<args.length;i++){
            addToModel(lang,model,new File(args[i]));
        }
        
        final String s = OntologyHelper.serializeModel(model, lang, false);
        
        FileUtils.writeStringToFile(outputPath, s);
    }
    
    private static void addToModel(
            final String lang,
            final Model current, 
            final File f
            ) throws IOException{
        
        List<File> files = new ArrayList<>();
        if(f.isDirectory()){
            files.addAll(FileUtils.listFiles(f, null, true));
        } else {
            files.add(f);
        }
        
        for(File file:files){
            current.read(
                new ByteArrayInputStream(FileUtils.readFileToByteArray(file)), 
                null, 
                lang
                );
        }
    }

}
