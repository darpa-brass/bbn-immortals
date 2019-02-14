package com.securboration.immortals.service.eos.adapt;


public interface IAdaptationContext {
    
    public boolean isAdaptationStrategyApplicable();
    
    public void beforeAdaptation();
    
    public void adapt();
    
    public void afterAdaptation();

}
