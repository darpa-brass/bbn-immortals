package com.securboration.immortals.deployment.pojos.values;

import com.securboration.immortals.deployment.pojos.ObjectInstance;
import com.securboration.immortals.deployment.pojos.TypeAbstraction;

/**
 * 
 * A complex value (ie one that contains an instance of an object)
 * 
 * @author jstaples
 *
 */
public class ValueComplex extends Value
{
    /**
     * The type of the field
     */
    private TypeAbstraction type;
    
    /**
     * The value of the field
     */
    private ObjectInstance value;

    private boolean isPointer;

    public TypeAbstraction getType() {
        return type;
    }

    public void setType(TypeAbstraction type) {
        this.type = type;
    }

    public ObjectInstance getValue() {
        return value;
    }

    public void setValue(ObjectInstance value) {
        this.value = value;
    }

    public boolean isPointer()
    {
        return isPointer;
    }

    public void setPointer(boolean pointer)
    {
        isPointer = pointer;
    }

    @Override
    public Value copy()
    {
        ValueComplex copy = new ValueComplex();

        copy.setType(type);
        copy.setValue(value.copy());

        return copy;
    }
}
