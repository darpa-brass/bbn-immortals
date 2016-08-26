package com.securboration.immortals.ontology.resources.constraints;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A range expresion
 * @author Securboration
 *
 */
public class Range {
    
    /**
     * The variable over which the range is defined
     */
    private Resource variable;
    
    /**
     * The range expression
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
