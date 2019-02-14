package com.securboration.immortals.ontology.expression;

import com.securboration.immortals.ontology.expression.operator.OperatorLogicalComparator;

public class ExpressionNodeBinary extends BooleanExpressionNode {
    
    private BooleanExpressionNode left;
    
    private Class<? extends OperatorLogicalComparator> operator;
    
    private BooleanExpressionNode right;

}
