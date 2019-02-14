package com.securboration.immortals.service.eos.nonapi.types;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.TeeOutputStream;

import com.securboration.immortals.adapt.engine.AdaptationEngine;
import com.securboration.immortals.service.eos.api.types.EvaluationConfiguration;
import com.securboration.immortals.service.eos.api.types.EvaluationRunCommandResult;
import com.securboration.immortals.service.eos.api.types.EvaluationRunConfiguration;
import com.securboration.immortals.service.eos.api.types.EvaluationRunStatus;

public class EvaluationContext {
    
    private final String contextId;
    
    private EvaluationRunStatus currentStatus = EvaluationRunStatus.IN_PROGRESS;
    
    private final long creationTime = System.currentTimeMillis();
    
    private final ByteArrayOutputStream stdout = 
            new ByteArrayOutputStream();
    private final ByteArrayOutputStream stderr = 
            new ByteArrayOutputStream();
    
    private final TeeOutputStream teeStdout = 
            new TeeOutputStream(System.out,stdout);
    private final TeeOutputStream teeStderr = 
            new TeeOutputStream(System.err,stderr);
    
    private final List<EvaluationRunCommandResult> commandResults = new ArrayList<>();
    
    private final List<Process> killAfterCompletion = new ArrayList<>();
    
    private byte[] evaluatedPackageZip;
    
    private AdaptationEngine adaptationEngine;
    
    private final EvaluationRunConfiguration evaluationConfiguration;
    
    private final EvaluationConfiguration highLevelConfiguartion;
    
    private volatile File evaluationWorkingDir;

    
    public EvaluationContext(
            final String contextId,
            final EvaluationRunConfiguration config,
            final EvaluationConfiguration request
            ){
        this.contextId = contextId;
        this.evaluationConfiguration = config;
        this.highLevelConfiguartion = request;
    }
    
    public EvaluationRunStatus getCurrentStatus() {
        return currentStatus;
    }

    
    public void setCurrentStatus(EvaluationRunStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    
    public String getContextId() {
        return contextId;
    }

    
    public long getCreationTime() {
        return creationTime;
    }

    
    public OutputStream getStdout() {
        return teeStdout;
    }

    
    public OutputStream getStderr() {
        return teeStderr;
    }
    
    public byte[] getStdoutDump(){
        return stdout.toByteArray();
    }
    
    public byte[] getStderrDump(){
        return stderr.toByteArray();
    }

    
    public List<EvaluationRunCommandResult> getCommandResults() {
        return commandResults;
    }

    
    public List<Process> getKillAfterCompletion() {
        return killAfterCompletion;
    }

    
    public byte[] getEvaluatedPackageZip() {
        return evaluatedPackageZip;
    }

    
    public void setEvaluatedPackageZip(byte[] evaluatedPackageZip) {
        this.evaluatedPackageZip = evaluatedPackageZip;
    }

    
    public AdaptationEngine getAdaptationEngine() {
        return adaptationEngine;
    }

    
    public void setAdaptationEngine(AdaptationEngine adaptationEngine) {
        this.adaptationEngine = adaptationEngine;
    }

    
    public EvaluationRunConfiguration getEvaluationConfiguration() {
        return evaluationConfiguration;
    }

    
    public File getEvaluationWorkingDir() {
        return evaluationWorkingDir;
    }

    
    public void setEvaluationWorkingDir(File evaluationWorkingDir) {
        this.evaluationWorkingDir = evaluationWorkingDir;
    }

    
    public EvaluationConfiguration getHighLevelConfiguartion() {
        return highLevelConfiguartion;
    }

}
