package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.ValueCriterionType;
import com.securboration.immortals.ontology.metrics.Metric;
import com.securboration.immortals.ontology.metrics.VersioningMetric;

/**
 * A criterion based on a measured value
 * 
 * @author jstaples
 *
 */
public class MeasurementCriterion extends CriterionStatement {

    /**
     * A criterion for a metric
     */
    private ValueCriterionType criterion;
    
    /**
     * A metric instance to which the criterion applies
     */
    private Metric measuredMetricValue;
    
    private VersioningMetric versioningMetric;

    
    public ValueCriterionType getCriterion() {
        return criterion;
    }

    
    public void setCriterion(ValueCriterionType criterion) {
        this.criterion = criterion;
    }


    
    public Metric getMeasuredMetricValue() {
        return measuredMetricValue;
    }


    
    public void setMeasuredMetricValue(Metric measuredMetricValue) {
        this.measuredMetricValue = measuredMetricValue;
    }


    public VersioningMetric getVersioningMetric() {
        return versioningMetric;
    }

    public void setVersioningMetric(VersioningMetric versioningMetric) {
        this.versioningMetric = versioningMetric;
    }
}
