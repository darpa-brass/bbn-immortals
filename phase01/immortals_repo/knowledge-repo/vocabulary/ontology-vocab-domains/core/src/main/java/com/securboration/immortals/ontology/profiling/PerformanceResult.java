package com.securboration.immortals.ontology.profiling;

public class PerformanceResult {

    private TestPlatformInfo testPlatform;
    
    private MetricValue[] metricValues;

    
    public TestPlatformInfo getTestPlatform() {
        return testPlatform;
    }

    
    public void setTestPlatform(TestPlatformInfo testPlatform) {
        this.testPlatform = testPlatform;
    }

    
    public MetricValue[] getMetricValues() {
        return metricValues;
    }

    
    public void setMetricValues(MetricValue[] metricValues) {
        this.metricValues = metricValues;
    }
    
}
