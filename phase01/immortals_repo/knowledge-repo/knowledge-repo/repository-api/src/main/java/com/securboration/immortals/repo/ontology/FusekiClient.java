package com.securboration.immortals.repo.ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.modify.UpdateProcessRemote;
import org.apache.jena.update.UpdateFactory;

/**
 * Simple Fuseki client for connecting to a dataset (a collection of graphs)
 * 
 * Instances of this class can be safely used concurrently by multiple threads
 * 
 * @author jstaples
 *
 */
public class FusekiClient {
    
    // e.g., http://localhost:3030/ds/data
    private final String fusekiServiceDataUrl;
    // e.g., http://localhost:3030/ds/query
    private final String fusekiServiceQueryUrl;
    // e.g., http://localhost:3030/ds/update
    private final String fusekiServiceUpdateUrl;
    
    
    
    /**
     * 
     * @return the names of the graphs stored in Fuseki
     */
    public Collection<String> getGraphNames(){
        List<String> graphNames = new ArrayList<>();
        
        String query = "SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o } }";
        
        executeSelectQuery(query, (QuerySolution s)->{
            graphNames.add(s.get("g").toString());
        });
        
        return graphNames;
    }

    /**
     * 
     * @param fusekiServiceBaseUrl
     *            a URL at which a running fuseki service can be reached. The
     *            dataset name is embedded in the URL. For example, the URL
     *            http://localhost:3030/ds will use a dataset with name "ds"
     */
    public FusekiClient(final String fusekiServiceBaseUrl) {
        this.fusekiServiceDataUrl = fusekiServiceBaseUrl + "/data";
        this.fusekiServiceQueryUrl = fusekiServiceBaseUrl + "/query";
        this.fusekiServiceUpdateUrl = fusekiServiceBaseUrl + "/update";
    }

    /**
     * Executes an UPDATE SPARQL command
     * @param sparql an UPDATE query to execute
     */
    public void executeUpdate(final String sparql) {
        new UpdateProcessRemote(
                UpdateFactory.create().add(sparql), 
                fusekiServiceUpdateUrl, 
                null
                ).execute();
    }
    
    private QueryEngineHTTP createQuery(final String sparql) {
        final Query query = QueryFactory.create(sparql);

        return QueryExecutionFactory.createServiceRequest(fusekiServiceQueryUrl,
                query);
    }

    /**
     * 
     * @param sparql an ASK query
     * @return the result of the ASK query
     */
    public boolean executeAskQuery(final String sparql) {
        try (QueryEngineHTTP query = createQuery(sparql)) {
            return query.execAsk();
        }
    }

    /**
     * 
     * @param sparql a CONSTRUCT query
     * @return the results of the CONSTRUCT query
     */
    public Model executeConstructQuery(final String sparql) {
        try (QueryEngineHTTP query = createQuery(sparql)) {
            return query.execConstruct();
        }
    }

    /**
     * 
     * @param sparql a SELECT query
     * @param processor the results of the SELECT query
     */
    public void executeSelectQuery(final String sparql,
            final ResultSetProcessor processor) {
        try (QueryEngineHTTP query = createQuery(sparql)) {
            ResultSet result = query.execSelect();

            while (result.hasNext()) {
                QuerySolution solution = result.next();

                processor.processQuerySolution(solution);
            }
        }
    }
    
    /**
     * Deletes the graph with the indicated name. If the name is null, deletes
     * the default graph.
     * 
     * @param graphName
     *            the name of a graph to delete, or null if the default graph
     *            should be deleted
     */
    public void deleteModel(String graphName) {
        DatasetAccessor accessor = getAccessor();
        
        if(graphName == null){
            accessor.deleteDefault();
        } else {
            accessor.deleteModel(graphName);
        }
    }
    
    private DatasetAccessor getAccessor(){
        return DatasetAccessorFactory.createHTTP(fusekiServiceDataUrl);
    }
    
    /**
     * 
     * @param graphName
     *            the name of the graph or null if the default graph
     * @return the graph stored in the fuseki triple store with the indicated
     *         name, or the default graph if the name is null
     */
    public Model getModel(String graphName) {
        DatasetAccessor accessor = getAccessor();
        
        if(graphName == null){
            return accessor.getModel();
        } else {
            return accessor.getModel(graphName);
        }
    }

    /**
     * Sets the current fuseki model
     * @param m the model to set
     */
    public void setModel(Model m,String graphName) {
        getAccessor().putModel(graphName,m);
    }

    /**
     * A simple functional interface for processing query results.  Lambda 
     * friendly.
     * 
     * @author jstaples
     *
     */
    public static interface ResultSetProcessor {
        public void processQuerySolution(QuerySolution s);
    }
}


