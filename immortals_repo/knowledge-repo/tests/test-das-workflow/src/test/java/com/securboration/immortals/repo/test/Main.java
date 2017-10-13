package com.securboration.immortals.repo.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.repo.ontology.FusekiClient;

public class Main {
    
    public static void main(String[] args){
        
        FusekiClient client = new FusekiClient("http://localhost:3030/ds");
        
        List<String> graphNames = new ArrayList<>();
        
        for(int i=0;i<10;i++){
            graphNames.add(pushTestGraph(client));
        }
        
        for(String graphName:graphNames){
            Model graph = client.getModel(graphName);
            
            printModel(graphName,graph);
        }
        
        for(String graphName:client.getGraphNames()){
            Model graph = client.getModel(graphName);
            
            printModel(graphName,graph);
        }
    }
    
    
    
    private static int counter = 1;
    private static String pushTestGraph(FusekiClient client){
        
        final String tag = "testGraph"+counter;
        counter++;
        
        generateModel(tag);
        
        client.setModel(generateModel(tag), tag);
        
        return tag;
    }
    
    private static Model generateModel(String tag){
        Model model = ModelFactory.createDefaultModel();
        
        Resource r = model.createResource("http://securboration.com/test#" + tag);
        r.addProperty(RDF.type, RDFS.Class);
        r.addLiteral(RDFS.comment, "a test graph with tag " + tag);
        
        return model;
    }
    
    private static void printModel(String graphName,Model model){
        System.out.printf("printing graph [%s]\n", graphName);
        
        OntologyHelper.printTriples(model, System.out);
        
        System.out.println();
    }

}
