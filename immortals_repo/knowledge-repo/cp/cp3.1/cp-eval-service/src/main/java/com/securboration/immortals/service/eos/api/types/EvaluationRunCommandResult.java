package com.securboration.immortals.service.eos.api.types;

public class EvaluationRunCommandResult extends EosType {
    
    private EvaluationRunCommand command;
    
    private Integer returnValue;
    
    private String stdoutPath;
    private String stderrPath;

    public EvaluationRunCommandResult(){
        
    }
    
    public EvaluationRunCommandResult(String s){
        super(s);
    }
    
    public EvaluationRunCommandResult(EvaluationRunCommand command, int returnValue) {
        super();
        this.command = command;
        this.returnValue = returnValue;
    }

    
    public EvaluationRunCommand getCommand() {
        return command;
    }

    
    public void setCommand(EvaluationRunCommand command) {
        this.command = command;
    }

    
    public Integer getReturnValue() {
        return returnValue;
    }

    
    public void setReturnValue(Integer returnValue) {
        this.returnValue = returnValue;
    }

    
    public String getStdoutPath() {
        return stdoutPath;
    }

    
    public void setStdoutPath(String stdoutPath) {
        this.stdoutPath = stdoutPath;
    }

    
    public String getStderrPath() {
        return stderrPath;
    }

    
    public void setStderrPath(String stderrPath) {
        this.stderrPath = stderrPath;
    }

}
