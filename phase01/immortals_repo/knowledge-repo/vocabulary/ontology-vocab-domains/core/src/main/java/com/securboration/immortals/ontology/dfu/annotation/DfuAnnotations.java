package com.securboration.immortals.ontology.dfu.annotation;

import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

/**
 * An abstraction that binds what is being done to how it is implemented in
 * bytecode
 * 
 * @author Securboration
 *
 */
@GenerateAnnotation
public class DfuAnnotations {

    private DfuAnnotation[] dfus;

    
    public DfuAnnotation[] getDfus() {
        return dfus;
    }

    
    public void setDfus(DfuAnnotation[] dfus) {
        this.dfus = dfus;
    }

}
