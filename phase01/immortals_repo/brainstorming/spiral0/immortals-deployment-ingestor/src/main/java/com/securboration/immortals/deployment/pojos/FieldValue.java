package com.securboration.immortals.deployment.pojos;

import com.securboration.immortals.deployment.pojos.values.Value;

/**
 * A field instance is a possibly recursive tuple of the form {name, value}
 * 
 * @author jstaples
 *
 */
public class FieldValue implements Copyable<FieldValue> {

    /**
     * The name of the field
     */
    private String name;
    
    /**
     * The value of the field
     */
    private Value value;

    public FieldValue() {}

    public FieldValue(String name, Value value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public FieldValue copy()
    {
        FieldValue copy = new FieldValue();

        copy.setName(name);
        copy.setValue(value.copy());

        return copy;
    }
}
