package com.securboration.immortals.ontology;

/**
 * Describes an invocation of a FunctionalityTest-annotated method
 */
public class FunctionalityTestRun {
    
    // Indicates whether the method threw an error during execution or not
    boolean success;

    // Indicates when this test run was began
    String testBeginTime;
    String testEndTime;
    
    CallGraph callGraph;

    public CallGraph getCallGraph() {
        return callGraph;
    }

    public void setCallGraph(CallGraph callGraph) {
        this.callGraph = callGraph;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getTestBeginTime() {
        return testBeginTime;
    }

    public void setTestBeginTime(String testBeginTime) {
        this.testBeginTime = testBeginTime;
    }

    public String getTestEndTime() {
        return testEndTime;
    }

    public void setTestEndTime(String testEndTime) {
        this.testEndTime = testEndTime;
    }
}
