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
import org.junit.Assert;
import org.junit.Test;

import com.securboration.immortals.j2t.analysis.JavaToOwl;
import com.securboration.immortals.j2t.analysis.JavaToTriplesConfiguration;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.inference.AskQuery;
import com.securboration.immortals.ontology.inference.ConstructQuery;
import com.securboration.immortals.ontology.inference.InferenceRule;
import com.securboration.immortals.ontology.inference.InferenceRules;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ratiocinate.engine.RatiocinationEngine;
import com.securboration.immortals.repo.ontology.FusekiClient;

public class RaticinationEngineTest {
    
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
            
            InferenceRule constructCreatures = getConstructCreatures();
            
            rules.getRules().add(getConstructAdditionalHumanoidInfo());
            rules.getRules().add(getConstructHumanoidInfo());
            rules.getRules().add(constructCreatures);
            rules.getRules().add(getConstructHumanoids(constructCreatures));
            
            return rules;
        }
        
        private InferenceRule getConstructAdditionalHumanoidInfo(){
            InferenceRule rule = new InferenceRule();
            
            rule.setHumanReadableDesc("a test rule with a predicate");
            
            final String predicate = 
                    "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                    "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                    "\n"+
                    "ASK WHERE {\n"+
                    "    GRAPH <?GRAPH?> {\n"+
                    "        ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#Humanoid> .\n"+
                    "        ?x <http://darpa.mil/immortals/ontology/r2.0.0#hasAdditionalProperty> ?y .\n"+
                    "    }\n"+
                    "}\n"+
                    "\n";
            
            rule.getPredicate().add(ask(predicate));
            
            final String query = 
                    "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                    "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                    "\n"+
                    "CONSTRUCT {\n"+
                    "        ?x IMMoRTALS:hasAdditionalNumericProperty ?value .\n"+
                    "}\n"+
                    "WHERE {\n"+
                    "    GRAPH <?GRAPH?> {\n"+
                    "        ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#Humanoid> .\n"+
                    "        BIND( rand() as ?value) .\n"+
                    "    }\n"+
                    "}\n"+
                    "\n"
                    ;
            
            rule.setForwardInferenceRule(construct(query));
            
            return rule;
        }
        
        private InferenceRule getConstructHumanoidInfo(){
            InferenceRule rule = new InferenceRule();
            
            rule.setHumanReadableDesc("a test rule with a predicate");
            
            final String predicate = 
                    "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                    "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                    "\n"+
                    "ASK WHERE {\n"+
                    "    GRAPH <?GRAPH?> {\n"+
                    "        ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#Humanoid> .\n"+
                    "    }\n"+
                    "}\n"+
                    "\n";
            
            rule.getPredicate().add(ask(predicate));
            
            final String query = 
                    "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                    "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                    "\n"+
                    "CONSTRUCT {\n"+
                    "        ?x IMMoRTALS:hasAdditionalProperty ?value .\n"+
                    "}\n"+
                    "WHERE {\n"+
                    "    GRAPH <?GRAPH?> {\n"+
                    "        ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#Humanoid> .\n"+
                    "        BIND( STR(NOW()) as ?value) .\n"+
                    "    }\n"+
                    "}\n"+
                    "\n"
                    ;
            
            rule.setForwardInferenceRule(construct(query));
            
            return rule;
        }
        
        private InferenceRule getConstructHumanoids(InferenceRule prior){
            InferenceRule rule = new InferenceRule();
            
            rule.getExplicitPrecondition().add(prior);
            rule.setHumanReadableDesc("a test rule that depends upon another");
            
            final String query = 
                    "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                    "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                    "\n"+
                    "CONSTRUCT {\n"+
                    "        ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#Humanoid> .\n"+
                    "        ?x IMMoRTALS:hasName ?name .\n"+
                    "        ?x <http://www.w3.org/2000/01/rdf-schema#comment> \"a humanoid creature\" .\n"+
                    "}\n"+
                    "WHERE {\n"+
                    "    GRAPH <?GRAPH?> {\n"+
                    "        ?x a ?y .\n"+
                    "        ?y <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#RaticinationEngineTest.ExampleVocabulary.Human> .\n"+
                    "        ?x IMMoRTALS:hasName ?name .\n"+
                    "    }\n"+
                    "}\n"+
                    "\n";
            
            rule.setForwardInferenceRule(construct(query));
            
            return rule;
        }
        
        private InferenceRule getConstructCreatures(){
            InferenceRule rule = new InferenceRule();
            
            rule.setHumanReadableDesc("a simple test rule");
            
            final String query = 
                    "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                    "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                    "\n"+
                    "CONSTRUCT {\n"+
                    "        ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#Creature> .\n"+
                    "        ?x IMMoRTALS:hasName ?name .\n"+
                    "        ?x <http://www.w3.org/2000/01/rdf-schema#comment> \"a creature\" .\n"+
                    "}\n"+
                    "WHERE {\n"+
                    "    GRAPH <?GRAPH?> {\n"+
                    "        ?x a ?y .\n"+
                    "        ?y <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#RaticinationEngineTest.ExampleVocabulary.Animal> .\n"+
                    "        ?x IMMoRTALS:hasName ?name .\n"+
                    "    }\n"+
                    "}\n"+
                    "\n";
            
            rule.setForwardInferenceRule(construct(query));
            
            return rule;
        }
        
        private static ConstructQuery construct(String query){
            ConstructQuery q = new ConstructQuery();
            q.setQueryText(query);
            
            return q;
        }
        
        private static AskQuery ask(String query){
            AskQuery q = new AskQuery();
            q.setQueryText(query);
            
            return q;
        }
    }
    
    @Ignore
    public static class ExampleVocabulary extends TestVocabularyBase{
        
        public static class Animal{
            private String name;

            public Animal(String name) {
                super();
                this.name = name;
            }
            
            public Animal(){}

            
            public String getName() {
                return name;
            }

            
            public void setName(String name) {
                this.name = name;
            }
        }
        
        public static class Reptile extends Animal{
            private String reptileProperty;
            
            public Reptile(){}
            
            public Reptile(String name, String reptileProperty){
                super(name);
                this.reptileProperty = reptileProperty;
            }

            
            public String getReptileProperty() {
                return reptileProperty;
            }

            
            public void setReptileProperty(String reptileProperty) {
                this.reptileProperty = reptileProperty;
            }
        }
        
        public static class Snake extends Reptile{
            private String snakeProperty;
            
            public Snake(){}
            
            public Snake(String name, String reptileProperty, String snakeProperty){
                super(name,reptileProperty);
                this.snakeProperty = snakeProperty;
            }

            
            public String getSnakeProperty() {
                return snakeProperty;
            }

            
            public void setSnakeProperty(String snakeProperty) {
                this.snakeProperty = snakeProperty;
            }
        }
        
        public static class Mammal extends Animal{
            private String mammalProperty;
            
            public Mammal(){}
            
            public Mammal(String name, String mammalProperty){
                super(name);
                this.mammalProperty = mammalProperty;
            }

            
            public String getMammalProperty() {
                return mammalProperty;
            }

            
            public void setMammalProperty(String mammalProperty) {
                this.mammalProperty = mammalProperty;
            }
        }
        
        public static class Human extends Mammal{
            private String humanProperty;
            
            public Human(){}

            public Human(String name, String mammalProperty,String humanProperty) {
                super(name,mammalProperty);
                this.humanProperty = humanProperty;
            }

            
            public String getHumanProperty() {
                return humanProperty;
            }

            
            public void setHumanProperty(String humanProperty) {
                this.humanProperty = humanProperty;
            }
        }
        

        @Override
        public List<Object> getIndividuals(){
            return Arrays.asList(
                new Human("human-1","98.6","english"),
                new Human("human-2","98.6","french"),
                new Human("human-3","98.6","italian"),
                new Mammal("human-4","98.6"),
                new Snake("snake-1","scaly","venomous")
                );
        }
        
    }
    
    private static class Tools{
        private static String printModel(String tag, Model m) throws IOException{
            String s = OntologyHelper.serializeModel(m, "TURTLE", false);
            
            return tag + s.replace("\n", "\n" + tag);
        }
    }
    
//    private static Model addNonTestVocab(
//            Model model, 
//            Class<?>...vocab
//            ) throws ClassNotFoundException{
//        model.add(
//            new JavaToOwl(
//                new JavaToTriplesConfiguration(NS)).analyze(
//                    Arrays.asList(vocab)
//                    )
//            );
//        
//        return model;
//    }
//    
//    private static Model addNonTestVocab(
//            Model model, 
//            File ttlDir
//            ) throws ClassNotFoundException, IOException{
//        
//        Assert.assertTrue(ttlDir.exists());
//        Assert.assertTrue(ttlDir.isDirectory());
//        Assert.assertTrue(ttlDir.list().length > 0);
//        
//        for(File f:FileUtils.listFiles(ttlDir, new String[]{"ttl"}, true)){
//            System.out.printf(
//                "found vocabulary document %s\n", 
//                f.getAbsolutePath()
//                );
//            
//            ByteArrayInputStream content = 
//                    new ByteArrayInputStream(FileUtils.readFileToByteArray(f));
//            
//            model.read(content, null, "TURTLE");
//        }
//        
//        return model;
//    }
    
    private static Model addNonTestVocab(
            Model model, 
            String ttlResourceName
            ) throws ClassNotFoundException, IOException{
        
        try(InputStream resource = RaticinationEngineTest.class.getResourceAsStream(ttlResourceName)){
            
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
            ExampleVocabulary.class,
            Rules.class
            );
        
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
            client.setModel(testModel, graphName);
            
            RatiocinationEngine engine = 
                    new RatiocinationEngine(client,graphName);
            
            validateEngine(engine);
            
            engine.execute();
            
            validate(client,client.getFusekiServiceDataUrl() + "/" + graphName);
        } finally {
            client.deleteModel(graphName);
        }
    }
    
    private void validateEngine(RatiocinationEngine engine){
        
        List<InferenceRules> rulesets = new ArrayList<>();
        
        engine.getInferenceRules().forEach(r->rulesets.add(r));
        
        Assert.assertTrue(
            "expected >= 1 but got " + rulesets.size(),
            rulesets.size() >= 1
            );
    }
    
    private void validate(
            final FusekiClient client,
            final String graphName
            ){
        
        //rule 1 (constructs Creatures from Animals)
        {
            assertAskResult(
                "verify at least one creature was created (rule1)",
                
                client,graphName,
                
                true,
                "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "\n"+
                "ASK WHERE {\n"+
                "    GRAPH <?GRAPH?> {\n"+
                "        ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#Creature> .\n"+
                "        ?x IMMoRTALS:hasName ?name .\n"+
                "    }\n"+
                "}\n"+
                "\n"
                );
            
            assertAskResult(
                "verify all animals are creatures (rule1)",
                
                client,graphName,
                
                false,
                "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "\n"+
                "ASK WHERE {\n"+
                "    GRAPH <?GRAPH?> {\n"+
                "        ?x a ?y .\n"+
                "        ?y <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#RaticinationEngineTest.ExampleVocabulary.Animal> .\n"+
                "        ?x IMMoRTALS:hasName ?name .\n"+
                "        FILTER NOT EXISTS { ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#Creature> } .\n"+
                "    }\n"+
                "}\n"+
                "\n"
                );
        }
        
        //rule 2 (creates Humanoids from Humans)
        {
            assertAskResult(
                "verify at least one humanoid was created (rule2)",
                
                client,graphName,
                
                true,
                "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "\n"+
                "ASK WHERE {\n"+
                "    GRAPH <?GRAPH?> {\n"+
                "        ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#Humanoid> .\n"+
                "        ?x IMMoRTALS:hasName ?name .\n"+
                "    }\n"+
                "}\n"+
                "\n"
                );
            
            assertAskResult(
                "verify all humans are humanoids (rule2)",
                
                client,graphName,
                
                false,
                "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "\n"+
                "ASK WHERE {\n"+
                "    GRAPH <?GRAPH?> {\n"+
                "        ?x a ?y .\n"+
                "        ?y <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#RaticinationEngineTest.ExampleVocabulary.Human> .\n"+
                "        ?x IMMoRTALS:hasName ?name .\n"+
                "        FILTER NOT EXISTS { ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#Humanoid> } .\n"+
                "    }\n"+
                "}\n"+
                "\n"
                );
        }
        
        //rule 3 (adds property to all Humans)
        {
            assertAskResult(
                "verify all Humans have an additional property (rule3)",
                
                client,graphName,
                
                false,
                "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "\n"+
                "ASK WHERE {\n"+
                "    GRAPH <?GRAPH?> {\n"+
                "        ?x a ?y .\n"+
                "        ?y <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#RaticinationEngineTest.ExampleVocabulary.Human> .\n"+
                "        FILTER NOT EXISTS { ?x <http://darpa.mil/immortals/ontology/r2.0.0#hasAdditionalProperty> ?value } .\n"+
                "    }\n"+
                "}\n"+
                "\n"
                );
        }
        
        //rule 4 (adds additional property to all Humans)
        {
            assertAskResult(
                "verify all Humans have an additional property (rule4)",
                
                client,graphName,
                
                false,
                "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "\n"+
                "ASK WHERE {\n"+
                "    GRAPH <?GRAPH?> {\n"+
                "        ?x a ?y .\n"+
                "        ?y <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/ratiociantion/engine/test#RaticinationEngineTest.ExampleVocabulary.Human> .\n"+
                "        FILTER NOT EXISTS { ?x <http://darpa.mil/immortals/ontology/r2.0.0#hasAdditionalNumericProperty> ?value } .\n"+
                "    }\n"+
                "}\n"+
                "\n"
                );
        }
    }
    
    private void assertAskResult(
            final String message,
            final FusekiClient client,
            final String graphName,
            final boolean expectedValue,
            final String query
            ){
        System.out.printf("\t%64s    ",message);
        
        Assert.assertTrue(
            message + " (failed)",
            expectedValue == client.executeAskQuery(query.replace("?GRAPH?", graphName))
            );
        
        System.out.printf("PASS\n");
    }

}
