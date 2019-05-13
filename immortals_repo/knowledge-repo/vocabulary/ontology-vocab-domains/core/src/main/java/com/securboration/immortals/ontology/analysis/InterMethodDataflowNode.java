package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.annotations.RdfsComment;

import java.util.Objects;

public class InterMethodDataflowNode extends InterProcessDataflowNode {
    
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
    
    @RdfsComment("Line number where method is located in java source code")
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

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof InterMethodDataflowNode)) {
            return false;
        }

        InterMethodDataflowNode methodNode = (InterMethodDataflowNode) o;
        return lineNumber == methodNode.lineNumber &&
                Objects.equals(javaMethodName, methodNode.getJavaMethodName()) &&
                Objects.equals(javaClassName, methodNode.getJavaClassName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, javaMethodName, javaClassName);
    }
}
