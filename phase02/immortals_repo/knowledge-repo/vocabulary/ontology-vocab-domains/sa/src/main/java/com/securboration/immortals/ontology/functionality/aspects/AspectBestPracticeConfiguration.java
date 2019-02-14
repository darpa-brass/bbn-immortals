package com.securboration.immortals.ontology.functionality.aspects;

import com.securboration.immortals.ontology.functionality.ConfigurationBinding;
import com.securboration.immortals.ontology.functionality.Functionality;

public class AspectBestPracticeConfiguration {
    
    private Class<? extends Functionality> boundFunctionality;

    private ConfigurationBinding[] configurationBindings;
    
    public ConfigurationBinding[] getConfigurationBindings() {
        return configurationBindings;
    }

    public void setConfigurationBindings(ConfigurationBinding[] configurationBindings) {
        this.configurationBindings = configurationBindings;
    }

    public Class<? extends Functionality> getBoundFunctionality() {
        return boundFunctionality;
    }

    public void setBoundFunctionality(Class<? extends Functionality> boundFunctionality) {
        this.boundFunctionality = boundFunctionality;
    }
}
