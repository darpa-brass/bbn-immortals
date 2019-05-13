package com.securboration.dataflow.analyzer;

import com.securboration.immortals.ontology.analysis.DataflowNode;
import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.Stmt;

import java.util.Map;

public class DataflowAnalysisListener {
    /**
     * Called exactly once for a trace along which analysis is performed
     *
     * @param traceName
     *            a name that uniquely identifies this trace among all others
     *            within an evaluation run
     * @param semanticDataProperties
     *            properties of the data at the trace entrypoint (the semantic
     *            type of data being passed, its version, etc.)
     * @param entrypoint
     *            the entrypoint
     */
    public void beginTrace(
            final String traceName,
            final Map<SemanticProperties,Object> semanticDataProperties,//type, version, etc.
            final ListenerDataflowNode entrypoint
    ) {

        System.out.println("BEGINNING TRACE: " + traceName);

        for (Map.Entry<SemanticProperties, Object> prop : semanticDataProperties.entrySet()) {
            System.out.println(prop.getKey());
            System.out.println(prop.getValue());
        }

        System.out.println(entrypoint.theMethod.getName() + ", " + entrypoint.theClass.getName());
    }

    /**
     * Called multiple times, once per edge in the trace
     *
     * @param semanticDataProperties
     *            properties of the data at this point in the trace
     *            the left trace node
     */
    public void visitTraceNode(
            final Map<SemanticProperties,Object> semanticDataProperties,//type, version, etc.
            final ListenerDataflowNode node
    ) {

        System.out.println("VISITING NODE: " + node.theMethod.getName() + ", " + node.theClass.getName());

        for (Map.Entry<SemanticProperties, Object> prop : semanticDataProperties.entrySet()) {
            System.out.println(prop.getKey());
            System.out.println(prop.getValue());
        }
    }

    /**
     * Called exactly once, at the end of a trace
     */
    public void endTrace() {
        System.out.println("ENDING TRACE");
    }

    public static class ListenerDataflowNode{

        SootClass theClass;
        SootMethod theMethod;
        Stmt theStatement;
        ValueBox theValue;
        DataflowNode semanticNode;

        public ListenerDataflowNode(DataflowNode dataflowNode, SootClass sootClass, SootMethod sootMethod,
                                    Stmt stmt, ValueBox valueBox) {
            semanticNode = dataflowNode;
            theClass = sootClass;
            theMethod = sootMethod;
            theStatement = stmt;
            theValue = valueBox;
        }

    }

    public enum SemanticProperties {

        SEMANTIC_TYPE,
        SCHEMA_VERSION

    }

    public static class ConstraintViolation{
        //TODO
        String id;
        String humanReadableDesc;
        //...
    }

}
