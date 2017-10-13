import com.securboration.immortals.j2s.mapper.PojoMappingContext;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.analysis.DataflowAnalysisFrame;
import com.securboration.immortals.ontology.analysis.DataflowEdge;
import com.securboration.immortals.ontology.analysis.DataflowNode;
import com.securboration.immortals.ontology.analysis.MethodInvocationDataflowNode;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.alg.encryption.aes.AES_256;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.compression.CompressionAlgorithm;
import com.securboration.immortals.ontology.functionality.dataproperties.Compressed;
import com.securboration.immortals.ontology.functionality.dataproperties.Encrypted;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.query.TriplesToPojo;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ConstraintWorkflow extends ConstraintQueryTestBase {
    
    public void testConstraintWorkflow() throws Exception {

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

    private void workflow(
            final FusekiClient client,
            final CleanupContext context
    ) throws Exception {

        System.out.println("Importing ontology concepts and pushing to fuseki...");
        client.copy(
                DEFAULT_GRAPH,
                super.generateUniqueUri(context,"bytecode-analysis")
        );
        client.setModel(
                getKnowledge(ClientServerConstraintEnvironment.class),
                super.generateUniqueUri(context, "repaired-client-server-environ")
        );

        final String uberGraph =
                super.generateUniqueUri(context,"uber-graph");

        //populate the uber graph
        {
            for(String graphName:context.getGraphsToCleanup()){

                if(graphName.equals(uberGraph)) {
                    continue;
                }

                client.addToModel(client.getModel(graphName), uberGraph);
            }
        }

        System.out.println("Done. Beginning constraint analysis...");
        constraintAnalysis(uberGraph, null, client);
        //analyzeBindingInstances(client, uberGraph);

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
    }

    private Model getKnowledge(Class<?> c){
        PojoMappingContext mappingContext =
                PojoMappingContext.acquireContext(VERSION);

        for(Object o:instantiate(c)){
            mappingContext.addToModel(o);
        }
        
        return mappingContext.getCurrentModel();
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

    private static Object instantiateInternal(Class<?> c){
        System.out.println("instantiating " + c.getName());
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
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
    
    private void analysisFrameValidation(List<Solution> dataFlows, FusekiClient client, String graphName, ObjectToTriplesConfiguration config) throws Exception {
        
        System.out.println("Before assessing constraints on found data flows, testing whether data transmitted across them will be handled correctly by consumers.");
        for (Solution dataFlow : dataFlows) {
            String dataFlowUUID = dataFlow.get("dataFlowEdge");
             System.out.println("Dataflow found: " + dataFlowUUID + "\n");
             TriplesToPojo.SparqlPojoContext results = getObjectRepresentation(dataFlowUUID, DATAFLOW_INTER_METHOD_TYPE, client, graphName, config);
            
            results.forEach(solution ->{
                
                DataflowEdge edge = (DataflowEdge) solution.get("obj");
                Model m = ObjectToTriples.convert(config.getCleanContext(true), edge);
                try {
                    System.out.println(OntologyHelper.serializeModel(m, "TURTLE", false));
                }catch (IOException exc) {
                    exc.printStackTrace();
                }
                
                DataflowAnalysisFrame frame = edge.getDataflowAnalysisFrame();
                LinkedBlockingQueue<UnWrapper> unWrappers = constructUnwrappers(frame);
                String getConsumerUUID = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "select ?consumer where {\n" +
                        "\tgraph<???GRAPH_NAME???> {\n" +
                        "    <???EDGE???> IMMoRTALS:hasConsumer ?consumer .\n" +
                        "\t}\n" +
                        "}";
                getConsumerUUID = getConsumerUUID.replace("???GRAPH_NAME???", graphName).replace("???EDGE???", dataFlowUUID);
                
                AssertableSolutionSet nodeUUIDSolutions = new AssertableSolutionSet();
                client.executeSelectQuery(getConsumerUUID, nodeUUIDSolutions);
                String consumerUUID = nodeUUIDSolutions.getSolutions().get(0).get("consumer");
                DataflowNode consumer = edge.getConsumer();
                
                try { 
                    unWrapFrames(unWrappers, frame, consumer, consumerUUID, client, graphName);
                    System.out.println("Dataflow analysis frame validation for data flow " + dataFlowUUID + " is complete.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        
        System.out.println("Continuing with constraint assessment on data flows...");
    }
    
    private boolean unWrapFrames(LinkedBlockingQueue<UnWrapper> unWrappers, DataflowAnalysisFrame frame,
                              DataflowNode consumer, String consumerUUID,
                              FusekiClient client, String graphName) throws Exception{
        
        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");
        
        final String getCalledNodes = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "\n" +
                "select ?nodes where {\n" +
                "\tgraph<???GRAPH_NAME???> {\n" +
                "\t\t{?edge a*  IMMoRTALS_analysis:DataflowEdge}\n" +
                "\t\tUNION\n" +
                "\t\t{?tester rdfs:subClassOf* IMMoRTALS_analysis:DataflowEdge .\n" +
                "\t\t ?edge a* ?tester .}\n" +
                "\t\t ?edge IMMoRTALS:hasProducer <???NODE???>\n" +
                "\t\t; IMMoRTALS:hasConsumer ?nodes .\n" +
                "\t}\n" +
                "}";
        
        AssertableSolutionSet nodeSolutions = new AssertableSolutionSet();
        client.executeSelectQuery(getCalledNodes.replace("???GRAPH_NAME???", graphName).replace("???NODE???", consumerUUID)
                , nodeSolutions);
        
        Stack<Solution> solutionStack = new Stack<>();
        solutionStack.addAll(nodeSolutions.getSolutions());
        
        System.out.println("Retrieving nodes the consumer is transmitting the observed data to using query:\n" + getCalledNodes + "\n\n");
        
        while (!solutionStack.isEmpty() && !unWrappers.isEmpty()) {
            
            Solution nodeSolution = solutionStack.pop();
            String nodeUUID = nodeSolution.get("nodes");

            TriplesToPojo.SparqlPojoContext results = getObjectRepresentation(nodeUUID, DATAFLOW_METHOD_NODE_TYPE, client, graphName, config);
            System.out.println("Node found: " + nodeUUID + "\n");
            results.forEach(solution -> {

                DataflowNode node = (DataflowNode) solution.get("obj");
                
                Model m = ObjectToTriples.convert(config.getCleanContext(true), node);
                String serilazedModel = null;
                try {
                     serilazedModel = OntologyHelper.serializeModel(m, "TURTLE", false);
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
                System.out.println(serilazedModel + "\n\n");
                if (node instanceof MethodInvocationDataflowNode) {

                    String methodPointer = ((MethodInvocationDataflowNode) node).getJavaMethodPointer();

                    String getAnnotationClasses = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                            "\n" +
                            "select distinct ?annotations ?className where {\n" +
                            "  graph<???GRAPH_NAME???> {\n" +
                            "  ?method IMMoRTALS:hasBytecodePointer \"???POINTER???\"\n" +
                            "  ; IMMoRTALS:hasAnnotations ?annotations .\n" +
                            "  \n" +
                            "  ?annotations IMMoRTALS:hasAnnotationClassName ?className .\n" +
                            "}\n" +
                            "}\n";

                    getAnnotationClasses = getAnnotationClasses.replace("???GRAPH_NAME???", graphName).replace("???POINTER???", methodPointer);
                    AssertableSolutionSet annotationSolutions = new AssertableSolutionSet();

                    client.executeSelectQuery(getAnnotationClasses, annotationSolutions);

                    for (Solution annotationSolution : annotationSolutions.getSolutions()) {

                        String annotClassName = annotationSolution.get("className");
                        annotClassName = annotClassName.substring(5, annotClassName.length() - 1);
                        
                        if (annotClassName.equals("mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/FunctionalAspectAnnotation")) {
                            System.out.println("Node: " + nodeUUID + " is a functional aspect instance, determining whether it alters the observed data...\n");
                            String annotationUUID = annotationSolution.get("annotations");

                            String getKeyValuePairs = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                    "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> \n" +
                                    "\n" +
                                    "select distinct ?value where {\n" +
                                    "  graph<???GRAPH_NAME???> {\n" +
                                    "  <???ANNOT???> a IMMoRTALS_bytecode:AnAnnotation\n" +
                                    "  ; IMMoRTALS:hasKeyValuePairs ?keyValuePairs .\n" +
                                    "  ?keyValuePairs IMMoRTALS:hasValue ?value\n" +
                                    "}\n" +
                                    "}\n";
                            getKeyValuePairs = getKeyValuePairs.replace("???GRAPH_NAME???", graphName).replace("???ANNOT???", annotationUUID);
                            AssertableSolutionSet keyValueSolutions = new AssertableSolutionSet();

                            client.executeSelectQuery(getKeyValuePairs, keyValueSolutions);

                            if (keyValueSolutions.getSolutions().size() != 0) {

                                Solution keyValueSolution = keyValueSolutions.getSolutions().get(0);
                                String aspectClassName = keyValueSolution.get("value");
                                aspectClassName = aspectClassName.substring(5, aspectClassName.length() - 1);
                                try {
                                    Object obj = Class.forName(aspectClassName).getConstructor().newInstance();
                                    if (obj instanceof FunctionalAspect) {

                                        FunctionalAspect aspect = (FunctionalAspect) obj;

                                        Class<? extends DataType> aspectExpectedInput = aspect.getInputs()[0].getType();
                                        System.out.println("Functional aspect instance expects input: " + aspectExpectedInput.getCanonicalName());

                                        // we can assume that the functional aspect method is using our observed data
                                        if (unWrappers.peek().getObservedDataType().equals(aspectExpectedInput)) {
                                            UnWrapper unWrapper = unWrappers.poll();

                                            System.out.println("Observed data type is: " + unWrapper.getObservedDataType().getCanonicalName() 
                                                    + ", the functional aspect is attempting to use the observed data.\n\n");
                                            
                                            List<Property> propertiesHidingData = unWrapper.getPropertiesToBeRemoved();
                                            
                                            for (ImpactStatement impactStatement : aspect.getImpactStatements()) {
                                                PropertyImpact propertyImpact = (PropertyImpact) impactStatement;

                                                Class<? extends Property> impactedProperty = propertyImpact.getImpactedProperty();
                                                Property expectedProperty = impactedProperty.getConstructor().newInstance();
                                                
                                                // TODO configure encrypted property by examining dependencies... long ways away
                                                // TODO for now just hard code property configuration
                                                if (expectedProperty instanceof Encrypted) {
                                                    ((Encrypted) expectedProperty).setEncryptionAlgorithm(AES_256.class);
                                                } else if (expectedProperty instanceof Compressed) {
                                                    ((Compressed) expectedProperty).setCompressionAlgorithm(CompressionAlgorithm.class);
                                                }
                                                for (int i = 0; i < propertiesHidingData.size(); i++) {
                                                    Property hiddenProperty = propertiesHidingData.get(i);
                                                    if (compareProperties(hiddenProperty, expectedProperty)) {
                                                        propertiesHidingData.remove(i);
                                                        break;
                                                    } else {
                                                        return;
                                                    }
                                                }
                                            }
                                            
                                            if (propertiesHidingData.size() != 0) {
                                                System.out.println("Aspect: " + aspectClassName + " fails to properly handle data being transmitted across data flow.");
                                                return;
                                            } else {
                                                System.out.println("Aspect: " + aspectClassName + " correctly handles the data being transmitted across data flow.");
                                            }
                                            
                                        } else {
                                            System.out.println("Observed data type is:" + unWrappers.peek().getObservedDataType().getCanonicalName()
                                                    + ", the functional aspect doesn't attempt to use the observed data.\n\n");

                                        }
                                        
                                        System.out.println("Retrieving nodes that consume the data used by " + nodeUUID 
                                                + " using query: \n\n" + getCalledNodes.replace("???GRAPH_NAME???", graphName)
                                                .replace("???NODE???", nodeUUID) +"\n");
                                        AssertableSolutionSet newNodes = new AssertableSolutionSet();
                                        client.executeSelectQuery(getCalledNodes.replace("???GRAPH_NAME???", graphName).replace("???NODE???", nodeUUID),
                                                newNodes);

                                        solutionStack.addAll(newNodes.getSolutions());
                                    }
                                } catch (Exception exc) {
                                    exc.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
        }
        return unWrappers.isEmpty();
    }
    
    private boolean compareProperties(Property actualProperty, Property expectedProperty) throws Exception {

        List<Field> actualPropertyFields = Arrays.asList(actualProperty.getClass().getDeclaredFields());
        List<Field> expectedPropertyFields = Arrays.asList(expectedProperty.getClass().getDeclaredFields());

        if (actualPropertyFields.size() == expectedPropertyFields.size()) {
            
            for (Field expectedField : expectedPropertyFields) {
                expectedField.setAccessible(true);
                String expectedFieldName = expectedField.getName();
                boolean hasField = false;
                for (Field actualField : actualPropertyFields) {
                    actualField.setAccessible(true);
                    String actualFieldName = actualField.getName();
                    
                    if (expectedFieldName.equals(actualFieldName)) {
                        if (!expectedField.get(expectedProperty).equals(actualField.get(actualProperty))) {
                            System.out.println("Found property differs from property anticipated.");
                            System.out.println("Expected property field, " + expectedFieldName + ", with value: " + expectedField.get(expectedProperty));
                            System.out.println("Actual property field, " + actualFieldName + ", with value: " + actualField.get(actualProperty) + "\n\n");
                            return false;
                        }
                        hasField = true;
                    }
                }
                if (!hasField) {
                    System.out.println("Found property lacks at least one field anticipated by specified aspect.");
                    System.out.println("Expected property field: " + expectedFieldName +"\n\n");
                    return false;
                }
            }
            
            
        } else if (actualPropertyFields.size() > expectedPropertyFields.size()) {
            System.out.println("The property found has additional fields not anticipated by the specified aspect.");
            return false;
        } else {
            System.out.println("The property found lacks fields anticipated by the specified aspect.");
            return false;
        }
        
        return true;
    }
    
    
    private LinkedBlockingQueue<UnWrapper> constructUnwrappers(DataflowAnalysisFrame frame) {

        LinkedBlockingQueue<UnWrapper> unWrappers = new LinkedBlockingQueue<>();
        
        while (frame != null) {
            UnWrapper unWrapper = new UnWrapper();
            List<Property> properties = new ArrayList<>();
            for (Property property : frame.getFrameProperties()) {
                if (property instanceof DataProperty) {
                    DataProperty dataProperty = (DataProperty) property;
                    if (dataProperty.isHidden()) {
                        System.out.println("Found property that will need to be handled by consumer: " + property.getClass().getCanonicalName());
                        properties.add(dataProperty);
                    }
                }
            }
            
            if (properties.size() != 0) {
                unWrapper.setPropertiesToBeRemoved(properties);
                unWrapper.setObservedDataType(frame.getAnalysisFrameDataType());
                unWrappers.add(unWrapper);
            }

            frame = frame.getAnalysisFrameChild();
        }
        
        return unWrappers;
    }

    private void constraintAnalysis(String graphName, String constraintGraphName, FusekiClient client) throws Exception {
        
        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");

        String getConstraints = "prefix IMMoRTALS_impact:  <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#>\n" +
                "\n" +
                "select ?constraints where {\n" +
                "\t\n" +
                "    graph <???GRAPH_NAME???> {\n" +
                " \n" +
                "\t\t  ?constraints a IMMoRTALS_impact:ProscriptiveCauseEffectAssertion .\n" +
                "\t}\n" +
                "\t\n" +
                "}";

        getConstraints = getConstraints.replace("???GRAPH_NAME???", graphName);
        System.out.println("Retrieving constraints using query:\n\n" + getConstraints + "\n\n");

        AssertableSolutionSet constraints = new AssertableSolutionSet();
        client.executeSelectQuery(getConstraints, constraints);

        if (constraints.getSolutions().size() != 0) {
            // For each constraint found...
            for (Solution constraint : constraints.getSolutions()) {
                boolean constraintSatisfied = true;
                String constraintUUID = constraint.get("constraints");
                System.out.println("Constraint found: " + constraintUUID);

                TriplesToPojo.SparqlPojoContext results = getObjectRepresentation(constraintUUID,
                        CONSTRAINT_TYPE, client, graphName, config);
                
                results.forEach(solution ->{
                    ProscriptiveCauseEffectAssertion assertion = (ProscriptiveCauseEffectAssertion) solution.get("obj");
                    Model assertModel = ObjectToTriples.convert(config.getCleanContext(true), assertion);
                    try {
                        System.out.println(OntologyHelper.serializeModel(assertModel, "TURTLE", false));
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                });

                System.out.println("Identifying applicable data flows...");

                // find any data flows that it applies to.
                String getApplicableDataflows = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n" +
                        "prefix IMMoRTALS_resources: <http://darpa.mil/immortals/ontology/r2.0.0/resources#> \n" +
                        "prefix IMMoRTALS_analysis:  <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> \n" +
                        "\n" +
                        "select ?dataFlowEdge where {\n" +
                        "\t\n" +
                        "    graph <???GRAPH_NAME???> {\n" +
                        " \n" +
                        "\t\t  <???CONSTRAINT???> IMMoRTALS:hasAssertionBindingSite ?bindingSite .\n" +
                        "\t\t  \n" +
                        "\t\t  ?bindingSite IMMoRTALS:hasDest ?dest\n" +
                        "\t\t  ; IMMoRTALS:hasSrc ?src .\n" +
                        "\t\t  \n" +
                        "\t\t  {?dataFlowEdge a* IMMoRTALS_analysis:DataflowEdge} \n" +
                        "\t\t   UNION\n" +
                        "\t\t  {?tester rdfs:subClassOf* IMMoRTALS_analysis:DataflowEdge.\n" +
                        "    \t\t?dataFlowEdge a* ?tester }\n" +
                        "    \n" +
                        "          ?dataFlowEdge IMMoRTALS:hasConsumer ?node1\n" +
                        "\t\t  ; IMMoRTALS:hasProducer ?node2 .\n" +
                        "\t\t  \n" +
                        "\t\t  ?node1 IMMoRTALS:hasResourceTemplate ?resource1 .\n" +
                        "\t\t  ?resource1 a* ?dest .\n" +
                        "\t\t   ?node2 IMMoRTALS:hasResourceTemplate ?resource2 .\n" +
                        "\t\t  ?resource2 a* ?src .\n" +
                        "\n" +
                        "\t}\n" +
                        "  \n" +
                        "}";
                getApplicableDataflows = getApplicableDataflows.replace("???GRAPH_NAME???", graphName)
                        .replace("???CONSTRAINT???", constraintUUID);
                System.out.println("Retrieving applicable data flows as well as their candidacy for violation" +
                        " using query:\n\n" + getApplicableDataflows + "\n\n");
                AssertableSolutionSet dataFlowEdges = new AssertableSolutionSet();
                client.executeSelectQuery(getApplicableDataflows, dataFlowEdges);

                if (dataFlowEdges.getSolutions().size() != 0) {
                    //System.out.println("Found applicable data flows. Determining whether data flows violate given constraint...");
                    
                    analysisFrameValidation(dataFlowEdges.getSolutions(), client, graphName, config);
                    
                    // Then once we find out how the constraint is violated...
                    String getCriterion = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                            "select ?criterion ?criterionRelation ?criterionProperty ?standardType ?violationType where {\n" +
                            "\t\n" +
                            "    graph <???GRAPH_NAME???> {\n" +
                            " \n" +
                            "\t\t  <???CONSTRAINT???> IMMoRTALS:hasCriterion ?criterion\n" +
                            "\t\t  ; IMMoRTALS:hasImpact ?violationImpact .\n" +
                            "\t\t  \n" +
                            "          ?violationImpact IMMoRTALS:hasConstraintViolationType ?violationType .\n" +
                            "\n" +
                            "\t\t  ?criterion IMMoRTALS:hasCriterion ?criterionRelation\n" +
                            "\t\t  ;IMMoRTALS:hasProperty ?criterionProperty .\n" +
                            "\t\t   OPTIONAL {?criterion IMMoRTALS:hasStandardCriterionType ?standardType}\t}\n" +
                            "\t\n" +
                            "}";

                    getCriterion = getCriterion.replace("???GRAPH_NAME???", graphName).replace("???CONSTRAINT???", constraintUUID);
                    //System.out.println("Retrieving criteria for violating constraint using query:\n\n" + getCriterion + "\n\n");
                    AssertableSolutionSet criterionInfo = new AssertableSolutionSet();
                    client.executeSelectQuery(getCriterion, criterionInfo);

                    for (Solution criterion : criterionInfo.getSolutions()) {
                        boolean criterionSatisfied = false;
                        String criterionUUID = criterion.get("criterion");
                        String criterionRelation = criterion.get("criterionRelation");
                        String criterionProperty = criterion.get("criterionProperty");
                        String standardType = criterion.get("standardType");

                        for (Solution dataFlowEdge : dataFlowEdges.getSolutions()) {
                            String dataFlowUUID = dataFlowEdge.get("dataFlowEdge");
                            System.out.println("Assessing constraint on data flow: " + dataFlowUUID);
                            System.out.println("Retrieving criteria for violating constraint using query:\n\n" + getCriterion + "\n\n");
                            // we can see if our data flow violates the constraint
                            switch (criterionRelation) {
                                case "PROPERTY_ABSENT":
                                    String getEdgeProperties = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                            "select ?properties where {\n" +
                                            "\t\n" +
                                            "    graph <???GRAPH_NAME???> {\n" +
                                            " \n" +
                                            "\t\t  <???DATA_FLOW???> IMMoRTALS:hasEdgeProperties ?properties .\n" +
                                            "\t}\n" +
                                            "\t\n" +
                                            "}";
                                    getEdgeProperties = getEdgeProperties.replace("???GRAPH_NAME???", graphName).replace("???DATA_FLOW???", dataFlowUUID);
                                    System.out.println("Retrieving current edge properties using query:\n\n" + getEdgeProperties + "\n\n");
                                    AssertableSolutionSet edgeProperties = new AssertableSolutionSet();
                                    client.executeSelectQuery(getEdgeProperties, edgeProperties);
                                    if (edgeProperties.getSolutions().size() == 0) {
                                        System.out.println("DataflowEdge " + dataFlowUUID + "doesn't have any properties, violates constraint.");
                                        break;
                                    } else {
                                        for (Solution edgeProperty : edgeProperties.getSolutions()) {
                                            String edgePropertyUUID = edgeProperty.get("properties");

                                            String getPropertyClass = "select ?class where { graph <???GRAPH_NAME???> {\n" +
                                                    "    <???PROPERTY_INSTANCE???> a ?class .\n" +
                                                    "}}";
                                            getPropertyClass = getPropertyClass.replace("???GRAPH_NAME???", graphName)
                                                    .replace("???PROPERTY_INSTANCE???", edgePropertyUUID);
                                            System.out.println("Retrieving property class using query:\n\n" + getPropertyClass + "\n\n");
                                            AssertableSolutionSet propertyClassSolution = new AssertableSolutionSet();
                                            client.executeSelectQuery(getPropertyClass, propertyClassSolution);

                                            String propertyClass = propertyClassSolution.getSolutions().get(0).get("class");

                                            String getSpecifiedProperty = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                                                    "\n" +
                                                    "ask where { graph <???GRAPH_NAME???> {\n" +
                                                    " <???PROPERTY_CLASS???> rdfs:subClassOf+  <???SPECIFIED_PROPERTY???> } }";
                                            getSpecifiedProperty = getSpecifiedProperty.replace("???PROPERTY_CLASS???", propertyClass)
                                                    .replace("???SPECIFIED_PROPERTY???", criterionProperty)
                                                    .replace("???GRAPH_NAME???", graphName);
                                            System.out.println("Asking whether the property class is an instance of the specified class using query:\n\n"
                                                    + getSpecifiedProperty + "\n\n");
                                            if (client.executeAskQuery(getSpecifiedProperty)) {
                                                if (standardType != null) {
                                                    criterionSatisfied = assertStandards(client, edgePropertyUUID, standardType,
                                                            graphName, dataFlowUUID, constraintUUID);
                                                } else {
                                                    criterionSatisfied = true;
                                                }
                                                break;
                                            } else {
                                                criterionSatisfied = false;
                                                continue;
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    System.out.println("Unable to handle specified property relation.");
                                    break;
                            }

                            constraintSatisfied = constraintSatisfied && criterionSatisfied;

                            // If the data flow satisfies the constraint, move on to the next one...
                            if (criterionSatisfied) {
                                System.out.println(dataFlowUUID + " satisfies criterion " + criterionUUID);
                            } else {
                                String violationType = criterion.get("violationType");
                                String insertViolationReports = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                        "insert {\n" +
                                        "  graph <???GRAPH_NAME???> {\n" +
                                        "    ?violationReport a <http://darpa.mil/immortals/ontology/r2.0.0/violations#ConstraintViolationReport> .\n" +
                                        "    ?violationReport IMMoRTALS:hasViolatedConstraint <http://darpa.mil/immortals/ontology/r2.0.0/cp2#ClientServerEnvironment.HighestConfidentialityConstraint> .\n" +
                                        "    ?violationReport IMMoRTALS:hasViolationType \"???VIOLATION_TYPE???\" .\n" +
                                        "    ?violationReport IMMoRTALS:hasOffender <???OFFENDER???> .\n" +
                                        "  }\n" +
                                        "}\n" +
                                        "where {\n" +
                                        "    BIND(URI(CONCAT(\"http://darpa.mil/immortals/ontology/r2.0.0/violations#ViolationReport-\",STRUUID())) AS ?violationReport)\n" +
                                        "}";
                                insertViolationReports = insertViolationReports.replace("???GRAPH_NAME???", constraintGraphName)
                                        .replace("???CONSTRAINT???", constraintUUID).replace("???VIOLATION_TYPE???", violationType)
                                        .replace("???OFFENDER???", dataFlowUUID);

                                client.executeUpdate(insertViolationReports);
                                // else try to find a mitigation strategy and apply it.
                                System.out.println(dataFlowUUID + " violates criterion. Creating violation report using query: " +
                                        insertViolationReports + "\n\n");
                                        //"Proceeding to identify and apply mitigation strategies.");
                            }
                        }
                    }
                } else {
                    System.out.println("Constraint has no applicable data flow edges.");
                }
            }
        }
        System.out.println("All constraints have been analyzed.");
    }

    private TriplesToPojo.SparqlPojoContext getObjectRepresentation(String objectUUID, String objectType, FusekiClient client,
                                                                    String graphName, ObjectToTriplesConfiguration config) throws Exception {
        
        String getObject = "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT ?obj WHERE {\n" +
                "    GRAPH <???GRAPH_NAME???> { \n" +
                "        {?obj a <???OBJECT_TYPE???>}\n" +
                "  UNION {?tester rdfs:subClassOf* <???OBJECT_TYPE???> . ?obj a* ?tester .}\n" +
                "        FILTER(?obj = <???OBJECT_UUID???>)\n" +
                "        \n" +
                "    } .\n" +
                "} LIMIT 1";
        getObject = getObject.replace("???GRAPH_NAME???", graphName).replace("???OBJECT_TYPE???", objectType)
                .replace("???OBJECT_UUID???", objectUUID);

        return TriplesToPojo.sparqlSelect(getObject, graphName, client, config);//.getCleanContext(true)); //config);
    }

    private void findAndEnactMitigationStrategy(String dataFlowUUID, String constraintUUID, String graphName , FusekiClient client) throws Exception {

        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");

        // Get the impacts involved in applying this mitigation strategy...
        String getStrategyImpacts = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_cp2: <http://darpa.mil/immortals/ontology/r2.0.0/cp2#>\n" +
                "prefix IMMoRTALS_impact:  <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#>\n" +
                "select ?criterion3 ?property1 ?provenance ?remedy ?strategy ?bindingSite\n\t where { \n\t graph <???GRAPH_NAME???> { \n" +
                " \n" +
                "  <???CONSTRAINT???> IMMoRTALS:hasImpact ?impact1 . \n" +
                "  \n" +
                "  ?impact1 IMMoRTALS:hasConstraintViolationType ?violation .\n" +
                "  \n" +
                "  BIND (  IF ( ?violation = \"HARD_CONSTRAINT_VIOLATION\", \"WHEN_HARD_VIOLATED\", IF (?violation = \"SOFT_CONSTRAINT_VIOLATION\", \"WHEN_SOFT_VIOLATED\", \"\" )) AS ?violationResponse) \n" +
                "  \n" +
                "  ?strategy a IMMoRTALS_impact:PrescriptiveCauseEffectAssertion\n" +
                "  ; IMMoRTALS:hasCriterion ?criterion1 .\n" +
                "  \n" +
                "  ?criterion1 IMMoRTALS:hasConstraint <???CONSTRAINT???>\n" +
                "  ; IMMoRTALS:hasTriggeringConstraintCriterion ?violationResponse .\n" +
                "  \n" +
                "  ?strategy IMMoRTALS:hasImpact ?impact2 .\n" +
                "  \n" +
                "  ?impact2 IMMoRTALS:hasRemediationStrategy ?remedy .\n" +
                "  \n" +
                "  ?remedy IMMoRTALS:hasCriterion ?criterion2\n" +
                "  ; IMMoRTALS:hasImpact ?impact3 .\n" +
                "  \n" +
                "  ?criterion2 IMMoRTALS:hasCriterion ?criterion3\n" +
                "  ; IMMoRTALS:hasProperty ?property1 .\n" +
                "  \n" +
                "  ?impact3 IMMoRTALS:hasImpactOnProperty ?propertyImpact\n" +
                "  ; IMMoRTALS:hasImpactedProperty ?property2 .\n" +
                "  \n" +
                "  ?property1 IMMoRTALS:hasPojoProvenance ?provenance .\n" +
                "  \n" +
                "}\n" +
                "}";
        getStrategyImpacts = getStrategyImpacts.replace("???CONSTRAINT???", constraintUUID).replace("???GRAPH_NAME???", graphName);
        System.out.println("Retrieving impacts of mitigation strategies using query:\n\n" + getStrategyImpacts + "\n\n");
        AssertableSolutionSet strategyImpacts = new AssertableSolutionSet();
        client.executeSelectQuery(getStrategyImpacts, strategyImpacts);

        if (strategyImpacts.getSolutions().size() != 0) {
            for (Solution strategyImpact : strategyImpacts.getSolutions()) {
                String mitigationStretegy = strategyImpact.get("strategy");
                System.out.print("Mitigation strategy found: \n");
                TriplesToPojo.SparqlPojoContext results = getObjectRepresentation(mitigationStretegy, MITIGATION_STRATEGY_TYPE,
                        client, graphName, config);

                results.forEach(solution ->{
                    PrescriptiveCauseEffectAssertion assertion = (PrescriptiveCauseEffectAssertion) solution.get("obj");
                    Model m = ObjectToTriples.convert(config, assertion);
                    try {
                        System.out.println(OntologyHelper.serializeModel(m, "TURTLE", false));
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                });
                String getEdgeNodes = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "select ?consumer ?producer \n" +
                        "\twhere {\n" +
                        "\t\tgraph <???GRAPH_NAME???> {\n" +
                        "\t\t\t<???DATAFLOW???> IMMoRTALS:hasConsumer ?consumer\n" +
                        "\t\t\t; IMMoRTALS:hasProducer ?producer .\n" +
                        "\t\t}\n" +
                        "\n" +
                        " }";
                getEdgeNodes = getEdgeNodes.replace("???GRAPH_NAME???", graphName).replace("???DATAFLOW???", dataFlowUUID);
                AssertableSolutionSet edgeNodesSolutionSet = new AssertableSolutionSet();
                client.executeSelectQuery(getEdgeNodes, edgeNodesSolutionSet);

                Solution edgeNodes = edgeNodesSolutionSet.getSolutions().get(0);
                String consumerNode = edgeNodes.get("consumer");
                String producerNode = edgeNodes.get("producer");

                final String bindingSiteUUID = "http://darpa.mil/immortals/ontology/r2.0.0/property/impact#DataflowBindingSite-" + UUID.randomUUID();
                String insertBindingSite = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "insert data {\n" +
                        "\tgraph<???GRAPH_NAME???> {\n" +
                        "\t\t<???BINDING???> a <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#DataflowBindingSite> \n" +
                        "\t\t; IMMoRTALS:hasSrc <???PRODUCER???>\n" +
                        "\t\t; IMMoRTALS:hasDest <???CONSUMER???> .\n" +
                        "\t}\t\n" +
                        "}";
                insertBindingSite = insertBindingSite.replace("???GRAPH_NAME???", graphName).replace("???BINDING???", bindingSiteUUID)
                        .replace("???PRODUCER???", producerNode).replace("???CONSUMER???", consumerNode);
                client.executeUpdate(insertBindingSite);

                String insertBindingInstance = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "insert {\n" +
                        "\tgraph<???GRAPH_NAME???> {\n" +
                        "\t\t?bindingInstance a <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#PrescriptiveBindingInstance> .\n" +
                        "\t\t?bindingInstance IMMoRTALS:hasMitigationStrategy <???STRATEGY???> .\n" +
                        "\t\t?bindingInstance IMMoRTALS:hasBindingSite <???BINDING_SITE???> .\n" +
                        "\t}\t\n" +
                        "}\n" +
                        "where { \n" +
                        "\tBIND(URI(CONCAT(\"http://darpa.mil/immortals/ontology/r2.0.0/property/impact#PrescriptiveBindingInstance-\",STRUUID())) AS ?bindingInstance)\n" +
                        "}";
                insertBindingInstance = insertBindingInstance.replace("???GRAPH_NAME???", graphName).replace("???STRATEGY???", mitigationStretegy)
                        .replace("???BINDING_SITE???", bindingSiteUUID);
                client.executeUpdate(insertBindingInstance);
                System.out.println(strategyImpact.get("remedy"));
                System.out.println("Applying strategy to data flow...");
                String propertyCriterion = strategyImpact.get("criterion3");

                // and apply them to the data flow, satisfying the constraint in the process
                switch(propertyCriterion) {
                    case "PROPERTY_ADDED":
                        String pojoPrevenance = strategyImpact.get("provenance");
                        Class<?> clazz = Class.forName(pojoPrevenance);
                        Object object = clazz.getConstructor().newInstance();
                        if (object instanceof Property) {
                            //TODO injectProperty()
                            Model m = ObjectToTriples.convert(config, object);
                            String newPropertyName = config.getNamingContext().getNameForObject(object);
                            client.addToModel(m, graphName);
                            String insertNewProperty = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                                    "insert data { graph <???GRAPH_NAME???> {\n" +
                                    "    <???DATAFLOW_EDGE???> IMMoRTALS:hasEdgeProperties <???NEW_PROPERTY???>\n" +
                                    "\t}\n" +
                                    "\t}";
                            insertNewProperty = insertNewProperty.replace("???GRAPH_NAME???", graphName).replace("???DATAFLOW_EDGE???", dataFlowUUID)
                                    .replace("???NEW_PROPERTY???", newPropertyName);
                            System.out.println("Inserting new property using query:\n\n" + insertNewProperty + "\n\n");
                            client.executeUpdate(insertNewProperty);
                            System.out.println("Done. Data flow has been modified to satisfy constraint.");
                        }
                        break;
                    default:
                        System.out.println("Unable to handle specified property criterion");
                        break;
                }
            }
        } else {
            System.out.println("Unable to find a mitigation strategy for constraint: " + constraintUUID);
        }
    }

    private boolean assertStandards(FusekiClient client, String propertyUUID, String standardType,
                                    String graphName, String dataFlowUUID, String constraintUUID) throws Exception {
        switch (standardType) {
            case "STANDARD_CURRENT_BEST":
                String checkIfCurrentBest = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "select ?isCurrentBest where {\n" +
                        "\t\n" +
                        "    graph <???GRAPH_NAME???> {\n" +
                        " \n" +
                        "\t\t  <???PROPERTY???> IMMoRTALS:hasEncryptionAlgorithm ?algorithm .\n" +
                        "    ?algorithm IMMoRTALS:hasProperties ?standardProps .\n" +
                        "    ?standardProps IMMoRTALS:hasIsCurrentBest ?isCurrentBest .\n" +
                        "    \n" +
                        "\t}\n" +
                        "\t\n" +
                        "}";
                checkIfCurrentBest = checkIfCurrentBest.replace("???GRAPH_NAME???", graphName)
                        .replace("???PROPERTY???", propertyUUID);
                System.out.println("Criterion specifies standardized property; determining what standard the given property is" +
                        "using query " + checkIfCurrentBest);
                AssertableSolutionSet solutionSet = new AssertableSolutionSet();
                client.executeSelectQuery(checkIfCurrentBest, solutionSet);

                if (solutionSet.getSolutions().size() != 0) {
                    String isCurrentBest = solutionSet.getSolutions().get(0).get("isCurrentBest");
                    if (Boolean.parseBoolean(isCurrentBest)) {
                        return true;
                    } else {
                        //TODO deleteObsoleteProperty()
                        /*System.out.println("Criterion requires current best standard, removing non-best property and inserting " +
                                "current best...");
                        String deleteNonBestProperty = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                                "delete data { graph <???GRAPH_NAME???> {\n" +
                                "    <???DATAFLOW_EDGE???> IMMoRTALS:hasEdgeProperties <???NONBEST_PROPERTY???>\n" +
                                "\t}\n" +
                                "\t}";
                        deleteNonBestProperty = deleteNonBestProperty.replace("???GRAPH_NAME???", graphName)
                                .replace("???NONBEST_PROPERTY???", propertyUUID)
                                .replace("???DATAFLOW_EDGE???", dataFlowUUID);
                        client.executeUpdate(deleteNonBestProperty);
                        findAndEnactMitigationStrategy(dataFlowUUID, constraintUUID, graphName, client);*/
                        //return false;
                        System.out.println("Property is not current best, fails constraint.");
                        return false;
                    }

                } else {
                    System.out.println("Property has not been standardized.");
                    return false;
                }
            default:
                System.out.println("Unable to handle specified standard type.");
                return false;
        }
    }

    private void analyzeBindingInstances(FusekiClient client, String graphName) throws Exception {

        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");

        System.out.println("Ontology modifications complete; reviewing violations in order to determine necessary code repairs...");
        String getBindingInstances = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?bindingInstance where {\n" +
                "\tgraph <???GRAPH_NAME???> {\n" +
                "\t\t?bindingInstance a <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#PrescriptiveBindingInstance> .\n" +
                "\t}\n" +
                "}";
        getBindingInstances = getBindingInstances.replace("???GRAPH_NAME???", graphName);
        System.out.println("Retrieving prescriptive binding instances to locate where in code repairs are to be made using query:\n "
                + getBindingInstances);

        AssertableSolutionSet bindingInstancesSolution = new AssertableSolutionSet();
        client.executeSelectQuery(getBindingInstances, bindingInstancesSolution);

        if (bindingInstancesSolution.getSolutions().size() != 0) {
            for (Solution bindingInstance : bindingInstancesSolution.getSolutions()) {
                String bindingUUID = bindingInstance.get("bindingInstance");

                String getStrategyImpacts = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "select ?property ?propertyCriterion ?strategy ?dest ?src where {\n" +
                        "\tgraph <???GRAPH_NAME???> {\n" +
                        "\t\t<???BINDING???> IMMoRTALS:hasMitigationStrategy ?strategy\n" +
                        "\t\t; IMMoRTALS:hasBindingSite ?site .\n" +
                        "\t\t ?site IMMoRTALS:hasDest ?dest" +
                        "\t\t; IMMoRTALS:hasSrc ?src ." +
                        "\t\t?strategy IMMoRTALS:hasImpact ?remediationImpact .\n" +
                        "\t\t?remediationImpact IMMoRTALS:hasRemediationStrategy ?remedyStrat .\n" +
                        "\t\t?remedyStrat IMMoRTALS:hasCriterion ?impactCriterion .\n" +
                        "\t\t?impactCriterion IMMoRTALS:hasProperty ?property\n" +
                        "\t\t; IMMoRTALS:hasCriterion ?propertyCriterion .\n" +
                        "\t}\n" +
                        "}";
                getStrategyImpacts = getStrategyImpacts.replace("???GRAPH_NAME???", graphName).replace("???BINDING???", bindingUUID);
                AssertableSolutionSet impactSolutions = new AssertableSolutionSet();
                client.executeSelectQuery(getStrategyImpacts, impactSolutions);

                if (impactSolutions.getSolutions().size() != 0) {
                    Solution impactSolution = impactSolutions.getSolutions().get(0);
                    String affectedProperty = impactSolution.get("property");
                    String propertyCriterion = impactSolution.get("propertyCriterion");
                    String strategy = impactSolution.get("strategy");

                    System.out.println("Binding instances found: \n");

                    TriplesToPojo.SparqlPojoContext results = getObjectRepresentation(bindingUUID, BINDING_INSTANCE_TYPE,
                            client, graphName, config);
                    results.forEach(solution -> {
                        PrescriptiveBindingInstance prescriptiveBindingInstance = (PrescriptiveBindingInstance) solution.get("obj");
                        Model m = ObjectToTriples.convert(config, prescriptiveBindingInstance);

                        try {
                            System.out.println(OntologyHelper.serializeModel(m, "TURTLE", false));
                        } catch (IOException exc) {
                            exc.printStackTrace();
                        }
                    });
                    
                    System.out.println("Retrieving details of mitigation strategy: " + strategy
                            + " using query:\n" + getStrategyImpacts);

                    switch (propertyCriterion) {
                        case "PROPERTY_ADDED":
                            String getApplicableAspects = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                    "\n" +
                                    "select ?aspect where {\n" +
                                    "\tgraph<???GRAPH_NAME???> {\n" +
                                    "\t\t?aspect IMMoRTALS:hasImpactStatements ?impacts .\n" +
                                    "\t\t?impacts IMMoRTALS:hasImpactOnProperty \"ADDS\"\n" +
                                    "\t\t; IMMoRTALS:hasImpactedProperty <???PROPERTY???> .\n" +
                                    "\t}\n" +
                                    "}";
                            getApplicableAspects = getApplicableAspects.replace("???GRAPH_NAME???", graphName)
                                    .replace("???PROPERTY???", affectedProperty);

                            System.out.println("Searching for aspects capable of fulfilling required impacts using query:\n"
                                    + getApplicableAspects);
                            AssertableSolutionSet aspectSolutions = new AssertableSolutionSet();
                            client.executeSelectQuery(getApplicableAspects, aspectSolutions);

                            if (aspectSolutions.getSolutions().size() != 0) {
                                // TODO technically should be able to handle multiple aspect solutions... for right now let's
                                // TODO assume there is only one aspect for each issue
                                // TODO Solution aspectSolution = determineWhichAspectToUse(aspectSolutions.getSolutions);
                                //for (Solution aspectSolution : aspectSolutions.getSolutions()) {
                                Solution aspectSolution = aspectSolutions.getSolutions().get(0);
                                String aspectUUID = aspectSolution.get("aspect");

                                String getAspectInstances = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                        "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> \n" +
                                        "\n" +
                                        "select ?method ?aspectInstance where {\n" +
                                        "\tgraph <???GRAPH_NAME???> {\n" +
                                        "\t\t?aspectInstance a IMMoRTALS_dfu_instance:FunctionalAspectInstance\n" +
                                        "\t\t; IMMoRTALS:hasAbstractAspect <???ASPECT???>\n" +
                                        "\t\t; IMMoRTALS:hasMethodPointer ?method .\n" +
                                        "\t}\n" +
                                        "}";
                                getAspectInstances = getAspectInstances.replace("???GRAPH_NAME???", graphName)
                                        .replace("???ASPECT???", aspectUUID);

                                System.out.println("Aspect found: \n");

                                results = getObjectRepresentation(aspectUUID, ASPECT_ABSTRACT_TYPE, client, graphName, config);

                                results.forEach(solution -> {
                                    DefaultAspectBase aspectBase = (DefaultAspectBase) solution.get("obj");
                                    Model m = ObjectToTriples.convert(config, aspectBase);

                                    try {
                                        System.out.println(OntologyHelper.serializeModel(m, "TURTLE", false));
                                    } catch (IOException exc) {
                                        exc.printStackTrace();
                                    }
                                });

                                System.out.println("Searching for instances that perform this abstract aspect using query:" +
                                        getAspectInstances);
                                AssertableSolutionSet instanceSolutions = new AssertableSolutionSet();
                                client.executeSelectQuery(getAspectInstances, instanceSolutions);

                                // TODO similar to above, need a mechanism for deciding which aspect instance to use
                                // TODO Solution instanceSolution = determineWhichInstanceToUse(instanceSolutions.getSolutions());
                                // if (instanceSolutions.getSolutions().size() != 0) {
                                for (Solution instanceSolution : instanceSolutions.getSolutions()) {
                                    String methodPointer = instanceSolution.get("method");

                                    String getAnnotClass = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                            "select distinct ?annotations ?className where {\n" +
                                            "\tgraph<???GRAPH_NAME???> {\n" +
                                            "\t\t?method IMMoRTALS:hasBytecodePointer <???POINTER???>\n" +
                                            "\t\t; IMMoRTALS:hasAnnotations ?annotations .\n" +
                                            "\t\t?annotations IMMoRTALS:hasAnnotationClassName ?className .\n" +
                                            "\t}\n" +
                                            "}";
                                    getAnnotClass = getAnnotClass.replace("???GRAPH_NAME???", graphName)
                                            .replace("???POINTER???", methodPointer);

                                    AssertableSolutionSet annotClassSolutions = new AssertableSolutionSet();
                                    client.executeSelectQuery(getAnnotClass, annotClassSolutions);

                                    if (annotClassSolutions.getSolutions().size() != 0) {

                                        for (Solution annotClassSolution : annotClassSolutions.getSolutions()) {
                                            String annotClass = annotClassSolution.get("className");
                                            if (annotClass.equals("mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/FunctionalAspectAnnotation")) {

                                                String annotUUID = annotClassSolution.get("annotations");

                                                String getAspectClass = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                                        "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n" +
                                                        "select distinct ?value where {\n" +
                                                        "\tgraph<???GRAPH_NAME???> {\n" +
                                                        "\t\t<???ANNOT???> a IMMoRTALS_bytecode:AnAnnotation\n" +
                                                        "\t\t; IMMoRTALS:hasKeyValuePairs ?keyValuePairs .\n" +
                                                        "\t\t?keyValuePairs IMMoRTALS:hasValue ?value .\n" +
                                                        "\t}\n" +
                                                        "}";
                                                getAspectClass = getAspectClass.replace("???GRAPH_NAME???", graphName)
                                                        .replace("???ANNOT???", annotUUID);

                                                AssertableSolutionSet aspectClassSolutions = new AssertableSolutionSet();
                                                client.executeSelectQuery(getAspectClass, aspectClassSolutions);

                                                if (aspectClassSolutions.getSolutions().size() != 0) {
                                                    String aspectClass = aspectClassSolutions.getSolutions().get(0).get("value");
                                                    aspectClass = aspectClass.substring(5, aspectClass.length() - 1);
                                                    Class<?> clazz = Class.forName(aspectClass);
                                                    Object object = clazz.getConstructor().newInstance();
                                                    System.out.println("Aspect instance found: \n");
                                                    String aspectInstanceUUID = instanceSolution.get("aspectInstance");

                                                    results = getObjectRepresentation(aspectInstanceUUID, ASPECT_INSTANCE_TYPE, client,
                                                            graphName, config);
                                                    results.forEach(solution -> {
                                                        FunctionalAspectInstance instance = (FunctionalAspectInstance) solution.get("obj");
                                                        Model m = ObjectToTriples.convert(config, instance);
                                                        try {
                                                            System.out.println(OntologyHelper.serializeModel(m, "TURTLE", false));
                                                        } catch (IOException exc) {
                                                            exc.printStackTrace();
                                                        }

                                                    });
                                                }
                                            }
                                        }
                                    }
                                }
                                System.out.println("Injecting any of the aforementioned instances will repair the delinquent data flow");
                            }
                            break;
                        default:
                            System.out.println("Unable to handle specified property criterion.");
                            break;
                    }
                }
            }
        }
    }
        public static final String CONSTRAINT_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/property/impact#ProscriptiveCauseEffectAssertion";
        public static final String DATAFLOW_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/analysis#DataflowEdge";
        public static final String DATAFLOW_METHOD_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/analysis#MethodInvocationDataflowEdge";
        public static final String DATAFLOW_INTER_METHOD_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/analysis#InterMethodDataflowEdge";
        public static final String DATAFLOW_NODE_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/analysis#DataflowNode";
        public static final String DATAFLOW_METHOD_NODE_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/analysis#MethodInvocationDataflowNode";
        public static final String MITIGATION_STRATEGY_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/property/impact#PrescriptiveCauseEffectAssertion";
        public static final String BINDING_INSTANCE_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/property/impact#PrescriptiveBindingInstance";
        public static final String ASPECT_ABSTRACT_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/functionality/aspects#DefaultAspectBase";
        private static final String ASPECT_INSTANCE_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance";
}
