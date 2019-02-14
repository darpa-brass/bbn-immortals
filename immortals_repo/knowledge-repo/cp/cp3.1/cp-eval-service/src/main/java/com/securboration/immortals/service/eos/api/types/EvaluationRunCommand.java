package com.securboration.immortals.service.eos.api.types;


public class EvaluationRunCommand extends EosType {
    
    private String name;
    private String workingDir;
    private String[] commandParts;
    private boolean async = false;
    private boolean workingDirAbsolute = false;
    
    public EvaluationRunCommand(){}
    
    public EvaluationRunCommand(String s){
        super(s);
    }

    public EvaluationRunCommand(
            String name, 
            String workingDirRelativeToExtractedPkgRoot,
            String[] commandParts
            ) {
        super();
        this.name = name;
        this.commandParts = commandParts;
        this.workingDir = workingDirRelativeToExtractedPkgRoot;
    }

    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

    
    public String[] getCommandParts() {
        return commandParts;
    }

    
    public void setCommandParts(String[] commandParts) {
        this.commandParts = commandParts;
    }

    
    public String getWorkingDir() {
        return workingDir;
    }

    
    public void setWorkingDir(
            String workingDirRelativeToExtractedPkgRoot) {
        this.workingDir =
            workingDirRelativeToExtractedPkgRoot;
    }

    
    public boolean isAsync() {
        return async;
    }

    
    public void setAsync(boolean async) {
        this.async = async;
    }

    
    public boolean isWorkingDirAbsolute() {
        return workingDirAbsolute;
    }

    
    public void setWorkingDirAbsolute(boolean workingDirAbsolute) {
        this.workingDirAbsolute = workingDirAbsolute;
    }
    
    
    

}
