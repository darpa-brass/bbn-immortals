package com.securboration.immortals.service.eos.adapt;

import com.securboration.immortals.service.eos.api.types.EvaluationRunConfiguration;

public class NoopAdaptationModule implements IAdaptationModule {

    @Override
    public IAdaptationContext createAdaptationContext(
            EvaluationRunConfiguration evaluationConfiguration
            ) {
        return new NoopAdaptationContext();
    }

}
