package com.securboration.immortals.ontology.resources.constraints;

/**
 * A binding of an operator to a single expression
 * 
 * @author Securboration
 *
 */
public class ExpressionUnary extends Expression {

    /**
     * the operator to apply
     */
    private Operator operator;
    
    /**
     * The expression to evaluate
     */
    private Expression expression;

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }
    
}
