package com.securboration.immortals.ontology.property.impact;

/**
 * Describes an impact on resources, properties, or other concepts.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Describes an impact on resources, properties, or other concepts. " +
    " @author jstaples ")
public class ImpactStatement {
    
    /**
     * A human readable description of the impact 
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A human readable description of the impact")
    private String humanReadableDescription;

    
    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }

    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }

}
