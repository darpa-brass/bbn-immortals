package com.securboration.immortals.ontology.expression;

import com.securboration.immortals.ontology.equation.NumericalEquationNode;
import com.securboration.immortals.ontology.expression.operator.OperatorArithmeticalComparator;

public class ExpressionNodeNumerical extends BooleanExpressionNode {
    
    private NumericalEquationNode left;
    
    private Class<? extends OperatorArithmeticalComparator> operator;
    
    private NumericalEquationNode right;

}
