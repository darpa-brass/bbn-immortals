package com.securboration.immortals.ontology.cp2;

import com.securboration.immortals.ontology.android.AndroidPlatform;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.metrics.MeasurementType;
import com.securboration.immortals.ontology.metrics.Metric;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.resources.network.NetworkConnection;
import com.securboration.immortals.ontology.server.ServerPlatform;

@Ignore
public class Measurements {
    
    private static void fillIn(
            Metric m,
            MeasurementType t,
            final String unit,
            final String measurement,
            final Class<? extends Resource> applicableResourceType
            ){
        m.setUnit(unit);
        m.setValue(measurement);
        m.setMeasurementType(t);
        m.setApplicableResourceType(applicableResourceType);
    }

    @ConceptInstance
    public static class ExcessiveBandwidthMeasurement extends Metric{
        public ExcessiveBandwidthMeasurement(){
            fillIn(
                this,
                MeasurementTypes.BANDWIDTH_MEASUREMENT,
                "kb/s",
                "45",
                NetworkConnection.class
                );
        }
    }
    
    @ConceptInstance
    public static class AcceptableBandwidthMeasurement extends Metric{
        public AcceptableBandwidthMeasurement(){
            fillIn(
                this,
                MeasurementTypes.BANDWIDTH_MEASUREMENT,
                "kb/s",
                "0.5",
                NetworkConnection.class
                );
        }
    }
    
    @ConceptInstance
    public static class InsufficientPliReportRateMeasurement1 extends Metric{
        public InsufficientPliReportRateMeasurement1(){
            fillIn(
                this,
                MeasurementTypes.PLI_REPORT_RATE_MEASUREMENT,
                "messages/minute",
                "5",
                AndroidPlatform.class
                );
        }
    }
    
    @ConceptInstance
    public static class InsufficientPliReportRateMeasurement2 extends Metric{
        public InsufficientPliReportRateMeasurement2(){
            fillIn(
                this,
                MeasurementTypes.PLI_REPORT_RATE_MEASUREMENT,
                "messages/minute",
                "0",
                AndroidPlatform.class
                );
        }
    }
    
    @ConceptInstance
    public static class InsufficientPliReportRatemeasurement3 extends Metric{
        public InsufficientPliReportRatemeasurement3(){
            fillIn(
                this,
                MeasurementTypes.PLI_REPORT_RATE_MEASUREMENT,
                "messages/minute",
                "10",
                AndroidPlatform.class
                );
        }
    }
    
    @ConceptInstance
    public static class AcceptablePliReportRateMeasurement extends Metric{
        public AcceptablePliReportRateMeasurement(){
            fillIn(
                this,
                MeasurementTypes.PLI_REPORT_RATE_MEASUREMENT,
                "messages/minute",
                "15",
                AndroidPlatform.class
                );
        }
    }
    
    @ConceptInstance
    public static class AcceptableImageReportRateMeasurement extends Metric{
        public AcceptableImageReportRateMeasurement(){
            fillIn(
                this,
                MeasurementTypes.IMAGE_REPORT_RATE_MEASUREMENT,
                "images/minute",
                "3",
                AndroidPlatform.class
                );
        }
    }
    
    @ConceptInstance
    public static class InsufficientImageReportRateMeasurement extends Metric{
        public InsufficientImageReportRateMeasurement(){
            fillIn(
                this,
                MeasurementTypes.IMAGE_REPORT_RATE_MEASUREMENT,
                "images/minute",
                "0.1",
                AndroidPlatform.class
                );
        }
    }
    
    @ConceptInstance
    public static class NumClientsMeasurement extends Metric{
        public NumClientsMeasurement(){
            fillIn(
                this,
                MeasurementTypes.IMAGE_REPORT_RATE_MEASUREMENT,
                "count",
                "6",
                ServerPlatform.class
                );
        }
    }
    
}
