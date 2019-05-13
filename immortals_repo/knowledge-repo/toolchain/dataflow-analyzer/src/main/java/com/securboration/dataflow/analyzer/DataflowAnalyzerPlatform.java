package com.securboration.dataflow.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.resources.Client;
import com.securboration.immortals.ontology.resources.xml.XmlDocument;
import com.securboration.immortals.ontology.resources.xml.XmlInstance;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.query.ResultSetAggregator;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JimpleLocalBox;
import soot.tagkit.AnnotationTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;
import soot.util.Chain;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataflowAnalyzerPlatform {

    private FusekiClient fusekiClient;
    private String graphName;
    private DataflowAnalysisListener listener = new DataflowAnalysisListener();

    public DataflowAnalyzerPlatform(FusekiClient _fusekiClient, String _graphName) {
        fusekiClient = _fusekiClient;
        graphName = _graphName;
    }

    private final Collection<String> outgoingFlowIndicators = Arrays.asList("java/io/OutputStream write ([B)V");
    private final Collection<String> incomingFlowIndicators = Arrays.asList("java/io/InputStream read ([B)I");

    private Set<Pair<String, DataType>> classNamesToNewData = new HashSet<>();

    private Collection<Pair<String, Class<? extends DataType>>> classNameToSemantics = Arrays.asList(
            new Pair<>("Lmil/darpa/immortals/annotation/dsl/ontology/resources/xml/XmlInstance;", XmlInstance.class),
            new Pair<>("Lmil/darpa/immortals/annotation/dsl/ontology/functionality/datatype/BinaryData;", BinaryData.class));

    public String serializeDataflows(Set<Stack<DataflowGraphComponent>> dataflowGraphs) throws IOException {

        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");
        Model model = ModelFactory.createDefaultModel();

        for (Stack<DataflowGraphComponent> dataflowGraph : dataflowGraphs) {
            while (!dataflowGraph.isEmpty()) {
                model.add(ObjectToTriples.convert(config, dataflowGraph.pop()));
            }
        }

        String output = OntologyHelper.serializeModel(model, "TTL", false);
        File outputFile = new File("df.ttl");
        FileUtils.writeStringToFile(outputFile, output, Charset.defaultCharset());

        return outputFile.getAbsolutePath();
    }

    public Set<Stack<DataflowGraphComponent>> processCallTraceStacks(List<Stack<String>> callTraceStacks, Class<? extends Resource> dataflowResource) throws IOException, ClassNotFoundException {

        Set<Stack<DataflowGraphComponent>> dataflowStacks = new HashSet<>();
        Class<? extends DataType> dataBeingObserved = null;
        String flowUUID = dataflowResource.getName();

        for (Stack<String> callTraceStack : callTraceStacks) {
            String terminalNode = callTraceStack.pop();
            if (this.getOutgoingFlowIndicators().stream().anyMatch(outFlow -> outFlow.equals(terminalNode))) {
                //outgoing stream
                String[] callTraceComponents = terminalNode.split(" ");
                String callClassOwner = callTraceComponents[0];
                String callMethodDesc = convertCallTraceSigToSootSig(callTraceComponents[1], callTraceComponents[2]);
                SootClass terminalClass = Scene.v().loadClassAndSupport(callClassOwner.replace("/", "."));
                SootMethod terminalMethod = terminalClass.getMethod(callMethodDesc);

                InterMethodDataflowEdge interProcessEdge = new InterMethodDataflowEdge();

                InterMethodDataflowNode dataflowNode = new InterMethodDataflowNode();
                dataflowNode.setJavaClassName(terminalClass.getName());
                dataflowNode.setJavaMethodName(terminalMethod.getName());
                dataflowNode.setAbstractResourceTemplate(dataflowResource);

                interProcessEdge.setProducer(dataflowNode);

                MethodInvocationDataflowEdge dataflowEdge = new MethodInvocationDataflowEdge();
                dataflowEdge.setConsumer(dataflowNode);

                Stack<DataflowGraphComponent> dataflows = new Stack<>();
                dataflows.push(interProcessEdge);
                dataflows.push(dataflowNode);
                dataflows.push(dataflowEdge);

                Stack<DataflowGraphComponent> dataflowGraph = null;

                while (!callTraceStack.isEmpty()) {

                    if (dataflowGraph != null) {
                        dataflowStacks.add(dataflowGraph);
                        //flow formed... done for now. In future we will likely want to continue and find other possible flows, of which there are likely many
                        break;
                    }

                    String callTraceMethod = callTraceStack.pop();

                    callTraceComponents = callTraceMethod.split(" ");
                    callClassOwner = callTraceComponents[0];
                    callMethodDesc = convertCallTraceSigToSootSig(callTraceComponents[1], callTraceComponents[2]);

                    SootClass sClass = Scene.v().loadClassAndSupport(callClassOwner.replace("/", "."));
                    SootMethod sootMethod = sClass.getMethod(callMethodDesc);

                    Body b = sootMethod.retrieveActiveBody();

                    UnitGraph graph = new ExceptionalUnitGraph(b);
                    Iterator gIt = graph.iterator();
                    int i = 0;
                    while (gIt.hasNext()) {
                        Unit u = (Unit)gIt.next();
                        if (u instanceof AssignStmt) {

                        } else if (u instanceof InvokeStmt) {
                            InvokeStmt invokeStmt = (InvokeStmt) u;
                            SootMethod methodOfInterest = invokeStmt.getInvokeExpr().getMethod();

                            if (methodOfInterest.equals(terminalMethod)) {
                                //found method, follow var

                                if (invokeStmt.getInvokeExpr() instanceof VirtualInvokeExpr) {
                                    VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) invokeStmt.getInvokeExpr();
                                    Local localOfInterest = null;

                                    for (Value value : virtualInvokeExpr.getArgs()) {
                                        if (value.getType().equals(Scene.v().getType("byte[]"))) {

                                            dataBeingObserved = BinaryData.class;

                                            Iterator<DataflowGraphComponent> graphIter = dataflows.iterator();
                                            while (graphIter.hasNext()) {
                                                DataflowGraphComponent dataflowGraphComponent = graphIter.next();
                                                if (dataflowGraphComponent instanceof DataflowEdge) {
                                                    DataflowEdge setEdgeData = (DataflowEdge) dataflowGraphComponent;
                                                    setEdgeData.setDataTypeCommunicated(dataBeingObserved);
                                                }
                                            }

                                            localOfInterest = (Local) value;
                                            break;
                                        }
                                    }

                                    int lineNumber = getLineNumber(u);
                                    dataflowNode.setLineNumber(lineNumber);

                                    Map<DataflowAnalysisListener.SemanticProperties, Object> semanticDataProperties = new HashMap<>();
                                    semanticDataProperties.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());

                                    DataflowAnalysisListener.ListenerDataflowNode listenerNode = new DataflowAnalysisListener.ListenerDataflowNode(
                                            dataflowNode, sClass, sootMethod, invokeStmt, new JimpleLocalBox(localOfInterest));

                                    listener.beginTrace(flowUUID += ("-" + UUID.randomUUID()), semanticDataProperties, listenerNode);
                                    dataflowGraph = reverseFlowAnalysis(graph, localOfInterest,
                                            callTraceStack, u, dataflows, dataflowResource, dataBeingObserved);
                                    listener.endTrace();
                                    break;

                                } else if (invokeStmt instanceof StaticInvokeExpr) {

                                } else if (invokeStmt instanceof SpecialInvokeExpr) {

                                }
                            }
                        }
                        i++;
                    }
                }
            } else if (this.getIncomingFlowIndicators().stream().anyMatch(inFlow -> inFlow.equals(terminalNode))) {
                //incoming stream

                String[] callTraceComponents = terminalNode.split(" ");
                String callClassOwner = callTraceComponents[0];
                String callMethodDesc = convertCallTraceSigToSootSig(callTraceComponents[1], callTraceComponents[2]);
                SootClass terminalClass = Scene.v().loadClassAndSupport(callClassOwner.replace("/", "."));
                SootMethod terminalMethod = terminalClass.getMethod(callMethodDesc);

                InterMethodDataflowEdge interProcessEdge = new InterMethodDataflowEdge();

                InterMethodDataflowNode dataflowNode = new InterMethodDataflowNode();
                dataflowNode.setJavaClassName(terminalClass.getName());
                dataflowNode.setJavaMethodName(terminalMethod.getName());
                dataflowNode.setAbstractResourceTemplate(dataflowResource);
                interProcessEdge.setConsumer(dataflowNode);

                MethodInvocationDataflowEdge dataflowEdge = new MethodInvocationDataflowEdge();
                dataflowEdge.setProducer(dataflowNode);

                Stack<DataflowGraphComponent> dataflows = new Stack<>();
                dataflows.push(interProcessEdge);
                dataflows.push(dataflowNode);
                dataflows.push(dataflowEdge);

                Stack<DataflowGraphComponent> dataflowGraph = null;

                Scene.v().loadBasicClasses();
                while (!callTraceStack.isEmpty()) {

                    if (dataflowGraph != null) {
                        dataflowStacks.add(dataflowGraph);
                        //flow formed... done for now. In future we will likely want to continue and find other possible flows, of which there are likely many
                        break;
                    }

                    String callTraceMethod = callTraceStack.pop();

                    callTraceComponents = callTraceMethod.split(" ");
                    callClassOwner = callTraceComponents[0];
                    callMethodDesc = convertCallTraceSigToSootSig(callTraceComponents[1], callTraceComponents[2]);

                    SootClass sClass = Scene.v().loadClassAndSupport(callClassOwner.replace("/", "."));
                    SootMethod sootMethod = sClass.getMethod(callMethodDesc);

                    Body b = sootMethod.retrieveActiveBody();

                    UnitGraph graph = new ExceptionalUnitGraph(b);
                    Iterator gIt = graph.iterator();
                    while (gIt.hasNext()) {
                        Unit u = (Unit) gIt.next();
                        if (u instanceof AssignStmt) {

                            Value rightValue = (Value) ((AssignStmt) u).getRightOp();

                            if (rightValue instanceof VirtualInvokeExpr) {
                                VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) rightValue;
                                SootMethod methodOfInterest = virtualInvokeExpr.getMethod();
                                if (methodOfInterest.equals(terminalMethod)) {
                                    Local localOfInterest = null;

                                    for (Value value : virtualInvokeExpr.getArgs()) {
                                        if (value.getType().equals(Scene.v().getType("byte[]"))) {

                                            dataBeingObserved = BinaryData.class;

                                            Iterator<DataflowGraphComponent> graphIter = dataflows.iterator();
                                            while (graphIter.hasNext()) {
                                                DataflowGraphComponent dataflowGraphComponent = graphIter.next();
                                                if (dataflowGraphComponent instanceof DataflowEdge) {
                                                    DataflowEdge setEdgeData = (DataflowEdge) dataflowGraphComponent;
                                                    setEdgeData.setDataTypeCommunicated(dataBeingObserved);
                                                }
                                            }

                                            localOfInterest = (Local) value;
                                            break;
                                        }
                                    }

                                    int lineNumber = getLineNumber(u);
                                    dataflowNode.setLineNumber(lineNumber);

                                    DataflowGraphComponent dataflowGraphComponent = dataflows.pop();

                                    Map<DataflowAnalysisListener.SemanticProperties, Object> semanticDataProperties = new HashMap<>();
                                    semanticDataProperties.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());

                                    DataflowAnalysisListener.ListenerDataflowNode listenerNode = new DataflowAnalysisListener.ListenerDataflowNode(
                                            dataflowNode, sClass, sootMethod, (AssignStmt) u, new JimpleLocalBox(localOfInterest));
                                    listener.beginTrace(flowUUID += ("-" + UUID.randomUUID()), semanticDataProperties, listenerNode);
                                    dataflowGraph = this.forwardFlowAnalysis(graph, localOfInterest,
                                            callTraceStack, u, dataflowGraphComponent, true, dataflowResource, dataBeingObserved);
                                    listener.endTrace();
                                    dataflowGraph.insertElementAt(dataflows.pop(), 0);
                                    dataflowGraph.insertElementAt(dataflows.pop(), 0);
                                    break;
                                }
                            }
                        } else if (u instanceof InvokeStmt) {
                            InvokeStmt invokeStmt = (InvokeStmt) u;
                            SootMethod methodOfInterest = invokeStmt.getInvokeExpr().getMethod();

                            if (methodOfInterest.equals(terminalMethod)) {

                            }
                        }
                    }
                }
            }
        }
        return dataflowStacks;
    }

    private Stack<DataflowGraphComponent> forwardFlowAnalysis(UnitGraph graph, Local localOfInterest, Stack<String> callTraceStack,
                                                              Unit currentUnit, DataflowGraphComponent edgeGeneric, boolean incoming,
                                                              Class<? extends Resource> dataflowResource, Class<? extends DataType> dataBeingObserved) throws IOException, ClassNotFoundException {

        Stack<DataflowGraphComponent> currentDataflow = new Stack<>();
        currentDataflow.push(edgeGeneric);
        UnitPatchingChain unitPatchingChain = graph.getBody().getUnits();
        Stack<Pair<Unit, Local>> fallbackPoints = new Stack<>();

        while (true) {

            while (unitPatchingChain.getSuccOf(currentUnit) != null) {
                currentUnit = unitPatchingChain.getSuccOf(currentUnit);
                if (currentUnit instanceof AssignStmt) {
                    Value rightValue = ((AssignStmt) currentUnit).getRightOp();
                    Value leftValue = ((AssignStmt) currentUnit).getLeftOp();

                    if (rightValue instanceof Local && rightValue.equals(localOfInterest)) {

                        boolean totalTransform = checkIfTransformIsTotal(unitPatchingChain.getSuccOf(currentUnit), unitPatchingChain, localOfInterest);
                        if (!totalTransform) {
                            fallbackPoints.push(new Pair<>(currentUnit, localOfInterest));
                        }

                        localOfInterest = (Local) leftValue;
                    } else if (rightValue instanceof VirtualInvokeExpr) {
                        //TODO need check to see if new local of interest occurs in rest of graph...
                        VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) rightValue;
                        SootMethod invokedMethod = virtualInvokeExpr.getMethod();
                        if (virtualInvokeExpr.getBase() instanceof Local && virtualInvokeExpr.getBase().equals(localOfInterest)) {
                            boolean totalTransform = checkIfTransformIsTotal(unitPatchingChain.getSuccOf(currentUnit), unitPatchingChain, localOfInterest);
                            if (!totalTransform) {
                                fallbackPoints.push(new Pair<>(currentUnit, localOfInterest));
                            }
                            pushVirtualBaseFlowsAndLocalForward(currentUnit, currentDataflow, invokedMethod, incoming);
                            localOfInterest = (Local) leftValue;
                        } else {
                            Local finalLocalOfInterest = localOfInterest;
                            if (virtualInvokeExpr.getArgs().stream().anyMatch(arg -> arg.equals(finalLocalOfInterest))) {

                                boolean totalTransform = checkIfTransformIsTotal(unitPatchingChain.getSuccOf(currentUnit), unitPatchingChain, localOfInterest);
                                if (!totalTransform) {
                                    fallbackPoints.push(new Pair<>(currentUnit, localOfInterest));
                                }

                                ClassData classData = new ClassData(graph, currentUnit).invoke();

                                MethodInvocationDataflowNode methodInvocationDataflowNode = configureNodeFields(dataflowResource, classData.getCurrentClassName(),
                                        invokedMethod, getLineNumber(currentUnit));

                                MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                                methodInvocationDataflowEdge.setDataTypeCommunicated(dataBeingObserved);
                                if (incoming) {
                                    linkProducerForward(currentDataflow, methodInvocationDataflowNode);
                                    methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
                                } else {
                                    linkProducerReverse(currentDataflow, methodInvocationDataflowNode);
                                    methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                                }

                                currentDataflow.push(methodInvocationDataflowNode);
                                currentDataflow.push(methodInvocationDataflowEdge);

                                Optional<DataType> dataTypeOption = wasDataTransformed(classData.getLineNumber(), classData.getCurrentClassName(), classData.getCurrentMethodName(),
                                        methodInvocationDataflowEdge);

                                //check source (if possible) for local var annots
                                if (dataTypeOption.isPresent()) {

                                    DataType dataType = dataTypeOption.get();
                                    Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                    props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataType.getClass().getCanonicalName());
                                    if (dataType instanceof XmlDocument) {
                                        props.put(DataflowAnalysisListener.SemanticProperties.SCHEMA_VERSION, ((XmlDocument) dataType).getSchemaVersion());
                                    }

                                    alertListener(localOfInterest, (AssignStmt) currentUnit, props, invokedMethod,
                                            methodInvocationDataflowNode);

                                    return currentDataflow;
                                } else if (didReturnTerminalType(invokedMethod, methodInvocationDataflowEdge)) {
                                    return currentDataflow;
                                } else {
                                    localOfInterest = (Local) leftValue;
                                }
                            }
                        }
                    } else if (rightValue instanceof SpecialInvokeExpr) {
                        SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) rightValue;
                        SootMethod invokedMethod = specialInvokeExpr.getMethod();

                        if (specialInvokeExpr.getBase() instanceof Local && specialInvokeExpr.getBase().equals(localOfInterest)) {
                            pushVirtualBaseFlowsAndLocalForward(currentUnit, currentDataflow, invokedMethod, incoming);
                            localOfInterest = (Local) leftValue;
                        } else {

                            Local finalLocalOfInterest1 = localOfInterest;
                            if (specialInvokeExpr.getArgs().stream().anyMatch(arg -> arg.equals(finalLocalOfInterest1))) {

                                boolean totalTransform = checkIfTransformIsTotal(unitPatchingChain.getSuccOf(currentUnit), unitPatchingChain, localOfInterest);
                                if (!totalTransform) {
                                    fallbackPoints.push(new Pair<>(currentUnit, localOfInterest));
                                }

                                ClassData classData = new ClassData(graph, currentUnit).invoke();

                                MethodInvocationDataflowNode methodInvocationDataflowNode = configureNodeFields(dataflowResource, classData.getCurrentClassName(),
                                        invokedMethod, getLineNumber(currentUnit));

                                MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                                methodInvocationDataflowEdge.setDataTypeCommunicated(dataBeingObserved);
                                if (incoming) {
                                    linkProducerForward(currentDataflow, methodInvocationDataflowNode);
                                    methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
                                } else {
                                    linkProducerReverse(currentDataflow, methodInvocationDataflowNode);
                                    methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                                }

                                currentDataflow.push(methodInvocationDataflowNode);
                                currentDataflow.push(methodInvocationDataflowEdge);

                                Optional<DataType> dataTypeOption = wasDataTransformed(classData.getLineNumber(), classData.getCurrentClassName(),
                                        classData.getCurrentMethodName(), methodInvocationDataflowEdge);

                                if (dataTypeOption.isPresent()) {

                                    DataType dataType = dataTypeOption.get();
                                    Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                    props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataType.getClass().getCanonicalName());
                                    if (dataType instanceof XmlDocument) {
                                        props.put(DataflowAnalysisListener.SemanticProperties.SCHEMA_VERSION, ((XmlDocument) dataType).getSchemaVersion());
                                    }

                                    alertListener(localOfInterest, (AssignStmt) currentUnit, props, invokedMethod,
                                            methodInvocationDataflowNode);

                                    return currentDataflow;
                                } else if (didReturnTerminalType(invokedMethod, methodInvocationDataflowEdge)) {
                                    return currentDataflow;
                                } else {
                                    //dive into method for further annots
                                    int argNumb = getArgNumb(finalLocalOfInterest1, specialInvokeExpr);
                                    boolean withinAnalysisPurview = checkInvokeClass(specialInvokeExpr.getMethod().getDeclaringClass());

                                    if (withinAnalysisPurview) {
                                        DataflowGraphComponent dataflowGraphComponent = currentDataflow.pop();
                                        Pair<Unit, Local> unitLocalPair = getLocalAndFirstUnit(specialInvokeExpr.getMethod(), argNumb);
                                        Stack<DataflowGraphComponent> dataflows = this.forwardFlowAnalysis(new ExceptionalUnitGraph(invokedMethod.retrieveActiveBody()),
                                                unitLocalPair.getO2(), callTraceStack, unitLocalPair.getO1(), dataflowGraphComponent, incoming, dataflowResource, dataBeingObserved);

                                        if (!dataflows.isEmpty()) {
                                            currentDataflow.addAll(dataflows);
                                            return currentDataflow;
                                        } else {
                                            currentDataflow.push(dataflowGraphComponent);
                                        }
                                    }

                                    Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                    props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());
                                    alertListener(localOfInterest, (AssignStmt) currentUnit, props, invokedMethod,
                                            methodInvocationDataflowNode);

                                    localOfInterest = (Local) leftValue;
                                }
                            }
                        }
                    } else if (rightValue instanceof CastExpr) {
                        CastExpr castExpr = (CastExpr) rightValue;
                        if (castExpr.getOp().equals(localOfInterest)) {
                            if (leftValue instanceof Local) {
                                localOfInterest = (Local) leftValue;
                            }
                        }
                    } else if (rightValue instanceof InterfaceInvokeExpr) {
                        InterfaceInvokeExpr interfaceInvokeExpr = (InterfaceInvokeExpr) rightValue;
                        SootMethod invokedMethod = interfaceInvokeExpr.getMethod();
                        if (interfaceInvokeExpr.getBase() instanceof Local) {
                            Local baseLocal = (Local) interfaceInvokeExpr.getBase();
                            if (baseLocal.equals(localOfInterest)) {

                                boolean totalTransform = checkIfTransformIsTotal(unitPatchingChain.getSuccOf(currentUnit), unitPatchingChain, localOfInterest);
                                if (!totalTransform) {
                                    fallbackPoints.push(new Pair<>(currentUnit, localOfInterest));
                                }

                                if (leftValue instanceof Local) {
                                    Local assignedValue = (Local) leftValue;
                                    if (assignedValue.getName().contains("$")) {
                                        //intermediate value
                                        //check if intermediate value is just being used for counter
                                        Unit intermediateCheck = unitPatchingChain.getSuccOf(currentUnit);
                                        if (intermediateCheck instanceof IfStmt) {
                                            IfStmt ifStmt = (IfStmt) intermediateCheck;
                                            if (ifStmt.getCondition() instanceof ConditionExpr) {
                                                ConditionExpr conditionExpr = (ConditionExpr) ifStmt.getCondition();
                                                if (!(conditionExpr.getOp1().equals(assignedValue) || conditionExpr.getOp2().equals(assignedValue))) {
                                                    //intermediate value is NOT a counter
                                                    localOfInterest = assignedValue;
                                                }
                                            }
                                        } else {
                                            localOfInterest = assignedValue;
                                        }
                                    } else {
                                        localOfInterest = assignedValue;
                                    }
                                }
                            } else {

                                Local finalLocalOfInterest1 = localOfInterest;
                                if (interfaceInvokeExpr.getArgs().stream().anyMatch(arg -> arg.equals(finalLocalOfInterest1))) {

                                    boolean totalTransform = checkIfTransformIsTotal(unitPatchingChain.getSuccOf(currentUnit), unitPatchingChain, localOfInterest);
                                    if (!totalTransform) {
                                        fallbackPoints.push(new Pair<>(currentUnit, localOfInterest));
                                    }

                                    ClassData classData = new ClassData(graph, currentUnit).invoke();

                                    MethodInvocationDataflowNode methodInvocationDataflowNode = configureNodeFields(dataflowResource, classData.getCurrentClassName(),
                                            invokedMethod, getLineNumber(currentUnit));

                                    MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                                    methodInvocationDataflowEdge.setDataTypeCommunicated(dataBeingObserved);
                                    if (incoming) {
                                        linkProducerForward(currentDataflow, methodInvocationDataflowNode);
                                        methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
                                    } else {
                                        linkProducerReverse(currentDataflow, methodInvocationDataflowNode);
                                        methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                                    }

                                    currentDataflow.push(methodInvocationDataflowNode);
                                    currentDataflow.push(methodInvocationDataflowEdge);

                                    Optional<DataType> dataTypeOption = wasDataTransformed(classData.getLineNumber(), classData.getCurrentClassName(),
                                            classData.getCurrentMethodName(), methodInvocationDataflowEdge);

                                    if (dataTypeOption.isPresent()) {

                                        DataType dataType = dataTypeOption.get();
                                        Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                        props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataType.getClass().getCanonicalName());
                                        if (dataType instanceof XmlDocument) {
                                            props.put(DataflowAnalysisListener.SemanticProperties.SCHEMA_VERSION, ((XmlDocument) dataType).getSchemaVersion());
                                        }

                                        alertListener(localOfInterest, (AssignStmt) currentUnit, props, invokedMethod,
                                                methodInvocationDataflowNode);

                                        return currentDataflow;
                                    } else if (didReturnTerminalType(invokedMethod, methodInvocationDataflowEdge)) {
                                        return currentDataflow;
                                    } else {
                                        //dive into method for further annots
                                        int argNumb = getArgNumb(finalLocalOfInterest1, interfaceInvokeExpr);
                                        boolean withinAnalysisPurview = checkInvokeClass(interfaceInvokeExpr.getMethod().getDeclaringClass());

                                        if (withinAnalysisPurview) {
                                            DataflowGraphComponent dataflowGraphComponent = currentDataflow.pop();
                                            Pair<Unit, Local> unitLocalPair = getLocalAndFirstUnit(interfaceInvokeExpr.getMethod(), argNumb);
                                            Stack<DataflowGraphComponent> dataflows = this.forwardFlowAnalysis(new ExceptionalUnitGraph(invokedMethod.retrieveActiveBody()),
                                                    unitLocalPair.getO2(), callTraceStack, unitLocalPair.getO1(), dataflowGraphComponent, incoming,
                                                    dataflowResource, dataBeingObserved);

                                            if (!dataflows.isEmpty()) {
                                                currentDataflow.addAll(dataflows);
                                                return currentDataflow;
                                            } else {
                                                currentDataflow.push(dataflowGraphComponent);
                                            }
                                        }

                                        Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                        props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());
                                        alertListener(localOfInterest, (AssignStmt) currentUnit, props, invokedMethod,
                                                methodInvocationDataflowNode);

                                        localOfInterest = (Local) leftValue;
                                    }
                                }
                            }
                        }
                    }
                } else if (currentUnit instanceof InvokeStmt) {
                    InvokeStmt invokeStmt = (InvokeStmt) currentUnit;
                    InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
                    if (invokeExpr instanceof VirtualInvokeExpr) {

                        VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) invokeExpr;
                        SootMethod invokedMethod = invokeExpr.getMethod();

                        if (virtualInvokeExpr.getBase() instanceof Local && virtualInvokeExpr.getBase().equals(localOfInterest)) {
                            //TODO might need to branch off of any arguments provided
                            pushVirtualBaseFlowsAndLocalForward(currentUnit, currentDataflow, invokedMethod, incoming);
                        } else {
                            Local finalLocalOfInterest = localOfInterest;
                            if (virtualInvokeExpr.getArgs().stream().anyMatch(arg -> arg.equals(finalLocalOfInterest))) {

                                boolean totalTransform = checkIfTransformIsTotal(unitPatchingChain.getSuccOf(currentUnit), unitPatchingChain, localOfInterest);
                                if (!totalTransform) {
                                    fallbackPoints.push(new Pair<>(currentUnit, localOfInterest));
                                }

                                ClassData classData = new ClassData(graph, currentUnit).invoke();

                                MethodInvocationDataflowNode methodInvocationDataflowNode = configureNodeFields(dataflowResource, classData.getCurrentClassName(),
                                        invokedMethod, getLineNumber(currentUnit));

                                MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                                methodInvocationDataflowEdge.setDataTypeCommunicated(dataBeingObserved);
                                if (incoming) {
                                    linkProducerForward(currentDataflow, methodInvocationDataflowNode);
                                    methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
                                } else {
                                    linkProducerReverse(currentDataflow, methodInvocationDataflowNode);
                                    methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                                }

                                currentDataflow.push(methodInvocationDataflowNode);
                                currentDataflow.push(methodInvocationDataflowEdge);

                                Optional<DataType> dataTypeOption = wasDataTransformed(classData.getLineNumber(), classData.getCurrentClassName(),
                                        classData.getCurrentMethodName(), methodInvocationDataflowEdge);

                                //check source (if possible) for local var annots
                                if (dataTypeOption.isPresent()) {

                                    DataType dataType = dataTypeOption.get();
                                    Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                    props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataType.getClass().getCanonicalName());
                                    if (dataType instanceof XmlDocument) {
                                        props.put(DataflowAnalysisListener.SemanticProperties.SCHEMA_VERSION, ((XmlDocument) dataType).getSchemaVersion());
                                    }

                                    alertListener(localOfInterest, (InvokeStmt) currentUnit, props, invokedMethod,
                                            methodInvocationDataflowNode);

                                    return currentDataflow;
                                } else if (didReturnTerminalType(invokedMethod, methodInvocationDataflowEdge)) {
                                    return currentDataflow;
                                } else {

                                    Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                    props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());
                                    alertListener(localOfInterest, (InvokeStmt) currentUnit, props, invokedMethod,
                                            methodInvocationDataflowNode);

                                    localOfInterest = (Local) virtualInvokeExpr.getBase();
                                }
                            }
                        }
                    } else if (invokeExpr instanceof SpecialInvokeExpr) {
                        SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) invokeExpr;
                        SootMethod invokedMethod = invokeExpr.getMethod();

                        Local finalLocalOfInterest1 = localOfInterest;
                        if (specialInvokeExpr.getArgs().stream().anyMatch(arg -> arg.equals(finalLocalOfInterest1))) {

                            boolean totalTransform = checkIfTransformIsTotal(unitPatchingChain.getSuccOf(currentUnit), unitPatchingChain, localOfInterest);
                            if (!totalTransform) {
                                fallbackPoints.push(new Pair<>(currentUnit, localOfInterest));
                            }

                            ClassData classData = new ClassData(graph, currentUnit).invoke();

                            MethodInvocationDataflowNode methodInvocationDataflowNode = configureNodeFields(dataflowResource, classData.getCurrentClassName(),
                                    invokedMethod, getLineNumber(currentUnit));

                            MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                            methodInvocationDataflowEdge.setDataTypeCommunicated(dataBeingObserved);
                            if (incoming) {
                                linkProducerForward(currentDataflow, methodInvocationDataflowNode);
                                methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
                            } else {
                                linkProducerReverse(currentDataflow, methodInvocationDataflowNode);
                                methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                            }

                            currentDataflow.push(methodInvocationDataflowNode);
                            currentDataflow.push(methodInvocationDataflowEdge);

                            Optional<DataType> dataTypeOption = wasDataTransformed(classData.getLineNumber(), classData.getCurrentClassName(),
                                    classData.getCurrentMethodName(), methodInvocationDataflowEdge);

                            if (dataTypeOption.isPresent()) {

                                DataType dataType = dataTypeOption.get();
                                Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataType.getClass().getCanonicalName());
                                if (dataType instanceof XmlDocument) {
                                    props.put(DataflowAnalysisListener.SemanticProperties.SCHEMA_VERSION, ((XmlDocument) dataType).getSchemaVersion());
                                }

                                alertListener(localOfInterest, (InvokeStmt) currentUnit, props, invokedMethod,
                                        methodInvocationDataflowNode);

                                return currentDataflow;
                            } else if (didReturnTerminalType(invokedMethod, methodInvocationDataflowEdge)) {
                                return currentDataflow;
                            } else {
                                if (specialInvokeExpr.getBase() instanceof Local) {

                                    Local baseLocal = (Local) specialInvokeExpr.getBase();
                                    if (baseLocal.getName().contains("$")) {
                                        //intermediate value
                                        localOfInterest = baseLocal;

                                        Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                        props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());
                                        alertListener(localOfInterest, (InvokeStmt) currentUnit, props, invokedMethod,
                                                methodInvocationDataflowNode);
                                    }
                                }
                            }
                        }
                    } else if (invokeExpr instanceof InterfaceInvokeExpr) {

                        InterfaceInvokeExpr interfaceInvokeExpr = (InterfaceInvokeExpr) invokeExpr;
                        Local finalLocalOfInterest1 = localOfInterest;
                        SootMethod invokedMethod = invokeExpr.getMethod();

                        if (interfaceInvokeExpr.getBase() instanceof Local && interfaceInvokeExpr.getBase().equals(localOfInterest)) {

                            boolean totalTransform = checkIfTransformIsTotal(unitPatchingChain.getSuccOf(currentUnit), unitPatchingChain, localOfInterest);
                            if (!totalTransform) {
                                fallbackPoints.push(new Pair<>(currentUnit, localOfInterest));
                            }

                            ClassData classData = new ClassData(graph, currentUnit).invoke();

                            MethodInvocationDataflowNode methodInvocationDataflowNode = configureNodeFields(dataflowResource, classData.getCurrentClassName(),
                                    invokedMethod, getLineNumber(currentUnit));

                            MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                            methodInvocationDataflowEdge.setDataTypeCommunicated(dataBeingObserved);
                            if (incoming) {
                                linkProducerForward(currentDataflow, methodInvocationDataflowNode);
                                methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
                            } else {
                                linkProducerReverse(currentDataflow, methodInvocationDataflowNode);
                                methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                            }

                            SootClass invokedClass = invokedMethod.getDeclaringClass();
                            String className = invokedClass.getName();
                            Class<?> classObject = this.getClass().getClassLoader().loadClass(className);

                            currentDataflow.push(methodInvocationDataflowNode);
                            currentDataflow.push(methodInvocationDataflowEdge);

                            Optional<DataType> dataTypeOption = wasDataTransformed(classData.getLineNumber(), classData.getCurrentClassName(),
                                    classData.getCurrentMethodName(), methodInvocationDataflowEdge);

                            if (dataTypeOption.isPresent()) {

                                DataType dataType = dataTypeOption.get();
                                Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataType.getClass().getCanonicalName());
                                if (dataType instanceof XmlDocument) {
                                    props.put(DataflowAnalysisListener.SemanticProperties.SCHEMA_VERSION, ((XmlDocument) dataType).getSchemaVersion());
                                }

                                alertListener(localOfInterest, (InvokeStmt) currentUnit, props, invokedMethod,
                                        methodInvocationDataflowNode);

                                return currentDataflow;
                            } else if (didReturnTerminalType(invokedMethod, methodInvocationDataflowEdge)) {
                                return currentDataflow;
                            }

                            if (classObject.isAssignableFrom(Collection.class)) {
                                // collection object, keep current local of interest
                            } else {
                                localOfInterest = (Local) interfaceInvokeExpr.getBase();

                                Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());
                                alertListener(localOfInterest, (InvokeStmt) currentUnit, props, invokedMethod,
                                        methodInvocationDataflowNode);
                            }
                        } else if (interfaceInvokeExpr.getArgs().stream().anyMatch(arg -> arg instanceof Local && arg.equals(finalLocalOfInterest1))) {

                            boolean totalTransform = checkIfTransformIsTotal(unitPatchingChain.getSuccOf(currentUnit), unitPatchingChain, localOfInterest);
                            if (!totalTransform) {
                                fallbackPoints.push(new Pair<>(currentUnit, localOfInterest));
                            }

                            ClassData classData = new ClassData(graph, currentUnit).invoke();

                            MethodInvocationDataflowNode methodInvocationDataflowNode = configureNodeFields(dataflowResource, classData.getCurrentClassName(),
                                    invokedMethod, getLineNumber(currentUnit));

                            MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                            methodInvocationDataflowEdge.setDataTypeCommunicated(dataBeingObserved);
                            if (incoming) {
                                linkProducerForward(currentDataflow, methodInvocationDataflowNode);
                                methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
                            } else {
                                linkProducerReverse(currentDataflow, methodInvocationDataflowNode);
                                methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                            }

                            currentDataflow.push(methodInvocationDataflowNode);
                            currentDataflow.push(methodInvocationDataflowEdge);

                            Optional<DataType> dataTypeOption = wasDataTransformed(classData.getLineNumber(), classData.getCurrentClassName(),
                                    classData.getCurrentMethodName(), methodInvocationDataflowEdge);

                            if (dataTypeOption.isPresent()) {

                                DataType dataType = dataTypeOption.get();
                                Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataType.getClass().getCanonicalName());
                                if (dataType instanceof XmlDocument) {
                                    props.put(DataflowAnalysisListener.SemanticProperties.SCHEMA_VERSION, ((XmlDocument) dataType).getSchemaVersion());
                                }

                                alertListener(localOfInterest, (InvokeStmt) currentUnit, props, invokedMethod,
                                        methodInvocationDataflowNode);

                                return currentDataflow;
                            } else if (didReturnTerminalType(invokedMethod, methodInvocationDataflowEdge)) {
                                return currentDataflow;
                            } else {
                                if (interfaceInvokeExpr.getBase() instanceof Local) {

                                    //if (baseLocal.getName().contains("$")) {
                                        //intermediate value
                                        localOfInterest = (Local) interfaceInvokeExpr.getBase();

                                        Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                                        props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());
                                        alertListener(localOfInterest, (InvokeStmt) currentUnit, props, invokedMethod,
                                                methodInvocationDataflowNode);
                                   // }
                                }
                            }
                        }
                    }
                } else if (currentUnit instanceof ReturnStmt) {

                    ReturnStmt returnStmt = (ReturnStmt) currentUnit;
                    if (returnStmt.getOp().equals(localOfInterest)) {
                        SootMethod sMethod = retrieveNextMethodInTrace(callTraceStack);

                        List<Pair<Unit, Local>> unitLocalPairs = retrieveUnitOfInterestInvokeAssign(graph.getBody().getMethod(), sMethod, null);
                        DataflowGraphComponent dataflowGraphComponent = currentDataflow.pop();
                        for (Pair<Unit, Local> unitLocalPair : unitLocalPairs) {
                            Stack<DataflowGraphComponent> dataflows = this.forwardFlowAnalysis(new ExceptionalUnitGraph(sMethod.retrieveActiveBody()),
                                    unitLocalPair.getO2(), callTraceStack, unitLocalPair.getO1(), dataflowGraphComponent, incoming, dataflowResource, dataBeingObserved);

                            if (!dataflows.isEmpty()) {
                                currentDataflow.addAll(dataflows);
                                return currentDataflow;
                            }
                        }
                        currentDataflow.push(dataflowGraphComponent);
                    }
                } else if (currentUnit instanceof ReturnVoidStmt) {

                    //return is void, check parameters for object of interest
                    List<IdentityStmt> identityStmts = unitPatchingChain.stream().filter(unit -> unit instanceof IdentityStmt)
                            .map(unit -> (IdentityStmt) unit).collect(Collectors.toList());
                    for (IdentityStmt identityStmt : identityStmts) {
                        if (identityStmt.getLeftOp().equals(localOfInterest)) {

                            ParameterRef parameterRef = (ParameterRef) identityStmt.getRightOp();
                            String desc = parameterRef.toString();
                            Pattern pattern = Pattern.compile("@parameter(\\d):");
                            Matcher matcher = pattern.matcher(desc);

                            int paramNumb = -1;
                            if (matcher.find()) {
                                paramNumb = Integer.parseInt(matcher.group(1));
                            }

                            SootMethod sMethod = retrieveNextMethodInTrace(callTraceStack);

                            if (paramNumb != -1) {
                                List<Pair<Unit, Local>> unitLocalPairs = retrieveUnitOfInterestInvokeArg(graph.getBody().getMethod(), sMethod, paramNumb, null);

                                DataflowGraphComponent dataflowGraphComponent = currentDataflow.pop();
                                for (Pair<Unit, Local> unitLocalPair : unitLocalPairs) {
                                    Stack<DataflowGraphComponent> dataflows = this.forwardFlowAnalysis(new ExceptionalUnitGraph(sMethod.retrieveActiveBody()),
                                            unitLocalPair.getO2(), callTraceStack, unitLocalPair.getO1(), dataflowGraphComponent, incoming, dataflowResource, dataBeingObserved);

                                    if (!dataflows.isEmpty()) {
                                        currentDataflow.addAll(dataflows);
                                        return currentDataflow;
                                    }
                                }
                                currentDataflow.push(dataflowGraphComponent);
                            }
                        }
                    }
                } else if (currentUnit instanceof ThrowStmt) {
                    ThrowStmt throwStmt = (ThrowStmt) currentUnit;
                    if (throwStmt.getOp().equals(localOfInterest)) {

                        if (fallbackPoints.isEmpty()) {
                            break;
                        } else {
                            Pair<Unit, Local> fallbackPoint = fallbackPoints.pop();
                            currentUnit = fallbackPoint.getO1();
                            localOfInterest = fallbackPoint.getO2();
                            continue;
                        }
                    }
                }
            }

            if (fallbackPoints.isEmpty()) {
                break;
            }

            Pair<Unit, Local> fallbackPoint = fallbackPoints.pop();
            currentUnit = fallbackPoint.getO1();
            localOfInterest = fallbackPoint.getO2();
        }

        //TODO current stop-gap, need to improve dataflow analysis
        currentDataflow.clear();
        return currentDataflow;
    }

    private SootMethod retrieveNextMethodInTrace(Stack<String> callTraceStack) {
        String nextNode = callTraceStack.pop();
        String[] callTraceComponents = nextNode.split(" ");
        String callClassOwner = callTraceComponents[0];
        String callMethodDesc = convertCallTraceSigToSootSig(callTraceComponents[1], callTraceComponents[2]);
        SootClass sClass = Scene.v().loadClassAndSupport(callClassOwner.replace("/", "."));
        return sClass.getMethod(callMethodDesc);
    }

    private boolean didReturnTerminalType(SootMethod invokedMethod, MethodInvocationDataflowEdge methodInvocationDataflowEdge) {
        String objectOfInterest = getObjectOfInterest(invokedMethod, classNameToSemantics);
        if (objectOfInterest != null) {
            // attempting to transform data, end of data flow analysis???
            methodInvocationDataflowEdge.setDataTypeCommunicated(XmlDocument.class);
            return true;
        }
        return false;
    }

    private boolean checkIfTransformIsTotal(Unit currentUnit, UnitPatchingChain unitPatchingChain, Local currentLOI) {

        while (currentUnit != null) {

            Unit unitToAnalyze = null;
            Expr exprToAnalyze = null;
            if (currentUnit instanceof AssignStmt) {
                //same process, just apply to both sides
                Value rightValue = ((AssignStmt) currentUnit).getRightOp();

                try {
                    unitToAnalyze = (Unit) rightValue;
                } catch (Exception exc) {// naked expr

                    if (rightValue instanceof Expr) {
                        exprToAnalyze = (Expr) rightValue;
                    }

                }
            } else {
                unitToAnalyze = currentUnit;
            }

            if (unitToAnalyze != null) {

                if (unitToAnalyze instanceof Stmt) {
                    Stmt stmt = (Stmt) unitToAnalyze;
                    if (stmt instanceof InvokeStmt) {
                        InvokeExpr expr = stmt.getInvokeExpr();

                        if (expr instanceof StaticInvokeExpr) {
                            //no base
                            StaticInvokeExpr staticInvokeExpr = (StaticInvokeExpr) expr;
                            if (staticInvokeExpr.getArgs().stream().anyMatch(value -> (value instanceof Local) && value.equals(currentLOI))) {
                                return false;
                            }
                        } else {
                            //has base
                            InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) expr;
                            if (instanceInvokeExpr.getBase() instanceof Local && instanceInvokeExpr.getBase().equals(currentLOI)) {
                                return false;
                            } else if (instanceInvokeExpr.getArgs().stream().anyMatch(value -> (value instanceof Local) && value.equals(currentLOI))) {
                                return false;
                            }
                        }
                    }
                }
            } else {

                if (exprToAnalyze instanceof StaticInvokeExpr) {
                    //no base
                    StaticInvokeExpr staticInvokeExpr = (StaticInvokeExpr) exprToAnalyze;
                    if (staticInvokeExpr.getArgs().stream().anyMatch(value -> (value instanceof Local) && value.equals(currentLOI))) {
                        return false;
                    }
                } else if (exprToAnalyze instanceof InstanceInvokeExpr) {
                    //has base
                    InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) exprToAnalyze;
                    if (instanceInvokeExpr.getBase() instanceof Local && instanceInvokeExpr.getBase().equals(currentLOI)) {
                        return false;
                    } else if (instanceInvokeExpr.getArgs().stream().anyMatch(value -> (value instanceof Local) && value.equals(currentLOI))) {
                        return false;
                    }
                }
            }

           currentUnit = unitPatchingChain.getSuccOf(currentUnit);
        }

        return true;
    }

    private Pair<Unit, Local> getLocalAndFirstUnit(SootMethod method, int argNumb) {

        Local localOfInterest = null;
        Unit lastIdentityUnit = null;

        Body methodBody = method.retrieveActiveBody();
        Chain<Unit> units = methodBody.getUnits();
        boolean foundParamAssignment = false;

        for (Unit unit : units) {
            if (unit instanceof IdentityStmt && !foundParamAssignment) {
                IdentityStmt identityStmt = (IdentityStmt) unit;
                Value value = identityStmt.getRightOpBox().getValue();
                if (value instanceof ParameterRef) {
                    ParameterRef parameterRef = (ParameterRef) value;
                    if (parameterRef.getIndex() == argNumb) {
                        //FOUND OBJECT OF INTEREST
                        foundParamAssignment = true;
                        value = identityStmt.getLeftOpBox().getValue();
                        if (value instanceof Local) {
                            localOfInterest = (Local) value;
                        }
                    }
                }
            } else if (!(unit instanceof IdentityStmt) && !foundParamAssignment) {
                // no idea how this happens...
                throw new RuntimeException("UNABLE TO FIND PARAM IN METHOD BODY");
            } else if (!(unit instanceof IdentityStmt) && foundParamAssignment) {
                lastIdentityUnit = units.getPredOf(unit);
                break;
            }
        }

        if (localOfInterest != null && lastIdentityUnit != null) {
            return new Pair<>(lastIdentityUnit, localOfInterest);
        }

        return null;
    }

    private boolean checkInvokeClass(SootClass declaringClass) {
        String queryDeclaringClass = "";
        //TODO temp... write actual query
        if (declaringClass.getName().contains("MessageListenerClient")) {
            return true;
        }
        return false;
    }

    private static List<Pair<Unit, Local>> retrieveUnitOfInterestInvokeAssign(SootMethod currentMethod, SootMethod methodOfInterest,
                                                                              MethodInvocationDataflowNode dataflowNode) {

        List<Pair<Unit, Local>> unitToLocalPairs = new ArrayList<>();
        Body b = methodOfInterest.retrieveActiveBody();

        UnitGraph graph = new ExceptionalUnitGraph(b);
        Iterator gIt = graph.iterator();
        while (gIt.hasNext()) {
            Unit u = (Unit) gIt.next();
            if (u instanceof AssignStmt) {

                Value rightValue = ((AssignStmt) u).getRightOp();
                Value leftValue = ((AssignStmt) u).getLeftOp();

                if (rightValue instanceof VirtualInvokeExpr) {

                } else if (rightValue instanceof SpecialInvokeExpr) {

                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) rightValue;
                    if (specialInvokeExpr.getMethod().equals(currentMethod) && leftValue instanceof Local) {
                        if (dataflowNode != null) {
                            dataflowNode.setLineNumber(getLineNumber(u));
                        }
                        unitToLocalPairs.add(new Pair<>(u, (Local) leftValue));
                    }

                } else if (rightValue instanceof StaticInvokeExpr) {

                }

            } else if (u instanceof InvokeStmt) {

            } else if (u instanceof ReturnStmt) {
                ReturnStmt returnStmt = (ReturnStmt) u;
                Value returnValue = returnStmt.getOp();

                if (returnValue instanceof SpecialInvokeExpr) {
                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) returnValue;
                    if (specialInvokeExpr.getMethod().equals(currentMethod)) {
                        // dataflowNode.setLineNumber(getLineNumber(u));
                        // return new Pair<>(u, (Local) specialInvokeExpr.getArg(paramNumb));
                    }
                }
            }
        }

        return unitToLocalPairs;
    }


    @Deprecated
    private static List<Pair<Unit, Local>> retrieveUnitOfInterestInvokeAssign(SootMethod currentMethod, SootMethod methodOfInterest) {

        List<Pair<Unit, Local>> unitToLocalPairs = new ArrayList<>();
        Body b = methodOfInterest.retrieveActiveBody();

        UnitGraph graph = new ExceptionalUnitGraph(b);
        Iterator gIt = graph.iterator();
        while (gIt.hasNext()) {
            Unit u = (Unit) gIt.next();
            if (u instanceof AssignStmt) {

                Value rightValue = ((AssignStmt) u).getRightOp();
                Value leftValue = ((AssignStmt) u).getLeftOp();

                if (rightValue instanceof VirtualInvokeExpr) {

                } else if (rightValue instanceof SpecialInvokeExpr) {

                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) rightValue;
                    if (specialInvokeExpr.getMethod().equals(currentMethod) && leftValue instanceof Local) {
                        unitToLocalPairs.add(new Pair<>(u, (Local) leftValue));
                    }

                } else if (rightValue instanceof StaticInvokeExpr) {

                }

            } else if (u instanceof InvokeStmt) {

            } else if (u instanceof ReturnStmt) {
                ReturnStmt returnStmt = (ReturnStmt) u;
                Value returnValue = returnStmt.getOp();

                if (returnValue instanceof SpecialInvokeExpr) {
                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) returnValue;
                    if (specialInvokeExpr.getMethod().equals(currentMethod)) {
                        // dataflowNode.setLineNumber(getLineNumber(u));
                        // return new Pair<>(u, (Local) specialInvokeExpr.getArg(paramNumb));
                    }
                }
            }
        }

        return unitToLocalPairs;
    }

    private void pushVirtualBaseFlowsAndLocalForward(Unit currentUnit, Stack<DataflowGraphComponent> currentDataflow, SootMethod invokedMethod, boolean incoming) {
        MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
        methodInvocationDataflowNode.setJavaMethodName(invokedMethod.getName());
        methodInvocationDataflowNode.setJavaClassName(invokedMethod.getDeclaringClass().getName());
        methodInvocationDataflowNode.setLineNumber(getLineNumber(currentUnit));
        methodInvocationDataflowNode.setAbstractResourceTemplate(Client.class);

        MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
        methodInvocationDataflowEdge.setDataTypeCommunicated(BinaryData.class);
        if (incoming) {
            linkProducerForward(currentDataflow, methodInvocationDataflowNode);
            methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
        } else {
            linkProducerReverse(currentDataflow, methodInvocationDataflowNode);
            methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
        }

        currentDataflow.push(methodInvocationDataflowNode);
        currentDataflow.push(methodInvocationDataflowEdge);
    }

    private void linkProducerForward(Stack<DataflowGraphComponent> currentDataflow, MethodInvocationDataflowNode methodInvocationDataflowNode) {
        DataflowGraphComponent edgeGeneric = currentDataflow.pop();
        if (edgeGeneric instanceof MethodInvocationDataflowEdge) {
            MethodInvocationDataflowEdge methodEdge = (MethodInvocationDataflowEdge) edgeGeneric;
            methodEdge.setConsumer(methodInvocationDataflowNode);
            currentDataflow.push(methodEdge);

        } else if (edgeGeneric instanceof InterMethodDataflowEdge) {
            InterMethodDataflowEdge interMethodEdge = (InterMethodDataflowEdge) edgeGeneric;
            interMethodEdge.setConsumer(methodInvocationDataflowNode);
            currentDataflow.push(interMethodEdge);
        }
    }

    private void linkProducerReverse(Stack<DataflowGraphComponent> currentDataflow, MethodInvocationDataflowNode methodInvocationDataflowNode) {
        DataflowGraphComponent edgeGeneric = currentDataflow.pop();
        if (edgeGeneric instanceof MethodInvocationDataflowEdge) {
            MethodInvocationDataflowEdge methodEdge = (MethodInvocationDataflowEdge) edgeGeneric;
            methodEdge.setProducer(methodInvocationDataflowNode);
            currentDataflow.push(methodEdge);

        } else if (edgeGeneric instanceof InterMethodDataflowEdge) {
            InterMethodDataflowEdge interMethodEdge = (InterMethodDataflowEdge) edgeGeneric;
            interMethodEdge.setProducer(methodInvocationDataflowNode);
            currentDataflow.push(interMethodEdge);
        }
    }

    private void pushVirtualBaseFlowsAndLocalReverse(Unit currentUnit, Stack<DataflowGraphComponent> currentDataflow, SootMethod invokedMethod) {
        MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
        methodInvocationDataflowNode.setJavaMethodName(invokedMethod.getName());
        methodInvocationDataflowNode.setJavaClassName(invokedMethod.getDeclaringClass().getName());
        methodInvocationDataflowNode.setLineNumber(getLineNumber(currentUnit));
        methodInvocationDataflowNode.setAbstractResourceTemplate(Client.class);

        linkProducerReverse(currentDataflow, methodInvocationDataflowNode);

        MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
        methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
        methodInvocationDataflowEdge.setDataTypeCommunicated(BinaryData.class);

        currentDataflow.push(methodInvocationDataflowNode);
        currentDataflow.push(methodInvocationDataflowEdge);
    }

    public static String getObjectOfInterest(SootMethod m, Collection<Pair<String, Class<? extends DataType>>> classesOfInterest) {

        for (Pair<String, Class<? extends DataType>> classOfInterest : classesOfInterest) {
            for (Tag tag : m.getTags()) {
                if (tag instanceof VisibilityAnnotationTag) {
                    VisibilityAnnotationTag visibilityAnnotTag = (VisibilityAnnotationTag) tag;
                    for (AnnotationTag annotTag : visibilityAnnotTag.getAnnotations()) {
                        if (classOfInterest.getO1().equals(annotTag.getType())) {
                            return classOfInterest.getO1();
                        }
                    }
                }
            }
        }
        return null;
    }

    private Stack<DataflowGraphComponent> reverseFlowAnalysis(UnitGraph graph, Local localOfInterest, Stack<String> callTraceStack,
                                                              Unit currentUnit, Stack<DataflowGraphComponent> currentDataflow,
                                                              Class<? extends Resource> dataflowResource, Class<? extends DataType> dataBeingObserved) throws IOException, ClassNotFoundException {

        UnitPatchingChain unitPatchingChain = graph.getBody().getUnits();

        while (unitPatchingChain.getPredOf(currentUnit) != null) {
            currentUnit = unitPatchingChain.getPredOf(currentUnit);

            if (currentUnit instanceof AssignStmt) {
                Value rightValue = ((AssignStmt) currentUnit).getRightOp();
                Value leftValue = ((AssignStmt) currentUnit).getLeftOp();

                if (rightValue instanceof VirtualInvokeExpr) {

                    VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) rightValue;
                    SootMethod invokedMethod = virtualInvokeExpr.getMethod();

                    if (virtualInvokeExpr.getBase() instanceof Local && virtualInvokeExpr.getBase().equals(localOfInterest)) {

                        pushVirtualBaseFlowsAndLocalReverse(currentUnit, currentDataflow, invokedMethod);

                    } else if (leftValue.equals(localOfInterest)){

                        ClassData classData = new ClassData(graph, currentUnit).invoke();

                        MethodInvocationDataflowNode methodInvocationDataflowNode = configureNodeFields(dataflowResource, classData.getCurrentClassName(),
                                invokedMethod, classData.getLineNumber());

                        linkProducerReverse(currentDataflow, methodInvocationDataflowNode);

                        MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                        methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                        methodInvocationDataflowEdge.setDataTypeCommunicated(dataBeingObserved);

                        currentDataflow.push(methodInvocationDataflowNode);
                        currentDataflow.push(methodInvocationDataflowEdge);

                        Optional<DataType> dataTypeOption = wasDataTransformed(classData.getLineNumber(), classData.getCurrentClassName(),
                                classData.getCurrentMethodName(), methodInvocationDataflowEdge);

                        if (dataTypeOption.isPresent()) {

                            DataType dataType = dataTypeOption.get();
                            Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                            props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataType.getClass().getCanonicalName());
                            if (dataType instanceof XmlDocument) {
                                props.put(DataflowAnalysisListener.SemanticProperties.SCHEMA_VERSION, ((XmlDocument) dataType).getSchemaVersion());
                            }

                            alertListener(localOfInterest, (AssignStmt) currentUnit, props, invokedMethod,
                                    methodInvocationDataflowNode);

                            return currentDataflow;
                        } else if (didReturnTerminalType(invokedMethod, methodInvocationDataflowEdge)) {
                            return currentDataflow;
                        } else {
                            List<Local> locals = new ArrayList<>();
                            for(Value arg : virtualInvokeExpr.getArgs()) {
                                if (arg instanceof Local) {
                                    locals.add((Local) arg);
                                }
                            }

                            if (!locals.isEmpty()) {
                                for (Local local : locals) {
                                    Stack<DataflowGraphComponent> newGraph = this.reverseFlowAnalysis(graph, local, callTraceStack, currentUnit,
                                            currentDataflow, dataflowResource, dataBeingObserved);
                                    if (!newGraph.isEmpty()) {
                                        // branch was correct, return
                                        return newGraph;
                                    }
                                }
                            }

                            localOfInterest = (Local) virtualInvokeExpr.getBase();

                            Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                            props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());
                            alertListener(localOfInterest, (AssignStmt) currentUnit, props, invokedMethod,
                                    methodInvocationDataflowNode);
                        }
                    } else {
                        int argNumb = getArgNumb(localOfInterest, virtualInvokeExpr);
                        if (argNumb != -1) {
                            String currentClassName = graph.getBody().getMethod().getDeclaringClass().getName();
                            //used by invoke
                            MethodInvocationDataflowNode methodInvocationDataflowNode = configureNodeFields(dataflowResource, currentClassName,
                                    invokedMethod, getLineNumber(currentUnit));

                            linkProducerReverse(currentDataflow, methodInvocationDataflowNode);

                            MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                            methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                            methodInvocationDataflowEdge.setDataTypeCommunicated(dataBeingObserved);

                            Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                            props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());
                            alertListener(localOfInterest, (AssignStmt) currentUnit, props, invokedMethod,
                                    methodInvocationDataflowNode);

                            localOfInterest = (Local) leftValue;
                        }
                    }

                } else if (rightValue instanceof StaticInvokeExpr) {

                } else if (rightValue instanceof SpecialInvokeExpr) {

                    if (leftValue.equals(localOfInterest)){

                        ClassData classData = new ClassData(graph, currentUnit).invoke();

                        SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) rightValue;
                        SootMethod specialInvokeMethod = specialInvokeExpr.getMethod();

                        MethodInvocationDataflowNode methodInvocationDataflowNode = configureNodeFields(dataflowResource, classData.getCurrentClassName(),
                                specialInvokeMethod, getLineNumber(currentUnit));

                        linkProducerReverse(currentDataflow, methodInvocationDataflowNode);

                        MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                        methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                        methodInvocationDataflowEdge.setDataTypeCommunicated(dataBeingObserved);

                        currentDataflow.push(methodInvocationDataflowNode);
                        currentDataflow.push(methodInvocationDataflowEdge);

                        Optional<DataType> dataTypeOption = wasDataTransformed(classData.getLineNumber(), classData.getCurrentClassName(),
                                classData.getCurrentMethodName(), methodInvocationDataflowEdge);

                        if (dataTypeOption.isPresent()) {

                            DataType dataType = dataTypeOption.get();
                            Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                            props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataType.getClass().getCanonicalName());
                            if (dataType instanceof XmlDocument) {
                                props.put(DataflowAnalysisListener.SemanticProperties.SCHEMA_VERSION, ((XmlDocument) dataType).getSchemaVersion());
                            }

                            alertListener(localOfInterest, (AssignStmt) currentUnit, props, specialInvokeMethod,
                                    methodInvocationDataflowNode);

                            return currentDataflow;
                        } else if (didReturnTerminalType(specialInvokeMethod, methodInvocationDataflowEdge)) {
                            return currentDataflow;
                        } else {
                            List<Local> locals = new ArrayList<>();
                            for(Value arg : specialInvokeExpr.getArgs()) {
                                if (arg instanceof Local) {
                                    locals.add((Local) arg);
                                }
                            }

                            if (!locals.isEmpty()) {
                                for (Local local : locals) {

                                    int argNumb = getArgNumb(local, specialInvokeExpr);
                                    boolean withinAnalysisPurview = checkInvokeClass(specialInvokeExpr.getMethod().getDeclaringClass());

                                    if (withinAnalysisPurview) {
                                        DataflowGraphComponent dataflowGraphComponent = currentDataflow.pop();
                                        Pair<Unit, Local> unitLocalPair = getLocalAndFirstUnit(specialInvokeExpr.getMethod(), argNumb);
                                        Stack<DataflowGraphComponent> dataflows = this.forwardFlowAnalysis(new ExceptionalUnitGraph(specialInvokeMethod.retrieveActiveBody()),
                                                unitLocalPair.getO2(), callTraceStack, unitLocalPair.getO1(), dataflowGraphComponent, false,
                                                dataflowResource, dataBeingObserved);

                                        if (!dataflows.isEmpty()) {

                                            //dataflows = reverseDataflowStructure(dataflows);
                                            //TODO the combination of reverse + forward analysis results in the order of nodes to incorrect,
                                            //TODO after looking more closely, it might be as simple as reversing after a forward dive
                                            //TODO no idea what's happening... they now appear to be in the correct order after removing the reversal...
                                            currentDataflow.addAll(dataflows);
                                            return currentDataflow;
                                        } else {
                                            currentDataflow.push(dataflowGraphComponent);
                                        }
                                    }

                                    Stack<DataflowGraphComponent> newGraph = this.reverseFlowAnalysis(graph, local, callTraceStack, currentUnit, currentDataflow,
                                            dataflowResource, dataBeingObserved);
                                    if (!newGraph.isEmpty()) {
                                        // branch was correct, return
                                        return newGraph;
                                    }
                                }
                            }

                            localOfInterest = (Local) specialInvokeExpr.getBase();

                            Map<DataflowAnalysisListener.SemanticProperties, Object> props = new HashMap<>();
                            props.put(DataflowAnalysisListener.SemanticProperties.SEMANTIC_TYPE, dataBeingObserved.getCanonicalName());
                            alertListener(localOfInterest, (AssignStmt) currentUnit, props, specialInvokeMethod,
                                    methodInvocationDataflowNode);

                        }
                    }
                } else if (rightValue instanceof CastExpr) {

                } else if (rightValue instanceof Constant) {

                    if (leftValue.equals(localOfInterest)) {
                        //end of transformations
                        if (callTraceStack.isEmpty()) {
                            //TODO
                        } else {
                            //wrong branch, return
                            return new Stack<>();
                        }
                    } else {

                    }
                }
            } else if (currentUnit instanceof InvokeStmt) {

                InvokeStmt invokeStmt = (InvokeStmt) currentUnit;
                if (invokeStmt instanceof InterfaceInvokeExpr) {
                    InterfaceInvokeExpr interfaceInvokeExpr = (InterfaceInvokeExpr) invokeStmt;
                    if (interfaceInvokeExpr.getMethod().getSignature().equals("java.util.List: boolean add(java.lang.Object)")) {
                        //our local of interest was passed info via another source...
                        List<Local> locals = new ArrayList<>();
                        for(Value arg : interfaceInvokeExpr.getArgs()) {
                            if (arg instanceof Local) {
                                locals.add((Local) arg);
                            }
                        }

                        if (!locals.isEmpty()) {
                            for (Local local : locals) {
                                Stack<DataflowGraphComponent> newGraph = this.reverseFlowAnalysis(graph, local, callTraceStack, currentUnit,
                                        currentDataflow, dataflowResource, dataBeingObserved);
                                if (!newGraph.isEmpty()) {
                                    // branch was correct, return
                                    return newGraph;
                                }
                            }
                        }
                    }
                }


            } else if (currentUnit instanceof ReturnStmt) {

            } else if (currentUnit instanceof IdentityStmt) {

                IdentityStmt identityStmt = (IdentityStmt) currentUnit;

                if (identityStmt.getLeftOp().equals(localOfInterest)) {
                    //found parameter ref of local of interest
                    String currentClassName = graph.getBody().getMethod().getDeclaringClass().getName();
                    ParameterRef parameterRef = (ParameterRef) identityStmt.getRightOp();
                    String desc = parameterRef.toString();
                    Pattern pattern = Pattern.compile("@parameter(\\d):");
                    Matcher matcher = pattern.matcher(desc);

                    int paramNumb = -1;
                    if (matcher.find()) {
                        paramNumb = Integer.parseInt(matcher.group(1));
                    }

                    SootMethod currentMethod = graph.getBody().getMethod();
                    MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
                    methodInvocationDataflowNode.setJavaMethodName(currentMethod.getName());
                    methodInvocationDataflowNode.setJavaClassName(currentMethod.getDeclaringClass().getName());
                    methodInvocationDataflowNode.setAbstractResourceTemplate(dataflowResource);
                    methodInvocationDataflowNode.setEnclosingClassName(currentClassName);

                    linkProducerReverse(currentDataflow, methodInvocationDataflowNode);

                    MethodInvocationDataflowEdge dataflowEdge = new MethodInvocationDataflowEdge();
                    dataflowEdge.setDataTypeCommunicated(dataBeingObserved);
                    dataflowEdge.setConsumer(methodInvocationDataflowNode);

                    SootMethod sMethod = retrieveNextMethodInTrace(callTraceStack);

                    currentDataflow.push(methodInvocationDataflowNode);
                    currentDataflow.push(dataflowEdge);

                    if (paramNumb != -1) {
                        List<Pair<Unit, Local>> unitLocalPairs = retrieveUnitOfInterestInvokeArg(graph.getBody().getMethod(), sMethod, paramNumb, methodInvocationDataflowNode);
                        for (Pair<Unit, Local> unitLocalPair : unitLocalPairs) {

                            Stack<DataflowGraphComponent> dataflows = this.reverseFlowAnalysis(new ExceptionalUnitGraph(sMethod.retrieveActiveBody()), unitLocalPair.getO2(), callTraceStack,
                                    unitLocalPair.getO1(), currentDataflow, dataflowResource, dataBeingObserved);
                            if (!dataflows.isEmpty()) {
                                return dataflows;
                            }
                        }
                    }
                }
            }
        }
        return new Stack<>();
    }

    private void alertListener(Local localOfInterest, Stmt currentUnit,  Map<DataflowAnalysisListener.SemanticProperties, Object> props, SootMethod invokedMethod, MethodInvocationDataflowNode methodInvocationDataflowNode) {
        DataflowAnalysisListener.ListenerDataflowNode listenerNode = new DataflowAnalysisListener.ListenerDataflowNode(
                methodInvocationDataflowNode, invokedMethod.getDeclaringClass(), invokedMethod, currentUnit,
                new JimpleLocalBox(localOfInterest));
        listener.visitTraceNode(props, listenerNode);
    }

    private Stack<DataflowGraphComponent> reverseDataflowStructure(Stack<DataflowGraphComponent> dataflows) {

        DataflowGraphComponent dataflowGraphComponent = dataflows.pop();
        Stack<DataflowGraphComponent> newStack = new Stack<>();
        DataflowEdge dataflowEdge = null;
        if (dataflowGraphComponent instanceof DataflowEdge) {
             dataflowEdge = (DataflowEdge) dataflowGraphComponent;
             dataflowEdge.setProducer(null);
        }

        while (!dataflows.isEmpty()) {
            DataflowGraphComponent node = dataflows.pop();
            dataflowEdge.setConsumer((DataflowNode) node);
            newStack.push(dataflowEdge);
            newStack.push(node);

            dataflowEdge = (DataflowEdge) dataflows.pop();
            dataflowEdge.setProducer((DataflowNode) node);
        }

        newStack.push(dataflowEdge);
        return newStack;
    }

    private MethodInvocationDataflowNode configureNodeFields(Class<? extends Resource> dataflowResource, String currentClassName, SootMethod specialInvokeMethod, int lineNumber2) {
        MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
        methodInvocationDataflowNode.setJavaMethodName(specialInvokeMethod.getName());
        methodInvocationDataflowNode.setJavaClassName(specialInvokeMethod.getDeclaringClass().getName());
        methodInvocationDataflowNode.setLineNumber(lineNumber2);
        methodInvocationDataflowNode.setAbstractResourceTemplate(dataflowResource);
        methodInvocationDataflowNode.setEnclosingClassName(currentClassName);
        return methodInvocationDataflowNode;
    }

    private Optional<DataType> wasDataTransformed(int lineNumber, String currentClassName, String currentMethodName, MethodInvocationDataflowEdge methodInvocationDataflowEdge) throws IOException {
        DataType transformedData = checkForTransform(lineNumber - 1, currentClassName, currentMethodName);
        if (transformedData != null) {
            getClassNamesToNewData().add(new Pair<>(currentClassName, transformedData));
            methodInvocationDataflowEdge.setDataTypeCommunicated(transformedData.getClass());
            return Optional.of(transformedData);
        }
        return Optional.empty();
    }

    private DataType checkForTransform(int lineNumber, String currentClassName, String currentMethodName) throws IOException {

        String getSourceFile = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?sourceFilePath where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t    ?sourceFile IMMoRTALS:hasAbsoluteFilePath ?sourceFilePath\n" +
                "\t\t; IMMoRTALS:hasCorrespondingClass ?classArtifact .\n" +
                "\t\t\n" +
                "\t\t?classArtifact IMMoRTALS:hasClassModel ?aClass .\n" +
                "\t\t\n" +
                "\t\t?aClass IMMoRTALS:hasClassName \"???CLASS_NAME???\"\n" +
                "\t\t; IMMoRTALS:hasMethods ?methods .\n" +
                "\t\t\n" +
                "\t\t?methods IMMoRTALS:hasMethodName \"???METHOD_NAME???\" .\n" +
                "\t}\n" +
                "}";
        getSourceFile = getSourceFile.replace("???GRAPH_NAME???", graphName).replace("???CLASS_NAME???", currentClassName.replace(".", "/"))
                .replace("???METHOD_NAME???", currentMethodName);
        ResultSetAggregator sourceFileResults = new ResultSetAggregator();
        fusekiClient.executeSelectQuery(getSourceFile, sourceFileResults);

        if (!sourceFileResults.getSolutions().isEmpty()) {
            Map<String, RDFNode> solution = sourceFileResults.getSolutions().get(0);
            RDFNode node = solution.get("sourceFilePath");

            String sourceFilePath = node.asLiteral().getLexicalForm();
            File sourceFile = new File(sourceFilePath);
            if (sourceFile.exists()) {
                CompilationUnit compilationUnit = JavaParser.parse(sourceFile);

                Optional<ClassOrInterfaceDeclaration> optionalDeclaration = compilationUnit.getClassByName(currentClassName.substring
                        (currentClassName.lastIndexOf(".") + 1));
                if (optionalDeclaration.isPresent()) {
                    ClassOrInterfaceDeclaration classDeclaration = optionalDeclaration.get();
                    for (MethodDeclaration methodDeclaration : classDeclaration.getMethods()) {
                        Optional<BlockStmt> methodBody = methodDeclaration.getBody();

                        if (methodBody.isPresent()) {
                            BlockStmt blockStmt = methodBody.get();

                            for (Statement statement : blockStmt.getStatements()) {

                                if (statement.isExpressionStmt()) {
                                    ExpressionStmt expressionStmt = (ExpressionStmt) statement;
                                    Expression expression = expressionStmt.getExpression();
                                    if (expression.isVariableDeclarationExpr()) {
                                        VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expression;
                                        for (AnnotationExpr annotationExpr : variableDeclarationExpr.getAnnotations()) {

                                            Optional<Position> positionOptional = annotationExpr.getBegin();
                                            if (positionOptional.isPresent()) {
                                                Position position = positionOptional.get();
                                                if (position.line == lineNumber) {
                                                    if (annotationExpr.getName().getIdentifier().contains("XmlDocument")) {

                                                        //TODO need to derive this somehow... don't think this is a given
                                                        XmlDocument xmlDocument = new XmlDocument();

                                                        for (Node childNode : annotationExpr.getChildNodes()) {

                                                            if (childNode instanceof MemberValuePair) {

                                                                MemberValuePair memberValuePair = (MemberValuePair) childNode;
                                                                String valueString = memberValuePair.getValue().toString();
                                                                switch (memberValuePair.getNameAsString()) {
                                                                    case "schemaVersion":
                                                                        xmlDocument.setSchemaVersion(valueString);
                                                                        break;
                                                                    case "schemaNamespace":
                                                                        xmlDocument.setSchemaNamespace(valueString);
                                                                        break;
                                                                    case "encoding":
                                                                        xmlDocument.setEncoding(valueString);
                                                                        break;
                                                                    case "xmlVersion":
                                                                        xmlDocument.setXmlVersion(valueString);
                                                                        break;
                                                                    default:
                                                                        break;

                                                                }
                                                            }
                                                        }

                                                        return xmlDocument;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (statement.isIfStmt()) {
                                    DataType xmlDocument = parseJavaIf(lineNumber, (com.github.javaparser.ast.stmt.IfStmt) statement);
                                    if (xmlDocument != null) return xmlDocument;
                                } else if (statement.isTryStmt()) {

                                    TryStmt tryStmt = (TryStmt) statement;
                                    BlockStmt tryBlockStmt = tryStmt.getTryBlock();

                                    for (Statement tryStatement : tryBlockStmt.getStatements()) {
                                        if (tryStatement.isExpressionStmt()) {
                                            ExpressionStmt expressionStmt = (ExpressionStmt) tryStatement;
                                            Expression expression = expressionStmt.getExpression();
                                            if (expression.isVariableDeclarationExpr()) {
                                                VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expression;
                                                for (AnnotationExpr annotationExpr : variableDeclarationExpr.getAnnotations()) {
                                                    Optional<Position> positionOptional = annotationExpr.getBegin();
                                                    if (positionOptional.isPresent()) {
                                                        Position position = positionOptional.get();
                                                        if (position.line == lineNumber) {
                                                            if (annotationExpr.getName().getIdentifier().contains("XmlDocument")) {
                                                                XmlDocument xmlDocument = new XmlDocument();

                                                                for (Node childNode : annotationExpr.getChildNodes()) {

                                                                    if (childNode instanceof MemberValuePair) {

                                                                        MemberValuePair memberValuePair = (MemberValuePair) childNode;
                                                                        String valueString = memberValuePair.getValue().toString();
                                                                        switch (memberValuePair.getNameAsString()) {
                                                                            case "schemaVersion":
                                                                                xmlDocument.setSchemaVersion(valueString);
                                                                                break;
                                                                            case "schemaNamespace":
                                                                                xmlDocument.setSchemaNamespace(valueString);
                                                                                break;
                                                                            case "encoding":
                                                                                xmlDocument.setEncoding(valueString);
                                                                                break;
                                                                            case "xmlVersion":
                                                                                xmlDocument.setXmlVersion(valueString);
                                                                                break;
                                                                            default:
                                                                                break;
                                                                        }
                                                                    }
                                                                }

                                                                return xmlDocument;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else if (tryStatement.isIfStmt()) {
                                            DataType xmlDocument = parseJavaIf(lineNumber, (com.github.javaparser.ast.stmt.IfStmt) tryStatement);
                                            if (xmlDocument != null) return xmlDocument;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private DataType parseJavaIf(int lineNumber, com.github.javaparser.ast.stmt.IfStmt statement) {

        com.github.javaparser.ast.stmt.IfStmt ifStmt = statement;
        Statement thenStmt = ifStmt.getThenStmt();
        if (thenStmt.isBlockStmt()) {
            for (Statement ifBlockStmt : thenStmt.asBlockStmt().getStatements()) {

                if (ifBlockStmt.isReturnStmt()) {
                    com.github.javaparser.ast.stmt.ReturnStmt returnStmt = (com.github.javaparser.ast.stmt.ReturnStmt) ifBlockStmt;
                    if (returnStmt.getExpression().isPresent()) {
                        Expression expression = returnStmt.getExpression().get();
                        if (expression.isVariableDeclarationExpr()) {
                            VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expression;
                            for (AnnotationExpr annotationExpr : variableDeclarationExpr.getAnnotations()) {
                                Optional<Position> positionOptional = annotationExpr.getBegin();
                                if (positionOptional.isPresent()) {
                                    Position position = positionOptional.get();
                                    if (position.line == lineNumber) {
                                        if (annotationExpr.getName().getIdentifier().contains("XmlDocument")) {
                                            XmlDocument xmlDocument = new XmlDocument();

                                            for (Node childNode : annotationExpr.getChildNodes()) {

                                                if (childNode instanceof MemberValuePair) {

                                                    MemberValuePair memberValuePair = (MemberValuePair) childNode;
                                                    String valueString = memberValuePair.getValue().toString();
                                                    switch (memberValuePair.getNameAsString()) {
                                                        case "schemaVersion":
                                                            xmlDocument.setSchemaVersion(valueString);
                                                            break;
                                                        case "schemaNamespace":
                                                            xmlDocument.setSchemaNamespace(valueString);
                                                            break;
                                                        case "encoding":
                                                            xmlDocument.setEncoding(valueString);
                                                            break;
                                                        case "xmlVersion":
                                                            xmlDocument.setXmlVersion(valueString);
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                }
                                            }

                                            return xmlDocument;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        continue;
                    }
                }

                if (ifBlockStmt.isExpressionStmt()) {
                    ExpressionStmt expressionStmt = (ExpressionStmt) ifBlockStmt;
                    Expression expression = expressionStmt.getExpression();
                    if (expression.isVariableDeclarationExpr()) {
                        VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expression;
                        for (AnnotationExpr annotationExpr : variableDeclarationExpr.getAnnotations()) {
                            Optional<Position> positionOptional = annotationExpr.getBegin();
                            if (positionOptional.isPresent()) {
                                Position position = positionOptional.get();
                                if (position.line == lineNumber) {
                                    if (annotationExpr.getName().getIdentifier().contains("XmlDocument")) {
                                        XmlDocument xmlDocument = new XmlDocument();

                                        for (Node childNode : annotationExpr.getChildNodes()) {

                                            if (childNode instanceof MemberValuePair) {

                                                MemberValuePair memberValuePair = (MemberValuePair) childNode;
                                                String valueString = memberValuePair.getValue().toString();
                                                switch (memberValuePair.getNameAsString()) {
                                                    case "schemaVersion":
                                                        xmlDocument.setSchemaVersion(valueString);
                                                        break;
                                                    case "schemaNamespace":
                                                        xmlDocument.setSchemaNamespace(valueString);
                                                        break;
                                                    case "encoding":
                                                        xmlDocument.setEncoding(valueString);
                                                        break;
                                                    case "xmlVersion":
                                                        xmlDocument.setXmlVersion(valueString);
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }
                                        }

                                        return xmlDocument;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (ifStmt.getElseStmt().isPresent() && ifStmt.getElseStmt().get().isBlockStmt()) {
            for (Statement blockElseStmt : ifStmt.getElseStmt().get().asBlockStmt().getStatements()) {
                if (blockElseStmt.isExpressionStmt()) {
                    ExpressionStmt expressionStmt = (ExpressionStmt) blockElseStmt;
                    Expression expression = expressionStmt.getExpression();
                    if (expression.isVariableDeclarationExpr()) {
                        VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expression;
                        for (AnnotationExpr annotationExpr : variableDeclarationExpr.getAnnotations()) {
                            Optional<Position> positionOptional = annotationExpr.getBegin();
                            if (positionOptional.isPresent()) {
                                Position position = positionOptional.get();
                                if (position.line == lineNumber) {
                                    if (annotationExpr.getName().getIdentifier().contains("XmlDocument")) {
                                        XmlDocument xmlDocument = new XmlDocument();

                                        for (Node childNode : annotationExpr.getChildNodes()) {

                                            if (childNode instanceof MemberValuePair) {

                                                MemberValuePair memberValuePair = (MemberValuePair) childNode;
                                                String valueString = memberValuePair.getValue().toString();
                                                switch (memberValuePair.getNameAsString()) {
                                                    case "schemaVersion":
                                                        xmlDocument.setSchemaVersion(valueString);
                                                        break;
                                                    case "schemaNamespace":
                                                        xmlDocument.setSchemaNamespace(valueString);
                                                        break;
                                                    case "encoding":
                                                        xmlDocument.setEncoding(valueString);
                                                        break;
                                                    case "xmlVersion":
                                                        xmlDocument.setXmlVersion(valueString);
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }
                                        }

                                        return xmlDocument;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static List<Pair<Unit, Local>> retrieveUnitOfInterestInvokeArg(SootMethod currentMethod, SootMethod methodOfInterest, int paramNumb,
                                                                           MethodInvocationDataflowNode dataflowNode) {

        List<Pair<Unit, Local>> invokeArgPairs = new ArrayList<>();

        Body b = methodOfInterest.retrieveActiveBody();

        UnitGraph graph = new ExceptionalUnitGraph(b);
        Iterator gIt = graph.iterator();
        while (gIt.hasNext()) {
            Unit u = (Unit) gIt.next();
            if (u instanceof AssignStmt) {

                Value rightValue = ((AssignStmt) u).getRightOp();

                if (rightValue instanceof VirtualInvokeExpr) {

                } else if (rightValue instanceof SpecialInvokeExpr) {

                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) rightValue;
                    if (specialInvokeExpr.getMethod().equals(currentMethod)) {

                        if (dataflowNode != null) {
                            dataflowNode.setLineNumber(getLineNumber(u));
                        }
                        invokeArgPairs.add(new Pair<>(u, (Local) specialInvokeExpr.getArg(paramNumb)));
                    }

                } else if (rightValue instanceof StaticInvokeExpr) {

                }

            } else if (u instanceof InvokeStmt) {

                InvokeExpr invokeExpr = ((InvokeStmt) u).getInvokeExpr();

                if (invokeExpr instanceof StaticInvokeExpr) {
                    StaticInvokeExpr staticInvokeExpr = (StaticInvokeExpr) invokeExpr;
                    if (staticInvokeExpr.getMethod().equals(currentMethod)) {
                        if (dataflowNode != null) {
                            dataflowNode.setLineNumber(getLineNumber(u));
                        }
                        invokeArgPairs.add(new Pair<>(u, (Local) staticInvokeExpr.getArg(paramNumb)));
                    }
                }
            } else if (u instanceof ReturnStmt) {
                ReturnStmt returnStmt = (ReturnStmt) u;
                Value returnValue = returnStmt.getOp();

                if (returnValue instanceof SpecialInvokeExpr) {
                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) returnValue;
                    if (specialInvokeExpr.getMethod().equals(currentMethod)) {
                        if (dataflowNode != null) {
                            dataflowNode.setLineNumber(getLineNumber(u));
                        }
                        invokeArgPairs.add(new Pair<>(u, (Local) specialInvokeExpr.getArg(paramNumb)));
                    }
                }
            }
        }

        return invokeArgPairs;
    }

    private static int getArgNumb(Local localOfInterest, Value value) {
        InvokeExpr invokeExpr = (InvokeExpr) value;
        return analyzeExprForInterestingInvoke(localOfInterest, invokeExpr);
    }

    private static int analyzeExprForInterestingInvoke(Local localOfInterest, InvokeExpr u) {

        InvokeExpr invokeExpr = u;
        for (int i = 0; i < invokeExpr.getArgs().size(); i++) {
            Value arg = invokeExpr.getArg(i);
            if (arg.equals(localOfInterest)) {
                return i;
            }
        }

        return -1;
    }

    private static int getLineNumber(Unit u) {
        int lineNumber = -1;
        try {
            Tag tag = u.getTag("LineNumberTag");
            if (tag instanceof LineNumberTag) {
                lineNumber = ((LineNumberTag) tag).getLineNumber();
            }
        } catch (Exception exc) {
            throw new RuntimeException("No line number tag found... make sure --keep-line-number option is" +
                    " specified in soot settings.");
        }
        return lineNumber;
    }

    private static String convertCallTraceSigToSootSig(String callTraceMethodName, String callTraceMethodDesc) {

        StringBuilder sootSig = new StringBuilder();

        if (callTraceMethodDesc.endsWith("V")) {
            sootSig.append("void ");
            callTraceMethodDesc = callTraceMethodDesc.substring(0, callTraceMethodDesc.length() - 1);
        } else if (callTraceMethodDesc.contains(")L")) {
            sootSig.append((callTraceMethodDesc.substring(callTraceMethodDesc.indexOf(")L") + 2)).replace("/", ".")
                    .replace(";", "") + " ");
            callTraceMethodDesc = callTraceMethodDesc.substring(0, callTraceMethodDesc.indexOf(")L") + 1);
        } else if (callTraceMethodDesc.contains(")[B")) {
            sootSig.append("byte[] ");
            callTraceMethodDesc = callTraceMethodDesc.substring(0, callTraceMethodDesc.length() - 2);
        } else if (callTraceMethodDesc.endsWith("I")) {
            sootSig.append("int ");
            callTraceMethodDesc = callTraceMethodDesc.substring(0, callTraceMethodDesc.length() - 1);
        }

        sootSig.append(callTraceMethodName);

        List<String> params = new ArrayList<>();
        int i = 1;
        while (true) {
            char auger = callTraceMethodDesc.charAt(i);
            if (auger == ')') {
                break;
            }
            switch (auger) {
                case '[':

                    switch (callTraceMethodDesc.charAt(i + 1)) {
                        case 'B':
                            params.add("byte[]");
                            i += 2;
                            break;
                        case 'I':
                            params.add("int[]");
                            i += 2;
                            break;
                        case 'L':
                            String temp = callTraceMethodDesc.substring(i + 1);
                            int EOO = temp.indexOf(";");
                            temp = temp.substring(1, EOO);
                            temp = temp.replace("/", ".");
                            params.add(temp + "[]");
                            i += (temp.length() + 3);
                        default:
                            break;
                    }
                    break;
                case 'L':

                    String temp = callTraceMethodDesc.substring(i);
                    int EOO = temp.indexOf(";");
                    temp = temp.substring(1, EOO);
                    temp = temp.replace("/", ".");
                    params.add(temp);
                    i += (temp.length() + 2);
                    break;
                default:
                    switch (auger) {
                        case 'B':
                            params.add("byte");
                            break;
                        case 'I':
                            params.add("int");
                            break;
                        case 'Z':
                            params.add("bool");
                            break;
                        default:
                            break;
                    }
                    i++;
                    break;
            }
        }

        sootSig.append("(");
        for (String param : params) {
            sootSig.append(param);
            sootSig.append(",");
        }

        int errantComma = sootSig.lastIndexOf(",");
        if (errantComma != -1) {
            sootSig.deleteCharAt(errantComma);
        }
        sootSig.append(")");

        return sootSig.toString();
    }

    public static List<Stack<String>> parseCallTraceStack(String pathToCallTraceFile) throws IOException {

        List<Stack<String>> callTraceStackList = new ArrayList<>();

        File callTraceFile = new File(pathToCallTraceFile);
        if (!callTraceFile.exists()) {
            throw new RuntimeException("CALL TRACE FILE NOT FOUND");
        }

        String callTraceStringTemp = FileUtils.readFileToString(callTraceFile);
        callTraceStringTemp = callTraceStringTemp.replace("\n\n\n", "\n\n");
        String[] callTraceStringArr = callTraceStringTemp.split("\n");
        int i = 0;
        while (true) {

            Stack<String> callTraceStack = new Stack<>();
            String callTraceString = callTraceStringArr[i];

            int auger = callTraceString.indexOf("invocations of");
            if (auger == -1) {

                auger = callTraceString.indexOf("occurrences of");
                if (auger == -1) {
                    break;
                } else {
                    i++;
                }
            } else {
                i++;
                callTraceString = callTraceStringArr[i];
                auger = callTraceString.indexOf("occurrences of");
                if (auger == -1) {
                    break;
                } else {
                    i++;
                }
            }

            callTraceString = callTraceStringArr[i];

            while (true) {
                int auger2 = callTraceString.indexOf("METHOD_BEGIN    ");
                if (auger2 == -1) {
                    break;
                }
                callTraceString = callTraceString.substring(auger2 + 16);
                String methodCall = callTraceString;
                callTraceStack.push(methodCall.replace("\r", ""));
                i++;
                if (i >= callTraceStringArr.length) {
                    break;
                }
                callTraceString = callTraceStringArr[i];
            }
            callTraceStackList.add(callTraceStack);
            i++;
            if (i >= callTraceStringArr.length) {
                break;
            }
        }
        return callTraceStackList;
    }

    public Collection<String> getOutgoingFlowIndicators() {
        return outgoingFlowIndicators;
    }

    public Collection<String> getIncomingFlowIndicators() {
        return incomingFlowIndicators;
    }

    public Set<Pair<String, DataType>> getClassNamesToNewData() {
        return classNamesToNewData;
    }

    private class ClassData {
        private UnitGraph graph;
        private Unit currentUnit;
        private int lineNumber;
        private String currentClassName;
        private String currentMethodName;

        public ClassData(UnitGraph graph, Unit currentUnit) {
            this.graph = graph;
            this.currentUnit = currentUnit;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getCurrentClassName() {
            return currentClassName;
        }

        public String getCurrentMethodName() {
            return currentMethodName;
        }

        public ClassData invoke() {
            lineNumber = DataflowAnalyzerPlatform.getLineNumber(currentUnit);
            currentClassName = graph.getBody().getMethod().getDeclaringClass().getName();
            currentMethodName = graph.getBody().getMethod().getName();
            return this;
        }
    }
}
