package com.securboration.immortals.deployment.pojos.values;

import com.securboration.immortals.deployment.pojos.TypeAbstraction;

/**
 * 
 * An array value
 * 
 * @author jstaples
 *
 */
public class ValueArray extends Value {
    
    /**
     * The type of the array
     */
    private TypeAbstraction arrayType;
    
    /**
     * The array values
     */
    private Value[] arrayValues;

    public TypeAbstraction getArrayType() {
        return arrayType;
    }

    public void setArrayType(TypeAbstraction arrayType) {
        this.arrayType = arrayType;
    }

    public Value[] getArrayValues() {
        return arrayValues;
    }

    public void setArrayValues(Value[] arrayValues) {
        this.arrayValues = arrayValues;
    }

    @Override
    public Value copy()
    {
        ValueArray copy = new ValueArray();

        copy.setArrayType(arrayType);

        Value copyValues[] = new Value[arrayValues.length];
        for(int i = 0; i < arrayValues.length; i++)
            copyValues[i] = arrayValues[i].copy();

        copy.setArrayValues(copyValues);

        return copy;
    }
}
