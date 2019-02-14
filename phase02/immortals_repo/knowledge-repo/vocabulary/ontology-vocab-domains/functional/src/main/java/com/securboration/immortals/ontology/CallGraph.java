package com.securboration.immortals.ontology;

import com.securboration.immortals.ontology.java.compiler.NamedClasspath;

public class CallGraph {

    StaticCallGraph staticCallGraph;
    DynamicCallGraph dynamicCallGraph;
    NamedClasspath classpath;

    public NamedClasspath getClasspath() {
        return classpath;
    }

     public void setClasspath(NamedClasspath classpath) {
         this.classpath = classpath;
     }

    public StaticCallGraph getStaticCallGraph() {
        return staticCallGraph;
    }
    public void setStaticCallGraph(StaticCallGraph staticCallGraph) {
        this.staticCallGraph = staticCallGraph;
    }

    public DynamicCallGraph getDynamicCallGraph() {
        return dynamicCallGraph;
    }
    public void setDynamicCallGraph(DynamicCallGraph dynamicCallGraph) {
        this.dynamicCallGraph = dynamicCallGraph;
    }
}