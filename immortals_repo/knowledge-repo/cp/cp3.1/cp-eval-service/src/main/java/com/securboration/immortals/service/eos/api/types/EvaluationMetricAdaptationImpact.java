package com.securboration.immortals.service.eos.api.types;

/**
 * A qualitative metric describes the impact of adaptation
 * 
 * @author jstaples
 *
 */
public class EvaluationMetricAdaptationImpact extends EvaluationMetric {
    
    private String metricValueBefore;
    private String impactOfAdaptation;
    private String metricValueAfter;
    
    public EvaluationMetricAdaptationImpact(){
        
    }

    public EvaluationMetricAdaptationImpact(String metricType,String metricDesc,
            String metricValueBefore,
            String impactOfAdaptation, String metricValueAfter) {
        super(metricType,metricDesc);
        this.metricValueBefore = metricValueBefore;
        this.impactOfAdaptation = impactOfAdaptation;
        this.metricValueAfter = metricValueAfter;
    }

    
    public String getMetricValueBefore() {
        return metricValueBefore;
    }

    
    public void setMetricValueBefore(String metricValueBefore) {
        this.metricValueBefore = metricValueBefore;
    }

    
    public String getImpactOfAdaptation() {
        return impactOfAdaptation;
    }

    
    public void setImpactOfAdaptation(String impactOfAdaptation) {
        this.impactOfAdaptation = impactOfAdaptation;
    }

    
    public String getMetricValueAfter() {
        return metricValueAfter;
    }

    
    public void setMetricValueAfter(String metricValueAfter) {
        this.metricValueAfter = metricValueAfter;
    }
    

}
