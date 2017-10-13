package com.securboration.immortals.repo.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import com.securboration.immortals.j2t.analysis.JavaToOwl;
import com.securboration.immortals.j2t.analysis.JavaToTriplesConfiguration;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.pojos.markup.PojoProperty;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.query.TriplesToPojo;
import com.securboration.immortals.repo.query.TriplesToPojo.SparqlPojoContext;
import com.securboration.immortals.repo.test.queries.ModelAssertionHelper;
import com.securboration.immortals.uris.Uris;

/**
 * Self-contained example of TriplesToPojo
 * @author jstaples
 *
 */
public class TriplesToPojoExampleSelfContained {
    
    public static void main(String[] args) throws Exception{
        new TriplesToPojoExampleSelfContained().testPojoSchemaGeneration();
    }
    
    @Test
    public void testPojoSchemaGeneration() throws Exception{
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
            
            new TriplesToPojoExampleSelfContained().doTest(
                client, 
                contextGraph
                );
        } finally {
            for(String graphName:cleanupThese){
                client.deleteModel(graphName);
            }
        }
    }
    
    private static Model getSchema(
            JavaToTriplesConfiguration config,
            Class<?>...pojos
            ) throws ClassNotFoundException{
        Model schema = new JavaToOwl(config).analyze(Arrays.asList(pojos));
        
        validateSchema(schema);
        
        return schema;
    }
    
    private static void validateSchema(Model schema){
        ModelAssertionHelper $ = new ModelAssertionHelper(schema);
        
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/repo/example#TriplesToPojoExampleSelfContained.Example",
            Uris.rdf.type$,
            Uris.owl.Class$
            );
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/repo/example#TriplesToPojoExampleSelfContained.Example",
            Uris.rdfs.subClassOf$,
            "http://darpa.mil/immortals/ontology/r2.0.0#ImmortalsBaseClass"
            );
        
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0#hasLongForm",
            Uris.rdf.type$,
            Uris.owl.DatatypeProperty$
            );
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0#hasLongForm",
            Uris.rdfs.domain$,
            "http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/repo/example#TriplesToPojoExampleSelfContained.Example"
            );
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0#hasLongForm",
            Uris.rdfs.range$,
            "http://www.w3.org/2001/XMLSchema#int"
            );
        
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0#hasHumanReadableDesc",
            Uris.rdf.type$,
            Uris.owl.DatatypeProperty$
            );
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0#hasHumanReadableDesc",
            Uris.rdfs.domain$,
            "http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/repo/example#TriplesToPojoExampleSelfContained.Example"
            );
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0#hasHumanReadableDesc",
            Uris.rdfs.range$,
            "http://www.w3.org/2001/XMLSchema#string"
            );
        
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0#hasExample",
            Uris.rdf.type$,
            Uris.owl.ObjectProperty$
            );
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0#hasExample",
            Uris.rdfs.domain$,
            "http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/repo/example#TriplesToPojoExampleSelfContained.Example"
            );
        $.assertContainsTriple(
            "http://darpa.mil/immortals/ontology/r2.0.0#hasExample",
            Uris.rdfs.range$,
            "http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/repo/example#TriplesToPojoExampleSelfContained.Example"
            );
        $.assertContainsTripleLiteral(
            "http://darpa.mil/immortals/ontology/r2.0.0#hasExample",
            Uris.rdfs.comment$,
            "a test comment about the HasExample property"
            );
        
        
    }
    
    private static Model getIndividuals(
            ObjectToTriplesConfiguration config
            ){
        Example e1 = new Example();
        Example e2 = new Example();
        Example e3 = new Example2();
        
        e1.humanReadableForm = "e1";
        e1.numberField = 1;
        
        e2.humanReadableForm = "e2";
        e2.numberField = 2;
        
        e3.humanReadableForm = "e3";
        e3.numberField = 3;
        
//        if(false)
        {
            e1.exampleee = e2;
            e2.exampleee = e3;
            e3.exampleee = e1;
        }
        
        Model model = ModelFactory.createDefaultModel();
        
        model.add(ObjectToTriples.convert(config, e1));
        model.add(ObjectToTriples.convert(config, e2));
        model.add(ObjectToTriples.convert(config, e3));
        
        return model;
    }
    
    private static String createAnalysisContext(FusekiClient client) throws IOException, ClassNotFoundException{
        Model model = ModelFactory.createDefaultModel();
        
        Model schema = getSchema(
            new JavaToTriplesConfiguration("r2.0.0"),
            Example.class,
            Example2.class
            );
        
        model.add(schema);
        model.add(getIndividuals(new ObjectToTriplesConfiguration("r2.0.0")));
        
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
            getExampleIndividualsQuery(graphName), 
            graphName, 
            client, 
            new ObjectToTriplesConfiguration("r2.0.0")
            );
        
        //iterate through results
        queryResult.forEach(solution ->{
            Example example = (Example)solution.get("x");
            
            try {
                System.out.println(OntologyHelper.serializeModel(
                    ObjectToTriples.convert(
                        new ObjectToTriplesConfiguration("r2.0.0"), 
                        example
                        ),
                    "TURTLE",
                    false
                    ));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    private String getExampleIndividualsQuery(
            final String graphName
            ){
        String query = (
                "" +
                "SELECT ?x WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        ?x <http://darpa.mil/immortals/ontology/r2.0.0#hasHumanReadableDesc> ?y .\r\n" +
                "    } .\r\n" + 
                "}"
                ).replace("?GRAPH?", graphName);
        
        System.out.println(query);
        
        return query;
    }
    
    public static class Example implements HumanReadable, HasNumber, HasExample, Comparable<Example>{

        private String humanReadableForm;

        private int numberField;
        
        private Example exampleee;

        @Override
        public String getHumanReadableDesc() {
            return humanReadableForm;
        }

        @Override
        public int hasFeaturedNumberField() {
            return numberField;
        }

        @Override
        public long hasLongForm() {
            return numberField;
        }

        @Override
        public Example getExample() {
            return exampleee;
        }

        @Override
        public int compareTo(Example o) {
            throw new RuntimeException("not implemented");
        }

    }
    
    public static class Example2 extends Example{
        
    }

    @PojoProperty
    public interface HumanReadable {
        public String getHumanReadableDesc();
    }
    
    @PojoProperty
    public interface HasExample {
        
        @Triple(
            predicateUri=Uris.rdfs.comment$,
            objectLiteral=@Literal(
                value="a test comment about the HasExample property"
                )
            )
        public Example getExample();
    }
    
    @PojoProperty
    public interface HasNumber {
        
        public int hasFeaturedNumberField();
        public long hasLongForm();
        
    }

}
