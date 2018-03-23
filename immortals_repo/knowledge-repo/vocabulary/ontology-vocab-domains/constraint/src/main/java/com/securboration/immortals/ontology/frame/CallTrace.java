package com.securboration.immortals.ontology.frame;

import com.securboration.immortals.ontology.analysis.DataflowNode;

import java.util.Arrays;

public class CallTrace {

    public CallTrace(CallTrace callTrace) {
        this.setCallSteps(Arrays.copyOf(callTrace.getCallSteps(), callTrace.getCallSteps().length));
        this.setCallStepUUIDs(Arrays.copyOf(callTrace.getCallStepUUIDs(), callTrace.getCallStepUUIDs().length));
    }

    public CallTrace() {
        this.setCallSteps(new DataflowNode[]{});
        this.setCallStepUUIDs(new String[]{});
    }
    
    private DataflowNode[] callSteps;
    
    private String[] callStepUUIDs;

    public DataflowNode[] getCallSteps() {
        return callSteps;
    }

    public void setCallSteps(DataflowNode[] callSteps) {
        this.callSteps = callSteps;
    }

    public String[] getCallStepUUIDs() {
        return callStepUUIDs;
    }

    public void setCallStepUUIDs(String[] callStepUUIDs) {
        this.callStepUUIDs = callStepUUIDs;
    }
    
}
