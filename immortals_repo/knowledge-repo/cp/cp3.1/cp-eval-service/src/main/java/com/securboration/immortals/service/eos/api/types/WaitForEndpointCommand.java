package com.securboration.immortals.service.eos.api.types;


public class WaitForEndpointCommand extends EvaluationRunCommand {
    
    private String endpointUrl;
    
    public WaitForEndpointCommand(){
        
    }

    
    public String getEndpointUrl() {
        return endpointUrl;
    }

    
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

}
