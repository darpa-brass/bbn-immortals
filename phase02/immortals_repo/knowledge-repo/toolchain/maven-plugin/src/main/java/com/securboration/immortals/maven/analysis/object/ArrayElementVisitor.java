package com.securboration.immortals.maven.analysis.object;

/**
 * An API invoked while walking through an array. See
 * {@link ObjectNode#accept} and {@link ObjectNodeVisitor#visitArrayField}
 * 
 * @author jstaples
 *
 */
public interface ArrayElementVisitor
{
    public void visitArrayElement(
            ObjectNode array,
            final int index,
            ObjectNode elementAtIndex
            );
}