package com.securboration.immortals.repo.test.queries;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import com.securboration.immortals.repo.ontology.FusekiClient;

public class TestQueries extends QueryTestBase {
    
    
    
    
    public void testTransitiveTripleGeneration(){
        
        final String graph = this.generateUniqueUri()+"-graph";
        
        final String calls = this.generateUniqueUri()+"-calls";
        
        final String a = this.generateUniqueUri()+"-A";
        final String b = this.generateUniqueUri()+"-B";
        final String c = this.generateUniqueUri()+"-C";
        final String d = this.generateUniqueUri()+"-D";
        final String e = this.generateUniqueUri()+"-E";
        final String f = this.generateUniqueUri()+"-F";
        final String g = this.generateUniqueUri()+"-G";
        
        FusekiClient client = super.acquireFusekiConnection();
        
        try{
            //add the initial call edges
            {
                client.executeUpdate(
                    makeInsertTriple(graph,a,calls,b),
                    makeInsertTriple(graph,b,calls,c),
                    makeInsertTriple(graph,c,calls,d),
                    makeInsertTriple(graph,d,calls,e),
                    makeInsertTriple(graph,e,calls,f),
                    makeInsertTriple(graph,a,calls,g)
                    );
                
                verifyNumTriples(client,graph,6);
            }
            
            //construct transitive edges
            for(int i=0;i<10;i++)
            {
                final String insertQuery = super.makeQuery(
                    "INSERT\n"+
                    "  {\n"+
                    "      GRAPH <${GRAPH}> {\n"+
                    "        ?p <${CALLS}> ?q .\n"+
                    "      }\n"+
                    "  }\n"+
                    "WHERE\n"+
                    "  {\n"+
                    "      GRAPH <${GRAPH}> {\n"+
                    "        ?p <${CALLS}> ?f .\n"+
                    "        ?f <${CALLS}> ?q .\n"+
                    "      }\n"+
                    "  }\n"+
                    "\n",
                    "${GRAPH}",graph,
                    "${CALLS}",calls
                    );
                
                client.executeUpdate(insertQuery);
            }
            
            //verify that a calls f
            {
                final String query = super.makeQuery(
                    "SELECT DISTINCT ?s \n"+
                    "  WHERE {\n"+
                    "      GRAPH <${GRAPH}> {\n"+
                    "        ?s <${CALLS}> <${F}> .\n"+
                    "      }\n"+
                    "  }\n"+
                    "\n",
                    "${GRAPH}",graph,
                    "${CALLS}",calls,
                    "${F}",f
                    );
                
                final String validationQuery = super.makeQuery(
                    "SELECT DISTINCT ?s \n"+
                    "  WHERE {\n"+
                    "      GRAPH <${GRAPH}> {\n"+
                    "        ?s <${CALLS}>+ <${F}> .\n"+
                    "      }\n"+
                    "  }\n"+
                    "\n",
                    "${GRAPH}",graph,
                    "${CALLS}",calls,
                    "${F}",f
                    );
                
                AssertableSolutionSet queryResult = 
                        new AssertableSolutionSet();
                AssertableSolutionSet validationQueryResult = 
                        new AssertableSolutionSet();
                
                client.executeSelectQuery(query,queryResult);
                client.executeSelectQuery(validationQuery,validationQueryResult);
                
                validationQueryResult.assertEqual(queryResult);
                
                validationQueryResult.assertContainsExactlyNSolutions(null, 5);
            }
        } finally {
            client.deleteModel(graph);
            verifyNumTriples(client,graph,0);
        }
    }
    
    private static Model createTestModel(final int numTriples){
        Model model = ModelFactory.createDefaultModel();
        
        for(int i=0;i<numTriples;i++){
            Resource s = model.createResource(generateUniqueUri());
            Property p = model.createProperty(generateUniqueUri());
            Resource o = model.createResource(generateUniqueUri());
            
            model.add(s,p,o);
        }
        
        return model;
    }
    
    
    public void testCopyGraph(){
        
        
        final String oldGraphName = generateUniqueUri();
        final String newGraphName = generateUniqueUri();
        
        FusekiClient client = super.acquireFusekiConnection();
        
        try{
            client.setModel(createTestModel(4096), oldGraphName);
            
            final int originalTriples = getNumTriples(client,oldGraphName);
            
            client.copy(oldGraphName, newGraphName);
            
            verifyNumTriples(client,newGraphName,originalTriples);
        } finally {
            client.deleteModel(oldGraphName);
            client.deleteModel(newGraphName);
            verifyNumTriples(client,newGraphName,0);
        }
    }
    
    private String makeInsertTriple(
            final String graph, 
            final String s, 
            final String p, 
            final String o
            ){
        return super.makeQuery(
            "INSERT DATA\n"+
            "  {\n"+
            "      GRAPH <${GRAPH}> {\n"+
            "        <${S}> <${P}> <${O}> .\n"+
            "      }\n"+
            "  }\n"+
            "\n",
            "${PREFIXES}",prefixes,
            "${GRAPH}",graph,
            "${S}",s,
            "${P}",p,
            "${O}",o
            );
    }
    
    public void testInsertToExistingGraph(){
        
        final String graphName = this.generateUniqueUri();
        
        final String s = this.generateUniqueUri();
        final String p = this.generateUniqueUri();
        final String o = this.generateUniqueUri();
        
        final String insertQuery = super.makeQuery(
                "${PREFIXES}"+
                "INSERT DATA\n"+
                "  {\n"+
                "      GRAPH <${GRAPH}> {\n"+
                "        <${S}> <${P}> <${O}> .\n"+
                "      }\n"+
                "  }\n"+
                "\n",
                "${PREFIXES}",prefixes,
                "${GRAPH}",graphName,
                "${S}",s,
                "${P}",p,
                "${O}",o
                );
        
        final String o2 = this.generateUniqueUri();
        
        final String updateQuery = super.makeQuery(
            "${PREFIXES}"+
            "WITH <${GRAPH}>\n"+
            "DELETE { <${S}> <${P}> <${O}> . }\n"+
            "INSERT { <${S}> <${P}> <${O2}> . }\n"+
            "WHERE  { <${S}> <${P}> <${O}> . }\n"+
            "\n",
            "${PREFIXES}",prefixes,
            "${GRAPH}",graphName,
            "${S}",s,
            "${P}",p,
            "${O}",o,
            "${O2}",o2
            );
        
        FusekiClient client = super.acquireFusekiConnection();
        
        try{
            System.out.println(insertQuery);
            client.executeUpdate(insertQuery);
            verifyNumTriples(client,graphName,1);
            
            System.out.println(updateQuery);
            client.executeUpdate(updateQuery);
            verifyNumTriples(client,graphName,1);
        } finally {
            client.deleteModel(graphName);
            verifyNumTriples(client,graphName,0);
        }
    }
    
    public void testDeleteGraph(){
        testInsertToNewGraph();
    }
    
    public void testInsertToNewGraph(){
        
        final String newInstanceId = this.generateUniqueUri();
        
        FusekiClient client = super.acquireFusekiConnection();
        
        final String graphName = this.generateUniqueUri();
        
        final String query = super.makeQuery(
                "${PREFIXES}"+
                "INSERT DATA\n"+
                "  {\n"+
                "      GRAPH <${GRAPH}> {\n"+
                "        <${NEW_INSTANCE_ID}> a dfu:DfuInstance .\n"+
                "      }\n"+
                "  }\n"+
                "\n",
                "${PREFIXES}",prefixes,
                "${GRAPH}",graphName,
                "${NEW_INSTANCE_ID}",newInstanceId
                );
        
        System.out.println(query);
        
        try{
            client.executeUpdate(query);
            verifyNumTriples(client,graphName,1);
        } finally {
            client.deleteModel(graphName);
            verifyNumTriples(client,graphName,0);
        }
    }
    
    private int getNumTriples(
            final FusekiClient client,
            final String graphName
            ){
        AssertableSolutionSet q = new AssertableSolutionSet();
        
        final String query = super.makeQuery(
                "${PREFIXES}"+
                "SELECT DISTINCT ?s ?p ?o\n"+
                "  WHERE {\n"+
                "      GRAPH <${GRAPH}> {\n"+
                "        ?s ?p ?o .\n"+
                "      }\n"+
                "  }\n"+
                "\n",
                "${PREFIXES}",prefixes,
                "${GRAPH}",graphName
                );
        
        System.out.println(query);
        client.executeSelectQuery(query,q);
        System.out.println(q.printSolutions());
        
        return q.getSolutions().size();
    }
    
    private void verifyNumTriples(
            final FusekiClient client,
            final String graphName,
            final int numTriples
            ){
        AssertableSolutionSet q = new AssertableSolutionSet();
        
        final String query = super.makeQuery(
                "${PREFIXES}"+
                "SELECT DISTINCT ?s ?p ?o\n"+
                "  WHERE {\n"+
                "      GRAPH <${GRAPH}> {\n"+
                "        ?s ?p ?o .\n"+
                "      }\n"+
                "  }\n"+
                "\n",
                "${PREFIXES}",prefixes,
                "${GRAPH}",graphName
                );
        
        System.out.println(query);
        client.executeSelectQuery(query,q);
        System.out.println(q.printSolutions());
        
        q.assertContainsExactlyNSolutions(null,numTriples);
        
    }
    
    
    
    
    
    
    
    
    

}
