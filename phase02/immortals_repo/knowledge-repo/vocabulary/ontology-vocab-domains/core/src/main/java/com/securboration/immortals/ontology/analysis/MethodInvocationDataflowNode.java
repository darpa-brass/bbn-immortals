package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.annotations.RdfsComment;

/**
 * A dataflow node that involves the transfer of information via method
 * invocation within the same process
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A dataflow node that involves the transfer of information via method" +
    " invocation within the same process  @author jstaples ")
public class MethodInvocationDataflowNode extends IntraProcessDataflowNode {
    
    /**
     * The name of the class being invoked. E.g.,
     * com.bbn.ataklite.service.SACommunicationService.java
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The name of the class being invoked. E.g.," +
        " com.bbn.ataklite.service.SACommunicationService.java")
    private String javaClassName;
    
    /**
     * The name of the method being invoked. E.g., handleActionSendImage(String
     * imageFilepath, Location location)
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The name of the method being invoked. E.g.," +
        " handleActionSendImage(String imageFilepath, Location location)")
    private String javaMethodName;

    /**
     * The pointer of the method being invoked
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
            "The pointer of the method being invoked")
    private String javaMethodPointer;
    
    @RdfsComment("The line number where this method invoked in source code")
    private int lineNumber;
    
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
    
    public String getJavaMethodPointer() {
        return javaMethodPointer;
    }

    public void setJavaMethodPointer(String javaMethodPointer) {
        this.javaMethodPointer = javaMethodPointer;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
}
