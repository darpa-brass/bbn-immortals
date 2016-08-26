package com.securboration.immortals.deployment.pojos;

/**
 * Abstraction of an object instance
 * 
 * @author jstaples
 *
 */
public class ObjectInstance extends FieldValueContainer implements Copyable<ObjectInstance>{
    
    /**
     * The type of object being instantiated
     */
    private TypeAbstraction instanceType;
    
    /**
     * The parents of this type should contain their own explicit instance 
     * values.  E.g., if the parent class contains a field called "a," the
     * value of "a" won't show up in this class--rather, it will show up in the 
     * parent's field values.
     */
    private ObjectInstance instanceParent;

    public TypeAbstraction getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(TypeAbstraction instanceType) {
        this.instanceType = instanceType;
    }

    public ObjectInstance getInstanceParent() {
        return instanceParent;
    }

    public void setInstanceParent(ObjectInstance instanceParents) {
        this.instanceParent = instanceParents;
    }

    @Override
    public ObjectInstance copy() {
        ObjectInstance copy = new ObjectInstance();

        copy.setName(this.getName());
        copy.setUuid(this.getUuid());
        copy.setComments(this.getComments());
        copy.instanceType = instanceType;

        if (instanceParent != null)
            copy.instanceParent = instanceParent.copy();

        FieldValue baseFieldValues[] = this.getFieldValues();
        if (baseFieldValues != null)
        {
            FieldValue fieldValues[] = new FieldValue[baseFieldValues.length];
            for (int i = 0; i < baseFieldValues.length; i++)
                fieldValues[i] = baseFieldValues[i].copy();

            copy.setFieldValues(fieldValues);
        }

        return copy;
    }
}
