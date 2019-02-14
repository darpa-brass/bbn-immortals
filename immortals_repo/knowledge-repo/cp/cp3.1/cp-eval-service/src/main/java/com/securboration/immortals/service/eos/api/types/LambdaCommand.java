package com.securboration.immortals.service.eos.api.types;


public class LambdaCommand extends EvaluationRunCommand {
    private Runnable r;
    public LambdaCommand(){
    }
    
    public Runnable getR() {
        return r;
    }

    
    public void setR(Runnable r) {
        this.r = r;
    }

}
