package com.securboration.immortals.instantiation.ast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.securboration.immortals.instantiation.annotationparser.bytecode.Console;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

public class Main {
    
    private static String[] getTestArgs(){
        
        return new String[]{
                "r2.0.0",
                "C:/Users/Securboration/Desktop/code/immortals/trunk/client",
                "./target/classes/ontology/ast",
                "R.,SACommunicationService."
        };
        
    }
    
    private static boolean isFiltered(final String name, final String[] filters){
        
        for(String filter:filters){
            if(name.startsWith(filter)){
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * args[0]: version
     * args[1]: a dir to recursively traverse containing JARs to process
     * args[2]: an output dir
     * 
     * @param args
     * @throws IOException
     * @throws ParseException 
     */
    public static void main(String[] args) throws IOException, ParseException {

//        args = getTestArgs();//TODO
        
        final String version = args[0];
        final File srcDir = new File(args[1]);
        final File outputDir = new File(args[2]);
        final String[] filters = 
                args.length > 3 ? args[3].split(",") : new String[]{};
        
        {
            Console.log("searching %s",srcDir.getAbsolutePath());
            Collection<File> sources = 
                    FileUtils.listFiles(
                        srcDir, 
                        new String[]{"java"}, 
                        true
                        );
            
            for(File source:sources){
                Console.log("found class %s", source.getName());
                
                if(isFiltered(source.getName(),filters)){
                    Console.log("(filtered)");
                    continue;
                }
                
                CompilationUnit c = null;
                
                try(FileInputStream in = new FileInputStream(source)){
                    c = JavaParser.parse(in);
                }
                
                ObjectToTriplesConfiguration config = 
                        new ObjectToTriplesConfiguration(version);
                config.setAddMetadata(false);
                
                final String outputPath = 
                        outputDir + "/" + source.getName() + ".ast.ttl";
                
                Model jarModel = 
                        ObjectToTriples.convert(
                            config, 
                            c
                            );
                
                OntologyHelper.addAutogenerationMetadata(
                    config, 
                    jarModel, 
                    config.getTargetNamespace(), 
                    config.getOutputFile()
                    );
                
                final String serialized = 
                        OntologyHelper.serializeModel(
                            jarModel, 
                            "TTL", 
                            config.isValidateOntology()
                            );
                
//                Console.log(
//                    "bytecode model for jar %s:\n%s\n", 
//                    jar.getName(), 
//                    serialized
//                    );
                
                FileUtils.writeStringToFile(
                    new File(outputPath), 
                    serialized
                    );
            }
        }
    }

}
