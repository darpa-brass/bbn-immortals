package com.securboration.immortals.deployment.pojos;

/**
 * Abstraction of a type defined in a GME model.  Analogous to a Java class.
 * 
 * @author jstaples
 *
 */
public class TypeAbstraction extends FieldValueContainer{

    /**
     * Note1: fields are inherited recursively from each parent
     */
    private TypeAbstraction parent;

    public TypeAbstraction getParent() {
        return parent;
    }

    public void setParent(TypeAbstraction parent) {
        this.parent = parent;
    }

    public ObjectInstance makeInstance()
    {
        ObjectInstance instance = new ObjectInstance();
        instance.setUuid(this.getUuid());
        instance.setName(this.getName());
        instance.setComments(this.getComments());
        instance.setInstanceType(this);

        if(parent != null)
            instance.setInstanceParent(parent.makeInstance());

        FieldValue fieldValues[] = getFieldValues();
        if(fieldValues != null)
        {
            FieldValue fieldCopies[] = new FieldValue[fieldValues.length];
            for (int i = 0; i < fieldValues.length; i++)
                fieldCopies[i] = fieldValues[i].copy();

            instance.setFieldValues(fieldCopies);
        }

        return instance;
    }
}
