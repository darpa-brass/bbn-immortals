package com.securboration.immortals.repo.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

import com.securboration.immortals.j2t.analysis.JavaToOwl;
import com.securboration.immortals.j2t.analysis.JavaToTriplesConfiguration;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.repo.example.QueryExampleUriIssue.Pojos.User;
import com.securboration.immortals.repo.example.QueryExampleUriIssue.Pojos.Users;
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
public class QueryExampleUriIssue {
    
    //http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption/aes#AES_256
    
    @Ignore
    public static class Pojos{
        
        public static class Users{
            protected final List<User> users = new ArrayList<>();
        }
        
        public static class User{
            protected String firstName;
            protected String lastName;
            protected User[] friends;
        }
        
        public static class EnhancedUser extends User{
            protected String tag;
        }
        
        @ConceptInstance
        public static class TestUsers extends Users{
            public TestUsers(){
                this.users.add(new UserAlice());
                this.users.add(new UserBob());
                this.users.add(new UserCharles());
            }
        }
        
        @ConceptInstance
        public static class UserAlice extends User{
            public UserAlice(){
                this.firstName="alice";
                this.lastName=null;
                
                this.friends = new User[]{
                        new UserBob(),
                        new UserCharles()
                };
            }
        }
        
        @ConceptInstance
        public static class UserBob extends User{
            public UserBob(){
                this.firstName="bob";
                this.lastName=null;
                
                this.friends = new User[]{
                        new UserCharles()
                };
            }
        }
        
        @ConceptInstance
        public static class UserCharles extends EnhancedUser{
            public UserCharles(){
                this.firstName="charles";
                this.lastName=null;
                this.tag="engineer";
                this.friends = new User[]{
                };
            }
        }
        
    }
    
    @Test
    public void testPojoQuery() throws Exception{
        main(new String[]{});
    }
    
    public static void main(String[] args) throws Exception{
        
        final FusekiClient client = new FusekiClient("http://localhost:3030/ds");
        
        
        Set<String> cleanupThese = new HashSet<>();
        
        try{
            String contextGraph = createAnalysisContext(client);
            
            cleanupThese.add(contextGraph);
            
//            System.out.println(
//                OntologyHelper.serializeModel(
//                    client.getModel(contextGraph), 
//                    "TURTLE", 
//                    false
//                    )
//                );
            
//            {//query for all interesting individuals within the graph
//                client.executeSelectQuery(getIndividualsQuery(contextGraph), r->{
//                    final String individualUri = 
//                            r.get("individual").asResource().getURI();
//                    System.out.println(individualUri);
//                });
//            }
            
            new QueryExampleUriIssue().doTest(client, contextGraph);
        } finally {
            for(String graphName:cleanupThese){
                client.deleteModel(graphName);
            }
        }
    }
    
    private static Model getVocabulary() throws ClassNotFoundException{
        JavaToTriplesConfiguration c = new JavaToTriplesConfiguration("r2.0.0");
        
        JavaToOwl converter = new JavaToOwl(c);
        
        return converter.analyze(
            Arrays.asList(
                Pojos.Users.class,
                Pojos.User.class, 
                Pojos.EnhancedUser.class,
                
                Pojos.TestUsers.class,
                Pojos.UserAlice.class, 
                Pojos.UserBob.class
                )
            );
    }
    
    private static String createAnalysisContext(FusekiClient client) throws IOException, ClassNotFoundException{
        Model model = ModelFactory.createDefaultModel();
        
        model.add(getVocabulary());
        
        return client.setModel(
            model, 
            "test-"+UUID.randomUUID().toString()
            );
    }
    
    private void doTest(
            final FusekiClient client, 
            final String graphName
            ) throws Exception {
        
        ObjectToTriplesConfiguration c = 
                new ObjectToTriplesConfiguration("r2.0.0");
        
        //run a query that selects all User instances
        SparqlPojoContext queryResult = TriplesToPojo.sparqlSelect(
            getIndividualsQuery(graphName), 
            graphName, 
            client, 
            c
            );
        
        //iterate through results
        queryResult.forEach(solution ->{
            
            Users users = (Users)solution.get("individual");
            
            System.out.printf(
                "found users %s\n", 
                (String)solution.get("individual$uri")//black magic to get URI of var bound to query result
                );
            
            for(User u:users.users){
                System.out.printf(
                    "\t%s\n", 
                    c.getNamingContext().getNameForObject(u)//more black magic to get URI of arbitrary POJO
                    );
            }
            
            System.out.printf("found %d users\n", users.users.size());
            
            //create and add a new friendly user
            final User newUser;
            {
                User user = new User();
                user.firstName = "Zander";
                user.friends = new User[]{
                        new Pojos.UserAlice(),
                        new Pojos.UserBob(),
                        new Pojos.UserCharles(),
                        user
                };
                
                users.users.add(user);
                newUser = user;
            }
            
            //update model
            {
                Model newModel = ObjectToTriples.convert(
                    c,
                    users
                    );
                
                client.addToModel(newModel, graphName);
                
                try {
                    System.out.println(OntologyHelper.serializeModel(newModel, "TURTLE", false));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            
            
            //test consistency
            {
                final String originalUri = c.getNamingContext().getNameForObject(newUser);
                
                Assert.assertNotNull(originalUri);
                
                ObjectToTriplesConfiguration cc = 
                        new ObjectToTriplesConfiguration("r2.0.0");
                
                //run a query that selects zander
                AtomicBoolean first = new AtomicBoolean(true);
                TriplesToPojo.sparqlSelect(
                    getZanderQuery(graphName), 
                    graphName, 
                    client, 
                    cc
                    ).forEach(s->{
                        //ensure we only see one solution
                        Assert.assertTrue(first.get());
                        first.set(false);
                        
                        Object o = s.get("individual");
                        
                        System.out.println("original: " + originalUri);
                        System.out.println("recovered: " + cc.getNamingContext().getNameForObject(o));
                        
                        Assert.assertEquals(
                            originalUri, 
                            cc.getNamingContext().getNameForObject(o)
                            );
                    });
                
                Assert.assertFalse(first.get());
                
            }
            
            //sanity checks on serialization
            {
                assertAndPrintUsers(new ObjectToTriplesConfiguration("r2.0.0"), users, "expect triples A",1,1000);
                assertAndPrintUsers(c, users, "expect no triples",0,0);            
                assertAndPrintUsers(c.getCleanContext(true), users, "expect triples B",1,1000);
            }
        });
    }
    
    private static void assertAndPrintUsers(
            ObjectToTriplesConfiguration c,
            Users users, 
            String tag,
            final int minTripleCount,
            final int maxTripleCount
            ) {
        final String serialization;
        try {
            Model model = ObjectToTriples.convert(c, users);
            
            Assert.assertTrue(model.size() >= minTripleCount);
            Assert.assertTrue(model.size() <= maxTripleCount);
            
            serialization = OntologyHelper.serializeModel(
                model,
                "TURTLE",
                false
                );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        printString("\t"+tag,serialization);
    }
    
    private static void printString(String prefix, String text){
        System.out.println(prefix + "    " + text.replace("\n", "\n" + prefix + "    "));
        System.out.println();
    }
    
    /**
     * 
     * @param graphName
     * @return a query that returns all individuals within the indicated graph
     */
    private static String getIndividualsQuery(
            final String graphName
            ){
        String query = (
                "" +
                "SELECT ?individual WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        ?individual a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/repo/example#QueryExampleUriIssue.Pojos.Users> .\r\n" +
                "    } .\r\n" + 
                "}"
                ).replace("?GRAPH?", graphName);
        
        System.out.println(query);
        
        return query;
    }
    
    /**
     * 
     * @param graphName
     * @return a query that returns all individuals within the indicated graph
     */
    private static String getZanderQuery(
            final String graphName
            ){
        String query = (
                "" +
                "SELECT ?individual WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        ?individual a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/repo/example#QueryExampleUriIssue.Pojos.User> .\r\n" +
                "        ?individual <http://darpa.mil/immortals/ontology/r2.0.0#hasFirstName> \"Zander\" .\r\n" +
                "    } .\r\n" + 
                "}"
                ).replace("?GRAPH?", graphName);
        
        System.out.println(query);
        
        return query;
    }

}
