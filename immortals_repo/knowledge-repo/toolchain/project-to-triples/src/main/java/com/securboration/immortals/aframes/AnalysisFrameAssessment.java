package com.securboration.immortals.aframes;

import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.frame.CallTrace;
import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;

import com.securboration.immortals.ontology.functionality.*;
import com.securboration.immortals.ontology.functionality.alg.encryption.*;
import com.securboration.immortals.ontology.functionality.aspects.AspectConfigureRequest;
import com.securboration.immortals.ontology.functionality.aspects.AspectConfigureSolution;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.lang.AugmentedMethodInvocation;
import com.securboration.immortals.ontology.lang.AugmentedUserSourceFile;
import com.securboration.immortals.ontology.lang.WrapperSourceFile;
import com.securboration.immortals.ontology.pattern.spec.CodeSpec;
import com.securboration.immortals.ontology.property.impact.AnalysisImpact;
import com.securboration.immortals.utility.Decompiler;
import com.securboration.immortals.utility.GradleTaskHelper;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.constraint.*;
import com.securboration.immortals.ontology.frame.AnalysisFrameAssessmentReport;

import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.ImpactStatement;
import com.securboration.immortals.ontology.property.impact.PropertyImpact;
import com.securboration.immortals.repo.query.TriplesToPojo;

import com.securboration.immortals.wrapper.Wrapper;
import com.securboration.immortals.wrapper.WrapperFactory;
import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static Wrapper constructWrapperFoundation(FunctionalAspectInstance aspectInstance, InterMethodDataflowNode node,
                                                     Set<File> dependencies, GradleTaskHelper taskHelper,
                                                     ObjectToTriplesConfiguration config, WrapperImplementationImpact impact,
                                                     String cipherImpl) throws Exception {

        boolean previouslyAugmented = false;
        FunctionalAspect functionalAspect = aspectInstance.getAbstractAspect().newInstance();
        impact.setAspectImplemented(functionalAspect.getClass());
        String applicableStreamObject = AnalysisFrameAssessment.getStreamObject(functionalAspect);
        AssertableSolutionSet traceSolutions = findStreamImplementationInUsersCode(node, taskHelper, applicableStreamObject);

        if (traceSolutions.getSolutions().isEmpty()) {
            taskHelper.getPw().println("No streams found; stream cipher implementation can not be inserted.");
            return null;
        } else {
            taskHelper.getPw().println("Stream found. Attempting to find where it is initialized...");

            ArrayList<CallTrace> callTraces = findWrapperInsertionSite(taskHelper, config, traceSolutions);
            CallTrace streamCallTrace = callTraces.get(0);
            MethodInvocationDataflowNode streamInitializationNode = (MethodInvocationDataflowNode)
                    streamCallTrace.getCallSteps()[streamCallTrace.getCallSteps().length - 1];

            taskHelper.getPw().println("Found where stream is initialized, getting environment information to correctly specify where to implement new wrapper" +
                    " class...");

            AssertableSolutionSet initCallInfoSolution = getInitializationData(taskHelper, config, streamInitializationNode);

            String initCallOwnerName = initCallInfoSolution.getSolutions().get(0).get("methodName");
            String initCallOwnerOwner = initCallInfoSolution.getSolutions().get(0).get("class");
            String initCallOwnerOwnerName = initCallInfoSolution.getSolutions().get(0).get("className");

            List<MethodInvocationDataflowNode> methodNodes = new ArrayList<>();
            MethodInvocationDataflowNode initializerNode = (MethodInvocationDataflowNode)
                    streamCallTrace.getCallSteps()[streamCallTrace.getCallSteps().length - 1];
            for (DataflowNode dataflowNode : streamCallTrace.getCallSteps()) {
                if (dataflowNode instanceof MethodInvocationDataflowNode) {
                    methodNodes.add((MethodInvocationDataflowNode) dataflowNode);
                }
            }

            methodNodes.removeIf(methodNode -> !methodNode.getJavaClassName().equals
                    (streamInitializationNode.getJavaClassName()));

            AssertableSolutionSet ownerSourceSolutions = getWrapperInsertionSiteContext(taskHelper, initCallOwnerOwnerName);

            String applicationSourceFileName = "fileNameNotFound.java";
            String applicationSource;
            List<String> applicationSourceLines = new ArrayList<>();
            if (!ownerSourceSolutions.getSolutions().isEmpty()) {
                applicationSourceFileName = ownerSourceSolutions.getSolutions().get(0).get("fileName");
                applicationSource = ownerSourceSolutions.getSolutions().get(0).get("source");
                applicationSourceLines = Arrays.asList(applicationSource.split("\n"));
            }

            AugmentedUserSourceFile augmentedUserSourceFile = new AugmentedUserSourceFile();
            augmentedUserSourceFile.setFileName(applicationSourceFileName);
            String getDependentFiles = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "\n" +
                    "select ?dependentFiles where {\n" +
                    "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t\t<???CLASS???> IMMoRTALS:hasDependentFiles ?dependentFiles .\n" +
                    "\t}\n" +
                    "}";
            getDependentFiles = getDependentFiles.replace("???GRAPH_NAME???", taskHelper.getGraphName()).
                    replace("???CLASS???", initCallOwnerOwner);
            AssertableSolutionSet dependentFileSolutions = new AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getDependentFiles, dependentFileSolutions);

            taskHelper.getPw().println("Precise location of initialization found, can now proceed to create wrapper class...");

            Set<String> dependentFiles = sanitizeDependencyPaths(dependentFileSolutions);
            List<String> dependencyPaths = new ArrayList<>();
            for (File dependency : dependencies) {
                dependencyPaths.add(dependency.getAbsolutePath());
            }

            WrapperFactory wrapperFactory = null;
            Wrapper wrapper = null;
            WrapperSourceFile[] producedSourceFiles = new WrapperSourceFile[3];
            boolean plugin = analysisViaPlugin(taskHelper);

            switch (functionalAspect.getAspectId()) {

                case "cipherEncrypt": {
                    wrapperFactory = new WrapperFactory();
                    wrapper = initializeWrapper(streamInitializationNode, dependentFiles, dependencyPaths,
                            wrapperFactory, "java.io.OutputStream");

                    previouslyAugmented = wrapperFactory.wrapWithCipher(wrapper, cipherImpl, producedSourceFiles,
                            taskHelper, plugin, methodNodes);
                    wrapper.getCipherInfo().setCipherClassName(cipherImpl.substring(cipherImpl.lastIndexOf(".") + 1));

                    augmentUserApplication(methodNodes, taskHelper, initializerNode, applicationSourceLines,
                            wrapper, initCallOwnerOwnerName);

                    checkForImplementationSpecificMethods(taskHelper, wrapper);
                    break;
                }

                case "cipherDecrypt": {

                    wrapperFactory = new WrapperFactory();
                    wrapper = initializeWrapper(streamInitializationNode, dependentFiles, dependencyPaths,
                            wrapperFactory, "java.io.InputStream");

                    previouslyAugmented = wrapperFactory.wrapWithCipher(wrapper, cipherImpl, producedSourceFiles,
                            taskHelper, plugin, methodNodes);
                    wrapper.getCipherInfo().setCipherClassName(cipherImpl.substring(cipherImpl.lastIndexOf(".") + 1));

                    augmentUserApplication(methodNodes, taskHelper, initializerNode, applicationSourceLines,
                            wrapper, initCallOwnerOwnerName);

                    checkForImplementationSpecificMethods(taskHelper, wrapper);
                }
            }

            taskHelper.getPw().println("Wrapper class created: Wrapper" + wrapper.getWrappedClass().getShortName() +
                    ". It will be placed in the specified plugin directory. Replace your stream implementation in " + initCallOwnerOwnerName
                    + ", method " + initCallOwnerName + ", at line number " + streamInitializationNode.getLineNumber() +
                    " with a reference to the produced wrapper class.\n");

            if (!previouslyAugmented) {
                String classFileLocation = wrapperFactory.produceWrapperClassFile(wrapper);
                File javaSource = decompileWrapperFoundation(taskHelper, wrapper, plugin,
                        classFileLocation);
                String source = FileUtils.readFileToString(javaSource);

                WrapperSourceFile wrapperSourceFile = new WrapperSourceFile();
                wrapperSourceFile.setSource(source);
                wrapperSourceFile.setFileSystemPath(javaSource.getAbsolutePath());
                wrapperSourceFile.setFileName(javaSource.getName());
                producedSourceFiles[2] = wrapperSourceFile;
                impact.setProducedSourceFiles(producedSourceFiles);
                impact.setAugmentedUserFile(augmentedUserSourceFile);
                impact.setInitializationNode(streamInitializationNode);
                taskHelper.getClient().addToModel(ObjectToTriples.convert(config, impact), taskHelper.getGraphName());

                wrapper.getAspectsAdapted().add(functionalAspect);
                return wrapper;
            } else {
                recordAspectAdaptation(taskHelper, functionalAspect, wrapper);
            }
        }
        return null;
    }

    public static AspectConfigureRequest generateConfigurationRequest(FunctionalAspectInstance aspectInstance, GradleTaskHelper taskHelper,
                                                                      ObjectToTriplesConfiguration config) throws Exception{

        // Find the dfu instance this aspect belongs to
        String getCipherImpl = retrieveChosenDfuImpl(aspectInstance, taskHelper, config);
        AssertableSolutionSet cipherImplSolutions = new AssertableSolutionSet();

        taskHelper.getClient().executeSelectQuery(getCipherImpl, cipherImplSolutions);

        // Pair each dfu with its uuid
        Map<DfuInstance, Pair<String, String>> dfuInstanceStringMap = getDfuInstanceStringMap(taskHelper,
                cipherImplSolutions);

        String functionalityUUID = cipherImplSolutions.getSolutions().get(0).get("dfuFunctionality");
        Functionality functionality = (Functionality) TriplesToPojo.convert(taskHelper.getGraphName(),
                functionalityUUID, taskHelper.getClient());

        AspectConfigureRequest configureRequest = new AspectConfigureRequest();
        List<DataType> typesOfParameters = new ArrayList<>();
        Class<? extends FunctionalAspect> abstractAspect = aspectInstance.getAbstractAspect();

        FunctionalAspect instantiateAspect = null;
        try {
            instantiateAspect = abstractAspect.newInstance();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        // each dfu has different configuration parameters... encode these in the request pojo
        Class[] aspectSpecificDependencies = instantiateAspect.getAspectSpecificResourceDependencies();
        if (aspectSpecificDependencies.length != 0) {
            List<FunctionalAspect> aspectDependencies = new ArrayList<>();
            for (Class clazz : aspectSpecificDependencies) {
                try {
                    Object obj = clazz.newInstance();
                    if (obj instanceof FunctionalAspect) {

                        FunctionalAspect functionalAspect = (FunctionalAspect) obj;
                        aspectDependencies.add(functionalAspect);
                        for (Input in : functionalAspect.getInputs()) {
                            Class<? extends DataType> abstractDataType = in.getType();
                            typesOfParameters.add(abstractDataType.newInstance());
                        }
                    }
                } catch(Exception exc) {
                    exc.printStackTrace();
                }
            }
        }

        DataType[] dataTypeArr = new DataType[typesOfParameters.size()];
        for (int i = 0; i < dataTypeArr.length; i++) {
            dataTypeArr[i] = typesOfParameters.get(i);
        }


        List<DfuInstance> dfuInstances = new ArrayList<>(dfuInstanceStringMap.keySet());
        DfuInstance[] dfuInstanceArr = new DfuInstance[dfuInstances.size()];
        for (int i = 0; i < dfuInstanceArr.length; i++) {
            DfuInstance dfuInstance = dfuInstances.get(i);
            dfuInstance.setFunctionalAspects(null);
            dfuInstanceArr[i] = dfuInstance;
        }

        configureRequest.setConfigurationUnknowns(dataTypeArr);
        configureRequest.setCandidateImpls(dfuInstanceArr);
        configureRequest.setRequiredFunctionality(functionality.getClass());

        String getMinimumConfiguration = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_functionality_aspects: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/aspects#>\n" +
                "\n" +
                "select ?configBindings where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t?minimumConfiguration a IMMoRTALS_functionality_aspects:AspectBestPracticeConfiguration\n" +
                "\t\t; IMMoRTALS:hasBoundFunctionality  <???FUNCTIONALITY???>\n" +
                "\t\t; IMMoRTALS:hasConfigurationBindings ?configBindings .\n" +
                "\n" +
                "\t}\n" +
                "}\n";
        getMinimumConfiguration = getMinimumConfiguration.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???FUNCTIONALITY???", functionalityUUID);
        AssertableSolutionSet configSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getMinimumConfiguration, configSolutions);

        ConfigurationBinding[] configurationBindings = null;
        if (!configSolutions.getSolutions().isEmpty()) {
            configurationBindings = new ConfigurationBinding[configSolutions.getSolutions().size()];
            int i = 0;
            for (Solution configSolution : configSolutions.getSolutions()) {
                String bindingUUID = configSolution.get("configBindings");
                Object bindingObj = TriplesToPojo.convert(taskHelper.getGraphName(), bindingUUID, taskHelper.getClient());
                
                if (bindingObj instanceof ConfigurationBinding) {
                    ConfigurationBinding configurationBinding = (ConfigurationBinding) bindingObj;
                    configurationBindings[i] = configurationBinding;
                }
                i++;
            }
            AspectConfigureSolution minimumConfigurationSolution = new AspectConfigureSolution();
            minimumConfigurationSolution.setConfigurationBindings(configurationBindings);

            configureRequest.setMinimumConfigurationSolution(minimumConfigurationSolution);
        }
        
        return configureRequest;
    }

    public static List<AspectConfigureSolution> retrieveConfigurationSolutions(GradleTaskHelper taskHelper) throws Exception{

        String getConfigurationSolution = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_functionality_aspects: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/aspects#>\n" +
                "\n" +
                "select ?configureSolution where {\n" +
                "\t\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t\t?configureSolution a IMMoRTALS_functionality_aspects:AspectConfigureSolution.\n" +
                "\t\n" +
                "\t}\n" +
                "}";
        getConfigurationSolution = getConfigurationSolution.replace("???GRAPH_NAME???", taskHelper.getGraphName());
        AssertableSolutionSet configureSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getConfigurationSolution, configureSolutions);

        List<AspectConfigureSolution> configureSolutionList = new ArrayList<>();
        for (Solution configureSolution : configureSolutions.getSolutions()) {
            String aspectSolutionUUID = configureSolution.get("configureSolution");
            configureSolutionList.add((AspectConfigureSolution) TriplesToPojo.convert(taskHelper.getGraphName(), aspectSolutionUUID, taskHelper.getClient()));
        }
        
        return configureSolutionList;
    }

    private static void recordAspectAdaptation(GradleTaskHelper taskHelper, FunctionalAspect functionalAspect, Wrapper wrapper) {
        for (Wrapper cachedWrapper : taskHelper.getWrappers()) {
            if (cachedWrapper.getWrapperClass().getName().equals(
                    wrapper.getWrapperClass().getName())) {
                boolean aspectAlreadyRecorded = false;
                for (FunctionalAspect wrapperAspect : cachedWrapper.getAspectsAdapted()) {
                    if (wrapperAspect.getAspectId().equals(functionalAspect.getAspectId())) {
                        aspectAlreadyRecorded = true;
                    }
                }
                if (!aspectAlreadyRecorded) {
                    cachedWrapper.getAspectsAdapted().add(functionalAspect);
                }
            }
        }
    }

    private static void checkForImplementationSpecificMethods(GradleTaskHelper taskHelper, Wrapper wrapper) {
        String wrappedClassName = wrapper.getWrappedClass().getName();
        String getNecessaryImplementations = "prefix IMMoRTALS_pattern_spec: <http://darpa.mil/immortals/ontology/r2.0.0/pattern/spec#> \n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?codeSpecSig ?codeSpecCode where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t\t?specComponent a IMMoRTALS_pattern_spec:SpecComponent\n" +
                "\t\t; IMMoRTALS:hasAspectBeingPerformed <http://darpa.mil/immortals/ontology/r2.0.0/functionality/wrapper#AspectRuntimeImplementation>\n" +
                "\t\t; IMMoRTALS:hasCodeSpec ?codeSpec .\n" +
                "\t\t\n" +
                "\t\t?codeSpec a IMMoRTALS_pattern_spec:CodeSpec\n" +
                "\t\t; IMMoRTALS:hasClassName \"???CLASS_NAME???\"\n" +
                "\t\t; IMMoRTALS:hasMethodSignature ?codeSpecSig\n" +
                "\t\t; IMMoRTALS:hasCode ?codeSpecCode .\n" +
                "\t}\n" +
                "}";
        getNecessaryImplementations = getNecessaryImplementations.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???CLASS_NAME???", wrappedClassName.replaceAll("\\.", "\\/"));
        AssertableSolutionSet necessaryImplementationSolutionSet = new AssertableSolutionSet();

        taskHelper.getClient().executeSelectQuery(getNecessaryImplementations, necessaryImplementationSolutionSet);
        List<CodeSpec> codeSpecs = new ArrayList<>();

        for (Solution necessaryImplementationSolution : necessaryImplementationSolutionSet.getSolutions()) {

            CodeSpec codeSpec = new CodeSpec();
            codeSpec.setClassName(wrappedClassName);
            codeSpec.setMethodSignature(necessaryImplementationSolution.get("codeSpecSig"));
            codeSpec.setCode(necessaryImplementationSolution.get("codeSpecCode"));
            codeSpecs.add(codeSpec);
        }

        wrapper.introduceMediationMethods(codeSpecs, taskHelper);
    }

    private static String getStreamObject(FunctionalAspect functionalAspect) {
        return functionalAspect.getAspectId().contains("Encrypt") ?
                "http://darpa.mil/immortals/ontology/r2.0.0/functionality/datatype#OutputStream" :
                "http://darpa.mil/immortals/ontology/r2.0.0/functionality/datatype#InputStream";
    }

    private static File decompileWrapperFoundation(GradleTaskHelper taskHelper, Wrapper wrapper, boolean plugin,
                                                   String classFileLocation) throws IOException {
        Decompiler decompiler = new Decompiler();
        File javaSource;
        if (plugin) {
            javaSource = decompiler.decompileClassFile(classFileLocation, taskHelper.getResultsDir()
                    + "/Wrapper" + wrapper.getWrappedClass().getShortName() + ".java", plugin);
        } else {
            javaSource = decompiler.decompileClassFile(classFileLocation,
                    classFileLocation.substring(0, classFileLocation.lastIndexOf("/"))
                            + "/Wrapper" + wrapper.getWrappedClass().getShortName() + ".java", plugin);
        }

        return javaSource;
    }

    public static void performWrapperCodeInsertions(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config) throws Exception {


        for (Wrapper wrapper : taskHelper.getWrappers()) {
            String wrappedClassFileName = wrapper.getWrapperClass().getShortName() + ".java";

            String getPreviouslyAugmentedSource = getWrapperSource(wrappedClassFileName, taskHelper);
            AssertableSolutionSet sourceSolutions = new AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getPreviouslyAugmentedSource, sourceSolutions);

            Solution sourceSolution = sourceSolutions.getSolutions().get(0);
            String sourceFileName = sourceSolution.get("sourceFile");
            String source = sourceSolution.get("sourceCode"); // retrieve source from ontology

            if (wrapper.getWrappedClass().isAbstract()) {
                Optional<String> sourceWithAbstractMethodsImplemented = taskHelper.checkForNecessaryImplementations(
                        wrapper.getWrappedClass().getName(), source, null);
                if (sourceWithAbstractMethodsImplemented.isPresent()) {
                    source = sourceWithAbstractMethodsImplemented.get();
                }
            }

            Optional<String> sourceWithAugmentationSurfaceMethods = taskHelper.augmentAdaptationSurfaceMethods(
                    wrapper.getWrappedClass().getName(), source, null, wrapper);
            if (sourceWithAugmentationSurfaceMethods.isPresent()) {
                source = sourceWithAugmentationSurfaceMethods.get();
            }

            WrapperSourceFile sourceFile = (WrapperSourceFile) TriplesToPojo.convert(taskHelper.getGraphName(),
                    sourceFileName, taskHelper.getClient());
            sourceFile.setSource(source);

            taskHelper.getClient().addToModel(ObjectToTriples.convert(config, sourceFile), taskHelper.getGraphName());

            Files.write(Paths.get(sourceFile.getFileSystemPath()), Collections.singleton(source), StandardCharsets.UTF_8);
        }
    }

    private static String getWrapperSource(String wrappedClassFileName, GradleTaskHelper taskHelper) {
        String getPreviouslyAugmentedSource = "prefix IMMoRTALS_constraint: <http://darpa.mil/immortals/ontology/r2.0.0/constraint#> \n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                "\n" +
                "select ?sourceFile ?sourceCode where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t\n" +
                "\t\t?impacts IMMoRTALS:hasProducedSourceFiles ?sourceFile .\n" +
                "\t\t\n" +
                "\t\t?sourceFile IMMoRTALS:hasFileName \"???FILE_NAME???\"\n" +
                "\t\t; IMMoRTALS:hasSource ?sourceCode .\n" +
                "\t}\n" +
                "}";
        getPreviouslyAugmentedSource = getPreviouslyAugmentedSource.replace("???FILE_NAME???",
                wrappedClassFileName)
                .replace("???GRAPH_NAME???", taskHelper.getGraphName());
        return getPreviouslyAugmentedSource;
    }

    public static String getImplementationSpecificStrings(GradleTaskHelper taskHelper, String chosenInstanceUUID) {
        String getUsageParadigmMagicString = "prefix IMMoRTALS_pattern_spec: <http://darpa.mil/immortals/ontology/r2.0.0/pattern/spec#>\n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?magicString ?configVars where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t?usageParadigm a IMMoRTALS_pattern_spec:AbstractUsageParadigm\n" +
                "\t\t; IMMoRTALS:hasDfuInstance <???DFU_INSTANCE???>\n" +
                "\t\t; IMMoRTALS:hasMagicInitString ?magicString\n" +
                "\t\t; IMMoRTALS:hasConfigurationVariables ?configVars .\n" +
                "\t}\n" +
                "}";
        getUsageParadigmMagicString = getUsageParadigmMagicString.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???DFU_INSTANCE???", chosenInstanceUUID);
        return getUsageParadigmMagicString;
    }

    private static String getFunctionalAspectUUID(GradleTaskHelper taskHelper, String aspectInstancePointer) {

        String getAspectUUID = "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> \n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?aspectUUID where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t?aspectUUID a IMMoRTALS_dfu_instance:FunctionalAspectInstance\n" +
                "\t\t; IMMoRTALS:hasMethodPointer \"???METHOD_POINTER???\" .\n" +
                "\t}\n" +
                "}";
        getAspectUUID = getAspectUUID.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???METHOD_POINTER???", aspectInstancePointer);
        AssertableSolutionSet aspectUUIDSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getAspectUUID, aspectUUIDSolutions);

        if (!aspectUUIDSolutions.getSolutions().isEmpty()) {
            String aspectUUID = aspectUUIDSolutions.getSolutions().get(0).get("aspectUUID");
            return aspectUUID;
        }
        return null;
    }

    public static String retrieveChosenDfuImpl(FunctionalAspectInstance aspectInstance, GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config) {
        String getCipherImpl = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                "select ?cipherImpl ?dfu ?dfuFunctionality where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t\n" +
                "\t\t?class IMMoRTALS:hasHash ?hash\n" +
                "\t\t; IMMoRTALS:hasClassModel ?classModel .\n" +
                "\n\t\t?classModel IMMoRTALS:hasClassName ?cipherImpl ." +
                "\t\t?dfu IMMoRTALS:hasClassPointer ?hash\n" +
                "\t\t; IMMoRTALS:hasFunctionalAspects <???ASPECT_INSTANCE???> \n" +
                "\t\t; IMMoRTALS:hasFunctionalityAbstraction ?dfuFunctionality .\n" +
                "\t}\n" +
                "}";
        getCipherImpl = getCipherImpl.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???ASPECT_INSTANCE???"
                , getFunctionalAspectUUID(taskHelper, aspectInstance.getMethodPointer()));
        return getCipherImpl;
    }
    
    public static String findCipherJar(FunctionalAspectInstance aspectInstance, GradleTaskHelper taskHelper) {

        String getCipherImplJar = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                "select ?jarPath where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t\n" +
                "\t\t?jar IMMoRTALS:hasFileSystemPath ?jarPath\n" +
                "\t\t; IMMoRTALS:hasJarContents ?class .\n" +
                "\t\t?class IMMoRTALS:hasHash ?hash\n" +
                "\t\t; IMMoRTALS:hasClassModel ?classModel .\n" +
                "\n\t\t?classModel IMMoRTALS:hasClassName ?cipherImpl ." +
                "\t\t?dfu IMMoRTALS:hasClassPointer ?hash\n" +
                "\t\t; IMMoRTALS:hasFunctionalAspects <???ASPECT_INSTANCE???> \n" +
                "\t\t; IMMoRTALS:hasFunctionalityAbstraction ?dfuFunctionality .\n" +
                "\t}\n" +
                "}";
        getCipherImplJar = getCipherImplJar.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???ASPECT_INSTANCE???"
                , getFunctionalAspectUUID(taskHelper, aspectInstance.getMethodPointer()));
        AssertableSolutionSet jarFileSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getCipherImplJar, jarFileSolutions);
        
        if (!jarFileSolutions.getSolutions().isEmpty()) {
            Solution jarFileSolution = jarFileSolutions.getSolutions().get(0);
            return jarFileSolution.get("jarPath");
        } else {
            return null;   
        }
    }

    private static Wrapper initializeWrapper(MethodInvocationDataflowNode streamInitializationNode, Set<String> dependentFiles,
                                             List<String> dependencyPaths, WrapperFactory wrapperFactory, String s) {
        Wrapper wrapper;
        wrapper = wrapperFactory.createWrapper(streamInitializationNode.getJavaClassName().replace("/", "."),
                dependencyPaths, dependentFiles);

        wrapper.setStreamType(s);
        return wrapper;
    }

    private static boolean analysisViaPlugin(GradleTaskHelper taskHelper) {
        boolean plugin;
        if (taskHelper.getResultsDir() == null) {
            plugin = false;
        } else {
            plugin = true;
        }
        return plugin;
    }

    private static Set<String> sanitizeDependencyPaths(AssertableSolutionSet dependentFileSolutions) throws UnsupportedEncodingException {
        Set<String> dependentFiles = new HashSet<>();
        for (Solution dependentFileSolution : dependentFileSolutions.getSolutions()) {
            String dependentFileFullPath = dependentFileSolution.get("dependentFiles");
            dependentFiles.add(URLDecoder.decode(dependentFileFullPath.replace("\\", "/").
                    substring(dependentFileFullPath.indexOf("jar:file:/") + 10, dependentFileFullPath.indexOf("!")), "UTF-8"));
        }
        return dependentFiles;
    }

    private static AssertableSolutionSet getWrapperInsertionSiteContext(GradleTaskHelper taskHelper, String initCallOwnerOwnerName) {
        String sourceCodeRepoSafeName = initCallOwnerOwnerName.replaceAll("/", ".");
        if (sourceCodeRepoSafeName.contains("$")) {
            sourceCodeRepoSafeName = sourceCodeRepoSafeName.substring(0, sourceCodeRepoSafeName.indexOf("$"));
        }

        String getInitCallOwnerOwnerSource = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select distinct ?fileName ?source where {\n" +
                "  \n" +
                "\t\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "    \n" +
                "\t\t?javaProject IMMoRTALS:hasSourceCodeRepo ?sourceCodeRepo\n" +
                "    \t; IMMoRTALS:hasCompiledSourceHash ?compiledHash .\n" +
                "\t\t\n" +
                "\t\t?sourceCodeRepo IMMoRTALS:hasSourceFiles ?sourceFiles .\n" +
                "\t\t\n" +
                "\t\t?sourceFiles IMMoRTALS:hasFullyQualifiedName \"???CLASS_NAME???\"\n" +
                "\t\t; IMMoRTALS:hasSource ?source \n" +
                "\t\t; IMMoRTALS:hasFileName ?fileName .\n" +
                "\t\t\n" +
                "\t\t?classArtifact IMMoRTALS:hasHash ?compiledHash .\n" +
                "\t\t\n" +
                "\t\t?classArtifact IMMoRTALS:hasClassModel ?aClass .\n" +
                "\t\t\n" +
                "\t\t?aClass IMMoRTALS:hasClassName \"???CLASS_NAME_URL???\" .}\n" +
                "}";
        getInitCallOwnerOwnerSource = getInitCallOwnerOwnerSource.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???CLASS_NAME???", sourceCodeRepoSafeName).replace(
                        "???CLASS_NAME_URL???", initCallOwnerOwnerName);
        AssertableSolutionSet ownerSourceSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getInitCallOwnerOwnerSource, ownerSourceSolutions);
        return ownerSourceSolutions;
    }

    private static ArrayList<CallTrace> findWrapperInsertionSite(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config, AssertableSolutionSet traceSolutions) throws Exception {
        String streamStarterNodeUUID = traceSolutions.getSolutions().get(0).get("nodes");
        String streamStarterEdgeUUID = traceSolutions.getSolutions().get(0).get("edge");

        MethodInvocationDataflowEdge streamStarterEdge = (MethodInvocationDataflowEdge) TriplesToPojo.convert(taskHelper.getGraphName(),
                streamStarterEdgeUUID, taskHelper.getClient());


        return getAllReverseCallTraces(taskHelper, streamStarterNodeUUID, new CallTrace(),
                streamStarterEdge.getDataflowAnalysisFrame(), config);
    }

    private static AssertableSolutionSet findStreamImplementationInUsersCode(InterMethodDataflowNode node, GradleTaskHelper taskHelper,
                                                                             String applicableStreamObject) {

        String headOfTraceUUID = getInterMethodNodeUUID(taskHelper, node);
        //TODO wednesday, can't use getNameForObject anymore so compare using discriminate characteristics like pointer
        AssertableSolutionSet traceSolutions = new AssertableSolutionSet();
        getStreamImplementation(taskHelper, applicableStreamObject, headOfTraceUUID, traceSolutions);
        return traceSolutions;
    }

    public static String getInterMethodNodeUUID(GradleTaskHelper taskHelper, InterMethodDataflowNode node) {

        String getNodeUUID = "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#>\n" +
                "prefix xsd:   <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT ?node WHERE {\n" +
                "\tGRAPH <http://localhost:3030/ds/data/???GRAPH_NAME???> { \n" +
                "\t\t?node a IMMoRTALS_analysis:InterMethodDataflowNode\n" +
                "\t\t; IMMoRTALS:hasJavaMethodPointer \"???METHOD_POINTER???\"\n" +
                "\t\t; IMMoRTALS:hasJavaMethodName \"???METHOD_NAME???\"\n" +
                "\t\t; IMMoRTALS:hasJavaClassName \"???CLASS_NAME???\"\n" +
                "\t\t; IMMoRTALS:hasLineNumber \"???LINE_NUMBER???\"^^xsd:int .\n" +
                "\t\t}\n" +
                "\t}\n" +
                "";
        getNodeUUID = getNodeUUID.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???METHOD_POINTER???",
                node.getJavaMethodPointer()).replace("???METHOD_NAME???", node.getJavaMethodName()).replace("???CLASS_NAME???",
                node.getJavaClassName()).replace("???LINE_NUMBER???", String.valueOf(node.getLineNumber()));
        AssertableSolutionSet nodeUUIDSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getNodeUUID, nodeUUIDSolutions);

        if (!nodeUUIDSolutions.getSolutions().isEmpty()) {
            return nodeUUIDSolutions.getSolutions().get(0).get("node");
        }
        return null;
    }

    private static void getStreamImplementation(GradleTaskHelper taskHelper, String applicableStreamObject, String headOfTraceUUID,
                                                AssertableSolutionSet traceSolutions) {
        final String getCalledNodes = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> \n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
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
    }

    private static AssertableSolutionSet getInitializationData(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config,
                                                               MethodInvocationDataflowNode streamInitializationNode) {
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
        return initCallInfoSolution;
    }

    public static String generateMagicString(GradleTaskHelper taskHelper, String magicString, AspectConfigureSolution configureSolution,
                                             AssertableSolutionSet usageParadigmSolutions) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, NoSuchFieldException {

        for (Solution usageParadigmSolution : usageParadigmSolutions.getSolutions()) {

            String configurationVarUUID = usageParadigmSolution.get("configVars");
            Object configurationVarObject = TriplesToPojo.convert(taskHelper.getGraphName(), configurationVarUUID,
                    taskHelper.getClient());
            if (configurationVarObject instanceof DfuConfigurationVariable) {
                DfuConfigurationVariable dfuConfigurationVariable = (DfuConfigurationVariable) configurationVarObject;
                Optional<ConfigurationBinding> configurationBindingOption = Arrays.stream(configureSolution.getConfigurationBindings())
                        .filter(binding -> binding.getSemanticType().equals(dfuConfigurationVariable.getSemanticType())).findFirst();

                if (configurationBindingOption.isPresent()) {
                    ConfigurationBinding configurationBinding = configurationBindingOption.get();
                    magicString = magicString.replace(dfuConfigurationVariable.getMagicStringVar(), configurationBinding.getBinding());
                }
            }
        }

        return magicString;
    }

    public static Map<DfuInstance, Pair<String, String>> getDfuInstanceStringMap(GradleTaskHelper taskHelper,
                                                                                 AssertableSolutionSet cipherImplSolutions) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Map<DfuInstance, Pair<String, String>> dfuInstanceStringMap = new HashMap<>();
        for (Solution cipherImplSolution : cipherImplSolutions.getSolutions()) {

            String cipherImplName = cipherImplSolution.get("cipherImpl");
            String dfuInstanceUUID = cipherImplSolution.get("dfu");
            Object dfuInstanceObject = TriplesToPojo.convert(taskHelper.getGraphName(), dfuInstanceUUID, taskHelper.getClient());

            if (dfuInstanceObject instanceof DfuInstance) {
                Pair<String, String> dfuUUIDAndCipherImpl = new Pair<>(dfuInstanceUUID, cipherImplName);
                dfuInstanceStringMap.put((DfuInstance) dfuInstanceObject, dfuUUIDAndCipherImpl);
            }
        }
        return dfuInstanceStringMap;
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

    private static void augmentUserApplication(List<MethodInvocationDataflowNode> affectedNodes, GradleTaskHelper taskHelper,
                                               MethodInvocationDataflowNode initializerNode, List<String> applicationLines,
                                               Wrapper wrapper, String initCallOwnerOwnerName) throws IOException {

        String streamIdentifier = augmentInitializationNode(initializerNode, applicationLines,
                wrapper.getWrapperClass().getShortName());

        if (streamIdentifier == null) {
            // no assignment performed, so no stream identifier
            return;
        }

        if (!affectedNodes.isEmpty()) {
            for (MethodInvocationDataflowNode nodeToAugment : affectedNodes) {
                augmentInaccessibleInheritedMethods(nodeToAugment, applicationLines, streamIdentifier);
            }
        }

        StringBuilder newSourceBuilder = new StringBuilder();
        for (String newSourceLine : applicationLines) {
            newSourceLine+="\\n";
            newSourceBuilder.append(newSourceLine);
        }

        expandAdaptationSurface(taskHelper, Optional.of(initCallOwnerOwnerName), Optional.empty()
                ,newSourceBuilder.toString(), true);
    }

    public static void expandAdaptationSurface(GradleTaskHelper taskHelper, Optional<String> ownerNameOption,
                                               Optional<String> fileNameOption, String newSource, boolean userCode) throws IOException {

        if (userCode) {

            String initCallOwnerOwnerName = null;
            if (ownerNameOption.isPresent()) {
                initCallOwnerOwnerName = ownerNameOption.get();
            }

            String sourceCodeRepoSafeName = initCallOwnerOwnerName.replaceAll("/", ".");
            if (sourceCodeRepoSafeName.contains("$")) {
                sourceCodeRepoSafeName = sourceCodeRepoSafeName.substring(0, sourceCodeRepoSafeName.indexOf("$"));
            }

            String replaceCurrentApplicationSource = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "\n" +
                    "WITH <http://localhost:3030/ds/data/???GRAPH_NAME???>\n" +
                    "DELETE {?sourceFiles IMMoRTALS:hasSource ?source}\n" +
                    "INSERT {?sourceFiles IMMoRTALS:hasSource \"???NEW_SOURCE???\"}\n" +
                    "WHERE { ?javaProject IMMoRTALS:hasClasspaths ?classpaths\n" +
                    "\t\t; IMMoRTALS:hasSourceCodeRepo ?sourceCodeRepo\n" +
                    "    \t; IMMoRTALS:hasCompiledSourceHash ?compiledHash .\n" +
                    "\t\t\n" +
                    "\t\t?sourceCodeRepo IMMoRTALS:hasSourceFiles ?sourceFiles .\n" +
                    "\t\t\n" +
                    "\t\t?sourceFiles IMMoRTALS:hasFullyQualifiedName \"???CLASS_NAME???\"\n" +
                    "\t\t; IMMoRTALS:hasSource ?source .\n" +
                    "\t\t\n" +
                    "\t\t?classArtifact IMMoRTALS:hasHash ?compiledHash .\n" +
                    "\t\t\n" +
                    "\t\t?classArtifact IMMoRTALS:hasClassModel ?aClass .\n" +
                    "\t\t\n" +
                    "\t\t?aClass IMMoRTALS:hasClassName \"???CLASS_NAME_URL???\" .}\n" +
                    "\t\t";
            replaceCurrentApplicationSource = replaceCurrentApplicationSource.replace("???GRAPH_NAME???",
                    taskHelper.getGraphName()).replace("???CLASS_NAME???", sourceCodeRepoSafeName)
                    .replace("???CLASS_NAME_URL???", initCallOwnerOwnerName)
                    .replace("???NEW_SOURCE???", newSource.replaceAll("\"", "\\\\\""));
            taskHelper.getClient().executeUpdate(replaceCurrentApplicationSource);

            String getActualSourceFile = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "select ?path where {" +
                    "\t\t graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {" +
                    " ?javaProject IMMoRTALS:hasClasspaths ?classpaths\n" +
                    "\t\t; IMMoRTALS:hasSourceCodeRepo ?sourceCodeRepo\n" +
                    "    \t; IMMoRTALS:hasCompiledSourceHash ?compiledHash .\n" +
                    "\t\t\n" +
                    "\t\t?sourceCodeRepo IMMoRTALS:hasSourceFiles ?sourceFiles .\n" +
                    "\t\t\n" +
                    "\t\t?sourceFiles IMMoRTALS:hasFullyQualifiedName \"???CLASS_NAME???\"\n" +
                    "\t\t; IMMoRTALS:hasFileSystemPath ?path .\n" +
                    "\t\t\n" +
                    "\t\t?classArtifact IMMoRTALS:hasHash ?compiledHash .\n" +
                    "\t\t\n" +
                    "\t\t?classArtifact IMMoRTALS:hasClassModel ?aClass .\n" +
                    "\t\t\n" +
                    "\t\t?aClass IMMoRTALS:hasClassName \"???CLASS_NAME_URL???\" ." +
                    "\t}" +
                    "}\n";
            getActualSourceFile = getActualSourceFile.replace("???GRAPH_NAME???",
                    taskHelper.getGraphName()).replace("???CLASS_NAME???", initCallOwnerOwnerName.replaceAll("/",
                    ".")).replace("???CLASS_NAME_URL???", initCallOwnerOwnerName).replace("???NEW_SOURCE???",
                    newSource);
            AssertableSolutionSet sourceFileSolutions = new AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getActualSourceFile, sourceFileSolutions);

            if (!sourceFileSolutions.getSolutions().isEmpty()) {

                String sourceFilePath = sourceFileSolutions.getSolutions().get(0).get("path");
                File sourceFile = new File(sourceFilePath);

                if (sourceFile.exists()) {

                    System.out.println("User application file found on local file system, do you want" +
                            " constraint analysis to automatically augment it in order to resolve remediation" +
                            " strategy?  y/n");
                    Scanner sc = new Scanner(System.in);
                    String userInput = sc.next();

                    if (userInput.equalsIgnoreCase("yes") || userInput.equalsIgnoreCase("y")) {
                        Files.write(sourceFile.toPath(), Collections.singleton(newSource), Charset.defaultCharset());
                    } else {
                        System.out.println("Immortals system won't change application source, user can retrieve augmented code from the" +
                                "source code repo preserved in ontology.");
                    }
                }
            }
        } else {

            String fileName = null;
            if (fileNameOption.isPresent()) {
                fileName = fileNameOption.get();
            }

            String replaceWrapperSource = "prefix IMMoRTALS_lang: <http://darpa.mil/immortals/ontology/r2.0.0/lang#> \n" +
                    "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                    "\n" +
                    "WITH <http://localhost:3030/ds/data/???GRAPH_NAME???>\n" +
                    "DELETE {?wrapperSourceFile IMMoRTALS:hasSource ?source}\n" +
                    "INSERT {?wrapperSourceFile IMMoRTALS:hasSource \"???NEW_SOURCE???\"}\n" +
                    "WHERE  {?wrapperSourceFile a IMMoRTALS_lang:WrapperSourceFile\n" +
                    "\t\t; IMMoRTALS:hasFileName \"???FILE_NAME???\"\n" +
                    "\t\t; IMMoRTALS:hasSource ?source .}";
            replaceWrapperSource = replaceWrapperSource.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace(
                    "???NEW_SOURCE???", newSource.replace("\n", "\\n")
                            .replaceAll("\"", "\\\\\"")).replace("???FILE_NAME???", fileName);
            taskHelper.getClient().executeUpdate(replaceWrapperSource);
        }
    }

    private static String augmentInitializationNode(MethodInvocationDataflowNode methodNode, List<String> applicationSourceLines,
                                                    String wrapperClassName) {

        String identifier = null;
        int initializationLineNumber = methodNode.getLineNumber() - 1;
        String initializationLine = applicationSourceLines.get(initializationLineNumber);

        String[] sidesOfExpression = initializationLine.split("=");

        if (sidesOfExpression.length == 1) {

            Pattern p = Pattern.compile("(.[a-zA-Z0-9_.-]+\\." + methodNode.getJavaMethodName() + ")");
            Matcher m = p.matcher(initializationLine);

            if (m.find()) {
                int indexOfInit = initializationLine.indexOf(m.group());
                String restOfLine = initializationLine.substring(indexOfInit);

                int resolvedInitMethod = 1;
                int currentIndex = 0;
                for (char c : restOfLine.toCharArray()) {
                    if (c == '(') {
                        resolvedInitMethod++;
                    } else if (c == ')') {
                        resolvedInitMethod--;
                    }
                    if (resolvedInitMethod == 1) {
                        break;
                    }
                    currentIndex++;
                }

                StringBuilder sb = new StringBuilder(initializationLine);
                sb.insert(currentIndex + indexOfInit,")");
                sb.insert(indexOfInit + 1, " new " + wrapperClassName + "(");
                applicationSourceLines.set(initializationLineNumber, sb.toString());
            }
        } else {

            String[] leftSideOfExpression = sidesOfExpression[0].trim().split(" ");
            if (leftSideOfExpression.length > 1) {
                // new var assignment
                if (initializationLine.contains(" final ")) {
                    // streaming abstraction is declared final
                    identifier = leftSideOfExpression[2];
                } else {
                    identifier = leftSideOfExpression[1];
                }
            } else {
                // existing field assignment
                identifier = leftSideOfExpression[0];
            }

            if (initializationLine.contains(wrapperClassName)) {
                return identifier;
            }

            StringBuilder sb = new StringBuilder(initializationLine);
            sb.insert(sb.indexOf("=") + 1, " new " + wrapperClassName + "(");
            sb.insert(sb.lastIndexOf(";"), ")");

            applicationSourceLines.set(initializationLineNumber, sb.toString());
        }

        return identifier;
    }

    private static AugmentedMethodInvocation[] recordAugmentedUserCode(MethodInvocationDataflowNode initializerNode,
                                                                       List<MethodInvocationDataflowNode> methodNodes) {

        AugmentedMethodInvocation augmentedInitializer = new AugmentedMethodInvocation();
        augmentedInitializer.setLineNumber(initializerNode.getLineNumber());
        augmentedInitializer.setMethodName(initializerNode.getJavaMethodName());

        AugmentedMethodInvocation[] augmentedUserNodes = new AugmentedMethodInvocation[methodNodes.size() + 1];

        for (int i = 0; i < methodNodes.size(); i++) {

            MethodInvocationDataflowNode methodInvocationDataflowNode = methodNodes.get(i);

            AugmentedMethodInvocation augmentedMethodInvocation = new AugmentedMethodInvocation();
            augmentedMethodInvocation.setLineNumber(methodInvocationDataflowNode.getLineNumber());
            augmentedMethodInvocation.setMethodName(methodInvocationDataflowNode.getJavaMethodName());

            augmentedUserNodes[i] = augmentedMethodInvocation;
        }

        if (methodNodes.size() != 0) {
            augmentedUserNodes[methodNodes.size() - 1] = augmentedInitializer;
        } else {
            augmentedUserNodes[0] = augmentedInitializer;
        }

        return augmentedUserNodes;
    }

    private static void augmentInaccessibleInheritedMethods(MethodInvocationDataflowNode methodNode, List<String> applicationSourceLines,
                                                            String streamIdentifier) {
        int methodLineNumber = methodNode.getLineNumber() - 1;
        String methodLine = applicationSourceLines.get(methodLineNumber);

        if (methodLine.contains(methodNode.getJavaMethodName() + "Wrapped")) {
            return;
        }

        StringBuilder sb = new StringBuilder(methodLine);
        int indexToAugment = sb.indexOf(streamIdentifier + "." + methodNode.getJavaMethodName());
        sb.insert(indexToAugment + streamIdentifier.length() + 1 + methodNode.getJavaMethodName().length(), "Wrapped");

        applicationSourceLines.set(methodLineNumber, sb.toString());

    }

    public static AspectConfigureSolution checkForUnknownConfigurations(FunctionalAspectInstance aspectInstance,
                                                                        Map<DfuInstance, Pair<String, String>> candidateImpls,
                                                                        Functionality functionality) {

        AspectConfigureRequest configureRequest = new AspectConfigureRequest();
        List<DataType> typesOfParameters = new ArrayList<>();
        Class<? extends FunctionalAspect> abstractAspect = aspectInstance.getAbstractAspect();

        FunctionalAspect instantiateAspect = null;
        try {
            instantiateAspect = abstractAspect.newInstance();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        Class[] aspectSpecificDependencies = instantiateAspect.getAspectSpecificResourceDependencies();
        if (aspectSpecificDependencies.length != 0) {
            List<FunctionalAspect> aspectDependencies = new ArrayList<>();
            for (Class clazz : aspectSpecificDependencies) {
                try {
                    Object obj = clazz.newInstance();
                    if (obj instanceof FunctionalAspect) {

                        FunctionalAspect functionalAspect = (FunctionalAspect) obj;
                        aspectDependencies.add(functionalAspect);
                        for (Input in : functionalAspect.getInputs()) {
                            Class<? extends DataType> abstractDataType = in.getType();
                            typesOfParameters.add(abstractDataType.newInstance());
                        }
                    }
                } catch(Exception exc) {
                    exc.printStackTrace();
                }
            }
        } else {
            return null;
        }

        DataType[] dataTypeArr = new DataType[typesOfParameters.size()];
        for (int i = 0; i < dataTypeArr.length; i++) {
            dataTypeArr[i] = typesOfParameters.get(i);
        }


        List<DfuInstance> dfuInstances = new ArrayList<>(candidateImpls.keySet());
        DfuInstance[] dfuInstanceArr = new DfuInstance[dfuInstances.size()];
        for (int i = 0; i < dfuInstanceArr.length; i++) {
            dfuInstanceArr[i] = dfuInstances.get(i);
        }

        configureRequest.setConfigurationUnknowns(dataTypeArr);
        configureRequest.setCandidateImpls(dfuInstanceArr);
        configureRequest.setRequiredFunctionality(functionality.getClass());

        //TODO doesn't do anything yet, for right now we will just fabricate a solution
        getConfigureSolution(configureRequest);

        ConfigurationBinding[] tempConfigurationBindings = new ConfigurationBinding[5];

        ConfigurationBinding configurationBinding0 = new ConfigurationBinding();
        configurationBinding0.setSemanticType(CipherAlgorithm.class);
        configurationBinding0.setBinding("AES");
        ConfigurationBinding configurationBinding1 = new ConfigurationBinding();
        configurationBinding1.setSemanticType(CipherKeyLength.class);
        configurationBinding1.setBinding("16");
        ConfigurationBinding configurationBinding2 = new ConfigurationBinding();
        configurationBinding2.setSemanticType(CipherBlockSize.class);
        configurationBinding2.setBinding("256");
        ConfigurationBinding configurationBinding3 = new ConfigurationBinding();
        configurationBinding3.setSemanticType(CipherChainingMode.class);
        configurationBinding3.setBinding("CBC");
        ConfigurationBinding configurationBinding4 = new ConfigurationBinding();
        configurationBinding4.setSemanticType(PaddingScheme.class);
        configurationBinding4.setBinding("PKCS5Padding");
        tempConfigurationBindings[0] = configurationBinding0;
        tempConfigurationBindings[1] = configurationBinding1;
        tempConfigurationBindings[2] = configurationBinding2;
        tempConfigurationBindings[3] = configurationBinding3;
        tempConfigurationBindings[4] = configurationBinding4;

        AspectConfigureSolution tempConfigureSolution = new AspectConfigureSolution();
        tempConfigureSolution.setChosenInstance(dfuInstanceArr[0]);
        tempConfigureSolution.setConfigurationBindings(tempConfigurationBindings);

        return tempConfigureSolution;
    }

    private static AspectConfigureSolution getConfigureSolution(AspectConfigureRequest aspectConfigureRequest) {
        return null;
    }

    public static AnalysisImpact repairConsumer(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config,
                                                InterMethodDataflowNode consumer, Set<File> dependencies,
                                                DesignPattern flowDesign, FunctionalAspectInstance aspectInstance,
                                                AspectConfigureSolution solution) throws Exception {

        WrapperImplementationImpact analysisImpact = new WrapperImplementationImpact();
        taskHelper.getPw().println("Finding instances that implement the required functional aspect...");

        Map<DfuInstance, Pair<String, String>> dfuInstanceStringMap = getDfuInstancePairMap(taskHelper, config, aspectInstance);

        String cipherImpl = null;
        String magicString = null;
        if (solution != null) {

            DfuInstance chosenInstance = solution.getChosenInstance();
            Pair<String, String> dfuUUIDToCipherImpl = null;
            for (DfuInstance dfuInstance : dfuInstanceStringMap.keySet()) {
                if (chosenInstance.getClassPointer().equals(dfuInstance.getClassPointer())) {
                    dfuUUIDToCipherImpl = dfuInstanceStringMap.get(dfuInstance);
                }
            }

            String getUsageParadigmMagicString = getImplementationSpecificStrings(taskHelper,
                    dfuUUIDToCipherImpl.getLeft());
            AssertableSolutionSet usageParadigmSolutions = new AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getUsageParadigmMagicString, usageParadigmSolutions);

            magicString = usageParadigmSolutions.getSolutions().get(0).get("magicString");
            magicString = generateMagicString(taskHelper, magicString, solution, usageParadigmSolutions);
            cipherImpl = dfuUUIDToCipherImpl.getRight();
        }

        taskHelper.getPw().println("Instance found. Determining its design pattern...");
        switch (flowDesign) {
            case FUNCTIONAL:
                taskHelper.getPw().println("Instance with method pointer: " + aspectInstance.getMethodPointer() + " utilizes a block design " +
                        "pattern. Repairs will occur immediately adjacent to the inter-process boundary...");
                break;
            case STREAM:
                taskHelper.getPw().println("Instance with method pointer: " + aspectInstance.getMethodPointer() + " utilizes a stream design pattern." +
                        " Repairs will vary based on the code system's stream implementation. Immortals will begin " +
                        "the process of wrapping the stream implementation in a custom class...");

                Wrapper wrapper = constructWrapperFoundation(aspectInstance, consumer, dependencies, taskHelper,
                        config, analysisImpact, cipherImpl.replace("/", "."));

                if (wrapper!= null) {

                    appendNewDependencies(taskHelper, aspectInstance, analysisImpact);
                    
                    if (magicString != null) {
                        // user specified configuration parameters, pass to code insertion stage
                        wrapper.getCipherInfo().setConfigurationParameters(Optional.of(magicString));
                    } else {
                        wrapper.getCipherInfo().setConfigurationParameters(Optional.empty());
                    }
                    taskHelper.getWrappers().add(wrapper);
                }
                break;
            default:
                break;
        }

        return analysisImpact;
    }


    public static AnalysisImpact repairProducer(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config,
                                                InterMethodDataflowNode producer, Set<File> dependencies,
                                                DesignPattern flowDesign, AspectConfigureSolution solution,
                                                FunctionalAspectInstance aspectInstance) throws Exception {

        WrapperImplementationImpact analysisImpact = new WrapperImplementationImpact();
        //TODO should be only one
        taskHelper.getPw().println("Instances found. Determining their design pattern...");

        Map<DfuInstance, Pair<String, String>> dfuInstanceStringMap = getDfuInstancePairMap(taskHelper, config, aspectInstance);

        String cipherImpl = null;
        String magicString = null;
        if (solution != null) {

            DfuInstance chosenInstance = solution.getChosenInstance();
            Pair<String, String> dfuUUIDToCipherImpl = null;
            for (DfuInstance dfuInstance : dfuInstanceStringMap.keySet()) {
                if (chosenInstance.getClassPointer().equals(dfuInstance.getClassPointer())) {
                    dfuUUIDToCipherImpl = dfuInstanceStringMap.get(dfuInstance);
                }
            }

            String getUsageParadigmMagicString = getImplementationSpecificStrings(taskHelper,
                    dfuUUIDToCipherImpl.getLeft());
            AssertableSolutionSet usageParadigmSolutions = new AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getUsageParadigmMagicString, usageParadigmSolutions);

            magicString = usageParadigmSolutions.getSolutions().get(0).get("magicString");
            magicString = generateMagicString(taskHelper, magicString, solution, usageParadigmSolutions);
            cipherImpl = dfuUUIDToCipherImpl.getRight();
        }

        switch (flowDesign) {
            case FUNCTIONAL:
                taskHelper.getPw().println("Instance with method pointer: " + aspectInstance.getMethodPointer() + " utilizes a block design " +
                        "pattern.");
                taskHelper.constructAspectImpact(aspectInstance, producer,
                        AspectAugmentationSpecification.AUGMENT_ONE, producer.getLineNumber() - 1, config);
                break;
            case STREAM:
                taskHelper.getPw().println("Instance with method pointer: " + aspectInstance.getMethodPointer() + " utilizes a stream design pattern." +
                        " Immortals will begin the process of wrapping the stream implementation in a custom class...");
                Wrapper wrapper = constructWrapperFoundation(aspectInstance, producer, dependencies, taskHelper,
                        config, analysisImpact, cipherImpl.replace("/", "."));
                
                if (wrapper!= null) {

                    appendNewDependencies(taskHelper, aspectInstance, analysisImpact);
                    
                    if (magicString != null) {
                        // user specified configuration parameters, pass to code insertion stage
                        wrapper.getCipherInfo().setConfigurationParameters(Optional.of(magicString));
                    } else {
                        wrapper.getCipherInfo().setConfigurationParameters(Optional.empty());
                    }
                    taskHelper.getWrappers().add(wrapper);
                }
                break;
            default:
                break;
        }
        return analysisImpact;
    }

    private static void appendNewDependencies(GradleTaskHelper taskHelper, FunctionalAspectInstance aspectInstance, WrapperImplementationImpact analysisImpact) {
        
        String[] newDependencies;
        if (analysisImpact.getAdditionalDependencies() == null) {
            newDependencies = new String[1];
        } else {
            newDependencies = Arrays.copyOf(analysisImpact.getAdditionalDependencies(), analysisImpact.getAdditionalDependencies().length + 1);
        }
        
        String cipherResourceLocation = findCipherJar(aspectInstance, taskHelper);
        if (cipherResourceLocation != null) {
            // cipher belongs to jar, user application may need to include it
            newDependencies[newDependencies.length - 1] = cipherResourceLocation;
        }
        
        analysisImpact.setAdditionalDependencies(newDependencies);
    }

    public static Map<DfuInstance, Pair<String, String>> getDfuInstancePairMap(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config,
                                                                               FunctionalAspectInstance aspectInstance) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        String getCipherImpl = retrieveChosenDfuImpl(aspectInstance, taskHelper, config);
        AssertableSolutionSet cipherImplSolutions = new AssertableSolutionSet();

        taskHelper.getClient().executeSelectQuery(getCipherImpl, cipherImplSolutions);

        return getDfuInstanceStringMap(taskHelper,
                cipherImplSolutions);
    }

    private static String getChosenInstanceUUID(DfuInstance chosenInstance, GradleTaskHelper taskHelper) {

        String getInstanceUUID = "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> \n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?uuid where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t?uuid a IMMoRTALS_dfu_instance:DfuInstance\n" +
                "\t\t; IMMoRTALS:hasClassPointer \"???CLASS_POINTER???\" .\n" +
                "\t}\n" +
                "}";
        getInstanceUUID = getInstanceUUID.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???CLASS_POINTER???", chosenInstance.getClassPointer());
        AssertableSolutionSet assertableSolutionSet = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getInstanceUUID, assertableSolutionSet);

        if (!assertableSolutionSet.getSolutions().isEmpty()) {
            return assertableSolutionSet.getSolutions().get(0).get("uuid");
        } else {
            return null;
        }
    }
}
