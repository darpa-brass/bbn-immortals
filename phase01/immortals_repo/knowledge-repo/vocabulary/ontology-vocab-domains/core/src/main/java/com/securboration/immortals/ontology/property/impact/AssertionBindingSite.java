package com.securboration.immortals.ontology.property.impact;

/**
 * Models a site to which an assertion can bind (e.g., an abstract resource or
 * a resource instance)
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Models a site to which an assertion can bind (e.g., an abstract" +
    " resource or a resource instance)  @author jstaples ")
public class AssertionBindingSite {
    
    /**
     * Describes the site
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Describes the site")
    private String humanReadableDescription;

    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }
    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }
    
    
}
