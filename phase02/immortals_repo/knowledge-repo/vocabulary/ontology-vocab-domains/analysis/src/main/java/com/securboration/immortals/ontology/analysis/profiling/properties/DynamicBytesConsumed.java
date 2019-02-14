package com.securboration.immortals.ontology.analysis.profiling.properties;

/**
 * The number of dynamic instructions executed
 * 
 * @author jstaples
 *
 */
public class DynamicBytesConsumed extends MeasuredProperty {

    private long numberOfBytesConsumed;

    public long getNumberOfBytesConsumed() {
        return numberOfBytesConsumed;
    }

    public void setNumberOfBytesConsumed(long numberOfBytesConsumed) {
        this.numberOfBytesConsumed = numberOfBytesConsumed;
    }

}
