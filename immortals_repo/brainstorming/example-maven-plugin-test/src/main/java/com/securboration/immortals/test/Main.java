package com.securboration.immortals.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;

import com.securboration.immortals.fuseki.FusekiClient;
import com.securboration.immortals.ontology.OntologyHelper;

/**
 * Demonstrates the use of the Jena Java API for accessing a remote Fuseki
 * triple store. Demos include:
 * 
 * <ul>
 * <li>pushing a model to the server</li>
 * <li>getting a model from the server</li>
 * <li>executing SPARQL queries</li>
 * </ul>
 * 
 * <a href="https://jena.apache.org/documentation/serving_data/">download and
 * install fuseki</a>
 * 
 * @author jstaples
 *
 */
public class Main {

    public static void main(String[] args) throws IOException {

        String fusekiUrl = "http://localhost:3030/ds";

        if (args.length == 0) {
            System.err
                    .printf("*** WARNING: expected exactly one arg, a URL to a "
                            + "Fuseki dataset (e.g., \"http://localhost:3030/ds\") "
                            + "***\n\n");
        } else {
            fusekiUrl = args[0];
        }

        FusekiClient f = new FusekiClient(fusekiUrl);

        new Main().test(f);
    }

    private void test(FusekiClient f) throws IOException {

        // push a model to Fuseki
        testPushModel(f);

        // get a model from fuseki
        Model m = testGetModel(f);

        // print the triples to stdout
        System.out.printf(
                "Retrieved the following model from fuseki [.n3]: [\n\t%s\n]\n",
                OntologyHelper.serializeModel(m, "N-TRIPLES").replace("\n",
                        "\n\t"));

//        // SELECT {?wine <locatedIn> ?location}
//        testSELECT(f);
//
//        // ASK {?wine <locatedIn> <newZealandRegion>}
//        testASK(f);
//
//        // SELECT {?wine <locatedIn> <newZealandRegion>}
//        testSELECT2(f);
//
//        // INSERT
//        // {?wine <locatedIn> <southwestPacificRegion>}
//        // WHERE
//        // {?wine <locatedIn> <newZealandRegion>}
//        testINSERT(f);
//
//        // SELECT {?wine <locatedIn> <newZealandRegion>}
//        testSELECT2(f);
    }

    private void testINSERT(FusekiClient f) {
        final String sparql = "INSERT " + "{  "
                + "?wine <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#locatedIn>  <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SouthwestPacificRegion> "
                + "} WHERE { "
                + "?wine <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#locatedIn> <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#NewZealandRegion> "
                + "}";

        f.executeUpdate(sparql);

        System.out.printf("executed [%s]\n", sparql);
    }

    private void testASK(FusekiClient f) {
        final String sparql = " ASK " + "{  "
                + "?wine <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#locatedIn>  <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#NewZealandRegion>"
                + "} ";

        System.out.printf("query [%s]\n\treturned %s\n", sparql,
                f.executeAskQuery(sparql));
    }

    private void testSELECT(FusekiClient f) {
        final String sparql = " select ?wine ?location where " + "{  "
                + "?wine <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#locatedIn>  ?location"
                + "} ";

        System.out.printf("query [%s] returned the following results:\n",
                sparql);

        final AtomicInteger count = new AtomicInteger(0);
        f.executeSelectQuery(sparql, (QuerySolution solution) -> {
            System.out.printf("\tsolution: \n\t\t?wine=%s\n\t\t?location=%s\n",
                    solution.get("wine"), solution.get("location"));
            count.incrementAndGet();
        });
    }

    private void testSELECT2(FusekiClient f) {
        final String sparql = " select ?wine where " + "{  "
                + "?wine <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#locatedIn>  <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#NewZealandRegion>"
                + "} ";

        System.out.printf("query [%s] returned the following results:\n",
                sparql);

        final AtomicInteger count = new AtomicInteger(0);
        f.executeSelectQuery(sparql, (QuerySolution solution) -> {
            System.out.printf("\tsolution: \n\t\t?wine=%s\n",
                    solution.get("wine"), solution.get("location"));
            count.incrementAndGet();
        });
    }

    private Model testGetModel(FusekiClient f) throws IOException {
        return f.getModel();
    }

    private FusekiClient testPushModel(FusekiClient f) throws IOException {
        // load a test model from the classpath
        Model m = getTestModel();

        // push the model to fuseki
        f.setModel(m);

        return f;
    }

    private Model getTestModel() throws IOException {
        try (InputStream modelStream = new FileInputStream(
                "C:/Users/Securboration/TBCFreeWorkspace/IMMoRTALS/immortals.rdf"))// TODO
        {
            return OntologyHelper.loadModel(modelStream, "RDF/XML");
        }

    }

}
