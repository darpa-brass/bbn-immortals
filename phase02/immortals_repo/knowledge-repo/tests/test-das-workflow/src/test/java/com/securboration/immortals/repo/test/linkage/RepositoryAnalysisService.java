package com.securboration.immortals.repo.test.linkage;

import com.securboration.immortals.repo.ontology.FusekiClient;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by CharlesEndicott on 6/21/2017.
 */
public class RepositoryAnalysisService {
    
    FusekiClient client;
    
    public RepositoryAnalysisService(String fusekiEndpoint) {
        client = new FusekiClient(fusekiEndpoint);
    }
    
    public Set<String> dataLink(String bootstrapUri, String ucrUri) {

        final String constructQuery = getDataLinkConstructQuery(ucrUri, bootstrapUri);

        Model queryResult = client.executeConstructQuery(constructQuery);
        
        return getTriples(queryResult);
        
    }

    private static Set<String> getTriples(Model m){
        Set<String> triples = new HashSet<>();
        
        if(m == null){
            return triples;
        }

        StmtIterator statements = m.listStatements();

        while (statements.hasNext()) {
            Statement s = statements.next();

            triples.add(
                    String.format(
                            "[%s] [%s] [%s]\n",
                            s.getSubject(),
                            s.getPredicate(),
                            s.getObject()
                    )
            );
        }
        return triples;
    }
    
    private String getDataLinkConstructQuery(
            final String ucrGraph,
            final String secGraph
    ){
        return (
                "" +
                        "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" +
                        "CONSTRUCT { ?dfuInstance <http://darpa.mil/immortals/ontology/r2.0.0#hasResourceDependencies> ?resourceType } WHERE {\r\n" +
                        "    GRAPH <?SEC?> { \r\n" +
                        "        ?dfuInstance a <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance> .\r\n" +
                        "        ?dfuInstance IMMoRTALS:hasFunctionalAspects ?functionalAspect .\r\n" +
                        "        ?functionalAspect IMMoRTALS:hasMethodPointer ?pointer .\r\n" +
                        "    } .\r\n" +
                        "    GRAPH <?UCR?> { \r\n" +
                        "        ?report a <http://darpa.mil/immortals/ontology/r2.0.0/analysis#AnalysisReport> .\r\n" +
                        "        ?report IMMoRTALS:hasMeasurementProfile ?measurementProfile .\r\n" +
                        "        ?measurementProfile IMMoRTALS:hasCodeUnit ?codeUnit .\r\n" +
                        "        ?codeUnit IMMoRTALS:hasPointerString ?pointer . \r\n" +
                        "        ?report IMMoRTALS:hasDiscoveredDependency ?dependencyAssertion .\r\n" +
                        "        ?dependencyAssertion IMMoRTALS:hasDependency ?resourceType .\r\n" +
                        "    } .\r\n" +
                        "}"
        ).replace("?SEC?", secGraph).replace("?UCR?", ucrGraph);
    }
}
