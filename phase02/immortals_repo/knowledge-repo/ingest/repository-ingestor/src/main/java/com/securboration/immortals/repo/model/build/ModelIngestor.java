package com.securboration.immortals.repo.model.build;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.securboration.immortals.repo.api.RepositoryUnsafe;

/**
 * Ingests a binary bytecode artifact
 * 
 * @author jstaples
 *
 */
public class ModelIngestor {
    
    private static final Logger logger = 
            LogManager.getLogger(ModelIngestor.class);
    
    private ModelIngestor(){}
    
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
     * @throws IOException
     */
    public static void ingest(
            RepositoryUnsafe client,
            Map<String,InputStream> models, 
            String graphName
            ) throws IOException {
        
        Model aggregateModel = ModelFactory.createDefaultModel();
        
        for(String modelName:models.keySet()){
            InputStream modelStream = models.get(modelName);
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            IOUtils.copy(modelStream, o);
            
            byte[] modelBytes = o.toByteArray();
            
            log("about to load model %s (%dB)",modelName,modelBytes.length);
            
            Model model = getModel(modelName,modelBytes);
            
            aggregateModel.add(model);
        }
        
        log("about to push an aggregate model derived from [%d] sub models into graph [%s]\n",models.size(),graphName);
        
        client.pushGraph(aggregateModel,graphName);
        
        log("done with push");
        
        log("done retrieving graph %s",graphName);
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

}
