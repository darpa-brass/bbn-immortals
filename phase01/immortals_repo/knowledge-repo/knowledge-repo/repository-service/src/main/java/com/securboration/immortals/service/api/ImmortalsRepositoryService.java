package com.securboration.immortals.service.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securboration.immortals.deployment.parser3.Parser;
import com.securboration.immortals.deployment.pojos.DeploymentParser;
import com.securboration.immortals.i2t.ontology.ModelToTriples;
import com.securboration.immortals.i2t.ontology.ModelToTriples.NamingContext;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.repo.api.RepositoryUnsafe;
import com.securboration.immortals.repo.etc.ExceptionWrapper;
import com.securboration.immortals.repo.model.build.JarIngestor;
import com.securboration.immortals.service.config.ImmortalsServiceProperties;

/**
 * An API for the IMMoRTALS repository service
 * 
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/immortalsRepositoryService")
public class ImmortalsRepositoryService {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(ImmortalsRepositoryService.class);
    
    @Autowired(required = true)
    private ImmortalsServiceProperties properties;
    
    @Autowired(required = true)
    private RepositoryUnsafe repository;
    
    @Autowired(required = true)
    private ObjectToTriplesConfiguration o2tc;
    
    /**
     * Pushes a deployment model to the triple store
     * @param json a JSON serialization of a deployment model produced by GME
     * @return a URI for the graph in which the deployment model was saved
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/pushDeploymentModel",
            consumes=MediaType.APPLICATION_JSON_VALUE,
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String pushDeploymentModel(
            @RequestBody
            String json
            ){
        DeploymentParser p = new Parser();
        
        p.parse(json);
        
        Model model = ModelToTriples.convert(
                o2tc, 
                p.getTypes(), 
                p.getInstances(), 
                new NamingContext()
                );
        
        final String graphName = 
                Helper.getImmortalsUuid(properties);
        
        repository.pushGraph(model,graphName);
        
        return graphName;
    }
    
    /**
     * Kicks off the bootstrapping process
     * @return a URI for a graph containing the bootstrapping knowledge
     * @throws IOException 
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/bootstrap",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String bootstrap() throws IOException{
        
        logger.info("bootstrapping...");
        
        InputStream jarStream = 
                this.getClass().getClassLoader().getResourceAsStream(
                    "ontology/immortals-ontologies-package-r2.0.0.jar");
        
        ByteArrayOutputStream jarBytes = new ByteArrayOutputStream();
        IOUtils.copy(jarStream, jarBytes);
        
        final String graphName = 
                Helper.getImmortalsUuid(properties);
        
        JarIngestor.ingest(
            repository, 
            jarBytes.toByteArray(), 
            graphName, 
            ".ttl"
            );
        
        if(logger.isDebugEnabled()){
            logger.debug(
                Helper.printModel(
                    graphName,
                    repository.getGraph(graphName)));
        }
        
        return graphName;
    }
    
    /**
     * GETs a list of named graphs
     * 
     * @return an array of URIs for the graphs currently in Fuseki
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/graphs",
            produces=MediaType.APPLICATION_JSON_VALUE
            )
    public String[] getGraphUris(){
        return repository.getGraphs().toArray(new String[]{});
    }
    
    /**
     * GETs a serialization of a graph
     * 
     * @param graphUri the URI of a graph
     * 
     * @return a Turtle serialization of the indicated graph
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/graph",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String getGraph(
            @RequestParam("graphUri")
            String graphUri
            ){
        return Helper.printModel(
                graphUri,
                repository.getGraph(graphUri));
    }
    
    /**
     * Deletes a named graph.  If an empty graph URI is provided, this will 
     * delete the default graph.
     * 
     * @param graphUri a URI for a graph to delete
     * @return the URI of the deleted graph
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            value="/graph",
            produces=MediaType.TEXT_PLAIN_VALUE,
            consumes=MediaType.ALL_VALUE
            )
    public String deleteGraph(
            @RequestParam("graphUri")
            String graphUri
            ){
        
        if("".equals(graphUri)){
            graphUri = null;
        } else if(!repository.getGraphs().contains(graphUri)){
            return "graph " + graphUri + " does not exist!";
        }
        
        repository.deleteGraph(graphUri);
        
        return "deleted " + graphUri;
    }
    
    /**
     * Zeroizes Fuseki.  I.e., deletes all named graphs and the default graph.
     * Use with care.
     * 
     * @return "success" if fuseki was zeroized successfully
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            value="/zeroizeFuseki",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String zeroizeFuseki(){
        
        //delete all named datasets
        repository.deleteDatasets();
        
        //delete the default graph
        deleteGraph(null);
        
        return "success";
    }
    
    
    
    private static class Helper{
        
        private static String getImmortalsUuid(ImmortalsServiceProperties p){
            return UUID.randomUUID().toString() + 
                    "-IMMoRTALS-" + 
                    p.getImmortalsVersion();
        }
    
        private static String printModel(String graphName,Model model){
            
            StringBuilder sb = new StringBuilder();
            ExceptionWrapper.wrap(()->{
                sb.append(String.format("dump of graph [%s]\n", graphName));
                sb.append(String.format("----------------------------------\n"));
                sb.append(String.format(OntologyHelper.serializeModel(model, "Turtle")));
                sb.append(String.format("----------------------------------\n"));
            });
            
            return sb.toString();
        }
        
    }

}
