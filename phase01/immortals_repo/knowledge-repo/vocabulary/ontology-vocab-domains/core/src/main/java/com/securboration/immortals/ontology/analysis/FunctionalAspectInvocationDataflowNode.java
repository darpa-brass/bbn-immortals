package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;

/**
 * A dataflow node involving the invocation of a DFU's functional aspect
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A dataflow node involving the invocation of a DFU's functional aspect " +
    " @author jstaples ")
public class FunctionalAspectInvocationDataflowNode extends MethodInvocationDataflowNode {
    
    /**
     * The aspect implemented by the DFU that is called in the flow
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The aspect implemented by the DFU that is called in the flow")
    private FunctionalAspect aspectImplemented;

    
    public FunctionalAspect getAspectImplemented() {
        return aspectImplemented;
    }
    
    public void setAspectImplemented(FunctionalAspect aspectImplemented) {
        this.aspectImplemented = aspectImplemented;
    }
    
}
