package com.securboration.immortals.service.eos.api.types;

/**
 * A metric describing the cost of adaptation
 * 
 * @author jstaples
 *
 */
public class EvaluationMetricCostOfAdaptation extends EvaluationMetric {
    
    private String metricValue;
    
    public EvaluationMetricCostOfAdaptation(){
        
    }
    
    public EvaluationMetricCostOfAdaptation(String metricType, String metricDesc,
            String metricValue) {
        super(metricType,metricDesc);
        this.metricValue = metricValue;
    }
    
    public String getMetricValue() {
        return metricValue;
    }

    
    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }
    
    

}
