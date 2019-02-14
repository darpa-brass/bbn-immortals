package com.securboration.immortals.ontology.functionality.aspects;

import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.functionality.ConfigurationBinding;

public class AspectConfigureSolution {
    
    private DfuInstance chosenInstance;
    
    private ConfigurationBinding[] configurationBindings;

    public DfuInstance getChosenInstance() {
        return chosenInstance;
    }

    public void setChosenInstance(DfuInstance chosenInstance) {
        this.chosenInstance = chosenInstance;
    }

    public ConfigurationBinding[] getConfigurationBindings() {
        return configurationBindings;
    }

    public void setConfigurationBindings(ConfigurationBinding[] configurationBindings) {
        this.configurationBindings = configurationBindings;
    }
}
