package com.securboration.dataflow.analyzer;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.resources.Client;
import com.securboration.immortals.ontology.resources.xml.XmlInstance;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import soot.*;
import soot.jimple.*;
import soot.tagkit.AnnotationTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataflowAnalyzerPlatform {

    private final Collection<String> outgoingFlowIndicators = Arrays.asList("java/io/OutputStream write ([B)V");
    private final Collection<String> incomingFlowIndicators = Arrays.asList("java/io/InputStream read ([B)I");

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

    public Set<Stack<DataflowGraphComponent>> processCallTraceStacks(List<Stack<String>> callTraceStacks) {

        Set<Stack<DataflowGraphComponent>> dataflowStacks = new HashSet<>();

        for (Stack<String> callTraceStack : callTraceStacks) {
            String terminalNode = callTraceStack.pop();
            if (this.getOutgoingFlowIndicators().stream().anyMatch(outFlow -> outFlow.equals(terminalNode))) {
                //outgoing stream
                //TODO reverse flow analysis

                String[] callTraceComponents = terminalNode.split(" ");
                String callClassOwner = callTraceComponents[0];
                //TODO currently here, need to parse things like read...
                String callMethodDesc = convertCallTraceSigToSootSig(callTraceComponents[1], callTraceComponents[2]);
                SootClass terminalClass = Scene.v().loadClassAndSupport(callClassOwner.replace("/", "."));
                SootMethod terminalMethod = terminalClass.getMethod(callMethodDesc);

                InterMethodDataflowEdge interProcessEdge = new InterMethodDataflowEdge();
                interProcessEdge.setDataTypeCommunicated(BinaryData.class);

                InterMethodDataflowNode dataflowNode = new InterMethodDataflowNode();
                dataflowNode.setJavaClassName(terminalClass.getName());
                dataflowNode.setJavaMethodName(terminalMethod.getName());
                dataflowNode.setAbstractResourceTemplate(Client.class);

                interProcessEdge.setProducer(dataflowNode);

                MethodInvocationDataflowEdge dataflowEdge = new MethodInvocationDataflowEdge();
                dataflowEdge.setConsumer(dataflowNode);
                dataflowEdge.setDataTypeCommunicated(BinaryData.class);


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
                                            localOfInterest = (Local) value;
                                            break;
                                        }
                                    }

                                    int lineNumber = getLineNumber(u);
                                    dataflowNode.setLineNumber(lineNumber);

                                    // Stack<String> tempCallTrace = cloneTrace(callTraceStack);
                                    // UnitGraph tempGraph = new ExceptionalUnitGraph(b);
                                    //Iterator tempIter = graph.iterator();
                                    //  shiftIter(tempIter, i);

                                    dataflowGraph = reverseFlowAnalysis(graph, localOfInterest,
                                            callTraceStack, u, dataflows);
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
                //TODO currently here, need to parse things like read...
                String callMethodDesc = convertCallTraceSigToSootSig(callTraceComponents[1], callTraceComponents[2]);
                SootClass terminalClass = Scene.v().loadClassAndSupport(callClassOwner.replace("/", "."));
                SootMethod terminalMethod = terminalClass.getMethod(callMethodDesc);

                InterMethodDataflowEdge interProcessEdge = new InterMethodDataflowEdge();
                interProcessEdge.setDataTypeCommunicated(BinaryData.class);

                InterMethodDataflowNode dataflowNode = new InterMethodDataflowNode();
                dataflowNode.setJavaClassName(terminalClass.getName());
                dataflowNode.setJavaMethodName(terminalMethod.getName());
                dataflowNode.setAbstractResourceTemplate(Client.class);
                interProcessEdge.setConsumer(dataflowNode);

                MethodInvocationDataflowEdge dataflowEdge = new MethodInvocationDataflowEdge();
                dataflowEdge.setProducer(dataflowNode);
                dataflowEdge.setDataTypeCommunicated(BinaryData.class);

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
                    int i = 0;
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
                                            localOfInterest = (Local) value;
                                            break;
                                        }
                                    }

                                    int lineNumber = getLineNumber(u);
                                    dataflowNode.setLineNumber(lineNumber);

                                    // Stack<String> tempCallTrace = cloneTrace(callTraceAnalysis.getCallTraceStack());
                                    // UnitGraph tempGraph = new ExceptionalUnitGraph(b);
                                    // Iterator tempIter = graph.iterator();
                                    // shiftIter(tempIter, i);
                                    DataflowGraphComponent dataflowGraphComponent = dataflows.pop();

                                    dataflowGraph = this.forwardFlowAnalysis(graph, localOfInterest,
                                            callTraceStack, u, dataflowGraphComponent);
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
                                                              Unit currentUnit, DataflowGraphComponent edgeGeneric) {

        Stack<DataflowGraphComponent> currentDataflow = new Stack<>();
        currentDataflow.push(edgeGeneric);
        UnitPatchingChain unitPatchingChain = graph.getBody().getUnits();

        while(unitPatchingChain.getSuccOf(currentUnit) != null) {
            currentUnit = unitPatchingChain.getSuccOf(currentUnit);
            if (currentUnit instanceof AssignStmt) {
                Value rightValue = ((AssignStmt) currentUnit).getRightOp();
                Value leftValue = ((AssignStmt) currentUnit).getLeftOp();

                if (rightValue instanceof Local && rightValue.equals(localOfInterest)) {
                    localOfInterest = (Local) leftValue;
                } else if (rightValue instanceof VirtualInvokeExpr) {
                    //TODO need check to see if new local of interest occurs in rest of graph...
                    VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) rightValue;
                    SootMethod invokedMethod = virtualInvokeExpr.getMethod();
                    if (virtualInvokeExpr.getBase() instanceof Local && virtualInvokeExpr.getBase().equals(localOfInterest)) {
                        //TODO might need to branch off of any arguments provided
                        pushVirtualBaseFlowsAndLocalForward(currentUnit, currentDataflow, invokedMethod);
                        localOfInterest = (Local) leftValue;
                    } /* else if (leftValue.equals(localOfInterest)){

                        MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
                        methodInvocationDataflowNode.setJavaMethodName(invokedMethod.getName());
                        methodInvocationDataflowNode.setJavaClassName(invokedMethod.getDeclaringClass().getName());
                        methodInvocationDataflowNode.setLineNumber(getLineNumber(currentUnit));

                        linkProducerForward(currentDataflow, methodInvocationDataflowNode);

                        MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                        methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
                        methodInvocationDataflowEdge.setDataTypeCommunicated(BinaryData.class);

                        currentDataflow.push(methodInvocationDataflowNode);
                        currentDataflow.push(methodInvocationDataflowEdge);

                        String objectOfInterest = getObjectOfInterest(invokedMethod, classNameToSemantics);
                        if (objectOfInterest != null) {
                            // attempting to transform data, end of data flow analysis???
                            methodInvocationDataflowEdge.setDataTypeCommunicated(XmlInstance.class);
                            return currentDataflow;
                        }

                        List<Local> locals = new ArrayList<>();
                        for(Value arg : virtualInvokeExpr.getArgs()) {
                            if (arg instanceof Local) {
                                locals.add((Local) arg);
                            }
                        }

                        if (!locals.isEmpty()) {
                            for (Local local : locals) {
                                Collection<DataflowGraphComponent> newGraph = this.reverseFlowAnalysis(graph, local, callTraceStack, currentUnit, currentDataflow);
                                if (!newGraph.isEmpty()) {
                                    // branch was correct, return
                                    return newGraph;
                                }
                            }
                        }
                        localOfInterest = (Local) virtualInvokeExpr.getBase();
                    }*/
                } else if (rightValue instanceof SpecialInvokeExpr) {
                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) rightValue;
                    SootMethod invokedMethod = specialInvokeExpr.getMethod();

                    Local finalLocalOfInterest1 = localOfInterest;
                    if (specialInvokeExpr.getArgs().stream().anyMatch(arg -> arg.equals(finalLocalOfInterest1))) {

                        MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
                        methodInvocationDataflowNode.setJavaMethodName(invokedMethod.getName());
                        methodInvocationDataflowNode.setJavaClassName(invokedMethod.getDeclaringClass().getName());
                        methodInvocationDataflowNode.setLineNumber(getLineNumber(currentUnit));
                        methodInvocationDataflowNode.setAbstractResourceTemplate(Client.class);

                        linkProducerForward(currentDataflow, methodInvocationDataflowNode);

                        MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                        methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
                        methodInvocationDataflowEdge.setDataTypeCommunicated(BinaryData.class);

                        currentDataflow.push(methodInvocationDataflowNode);
                        currentDataflow.push(methodInvocationDataflowEdge);

                        String objectOfInterest = getObjectOfInterest(invokedMethod, classNameToSemantics);
                        if (objectOfInterest != null) {
                            // attempting to transform data, end of data flow analysis???
                            methodInvocationDataflowEdge.setDataTypeCommunicated(XmlInstance.class);
                            return currentDataflow;
                        }

                        localOfInterest = (Local) leftValue;
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
                        pushVirtualBaseFlowsAndLocalForward(currentUnit, currentDataflow, invokedMethod);
                    } else {
                        Local finalLocalOfInterest = localOfInterest;
                        if (virtualInvokeExpr.getArgs().stream().anyMatch(arg -> arg.equals(finalLocalOfInterest))) {

                            MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
                            methodInvocationDataflowNode.setJavaMethodName(invokedMethod.getName());
                            methodInvocationDataflowNode.setJavaClassName(invokedMethod.getDeclaringClass().getName());
                            methodInvocationDataflowNode.setLineNumber(getLineNumber(currentUnit));
                            methodInvocationDataflowNode.setAbstractResourceTemplate(Client.class);

                            linkProducerForward(currentDataflow, methodInvocationDataflowNode);

                            MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                            methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
                            methodInvocationDataflowEdge.setDataTypeCommunicated(BinaryData.class);

                            currentDataflow.push(methodInvocationDataflowNode);
                            currentDataflow.push(methodInvocationDataflowEdge);

                            String objectOfInterest = getObjectOfInterest(invokedMethod, classNameToSemantics);
                            if (objectOfInterest != null) {
                                // attempting to transform data, end of data flow analysis???
                                methodInvocationDataflowEdge.setDataTypeCommunicated(XmlInstance.class);
                                return currentDataflow;
                            }

                            localOfInterest = (Local) virtualInvokeExpr.getBase();
                        }
                    }
                }
            } else if (currentUnit instanceof ReturnStmt) {

                ReturnStmt returnStmt = (ReturnStmt) currentUnit;
                if (returnStmt.getOp().equals(localOfInterest)) {
                    String nextNode = callTraceStack.pop();
                    String[] callTraceComponents = nextNode.split(" ");
                    String callClassOwner = callTraceComponents[0];
                    //TODO currently here, need to parse things like read...
                    String callMethodDesc = convertCallTraceSigToSootSig(callTraceComponents[1], callTraceComponents[2]);
                    SootClass sClass = Scene.v().loadClassAndSupport(callClassOwner.replace("/", "."));
                    SootMethod sMethod = sClass.getMethod(callMethodDesc);

                    List<Pair<Unit, Local>> unitLocalPairs = retrieveUnitOfInterestInvokeAssign(graph.getBody().getMethod(), sMethod, null);
                    DataflowGraphComponent dataflowGraphComponent = currentDataflow.pop();
                    for (Pair<Unit, Local> unitLocalPair : unitLocalPairs) {
                        Stack<DataflowGraphComponent> dataflows = this.forwardFlowAnalysis(new ExceptionalUnitGraph(sMethod.retrieveActiveBody()), unitLocalPair.getO2(), callTraceStack,
                                unitLocalPair.getO1(), dataflowGraphComponent);

                        if (!dataflows.isEmpty()) {
                            currentDataflow.addAll(dataflows);
                            return currentDataflow;
                        } else {
                            //failed branch
                        }
                    }
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

                        String nextNode = callTraceStack.pop();
                        String[] callTraceComponents = nextNode.split(" ");
                        String callClassOwner = callTraceComponents[0];
                        //TODO currently here, need to parse things like read...
                        String callMethodDesc = convertCallTraceSigToSootSig(callTraceComponents[1], callTraceComponents[2]);
                        SootClass sClass = Scene.v().loadClassAndSupport(callClassOwner.replace("/", "."));
                        SootMethod sMethod = sClass.getMethod(callMethodDesc);

                        if (paramNumb != -1) {
                            List<Pair<Unit, Local>> unitLocalPairs = retrieveUnitOfInterestInvokeArg(graph.getBody().getMethod(), sMethod, paramNumb, null);

                            DataflowGraphComponent dataflowGraphComponent = currentDataflow.pop();
                            for (Pair<Unit, Local> unitLocalPair : unitLocalPairs) {
                                Stack<DataflowGraphComponent> dataflows = this.forwardFlowAnalysis(new ExceptionalUnitGraph(sMethod.retrieveActiveBody()), unitLocalPair.getO2(), callTraceStack,
                                        unitLocalPair.getO1(), dataflowGraphComponent);

                                if (!dataflows.isEmpty()) {
                                    currentDataflow.addAll(dataflows);
                                    return currentDataflow;
                                } else {
                                    //failed branch
                                }
                            }
                        }
                    }
                }
            } else if (currentUnit instanceof ThrowStmt) {
                ThrowStmt throwStmt = (ThrowStmt) currentUnit;
                if (throwStmt.getOp().equals(localOfInterest)) {
                    if (callTraceStack.isEmpty()) {
                        // end of call trace, but no transformation found??? No idea what this means
                    } else {
                        currentDataflow.clear();
                        return currentDataflow;
                    }
                }
            }
        }

        //TODO current stop-gap, need to improve dataflow analysis
        currentDataflow.clear();
        return currentDataflow;
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

    private void pushVirtualBaseFlowsAndLocalForward(Unit currentUnit, Stack<DataflowGraphComponent> currentDataflow, SootMethod invokedMethod) {
        MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
        methodInvocationDataflowNode.setJavaMethodName(invokedMethod.getName());
        methodInvocationDataflowNode.setJavaClassName(invokedMethod.getDeclaringClass().getName());
        methodInvocationDataflowNode.setLineNumber(getLineNumber(currentUnit));
        methodInvocationDataflowNode.setAbstractResourceTemplate(Client.class);

        linkProducerForward(currentDataflow, methodInvocationDataflowNode);

        MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
        methodInvocationDataflowEdge.setProducer(methodInvocationDataflowNode);
        methodInvocationDataflowEdge.setDataTypeCommunicated(BinaryData.class);

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
                                                              Unit currentUnit, Stack<DataflowGraphComponent> currentDataflow) {

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

                        String objectOfInterest = getObjectOfInterest(invokedMethod, classNameToSemantics);
                        if (objectOfInterest != null) {
                            // attempting to transform data, end of data flow analysis???
                            methodInvocationDataflowEdge.setDataTypeCommunicated(XmlInstance.class);
                            return currentDataflow;
                        }

                        List<Local> locals = new ArrayList<>();
                        for(Value arg : virtualInvokeExpr.getArgs()) {
                            if (arg instanceof Local) {
                                locals.add((Local) arg);
                            }
                        }

                        if (!locals.isEmpty()) {
                            for (Local local : locals) {
                                Stack<DataflowGraphComponent> newGraph = this.reverseFlowAnalysis(graph, local, callTraceStack, currentUnit, currentDataflow);
                                if (!newGraph.isEmpty()) {
                                    // branch was correct, return
                                    return newGraph;
                                }
                            }
                        }

                        localOfInterest = (Local) virtualInvokeExpr.getBase();

                    } else {
                        int argNumb = getArgNumb(localOfInterest, virtualInvokeExpr);
                        if (argNumb != -1) {
                            //used by invoke
                            MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
                            methodInvocationDataflowNode.setJavaMethodName(invokedMethod.getName());
                            methodInvocationDataflowNode.setJavaClassName(invokedMethod.getDeclaringClass().getName());
                            methodInvocationDataflowNode.setLineNumber(getLineNumber(currentUnit));
                            methodInvocationDataflowNode.setAbstractResourceTemplate(Client.class);

                            linkProducerReverse(currentDataflow, methodInvocationDataflowNode);

                            MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                            methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                            methodInvocationDataflowEdge.setDataTypeCommunicated(BinaryData.class);

                            localOfInterest = (Local) leftValue;
                        }
                    }

                } else if (rightValue instanceof StaticInvokeExpr) {

                } else if (rightValue instanceof SpecialInvokeExpr) {

                    if (leftValue.equals(localOfInterest)){
                        SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) rightValue;
                        SootMethod specialInvokeMethod = specialInvokeExpr.getMethod();
                        //TODO need logic for checking if object of interest is undergoing transformations i.e. is passed to annotated method
                        MethodInvocationDataflowNode methodInvocationDataflowNode = new MethodInvocationDataflowNode();
                        methodInvocationDataflowNode.setJavaMethodName(specialInvokeMethod.getName());
                        methodInvocationDataflowNode.setJavaClassName(specialInvokeMethod.getDeclaringClass().getName());
                        methodInvocationDataflowNode.setLineNumber(getLineNumber(currentUnit));
                        methodInvocationDataflowNode.setAbstractResourceTemplate(Client.class);

                        linkProducerReverse(currentDataflow, methodInvocationDataflowNode);

                        MethodInvocationDataflowEdge methodInvocationDataflowEdge = new MethodInvocationDataflowEdge();
                        methodInvocationDataflowEdge.setConsumer(methodInvocationDataflowNode);
                        methodInvocationDataflowEdge.setDataTypeCommunicated(BinaryData.class);

                        currentDataflow.push(methodInvocationDataflowNode);
                        currentDataflow.push(methodInvocationDataflowEdge);

                        String objectOfInterest = getObjectOfInterest(specialInvokeMethod, classNameToSemantics);
                        if (objectOfInterest != null) {
                            // attempting to transform data, end of data flow analysis???
                            methodInvocationDataflowEdge.setDataTypeCommunicated(XmlInstance.class);
                            return currentDataflow;
                        }

                        List<Local> locals = new ArrayList<>();
                        for(Value arg : specialInvokeExpr.getArgs()) {
                            if (arg instanceof Local) {
                                locals.add((Local) arg);
                            }
                        }

                        if (!locals.isEmpty()) {
                            for (Local local : locals) {
                                Stack<DataflowGraphComponent> newGraph = this.reverseFlowAnalysis(graph, local, callTraceStack, currentUnit, currentDataflow);
                                if (!newGraph.isEmpty()) {
                                    // branch was correct, return
                                    return newGraph;
                                }
                            }
                        }

                        localOfInterest = (Local) specialInvokeExpr.getBase();
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

            } else if (currentUnit instanceof ReturnStmt) {

            } else if (currentUnit instanceof IdentityStmt) {

                IdentityStmt identityStmt = (IdentityStmt) currentUnit;

                if (identityStmt.getLeftOp().equals(localOfInterest)) {
                    //found parameter ref of local of interest

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
                    methodInvocationDataflowNode.setAbstractResourceTemplate(Client.class);

                    linkProducerReverse(currentDataflow, methodInvocationDataflowNode);

                    MethodInvocationDataflowEdge dataflowEdge = new MethodInvocationDataflowEdge();
                    dataflowEdge.setDataTypeCommunicated(BinaryData.class);
                    dataflowEdge.setConsumer(methodInvocationDataflowNode);

                    String nextNode = callTraceStack.pop();
                    String[] callTraceComponents = nextNode.split(" ");
                    String callClassOwner = callTraceComponents[0];
                    //TODO currently here, need to parse things like read...
                    String callMethodDesc = convertCallTraceSigToSootSig(callTraceComponents[1], callTraceComponents[2]);
                    SootClass sClass = Scene.v().loadClassAndSupport(callClassOwner.replace("/", "."));
                    SootMethod sMethod = sClass.getMethod(callMethodDesc);

                    currentDataflow.push(methodInvocationDataflowNode);
                    currentDataflow.push(dataflowEdge);

                    if (paramNumb != -1) {
                        List<Pair<Unit, Local>> unitLocalPairs = retrieveUnitOfInterestInvokeArg(graph.getBody().getMethod(), sMethod, paramNumb, methodInvocationDataflowNode);
                        for (Pair<Unit, Local> unitLocalPair : unitLocalPairs) {

                            //TODO check if this is necessary... not actually cloning graph, just copying references
                            Stack<DataflowGraphComponent> tempGraph = cloneGraph(currentDataflow);
                            Stack<DataflowGraphComponent> dataflows = this.reverseFlowAnalysis(new ExceptionalUnitGraph(sMethod.retrieveActiveBody()), unitLocalPair.getO2(), callTraceStack,
                                    unitLocalPair.getO1(), tempGraph);
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

    private Stack<DataflowGraphComponent> cloneGraph(Stack<DataflowGraphComponent> currentDataflow) {


        Stack<DataflowGraphComponent> tempCallStack = new Stack<>();

        ListIterator<DataflowGraphComponent> stackIter = currentDataflow.listIterator();
        while (stackIter.hasNext()) {
            DataflowGraphComponent graphNode = stackIter.next();
            tempCallStack.push(graphNode);
        }

        return tempCallStack;
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
}
