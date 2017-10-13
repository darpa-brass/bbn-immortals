package com.securboration.immortals.repo.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.securboration.immortals.ontology.bytecode.ClasspathElement;
import com.securboration.immortals.uris.Immortals;
import org.apache.jena.rdf.model.Model;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.bytecode.ClassArtifact;
import com.securboration.immortals.ontology.bytecode.JarArtifact;
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
public class PojoQueryExample {
    
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException{
        
        final FusekiClient client = new FusekiClient("http://localhost:3030/ds");
        final String graphName = "IMMoRTALS_r2.0.0";
        
        new PojoQueryExample().doTest(client, graphName);
    }
    
    
    private void testConversion(final FusekiClient client, final String graphName) {

        final ObjectToTriplesConfiguration config =
                new ObjectToTriplesConfiguration("r2.0.0");

        //run a query that selects up to 10 ClassArtifact individuals and their 
        //names
        SparqlPojoContext queryResult = TriplesToPojo.sparqlSelect(
                getAllIndividuals(graphName),
                graphName,
                client,
                config
        );

        List<Object> pojos = new ArrayList<>();

        queryResult.forEach(solution ->{
            
            Object converted = solution.get("individual");
            
            System.out.println("Serialized a concept with pojo provenance of: "
            + converted.getClass().getCanonicalName());
            
            pojos.add(converted);
            
        });

    }
    
    private void doTest(final FusekiClient client, final String graphName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException{
        
        final ObjectToTriplesConfiguration config = 
                new ObjectToTriplesConfiguration("r2.0.0");
        
        //run a query that selects up to 10 ClassArtifact individuals and their 
        // names
        SparqlPojoContext queryResult = TriplesToPojo.sparqlSelect(
            getIndividualsQuery(graphName), 
            graphName, 
            client, 
            config
            );
        
        Map<String,ClassArtifact> results = new HashMap<>();
        
        queryResult.forEach(solution ->{
            //a populated ClassArtifact
            ClassArtifact var1 = (ClassArtifact)solution.get("aClass");
            
            //a literal value (String) containing artifact's name
            String var2 = (String)solution.get("name");
            
            //note that the fields of the object have been recursively filled-in
            if(!var2.equals(var1.getName())){
                throw new RuntimeException("sanity check failed!");
            }
            
            results.put(var2, var1);
        });
        
        //now that we've queried some information from the model, let's create
        // some new triples
        
        JarArtifact newJar = new JarArtifact();
        newJar.setHash("hashValGoesHere");
        newJar.setJarContents(results.values().toArray(new ClassArtifact[]{}));
        newJar.setName("fakeJar");
        
        //and now serialize the synthesized POJO into a model.  Note that upon 
        // serialization, the URIs of any previously queried POJO are 
        // guaranteed to be consistent with the ontology
        
        Model newTriples = ObjectToTriples.convert(config, newJar);
        
        System.out.println(
            OntologyHelper.serializeModel(newTriples, "TURTLE", false)
            );
    }
    
    private String getIndividualsQuery(
            final String graphName
            ){
        String query = (
                "" +
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "SELECT ?aClass ?name WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        ?aClass a <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#ClassArtifact> .\r\n" +
                "        ?aClass IMMoRTALS:hasName ?name .\r\n" +
                "    } .\r\n" + 
                "} LIMIT 10"
                ).replace("?GRAPH?", graphName);
        
        System.out.println(query);
        
        return query;
    }
    
    private String getSelectQuery(
            final String graphName
            ){
        return (
                "" +
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "SELECT ?aClass WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        ?aClass a <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#AClass> .\r\n" + 
                "    } .\r\n" + 
                "}"
                ).replace("?GRAPH?", graphName);
    }


    private static String getAllIndividuals(
            final String graphName
    ){
        String query = (
                "" +
                        "SELECT ?individual WHERE {\r\n" +
                        "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" +
                        "        ?individual a ?type .\r\n" +
                        "        ?type <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://darpa.mil/immortals/ontology/r2.0.0#ImmortalsBaseClass> .\r\n" +
                        "    } .\r\n" +
                        "}"
        ).replace("?GRAPH?", graphName);

        System.out.println(query);

        return query;
    }


}
