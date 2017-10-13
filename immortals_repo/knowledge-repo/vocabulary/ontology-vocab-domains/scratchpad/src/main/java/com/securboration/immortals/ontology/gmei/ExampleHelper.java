package com.securboration.immortals.ontology.gmei;

import java.util.ArrayList;
import java.util.List;

import com.securboration.immortals.ontology.android.AndroidPlatform;
import com.securboration.immortals.ontology.connectivity.BandwidthKiloBitsPerSecond;
import com.securboration.immortals.ontology.connectivity.NumClients;
import com.securboration.immortals.ontology.constraint.MultiplicityType;
import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.constraint.ValueCriterionType;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.cp.FunctionalitySpec;
import com.securboration.immortals.ontology.cp.MissionSpec;
import com.securboration.immortals.ontology.cp.SoftwareSpec;
import com.securboration.immortals.ontology.cp2.MeasurementTypes;
import com.securboration.immortals.ontology.functionality.imageprocessor.ImageProcessor;
import com.securboration.immortals.ontology.functionality.locationprovider.LocationProvider;
import com.securboration.immortals.ontology.image.fidelity.ImageSize1024x1024;
import com.securboration.immortals.ontology.image.fidelity.Rgb24;
import com.securboration.immortals.ontology.metrics.MeasurementType;
import com.securboration.immortals.ontology.metrics.Metric;
import com.securboration.immortals.ontology.ordering.ExplicitNumericOrderingMechanism;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.PropertyConstraint;
import com.securboration.immortals.ontology.resources.BluetoothResource;
import com.securboration.immortals.ontology.resources.FileSystemResource;
import com.securboration.immortals.ontology.resources.PlatformResource;
import com.securboration.immortals.ontology.resources.RadioChannel;
import com.securboration.immortals.ontology.resources.UsbResource;
import com.securboration.immortals.ontology.resources.UserInterface;
import com.securboration.immortals.ontology.resources.gps.GpsReceiver;
import com.securboration.immortals.ontology.resources.gps.GpsReceiverEmbedded;
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
import com.securboration.immortals.ontology.resources.network.NetworkConnection;
import com.securboration.immortals.ontology.server.ServerPlatform;

@Ignore
public class ExampleHelper {
    
    private static GpsSatelliteConstellation constellation = getConstellation(3);
    private static RadioChannel[] gpsSpectrum = getGpsSpectrum();
    
    public static MissionSpec getNumClientsSpec(Resource r){
        MissionSpec m = new MissionSpec();
        
        m.setHumanReadableForm(
            "the software must support at least 25 concurrent clients"
            );
        m.setAssertionCriterion(ValueCriterionType.VALUE_GREATER_THAN_INCLUSIVE);
        m.setRightValue(getValue(MeasurementTypes.NUM_CLIENTS_MEASUREMENT,"count","25",r.getClass()));
        
        return m;
    }
    
    public static MissionSpec getPliReportRateSpec(Resource r){
        MissionSpec m = new MissionSpec();
        
        m.setHumanReadableForm(
            "the client must issue > 10 PLI messages per minute"
            );
        m.setAssertionCriterion(ValueCriterionType.VALUE_GREATER_THAN_EXCLUSIVE);
        m.setRightValue(getValue(MeasurementTypes.PLI_REPORT_RATE_MEASUREMENT,"messages/minute","10",r.getClass()));
        
        return m;
    }
    
    public static MissionSpec getMinimumBandwidth(Resource r){
        MissionSpec m = new MissionSpec();
        
        m.setHumanReadableForm(
            "the link must never see > 25kbps in traffic"
            );
        m.setAssertionCriterion(ValueCriterionType.VALUE_LESS_THAN_INCLUSIVE);
        m.setRightValue(getValue(MeasurementTypes.BANDWIDTH_MEASUREMENT,"kb/s","25",r.getClass()));
        
        return m;
    }
    
    public static MissionSpec getImageReportRateSpec(Resource r){
        MissionSpec m = new MissionSpec();
        
        m.setHumanReadableForm(
            "the software must provide at least 1 image updates per minute"
            );
        m.setAssertionCriterion(ValueCriterionType.VALUE_GREATER_THAN_INCLUSIVE);
        m.setRightValue(getValue(MeasurementTypes.IMAGE_REPORT_RATE_MEASUREMENT,"images/minute","1",r.getClass()));
        
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
            PropertyCriterionType.PROPERTY_LESS_THAN_EXCLUSIVE
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
    
//    public static FunctionalitySpec getSaDataProviderSpec(){
//        return new FunctionalitySpec(
//            SaDataProvider.class,
//            getAtLeastNBandwidthBytesPerS(1024)
////            getAtLeastNClients(16)
//            );
//    }
    
    private static PropertyConstraint getAtLeastNBandwidthBytesPerS(final long b){
        PropertyConstraint c = new PropertyConstraint();
        
        c.setHumanReadableForm(
            "at least " + b + " kb/s"
            );
        c.setConstraintCriterion(
            PropertyCriterionType.PROPERTY_GREATER_THAN_INCLUSIVE
            );
        
        c.setConstrainedProperty(new Property[]{
                new BandwidthKiloBitsPerSecond(b)
        });
        
        return c;
    }
    
    private static PropertyConstraint getAtLeastNClients(final int n){
        PropertyConstraint c = new PropertyConstraint();
        
        c.setHumanReadableForm(
            "at least " + n + " clients"
            );
        c.setConstraintCriterion(
            PropertyCriterionType.PROPERTY_GREATER_THAN_INCLUSIVE
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
//        c.setConstraintMultiplicity(
//            MultiplicityType.APPLICABLE_TO_ALL_OF
//            );
        c.setConstraintCriterion(
            PropertyCriterionType.PROPERTY_PRESENT
            );
        c.setConstrainedProperty(new Property[]{
                new TrustedProperty()
        });
        
        return c;
    }
    
    public static ServerPlatform getServer(PlatformResource...resources){
        ServerPlatform s = new ServerPlatform();
        
//        s.setPlatformResources(resources);
        
        return s;
    }
    
    public static AndroidPlatform getAndroidDeviceWithGpsReceiverAdvanced(NetworkConnection n){
        
        AndroidPlatform p = new AndroidPlatform();
        
        p.setAndroidPlatformVersion("6.0.1 Marshmallow");
        p.setHumanReadableDescription(
            "Marshmallow device with BlueTooth, USB, and a SASM GPS module");
        
        p.setPlatformResources(
            new PlatformResource[]{
                    getGpsReceiver(),
                    getBluetoothResource(),
                    getUsbResource(),
                    getFileSystemResource(),
                    n
                    }
            );
        
        return p;
    }
    
    private static UsbResource getUsbResource(){
        UsbResource u =  new UsbResource();
        
        u.setHumanReadableDescription("a USB port");
        
        return u;
    }
    
    private static BluetoothResource getBluetoothResource(){
        BluetoothResource r = new BluetoothResource();
        
        r.setHumanReadableDescription("a Bluetooth transceiver");
        
        return r;
    }
    
    private static UserInterface getUiResource(){
        UserInterface r = new UserInterface();
        
        r.setHumanReadableDescription("a user interface");
        
        return r;
    }
    
    public static AndroidPlatform getAndroidDeviceTemplate(NetworkConnection c){
        AndroidPlatform p = new AndroidPlatform();
        
        p.setAndroidPlatformVersion(
            "6.0.1 Marshmallow"
            );
        p.setHumanReadableDescription(
            "Marshmallow device with the following hardware: " +
            "a USB port, " +
            "a Bluetooth transceiver, " +
            "a physical UI, " +
            "an embedded GPS receiver, " +
            "a network connection"
            );
        p.setPlatformResources(new PlatformResource[]{
                getUsbResource(),
                getBluetoothResource(),
                getUiResource(),
                getGpsReceiverSimple(),
                c
        });
        
        return p;
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
        
        GpsReceiver r = new GpsReceiverEmbedded();
        r.setHumanReadableDescription("an embedded GPS receiver");
//        r.setNumChannels(5);
        
        return r;
        
    }
    
    private static FileSystemResource getFileSystemResource(){
        
        FileSystemResource f = new FileSystemResource();
        f.setHumanReadableDescription("a device file system");
        return f;
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
    
    public static GpsSatelliteConstellation getConstellation(final int n){
        GpsSatelliteConstellation c = new GpsSatelliteConstellation();
        
        c.setHumanReadableDescription("a constellation of satellites");
        c.setConstellationName("GPS");
        c.setSatellites(getSatellites(n));
        
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
    
    private static MeasurementType getMeasurement(
            final String type,
            final Class<? extends Property> indicativeOfProperty
            ){
        MeasurementType m = new MeasurementType();
        m.setMeasurementType(type);
        m.setCorrespondingProperty(indicativeOfProperty);
        return m;
    }
    
    private static Metric getValue(
            final MeasurementType type,
            final String unit,
            final String value,
            final Class<? extends Resource> resource
            ){
        Metric v = new Metric();
        
        v.setUnit(unit);
        v.setValue(value);
        v.setMeasurementType(type);
        v.setApplicableResourceType(resource);
        
        return v;
    }

}
