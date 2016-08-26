package com.securboration.immortals.deployment.pojos;

import java.util.Arrays;

/**
 * Something that contains fields.  This can be either a class or an instance 
 * of a class.
 * 
 * @author jstaples
 *
 */
public abstract class FieldValueContainer extends ModelDerivedArtifact
{
    public FieldValueContainer()
    {
        fieldValues = new FieldValue[0];
    }

    /**
     * The values of the fields
     */
    private FieldValue[] fieldValues;

    public FieldValue[] getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(FieldValue[] instanceFieldValues) {
        this.fieldValues = instanceFieldValues;
    }

    public void addFieldValue(FieldValue field)
    {
        fieldValues = Arrays.copyOf(fieldValues, fieldValues.length + 1);
        fieldValues[fieldValues.length - 1] = field;
    }
}
