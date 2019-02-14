package com.securboration.immortals.service.eos.api.types;

import java.util.ArrayList;
import java.util.List;

public class EvaluationRunConfiguration extends EosType {
    
    private byte[] codePackageZipped;
    
    private final List<EvaluationRunCommand> evaluationCommands = new ArrayList<>();
    
    private String pathToResultRelativeToExtractedDir;
    
    public EvaluationRunConfiguration(){}
    
    public EvaluationRunConfiguration(String s){
        super(s);
    }
    
    public byte[] getCodePackageZipped() {
        return codePackageZipped;
    }

    
    public void setCodePackageZipped(byte[] codePackageZipped) {
        this.codePackageZipped = codePackageZipped;
    }

    
    public List<EvaluationRunCommand> getEvaluationCommands() {
        return evaluationCommands;
    }

    
    public String getPathToResultRelativeToExtractedDir() {
        return pathToResultRelativeToExtractedDir;
    }

    
    public void setPathToResultRelativeToExtractedDir(
            String pathToResultRelativeToExtractedDir) {
        this.pathToResultRelativeToExtractedDir =
            pathToResultRelativeToExtractedDir;
    }
    
    

}
