package com.securboration.immortals.ontology.property;

import com.securboration.immortals.ontology.core.TruthConstraint;

/**
 * A property of something. Broadly can be broken into properties of data,
 * properties of algorithms, and properties of resources.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A property of something. Broadly can be broken into properties of" +
    " data, properties of algorithms, and properties of resources.  @author" +
    " jstaples ")
public class Property {

    /**
     * Describes the conditions under which the property's truth can be assumed
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Describes the conditions under which the property's truth can be" +
        " assumed")
    private TruthConstraint truthConstraint;

    public TruthConstraint getTruthConstraint() {
        return truthConstraint;
    }

    public void setTruthConstraint(TruthConstraint truthConstraint) {
        this.truthConstraint = truthConstraint;
    }
    
}
