package com.securboration.immortals.o2t.analysis;

/**
 * An API invoked while walking through the object graph. See
 * {@link ObjectNode#accept}
 * 
 * @author jstaples
 *
 */
public interface ObjectNodeVisitor
{
    public default boolean shouldDiveInto(ObjectNode objectFieldValue){
        return true;
    }
    
    public void visitPrimitiveField(
            ObjectNode primitiveFieldOwner,
            ObjectNode primitiveFieldValue
            );
    
    public void visitObjectField(
            ObjectNode objectFieldOwner,
            ObjectNode objectFieldValue
            );
    
    public ArrayElementVisitor visitArrayField(
            ObjectNode arrayFieldOwner,
            ObjectNode arrayFieldValue
            );
}