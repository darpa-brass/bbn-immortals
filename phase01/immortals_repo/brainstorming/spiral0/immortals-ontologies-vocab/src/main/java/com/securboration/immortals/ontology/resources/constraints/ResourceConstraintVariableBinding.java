package com.securboration.immortals.ontology.resources.constraints;

import com.securboration.immortals.ontology.resources.PlatformResource;

/**
 * A variable binding in a constraint expression
 * @author Securboration
 *
 */
public class ResourceConstraintVariableBinding {
    
    /**
     * The name of the variable
     */
    private String variableName;
    
    /**
     * The resource being constrained
     */
    private PlatformResource resourceVariableRepresents;

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public PlatformResource getResourceVariableRepresents() {
        return resourceVariableRepresents;
    }

    public void setResourceVariableRepresents(
            PlatformResource resourceVariableRepresents) {
        this.resourceVariableRepresents = resourceVariableRepresents;
    }
    
}
