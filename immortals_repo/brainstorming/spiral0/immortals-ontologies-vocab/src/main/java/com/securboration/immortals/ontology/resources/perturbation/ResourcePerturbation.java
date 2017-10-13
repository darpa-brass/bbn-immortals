package com.securboration.immortals.ontology.resources.perturbation;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.resources.constraints.Expression;

/**
 * Describes a perturbation to a resource
 * 
 * @author Securboration
 *
 */
public class ResourcePerturbation {
    
    /**
     * The resource being perturbed
     */
    private Resource variable;
    
    /**
     * An expression describing the perturbation
     */
    private Expression rangeExpression;

    public Resource getVariable() {
        return variable;
    }

    public void setVariable(Resource variable) {
        this.variable = variable;
    }

    public Expression getRangeExpression() {
        return rangeExpression;
    }

    public void setRangeExpression(Expression rangeExpression) {
        this.rangeExpression = rangeExpression;
    }

}
