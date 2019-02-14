package com.securboration.immortals.ontology.frame;

import com.securboration.immortals.ontology.analysis.DataflowAnalysisFrame;
import com.securboration.immortals.ontology.constraint.DataSanitizationViolation;

public class AnalysisFrameViolation {

    private DataflowAnalysisFrame analysisFrame;

    private DataSanitizationViolation sanitizationViolation;

    public DataflowAnalysisFrame getAnalysisFrame() {
        return analysisFrame;
    }

    public void setAnalysisFrame(DataflowAnalysisFrame analysisFrame) {
        this.analysisFrame = analysisFrame;
    }

    public DataSanitizationViolation getSanitizationViolation() {
        return sanitizationViolation;
    }

    public void setSanitizationViolation(DataSanitizationViolation sanitizationViolation) {
        this.sanitizationViolation = sanitizationViolation;
    }
}
