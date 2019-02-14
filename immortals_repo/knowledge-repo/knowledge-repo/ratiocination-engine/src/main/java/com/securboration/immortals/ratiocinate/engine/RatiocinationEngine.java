package com.securboration.immortals.ratiocinate.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.securboration.immortals.ontology.inference.AskQuery;
import com.securboration.immortals.ontology.inference.ConstructQuery;
import com.securboration.immortals.ontology.inference.InferenceRule;
import com.securboration.immortals.ontology.inference.InferenceRules;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.query.TriplesToPojo;


/**
 * A simple inferencing engine that operates on inference rules similar in
 * spirit to SPARQL Inferencing Notation (SPIN).
 * 
 * @author jstaples
 *
 */
public class RatiocinationEngine {
    
    private final Logger logger = LoggerFactory.getLogger(RatiocinationEngine.class);
    private final FusekiClient client;
    private final String graphName;
    
    /**
     * 
     * @param client
     *            a client to a Fuseki triple store
     * @param graphName
     *            the name of a graph that contains inference rules and over
     *            which those rules will operate
     */
    public RatiocinationEngine(FusekiClient client, String graphName) {
        super();
        this.client = client;
        this.graphName = graphName;
    }
    
    /**
     * Runs the inference engine.  This involves the following steps:
     * <ol>
     * <li>Retrieve all InferenceRules from the specified graph</li>
     * <li>Retrieve all InferenceRule values for each InferenceRules</li>
     * <li>Evaluate each rule, adding any generated triples to the graph</li>
     * </ol>
     * 
     * @return a report about the run
     */
    public RatiocinationReport execute(){
        RatiocinationReport report = new RatiocinationReport();

        int triplesAdded = 0;
        final long start = System.currentTimeMillis();
        int count = 0;
        for(InferenceRules r:getInferenceRules()){
            triplesAdded+=execute(report,r);
            count++;
        }
        final long elapsed = System.currentTimeMillis() - start;
        
        report.print("processed %d bags of rules in %dms\n", count, elapsed);
        report.setTriplesAdded(triplesAdded);
        
        return report;
    }
    
    private int execute(RatiocinationReport report, InferenceRules ruleset){

        int triplesAdded = 0;
        final long startTime = System.currentTimeMillis();
        report.print(
            "start time: %dms\n", 
            startTime
            );
        int iterations = 0;
        
        boolean halt = false;
        while(!halt){
            iterations++;

            report.print(
                "iteration %d\n", 
                iterations
                );
            final long iterationStart = System.currentTimeMillis();
            
            logger.info(String.format("iteration %d", iterations));
            final long initialCount = count();
            
            Set<InferenceRule> rulesFired = new HashSet<>();
            boolean handledAllRules = false;
            while(!handledAllRules){
                
                handledAllRules = true;
                
                for(InferenceRule rule:ruleset.getRules()){
                    final long ruleStart = System.currentTimeMillis();
                    
                    //determine whether this rule was previously evaluated
                    {
                        if(rulesFired.contains(rule)){
                            continue;
                        }
                    }
                    
                    //determine whether all fired rules are met
                    {
                        Set<InferenceRule> requirements = 
                                new HashSet<>(rule.getExplicitPrecondition());
                        
                        requirements.removeAll(rulesFired);
                        
                        if(requirements.size() > 0){
                            //still need to fire some other rule, skip this one
                            continue;
                        }
                    }
                    
                    //determine whether all predicates are met
                    {
                        boolean predicatesMet = true;
                        for(AskQuery a:rule.getPredicate()){
                            final String query = a.getQueryText();
                            
                            boolean result = client.executeAskQuery(
                                resolveVariables(query)
                                );
                            
                            if(!result){
                                predicatesMet = false;
                            }
                        }
                        
                        if(!predicatesMet){
                            //a predicate is not met, preventing this rule from
                            //firing
                            continue;
                        }
                    }
                    
                    rulesFired.add(rule);
                    
                    final ConstructQuery ruleQuery = 
                            rule.getForwardInferenceRule();
                    
                    if(ruleQuery == null){
                        logger.warn(
                            String.format(
                                "found an inference construct with no " +
                                "specified forward inference rule"
                                )
                            );
                        continue;
                    }
                    
                    final String query = resolveVariables(ruleQuery.getQueryText());
                    
                    Model newTriples = client.executeConstructQuery(query);
                    
                    report.print(">>%s\n", query);
                    
                    report.print(
                        "\trule [%s] fired in %dms, producing %d triples\n", 
                        rule.getHumanReadableDesc(),
                        System.currentTimeMillis() - ruleStart,
                        newTriples.size()
                        );

                    triplesAdded+=newTriples.getGraph().size();
                    client.addToModel(newTriples, graphName);
                    
                    handledAllRules = false;
                }
            }//end loop over all rules
            
            halt = true;
            
            //handle halting criteria (determine whether we should re-run all 
            //rules)
            {
                final long afterCount = count();
                final long elapsed = System.currentTimeMillis() - startTime;
                
                final boolean foundNewTriples = 
                        ruleset.getIterateUntilNoNewTriples() == null ? false : afterCount > initialCount;
                final boolean underTimeLimit = 
                        ruleset.getMaxTimeMillis() == null ? true : elapsed < ruleset.getMaxTimeMillis();
                final boolean underIterationLimit = 
                        ruleset.getMaxIterations() == null ?  true : iterations < ruleset.getMaxIterations();
                
                //keep going so long as no halting criterion is violated
                if(foundNewTriples && underTimeLimit && underIterationLimit){
                    halt = false;
                }
                
                report.print("\t*\n");
                report.print(
                    "\titeration produced %d new triples\n", 
                    afterCount - initialCount
                    );
                report.print(
                    "\tfound new triples? %s\n", 
                    foundNewTriples
                    );
                report.print(
                    "\tunder time limit? %s\n", 
                    underTimeLimit
                    );
                report.print(
                    "\tunder iteration limit? %s\n", 
                    underIterationLimit
                    );
                report.print(
                    "\thalt? %s\n", 
                    halt
                    );
            }
            

            report.print(
                "\titeration %d took %dms\n", 
                iterations,
                System.currentTimeMillis() - iterationStart
                );
        }//end loop until halting criteria
        
        report.print(
            "done processing ruleset in %dms\n", 
            System.currentTimeMillis() - startTime
            );

        return triplesAdded;
    }
    
    private String resolveVariables(String query){
        return query.replace("?GRAPH?", client.getFusekiServiceDataUrl() + "/" + graphName);
    }
    
    /**
     * 
     * @return any InferenceRules contained in the graph
     */
    public Iterable<InferenceRules> getInferenceRules(){
        
        final Set<String> uris = new LinkedHashSet<>();
        
        //get URIs for all inference rules
        client.executeSelectQuery(
            getQueryForIndividualsOfType(graphName,InferenceRules.class), 
            (s)->{
                uris.add(s.get("x").asResource().getURI());
            }
            );
        
        logger.info(
            String.format(
                "found %d rulesets: %s\n", 
                uris.size(), 
                uris
                )
            );
        
        //build an interable structure
        return new Iterable<InferenceRules>(){
            
            final Iterator<String> uriIterator = uris.iterator();

            @Override
            public Iterator<InferenceRules> iterator() {
                
                return new Iterator<InferenceRules>(){

                    @Override
                    public boolean hasNext() {
                        return uriIterator.hasNext();
                    }

                    @Override
                    public InferenceRules next() {
                        
                        if(!uriIterator.hasNext()){
                            throw new NoSuchElementException();
                        }
                        
                        final String uri = uriIterator.next();
                        
                        logger.info(
                            String.format(
                                "retrieving ruleset %s\n",
                                uri
                                )
                            );
                        
                        try {
                            return (InferenceRules) TriplesToPojo.convert(graphName, uri, client);
                        } catch (ClassNotFoundException | InstantiationException
                                | IllegalAccessException | NoSuchFieldException
                                | SecurityException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    
                };
            }
            
        };
    }
    
    private long count(){
        
        final List<Long> counts = new ArrayList<>();
        client.executeSelectQuery(getCountQuery(graphName), result->{
            final long count = result.getLiteral("count").getLong();
            counts.add(count);
        });
        
        if(counts.size() != 1){
            throw new RuntimeException("expected 1 but got " + counts.size());
        }
        
        return counts.get(0);
    }
    
    private static String getCountQuery(
            final String graphName
            ){
        String q = (
                "SELECT (count(*) as ?count) WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        ?x ?p ?o .\r\n" +
                "    } .\r\n" + 
                "}"
                )
                .replace("?GRAPH?", graphName)
                ;
        
        return q;
    }
    
    private static String getQueryForIndividualsOfType(
            final String graphName, 
            final Class<?> type
            ){
        String q = (
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "SELECT ?x WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        ?x a ?y .\r\n" +
                "        ?y <http://www.w3.org/2000/01/rdf-schema#:subClassOf>* ?z .\r\n" +
                "        ?z IMMoRTALS:hasPojoProvenance \"?TYPE_NAME?\" .\r\n" +
                "    } .\r\n" + 
                "}"
                )
                .replace("?GRAPH?", graphName)
                .replace("?TYPE_NAME?",type.getTypeName())
                ;
        
        return q;
    }

}
