package com.securboration.immortals.service.eos.adapt;

import com.securboration.immortals.service.eos.api.types.EvaluationRunConfiguration;

public interface IAdaptationModule {
    
    public IAdaptationContext createAdaptationContext(
            EvaluationRunConfiguration evaluationConfiguration
            );
}
