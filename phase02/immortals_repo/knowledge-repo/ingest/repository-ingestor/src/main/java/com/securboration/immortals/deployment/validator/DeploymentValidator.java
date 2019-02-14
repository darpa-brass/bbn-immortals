package com.securboration.immortals.deployment.validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

public class DeploymentValidator {
    
    private static Set<String> select(
            Model model, 
            String sparql,
            StringBuilder report
            ){
        
        report.append(String.format("\t\tusing query: %s\n", sparql));
        
        Set<String> solutions = new HashSet<>();
        
        Query query = QueryFactory.create(sparql) ;
        
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect() ;
            
            results.forEachRemaining(r->{
                Set<String> vars = new HashSet<>();
                r.varNames().forEachRemaining(n->vars.add(n));
                
                if(vars.size() == 0){
                    throw new RuntimeException("no solution var for query " + sparql);
                }
                
                if(vars.size() > 1){
                    throw new RuntimeException("expected exactly 1 solution var for query " + sparql);
                }
                
                vars.forEach(n->{
                    solutions.add(r.get(n).toString());
                });
            });
        }
        
        return solutions;
    }
    
    private static class Metrics{
        private Map<String,Object> map = new HashMap<>();
        
        public void increment(String metric){
            if(!map.containsKey(metric)){
                map.put(metric, new AtomicLong(0l));
            }
            
            AtomicLong m = (AtomicLong)map.get(metric);
            m.incrementAndGet();
        }
        
        public void set(String key,String value){
            map.put(key, value);
        }
        
        public void set(String key,long value){
            map.put(key, new AtomicLong(value));
        }
        
        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            
            sb.append("Metrics:\n");
            for(String key:new TreeSet<>(map.keySet())){
                Object value = map.get(key);
                
                if(value instanceof AtomicLong){
                    AtomicLong a = (AtomicLong)value;
                    
                    sb.append(String.format("\t%s->%d\n", key, a.get()));
                } else {
                    sb.append(String.format("\t%s->%s\n", key,value));
                }
            }
            
            return sb.toString();
        }
    }
    
    private static boolean validate(Model...models){
        Model uberModel = ModelFactory.createDefaultModel();
        
        for(Model m:models){
            uberModel.add(m);
        }
        
        InfModel infModel = 
                ModelFactory.createOntologyModel(
                    getValidationSpec(), 
                    uberModel
                    );
        
        return validateModel(infModel,System.out);
    }
    
    private static void verify(Model vocabModel, Model deploymentModel){

        
        //TODO: verify that all properties meet domain/range restrictions and
        //        match the core schema
        //      verify that all class types are consistent
        
        
        StringBuilder report = new StringBuilder();
        StringBuilder summary = new StringBuilder();
        Metrics metrics = new Metrics();
        
        //verify that the deployment graph is a model
        {
            Score s = validate(deploymentModel)?Score.PASS:Score.FAIL;
            summary.append(String.format("\t[%s] on assertion [deployment graph is a model consistent with OWL_MEM_RDFS_INF]\n",s));
        }
        
        //verify that the deployment graph + vocab is a model
        {
            Score s = validate(vocabModel,deploymentModel)?Score.PASS:Score.FAIL;
            summary.append(String.format("\t[%s] on assertion [{deployment graph U vocab} is a model consistent with OWL_MEM_RDFS_INF]\n",s));
        }
        
        
        verifyValidate(
            "model contains >0 triples",
            deploymentModel,
            "SELECT ?s WHERE { { ?s ?p ?o } } ",
            report,
            summary,
            (delta)->{
                    metrics.set("# triples", delta.size());
                    
                    report.append(
                        String.format(
                            "\t\t\tfound %d triples [omitted]\n",
                            delta.size()
                            )
                        );
                    
                    return delta.size() > 0 ? Score.PASS : Score.FAIL;
                }
            );
        
        verifyValidate(
            "subject and predicate URIs in the deployment model should have the correct ns prefix (http://darpa.mil/immortals/ontology)",
            deploymentModel,
            "SELECT ?n WHERE { { { ?n ?p ?o } UNION { ?s ?n ?o } } } ",
            report,
            summary,
            (delta)->{
                Score score = Score.PASS;
                for(String s:delta){
                    
                    if(s.startsWith("http://www.w3.org/")){
                        continue;
                    }
                    
                    if(!s.startsWith("http://darpa.mil/immortals/ontology/")){
                        score = Score.FAIL;
                        metrics.increment("# URIs with incorrect prefix");
                        
                        report.append(
                            String.format(
                                "\t\t\tfound incorrect prefix: %s\n",
                                s
                                )
                            );
                    } else {
                        metrics.increment("# URIs with correct prefix");
                    }
                    
                    metrics.increment("# URIs");
                }
                
                return score;
                }
            );
        
        verifyValidate(
            "subject and predicate URIs in the deployment model should have the correct version (r2.0.0)",
            deploymentModel,
            "SELECT ?n WHERE { { { ?n ?p ?o } UNION { ?s ?n ?o } } } ",
            report,
            summary,
            (delta)->{
                Score score = Score.PASS;
                for(String s:delta){
                    if(s.startsWith("http://darpa.mil/immortals/ontology/") && !(s.startsWith("http://darpa.mil/immortals/ontology/r2.0.0/")||s.startsWith("http://darpa.mil/immortals/ontology/r2.0.0#"))){
                        score = Score.FAIL;
                        metrics.increment("# URIs with incorrect version");
                        
                        report.append(
                            String.format(
                                "\t\t\tfound incorrect version: %s\n",
                                s
                                )
                            );
                    } else if(s.startsWith("http://darpa.mil/immortals/ontology/")) {
                        metrics.increment("# URIs with correct version");
                    }
                }
                
                return score;
            }
            );
        
        verifyValidate(
            "string literals probably should not use @en-gb",
            deploymentModel,
            "SELECT ?o WHERE { { { ?s ?p ?o } } } ",
            report,
            summary,
            (delta)->{
                Score score = Score.PASS;
                for(String s:delta){
                    
                    if(s.endsWith("@en-gb")){
                        metrics.increment("# string literals encoded as @en-gb");
                        
                        report.append(
                            String.format(
                                "\t\t\tfound weird string literal node: %s\n",
                                s.replace("\r", "").replace("\n", "")
                                )
                            );
                        
                        score = Score.WARN;
                    }
                }
                
                return score;
                }
            );
        
        verifyValidate(
            "the deployment model should not define any new classes",
            deploymentModel,
            "SELECT ?c WHERE { { ?c a <"+OWL.Class.getURI()+"> } UNION { ?c a <"+RDFS.Class.getURI()+"> } }",
            report,
            summary,
            (delta)->{
                Score score = Score.PASS;
                
                if(delta.size() > 0){
                    score = Score.FAIL;
                    
                    report.append(
                        String.format(
                            "\t\tfound %d class defs:\n",
                            delta.size()
                            )
                        );
                    for(String s:delta){
                        report.append(
                            String.format(
                                "\t\t\tfound definition of class: %s\n",
                                s
                                )
                            );
                    }
                }
                
                metrics.set("# new class types defined",delta.size());
                
                return score;
            }
            );
        
        verifyValidate(
            "the deployment model should not define any new properties",
            deploymentModel,
            "SELECT ?c WHERE { { ?c a <"+OWL.ObjectProperty.getURI()+"> } UNION { ?c a <"+OWL.DatatypeProperty.getURI()+"> } }",
            report,
            summary,
            (delta)->{
                Score score = Score.PASS;
                
                if(delta.size() > 0){
                    score = Score.FAIL;
                    
                    report.append(
                        String.format(
                            "\t\tfound %d property defs:\n",
                            delta.size()
                            )
                        );
                    for(String s:delta){
                        report.append(
                            String.format(
                                "\t\t\tfound definition of property: %s\n",
                                s
                                )
                            );
                    }
                }
                metrics.set("# new property types defined",delta.size());
                
                return score;
            }
            );
        
        verifyValidate(
            "predicates in the deployment model typically start with 'has'",
            deploymentModel,
            "SELECT ?p WHERE { { ?s ?p ?o . FILTER regex(str(?p), \"^http://darpa.mil/immortals/ontology/.*\") } }",
            report,
            summary,
            (delta)->{
                Score score = Score.PASS;
                for(String p:delta){
                    final String propertyName = p.substring(p.indexOf("#"));
                    if(!propertyName.startsWith("#has")){
                        score = Score.WARN;
                        metrics.increment("# predicates NOT starting with 'has...'");
                        
                        report.append(
                            String.format(
                                "\t\t\tfound nonconformant property: %s\n",
                                propertyName
                                )
                            );
                    } else {
                        metrics.increment("# predicates starting with 'has...'");
                    }
                    
                    metrics.increment("# predicates");
                }
                
                return score;
            }
            );
        
        verifyCompare(
            "predicates used in the deployment model should exist in the vocab",
            vocabModel,
            deploymentModel,
            "SELECT ?p WHERE { ?s ?p ?o }",
            report,
            summary,
            (delta)->{
                Score score = Score.PASS;
                
                if(delta.size() > 0){
                    score = Score.FAIL;
                    
                    report.append(String.format("\t\tfound %d non-vocab predicates:\n",delta.size()));
                    for(String s:delta){
                        report.append(String.format("\t\t\t%s\n",s));
                    }
                }
                metrics.set("# predicates used in deployment model that don't exist in vocab", delta.size());
                
                return score;
            }
            );
        
        verifyCompare(
            "# of class types instantiated > 0",
            vocabModel,
            deploymentModel,
            "SELECT ?c WHERE { { ?s a ?c } }",
            report,
            summary,
            (delta)->{
                Score score = Score.PASS;
                
                if(delta.size() == 0){
                    score = Score.FAIL;
                }
                metrics.set("# class types instantiated", delta.size());
                
                return score;
            }
            );
        
        //check all <x> a <y> statements to ensure that <y> is in the vocab
        verifyCompare(
            "for all {?x a ?y} triples, ?y must be a class that exists in the vocab",
            vocabModel,
            deploymentModel,
            "SELECT ?c WHERE { { ?s a ?c } UNION { ?c a <"+OWL.Class.getURI()+"> } }",
            report,
            summary,
            (delta)->{
                Score score = Score.PASS;
                
                if(delta.size() > 0){
                    score = Score.FAIL;
                    
                    report.append(String.format("\t\tfound %d non-vocab type instantiations:\n",delta.size()));
                    for(String s:delta){
                        report.append(String.format("\t\t\t%s\n",s));
                    }
                }
                metrics.set("# class types instantiated in deployment model that don't exist in vocab", delta.size());
                
                return score;
            }
            );
        
        report.append("Summmary:\n");
        report.append(summary.toString());
        report.append("\n");
        report.append(metrics.toString());
        report.append("\n\n[end]\n");
        
        System.out.println("\n\n\nReport for verification of [deployment model] against [IMMoRTALS vocab]:\n");
        System.out.println(report.toString());
    }
    
    private static void verifyValidate(
            String criterionName,
            Model testThis,
            String query,
            StringBuilder report,
            StringBuilder summary,
            Rubric rubric
            ){
        
        report.append(
            String.format(
                "\tVerifying model using criterion [%s]\n", 
                criterionName
                )
            );
        
        Set<String> testSolutions = select(testThis,query,report);
        
        Set<String> tUnique = new HashSet<>(testSolutions);
        
//        report.append(
//            String.format(
//                "\t\tfound the following %d solutions:\n", 
//                tUnique.size()
//                )
//            );
//        for(String s:new TreeSet<>(tUnique)){
//            report.append(String.format("\t\t\t%s\n", s));
//        }
        
        final String s = 
                String.format(
                    "\t[%s] on assertion [%s]\n",
                    rubric.score(tUnique),
                    criterionName
                    );
        report.append(s);
        summary.append(s);
        
        report.append("\n");
    }
    
    private static void verifyCompare(
            String criterionName,
            Model vocabulary, 
            Model testThis,
            String query,
            StringBuilder report,
            StringBuilder summary,
            Rubric rubric
            ){
        
        report.append(
            String.format(
                "\tComparing two models by criterion [%s]\n", 
                criterionName
                )
            );
        
        if(vocabulary == testThis){
            throw new RuntimeException("the two are trivially equal");
        }
        
        Set<String> vocabularySolutions = select(vocabulary,query,report);
        Set<String> testSolutions = select(testThis,query,report);
        
        Set<String> tUnique = new HashSet<>(testSolutions);
        
//        report.append(
//            String.format(
//                "\t\tfound the following %d items in the test model to verify in the vocabulary (which contains %d items):\n", 
//                tUnique.size(),
//                vocabularySolutions.size()
//                )
//            );
//        for(String s:new TreeSet<>(tUnique)){
//            report.append(String.format("\t\t\t%s\n", s));
//        }
        
        tUnique.removeAll(vocabularySolutions);
        
//        report.append(String.format(
//            "\t\tfound the following %d matches ONLY in the model to validate:\n", 
//            tUnique.size()
//            ));
//        for(String unique:new TreeSet<>(tUnique)){
//            report.append(String.format("\t\t\tDELTA: %s\n", unique));
//        }
        
        final String s = 
                String.format(
                    "\t[%s] on assertion [%s]\n",
                    rubric.score(tUnique),
                    criterionName
                    );
        report.append(s);
        summary.append(s);
        
        report.append("\n");
    }
    
    private interface Rubric{
        public Score score(Set<String> delta);
    }
    
    private static enum Score{
        PASS,
        WARN,
        FAIL
        ;
    }
    
    public static void main(String[] args) throws IOException{
        
        if(args.length != 2){
            
            System.out.println("This tool validates a test graph against a " +
                    "vocabulary.  Specifically, it determines whether the " +
                    "union of the vocabulary and test graph is a " +
                    "consistent model.  It also verifies several assertions " +
                    "about the models (e.g., that the test graph doesn't " +
                    "define new classes)."
                    );
            System.out.println("  Accepts 2 args");
            System.out.println("  Each arg is a .ttl file or dir containing arbitrarily nested .ttl files.");
            System.out.println("  Each arg is used to build a graph.");
            System.out.println("  Arg0 is the vocabulary graph and Arg1 is the test graph to be validated.");
            
            System.out.println("");
            System.out.println("Example args when run from ${immortals_trunk}/knowledge-repo/ingest/repository-ingestor");
            System.out.println("arg0:  ../../vocabulary/ontology-generate/target/classes/ontology");
            System.out.println("arg1:  ./immortals_model_LIB.ttl");
            
            System.out.println("");
            
            System.exit(-1);
        }
        
        //validate args
        {
            final File vocab = new File(args[0]);
            if(!vocab.exists()){
                throw new RuntimeException(
                    "vocabulary file/dir does not exist: " + args[0]);
            }
            
            final File validate = new File(args[1]);
            if(!validate.exists()){
                throw new RuntimeException(
                    "test graph file/dir does not exist: " + args[1]);
            }
        }
        
        final Model vocabGraph = getModel(args[0]);
        final Model testGraph = getModel(args[1]);
        
        verify(vocabGraph,testGraph);
    }
    
    private static Model getModel(String...paths) throws FileNotFoundException, IOException{
        List<File> files = new ArrayList<>();
        
        for(String path:paths){
            
            File f = new File(path);
            
            if(f.isFile()){
                files.add(f);
            } else if(f.isDirectory()){
                files.addAll(
                    FileUtils.listFiles(
                        f, 
                        new String[]{"ttl"}, 
                        true
                        )
                    );
            }
        }
        
        Model m = ModelFactory.createDefaultModel();
        
        for(File f:files){
            try(FileInputStream fi = new FileInputStream(f)){
                m.read(fi, null, "Turtle");
            }
        }
        
        return m;
    }
    
    private static OntModelSpec getValidationSpec(){
        
        final String spec = System.getProperty("validationSpec");
        
        if(spec != null){
            try {
                return (OntModelSpec)OntModelSpec.class.getField(spec).get(null);
            } catch (IllegalArgumentException | IllegalAccessException
                    | NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        
        return OntModelSpec.OWL_MEM_RDFS_INF;
    }
    
    private static void tee(
            StringBuilder sb, 
            PrintStream o, 
            String format, 
            Object...args
            ){
        sb.append(String.format(format, args));
        o.printf(format, args);
    }
    
    private static boolean validateModel(InfModel m,PrintStream o){
        
        StringBuilder sb = new StringBuilder();
        
        tee(
            sb,
            o,
            "Validating graph containing " + m.size() + " triples...\n"
            );
        
        final long startTime = System.currentTimeMillis();
        
        ValidityReport validity = m.validate();
        if (validity.isValid()) {
            tee(
                sb,
                o,
                "\t*** The graph is a valid model! ****\n"
                );
        }  else {
            tee(
                sb,
                o,
                "\t*** The graph is *NOT* a valid model! ****\n"
                );
        }
        
        final long stopTime = System.currentTimeMillis();
        
        final AtomicBoolean foundAnyIssues = new AtomicBoolean(false);
        
        tee(
            sb,
            o,
            "\tModel inconsistencies, if any, will be reported below:"
            );
        validity.getReports().forEachRemaining(r->{
            tee(sb,o,"\t - " + r + "\n");
            foundAnyIssues.set(true);
        });
        
        tee(
            sb,
            o,
            "\nChecked model validity in ~%ds\n", 
            (stopTime - startTime)/1000
            );
        

        if(foundAnyIssues.get()){
            return false;
        }
        
        return true;
    }

}
