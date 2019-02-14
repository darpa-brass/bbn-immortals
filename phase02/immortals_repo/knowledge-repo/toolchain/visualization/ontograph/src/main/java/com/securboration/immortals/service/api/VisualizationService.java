package com.securboration.immortals.service.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.service.vis.VowlGenerator;

/**
 * Service interface containing simple test methods
 * 
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/immortalsVisualizationService")
public class VisualizationService {

    /**
     * 
     * @return
     * @throws Exception 
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/convert",
            produces=MediaType.APPLICATION_JSON_VALUE
            )
    public String owl2vowl(
            @RequestBody
            String ttlContent
            ) throws Exception {
        
        //TODO: read TTL content from jar
        {
            System.out.println(this.getClass().getClassLoader().getResourceAsStream(
                "ontology/immortals-ontologies-package-r2.0.0.jar"
                ));
        }
        
        return VowlGenerator.generate(
            ttlContent, 
            "TURTLE", 
            "http://darpa.mil/immortals/ontology/r2.0.0"
            );
    }
    
    /**
     * 
     * @return
     * @throws Exception 
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/ttlJson",
            produces=MediaType.APPLICATION_JSON_VALUE
            )
    public String getTtlJson(
            @RequestParam("ttlDoc")
            String ttlDocName
            ) throws Exception {
        
        if(ttlDocName.startsWith("data/")){
            ttlDocName = ttlDocName.substring(5);
        }
        
        if(ttlDocName.endsWith(".json")){
            ttlDocName = ttlDocName.replace(".json", ".ttl");
        }
        
        InputStream jarInStream = 
                this.getClass().getClassLoader().getResourceAsStream(
                    "ontology/immortals-ontologies-package-r2.0.0.jar"
                    );
        
        ByteArrayOutputStream jarOutStream = new ByteArrayOutputStream();
        
        IOUtils.copy(jarInStream, jarOutStream);
        
        byte[] jarBytes = jarOutStream.toByteArray();
        
        String ttlContent = 
                JarTtlScanner.getTtlModel(
                    jarBytes,
                    ttlDocName
                    );
        
        {
            //TODO: weird bug with typed literals
            ttlContent = ttlContent.replace("^^<xsd:string>", "");
        }
        
        return VowlGenerator.generate(
            ttlContent, 
            "TURTLE", 
            "http://darpa.mil/immortals/ontology/r2.0.0"
            );
    }
    
    private static class JarTtlScanner {
        
        private JarTtlScanner(){}
        
        /**
         * 
         * @param jar
         * @param ttlFiles
         * @return
         * @throws IOException
         */
        public static String getTtlModel(
                byte[] jar, 
                String...ttlFiles
                ) throws IOException {
            
            Map<String, byte[]> models = new LinkedHashMap<>();

            JarTtlScanner.openJar(
                    new ByteArrayInputStream(jar),
                    Arrays.asList("ttl"),
                    models
                    );
            
            Model aggregateModel = ModelFactory.createDefaultModel();
            
            boolean matchedAny = false;
            for(String ttlFileName:models.keySet()){
                boolean match = false;
                
                for(String ttlFile:ttlFiles){
                    if(ttlFileName.endsWith(ttlFile)){
                        match = true;
                        break;
                    }
                    
                    if(ttlFileName.endsWith(ttlFile.replace("-", "_"))){
                        match = true;
                        break;
                    }
                }
                
                if(match){
                    matchedAny = true;
                    byte[] modelBytes = models.get(ttlFileName);
                    Model model = getModel(ttlFileName,modelBytes);
                    aggregateModel.add(model);
                }
            }
            
            if(!matchedAny){
                throw new RuntimeException(
                    "no matches for " + Arrays.asList(ttlFiles) + 
                    ", valid keys are: " + models.keySet()
                    );
            }
            
//            for(String ttlFile:ttlFiles){
//                if(!models.containsKey(ttlFile)){
//                    throw new RuntimeException(
//                        "no key \"" + ttlFile + 
//                        "\", valid keys are: " + models.keySet()
//                        );
//                }
//                
//                byte[] modelBytes = models.get(ttlFile);
//                Model model = getModel(ttlFile,modelBytes);
//                aggregateModel.add(model);
//            }
            
            return OntologyHelper.serializeModel(
                aggregateModel, 
                "Turtle", 
                false
                );
        }
        
        private static String getLanguage(String modelName){
            if(modelName.endsWith(".ttl")){
                return "TTL";
            }
            
            throw new RuntimeException(
                    "no language found for model name " + modelName);
        }
        
        private static Model getModel(
                final String modelName,
                byte[] modelBytes
                ) throws IOException{
            
            Model model = ModelFactory.createDefaultModel();
            
            model.read(
                    new ByteArrayInputStream(modelBytes),
                    null,
                    getLanguage(modelName));
            
            return model;
        }
        
        private static boolean isValidName(String name,Collection<String> validSuffixes){
            
            for(String validSuffix:validSuffixes){
                if(name.endsWith(validSuffix)){
                    return true;
                }
            }
            
            return false;
        }

        private static void openJar(
                InputStream jarWithDependenciesPath,
                Collection<String> validSuffixes,
                Map<String,byte[]> models
                ) throws IOException{
            
            try(JarArchiveInputStream inJar = new JarArchiveInputStream(jarWithDependenciesPath);)
            {
                final int MAX_SIZE = 
                        1024*1024//1MB
                        *64;
                
                byte[] buffer = new byte[MAX_SIZE];
        
                // extract everything from the jar
                boolean stop = false;
                while (!stop) {
                    JarArchiveEntry jarEntry = inJar.getNextJarEntry();
        
                    if (jarEntry == null) {
                        stop = true;
                    } else {
                        if (jarEntry.getSize() > MAX_SIZE) {
                            throw new RuntimeException("jar entry too large, > " + MAX_SIZE);
                        } else if (jarEntry.getSize() == 0) {
                            // do nothing, the entry is not a file
                        } else {
                            final int length = IOUtils.read(inJar, buffer);
                            byte[] jarContent = new byte[length];
        
                            System.arraycopy(buffer, 0, jarContent, 0, length);
        
                            if(jarEntry.getName().endsWith(".jar")) {
                                //it's a nested jar, recurse
                                openJar(
                                        new ByteArrayInputStream(jarContent),
                                        validSuffixes,
                                        models
                                        );
                            } else if (isValidName(jarEntry.getName(),validSuffixes)) {
                                models.put(jarEntry.getName(), jarContent);
                            }
                        }
                    }
                }
            }
        }
    }

}
