package com.securboration.immortals.repo.test.queries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDFS;

import com.securboration.immortals.j2s.mapper.PojoMappingContext;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.cp2.Analysis;
import com.securboration.immortals.ontology.cp2.DomainKnowledge;
import com.securboration.immortals.ontology.cp2.ScalingFactorMeasurements;
import com.securboration.immortals.ontology.gmei.GmeInterchangeFormatUberExample;
import com.securboration.immortals.ontology.metrics.Metric;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.uris.Immortals;

public class TestDasWorkflow2 extends QueryTestBase {
    
    public void testDasWorkflow() throws IOException{
        
        final CleanupContext cleanup = new CleanupContext();
        final FusekiClient client = super.acquireFusekiConnection();
        
        try{
            workflow(client,cleanup);
        }finally{
            for(String graphToCleanup:cleanup.getGraphsToCleanup()){
                client.deleteModel(graphToCleanup);
            }
        }
        
    }
    
    private static void writeGraph(
            final Model m, 
            final String graphName, 
            final String niceName
            ) throws IOException{
        final String s = OntologyHelper.serializeModel(m, "TURTLE", false);
        
        FileUtils.writeStringToFile(
            new File(niceName), 
            "graph " + graphName + "\n" + s
            );
    }
    
    private static Model getKnowledge(Class<?> c){
        PojoMappingContext mappingContext = 
                PojoMappingContext.acquireContext(VERSION);
        
        for(Object o:instantiate(c)){
            mappingContext.addToModel(o);
        }
        
        return mappingContext.getCurrentModel();
    }
    
    private static Object instantiateInternal(Class<?> c){
        
        System.out.println("instantiating " + c.getName());
        
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static List<Object> instantiate(Class<?> wrapper){
        List<Object> objects = new ArrayList<>();
        
        if(wrapper.getAnnotation(ConceptInstance.class) != null){
            objects.add(instantiateInternal(wrapper));
        }
        
        for(Class<?> c:wrapper.getClasses()){
            if(c.getAnnotation(ConceptInstance.class) == null){
                continue;
            }
            if(c.getAnnotation(Ignore.class) != null){
                continue;
            }
            
            objects.add(instantiateInternal(c));
        }
        
        return objects;
    }
    
    private static final String VIOLATIONS = 
            "http://darpa.mil/immortals/ontology/r2.0.0/violations#";
    private static final String REMEDIATIONS = 
            "http://darpa.mil/immortals/ontology/r2.0.0/remediations#";
    
//  http://darpa.mil/immortals/ontology/r2.0.0/violations#>\n"+
//  "\n"+
//  "INSERT {\n"+
//  "    GRAPH ${CONSTRAINT_GRAPH} {\n"+
//  "        ?newSubject ${RdfsComment} \"a constraint violation report built dynamically from a CONSTRUCT rule operating on ${UBER_GRAPH}\" .\n"+
//  "        ?newSubject a v:ConstraintViolationReport .\n"+
//  "        ?newSubject v:hasViolatedSpec ?spec .\n"+
//  "        ?newSubject v:hasViolatedSpec ?spec .\n"+
//  "        ?newSubject v:hasViolatingMeasurement ?measurement .\n"+
//  "        ?newSubject v:hasDirection ${DIRECTION} .\n"+
//  "        ?newSubject v:hasViolatedProperty ?property .\n"+
//  "        ?newSubject v:hasRemedialEffectOnProperty ${STRATEGY} .\n"+
    
    private static enum QueryHelper{
        
        //TODO
        
        Ratiocinate(REMEDIATIONS+"Ratiocinate"),
        
        HasQuery(REMEDIATIONS+"Ratiocinate"),
        
        Solution(REMEDIATIONS+"Solution"),
        HasSolutionAction(REMEDIATIONS+"hasSolutionAction"),
        HasSolutionSubject(REMEDIATIONS+"hasSolutionSubject"),
        HasDesiredAction(REMEDIATIONS+"hasDesiredAction"),
        HasDesiredSubject(REMEDIATIONS+"hasDesiredSubject"),
        
        RdfsComment(RDFS.comment.getURI()),
        
        PredictiveCauseEffectAssertion(Immortals.immortals_core.PredictiveCauseEffectAssertion$),
        PrescriptiveCauseEffectAssertion(Immortals.immortals_core.PrescriptiveCauseEffectAssertion$),
        ProscriptiveCauseEffectAssertion(Immortals.immortals_core.ProscriptiveCauseEffectAssertion$),
        MissionSpec(Immortals.immortals_cp.MissionSpec$),
        Metric(Immortals.immortals_cp.Metric$),
        ConstraintViolationReport(VIOLATIONS+"ConstraintViolationReport"),
        
        HasSequentialIndex(REMEDIATIONS+"hasSequentialIndex"),
        HasViolatingMeasurement(VIOLATIONS+"hasViolatingMeasurement"),
        HasViolatedProperty(VIOLATIONS+"hasViolatedProperty"),
        HasRemedialEffectOnProperty(VIOLATIONS+"hasRemedialEffectOnProperty"),
        HasDirection(VIOLATIONS+"hasDirection"),
        HasViolatedSpec(VIOLATIONS+"hasViolatedSpec"),
        HasInvokedAspect(Immortals.immortals_core.hasInvokedAspect$),
        HasProperty(Immortals.immortals_core.hasProperty$),
        HasImpact(Immortals.immortals_core.hasImpact$),
        HasImpactOnProperty(Immortals.immortals_core.hasImpactOnProperty$),
        HasImpactedProperty(Immortals.immortals_core.hasImpactedProperty$),
        HasConstraint(Immortals.immortals_core.hasConstraint$),
        HasCriterion(Immortals.immortals_core.hasCriterion$),
        HasHumanReadableForm(Immortals.immortals_cp.hasHumanReadableForm$),
        HasHumanReadableDesc(Immortals.immortals_cp.hasHumanReadableDescription$),
        HasRightValue(Immortals.immortals_cp.hasRightValue$),
        HasUnit(Immortals.immortals_core.hasUnit$),
        HasValue(Immortals.immortals_core.hasValue$),
        HasApplicableResourceType(Immortals.immortals_core.hasApplicableResourceType$),
        HasAssertionCriterion(Immortals.immortals_cp.hasAssertionCriterion$),
        HasMeasurementType(Immortals.immortals_core.hasMeasurementType$),
        HasCorrespondingProperty(Immortals.immortals_core.hasCorrespondingProperty$),
        ;
        
        
        
        private final String value;
        
        private QueryHelper(String v){
            this.value = v;
        }
        
        private static String buildQuery(
                String query,
                String[]vars,
                String...otherVars
                ){
            
            String[] newVars = new String[vars.length+otherVars.length];
            
            System.arraycopy(vars, 0, newVars, 0, vars.length);
            System.arraycopy(otherVars, 0, newVars, vars.length, otherVars.length);
            
            return buildQuery(query,newVars);
        }
        
        private static String buildQuery(
                String query,
                String...otherVars
                ){
            
            for(int i=0;i<otherVars.length;i+=2){
                if(otherVars[i] == null){
                    throw new RuntimeException(
                        "keys cannot be null");
                }
                
                if(otherVars[i+1] == null){
                    throw new RuntimeException(
                        "value for key " + otherVars[i] + " is null");
                }
            }
            
            for(String v:otherVars){
                
            }
            
            for(QueryHelper q:QueryHelper.values()){
                query = query.replace("${"+q.name()+"}", "<"+q.value+">");
            }
            
            for(int i=0;i<otherVars.length;i+=2){
                
                if(!otherVars[i+1].startsWith("http://")){
                    query = query.replace(otherVars[i], otherVars[i+1]);
                } else {
                    query = query.replace(otherVars[i], "<"+otherVars[i+1]+">");
                }
            }
            
            return query;
        }
    }
    
    private void workflow(
            final FusekiClient client,
            final CleanupContext context
            ) throws IOException{
        
        //write knowledge to graphs that can later be safely cleaned up
        {
            client.copy(
                DEFAULT_GRAPH, 
                super.generateUniqueUri(context,"bytecode-analysis")
                );
            client.setModel(
                getKnowledge(DomainKnowledge.class), 
                super.generateUniqueUri(context,"domain-knowledge")
                );
            client.setModel(
                getKnowledge(ScalingFactorMeasurements.class), 
                super.generateUniqueUri(context,"scaling-factor-metrics-gathered")
                );
            client.setModel(
                getKnowledge(Analysis.class), 
                super.generateUniqueUri(context,"dataflow-analysis")
                );
            client.setModel(
                getKnowledge(GmeInterchangeFormatUberExample.class), 
                super.generateUniqueUri(context,"gme-output")
                );
        }
        
        final String uberGraph = 
                super.generateUniqueUri(context,"uber-graph");
        
        //populate the uber graph
        {
            for(String graphName:context.getGraphsToCleanup()){
                
                if(graphName.equals(uberGraph)){
                    continue;
                }
                
                client.addToModel(client.getModel(graphName), uberGraph);
            }
        }
        
//        final String constraintViolationGraph = 
//                super.generateUniqueUri(context,"constraints-violated");
//        
//        final String remediationGraph = 
//                super.generateUniqueUri(context,"remediation-graph");
//        
//        //build up the constraint violations
//        {
//            Workflow.constructViolationTriples(
//                client,
//                uberGraph,
//                constraintViolationGraph
//                );
//            
//            Workflow.runSynthesisReasoning(
//                client, 
//                uberGraph, 
//                constraintViolationGraph, 
//                remediationGraph
//                );
//        }
        
        //logging
        {
            Map<String,String> graphNamesToTags = context.getGraphs();
            
            for(final String graphName:graphNamesToTags.keySet()){
                final String tag = graphNamesToTags.get(graphName);
                
                writeGraph(
                    client.getModel(graphName),
                    graphName,
                    "workflow/"+tag+".ttl"
                    );
            }
        }
        
//        System.exit(-1);//TODO
        
        //now run the workflow
//        {
//            Workflow.logMessage("\nstarting example DAS workflow...\n");
//            
//            final String allGraphs = 
//                    createFromNamed(context.getGraphs().keySet());
//            
//            final List<String> missionSpecs = 
//                    Workflow.getMissionSpecs(
//                        client, 
//                        "${GRAPH}",allGraphs
//                        );
//            
//            for(String spec:missionSpecs){
//                Workflow.getMeasurementsExceedingSpec(
//                    client, 
//                    spec, 
//                    "${GRAPH}",allGraphs
//                    );
//            }
//        }
        
        {
            System.out.println("summary of queries used during sample DAS workflow:\n");
            System.out.println(Workflow.queries.toString());
            
            FileUtils.writeStringToFile(
                new File("workflow/queries.trace"), 
                Workflow.queries.toString()
                );
        }
    }
    
    private static class Workflow{
        
        private static StringBuilder queries = 
                new StringBuilder("summary of queries used during sample DAS workflow:\n");
        
        private static void logMessage(final String message){
            System.out.println(message);
        }
        
        private static void logQuery(
                final String query,
                final String purpose
                ){
            
            final String s = "query \""+purpose+"\"\n  " + query.replace("\n", "\n  ");
            
            System.out.println(s);
            
            queries.append(s).append("\n");
        }
        
        private static void logResults(
                final AssertableSolutionSet result
                ){
            System.out.println("has " + result.getSolutions().size() + " solutions:\n");
            for(Solution s:result.getSolutions()){
                System.out.println("  "+s.toString().replace("\n", "\n  "));
            }
        }
        
        private static AssertableSolutionSet executeQuery(
                final FusekiClient client,
                final String query,
                final String purpose
                ){
            logQuery(query,purpose);
            
            AssertableSolutionSet solutionSet = new AssertableSolutionSet();
            client.executeSelectQuery(query,solutionSet);
            
            logResults(solutionSet);
            
            return solutionSet;
        }
        
//        private static String getClassImplementingAspect(
//                final FusekiClient client,
//                final String aspect,
//                final String...vars
//                ){
//            logMessage("retrieving DFU class implementing " + aspect);
//            
//            final String query = QueryHelper.buildQuery(
//                "SELECT DISTINCT ?unit ?resourceType\n"+
//                "${GRAPH}"+
//                "WHERE {\n"+
//                "    <${SPEC}> ${HasRightValue} ?r .\n"+
//                "    ?r ${HasUnit} ?unit .\n"+
//                "    ?r ${HasApplicableResourceType} ?resourceType .\n"+
//                "}\n"+
//                "\n",
//                vars,
//                "${SPEC}",missionSpec
//                );
//            
//            executeQuery(client,query);
//            
//            return null;//TODO
//        }
        
        private static Metric getMeasurementsExceedingSpec(
                final FusekiClient client,
                final String missionSpec,
                final String...vars
                ){
            logMessage("searching for measured values that OVERSHOOT missionSpec " + missionSpec);
            
            final String queryBase = 
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                "SELECT DISTINCT ?limit ?unitOfMeasure ?value ?invariant ?property\n"+
                "${GRAPH}"+
                "WHERE {\n"+
                "    ${SPEC} ${HasHumanReadableForm} ?human_readable .\n"+
                "    ${SPEC} ${HasAssertionCriterion} ${OPERATOR} .\n"+
                "    ${SPEC} ${HasAssertionCriterion} ?invariant .\n"+
                "    ${SPEC} ${HasRightValue} ?rValue .\n"+
                "    ?rValue ${HasUnit} ?unitOfMeasure .\n"+
                "    ?rValue ${HasValue} ?limit .\n"+
                "    ?rValue ${HasApplicableResourceType} ?specResourceType .\n"+
                "    ?rValue ${HasMeasurementType} ?measurementType .\n"+
                "    ?measurementType ${HasCorrespondingProperty} ?property .\n"+
                "\n"+
                "    ?measurement a ${Metric} .\n"+
                "    ?measurement ${HasApplicableResourceType} ?specResourceType .\n"+
                "    ?measurement ${HasValue} ?value .\n"+
                "    ?measurement ${HasMeasurementType} ?measurementType .\n"+
                "    OPTIONAL { ?x ${HasRightValue} ?measurement . }\n"+
                "\n"+
                "    FILTER (!BOUND (?x)) .\n"+
                "\n"+
                "    ${FILTER} .\n"+
                "}\n"+
                "\n";
            
            final String getExceededLimits = QueryHelper.buildQuery(
                queryBase,
                vars,
                "${SPEC}",missionSpec,
                "${OPERATOR}","\"VALUE_LESS_THAN_INCLUSIVE\"",
                "${FILTER}","FILTER (xsd:double(?value) > xsd:double(?limit))"
                );
            
            List<Solution> results = 
                    executeQuery(
                        client,
                        getExceededLimits,
                        "find measured values that exceed mission spec"
                        ).getSolutions();
            
            for(Solution s:results){
                final String property = s.get("property");
                
                List<Solution> strategyChain = getMitigationStrategy(
                    new ArrayList<>(Arrays.asList(s)),
                    client,
                    "\"PROPERTY_DECREASES\"",
                    property,
                    vars
                    );
                
                
                if(strategyChain != null){
                    
                    Solution lastSolution = 
                            strategyChain.get(strategyChain.size()-1);
                    
                    final String aspect = 
                            lastSolution.get("invokedAspect");
                    
                    //TODO
                    
                    logMessage(
                        String.format(
                            "the following %d steps illustrate a strategy for " +
                            "resolving an overshoot on %s:",
                            strategyChain.size(),
                            s.get("property")
                            )
                        );
                    
                    int count = 0;
                    for(Solution step:strategyChain){
                        
                        String suffix = "";
                        if(count == 0){
                            suffix = " NOTE: this is the initial constraint violation\n";
                        } else if (count == strategyChain.size()-1){
                            suffix = " NOTE: this is the functional aspect we should invoke to resolve this issue\n";
                        } else {
                            suffix = " NOTE: this is explicitly encoded as domain knowledge\n";
                        }
                        
                        logMessage("    "+step.toString().trim().replace("\n", "\n    ")+suffix);
                        count++;
                    }
                } else {
                    logMessage(
                        String.format(
                            "no strategy found for " +
                            "resolving an overshoot on %s:", 
                            s.get("property")
                            )
                        );
                }
            }
            
            return null;
        }
        
        

        
//      <http://darpa.mil/immortals/ontology/r2.0.0/violations#ViolationReport-c93d9b90-fde1-4315-b181-f7123f731f75>
//          a       <http://darpa.mil/immortals/ontology/r2.0.0/violations#ConstraintViolationReport> ;
//          <http://www.w3.org/2000/01/rdf-schema#comment>
//                  "a constraint violation report built dynamically from a CONSTRUCT rule operating on <http://securboration.com/immortals/test-e85eea87-44f1-42f9-83f0-90bb6d3d611a-uber-graph>" ;
//          <http://darpa.mil/immortals/ontology/r2.0.0/violations#hasDirection>
//                  "MEASURED_VALUE_UNDERSHOOTS_LOWER_LIMIT" ;
//          <http://darpa.mil/immortals/ontology/r2.0.0/violations#hasRemedialEffectOnProperty>
//                  "INCREASES" ;
//          <http://darpa.mil/immortals/ontology/r2.0.0/violations#hasViolatedProperty>
//                  <http://darpa.mil/immortals/ontology/r2.0.0/connectivity#PliReportRate> ;
//          <http://darpa.mil/immortals/ontology/r2.0.0/violations#hasViolatedSpec>
//                  <http://darpa.mil/immortals/ontology/r2.0.0/cp#MissionSpec-55a67e5c-b1c3-42c6-9fec-5d4a5ee7d52b> ;
//          <http://darpa.mil/immortals/ontology/r2.0.0/violations#hasViolatingMeasurement>
//                  <http://darpa.mil/immortals/ontology/r2.0.0/cp2#Measurements.InsufficientPliReportRatemeasurement3> .
      
        
        private static void runSynthesisReasoning(
                final FusekiClient client,
                final String uberGraph,
                final String constraintViolationGraph,
                final String remediationGraph,
                final String...vars
                ){
            final String getThingsThatShouldBeFixed = QueryHelper.buildQuery(
                "SELECT DISTINCT ?fixRequiredForProperty ?property \n"+
                "FROM ${CV_GRAPH} \n"+
                "WHERE {\n"+
                "    ?cv a ${ConstraintViolationReport} .\n"+
                "    ?cv ${HasRemedialEffectOnProperty} ?fixRequiredForProperty .\n"+
                "    ?cv ${HasViolatedProperty} ?property .\n"+
                "}\n"+
                "\n",
                vars,
                "${CV_GRAPH}",constraintViolationGraph
                );
            
            List<Solution> thingsThatShouldBeFixed = 
                    executeQuery(
                        client,
                        getThingsThatShouldBeFixed,
                        "retrieve things that should be fixed"
                        ).getSolutions();
            
            final Map<String,String> fixes = 
                    new HashMap<>();
            
            for(Solution s:thingsThatShouldBeFixed){
                
                final String requiredImpact = 
                        s.get("fixRequiredForProperty");
                
                final String property = 
                        s.get("property");
                
                final String existingImpact = 
                        fixes.get(property);
                
                if(existingImpact != null && !existingImpact.equals(requiredImpact)){
                    throw new RuntimeException(
                        "unable to reconcile required impacts " + 
                        requiredImpact + " and " + existingImpact
                        );
                }
                
                fixes.put(property, requiredImpact);
            }
            
            for(final String property:fixes.keySet()){
                final String fix = "\""+fixes.get(property)+"\"";
                
                final List<Solution> solutionChain = 
                        getMitigationStrategy(
                            new ArrayList<>(),
                            client,
                            fix,
                            property,
                            "${GRAPH}","FROM <" + uberGraph + ">"
                            );
                
                if(solutionChain == null){
                    //TODO
                    continue;
                }
                
                int index = 0;
                for(Solution s:solutionChain){
                    
                    addRemediationStrategy(
                        client,
                        remediationGraph,
                        fix,
                        property,
                        index++,
                        null,
                        s.get("actionToTake"),
                        s.get("onProperty"),
                        s.get("invokedAspect"),
                        s.get("humanReadableStatement")
                        );
                }
            }
        }
        
        private static void constructViolationTriples(
                final FusekiClient client,
                final String uberGraph,
                final String constraintViolationGraph,
                final String...vars
                ){
            logMessage(
                "constructing constraint violation triples into: " + constraintViolationGraph
                );
            
            final Map<String,String[]> checks = new HashMap<>();
            
            checks.put(
                "exceeds_exclusive", 
                new String[]{
                        "${UBER_GRAPH}",uberGraph,
                        "${CONSTRAINT_GRAPH}",constraintViolationGraph,
                        "${OPERATOR}","\"VALUE_LESS_THAN_INCLUSIVE\"",
                        "${FILTER}","FILTER (xsd:double(?value) > xsd:double(?limit))",
                        "${DIRECTION}","\"MEASURED_VALUE_EXCEEDS_UPPER_LIMIT\"",
                        "${STRATEGY}","\"PROPERTY_DECREASES\""
                });
            checks.put(
                "exceeds_inclusive", 
                new String[]{
                        "${UBER_GRAPH}",uberGraph,
                        "${CONSTRAINT_GRAPH}",constraintViolationGraph,
                        "${OPERATOR}","\"VALUE_LESS_THAN_EXCLUSIVE\"",
                        "${FILTER}","FILTER (xsd:double(?value) >= xsd:double(?limit))",
                        "${DIRECTION}","\"MEASURED_VALUE_EXCEEDS_UPPER_LIMIT\"",
                        "${STRATEGY}","\"PROPERTY_DECREASES\""
                });
            checks.put(
                "undershoot_exclusive", 
                new String[]{
                        "${UBER_GRAPH}",uberGraph,
                        "${CONSTRAINT_GRAPH}",constraintViolationGraph,
                        "${OPERATOR}","\"VALUE_GREATER_THAN_INCLUSIVE\"",
                        "${FILTER}","FILTER (xsd:double(?value) < xsd:double(?limit))",
                        "${DIRECTION}","\"MEASURED_VALUE_UNDERSHOOTS_LOWER_LIMIT\"",
                        "${STRATEGY}","\"INCREASES\""
                });
            checks.put(
                "undershoot_inclusive", 
                new String[]{
                        "${UBER_GRAPH}",uberGraph,
                        "${CONSTRAINT_GRAPH}",constraintViolationGraph,
                        "${OPERATOR}","\"VALUE_GREATER_THAN_EXCLUSIVE\"",
                        "${FILTER}","FILTER (xsd:double(?value) <= xsd:double(?limit))",
                        "${DIRECTION}","\"MEASURED_VALUE_UNDERSHOOTS_LOWER_LIMIT\"",
                        "${STRATEGY}","\"INCREASES\""
                });
            
            final String queryBase = 
                    "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"+
                    "\n"+
                    "INSERT {\n"+
                    "    GRAPH ${CONSTRAINT_GRAPH} {\n"+
                    "        ?newSubject ${RdfsComment} \"a constraint violation report built dynamically from a CONSTRUCT rule operating on ${UBER_GRAPH}\" .\n"+
                    "        ?newSubject a ${ConstraintViolationReport} .\n"+
                    "        ?newSubject ${HasViolatedSpec} ?spec .\n"+
                    "        ?newSubject ${HasViolatingMeasurement} ?measurement .\n"+
                    "        ?newSubject ${HasDirection} ${DIRECTION} .\n"+
                    "        ?newSubject ${HasViolatedProperty} ?property .\n"+
                    "        ?newSubject ${HasRemedialEffectOnProperty} ${STRATEGY} .\n"+
                    "    }\n"+
                    "}\n"+
                    "WHERE {\n"+
                    "    GRAPH ${UBER_GRAPH} {\n"+
                    "        ?spec a ${MissionSpec} .\n"+
                    "        ?spec ${HasHumanReadableForm} ?human_readable .\n"+
                    "        ?spec ${HasAssertionCriterion} ${OPERATOR} .\n"+
                    "        ?spec ${HasAssertionCriterion} ?invariant .\n"+
                    "        $spec ${HasRightValue} ?rValue .\n"+
                    "        ?rValue ${HasUnit} ?unitOfMeasure .\n"+
                    "        ?rValue ${HasValue} ?limit .\n"+
                    "        ?rValue ${HasApplicableResourceType} ?specResourceType .\n"+
                    "        ?rValue ${HasMeasurementType} ?measurementType .\n"+
                    "        ?measurementType ${HasCorrespondingProperty} ?property .\n"+
                    "\n"+
                    "        ?measurement a ${Metric} .\n"+
                    "        ?measurement ${HasApplicableResourceType} ?specResourceType .\n"+
                    "        ?measurement ${HasValue} ?value .\n"+
                    "        ?measurement ${HasMeasurementType} ?measurementType .\n"+
                    "\n"+
                    "        OPTIONAL { ?x ${HasRightValue} ?measurement . }\n"+
                    "\n"+
                    "        BIND(URI(CONCAT(\"http://darpa.mil/immortals/ontology/r2.0.0/violations#ViolationReport-\",STRUUID())) AS ?newSubject) .\n"+
                    "\n"+
                    "        FILTER (!BOUND (?x)) .\n"+//x only binds if this is a virtual measurement, we want to filter these
                    "\n"+
                    "        ${FILTER} .\n"+
                    "    }\n"+
                    "}\n"+
                    "\n";
            
            for(String key:checks.keySet()){
                String[] additionalVars = checks.get(key);
                    
                final String query = QueryHelper.buildQuery(
                    queryBase,
                    vars,
                    additionalVars
                    );
                
                logQuery(
                    query,
                    "retrieve measurements that " + key + " mission specs and insert a summary of the violation into a new graph"
                    );
                
                client.executeUpdate(query);
            }
        }
        
        private static String addRemediationStrategy(
                final FusekiClient client,
                final String remediationGraph,
                
                final String actionToTake,
                final String onProperty,
                
                final int index,
                final String solutionQuery,
                final String solutionAction,
                final String solutionProperty,
                final String solutionAspectToInvoke,
                final String solutionHumanReadable
                ){
            
            final String uuid = StringUtils.substringAfterLast(UUID.randomUUID().toString(),"-");
            final String subject = QueryHelper.Solution.value+"-"+uuid;
            
            final String queryBase = 
                    "INSERT {\n"+
                    "    GRAPH ${REMEDIATION_GRAPH} {\n"+
                    "        ${S} a ${Solution} .\n"+
                    "        ${S} ${RdfsComment} \"A means to achieve some desired effect on some desired property. Built dynamically from a CONSTRUCT rule.\" .\n"+
                    "        ${S} ${HasHumanReadableDesc} \"${HUMAN_READABLE}\" .\n"+
                    "        ${S} ${HasDesiredAction} ${ACTION_TO_TAKE} .\n"+
                    "        ${S} ${HasDesiredSubject} ${ON_PROPERTY} .\n"+
                    "        ${S} ${HasSolutionAction} \"${SOLUTION_ACTION}\" .\n"+
                    "        ${S} ${HasSolutionSubject} ${SOLUTION_SUBJECT} .\n"+
                    "    }\n"+
                    "}\n"+
                    "WHERE {\n"+
                    "}\n"+
                    "\n";
            
            String target = solutionProperty;
            if(target == null){
                target = solutionAspectToInvoke;
            }
            if(target == null){
                throw new RuntimeException("no property to modify or dfu to invoke");
            }
            
            final String query = QueryHelper.buildQuery(
                queryBase,
                "${REMEDIATION_GRAPH}",remediationGraph,
                "${S}",subject,
                "${HUMAN_READABLE}",solutionHumanReadable,
                "${INDEX}",""+index,
                
                "${SOLUTION_ACTION}",solutionAction,
                "${SOLUTION_SUBJECT}",target,
                
                "${ACTION_TO_TAKE}",actionToTake,
                "${ON_PROPERTY}",onProperty
                );
            
            logQuery(
                query,
                "synthesize a corrective action"
                );
            
            client.executeUpdate(query);
            
            return subject;
        }
        
        private static List<Solution> getMitigationStrategy(
                final List<Solution> chain,
                final FusekiClient client,
                final String actionToTake,
                final String property,
                final String...vars
                ){
            logMessage(
                "searching for strategies that allow us to achieve corrective effect " + actionToTake + 
                " on property <" + property + ">"
                );
            
            final String getMitigationStrategy = QueryHelper.buildQuery(
                "SELECT DISTINCT ?humanReadableStatement ?actionToTake ?onProperty ?invokedAspect\n"+
                "${GRAPH}\n"+
                "WHERE {\n"+
                "    ?strategy a ${PredictiveCauseEffectAssertion} .\n"+
                "    ?strategy ${HasImpact} ?impact .\n"+
                "    ?strategy ${HasHumanReadableDesc} ?humanReadableStatement .\n"+
                "    ?impact ${HasHumanReadableDesc} ?humanReadableImpact .\n"+
                "    ?impact ${HasImpactOnProperty} ${IMPACT} .\n"+
                "    ?impact ${HasImpactOnProperty} ?impactOnProperty .\n"+
                "    ?impact ${HasImpactedProperty} ${PROPERTY} .\n"+
                "    ?impact ${HasImpactedProperty} ?impactedProperty .\n"+
                "    ?strategy ${HasCriterion} ?criterion .\n"+
                "    ?criterion ${HasHumanReadableDesc} ?humanReadableActionToTake .\n"+
                "    ?criterion ${HasCriterion} ?actionToTake .\n"+
                "    OPTIONAL { ?criterion ${HasProperty} ?onProperty . }\n"+
                "    OPTIONAL { ?criterion ${HasInvokedAspect} ?invokedAspect . }\n"+
                "}\n"+
                "\n",
                vars,
                "${IMPACT}",actionToTake,
                "${PROPERTY}",property
                );
            
            List<Solution> q1Result = 
                    executeQuery(
                        client,
                        getMitigationStrategy,
                        "select a mitigation strategy for action " + 
                        actionToTake + 
                        " and subject " + 
                        property
                        ).getSolutions();
            
            for(Solution s:q1Result){
                List<Solution> newChain = new ArrayList<>(chain);
                
                newChain.add(s);
                
                if(s.get("invokedAspect") != null){
                    return newChain;
                }
                
                List<Solution> solvedChain = getMitigationStrategy(
                    newChain,
                    client,
                    actionToTake,
                    s.get("onProperty"),
                    vars
                    );
                
                if(solvedChain != null){
                    return solvedChain;
                }
            }
            
            return null;
        }
        
        private static List<String> getMissionSpecs(
                final FusekiClient client,
                final String...vars
                ){
            logMessage("retrieving a list of MissionSpecs");
            
            final String query = QueryHelper.buildQuery(
                "SELECT DISTINCT *\n"+
                "${GRAPH}"+
                "WHERE {\n"+
                "    ?spec a ${MissionSpec} .\n"+
                "    ?spec ${HasHumanReadableForm} ?human_readable .\n"+
                "}\n"+
                "\n",
                vars
                );
            
            return new ArrayList<>(
                    executeQuery(
                        client,
                        query,
                        "retrieve all mission specs"
                        ).getSolutionsForVar("spec")
                    );
        }
        
    }

}
