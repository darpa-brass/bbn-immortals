package com.securboration.immortals.service.eos.api.types;

import java.util.ArrayList;
import java.util.List;

public class EvaluationStatusReport extends EosType {
    
    private int checkCount;
    private String contextId;
    private EvaluationRunStatus status;
    private long runtime;
    
    private byte[] evaluationReportZip;
    
    private final List<EvaluationRunCommandResult> commandResults = new ArrayList<>();
    
    public EvaluationStatusReport(){}
    
    public EvaluationStatusReport(String s){
        super(s);
    }

    
    public String getContextId() {
        return contextId;
    }

    
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    
    public EvaluationRunStatus getStatus() {
        return status;
    }

    
    public void setStatus(EvaluationRunStatus status) {
        this.status = status;
    }

    
    public long getRuntime() {
        return runtime;
    }

    
    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    
    
    public int getCheckCount() {
        return checkCount;
    }

    
    public void setCheckCount(int checkCount) {
        this.checkCount = checkCount;
    }

    
    public List<EvaluationRunCommandResult> getCommandResults() {
        return commandResults;
    }

    
    public byte[] getEvaluationReportZip() {
        return evaluationReportZip;
    }

    
    public void setEvaluationReportZip(byte[] evaluationReportZip) {
        this.evaluationReportZip = evaluationReportZip;
    };
    
    
    

}
