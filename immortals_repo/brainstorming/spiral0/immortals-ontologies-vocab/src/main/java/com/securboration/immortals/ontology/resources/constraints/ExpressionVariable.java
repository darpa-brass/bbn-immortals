package com.securboration.immortals.ontology.resources.constraints;

import com.securboration.immortals.ontology.core.Resource;

/**
 * An expression that contains a single variable
 * 
 * @author Securboration
 *
 */
public class ExpressionVariable extends Expression {
    
    /**
     * A resource variable to use in an expression
     */
    private Resource resource;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
    
}
