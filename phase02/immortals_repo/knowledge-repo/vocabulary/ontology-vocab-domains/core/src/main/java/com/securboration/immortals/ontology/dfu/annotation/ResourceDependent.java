package com.securboration.immortals.ontology.dfu.annotation;

import com.securboration.immortals.ontology.pojos.markup.PojoProperty;

@PojoProperty
public interface ResourceDependent {
    
    /**
     * The resources upon which something is dependent (referenced by URI)
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
            "The resources upon which something is dependent (referenced by " +
            "URI)")
    public String[] getResourceDependencyUris();

}
