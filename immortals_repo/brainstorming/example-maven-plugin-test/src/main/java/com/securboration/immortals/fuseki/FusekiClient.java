package com.securboration.immortals.fuseki;

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
 * Simple pure Java Fuseki client
 * 
 * @author jstaples
 *
 */
public class FusekiClient {
    // e.g., http://localhost:3030/ds
    private final String fusekiServiceBaseUrl;
    // e.g., http://localhost:3030/ds/data
    private final String fusekiServiceDataUrl;
    // e.g., http://localhost:3030/ds/query
    private final String fusekiServiceQueryUrl;
    // e.g., http://localhost:3030/ds/update
    private final String fusekiServiceUpdateUrl;

    /**
     * 
     * @param fusekiServiceBaseUrl
     *            a URL at which a running fuseki service can be reached. The
     *            dataset name is embedded in the URL. For example, the URL
     *            http://localhost:3030/ds will use a dataset with name "ds"
     */
    public FusekiClient(final String fusekiServiceBaseUrl) {
        this.fusekiServiceBaseUrl = fusekiServiceBaseUrl;

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
     * 
     * @return the current model stored in the fuseki triple store
     */
    public Model getModel() {
        DatasetAccessor accessor = DatasetAccessorFactory
                .createHTTP(fusekiServiceDataUrl);

        return accessor.getModel();
    }

    /**
     * Sets the current fuseki model
     * @param m the model to set
     */
    public void setModel(Model m) {
        DatasetAccessor accessor = DatasetAccessorFactory
                .createHTTP(fusekiServiceDataUrl);

        accessor.putModel(m);
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
