package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.InvocationCriterionType;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;

/**
 * Models invocation of a DFU lifecycle method as a change driver
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Models invocation of a DFU lifecycle method as a change driver " +
    " @author jstaples ")
public class InvocationCriterion extends CriterionStatement {
    
    /**
     * The type of invocation criterion (e.g., before or after invocation)
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The type of invocation criterion (e.g., before or after invocation)")
    private InvocationCriterionType criterion;
    
    /**
     * The functional aspect invoked
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The functional aspect invoked")
    private Class<? extends FunctionalAspect> invokedAspect;
    
    public InvocationCriterionType getCriterion() {
        return criterion;
    }
    
    public void setCriterion(InvocationCriterionType criterion) {
        this.criterion = criterion;
    }

    
    public Class<? extends FunctionalAspect> getInvokedAspect() {
        return invokedAspect;
    }

    
    public void setInvokedAspect(Class<? extends FunctionalAspect> invokedAspect) {
        this.invokedAspect = invokedAspect;
    }
    
    
}
