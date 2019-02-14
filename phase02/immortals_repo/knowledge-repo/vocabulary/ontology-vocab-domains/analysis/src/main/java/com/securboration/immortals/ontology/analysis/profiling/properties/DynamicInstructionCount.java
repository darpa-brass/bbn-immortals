package com.securboration.immortals.ontology.analysis.profiling.properties;

/**
 * The number of dynamic instructions observed to have executed
 * 
 * @author jstaples
 *
 */
public class DynamicInstructionCount extends MeasuredProperty {

    private long numberOfDynamicInstructionsExecuted;

    public long getNumberOfDynamicInstructionsExecuted() {
        return numberOfDynamicInstructionsExecuted;
    }

    public void setNumberOfDynamicInstructionsExecuted(
            long numberOfDynamicInstructionsExecuted) {
        this.numberOfDynamicInstructionsExecuted =
            numberOfDynamicInstructionsExecuted;
    }

}
