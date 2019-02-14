package com.securboration.immortals.ontology.functionality.aspects;

import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.functionality.datatype.DataType;

public class AspectConfigureRequest {
    
    private Class<? extends Functionality> requiredFunctionality;
    
    private DataType[] configurationUnknowns;
    
    private DfuInstance[] candidateImpls;

    public Class<? extends Functionality> getRequiredFunctionality() {
        return requiredFunctionality;
    }
    
    private AspectConfigureSolution minimumConfigurationSolution;

    public void setRequiredFunctionality(Class<? extends Functionality> requiredFunctionality) {
        this.requiredFunctionality = requiredFunctionality;
    }

    public DataType[] getConfigurationUnknowns() {
        return configurationUnknowns;
    }

    public void setConfigurationUnknowns(DataType[] configurationUnknowns) {
        this.configurationUnknowns = configurationUnknowns;
    }

    public DfuInstance[] getCandidateImpls() {
        return candidateImpls;
    }

    public void setCandidateImpls(DfuInstance[] candidateImpls) {
        this.candidateImpls = candidateImpls;
    }

    public AspectConfigureSolution getMinimumConfigurationSolution() {
        return minimumConfigurationSolution;
    }

    public void setMinimumConfigurationSolution(AspectConfigureSolution minimumConfigurationSolution) {
        this.minimumConfigurationSolution = minimumConfigurationSolution;
    }
}
