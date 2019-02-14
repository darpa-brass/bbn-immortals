package com.securboration.immortals.repo.test.linkage;

import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.test.queries.QueryTestBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.UUID;


/**
 * Created by CharlesEndicott on 6/21/2017.
 */
public class RepositoryAnalysisServiceTest {

    private static final Logger logger =
            LoggerFactory.getLogger(RepositoryAnalysisServiceTest.class);
    
    private final String DEFAULT_FUSEKI_ENDPOINT = "http://localhost:3030/ds";
    
    FusekiClient client = new FusekiClient(DEFAULT_FUSEKI_ENDPOINT);
    QueryTestBase.CleanupContext cleanupContext = new QueryTestBase.CleanupContext();
    
    @Test
    public void runDataLinkTests() {
        
        try {
            logger.warn("Beginning data link test : Expecting 1 triple(s).");
            assert (dataLinkAnalysisTest(TestData.inputFromBytecodeAnalysis, TestData.inputFromUcr)
                    .toString().equals(TestData.expectedTriple));
            logger.warn("Done. This test has completed successfully.");
            
            logger.warn("Beginning data link test : Expecting 0 triple(s).");
            assert (dataLinkAnalysisTest(TestData.inputFromBytecodeAnalysis, TestData.ucrBadInput).size() == 0);
            logger.warn("Done. This test has completed successfully.");
            
            logger.warn("Beginning data link test : Expecting > 0 triple(s). ");
            assert (dataLinkAnalysisTest(TestData.customSecurInput, TestData.inputFromUcr).size() != 0);
            logger.warn("Done. This test has completed successfully.");

        } finally {
            for (String graphName : cleanupContext.getGraphsToCleanup()) {
                client.deleteModel(graphName);
            }
        }
    }
    
    public Model convertToModel(String ttlGraph) {
        final Model m = ModelFactory.createDefaultModel();
        m.read(new ByteArrayInputStream(ttlGraph.getBytes()),
                null,
                "TURTLE");
        
        return m;
    }
    
    public String getUniqueModelName() {
        return this.getClass().getSimpleName() +
                "-" +
                UUID.randomUUID().toString();
    }
    
    public Set<String> dataLinkAnalysisTest(String bootStrapInput, String ucrInput) {

        logger.warn("Initializing input graphs...");
        final Model bootStrapModel = convertToModel(bootStrapInput);
        final Model ucrModel = convertToModel(ucrInput);

        final String bootStrapName = getUniqueModelName();
        final String ucrName = getUniqueModelName();
        
        logger.warn("Done. Attempting to push to Fuseki...");
        cleanupContext.add(bootStrapName, "Bootstrap Graph");
        client.setModel(bootStrapModel, bootStrapName);
        cleanupContext.add(ucrName, "Ucr Graph");
        client.setModel(ucrModel, ucrName);
 
        String bootStrapUri = DEFAULT_FUSEKI_ENDPOINT + "/data/" + bootStrapName;
        String ucrUri = DEFAULT_FUSEKI_ENDPOINT + "/data/" + ucrName;
        
        logger.warn("Done. Retrieving query results...");
        RepositoryAnalysisService test = new RepositoryAnalysisService(DEFAULT_FUSEKI_ENDPOINT);
        Set<String> triples = test.dataLink(bootStrapUri, ucrUri);
        
        Model newTriples = ModelFactory.createDefaultModel();
        
        newTriples.setNsPrefixes(bootStrapModel);
        
        final String newTriplesName = this.getClass().getSimpleName() +
                "-" +
                UUID.randomUUID().toString();
        
        logger.warn("Done. Inserting results into both bootstrap and new, results graph... ");
        cleanupContext.add(newTriplesName, "Results Graph");
        client.setModel(newTriples, newTriplesName);

        String newTriplesUri = DEFAULT_FUSEKI_ENDPOINT + "/data/" + newTriplesName;

        for (String triple : triples) {
            triple = triple.replaceAll("\\[", "<").replaceAll("\\]", ">");
            
            String updateBootstrap = "INSERT DATA" +
                    " { GRAPH <?g> { " + triple + " } }";
            
            updateBootstrap = updateBootstrap.replace("?g", bootStrapUri);
            client.executeUpdate(updateBootstrap);
            updateBootstrap = updateBootstrap.replace(bootStrapUri, newTriplesUri);
            client.executeUpdate(updateBootstrap);
        }
        return triples;
    }
    
}