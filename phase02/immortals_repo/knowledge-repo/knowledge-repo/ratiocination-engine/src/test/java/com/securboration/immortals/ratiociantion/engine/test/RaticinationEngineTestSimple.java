package com.securboration.immortals.ratiociantion.engine.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import com.securboration.immortals.j2t.analysis.JavaToOwl;
import com.securboration.immortals.j2t.analysis.JavaToTriplesConfiguration;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.inference.ConstructQuery;
import com.securboration.immortals.ontology.inference.InferenceRule;
import com.securboration.immortals.ontology.inference.InferenceRules;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ratiocinate.engine.RatiocinationEngine;
import com.securboration.immortals.ratiocinate.engine.RatiocinationReport;
import com.securboration.immortals.repo.ontology.FusekiClient;

public class RaticinationEngineTestSimple {
    
    private static final String NS = "r2.0.0";
    
    public static interface HasIndividuals{
        public List<Object> getIndividuals();
    }
    
    public static abstract class TestVocabularyBase implements HasIndividuals{
        /* intentionally empty */
    }
    
    @Ignore
    public static class Rules extends TestVocabularyBase{
        
        @Override
        public List<Object> getIndividuals(){
            return Arrays.asList(
                getRules()
                );
        }
        
        private InferenceRules getRules(){
            InferenceRules rules = new InferenceRules();
            
            rules.getRules().add(getTransitiveInference());
            rules.getRules().add(getInverseInference());
            rules.getRules().add(getIdentityInference());
            
            rules.setIterateUntilNoNewTriples(true);
            
            return rules;
        }
        
        private InferenceRule getIdentityInference(){
            InferenceRule rule = new InferenceRule();
            
            rule.setHumanReadableDesc("x -> [x friendOf x]");
            
            final String query = 
                    "CONSTRUCT {" +
                    "  ?x <http://test#friendOf> ?x . " +
                    "} WHERE { " +
                    "  GRAPH <?GRAPH?> {" +
                    "    ?x a <http://test#TestThing> . " +
                    "  }" +
                    "}";
            
            rule.setForwardInferenceRule(construct(query));
            
            return rule;
        }
        
        private InferenceRule getInverseInference(){
            InferenceRule rule = new InferenceRule();
            
            rule.setHumanReadableDesc("[x friendOf y] -> [y friendOf x]");
            
            final String query = 
                    "CONSTRUCT {" +
                    "  ?y <http://test#friendOf> ?x . " +
                    "} WHERE { " +
                    "  GRAPH <?GRAPH?> {" +
                    "    ?x <http://test#friendOf> ?y . " +
                    "    ?x a <http://test#TestThing> . " +
                    "    ?y a <http://test#TestThing> . " +
                    "  }" +
                    "}";
            
            rule.setForwardInferenceRule(construct(query));
            
            return rule;
        }
        
        private InferenceRule getTransitiveInference(){
            InferenceRule rule = new InferenceRule();
            
            rule.setHumanReadableDesc("[x friendOf y] [y friendOf z] -> [x friendOf z]");
            
            final String query = 
                    "CONSTRUCT {" +
                    "  ?x <http://test#friendOf> ?z . " +
                    "} WHERE { " +
                    "  GRAPH <?GRAPH?> {" +
                    "    ?x <http://test#friendOf> ?y . " +
                    "    ?y <http://test#friendOf> ?z . " +
                    "    ?x a <http://test#TestThing> . " +
                    "    ?y a <http://test#TestThing> . " +
                    "    ?z a <http://test#TestThing> . " +
                    "  }" +
                    "}";
            
            rule.setForwardInferenceRule(construct(query));
            
            return rule;
        }
        
        private static ConstructQuery construct(String query){
            ConstructQuery q = new ConstructQuery();
            q.setQueryText(query);
            
            return q;
        }
    }
    
    private static class Tools{
        private static String printModel(String tag, Model m) throws IOException{
            String s = OntologyHelper.serializeModel(m, "TURTLE", false);
            
            return tag + s.replace("\n", "\n" + tag);
        }
    }
    
    
    
    private static Model addNonTestVocab(
            Model model, 
            String ttlResourceName
            ) throws ClassNotFoundException, IOException{
        
        try(InputStream resource = RaticinationEngineTestSimple.class.getResourceAsStream(ttlResourceName)){
            
            Assert.assertNotNull("unable to find " + ttlResourceName,resource);
            
            ByteArrayInputStream content = new ByteArrayInputStream(
                IOUtils.toByteArray(resource)
                );
            
            final long start = model.size();
            model.read(content, null, "TURTLE");
            final long end = model.size();
            
            System.out.printf(
                "read %d triples from test resource %s\n", 
                end - start, 
                ttlResourceName
                );
        }
        
        return model;
    }
    
    @SafeVarargs//wink wink
    private static Model getTestVocabulary(
            Class<? extends TestVocabularyBase>...vocabularyClasses
            ) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        
        Model model = ModelFactory.createDefaultModel();
        
        for(Class<? extends TestVocabularyBase> vocabulary:vocabularyClasses){
            
            //add vocabulary constructs
            {
                List<Class<?>> vocabularyConstructs = new ArrayList<>();
                
                for(Class<?> c:vocabulary.getDeclaredClasses()){
                    vocabularyConstructs.add(c);
                }
                
                model.add(
                    new JavaToOwl(
                        new JavaToTriplesConfiguration(NS)).analyze(
                            vocabularyConstructs
                            )
                    );
            }
            
            //add individuals
            {
                ObjectToTriplesConfiguration config = 
                        new ObjectToTriplesConfiguration(NS);
                
                for(Object o: vocabulary.newInstance().getIndividuals()){
                    model.add(ObjectToTriples.convert(config, o));
                }
            }
            
        }
        
        
        
        return model;
    }
    
    private static Model getVocabulary() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
        Model testModel = getTestVocabulary(
            Rules.class
            );
        
        testModel.add(getModelToQuery());
        
        final long tripleCount1 = testModel.size();
        
        System.out.println(
            Tools.printModel("test vocab\t", testModel)
            );
        
        addNonTestVocab(
            testModel, 
            "/immortals_uber.ttl"
            );
        
        final long tripleCount2 = testModel.size();
        
        Assert.assertTrue(tripleCount2 > tripleCount1);
        
        return testModel;
    }
    
    @Test
    public void testEngine() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException{
        
        Model testModel = getVocabulary();
        
        FusekiClient client = new FusekiClient("http://localhost:3030/ds");
        
        final String graphName = "IMMoRTALS_test";
        
        try{
            client.deleteModel(graphName);
            client.setModel(testModel, graphName);
            
            final long startSize = testModel.size();
            
            RatiocinationEngine engine = 
                    new RatiocinationEngine(client,graphName);
            
            RatiocinationReport report = engine.execute();
            
            final long endSize = client.getModel(graphName).size();
            final long inferred = endSize - startSize;
            System.out.printf("inferred %d triples\n", inferred);
            
            Assert.assertTrue(
                "expected to infer > 0 triples but got " + inferred,
                inferred > 0
                );
            
            validate(client,client.getFusekiServiceDataUrl() + "/" +graphName);
            
            System.out.println(report.getReportText());
        } finally {
            client.deleteModel(graphName);
        }
    }
    
    private void validate(
            final FusekiClient client,
            final String graphName
            ){
        
        String[] people = {
                "<http://test#a>",
                "<http://test#b>",
                "<http://test#c>",
                "<http://test#d>",
                "<http://test#e>"
                };
        
        for(String p1:people){
            for(String p2:people){
                assertTriple(
                    client,
                    graphName,
                    true,
                    p1,"<http://test#friendOf>",p2
                    );
            }
        }
    }
    
    private void assertTriple(
            final FusekiClient client,
            final String graphName,
            final boolean expectedValue,
            final String s,final String p,final String o
            ){
        String message = String.format(
            "\tverifying that %64s %s    ",
            String.format("<%s> <%s> <%s>",s,p,o),
            expectedValue==true?"exists":"does not exist"
            );
        
        System.out.printf(message);
        
//        System.out.printf("%s %s %s %s\n", graphName,s,p,o);
        
        String query =
                (
                "ASK WHERE {\n"+
                "    GRAPH <?GRAPH?> {\n"+
                "        ?s? ?p? ?o? .\n"+
                "    }\n"+
                "}\n"
                )
                .replace("?GRAPH?", graphName)
                .replace("?s?", s)
                .replace("?p?", p)
                .replace("?o?", o)
                ;
        
//        System.out.println(query);
        
        Assert.assertTrue(
            message + " (failed)",
            expectedValue == client.executeAskQuery(query)
            );
        
        System.out.printf("PASS\n");
    }
    
    
    private static Model getModelToQuery(){
        Model m = ModelFactory.createDefaultModel();
        
        Resource thing = m.createResource("http://test#TestThing");
        
        Resource a = m.createResource("http://test#a");
        Resource b = m.createResource("http://test#b");
        Resource c = m.createResource("http://test#c");
        Resource d = m.createResource("http://test#d");
        Resource e = m.createResource("http://test#e");
        
        Property type = RDF.type;
        Property friendOf = m.createProperty("http://test#friendOf");
        
        //{a,b,c,d} are things
        m.add(a,type,thing);
        m.add(b,type,thing);
        m.add(c,type,thing);
        m.add(d,type,thing);
        m.add(e,type,thing);
        
        //{a friendOf b} and {b friendOf c}, {c friendOf d}
        m.add(a,friendOf,b);
        m.add(b,friendOf,c);
        m.add(c,friendOf,d);
        m.add(d,friendOf,e);
        
        return m;
    }

}
