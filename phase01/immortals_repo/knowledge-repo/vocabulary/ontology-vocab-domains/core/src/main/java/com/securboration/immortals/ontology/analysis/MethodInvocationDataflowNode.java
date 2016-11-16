package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A call to a method"
        )
    )
public class MethodInvocationDataflowNode extends IntraProcessDataflowNode {
    
    private String javaClassName;
    private String javaMethodName;
    
    public String getJavaClassName() {
        return javaClassName;
    }
    
    public void setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
    }
    
    public String getJavaMethodName() {
        return javaMethodName;
    }
    
    public void setJavaMethodName(String javaMethodName) {
        this.javaMethodName = javaMethodName;
    }
    
}
