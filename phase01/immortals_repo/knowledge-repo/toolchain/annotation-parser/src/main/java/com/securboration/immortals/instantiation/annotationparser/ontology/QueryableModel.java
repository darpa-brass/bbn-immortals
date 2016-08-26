package com.securboration.immortals.instantiation.annotationparser.ontology;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

public class QueryableModel {
    private final Model model;

    public QueryableModel(Model model) {
        super();
        this.model = model;
    }
    
    private static String prefixes = 
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> \n" +        
            "PREFIX immortals: <http://darpa.mil/immortals/ontology/r2.0.0#> \n"
            ;
    
    public void printAClassInstances(){
        final String sparql = 
                prefixes + 
                "SELECT ?class WHERE {                                    \n" +
                " ?class a bytecode:AClass                                \n" +
                "}                                                        \n"
                ;
        
        executeSelectQuery(sparql,s->{
            System.out.println(s.get("class").asResource().getURI());
        });
    }
    
    private void executeSelectQuery(
            final String sparql,
            QueryResultProcessor p
            ){
        
//        Console.log(
//            "executing query:\n%s\n", 
//            sparql
//            );
        
        Query query = QueryFactory.create(sparql);
        
        try(QueryExecution execution = 
                QueryExecutionFactory.create(query, model);){
        
            ResultSet rs = execution.execSelect();
            
            int count = 0;
            while(rs.hasNext()){
                QuerySolution s = rs.next();
                
                p.visitSolution(s);
                count++;
            }
            
//            Console.log("%d solutions found\n", count);
        }
    }
    
    private static interface QueryResultProcessor{
        void visitSolution(QuerySolution s);
    }
    
    public String getUriOfClassWithHash(
            final String classHash
            ){
        final String sparql = 
                prefixes + 
                "SELECT ?class WHERE { \n" +
                " ?class a bytecode:AClass . \n" +
                " ?class immortals:hasBytecodePointer_String \"" + classHash + "\" . \n" +
                "}\n"
                ;
        
        List<String> solutions = new ArrayList<>();
        executeSelectQuery(sparql,s->{
            solutions.add(s.get("class").asResource().getURI());
        });
        
        if(solutions.size() == 0){
            return null;
        } else if(solutions.size() == 1){
            return solutions.get(0);
        } else {
            throw new RuntimeException(
                "query returned " + solutions.size() + " but expected 0 or 1");
        }
    }
    
    public String getUriOfMethodWithHash(
            final String classHash,
            final String methodName
            ){
        
        final String pointer = classHash + "/methods/" + methodName;
        
        final String sparql = 
                prefixes + 
                "SELECT ?class WHERE { \n" +
                " ?class a bytecode:AMethod . \n" +
                " ?class immortals:hasBytecodePointer_String \"" + pointer + "\" . \n" +
                "}\n"
                ;
        
        List<String> solutions = new ArrayList<>();
        executeSelectQuery(sparql,s->{
            solutions.add(s.get("class").asResource().getURI());
        });
        
        if(solutions.size() == 0){
            return null;
        } else if(solutions.size() == 1){
            return solutions.get(0);
        } else {
            throw new RuntimeException(
                "query returned " + solutions.size() + " but expected 0 or 1");
        }
        
        
    }
}
