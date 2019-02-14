package com.securboration.immortals.repo.model.build;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.securboration.immortals.repo.ontology.FusekiClient;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.repo.api.RepositoryUnsafe;

/**
 * Ingests a binary bytecode artifact
 * 
 * @author jstaples
 *
 */
public class JarIngestor {
    
    private static final Logger logger = 
            LogManager.getLogger(JarIngestor.class);
    
    private JarIngestor(){}
    
    private static void log(String format,Object...args){
        System.out.println(String.format(format, args));//TODO
    }
    
    /**
     * Ingests models contained in a JAR into a graph
     * 
     * @param client 
     *            a preconfigured Fuseki client
     * @param jar
     *            the JAR to ingest
     * @param graphName
     *            the name of the graph to insert triples into
     * @param validSuffixes
     *            the suffixes of models to pull from the jar (e.g., .ttl)
     * @throws IOException
     */
    public static void ingest(
            RepositoryUnsafe client,
            byte[] jar, 
            String namespace,
            String version,
            String graphName,
            String... validSuffixes
            ) throws IOException {
        
        Map<String, byte[]> models = new LinkedHashMap<>();

        JarIngestor.openJar(
                new ByteArrayInputStream(jar),
                Arrays.asList(validSuffixes),
                models
                );
        
        Model aggregateModel = ModelFactory.createDefaultModel();
        
        for(String modelName:models.keySet()){
            byte[] modelBytes = models.get(modelName);
            
            log("about to load model %s (%dB)",modelName,modelBytes.length);
            
            Model model = getModel(modelName,modelBytes);
            
            aggregateModel.add(model);
        }
        
        ObjectToTriplesConfiguration c = 
                new ObjectToTriplesConfiguration(version);
        
        OntologyHelper.addOntologyMetadata(
            c,
            aggregateModel,
            "IMMoRTALS ontology"
            );
        
        log("about to push an aggregate model derived from [%d] sub models into graph [%s]\n",models.size(),graphName);
        
        client.appendToGraph(aggregateModel,graphName);
        
        log("done with push");
        
        log("retrieving graph %s",graphName);
        
        Model retrievedModel = client.getGraph(graphName);
        
        FileUtils.writeStringToFile(
                new File("./"+graphName+".ttl"),
                OntologyHelper.serializeModel(
                    retrievedModel, 
                    "Turtle", 
                    c.isValidateOntology()
                    )
                );
        
        log("done retrieving graph %s",graphName);
    }

    public static int ingest(
            FusekiClient client,
            byte[] jar,
            String namespace,
            String version,
            String graphName,
            String... validSuffixes
    ) throws IOException {

        int triplesAdded = 0;

        Map<String, byte[]> models = new LinkedHashMap<>();

        JarIngestor.openJar(
                new ByteArrayInputStream(jar),
                Arrays.asList(validSuffixes),
                models
        );

        Model aggregateModel = ModelFactory.createDefaultModel();

        for(String modelName:models.keySet()){
            byte[] modelBytes = models.get(modelName);

            log("about to load model %s (%dB)",modelName,modelBytes.length);

            Model model = getModel(modelName,modelBytes);
            triplesAdded+=model.getGraph().size();

            aggregateModel.add(model);
        }

        ObjectToTriplesConfiguration c =
                new ObjectToTriplesConfiguration(version);

        OntologyHelper.addOntologyMetadata(
                c,
                aggregateModel,
                "IMMoRTALS ontology"
        );

        log("about to push an aggregate model derived from [%d] sub models into graph [%s]\n",models.size(),graphName);

        client.addToModel(aggregateModel,graphName);

        log("done with push");

        log("retrieving graph %s",graphName);

        Model retrievedModel = client.getModel(graphName);

        FileUtils.writeStringToFile(
                new File("./"+graphName+".ttl"),
                OntologyHelper.serializeModel(
                        retrievedModel,
                        "Turtle",
                        c.isValidateOntology()
                )
        );

        log("done retrieving graph %s",graphName);

        return triplesAdded;
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

    public static void openJarForSchema(
            InputStream jarWithDependenciesPath,
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
                    if (jarEntry != null && !jarEntry.isDirectory()) {

                        File file = new File(jarEntry.getName());
                        
                        if (file.getParent() != null && file.getParent().equals("ontology" + File.separator + "immortals-vocab")) {
                            System.out.println();

                            final int length = IOUtils.read(inJar, buffer);
                            byte[] jarContent = new byte[length];
                            
                            System.arraycopy(buffer, 0, jarContent, 0, length);
                            models.put(jarEntry.getName(), jarContent);
                        }
                    }
                    if (jarEntry == null) {
                        stop = true;
                    }
            }
        }
    }

    public static void openJar(
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
                            log("found a nested jar: " + jarEntry.getName());
                            
                            //it's a nested jar, recurse
                            openJar(
                                    new ByteArrayInputStream(jarContent),
                                    validSuffixes,
                                    models
                                    );
                        } else if (isValidName(jarEntry.getName(),validSuffixes)) {
                            
                            log("found a model: " + jarEntry.getName());
                            
                            models.put(jarEntry.getName(), jarContent);
                        }
                    }
                }
            }
        }
    }

}
