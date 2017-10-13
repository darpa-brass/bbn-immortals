package com.securboration.immortals.ontology.profiling;

/**
 * A measurement represented as a flat string
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A measurement represented as a flat string  @author jstaples ")
public class Value {
    
    /**
     * The flat string representation of the measurement
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The flat string representation of the measurement")
    private String stringValue;

    
    public String getStringValue() {
        return stringValue;
    }

    
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
}
