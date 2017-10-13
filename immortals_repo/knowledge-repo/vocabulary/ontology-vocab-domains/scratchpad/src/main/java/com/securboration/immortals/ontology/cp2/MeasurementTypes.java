package com.securboration.immortals.ontology.cp2;

import com.securboration.immortals.ontology.connectivity.BandwidthKiloBitsPerSecond;
import com.securboration.immortals.ontology.connectivity.ImageReportRate;
import com.securboration.immortals.ontology.connectivity.LocationAccuracy;
import com.securboration.immortals.ontology.connectivity.LocationReportRate;
import com.securboration.immortals.ontology.connectivity.NumClients;
import com.securboration.immortals.ontology.connectivity.PliReportRate;
import com.securboration.immortals.ontology.functionality.dataproperties.Compressed;
import com.securboration.immortals.ontology.functionality.imagescaling.NumberOfPixels;
import com.securboration.immortals.ontology.metrics.MeasurementType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;

@Ignore
public class MeasurementTypes {
    
    public static MeasurementType PLI_REPORT_RATE_MEASUREMENT = 
            new PliReportRateMeasurementType();
    public static MeasurementType BANDWIDTH_MEASUREMENT = 
            new BandwidthMeasurementType();
    public static MeasurementType NUM_CLIENTS_MEASUREMENT = 
            new NumClientsMeasurementType();
    public static MeasurementType IMAGE_REPORT_RATE_MEASUREMENT = 
            new ImageReportRateMeasurementType();
    public static MeasurementType SCALING_FACTOR_MEASUREMENT = 
            new ScalingFactorMeasurementType();
    public static MeasurementType INPUT_MEGAPIXELS = 
            new InputMegapixelsMeasurementType();
    public static MeasurementType OUTPUT_MEGAPIXELS = 
            new OutputMegapixelsMeasurementType();
    public static MeasurementType LOCATION_REPORT_RATE_MEASUREMENT = 
            new LocationReportRateMeasurementType();
    public static MeasurementType LOCATION_ACCURACY_MEASUREMENT = 
            new LocationAccuracyMeasurementType();
    
    @ConceptInstance
    public static class InputMegapixelsMeasurementType extends MeasurementType{
        public InputMegapixelsMeasurementType(){
            this.setMeasurementType("Input megapixels");
            this.setCorrespondingProperty(NumberOfPixels.class);
        }
    }  
    
    @ConceptInstance
    public static class OutputMegapixelsMeasurementType extends MeasurementType{
        public OutputMegapixelsMeasurementType(){
            this.setMeasurementType("Output megapixels");
            this.setCorrespondingProperty(NumberOfPixels.class);
        }
    }  
            
    @ConceptInstance
    public static class ScalingFactorMeasurementType extends MeasurementType{
        public ScalingFactorMeasurementType(){
            this.setMeasurementType("Scaling factor");
            this.setCorrespondingProperty(Compressed.class);
        }
    }
    
    @ConceptInstance
    public static class PliReportRateMeasurementType extends MeasurementType{
        public PliReportRateMeasurementType(){
            this.setMeasurementType("PLI report rate");
            this.setCorrespondingProperty(PliReportRate.class);
        }
    }
    
    @ConceptInstance
    public static class BandwidthMeasurementType extends MeasurementType{
        public BandwidthMeasurementType(){
            this.setMeasurementType("EWMA bandwidth consumption");
            this.setCorrespondingProperty(BandwidthKiloBitsPerSecond.class);
        }
    }
    
    @ConceptInstance
    public static class ImageReportRateMeasurementType extends MeasurementType{
        public ImageReportRateMeasurementType(){
            this.setMeasurementType("Image report rate");
            this.setCorrespondingProperty(ImageReportRate.class);
        }
    }
    
    @ConceptInstance
    public static class NumClientsMeasurementType extends MeasurementType{
        public NumClientsMeasurementType(){
            this.setMeasurementType("Number of clients");
            this.setCorrespondingProperty(NumClients.class);
        }
    }
    
    @ConceptInstance
    public static class LocationReportRateMeasurementType extends MeasurementType{
        public LocationReportRateMeasurementType(){
            this.setMeasurementType("Location report rate");
            this.setCorrespondingProperty(LocationReportRate.class);
        }
    }
    
    @ConceptInstance
    public static class LocationAccuracyMeasurementType extends MeasurementType{
        public LocationAccuracyMeasurementType(){
            this.setMeasurementType("Location accuracy measurement");
            this.setCorrespondingProperty(LocationAccuracy.class);
        }
    }

}
