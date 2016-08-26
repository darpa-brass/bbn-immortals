package com.securboration.immortals.example.instantiation;

import java.util.ArrayList;
import java.util.List;

import com.securboration.immortals.ontology.android.AndroidPlatform;
import com.securboration.immortals.ontology.connectivity.BandwidthBytesPerSecond;
import com.securboration.immortals.ontology.connectivity.NumClients;
import com.securboration.immortals.ontology.connectivity.SaDataProvider;
import com.securboration.immortals.ontology.constraint.MultiplicityType;
import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.constraint.ValueCriterionType;
import com.securboration.immortals.ontology.cp.FunctionalitySpec;
import com.securboration.immortals.ontology.cp.MissionSpec;
import com.securboration.immortals.ontology.cp.SoftwareSpec;
import com.securboration.immortals.ontology.functionality.imageprocessor.ImageProcessor;
import com.securboration.immortals.ontology.functionality.locationprovider.LocationProvider;
import com.securboration.immortals.ontology.image.fidelity.ImageSize1024x1024;
import com.securboration.immortals.ontology.image.fidelity.Rgb24;
import com.securboration.immortals.ontology.metrics.MeasuredValue;
import com.securboration.immortals.ontology.metrics.MeasurementType;
import com.securboration.immortals.ontology.ordering.ExplicitNumericOrderingMechanism;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.PropertyConstraint;
import com.securboration.immortals.ontology.resources.BluetoothResource;
import com.securboration.immortals.ontology.resources.PlatformResource;
import com.securboration.immortals.ontology.resources.RadioChannel;
import com.securboration.immortals.ontology.resources.UsbResource;
import com.securboration.immortals.ontology.resources.gps.GpsReceiver;
import com.securboration.immortals.ontology.resources.gps.GpsReceiverSaasm;
import com.securboration.immortals.ontology.resources.gps.GpsSatellite;
import com.securboration.immortals.ontology.resources.gps.GpsSatelliteConstellation;
import com.securboration.immortals.ontology.resources.gps.L1_C;
import com.securboration.immortals.ontology.resources.gps.L1_CA;
import com.securboration.immortals.ontology.resources.gps.L1_PY;
import com.securboration.immortals.ontology.resources.gps.L2_C;
import com.securboration.immortals.ontology.resources.gps.L2_CA;
import com.securboration.immortals.ontology.resources.gps.L2_PY;
import com.securboration.immortals.ontology.resources.gps.L5;
import com.securboration.immortals.ontology.resources.gps.properties.TrustedProperty;

@Ignore
public class ExampleHelper {
    
    private static GpsSatelliteConstellation constellation = getConstellation();
    private static RadioChannel[] gpsSpectrum = getGpsSpectrum();
    
    private static MeasurementType pliReportRate = 
            getMeasurement("PLI report rate");
    
    private static MeasurementType imageReportRate = 
            getMeasurement("Image report rate");
    
    private static MeasurementType numClients = 
            getMeasurement("Number of clients");
    
    public static MissionSpec getNumClientsSpec(){
        MissionSpec m = new MissionSpec();
        
        m.setHumanReadableForm(
            "the software must support at least 25 concurrent clients"
            );
        m.setValueCriterion(ValueCriterionType.WHEN_VALUE_GREATER_THAN_INCLUSIVE);
        m.setRightValue(getValue(numClients,"count","25"));
        
        return m;
    }
    
    public static MissionSpec getPliReportRateSpec(){
        MissionSpec m = new MissionSpec();
        
        m.setHumanReadableForm(
            "the software must issue > 10 PLI messages per minute"
            );
        m.setValueCriterion(ValueCriterionType.WHEN_VALUE_GREATER_THAN_EXCLUSIVE);
        m.setRightValue(getValue(pliReportRate,"messages/minute","10"));
        
        return m;
    }
    
    public static MissionSpec getImageReportRateSpec(){
        MissionSpec m = new MissionSpec();
        
        m.setHumanReadableForm(
            "the software must provide at least 1 image updates per minute"
            );
        m.setValueCriterion(ValueCriterionType.WHEN_VALUE_GREATER_THAN_INCLUSIVE);
        m.setRightValue(getValue(imageReportRate,"images/minute","1"));
        
        return m;
    }
    
    public static FunctionalitySpec getImageProcessorSpec(){
        return new FunctionalitySpec(
            ImageProcessor.class,
            getImageQualityConstraint()
            );
    }
    
    public static void setPrecedences(SoftwareSpec[] spec){
        for(int i=0;i<spec.length;i++){
            ExplicitNumericOrderingMechanism n = 
                    new ExplicitNumericOrderingMechanism();
            n.setPrecedence(i);
            spec[i].setPrecedenceOfSpec(n);
        }
    }
    
    private static PropertyConstraint getImageQualityConstraint(){
        PropertyConstraint c = new PropertyConstraint();
        
        c.setHumanReadableForm(
            "the implementer must produce images at " +
            "least 1024x1024 pixels in size AND with at least 24-bit RGB " +
            "channels"
            );
        c.setConstraintCriterion(
            PropertyCriterionType.WHEN_PROPERTY_GREATER_THAN_INCLUSIVE
            );
        c.setConstraintMultiplicity(
            MultiplicityType.APPLICABLE_TO_ALL_OF
            );
        c.setConstrainedProperty(new Property[]{
                new ImageSize1024x1024(),
                new Rgb24()
        });
        
        return c;
    }
    
    public static FunctionalitySpec getTrustedLocationProviderSpec(){
        return new FunctionalitySpec(
            LocationProvider.class,
            getTrustedConstraint()
            );
    }
    
    public static FunctionalitySpec getSaDataProviderSpec(){
        return new FunctionalitySpec(
            SaDataProvider.class,
            getAtLeastNBandwidthBytesPerS(1024),
            getAtLeastNClients(16)
            );
    }
    
    private static PropertyConstraint getAtLeastNBandwidthBytesPerS(final long b){
        PropertyConstraint c = new PropertyConstraint();
        
        c.setHumanReadableForm(
            "at least " + b + " bytes/s"
            );
        c.setConstraintCriterion(
            PropertyCriterionType.WHEN_PROPERTY_GREATER_THAN_INCLUSIVE
            );
        
        c.setConstrainedProperty(new Property[]{
                new BandwidthBytesPerSecond(b)
        });
        
        return c;
    }
    
    private static PropertyConstraint getAtLeastNClients(final int n){
        PropertyConstraint c = new PropertyConstraint();
        
        c.setHumanReadableForm(
            "at least " + n + " clients"
            );
        c.setConstraintCriterion(
            PropertyCriterionType.WHEN_PROPERTY_GREATER_THAN_INCLUSIVE
            );
        
        c.setConstrainedProperty(new Property[]{
                new NumClients(n)
        });
        
        return c;
    }
    
    private static PropertyConstraint getTrustedConstraint(){
        PropertyConstraint c = new PropertyConstraint();
        
        c.setHumanReadableForm(
            "the implementer must possess TrustedProperty"
            );
        c.setConstraintMultiplicity(
            MultiplicityType.APPLICABLE_TO_ONE_OF
            );
        c.setConstrainedProperty(new Property[]{
                new TrustedProperty()
        });
        
        return c;
    }
    
    public static AndroidPlatform getAndroidDeviceWithGpsReceiverAdvanced(){
        
        AndroidPlatform p = new AndroidPlatform();
        
        p.setAndroidPlatformVersion("6.0.1 Marshmallow");
        p.setHumanReadableDescription(
            "Marshmallow device with BlueTooth, USB, and a SASM GPS module");
        
        p.setPlatformResources(
            new PlatformResource[]{
                    getGpsReceiver(),
                    getBluetoothResource(),
                    getUsbResource(),
                    }
            );
        
        return p;
    }
    
    private static UsbResource getUsbResource(){
        return new UsbResource();
    }
    
    private static BluetoothResource getBluetoothResource(){
        BluetoothResource r = new BluetoothResource();
        
        return r;
    }
    
    public static AndroidPlatform getAndroidDeviceWithGpsReceiverSimple(){
        
        AndroidPlatform p = new AndroidPlatform();
        
        p.setAndroidPlatformVersion("6.0.1 Marshmallow");
        p.setHumanReadableDescription("Marshmallow device with a standard GPS receiver");
        p.setPlatformResources(
            new PlatformResource[]{
                    getGpsReceiverSimple()
                    }
            );
        
        return p;
    }
    
    private static RadioChannel[] getGpsSpectrum(){
        return new RadioChannel[]{
                new L1_C(),
                new L1_CA(),
                new L1_PY(),
                new L2_C(),
                new L2_CA(),
                new L2_PY(),
                new L5()
        };
    }
    
    private static GpsReceiver getGpsReceiverSimple(){
        
        GpsReceiver r = new GpsReceiver();
        
        r.setNumChannels(5);
        
        return r;
        
    }
    
    private static GpsReceiver getGpsReceiver(){
        
        GpsReceiverSaasm r = new GpsReceiverSaasm();
        
        r.setNumChannels(5);
        r.setReceivableSpectrum(gpsSpectrum);
        r.setConstellation(constellation);
        r.setResourceProperty(new Property[]{
            new TrustedProperty()
        });
        
        return r;
        
    }
    
    private static GpsSatelliteConstellation getConstellation(){
        GpsSatelliteConstellation c = new GpsSatelliteConstellation();
        c.setConstellationName("GPS");
        c.setSatellites(getSatellites(24));
        
        return c;
    }
    
    private static GpsSatellite[] getSatellites(int n){
        
        List<GpsSatellite> satellites = new ArrayList<>();
        for(int i=0;i<n;i++){
            GpsSatellite satellite = new GpsSatellite();
            satellite.setHasFineGrainedEncryptedLocation(true);
            satellite.setSatelliteId("OPS 511"+i);
            
            satellites.add(satellite);
        }
        
        return satellites.toArray(new GpsSatellite[]{});
    }
    
    private static MeasurementType getMeasurement(final String type){
        MeasurementType m = new MeasurementType();
        m.setMeasurementType(type);
        return m;
    }
    
    private static MeasuredValue getValue(
            final MeasurementType type,
            final String unit,
            final String value
            ){
        MeasuredValue v = new MeasuredValue();
        
        v.setUnit(unit);
        v.setValue(value);
        v.setType(type);
        
        return v;
    }

}
