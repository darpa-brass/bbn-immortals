package com.securboration.immortals.service.eos.api.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojo backing an evaluation report
 * 
 * @author jstaples
 *
 */
public class EvaluationReport {
    
    private EvaluationRunStatus evaluationStatus;
    
    private final List<EvaluationMetricCategory> evaluationMetricCategories = new ArrayList<>();
    
    private EvaluationStatusReport evaluationDetails;

    public EvaluationReport() {
        super();
    }

    
    public EvaluationRunStatus getEvaluationStatus() {
        return evaluationStatus;
    }

    
    public void setEvaluationStatus(EvaluationRunStatus evaluationStatus) {
        this.evaluationStatus = evaluationStatus;
    }


    
    public List<EvaluationMetricCategory> getCategories() {
        return evaluationMetricCategories;
    }


    
    public EvaluationStatusReport getEvaluationDetails() {
        return evaluationDetails;
    }


    
    public void setEvaluationDetails(EvaluationStatusReport evaluationDetails) {
        this.evaluationDetails = evaluationDetails;
    }
    
    
    

}
