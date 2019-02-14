package com.securboration.immortals.ontology.profiling;

/**
 * An instance of a measurement for some metric type
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An instance of a measurement for some metric type  @author jstaples ")
public class MetricValue {

    /**
     * The type of metric being measured
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The type of metric being measured")
    private MetricType metric;
    
    /**
     * The value measured
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The value measured")
    private Value value;
    
    public MetricType getMetric() {
        return metric;
    }
    
    public void setMetric(MetricType metric) {
        this.metric = metric;
    }
    
    public Value getValue() {
        return value;
    }
    
    public void setValue(Value value) {
        this.value = value;
    }
    
}
