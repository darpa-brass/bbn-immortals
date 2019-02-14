package com.securboration.immortals.service.eos.api.types;

import java.util.ArrayList;
import java.util.List;

/**
 * A category for organizing similarly themed evaluation metrics
 * 
 * @author jstaples
 *
 */
public class EvaluationMetricCategory {
    
    private String categoryDesc;
    private final List<EvaluationMetric> metricsForCategory = new ArrayList<>();
    
    public EvaluationMetricCategory(){
        
    }

    public EvaluationMetricCategory(String categoryDesc) {
        super();
        this.categoryDesc = categoryDesc;
    }

    
    public String getCategoryDesc() {
        return categoryDesc;
    }

    
    public void setCategoryDesc(String categoryDesc) {
        this.categoryDesc = categoryDesc;
    }

    
    public List<EvaluationMetric> getMetricsForCategory() {
        return metricsForCategory;
    }
    
    
    

}
