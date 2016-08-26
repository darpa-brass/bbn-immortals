package com.securboration.immortals.ontology.profiling;


public class MetricValue {

    private MetricType metric;
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
