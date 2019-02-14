package com.securboration.immortals.ontology.ordering;

/**
 * Describes the logical ordering of elements by explicitly asserting 
 * before/after relationships between them
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Describes the logical ordering of elements by explicitly asserting " +
    " before/after relationships between them  @author jstaples ")
public class LogicalOrderingMechanism extends OrderingMechanism {
    
    /**
     * The things that come before
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The things that come before")
    private Class<?>[] comesBefore;
    
    /**
     * The things that come after
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The things that come after")
    private Class<?>[] comesAfter;
    
    public Class<?>[] getComesBefore() {
        return comesBefore;
    }
    
    public void setComesBefore(Class<?>[] comesBefore) {
        this.comesBefore = comesBefore;
    }
    
    public Class<?>[] getComesAfter() {
        return comesAfter;
    }
    
    public void setComesAfter(Class<?>[] comesAfter) {
        this.comesAfter = comesAfter;
    }

}
