package com.securboration.immortals.repo.example;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.cp.FunctionalitySpec;
import com.securboration.immortals.ontology.cp.GmeInterchangeFormat;
import com.securboration.immortals.ontology.cp.MissionSpec;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.query.TriplesToPojo;
import com.securboration.immortals.repo.query.TriplesToPojo.SparqlPojoContext;

/**
 * Illustrates the retrieval of a POJO view of the triples in a graph and the
 * synthesis of new triples that are linked to that graph
 * 
 * @author jstaples
 *
 */
public class PojoQueryExampleGmei {
    
    public static void main(String[] args) throws Exception{
        
        final FusekiClient client = new FusekiClient("http://localhost:3030/ds");
        
        
        Set<String> cleanupThese = new HashSet<>();
        
        try{
            String contextGraph = createAnalysisContext(client);
            
            cleanupThese.add(contextGraph);
            
            System.out.println(
                OntologyHelper.serializeModel(
                    client.getModel(contextGraph), 
                    "TURTLE", 
                    false
                    )
                );
            
            new PojoQueryExampleGmei().doTest(client, contextGraph);
        } finally {
            for(String graphName:cleanupThese){
                client.deleteModel(graphName);
            }
        }
    }
    
    private static void addGmei(Model m) throws IOException{
        getModelFromResource(m,"gmei.ttl");
    }
    
    private static void addSchema(Model m) throws IOException{
        getModelFromResource(m,"immortals_analysis.ttl");
        getModelFromResource(m,"immortals_bytecode.ttl");
        getModelFromResource(m,"immortals_core.ttl");
        getModelFromResource(m,"immortals_cp.ttl");
        getModelFromResource(m,"immortals_sa.ttl");
    }
    
    private static void getModelFromResource(
            final Model readInto, 
            final String path
            ) throws IOException{
        readInto.read(
            PojoQueryExampleGmei.class.getClassLoader().getResourceAsStream(path), 
            null, 
            "TURTLE"
            );
    }
    
    private static String createAnalysisContext(FusekiClient client) throws IOException{
        Model model = ModelFactory.createDefaultModel();
        
        addGmei(model);
        addSchema(model);
        
        return client.setModel(
            model, 
            "test-"+UUID.randomUUID().toString()
            );
    }
    
    private void doTest(
            final FusekiClient client, 
            final String graphName
            ) throws Exception {
        
        //run a query that selects all GME Interchange document instances
        SparqlPojoContext queryResult = TriplesToPojo.sparqlSelect(
            getGmeiQuery(graphName), 
            graphName, 
            client, 
            null//NOTE: this arg is only needed if you want to create new POJOs 
                //      that reference the query results
            );
        
        //iterate through results
        queryResult.forEach(solution ->{
            GmeInterchangeFormat gmei = (GmeInterchangeFormat)solution.get("gmei");
            
            System.out.printf(
                "found a GME Interchange instance: %s\n", 
                gmei.getHumanReadableDescription()
                );
            
            System.out.printf(
                "\twith URI: %s\n", 
                solution.get("gmei$uri")//special var injected to ease URI retrieval
                );
            
            System.out.printf("\tresources:\n");
            for(Resource r:gmei.getAvailableResources()){
                System.out.printf(
                    "\t\t%s: %s\n", 
                    r.getClass().getName(), 
                    r.getHumanReadableDescription()
                    );
            }
            
            System.out.printf("\tfunctionalities:\n");
            for(FunctionalitySpec spec:gmei.getFunctionalitySpec()){
                System.out.printf(
                    "\t\t%s\n", 
                    spec.getFunctionalityPerformed().getName()
                    );
            }
            
            System.out.printf("\tmission specs:\n");
            for(MissionSpec spec:gmei.getMissionSpec()){
                System.out.printf(
                    "\t\t%s\n", 
                    spec.getHumanReadableForm()
                    );
            }
        });
    }
    
    private String getGmeiQuery(
            final String graphName
            ){
        String query = (
                "" +
                "SELECT ?gmei WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        ?gmei a <http://darpa.mil/immortals/ontology/r2.0.0/cp#GmeInterchangeFormat> .\r\n" +
                "    } .\r\n" + 
                "}"
                ).replace("?GRAPH?", graphName);
        
        System.out.println(query);
        
        return query;
    }

}
