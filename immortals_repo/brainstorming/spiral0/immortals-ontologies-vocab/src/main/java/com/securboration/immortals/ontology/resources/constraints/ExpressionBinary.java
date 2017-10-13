package com.securboration.immortals.ontology.resources.constraints;


/**
 * An expression of the form [left OPERATOR right]
 * 
 * @author Securboration
 *
 */
public class ExpressionBinary extends Expression {

    /**
     * The left statement in the [left OPERATOR right] tuple
     */
    private Expression left;
    
    /**
     * The operator in the [left OPERATOR right] tuple
     */
    private Operator operator;
    
    /**
     * The right statement in the [left OPERATOR right] tuple
     */
    private Expression right;

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }
    
}
