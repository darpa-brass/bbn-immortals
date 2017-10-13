package com.securboration.immortals.ontology.equation;

import com.securboration.immortals.ontology.expression.operator.OperatorArithmeticalBinary;

/**
 * 
 * 
 * @author jstaples
 *
 */
public class ArithmeticalEquationNodeBinary extends NumericalEquationNode {
    
    private NumericalEquationNode left;
    
    private Class<? extends OperatorArithmeticalBinary> operator;
    
    private NumericalEquationNode right;
    
}
