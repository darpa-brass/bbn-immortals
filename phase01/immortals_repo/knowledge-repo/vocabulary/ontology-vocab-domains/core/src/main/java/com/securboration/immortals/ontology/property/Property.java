package com.securboration.immortals.ontology.property;

import com.securboration.immortals.ontology.core.TruthConstraint;

/**
 * A property
 * 
 * @author Securboration
 *
 */
public class Property {

    /**
     * Describes the conditions under which the property's truth can be assumed
     */
    private TruthConstraint truthConstraint;

    public TruthConstraint getTruthConstraint() {
        return truthConstraint;
    }

    public void setTruthConstraint(TruthConstraint truthConstraint) {
        this.truthConstraint = truthConstraint;
    }
    
}
