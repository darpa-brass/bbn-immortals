package com.securboration.immortals.aframes;

import com.securboration.immortals.ontology.frame.CallTrace;
import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;

import com.securboration.immortals.ontology.functionality.DesignPattern;
import com.securboration.immortals.ontology.lang.ProgrammingLanguage;
import com.securboration.immortals.ontology.lang.WrapperSourceFile;
import com.securboration.immortals.utility.Decompiler;
import com.securboration.immortals.utility.GradleTaskHelper;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.constraint.*;
import com.securboration.immortals.ontology.frame.AnalysisFrameAssessmentReport;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;

import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.ImpactStatement;
import com.securboration.immortals.ontology.property.impact.PropertyImpact;
import com.securboration.immortals.repo.query.TriplesToPojo;

import com.securboration.immortals.wrapper.Wrapper;
import com.securboration.immortals.wrapper.WrapperFactory;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static com.securboration.immortals.utility.GradleTaskHelper.*;

public class AnalysisFrameAssessment {

    private static final Logger logger =
            LoggerFactory.getLogger(AnalysisFrameAssessment.class);

    private AnalysisFrameAssessment() {}

    public static AnalysisFrameAssessmentReport analysisFrameValidation(GradleTaskHelper taskHelper, List<GradleTaskHelper.Solution> dataFlows,
                                                                        ObjectToTriplesConfiguration config) throws Exception {

        taskHelper.getPw().println("Beginning analysis frame validation. Searching for dataflows that transcend the process boundary...");
        AnalysisFrameAssessmentReport report = new AnalysisFrameAssessmentReport();
        ArrayList<CallTrace> callTraces = new ArrayList<>();

        for (GradleTaskHelper.Solution dataFlow : dataFlows) {
            String dataFlowUUID = dataFlow.get("dataFlowEdge");
            TriplesToPojo.SparqlPojoContext results = taskHelper.getObjectRepresentation(dataFlowUUID, DATAFLOW_INTER_METHOD_TYPE, config);

            results.forEach(solution ->{
                taskHelper.getPw().println("Dataflow found: " + dataFlowUUID + "\n");
                DataflowEdge edge = (DataflowEdge) solution.get("obj");

                Model m = ObjectToTriples.convert(config.getCleanContext(true), edge);
                try {
                    taskHelper.getPw().println(OntologyHelper.serializeModel(m, "TURTLE", false));
                }catch (IOException exc) {
                    logger.debug(exc.getLocalizedMessage());
                }

                taskHelper.getPw().println("Examining data being transmitted across data flow...");
                DataflowAnalysisFrame frame = edge.getDataflowAnalysisFrame();
                LinkedBlockingQueue<UnWrapper> unWrappers = constructUnwrappers(frame, taskHelper.getPw());
                String getConsumerUUID = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "select ?consumer ?producer where {\n" +
                        "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "    <???EDGE???> IMMoRTALS:hasConsumer ?consumer\n" +
                        "    ; IMMoRTALS:hasProducer ?producer ." +
                        "\t}\n" +
                        "}";
                getConsumerUUID = getConsumerUUID.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???EDGE???", dataFlowUUID);

                GradleTaskHelper.AssertableSolutionSet nodeUUIDSolutions = new GradleTaskHelper.AssertableSolutionSet();
                taskHelper.getClient().executeSelectQuery(getConsumerUUID, nodeUUIDSolutions);
                String consumerUUID = nodeUUIDSolutions.getSolutions().get(0).get("consumer");

                try {
                    callTraces.addAll(unWrapFrames(unWrappers, consumerUUID, taskHelper, frame, config));
                    taskHelper.getPw().println("Dataflow analysis frame validation for data flow " + dataFlowUUID + " is complete.");
                } catch (Exception e) {
                    logger.debug(e.getLocalizedMessage());
                }
            });
        }
        report.setCallTraces(callTraces.toArray(new CallTrace[callTraces.size()]));
        taskHelper.getPw().flush();
        taskHelper.getPw().close();
        return report;
    }

    private static ArrayList<CallTrace> getAllReverseCallTraces(GradleTaskHelper taskHelper, String startNode, CallTrace callTrace,
                                                                DataflowAnalysisFrame frame, ObjectToTriplesConfiguration config) throws Exception {
        ArrayList<DataflowNode> tempNodes = new ArrayList<>(Arrays.asList(callTrace.getCallSteps()));
        ArrayList<String> tempUUIDs = new ArrayList<>(Arrays.asList(callTrace.getCallStepUUIDs()));
        ArrayList<CallTrace> callTraces = new ArrayList<>();
        if (tempNodes.isEmpty()) {
            tempNodes.add(pojofyNode(startNode, taskHelper, config));
            tempUUIDs.add(startNode);
        }
        callTrace = new CallTrace(callTrace);

        ObjectToTriples.convert(config, frame);
        String dataTypeURI = config.getNamingContext().getNameForObject(frame.getAnalysisFrameDataType());

        GradleTaskHelper.AssertableSolutionSet traceSolutions = new GradleTaskHelper.AssertableSolutionSet();
        final String getCalledNodes = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "\n" +
                "select ?nodes ?edge where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t{?edge a*  IMMoRTALS_analysis:DataflowEdge}\n" +
                "\t\tUNION\n" +
                "\t\t{?tester rdfs:subClassOf* IMMoRTALS_analysis:DataflowEdge .\n" +
                "\t\t ?edge a* ?tester .}\n" +
                "\t\t ?edge IMMoRTALS:hasConsumer <???NODE???>\n" +
                "\t\t; IMMoRTALS:hasDataTypeCommunicated <???DATA???>" +
                "\t\t; IMMoRTALS:hasProducer ?nodes .\n" +
                "\t}\n" +
                "}";
        taskHelper.getClient().executeSelectQuery(getCalledNodes.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???NODE???", startNode)
                .replace("???DATA???", dataTypeURI), traceSolutions);

        while (true) {

            int numOfTraces = traceSolutions.getSolutions().size();
            if (numOfTraces > 1) {

                for (int i = 0; i < numOfTraces; i++) {
                    String nodeUUID = traceSolutions.getSolutions().get(i).get("nodes");
                    tempNodes.add(pojofyNode(nodeUUID, taskHelper, config));
                    tempUUIDs.add(nodeUUID);

                    callTrace.setCallSteps(tempNodes.toArray(new DataflowNode[tempNodes.size()]));
                    callTrace.setCallStepUUIDs(tempUUIDs.toArray(new String[tempUUIDs.size()]));
                    callTraces.addAll(getAllReverseCallTraces(taskHelper, nodeUUID, callTrace,
                            taskHelper.isAspectInstance(nodeUUID) ? frame.getAnalysisFrameChild()
                                    : frame, config));

                    int indexToRemove = tempUUIDs.indexOf(nodeUUID);
                    tempNodes.remove(indexToRemove);
                    tempUUIDs.remove(indexToRemove);

                    callTrace.setCallSteps(tempNodes.toArray(new DataflowNode[tempNodes.size()]));
                    callTrace.setCallStepUUIDs(tempUUIDs.toArray(new String[tempUUIDs.size()]));
                }
                return callTraces;
            } else if (numOfTraces == 0) {
                callTrace.setCallSteps(tempNodes.toArray(new DataflowNode[tempNodes.size()]));
                callTrace.setCallStepUUIDs(tempUUIDs.toArray(new String[tempUUIDs.size()]));
                
                callTraces.add(callTrace);
                return callTraces;
            } else {
                String nodeUUID = traceSolutions.getSolutions().get(0).get("nodes");
                tempNodes.add(pojofyNode(nodeUUID, taskHelper, config));
                tempUUIDs.add(nodeUUID);
                callTrace.setCallSteps(tempNodes.toArray(new DataflowNode[tempNodes.size()]));
                callTrace.setCallStepUUIDs(tempUUIDs.toArray(new String[tempUUIDs.size()]));

                traceSolutions.getSolutions().remove(0);

                if (taskHelper.isAspectInstance(nodeUUID)) {
                    frame = frame.getAnalysisFrameChild();

                    if (frame == null) {
                        callTraces.add(callTrace);
                        break;
                    }

                    ObjectToTriples.convert(config, frame);
                    dataTypeURI = config.getNamingContext().getNameForObject(frame.getAnalysisFrameDataType());
                }

                taskHelper.getClient().executeSelectQuery(getCalledNodes.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                        .replace("???NODE???", nodeUUID).replace("???DATA???", dataTypeURI), traceSolutions);
            }
        }
        return callTraces;
    }

    private static ArrayList<CallTrace> getAllCallTraces(GradleTaskHelper taskHelper, String startNode, CallTrace callTrace,
                                                         DataflowAnalysisFrame frame, ObjectToTriplesConfiguration config) throws Exception {

        ArrayList<DataflowNode> tempNodes = new ArrayList<>(Arrays.asList(callTrace.getCallSteps()));
        ArrayList<String> tempUUIDs = new ArrayList<>(Arrays.asList(callTrace.getCallStepUUIDs()));
        ArrayList<CallTrace> callTraces = new ArrayList<>();
        if (tempNodes.isEmpty()) {
            tempNodes.add(pojofyNode(startNode, taskHelper, config));
            tempUUIDs.add(startNode);
        }
        callTrace = new CallTrace(callTrace);
        
        ObjectToTriples.convert(config, frame);
        String dataTypeURI = config.getNamingContext().getNameForObject(frame.getAnalysisFrameDataType());

        GradleTaskHelper.AssertableSolutionSet traceSolutions = new GradleTaskHelper.AssertableSolutionSet();
        final String getCalledNodes = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "\n" +
                "select ?nodes ?edge where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t{?edge a*  IMMoRTALS_analysis:DataflowEdge}\n" +
                "\t\tUNION\n" +
                "\t\t{?tester rdfs:subClassOf* IMMoRTALS_analysis:DataflowEdge .\n" +
                "\t\t ?edge a* ?tester .}\n" +
                "\t\t ?edge IMMoRTALS:hasProducer <???NODE???>\n" +
                "\t\t; IMMoRTALS:hasDataTypeCommunicated <???DATA???>" +
                "\t\t; IMMoRTALS:hasConsumer ?nodes .\n" +
                "\t}\n" +
                "}";
        taskHelper.getClient().executeSelectQuery(getCalledNodes.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???NODE???", startNode)
                .replace("???DATA???", dataTypeURI), traceSolutions);

        while (true) {

            int numOfTraces = traceSolutions.getSolutions().size();
            if (numOfTraces > 1) {

                for (int i = 0; i < numOfTraces; i++) {
                    String nodeUUID = traceSolutions.getSolutions().get(i).get("nodes");
                    tempNodes.add(pojofyNode(nodeUUID, taskHelper, config));
                    tempUUIDs.add(nodeUUID);
                    
                    callTrace.setCallSteps(tempNodes.toArray(new DataflowNode[tempNodes.size()]));
                    callTrace.setCallStepUUIDs(tempUUIDs.toArray(new String[tempUUIDs.size()]));
                    callTraces.addAll(getAllCallTraces(taskHelper, nodeUUID, callTrace, 
                            taskHelper.isAspectInstance(nodeUUID) ? frame.getAnalysisFrameChild()
                            : frame, config));
                    
                    int indexToRemove = tempUUIDs.indexOf(nodeUUID);
                    tempNodes.remove(indexToRemove);
                    tempUUIDs.remove(indexToRemove);
                    
                    callTrace.setCallSteps(tempNodes.toArray(new DataflowNode[tempNodes.size()]));
                    callTrace.setCallStepUUIDs(tempUUIDs.toArray(new String[tempUUIDs.size()]));
                }
                return callTraces;
            } else if (numOfTraces == 0) {
                callTrace.setCallSteps(tempNodes.toArray(new DataflowNode[tempNodes.size()]));
                callTrace.setCallStepUUIDs(tempUUIDs.toArray(new String[tempUUIDs.size()]));
                
                callTraces.add(callTrace);
              
                return callTraces;
            } else {
                String nodeUUID = traceSolutions.getSolutions().get(0).get("nodes");
                tempNodes.add(pojofyNode(nodeUUID, taskHelper, config));
                tempUUIDs.add(nodeUUID);
                callTrace.setCallSteps(tempNodes.toArray(new DataflowNode[tempNodes.size()]));
                callTrace.setCallStepUUIDs(tempUUIDs.toArray(new String[tempUUIDs.size()]));
                
                traceSolutions.getSolutions().remove(0);

                if (taskHelper.isAspectInstance(nodeUUID)) {
                    frame = frame.getAnalysisFrameChild();
                    
                    if (frame == null) {
                        callTraces.add(callTrace);
                        break;
                    }
                    
                    ObjectToTriples.convert(config, frame);
                    dataTypeURI = config.getNamingContext().getNameForObject(frame.getAnalysisFrameDataType());
                }
                
                taskHelper.getClient().executeSelectQuery(getCalledNodes.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                                .replace("???NODE???", nodeUUID).replace("???DATA???", dataTypeURI), traceSolutions);
            }
        }
        return callTraces;
    }

    private static ArrayList<CallTrace> unWrapFrames(LinkedBlockingQueue<UnWrapper> unWrappers, String consumerUUID,
                                                     GradleTaskHelper taskHelper, DataflowAnalysisFrame frame,
                                                     ObjectToTriplesConfiguration config) throws Exception {

        taskHelper.getPw().println("Retrieving all possible paths the program could transfer the observed data across...");
        ArrayList<CallTrace> callTraces = getAllCallTraces(taskHelper, consumerUUID, new CallTrace(), frame, config);

        ArrayList<CallTrace> resultTraces = new ArrayList<>();
        taskHelper.getPw().println("Examining call traces for data inconsistencies...");
       /* for (CallTrace callTrace : callTraces) {
            int i = 0;

            CallTrace originalCallTrace = new CallTrace(callTrace);
            RepairedCallTrace repairedCallTrace = new RepairedCallTrace(callTrace);

            LinkedBlockingQueue<UnWrapper> tempUnwrappers = UnWrapper.deepCopy(unWrappers);

            ArrayList<AspectAugmentationImpact> augmentationImpacts = new ArrayList<>();

            ArrayList<DataflowNode> tempNodes = new ArrayList<>(Arrays.asList(callTrace.getCallSteps()));
            ArrayList<String> tempUUIDs = new ArrayList<>(Arrays.asList(callTrace.getCallStepUUIDs()));
            ArrayList<DataflowNode> repairedTempNodes = new ArrayList<>(Arrays.asList(repairedCallTrace.getCallSteps()));
            ArrayList<String> repairedTempUUIDs = new ArrayList<>(Arrays.asList(repairedCallTrace.getCallStepUUIDs()));

            
            while (!tempNodes.isEmpty() && !tempUnwrappers.isEmpty()) {
               DataflowNode node = tempNodes.remove(0);
               String nodeUUID = tempUUIDs.remove(0);

                if (node instanceof MethodInvocationDataflowNode) {

                    UnWrapper unWrapper = tempUnwrappers.poll();
                    List<Property> propertiesHidingData = unWrapper.getPropertiesToBeRemoved();
                    
                    String methodPointer = ((MethodInvocationDataflowNode) node).getJavaMethodPointer();
                    String getAnnotationClasses = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                            "\n" +
                            "select distinct ?annotations ?className where {\n" +
                            "  graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                            "  ?method IMMoRTALS:hasBytecodePointer \"???POINTER???\"\n" +
                            "  ; IMMoRTALS:hasAnnotations ?annotations .\n" +
                            "  \n" +
                            "  ?annotations IMMoRTALS:hasAnnotationClassName ?className .\n" +
                            "}\n" +
                            "}\n";

                    getAnnotationClasses = getAnnotationClasses.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???POINTER???", methodPointer);
                    GradleTaskHelper.AssertableSolutionSet annotationSolutions = new GradleTaskHelper.AssertableSolutionSet();

                    taskHelper.getClient().executeSelectQuery(getAnnotationClasses, annotationSolutions);
                    for (GradleTaskHelper.Solution annotationSolution : annotationSolutions.getSolutions()) {

                        String annotClassName = annotationSolution.get("className");

                        if (annotClassName.equals("mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/FunctionalAspectAnnotation")) {
                            taskHelper.getPw().print("Node: " + nodeUUID + " is a functional aspect instance implementing: ");

                            String annotationUUID = annotationSolution.get("annotations");

                            String getKeyValuePairs = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                    "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> \n" +
                                    "\n" +
                                    "select distinct ?value where {\n" +
                                    "  graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                                    "  <???ANNOT???> a IMMoRTALS_bytecode:AnAnnotation\n" +
                                    "  ; IMMoRTALS:hasKeyValuePairs ?keyValuePairs .\n" +
                                    "  ?keyValuePairs IMMoRTALS:hasValue ?value\n" +
                                    "}\n" +
                                    "}\n";
                            getKeyValuePairs = getKeyValuePairs.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???ANNOT???", annotationUUID);
                            GradleTaskHelper.AssertableSolutionSet keyValueSolutions = new GradleTaskHelper.AssertableSolutionSet();

                            taskHelper.getClient().executeSelectQuery(getKeyValuePairs, keyValueSolutions);

                            if (!keyValueSolutions.getSolutions().isEmpty()) {

                                GradleTaskHelper.Solution keyValueSolution = keyValueSolutions.getSolutions().get(0);
                                String aspectClassName = keyValueSolution.get("value");
                                aspectClassName = aspectClassName.substring(5, aspectClassName.length() - 1);
                                taskHelper.getPw().println(aspectClassName);
                                try {
                                    Object obj = Class.forName(aspectClassName).getConstructor().newInstance();
                                    if (obj instanceof FunctionalAspect) {

                                        FunctionalAspect aspect = (FunctionalAspect) obj;

                                        if (tempUnwrappers.isEmpty()) {

                                            if (aspect.getImpactStatements() == null) {
                                                aspect.setImpactStatements(new ImpactStatement[] {});
                                            }

                                            if (aspect.getImpactStatements().length != 0) {
                                                taskHelper.getPw().write("Data inconsistency detected in call trace: \nBeginning of trace -> ");
                                                printCallTrace(taskHelper, originalCallTrace);
                                            }

                                            for (ImpactStatement impactStatement : aspect.getImpactStatements()) {

                                                if (impactStatement instanceof PropertyImpact) {
                                                    PropertyImpact propertyImpact = (PropertyImpact) impactStatement;
                                                    PropertyImpact hiddenPropertyImpact = new PropertyImpact();
                                                    hiddenPropertyImpact.setImpactOnProperty(PropertyImpactType.ADDS);
                                                    hiddenPropertyImpact.setImpactedProperty(propertyImpact.getImpactedProperty());

                                                    FunctionalAspectInstance[] aspectInstances = taskHelper.getInstancesFromImpactStatement(hiddenPropertyImpact, config);
                                                    augmentationImpacts.add(taskHelper.constructAspectImpact(aspectInstances, nodeUUID, AspectAugmentationSpecification.AUGMENT_ONE,
                                                            ((MethodInvocationDataflowNode) node).getLineNumber() - 1, config));

                                                    if (aspectInstances.length > 1) {
                                                        taskHelper.getPw().println("Multiple instances found that implement required aspect. The following instances" +
                                                                " are interchangeable;\nfor simplicity's sake the system will use the first one found, but feel free \n" +
                                                                "to substitute as you see fit. An asterisk will placed next to the instance's name in the final output to reflect this.\n");

                                                        for (FunctionalAspectInstance aspectInstance : aspectInstances) {
                                                            taskHelper.getPw().println(aspectInstance.getMethodPointer() +  ",");
                                                        }
                                                        taskHelper.getPw().println("Inserting a call at line number " + (((MethodInvocationDataflowNode) node).getLineNumber() - 1) +
                                                                " in method " + ((MethodInvocationDataflowNode) node).getJavaMethodName() + " in class " + ((MethodInvocationDataflowNode) node).getJavaClassName() +
                                                                " to any of the above will resolve the data inconsistency for:" + nodeUUID);
                                                    } else if (aspectInstances.length == 0) {
                                                        taskHelper.getPw().println("No instances found that implement required aspect. " +
                                                                "Implement an instance and rerun analysis.");
                                                        return (ArrayList<CallTrace>) Collections.EMPTY_LIST;
                                                    } else {
                                                        taskHelper.getPw().println("Single instance found: " + aspectInstances[0].getMethodPointer());
                                                    }

                                                    String getMethodFromPointer = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                                            "select ?methodName ?className where {\n" +
                                                            "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                                                            "\t\t?method IMMoRTALS:hasBytecodePointer \"???POINTER???\"\n" +
                                                            "\t\t; IMMoRTALS:hasMethodName ?methodName\n" +
                                                            "\t\t; IMMoRTALS:hasOwner ?class .\n" +
                                                            "\t\t\n" +
                                                            "\t\t?class IMMoRTALS:hasClassName ?className .\n" +
                                                            "\t}\n" +
                                                            "}";
                                                    getMethodFromPointer = getMethodFromPointer.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                                                            .replace("???POINTER???", aspectInstances[0].getMethodPointer());
                                                    AssertableSolutionSet methodSolutions = new AssertableSolutionSet();
                                                    taskHelper.getClient().executeSelectQuery(getMethodFromPointer, methodSolutions);

                                                    String aspectMethodName = methodSolutions.getSolutions().get(0).get("methodName");
                                                    aspectClassName = methodSolutions.getSolutions().get(0).get("className");

                                                    MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
                                                    methodInvocationDataflowNode.setJavaClassName(aspectClassName);
                                                    methodInvocationDataflowNode.setJavaMethodName(aspectMethodName + "*");
                                                    methodInvocationDataflowNode.setJavaMethodPointer(aspectInstances[0].getMethodPointer());

                                                    repairedTempNodes.add(i, methodInvocationDataflowNode);
                                                    repairedTempUUIDs.add(i, "notSureWhatToPut");
                                                }
                                            }
                                        } else {

                                            Class<? extends DataType> aspectExpectedInput = aspect.getInputs()[0].getType();
                                            taskHelper.getPw().println("Functional aspect instance expects input: " + aspectExpectedInput.getCanonicalName());
                                            // we can now assume that the functional aspect method is using our observed data
                                            if (tempUnwrappers.peek().getObservedDataType().equals(aspectExpectedInput)) {
                                                taskHelper.getPw().println("Observed data type is: " + unWrapper.getObservedDataType().getCanonicalName()
                                                        + ", the node is attempting to use the observed data.\n");
                                                
                                                if (aspect.getImpactStatements() == null) {
                                                    aspect.setImpactStatements(new ImpactStatement[] {});
                                                }

                                                for (int j = 0; j < aspect.getImpactStatements().length; j++) {
                                                    ImpactStatement impactStatement = aspect.getImpactStatements()[j];
                                                    // not safe
                                                    PropertyImpact propertyImpact = (PropertyImpact) impactStatement;

                                                    Class<? extends Property> impactedProperty = propertyImpact.getImpactedProperty();
                                                    Property expectedProperty = impactedProperty.getConstructor().newInstance();

                                                    // Hard coded, needs to be inferred or given to us eventually
                                                    if (expectedProperty instanceof Encrypted) {
                                                        ((Encrypted) expectedProperty).setEncryptionAlgorithm(AES_256.class);
                                                    } else if (expectedProperty instanceof Compressed) {
                                                        ((Compressed) expectedProperty).setCompressionAlgorithm(CompressionAlgorithm.class);
                                                    }
                                                    if (propertiesHidingData.isEmpty()) {
                                                        
                                                        taskHelper.getPw().write("Data inconsistency detected in call trace: \nBeginning of trace -> ");

                                                        printCallTrace(taskHelper, originalCallTrace);
                                                        
                                                        taskHelper.getPw().println("End of trace");
                                                        
                                                        taskHelper.getPw().println("Aspect attempts to modify properties that don't exist on the data flow edge.");
                                                        
                                                        PropertyImpact propertyImpact1 = new PropertyImpact();
                                                        propertyImpact1.setImpactedProperty(propertyImpact.getImpactedProperty());
                                                        propertyImpact1.setImpactOnProperty(PropertyImpactType.ADDS);

                                                        ArrayList<FunctionalAspectInstance> aspectInstances = taskHelper.getInstanceWithMultipleImpacts(Arrays.copyOfRange(
                                                                aspect.getImpactStatements(), j, aspect.getImpactStatements().length), config);

                                                        augmentationImpacts.add(taskHelper.constructAspectImpact(aspectInstances.toArray(new FunctionalAspectInstance[aspectInstances.size()]),
                                                                nodeUUID, AspectAugmentationSpecification.AUGMENT_ALL,
                                                                ((MethodInvocationDataflowNode) node).getLineNumber() + 1, config));
                                                        break;
                                                         
                                                    }

                                                    for (int k = 0; k < propertiesHidingData.size(); k++) {
                                                        Property hiddenProperty = propertiesHidingData.get(k);

                                                        PropertyMisMatch propertyMisMatch = new PropertyMisMatch();
                                                        propertyMisMatch.setProperty1(expectedProperty);
                                                        propertyMisMatch.setProperty2(hiddenProperty);

                                                        if (compareProperties(hiddenProperty, expectedProperty, propertyMisMatch, taskHelper.getPw())) {
                                                            propertiesHidingData.remove(k);
                                                            break;
                                                        }
                                                    }
                                                }

                                                if (!propertiesHidingData.isEmpty()) {

                                                    taskHelper.getPw().write("Data inconsistency detected in call trace: ");//\nBeginning of trace -> ");
                                                    printCallTrace(taskHelper, originalCallTrace);

                                                    if (propertiesHidingData.size() == 1) {
                            
                                                        FunctionalAspectInstance[] aspectInstances = getInstancesForSingleImpact(propertiesHidingData.get(0).getClass(), PropertyImpactType.REMOVES,
                                                                node, nodeUUID, taskHelper, config);
                                                        augmentationImpacts.add(taskHelper.constructAspectImpact(aspectInstances, nodeUUID,
                                                               AspectAugmentationSpecification.AUGMENT_ONE, ((MethodInvocationDataflowNode) node).getLineNumber() - 1, config));

                                                        repairedTempNodes.add(i, getNodeFromPointer(aspectInstances[0], taskHelper));
                                                        repairedTempUUIDs.add(i, "notSureWhatToPut");

                                                        tempNodes.add(0, node);
                                                        tempUUIDs.add(0, nodeUUID);

                                                    } else {
                                                        
                                                        ArrayList<FunctionalAspectInstance> aspectInstances = getInstancesWithMultipleImpacts(propertiesHidingData, taskHelper, config);
                                                        AspectAugmentationImpact aspectAugmentationImpact = taskHelper.constructAspectImpact(aspectInstances.toArray(
                                                                new FunctionalAspectInstance[aspectInstances.size()]), nodeUUID, AspectAugmentationSpecification.AUGMENT_ALL,
                                                                ((MethodInvocationDataflowNode) node).getLineNumber() - 1, config);

                                                        TriplesToPojo.SparqlPojoContext consumerResults = taskHelper.getObjectRepresentation(nodeUUID, DATAFLOW_NODE_TYPE, config);
                                                        consumerResults.forEach(consumerResult -> aspectAugmentationImpact.setAugmentationNode((DataflowNode) consumerResult.get("obj")));
                                                        augmentationImpacts.add(aspectAugmentationImpact);
                                                    }
                                                } else {
                                                    taskHelper.getPw().println("Node successfully handled data.\n");
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception exc) {
                                    logger.debug(exc.getLocalizedMessage());
                                }
                            }
                        }
                    }
                    
                    if (!propertiesHidingData.isEmpty()) {
                        
                        if (propertiesHidingData.size() > 1) {

                            ArrayList<FunctionalAspectInstance> aspectInstances = getInstancesWithMultipleImpacts(propertiesHidingData, taskHelper, config);
                            AspectAugmentationImpact aspectAugmentationImpact = taskHelper.constructAspectImpact(aspectInstances.toArray(
                                    new FunctionalAspectInstance[aspectInstances.size()]), nodeUUID, AspectAugmentationSpecification.AUGMENT_ALL,
                                    ((MethodInvocationDataflowNode) node).getLineNumber() - 1, config);

                            TriplesToPojo.SparqlPojoContext consumerResults = taskHelper.getObjectRepresentation(nodeUUID, DATAFLOW_NODE_TYPE, config);
                            consumerResults.forEach(consumerResult -> aspectAugmentationImpact.setAugmentationNode((DataflowNode) consumerResult.get("obj")));
                            augmentationImpacts.add(aspectAugmentationImpact);

                        } else {
                            FunctionalAspectInstance[] aspectInstances = getInstancesForSingleImpact(propertiesHidingData.get(0).getClass(), PropertyImpactType.REMOVES,
                                    node, nodeUUID, taskHelper, config);
                            
                            for (FunctionalAspectInstance aspectInstance : aspectInstances) {
                                
                                switch (aspectInstance.getDesignPattern()) {
                                    case FUNCTIONAL:
                                        augmentationImpacts.add(taskHelper.constructAspectImpact(aspectInstances, nodeUUID, AspectAugmentationSpecification.AUGMENT_ONE,
                                                ((MethodInvocationDataflowNode) node).getLineNumber() - 1, config));
                                        break;
                                    case STREAM:
                                        augmentationImpacts.addAll(developInstanceStreamSolution(aspectInstance, callTrace, taskHelper, 
                                                config));
                                        break;
                                    default:
                                        break;
                                }
                            }
                            
                            repairedTempNodes.add(i, getNodeFromPointer(aspectInstances[0], taskHelper));
                            repairedTempUUIDs.add(i, "notSureWhatToPut");

                            tempNodes.add(0, node);
                            tempUUIDs.add(0, nodeUUID);
                        }
                    }
                }
                i++;
            }

            repairedCallTrace.setCallSteps(repairedTempNodes.toArray(new DataflowNode[repairedTempNodes.size()]));
            repairedCallTrace.setCallStepUUIDs(repairedTempUUIDs.toArray(new String[repairedTempUUIDs.size()]));
            repairedCallTrace.setAspectAugmentationImpacts(augmentationImpacts.toArray(new AspectAugmentationImpact[augmentationImpacts.size()]));

            if (originalCallTrace.getCallSteps().length == (repairedTempNodes.size())) {
                taskHelper.getPw().println("Call trace contains no data inconsistencies and is already optimized; no repairs needed.");
                originalCallTrace.setRepairedCallTrace(null);
                resultTraces.add(originalCallTrace);
            } else {
                taskHelper.getPw().print("Call trace contains data inconsistencies and/or needs optimizations; original trace: ");//\nBeginning of trace -> ");

                printCallTrace(taskHelper, originalCallTrace);

                taskHelper.getPw().print("Repaired trace: ");//\nBeginning of trace ->");
                printCallTrace(taskHelper, repairedCallTrace);
                
                originalCallTrace.setRepairedCallTrace(repairedCallTrace);
                resultTraces.add(originalCallTrace);
            }
        }*/
        return resultTraces;
    }
    

    private static DataflowNode pojofyNode(String nodeUUID, GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config) throws Exception {

        TriplesToPojo.SparqlPojoContext nodePojoResults = taskHelper.getObjectRepresentation(nodeUUID, DATAFLOW_NODE_TYPE, config);

        DataflowNode node = null;
        for (Map<String, Object> nodePojo : nodePojoResults) {

            node = (DataflowNode) nodePojo.get("obj");

            Model m = ObjectToTriples.convert(config.getCleanContext(true), node);
            try {
                OntologyHelper.serializeModel(m, "TURTLE", false);
            } catch (IOException exc) {
                logger.debug(exc.getLocalizedMessage());
            }
        }
        return node;
    }

    private static DataflowEdge pojofyEdge(String edgeUUID, GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config) throws Exception {

        TriplesToPojo.SparqlPojoContext nodePojoResults = taskHelper.getObjectRepresentation(edgeUUID, DATAFLOW_TYPE, config);

        for (Map<String, Object> nodePojo : nodePojoResults) {

            DataflowEdge edge = (DataflowEdge) nodePojo.get("obj");

            Model m = ObjectToTriples.convert(config.getCleanContext(true), edge);
            String serilazedModel = null;
            try {
                serilazedModel = OntologyHelper.serializeModel(m, "TURTLE", false);
            } catch (IOException exc) {
                logger.debug(exc.getLocalizedMessage());
            }
            taskHelper.getPw().println(serilazedModel + "\n\n");

            return edge;

        }
        return null;
    }

    private static LinkedBlockingQueue<UnWrapper> constructUnwrappers(DataflowAnalysisFrame frame, PrintWriter pw) {

        LinkedBlockingQueue<UnWrapper> unWrappers = new LinkedBlockingQueue<>();

        while (frame != null) {
            UnWrapper unWrapper = new UnWrapper();
            List<Property> properties = new ArrayList<>();
            if (frame.getFrameProperties() != null) {
                for (Property property : frame.getFrameProperties()) {
                    if (property instanceof DataProperty) {
                        DataProperty dataProperty = (DataProperty) property;
                        if (dataProperty.isHidden()) {
                            pw.println("Found property that will need to be handled by consumer: " + property.getClass().getCanonicalName());
                            properties.add(dataProperty);
                        }
                    }
                }
            }

            if (!properties.isEmpty()) {
                unWrapper.setPropertiesToBeRemoved(properties);
                unWrapper.setObservedDataType(frame.getAnalysisFrameDataType());
                unWrappers.add(unWrapper);
            }

            frame = frame.getAnalysisFrameChild();
        }

        return unWrappers;
    }
    
    private static void printCallTrace(GradleTaskHelper taskHelper, CallTrace callTrace) {
        
        taskHelper.getPw().print("Beginning of trace \n-> ");
        
        for (DataflowNode dataflowNode : callTrace.getCallSteps()) {
            if (dataflowNode instanceof InterMethodDataflowNode) {
                InterMethodDataflowNode interMethodDataflowNode = (InterMethodDataflowNode) dataflowNode;
                taskHelper.getPw().write(interMethodDataflowNode.getJavaMethodName() + " -> ");
            } else if (dataflowNode instanceof MethodInvocationDataflowNode) {
                MethodInvocationDataflowNode methodInvocationDataflowNode = (MethodInvocationDataflowNode) dataflowNode;
                taskHelper.getPw().write(methodInvocationDataflowNode.getJavaMethodName() + " -> ");
            }
        }
        
        taskHelper.getPw().println("\nEnd of trace");
    }
    
    private static ArrayList<FunctionalAspectInstance> getInstancesWithMultipleImpacts(List<Property> propertiesHidingData, GradleTaskHelper taskHelper,
                                                                                       ObjectToTriplesConfiguration config) throws Exception {
        ArrayList<ImpactStatement> impactStatements = new ArrayList<>();
        for (Property property : propertiesHidingData) {
            PropertyImpact propertyImpact = new PropertyImpact();
            propertyImpact.setImpactedProperty(property.getClass());
            propertyImpact.setImpactOnProperty(PropertyImpactType.REMOVES);

            impactStatements.add(propertyImpact);
        }

       return taskHelper.getInstanceWithMultipleImpacts(
                impactStatements.toArray(new ImpactStatement[impactStatements.size()]), config);
    }
    
    private static WrapperImplementationImpact developInstanceStreamSolution(FunctionalAspectInstance aspectInstance, InterMethodDataflowNode node,
                                                                                     Set<File> dependencies, GradleTaskHelper taskHelper,
                                                                                     ObjectToTriplesConfiguration config) throws Exception {
        WrapperImplementationImpact wrapperImpact = new WrapperImplementationImpact();
        String applicableStreamObject;
        FunctionalAspect functionalAspect = aspectInstance.getAbstractAspect().newInstance();

        if (functionalAspect.getInverseAspect() == null) {
            applicableStreamObject = "http://darpa.mil/immortals/ontology/r2.0.0/functionality/datatype#InputStream";
        } else {
            applicableStreamObject = "http://darpa.mil/immortals/ontology/r2.0.0/functionality/datatype#OutputStream";
        }
        
        String headOfTraceUUID = config.getNamingContext().getNameForObject(node);
        
        GradleTaskHelper.AssertableSolutionSet traceSolutions = new GradleTaskHelper.AssertableSolutionSet();
        final String getCalledNodes = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> \n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "\n" +
                "select ?nodes ?edge where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t{?edge a*  IMMoRTALS_analysis:DataflowEdge}\n" +
                "\t\tUNION\n" +
                "\t\t{?tester rdfs:subClassOf* IMMoRTALS_analysis:DataflowEdge .\n" +
                "\t\t ?edge a* ?tester .}\n" +
                "\t\t ?edge IMMoRTALS:hasConsumer <???NODE???>\n" +
                "\t\t; IMMoRTALS:hasDataTypeCommunicated <???DATA???>" +
                "\t\t; IMMoRTALS:hasProducer ?nodes .\n" +
                "\t}\n" +
                "}";

        taskHelper.getClient().executeSelectQuery(getCalledNodes.replace("???GRAPH_NAME???", taskHelper.getGraphName()).
                replace("???NODE???", headOfTraceUUID)
                .replace("???DATA???", applicableStreamObject), traceSolutions);
        taskHelper.getPw().println("Getting nodes that called current node using query:\n\n" + getCalledNodes.replace
                ("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???NODE???", headOfTraceUUID)
                .replace("???DATA???", applicableStreamObject) + "\n\n");
        taskHelper.getPw().println("Searching for your specific stream implementation...");
        if (traceSolutions.getSolutions().isEmpty()) {
            taskHelper.getPw().println("No streams found; stream cipher implementation can not be inserted.");
        } else {
            taskHelper.getPw().println("Stream found. Attempting to find where it is initialized...");
            
            String streamStarterNodeUUID = traceSolutions.getSolutions().get(0).get("nodes");
            String streamStarterEdgeUUID = traceSolutions.getSolutions().get(0).get("edge");

            TriplesToPojo.SparqlPojoContext streamStarterEdges = taskHelper.getObjectRepresentation(streamStarterEdgeUUID,
                    DATAFLOW_METHOD_TYPE, config);
            MethodInvocationDataflowEdge streamStarterEdge = null;
            for (Map<String, Object> mapping : streamStarterEdges) {
                streamStarterEdge = (MethodInvocationDataflowEdge) mapping.get("obj");
            }

            ArrayList<CallTrace> callTraces = getAllReverseCallTraces(taskHelper, streamStarterNodeUUID, new CallTrace(),
                    streamStarterEdge.getDataflowAnalysisFrame(), config);
            
            CallTrace streamCallTrace = callTraces.get(0);
            MethodInvocationDataflowNode streamInitializationNode = (MethodInvocationDataflowNode) streamCallTrace.getCallSteps()[streamCallTrace.getCallSteps().length - 1];

            taskHelper.getPw().println("Found where stream is initialized, getting environment information to correctly specify where to implement new wrapper" +
                    " class...");
            
            String getInformationAboutInitMethodCall = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "\t\t\n" +
                    "select * where {\n" +
                    "\tgraph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t\t?aMethod IMMoRTALS:hasMethodName ?methodName\n" +
                    "\t\t; IMMoRTALS:hasInterestingInstructions ?methodCallNodes\n" +
                    "        ; IMMoRTALS:hasOwner ?class .\n" +
                    "    \n" +
                    "        ?class IMMoRTALS:hasClassName ?className .\n" +
                    "\t\t\n" +
                    "\t\t?methodCallNodes  IMMoRTALS:hasSemanticLink <???SEMANTIC_LINK???>" +
                    "\t}\n" +
                    "}";
            getInformationAboutInitMethodCall = getInformationAboutInitMethodCall.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                    .replace("???SEMANTIC_LINK???", config.getNamingContext().getNameForObject(streamInitializationNode));
            taskHelper.getPw().println("Getting surrounding semantic information of method call node using query:\n\n" 
                    + getInformationAboutInitMethodCall + "\n\n");
            AssertableSolutionSet initCallInfoSolution = new AssertableSolutionSet();

            taskHelper.getClient().executeSelectQuery(getInformationAboutInitMethodCall, initCallInfoSolution);
            
            if (initCallInfoSolution.getSolutions().isEmpty()) {
                taskHelper.getPw().println("Unable to gather required information about stream initialization, possible cause is" +
                        " insufficient data flow analysis.");
            }
            
            String initCallOwner = initCallInfoSolution.getSolutions().get(0).get("methodName");
            String initCallOwnerOwner = initCallInfoSolution.getSolutions().get(0).get("className");

            taskHelper.getPw().println("Precise location of initialization found, can now proceed to create wrapper class...");
            
            List<String> dependencyPaths = new ArrayList<>();
            
            for (File dependency : dependencies) {
                dependencyPaths.add(dependency.getAbsolutePath());
            }

            WrapperFactory wrapperFactory = null;
            Wrapper wrapper = null;
            String aspectUUID = null;
            String cipherImpl = null;
            List<String> augmentedMethods = new ArrayList<>();
            boolean alreadyExistingWrapper = false;

            switch (functionalAspect.getAspectId()) {
                
                case "cipherEncrypt": {
                    wrapperFactory = new WrapperFactory();
                    wrapper = wrapperFactory.createWrapper(streamInitializationNode.getJavaClassName().replace("/", "."), dependencyPaths);
                    wrapper.setStreamType("java.io.OutputStream");
                    
                    String getCipherImpl = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                            "select ?cipherImpl where {\n" +
                            "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                            "\t\t\n" +
                            "\t\t?class IMMoRTALS:hasName ?cipherImpl\n" +
                            "\t\t; IMMoRTALS:hasHash ?hash .\n" +
                            "\t\t\n" +
                            "\t\t?dfu IMMoRTALS:hasClassPointer ?hash\n" +
                            "\t\t; IMMoRTALS:hasFunctionalAspects <???ASPECT_INSTANCE???> .\n" +
                            "\t}\n" +
                            "}";
                    getCipherImpl = getCipherImpl.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???ASPECT_INSTANCE???"
                            , config.getNamingContext().getNameForObject(aspectInstance));
                    AssertableSolutionSet cipherImplSoln = new AssertableSolutionSet();

                    taskHelper.getClient().executeSelectQuery(getCipherImpl, cipherImplSoln);
                    cipherImpl = cipherImplSoln.getSolutions().get(0).get("cipherImpl");
                    cipherImpl = cipherImpl.replace("/", ".").replace(".class", "");

                    augmentedMethods.addAll(wrapperFactory.wrapWithCipher(wrapper, cipherImpl, taskHelper));
                    
                    if (augmentedMethods.isEmpty()) {
                        alreadyExistingWrapper = true;
                    }
                    
                    aspectUUID = "http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#AspectCipherEncrypt";
                    break;
                }                  
                case "cipherDecrypt":

                    wrapperFactory = new WrapperFactory();
                    wrapper = wrapperFactory.createWrapper(streamInitializationNode.getJavaClassName().replace("/", "."), dependencyPaths);
                    wrapper.setStreamType("java.io.InputStream");
                    
                    String getCipherImpl = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                            "select ?cipherImpl where {\n" +
                            "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                            "\t\t\n" +
                            "\t\t?class IMMoRTALS:hasName ?cipherImpl\n" +
                            "\t\t; IMMoRTALS:hasHash ?hash .\n" +
                            "\t\t\n" +
                            "\t\t?dfu IMMoRTALS:hasClassPointer ?hash\n" +
                            "\t\t; IMMoRTALS:hasFunctionalAspects <???ASPECT_INSTANCE???> .\n" +
                            "\t}\n" +
                            "}";
                    getCipherImpl = getCipherImpl.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???ASPECT_INSTANCE???"
                            ,config.getNamingContext().getNameForObject(aspectInstance));
                    AssertableSolutionSet cipherImplSoln = new AssertableSolutionSet();

                    taskHelper.getClient().executeSelectQuery(getCipherImpl, cipherImplSoln);
                    cipherImpl = cipherImplSoln.getSolutions().get(0).get("cipherImpl");
                    cipherImpl = cipherImpl.replace("/", ".").replace(".class", "");

                    augmentedMethods.addAll(wrapperFactory.wrapWithCipher(wrapper, cipherImpl, taskHelper));

                    if (augmentedMethods.isEmpty()) {
                        alreadyExistingWrapper = true;
                    }
                    
                    aspectUUID = "http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#AspectCipherDecrypt";
                    break;
            }
            
            if (alreadyExistingWrapper) {
                
                String getPreviouslyAugmentedSource = "prefix IMMoRTALS_constraint: <http://darpa.mil/immortals/ontology/r2.0.0/constraint#> \n" +
                        "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                        "\n" +
                        "select ?sourceFile ?sourceCode where {\n" +
                        "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\t\n" +
                        "\t\t?impacts IMMoRTALS:hasProducedSourceFile ?sourceFile .\n" +
                        "\t\t\n" +
                        "\t\t?sourceFile IMMoRTALS:hasFileName \"???FILE_NAME???\"\n" +
                        "\t\t; IMMoRTALS:hasSource ?sourceCode .\n" +
                        "\t}\n" +
                        "}";
                getPreviouslyAugmentedSource = getPreviouslyAugmentedSource.replace("???FILE_NAME???",
                        "Wrapper" + wrapper.getWrappedClass().getShortName() + ".java")
                                    .replace("???GRAPH_NAME???", taskHelper.getGraphName());
                AssertableSolutionSet sourceSolutions = new AssertableSolutionSet();
                
                taskHelper.getClient().executeSelectQuery(getPreviouslyAugmentedSource, sourceSolutions);
                
                Solution sourceSolution = sourceSolutions.getSolutions().get(0);
                String source = sourceSolution.get("sourceCode"); // retrieve source from ontology
                

                TriplesToPojo.SparqlPojoContext sourceFileResults = taskHelper.getObjectRepresentation(sourceSolution.get("sourceFile"),
                        WRAPPER_SOURCE_FILE_TYPE, config);
                
                WrapperSourceFile sourceFile = null;
                for (Map<String, Object> sourceFileResult : sourceFileResults) {
                    sourceFile = (WrapperSourceFile) sourceFileResult.get("obj");
                }
                
                String[] existingAugmentedMethods = sourceFile.getAugmentedMethods();
                ArrayList<String> augmentedMethodsList = new ArrayList<>(Arrays.asList(existingAugmentedMethods));
                
                source = taskHelper.parseRawCode(source, wrapper.getWrappedClass().getName().replace(".", "/"),
                        augmentedMethodsList, wrapper.getStreamType(), aspectUUID, cipherImpl);
                sourceFile.setSource(source);
                String[] newAugmentedMethods = new String[existingAugmentedMethods.length + augmentedMethodsList.size()];
                
                for (int i = 0; i < existingAugmentedMethods.length; i++) {
                    newAugmentedMethods[i] = existingAugmentedMethods[i];
                }
                
                Iterator<String> augmentedMethodsIter = augmentedMethodsList.iterator();
                for (int i = existingAugmentedMethods.length; i < newAugmentedMethods.length; i++) {
                    newAugmentedMethods[i] = augmentedMethodsIter.next();
                }
                
                sourceFile.setAugmentedMethods(newAugmentedMethods);
                taskHelper.getClient().addToModel(ObjectToTriples.convert(config, sourceFile), taskHelper.getGraphName());

                Files.write(Paths.get(sourceFile.getFileSystemPath()), Collections.singleton(source), StandardCharsets.UTF_8);
                return null;
            }
            
            
            taskHelper.getPw().println("Wrapper class created: Wrapper" + wrapper.getWrappedClass().getShortName() +
                ". It will be placed in the specified plugin directory. Replace your stream implementation in " + initCallOwnerOwner 
                + ", method " + initCallOwner + ", at line number " + streamInitializationNode.getLineNumber() + 
                " with a reference to the produced wrapper class.\n");
            
            String classFileLocation = wrapperFactory.produceWrapperClassFile(wrapper);

            Decompiler decompiler = new Decompiler();
            File javaSource = null;
            String source = null;
            //TODO check make sure this works
            if (taskHelper.getResultsDir() == null) {
                javaSource = decompiler.decompileClassFile(classFileLocation, 
                        classFileLocation.substring(0, classFileLocation.lastIndexOf("/"))
                        + "/Wrapper" + wrapper.getWrappedClass().getShortName() + ".java", false);
                source = taskHelper.parseWrapperClasses(javaSource, aspectUUID,
                        wrapper.getWrappedClass().getName().replace(".", "/"), augmentedMethods);
            } else {
                javaSource = decompiler.decompileClassFile(classFileLocation, taskHelper.getResultsDir()
                        + "/Wrapper" + wrapper.getWrappedClass().getShortName() + ".java", true);
                source = taskHelper.parseWrapperClasses(javaSource, aspectUUID,
                        wrapper.getWrappedClass().getName().replace(".", "/"), augmentedMethods);
            }
            String[] augmentedMethodsArray = new String[augmentedMethods.size()];
            
            for (int i = 0; i < augmentedMethodsArray.length; i++) {
                augmentedMethodsArray[i] = augmentedMethods.get(i);
            }
            
            wrapperImpact.setAspectImplemented(functionalAspect.getClass());
            wrapperImpact.setInitializationNode(streamInitializationNode);
            WrapperSourceFile producedFile = new WrapperSourceFile();
            producedFile.setSource(source);
            producedFile.setAugmentedMethods(augmentedMethodsArray);
            producedFile.setFileSystemPath(javaSource.getAbsolutePath().replace("\\", "/"));
            
            producedFile.setFileName(javaSource.getName());
            ProgrammingLanguage sourceLanguage = new ProgrammingLanguage();
            sourceLanguage.setLanguageName("java");
            sourceLanguage.setVersionTag("8");
            producedFile.setLanguageModel(sourceLanguage);
            wrapperImpact.setProducedSourceFile(producedFile);
            taskHelper.getClient().addToModel(ObjectToTriples.convert(config, wrapperImpact), taskHelper.getGraphName());
        }
        return wrapperImpact;
    }
    
    
    private static void getEveryStepsImpact(GradleTaskHelper taskHelper, CallTrace callTrace) {
        
        callTrace.setCallSteps(Arrays.copyOfRange(callTrace.getCallSteps(), 1, callTrace.getCallSteps().length));
        callTrace.setCallStepUUIDs(Arrays.copyOfRange(callTrace.getCallStepUUIDs(), 1, callTrace.getCallStepUUIDs().length));
        
        for (DataflowNode dataflowNode : callTrace.getCallSteps()) {
            
            if (dataflowNode instanceof MethodInvocationDataflowNode) {
                
                MethodInvocationDataflowNode methodInvocationDataflowNode = (MethodInvocationDataflowNode) dataflowNode;
                
                String getInvokeNodeInfo = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\t\t\n" +
                        "select * where {\n" +
                        "\tgraph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\t?aMethod IMMoRTALS:hasMethodName ?methodName\n" +
                        "\t\t; IMMoRTALS:hasInterestingInstructions ?methodCallNodes\n" +
                        "        ; IMMoRTALS:hasOwner ?class .\n" +
                        "    \n" +
                        "        ?class IMMoRTALS:hasClassName ?className .\n" +
                        "\t\t\n" +
                        "\t\t?methodCallNodes IMMoRTALS:hasCalledMethodName  \"???METHOD_NAME???\"\n" +
                        "\t\t; IMMoRTALS:hasOwner \"???OWNER_NAME???\" .\n" +
                        "\t}\n" +
                        "}";
                getInvokeNodeInfo = getInvokeNodeInfo.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                        .replace("???METHOD_NAME???", methodInvocationDataflowNode.getJavaMethodName()).replace("???OWNER_NAME???",
                                methodInvocationDataflowNode.getJavaClassName());
                AssertableSolutionSet invokeNodeInfoSolution = new AssertableSolutionSet();
                
                taskHelper.getClient().executeSelectQuery(getInvokeNodeInfo, invokeNodeInfoSolution);
                
                taskHelper.getPw().println("Found affected code segment in method call to: " + methodInvocationDataflowNode.getJavaMethodName()
                + " in method: " + invokeNodeInfoSolution.getSolutions().get(0).get("methodName") + "in class: " 
                + invokeNodeInfoSolution.getSolutions().get(0).get("className"));
                
            } else if (dataflowNode instanceof InterMethodDataflowNode) {
                InterMethodDataflowNode interMethodDataflowNode = (InterMethodDataflowNode) dataflowNode;

                String getInvokeNodeInfo = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\t\t\n" +
                        "select * where {\n" +
                        "\tgraph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\t?aMethod IMMoRTALS:hasMethodName ?methodName\n" +
                        "\t\t; IMMoRTALS:hasInterestingInstructions ?methodCallNodes\n" +
                        "        ; IMMoRTALS:hasOwner ?class .\n" +
                        "    \n" +
                        "        ?class IMMoRTALS:hasClassName ?className .\n" +
                        "\t\t\n" +
                        "\t\t?methodCallNodes IMMoRTALS:hasCalledMethodName  \"???METHOD_NAME???\"\n" +
                        "\t\t; IMMoRTALS:hasOwner \"???OWNER_NAME???\" .\n" +
                        "\t}\n" +
                        "}";
                getInvokeNodeInfo = getInvokeNodeInfo.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                        .replace("???METHOD_NAME???", interMethodDataflowNode.getJavaMethodPointer().replace("???OWNER_NAME???",
                                interMethodDataflowNode.getJavaClassName()));
                AssertableSolutionSet invokeNodeInfoSolution = new AssertableSolutionSet();

                taskHelper.getClient().executeSelectQuery(getInvokeNodeInfo, invokeNodeInfoSolution);

                taskHelper.getPw().println("Found affected code segment in method call to: " + interMethodDataflowNode.getJavaMethodName()
                        + " in method: " + invokeNodeInfoSolution.getSolutions().get(0).get("methodName") + "in class: "
                        + invokeNodeInfoSolution.getSolutions().get(0).get("className"));
            }
        }
    }
    
    public static AnalysisImpact repairConsumer(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config,
                                      InterMethodDataflowNode consumer, PropertyImpact propertyImpact, Set<File> dependencies,
                                                            DesignPattern flowDesign) throws Exception {
        
        AnalysisImpact analysisImpact = new AnalysisImpact();
        taskHelper.getPw().println("Finding instances that implement the required functional aspect...");
        FunctionalAspectInstance[] aspectInstances = taskHelper.getInstancesFromImpactStatement(propertyImpact, config);

        if (aspectInstances.length == 0) {
            taskHelper.getPw().println("Unable to find instance(s) required for repairing system, implement instance and re-run analysis.");
            return analysisImpact;
        }
        
        for (FunctionalAspectInstance aspectInstance : aspectInstances) {
            taskHelper.getPw().println("Instance found. Determining its design pattern...");
            switch (flowDesign) {
                case FUNCTIONAL:
                    taskHelper.getPw().println("Instance with method pointer: " + aspectInstance.getMethodPointer() + " utilizes a block design " +
                            "pattern. Repairs will occur immediately adjacent to the inter-process boundary...");
                    analysisImpact = taskHelper.constructAspectImpact(aspectInstance, consumer,
                            AspectAugmentationSpecification.AUGMENT_ONE, consumer.getLineNumber() + 1, config);
                    break;
                case STREAM:
                    taskHelper.getPw().println("Instance with method pointer: " + aspectInstance.getMethodPointer() + " utilizes a stream design pattern." +
                            " Repairs will vary based on the code system's stream implementation. Immortals will begin " +
                            "the process of wrapping the stream implementation in a custom class...");
                    analysisImpact = developInstanceStreamSolution(aspectInstance, consumer, dependencies, taskHelper,
                            config);
                    break;
                default:
                    break;
            }
        }
       return analysisImpact;
    }


    public static AnalysisImpact repairProducer(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config,
                                                            InterMethodDataflowNode producer, PropertyImpact propertyImpact, Set<File> dependencies,
                                                            DesignPattern flowDesign) throws Exception {
        
        AnalysisImpact analysisImpact = new AnalysisImpact();
        taskHelper.getPw().println("Finding instances that implement the required functional aspect...\n\n");
        FunctionalAspectInstance[] aspectInstances = taskHelper.getInstancesFromImpactStatement(propertyImpact, config);

        if (aspectInstances.length == 0) {
            taskHelper.getPw().println("Unable to find instance(s) required for repairing system, implement instance and re-run analysis.");
            return analysisImpact;
        }
        
        taskHelper.getPw().println("Instances found. Determining their design pattern...");
        for (FunctionalAspectInstance aspectInstance : aspectInstances) {

            switch (flowDesign) {
                case FUNCTIONAL:
                    taskHelper.getPw().println("Instance with method pointer: " + aspectInstance.getMethodPointer() + " utilizes a block design " +
                            "pattern.");
                    analysisImpact = taskHelper.constructAspectImpact(aspectInstance, producer,
                            AspectAugmentationSpecification.AUGMENT_ONE, producer.getLineNumber() - 1, config);
                    break;
                case STREAM:
                    taskHelper.getPw().println("Instance with method pointer: " + aspectInstance.getMethodPointer() + " utilizes a stream design pattern." +
                            " Immortals will begin the process of wrapping the stream implementation in a custom class...");
                    analysisImpact = developInstanceStreamSolution(aspectInstance, producer, dependencies, taskHelper,
                            config);
                    break;
                default:
                    break;
            }
        }
        return analysisImpact;
    }
}
