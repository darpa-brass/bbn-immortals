package com.securboration.immortals.ontology.resources.constraints;

/**
 * A constraint on a resource
 * @author Securboration
 *
 */
public class ResourceConstraint {
    
    /**
     * The constraint expression
     */
    private Expression constraint;

    public Expression getConstraint() {
        return constraint;
    }

    public void setConstraint(Expression constraint) {
        this.constraint = constraint;
    }

}
