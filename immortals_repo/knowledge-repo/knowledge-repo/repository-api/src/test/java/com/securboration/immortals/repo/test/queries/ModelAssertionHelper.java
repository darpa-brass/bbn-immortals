package com.securboration.immortals.repo.test.queries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class ModelAssertionHelper {
    
    private final Model model;
    private Set<Statement> statements = new HashSet<>();
    private Map<String,Set<Statement>> statementsAboutSubject = new HashMap<>();
    private Map<String,Set<Statement>> statementsContainingPredicate = new HashMap<>();
    private Map<String,Set<Statement>> statementsAboutObject = new HashMap<>();
    
    
    private static void addStatement(
            Map<String,Set<Statement>> statements, 
            String key, 
            Statement statement
            ){
        Set<Statement> statementsForKey = statements.get(key);
        if(statementsForKey == null){
            statementsForKey = new HashSet<>();
            statements.put(key, statementsForKey);
        }
        statementsForKey.add(statement);
    }
    
    public ModelAssertionHelper(Model model){
        Model m = ModelFactory.createDefaultModel();
        m.add(model);
        
        this.model = m;
        
        StmtIterator iterator = m.listStatements();
        
        while(iterator.hasNext()){
            Statement i = iterator.next();
            
            Resource s = i.getSubject();
            Property p = i.getPredicate();
            RDFNode o = i.getObject();
            
            if(s.isURIResource()){
                addStatement(statementsAboutSubject,s.getURI(),i);
            }
            
            addStatement(statementsContainingPredicate,p.getURI(),i);
            
            if(o.isURIResource()){
                addStatement(statementsAboutObject,o.asResource().getURI(),i);
            }
            
            statements.add(i);
        }
    }
    
    private static boolean exists(
            Set<Statement> statements, 
            String subject, 
            String predicate, 
            Object object,
            boolean isObjectLiteral
            ){
        
        System.out.printf(
            "searching for triple\n\ts: %s\n\tp: %s\n\to: %s%s%s (a %s)\n", 
            subject, 
            predicate, 
            isObjectLiteral?"\"":"",object,isObjectLiteral?"\"":"",
            isObjectLiteral?(object == null?("null"):("literal " + object.getClass().getName())):("uri")
            );
        
        for(Statement i:statements){
            Resource s = i.getSubject();
            Property p = i.getPredicate();
            RDFNode o = i.getObject();
            
            final boolean sMatches = 
                    (s == null) || (s.isURIResource() && s.getURI().equals(subject));
            
            final boolean pMatches = 
                    (p == null) || p.getURI().equals(predicate);
            
            final boolean oMatches;
            
            if(isObjectLiteral){
                
                String matchThis = null;
                
                if(o.isLiteral()){
                    String stringForm = o.asLiteral().toString();
                    String uri = o.asLiteral().getDatatypeURI();
                    String findThis = "^^"+uri;
                    
                    if(uri != null && stringForm.contains(findThis)){
                        matchThis = stringForm.substring(0, stringForm.lastIndexOf(findThis));
                    } else {
                        matchThis = stringForm;
                    }
                }
                oMatches = (o == null) || (o.isLiteral() && matchThis.equals(object.toString()));
            } else {
                oMatches = (o == null) || (o.isURIResource() && o.asResource().getURI().equals(object));
            }
            
            if(sMatches && pMatches && oMatches){
                System.out.println("\t\u2713");
                return true;
            }
        }
        
        System.out.println("\t\u2717");
        return false;
    }
    
    private void fail(String assumptionViolated){
        throw new RuntimeException(assumptionViolated);
    }
    
    
    public void assertContainsTriple(
            String subject, 
            String predicate, 
            String object
            ){
        boolean containsTriple = exists(
            statements,
            subject,
            predicate,
            object,
            false
            );
        
        if(!containsTriple){
            fail(
                String.format(
                    "expected to find triple [%s %s %s]", 
                    subject, 
                    predicate, 
                    object
                    )
                );
        }
    }
    
    public void assertDoesNotContainTriple(
            String subject, 
            String predicate, 
            String object
            ){
        boolean containsTriple = exists(
            statements,
            subject,
            predicate,
            object,
            false
            );
        
        if(containsTriple){
            fail(
                String.format(
                    "expected to NOT find triple [%s %s %s]", 
                    subject, 
                    predicate, 
                    object
                    )
                );
        }
    }
    
    public void assertContainsTripleLiteral(
            String subject, 
            String predicate, 
            Object object
            ){
        boolean containsTriple = exists(
            statements,
            subject,
            predicate,
            object,
            true
            );
        
        if(!containsTriple){
            fail(
                String.format(
                    "expected to find triple [%s %s %s (a %s)]", 
                    subject, 
                    predicate, 
                    object,
                    object == null ? "null" : object.getClass().getName()
                    )
                );
        }
    }
    
    public void assertDoesNotContainTripleLiteral(
            String subject, 
            String predicate, 
            Object object
            ){
        boolean containsTriple = exists(
            statements,
            subject,
            predicate,
            object,
            true
            );
        
        if(containsTriple){
            fail(
                String.format(
                    "expected to NOT find triple [%s %s %s (a %s)]", 
                    subject, 
                    predicate, 
                    object,
                    object == null ? "null" : object.getClass().getName()
                    )
                );
        }
    }

}
