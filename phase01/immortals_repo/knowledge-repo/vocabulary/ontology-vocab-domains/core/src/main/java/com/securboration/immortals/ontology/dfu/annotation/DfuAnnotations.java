package com.securboration.immortals.ontology.dfu.annotation;

import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

/**
 * Wrapper for multiple values
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Wrapper for multiple values  @author jstaples ")
@GenerateAnnotation
public class DfuAnnotations {

    /**
     * The DFUs
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment("The DFUs")
    private DfuAnnotation[] dfus;

    
    public DfuAnnotation[] getDfus() {
        return dfus;
    }

    
    public void setDfus(DfuAnnotation[] dfus) {
        this.dfus = dfus;
    }

}
