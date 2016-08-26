package com.securboration.immortals.repo.api;

import java.util.Collection;

import org.apache.jena.rdf.model.Model;

import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.ontology.FusekiClient.ResultSetProcessor;

/**
 * Useful for slinging your own SPARQL queries without relying upon the
 * type-safe Java API in the superclass.
 * <p>
 * <i>It is <b>strongly</b> recommended that this be used only for testing and not
 * make its way into production code</i>
 * 
 * @author jstaples
 *
 */
public class RepositoryUnsafe extends Repository {

    public RepositoryUnsafe(RepositoryConfiguration configuration) {
        super(configuration);
    }
    
    /**
     * Converts an object, assumed to be one of the types defined in the
     * IMMoRTALS vocabulary, into a Model
     * 
     * @param immortalsObject
     *            an object to convert
     * @return a Model derived from the object provided
     */
    public Model getModelForObject(Object immortalsObject){
        return super.getModel(immortalsObject);
    }
    
    /**
     * Retrieve the names of all graphs in the current Fuseki dataset (note:
     * Fuseki datasets are different from IMMoRTALS datasets)
     * 
     * @return the names of all graphs
     */
    public Collection<String> getGraphs(){
        return getFusekiClient().getGraphNames();
    }
    
    /**
     * DELETE all datasets, use with care
     */
    public void deleteDatasets(){
        FusekiClient client = getFusekiClient();
        
        for(String graphName:client.getGraphNames()){
            client.deleteModel(graphName);
        }
    }
    
    /**
     * Executes a SELECT query against a Fuseki dataset.
     * 
     * @param sparql a SELECT SPARQL statement
     * @param processor a lambda-friendly result processor
     */
    public void executeSparqlQuery(
            String sparql,
            ResultSetProcessor processor
            ){
        
        getFusekiClient().executeSelectQuery(sparql, processor);
    }
    
    /**
     * Retrieves the indicated graph from the triple store
     * 
     * @param graphName the name of a graph to retrieve
     * @return the graph retrieved
     */
    public Model getGraph(String graphName){
        return getFusekiClient().getModel(graphName);
    }
    
    /**
     * Pushes a graph into Fuseki
     * 
     * @param graph the graph to push
     * @return a value that uniquely identifies the pushed graph
     */
    public String pushGraph(Model graph){
        
        final String uuid = generateRandomUuid();
        
        pushGraph(graph,uuid);
        
        return uuid;
    }
    
    /**
     * Pushes a named graph into Fuseki, overwriting any matching graph
     * 
     * @param graph the graph to push
     * @param name the name of the graph to push
     */
    public void pushGraph(Model graph,String name){
        
        getFusekiClient().setModel(graph, name);
    }

}
