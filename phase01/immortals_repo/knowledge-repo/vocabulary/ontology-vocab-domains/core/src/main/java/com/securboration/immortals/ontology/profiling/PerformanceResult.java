package com.securboration.immortals.ontology.profiling;

/**
 * Describes a set of metric values gathered on some test platform
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Describes a set of metric values gathered on some test platform " +
    " @author jstaples ")
public class PerformanceResult {

    /**
     * The platform on which testing was performed
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The platform on which testing was performed")
    private TestPlatformInfo testPlatform;
    
    /**
     * The values gathered during testing
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The values gathered during testing")
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
