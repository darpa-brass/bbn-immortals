package com.securboration.immortals.service.eos.adapt;


public class NoopAdaptationContext implements IAdaptationContext {

    @Override
    public boolean isAdaptationStrategyApplicable() {
        return false;
    }

    @Override
    public void beforeAdaptation() {
        
    }

    @Override
    public void adapt() {
        
    }

    @Override
    public void afterAdaptation() {
        
    }

}
