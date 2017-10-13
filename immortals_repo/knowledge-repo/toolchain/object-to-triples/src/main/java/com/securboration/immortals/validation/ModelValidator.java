package com.securboration.immortals.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ValidityReport;

public class ModelValidator {
    
    public static void main(String[] args) throws IOException{
        
        Model model = getModel(
            "Turtle",
            
            //vocab
//            "../../vocabulary/ontology-package/target/classes/ontology/immortals-vocab"
            
            "../../vocabulary/ontology-package/target/classes/ontology/immortals-vocab/immortals_bytecode.ttl"
            
            
            //instances
            ,"../../vocabulary/ontology-package/target/classes/ontology/immortals-instances/bytecode/CompressionHelper-1.0-LOCAL.jar.ttl"
            
//            new FileInputStream("../../IMMoRTALS_r2.0.0.ttl")
            );
        
        validateModel(
            model,
            System.out
            );
    }
    
    private static Model getModel(
            String language,
            String...paths
            ) throws IOException {
        
        List<InputStream> streams = new ArrayList<>();
        
        try{
            for(String s:paths){
                File f = new File(s);
                
                if(!f.exists()){
                    System.out.printf(
                        "warning: path %s does not point to a valid file\n", 
                        s
                        );
                    continue;
                }
                
                if(f.isDirectory()){
                    for(File dirFile:FileUtils.listFiles(f, new String[]{"ttl"}, true)){
                        streams.add(new FileInputStream(dirFile));
                    }
                } else if(f.isFile()){
                    streams.add(new FileInputStream(s));
                }
            }
            
            Model m = getModel(language,streams.toArray(new InputStream[]{}));
            
            return m;
        } finally {
            for(InputStream i:streams){
                try {
                    i.close();
                } catch (IOException e) {
                    System.out.printf(
                        "warning: unable to close one or more input streams " +
                        " **this has created a resource leak!**\n");
                }
            }
        }
        
    }
    
    private static Model getModel(String language,InputStream...streams){
        Model model = ModelFactory.createDefaultModel();
        
        System.out.printf("Reading models...\n");
        for(InputStream i:streams){
            model.read(i,null,language);
            
            System.out.printf(
                "\tparsed %d triples from stream\n", 
                model.size()
                );
        }
        
        System.out.printf(
            "Done reading models.  Parsed %d triples from %d streams.\n\n", 
            model.size(),
            streams.length
            );
        
        return model;
    }
    
    private static OntModelSpec getValidationSpec(){
        
        final String spec = System.getProperty("validationSpec");
        
        if(spec != null){
            try {
                return (OntModelSpec)OntModelSpec.class.getField(spec).get(null);
            } catch (IllegalArgumentException | IllegalAccessException
                    | NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        
        return OntModelSpec.OWL_MEM_RDFS_INF;
    }
    
    public static void validateModel(
            Model model,
            PrintStream logStream
            ){
        
        //OWL_LITE_MEM_RULES_INF   //untested
        
        //OWL_MEM_RDFS_INF         //fast (1s for 15k triples)
        //OWL_MEM_MICRO_RULE_INF   //slow (5 mins for 6k triples)
        //OWL_MEM_MINI_RULE_INF    //exceedingly slow (20 mins for 1k triples)
        //OWL_MEM_RULE_INF         //unusably slow (10s for 600 triples)
        
        InfModel infModel = 
                ModelFactory.createOntologyModel(
                    getValidationSpec(), 
                    model
                    );
        
        validateModel(infModel,logStream);
    }
    
    private static void tee(
            StringBuilder sb, 
            PrintStream o, 
            String format, 
            Object...args
            ){
        sb.append(String.format(format, args));
        o.printf(format, args);
    }
    
    private static void validateModel(InfModel m,PrintStream o){
        
        StringBuilder sb = new StringBuilder();
        
        tee(
            sb,
            o,
            "Validating model containing " + m.size() + " triples...\n"
            );
        
        final long startTime = System.currentTimeMillis();
        
        ValidityReport validity = m.validate();
        if (validity.isValid()) {
            tee(
                sb,
                o,
                "\t*** Model is valid! ****\n"
                );
        }  else {
            tee(
                sb,
                o,
                "\t*** Model is *NOT* valid! ****\n"
                );
        }
        
        final long stopTime = System.currentTimeMillis();
        
        final AtomicBoolean foundAnyIssues = new AtomicBoolean(false);
        
        tee(
            sb,
            o,
            "\tReporting any model inconsistencies below:"
            );
        validity.getReports().forEachRemaining(r->{
            tee(sb,o,"\t - " + r + "\n");
            foundAnyIssues.set(true);
        });
        
        tee(
            sb,
            o,
            "\nChecked model validity in ~%ds\n", 
            (stopTime - startTime)/1000
            );
        

        if(foundAnyIssues.get()){
            throw new RuntimeException(
                "Model validation has failed! " + sb.toString());
        }
    }

}
