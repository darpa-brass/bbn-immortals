package com.securboration.immortals.ontology.resources.constraints;

/**
 * An expression for a constraint on a resource
 * 
 * @author Securboration
 *
 */
public class ResourceConstraintExpression {
    /**
     * Simple surface form expression
     */
    private String expression;

    /**
     * Bindings of variables to variables present in the surface form expression
     */
    private ResourceConstraintVariableBinding[] variables;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public ResourceConstraintVariableBinding[] getVariables() {
        return variables;
    }

    public void setVariables(ResourceConstraintVariableBinding[] variables) {
        this.variables = variables;
    }
}
