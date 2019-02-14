package com.securboration.immortals.repo.test.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.Assert;

import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.ontology.FusekiClient.ResultSetProcessor;

import junit.framework.TestCase;

public abstract class QueryTestBase extends TestCase {
    
    protected static final String DEFAULT_GRAPH = 
            "http://localhost:3030/ds/data/IMMoRTALS_r2.0.0";
    
    protected static final String VERSION = 
            "r2.0.0";
    
    protected static final String prefixes = 
            "PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#>\n"+
            "PREFIX dfu: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#>\n"+
            "PREFIX lp_func: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#LocationProvider>\n"+
            "PREFIX bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n"+
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
            "PREFIX lp: <http://launchpad.net/rdf/launchpad#>\n"+
            "\n";
    
    
    private FusekiClient client;
    private final Configuration configuration;
    
    public static class Configuration{
        private String fusekiDatasetUrl = "http://localhost:3030/ds";
        
        public void setFusekiDatasetUrl(String fusekiDatasetUrl) {
            this.fusekiDatasetUrl = fusekiDatasetUrl;
        }
        
        
    }
    
    public static class CleanupContext{
        private final Map<String,String> graphsToCleanup = 
                new LinkedHashMap<>();
        
        public void add(final String graphName,final String humanReadable){
            graphsToCleanup.put(graphName,humanReadable);
        }
        
        public Set<String> getGraphsToCleanup(){
            return new LinkedHashSet<>(graphsToCleanup.keySet());
        }
        
        protected Map<String,String> getGraphs(){
            return new LinkedHashMap<>(graphsToCleanup);
        }
    }
    
    protected static String generateUniqueUri(
            CleanupContext c,
            final String tag
            ){
        final String u = generateUniqueUri()+"-"+tag;
        
        c.add(u,tag);
        
        return u;
    }
    
    protected static String generateUniqueUri(){
        return "http://securboration.com/immortals/test-"+UUID.randomUUID().toString();
    }
    
    public QueryTestBase(Configuration c){
        super();
        super.setName(this.getClass().getName());
        this.configuration = c;
    }
    
    public QueryTestBase(){
        this(new Configuration());
    }
    
    protected synchronized FusekiClient acquireFusekiConnection(){
        if(client == null){
            client = new FusekiClient(configuration.fusekiDatasetUrl);
        }
        
        Assert.assertNotNull("fuseki client should not be null!",client);
        
        return client;
    }
    
    protected Configuration getConfiguration() {
        return configuration;
    }
    
    protected ResultSetProcessor assertSolutions(){
        return new ResultSetProcessor(){

            @Override
            public void processQuerySolution(QuerySolution s) {
            }
            
        };
    }
    
    protected static class AssertableSolutionSet implements ResultSetProcessor {
        
        private List<Solution> solutions = new ArrayList<>();
        
        public AssertableSolutionSet(){}
        
        public String printSolutions(){
            StringBuilder sb = new StringBuilder();
            
            sb.append(solutions.size() + " solutions:\n\t");
            
            int count = 1;
            for(Solution s:solutions){
                sb.append(s.toString().replace("\n", "\n\t"));
                
                if(count == 10){
                    final int remaining = solutions.size() - 10;
                    sb.append(remaining + " more solutions...\n");
                    break;
                }
                count++;
            }
            
            return sb.toString();
        }
        
        @Override
        public void processQuerySolution(QuerySolution s) {
            
            List<TupleValue> solution = new ArrayList<>();
            s.varNames().forEachRemaining(k->{
                final String v;
                RDFNode node = s.get(k);
                
                if(node.isLiteral()){
                    v = node.asLiteral().getLexicalForm();
                } else {
                    v = node.asResource().getURI();
                }
                
                solution.add(new TupleValue(k,v));
            });
            
            solutions.add(new Solution(solution));
        }
        
        public void assertContainsSolution(Solution findThis){
            assertContainsSolutions(findThis,0,null);
        }
        
        public void assertContainsSolutions(Collection<Solution> solutions){
            for(Solution s:solutions){
                assertContainsSolution(s);
            }
        }
        
        public void assertEqual(AssertableSolutionSet r){
            this.assertContainsSolutions(r.getSolutions());
            r.assertContainsSolutions(this.getSolutions());
        }
        
        public void assertContainsExactlyNSolutions(Solution findThis,final int n){
            assertContainsSolutions(findThis,n-1,n+1);
        }
        
        public void assertContainsAtLeastNSolutions(Solution findThis,final int n){
            assertContainsSolutions(findThis,n-1,null);
        }
        
        public void assertContainsAtMostNSolutions(Solution findThis,final int n){
            assertContainsSolutions(findThis,null,n+1);
        }
        
        private void assertContainsSolutions(
                Solution findThis,
                final Integer minExclusive,
                final Integer maxExclusive
                ){
            
            int count = 0;
            for(Solution s:solutions){
                if(findThis == null || findThis.matches(s)){
                    count++;
                }
            }
            
            if(minExclusive != null){
                Assert.assertTrue(
                    "Expected > " + minExclusive + " but found " + count,
                    count > minExclusive
                    );
            }
            
            if(maxExclusive != null){
                Assert.assertTrue(
                    "Expected < " + maxExclusive + " but found " + count,
                    count < maxExclusive
                    );
            }
        }
        
        public void assertSolutionsContainVariables(String...vars){
            
            Set<String> target = new HashSet<>(Arrays.asList(vars));
            
            for(Solution s:solutions){
                
                Set<String> discovered = new HashSet<>();
                
                for(TupleValue t:s.tuples){
                    if(target.contains(t.key)){
                        discovered.add(t.key);
                    }
                }
                
                Assert.assertTrue(
                    "expected to find " + target + 
                    " but actually found " + discovered,
                    !discovered.retainAll(target)
                    );
            }
        }

        
        protected List<Solution> getSolutions() {
            return solutions;
        }
        
        protected Set<String> getSolutionsForVar(final String key){
            Set<String> solutions = new LinkedHashSet<>();
            
            for(Solution s:this.solutions){
                solutions.add(s.get(key));
            }
            
            return solutions;
        }
        
        
    }
    
    protected static class Solution{
        private final List<TupleValue> tuples = new ArrayList<>();
        private final Map<String,String> map = new HashMap<>();
        
        protected Solution(String...kvs){
            
            if(kvs.length %2 != 0){
                Assert.fail(
                    "expected an even number of kvs but got " + kvs.length);
            }
            
            for(int i=0;i<kvs.length;i+=2){
                tuples.add(new TupleValue(kvs[i],kvs[i+1]));
            }
            
            index();
        }
        
        protected Solution(TupleValue...tuples){
            this.tuples.addAll(Arrays.asList(tuples));
            
            index();
        }
        
        private Solution(Collection<TupleValue> tuples){
            this.tuples.addAll(tuples);
            
            index();
        }
        
        protected String get(final String key){
//            Assert.assertTrue(
//                "found null but expected to find value for key " + key + 
//                ", valid keys = " + map.keySet(),
//                map.containsKey(key)
//                );
            
            return map.get(key);
        }
        
        private void index(){
            for(TupleValue t:tuples){
                map.put(t.key, t.value);
            }
        }
        
        private boolean matches(Solution solution){
            
            for(TupleValue mustContainThisTuple:tuples){
                
                boolean foundMatch = false;
                for(TupleValue candidateTuple:solution.tuples){
                    if(mustContainThisTuple.key.equals(candidateTuple.key)){
                        final String mustContainValue = mustContainThisTuple.value;
                        final String actualValue = candidateTuple.value;
                        
                        if(mustContainValue == null){
                            foundMatch = true;
                        } else if(mustContainValue.endsWith("*")){
                            foundMatch = 
                                    actualValue.startsWith(
                                        mustContainValue.substring(
                                            0,
                                            mustContainValue.length()-1
                                            )
                                        );
                        } else {
                            foundMatch = mustContainValue.equals(actualValue);
                        }
                        
                        if(foundMatch){
                            break;
                        }
                    }
                }
                
                if(!foundMatch){
                    return false;
                }
            }
            
            return true;
        }
        
        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            
            sb.append("solution {\n");
            for(TupleValue t:tuples){
                sb.append("\t").append(String.format("%-16s",t.key)).append(" : ").append(t.value).append("\n");
            }
            sb.append("}\n");
            
            return sb.toString();
        }
    }
    
    private static class TupleValue{
        private final String key;
        private final String value;
        
        protected TupleValue(String k, String v){
            this.key = k;
            this.value = v;
        }
        
        @Override
        public int hashCode(){
            return 17 + 13*(1337*key.hashCode() + value.hashCode());
        }
        
        @Override
        public boolean equals(Object o){
            if(!(o instanceof TupleValue)){
                return false;
            }
            
            TupleValue t = (TupleValue)o;
            
            if(!t.key.equals(this.key)){
                return false;
            }
            
            if(!t.value.equals(this.value)){
                return false;
            }
            
            return true;
        }
    }
    
    protected class Tuple{
        private Map<String,String> tuple;
        
        public Tuple(TupleValue...components){
            for(TupleValue t:components){
                final String k = t.key;
                final String v = t.value;
                
                if(tuple.containsKey(k)){
                    Assert.fail("duplicate definition of tuple key " + k);
                }
                
                tuple.put(k, v);
            }
        }
    }
    
    protected static String makeQuery(String query,String...bindings){
        for(int i=0;i<bindings.length;i+=2){
            final String var = bindings[i];
            final String binding = bindings[i+1];
            
            query = query.replace(var, binding);
        }
        
        return query;
    }
    
    protected static String createFromNamed(Collection<String> graphNames){
        return createFromNamed(graphNames.toArray(new String[]{}));
    }
    
    protected static String createFromNamed(String...graphNames){
        StringBuilder sb = new StringBuilder();
        
        for(String graph:graphNames){
            sb.append("FROM <");
            sb.append(graph);
            sb.append(">\n");
        }
        
        return sb.toString();
    }

}
