package com.securboration.immortals.ontology.cp;

import com.securboration.immortals.ontology.core.Resource;

public class GmeInterchangeFormat {
    
    /**
     * Identifies a GME session
     */
    private String sessionIdentifier;
    
    /**
     * A human readable description of this interchange document
     */
    private String humanReadableDescription;
    
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


    
    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }


    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }


    
    public String getSessionIdentifier() {
        return sessionIdentifier;
    }


    
    public void setSessionIdentifier(String sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
    }

}
