package com.securboration.immortals.ontology.criterion;

/**
 * Models a criterion statement.  E.g., a criterion might be the presence or
 * absence of a specific resource, or of a property, or some other condition.
 * 
 * @author jstaples
 *
 */
public class CriterionStatement {
    
    /**
     * A human readable description of the criterion statement
     */
    private String humanReadableDescription;

    
    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }

    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }

}
