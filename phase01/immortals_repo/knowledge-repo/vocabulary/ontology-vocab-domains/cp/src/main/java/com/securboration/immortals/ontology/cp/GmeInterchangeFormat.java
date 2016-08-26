package com.securboration.immortals.ontology.cp;

import com.securboration.immortals.ontology.core.Resource;

public class GmeInterchangeFormat {
    
    /**
     * Describes the functionalities being performed by the software (ie, its
     * intent with regard to functionality)
     */
    private FunctionalitySpec[] functionalitySpec;
    
    /**
     * Describes the mission objectives of the software (ie, its intent with 
     * regard to the mission)
     */
    private MissionSpec[] missionSpec;
    
    /**
     * Describes the sea of resources available for performing that intent
     */
    private Resource[] availableResources;

    
    public FunctionalitySpec[] getFunctionalitySpec() {
        return functionalitySpec;
    }

    
    public void setFunctionalitySpec(FunctionalitySpec[] functionalitySpec) {
        this.functionalitySpec = functionalitySpec;
    }

    
    public Resource[] getAvailableResources() {
        return availableResources;
    }

    
    public void setAvailableResources(Resource[] availableResources) {
        this.availableResources = availableResources;
    }


    
    public MissionSpec[] getMissionSpec() {
        return missionSpec;
    }


    
    public void setMissionSpec(MissionSpec[] missionSpec) {
        this.missionSpec = missionSpec;
    }

}
