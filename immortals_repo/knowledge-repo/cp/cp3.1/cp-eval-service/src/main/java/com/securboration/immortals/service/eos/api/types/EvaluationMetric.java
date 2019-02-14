package com.securboration.immortals.service.eos.api.types;

/**
 * A metric generated during evaluation
 * 
 * @author jstaples
 *
 */
public abstract class EvaluationMetric {
    
    private String metricType;
    private String metricDesc;
    
    
    public EvaluationMetric(){
        
    }
    
    public EvaluationMetric(String metricType, String metricDesc) {
        super();
        this.metricDesc = metricDesc;
        this.metricType = metricType;
    }

    
    public String getMetricDesc() {
        return metricDesc;
    }

    
    public void setMetricDesc(String metricDesc) {
        this.metricDesc = metricDesc;
    }

    
    public String getMetricType() {
        return metricType;
    }

    
    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }
    
    

}
