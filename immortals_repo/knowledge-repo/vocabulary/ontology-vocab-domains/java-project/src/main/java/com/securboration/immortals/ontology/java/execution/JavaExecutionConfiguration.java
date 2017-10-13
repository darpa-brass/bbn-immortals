package com.securboration.immortals.ontology.java.execution;


public class JavaExecutionConfiguration {
    
    private String mainClassName;
    private String[] args;
    private String[] jvmArgs;
    
    public String getMainClassName() {
        return mainClassName;
    }
    
    public void setMainClassName(String mainClassName) {
        this.mainClassName = mainClassName;
    }
    
    public String[] getArgs() {
        return args;
    }
    
    public void setArgs(String[] args) {
        this.args = args;
    }
    
    public String[] getJvmArgs() {
        return jvmArgs;
    }
    
    public void setJvmArgs(String[] jvmArgs) {
        this.jvmArgs = jvmArgs;
    }
    

}
