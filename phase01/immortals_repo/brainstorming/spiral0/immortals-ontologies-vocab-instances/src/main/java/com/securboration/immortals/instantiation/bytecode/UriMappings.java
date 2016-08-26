package com.securboration.immortals.instantiation.bytecode;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.ModelFactory;
import org.objectweb.asm.Type;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.functionality.datatype.PhysicalLocationMeasurement;
import com.securboration.immortals.ontology.functionality.location.GetLastKnownLocation;
import com.securboration.immortals.ontology.functionality.location.LocationProvider;
import com.securboration.immortals.ontology.functionality.location.LocationProviderAndroidGps;
import com.securboration.immortals.ontology.functionality.location.LocationProviderBluetoothGps;
import com.securboration.immortals.ontology.functionality.location.LocationProviderManual;
import com.securboration.immortals.ontology.functionality.location.LocationProviderSaasm;
import com.securboration.immortals.ontology.functionality.location.LocationProviderSimulated;
import com.securboration.immortals.ontology.functionality.location.LocationProviderUsbGps;
import com.securboration.immortals.ontology.resources.PlatformResource;
import com.securboration.immortals.ontology.resources.TODO.BluetoothResource;
import com.securboration.immortals.ontology.resources.TODO.HardwareUserInterface;
import com.securboration.immortals.ontology.resources.TODO.SaasmCryptoKey;
import com.securboration.immortals.ontology.resources.TODO.UsbInput;
import com.securboration.immortals.ontology.resources.gps.GpsReceiver;
import com.securboration.immortals.ontology.resources.gps.GpsSatellite;

/**
 * Maps the URIs in <a href=
 * "https://dsl-external.bbn.com/svn/immortals/trunk/shared/modules/core/src/main/java/mil/darpa/immortals/core/Semantics.java">
 * Semantics.java</a> to URIs in the ontology
 * 
 * @author jstaples
 *
 */
public class UriMappings {
    
    private final Map<String,SemanticMapping> map = 
            new HashMap<>();
    
    private final ObjectToTriplesConfiguration context;
    
    private void init(){
        
        //types
        addMapping(
                Semantics.Datatype_Coordinates,
                PhysicalLocationMeasurement.class
                );
        
        //functionality
        addMapping(
                Semantics.Functionality_LocationProvider,
                LocationProvider.class
                );
        
        addMapping(
                Semantics.Functionality_LocationProvider_AndroidGPS,
                LocationProviderAndroidGps.class
                );
        addMapping(
                Semantics.Functionality_LocationProvider_BluetoothGPS,
                LocationProviderBluetoothGps.class
                );
        addMapping(
                Semantics.Functionality_LocationProvider_Manual,
                LocationProviderManual.class
                );
        addMapping(
                Semantics.Functionality_LocationProvider_SAASM,
                LocationProviderSaasm.class
                );
        addMapping(
                Semantics.Functionality_LocationProvider_Simulated,
                LocationProviderSimulated.class
                );
        addMapping(
                Semantics.Functionality_LocationProvider_USBGPS,
                LocationProviderUsbGps.class
                );
        
        //functional aspects
        addMapping(
                Semantics.Functionality_LocationProvider_LastKnown,
                GetLastKnownLocation.class
                );
        
        
        //resources
        addMapping(
                Semantics.Ecosystem_Platform_Android,
                PlatformResource.class
                );
        addMapping(
                Semantics.Ecosystem_Hardware_EmbeddedGPS,
                GpsReceiver.class
                );
        addMapping(
                Semantics.Ecosystem_Environment_GPSSatellites,
                GpsSatellite.class
                );
        addMapping(
                Semantics.Ecosystem_Hardware_Bluetooth,
                BluetoothResource.class
                );
        addMapping(
                Semantics.Ecosystem_Hardware_UserInterface,
                HardwareUserInterface.class
                );
        addMapping(
                Semantics.Ecosystem_Data_SAASMCryptoKey,
                SaasmCryptoKey.class
                );
        addMapping(
                Semantics.Ecosystem_Hardware_USBInput,
                UsbInput.class
                );
    }
    
    private void addMapping(String uri,Class<?> c){
        map.put(uri, new ClassMapping(c));
    }
    
    public abstract class SemanticMapping{
        private final String uri;

        public SemanticMapping(String uri) {
            super();
            this.uri = uri;
        }
    }
    
    public class ClassMapping extends SemanticMapping{
        private final Class<?> c;
        
        public ClassMapping(Class<?> c) {
            super(getUriForClass(c));
            this.c = c;
        }
    }
    
    private class FieldMapping extends SemanticMapping{
        private final Class<?> classContainingField;
        private final String fieldName;
        private final Class<?> fieldType;
        
        public FieldMapping(
                Class<?> c,
                String fieldName,
                Class<?> fieldType
                ) {
            super(getUriForField(c,fieldName,fieldType));
            
            this.classContainingField = c;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }
    }
    
    private String getUriForClass(Class<?> c){
        return OntologyHelper.makeUriName(
                context, 
                c.getName()
                );
    }
    
    private String getUriForField(
            Class<?> c,
            String fieldName,
            Class<?> fieldType
            ){
        
        try{
            c.getField(fieldName);
        } catch(NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        
        return OntologyHelper.getPropertyForField(
                context, 
                ModelFactory.createDefaultModel(),
                fieldName,
                Type.getType(fieldType)
                ).getURI();
    }
    
    private Class<?> getClassMapping(String uriKey){
        if(!map.containsKey(uriKey)){
            return null;
        }
        
        return ((ClassMapping)map.get(uriKey)).c;
    }
    
    public <T> T getInstance(final String uri,Class<T> type){
        Class<?> c = getClassMapping(uri);
        
        if(c == null){
            throw new RuntimeException("unable to instantiate a POJO for uri " + uri);
        }
        
        if(!type.isAssignableFrom(c)){
            throw new RuntimeException(
                    type.getName() + " is not a assignable from " + c.getName());
        }
        
        Object o = null;
        try {
            o = c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(
                    "unable to instantiate " + c.getName() + " for URI " + uri);
        }
        
        return (T)o;//erasure issue :(
    }
    
    public Class<? extends DataType> mapSemanticUriToDatatype(String uri){
        
        Class<?> c = getClassMapping(uri);
        
        if(DataType.class.isAssignableFrom(c)){
            return (Class<? extends DataType>)c;//erasure issue :(
        }
        
        throw new RuntimeException(
                "The class mapped by URI " + uri + 
                " is not assignable from " + DataType.class.getName());
    }
    
    public String map(String uriKey){
        SemanticMapping m = map.get(uriKey);
        
        if(m == null){
            return null;
        }
        
        return m.uri;
    }

    public UriMappings(ObjectToTriplesConfiguration context) {
        super();
        this.context = context;
        
        init();
    }
    
    
    
    
    
    public static class AnnotationDescriptors {
        public static final String SemanticTypeBinding = 
                "Lmil/darpa/immortals/core/annotations/SemanticTypeBinding;";
        
        public static final String Dfu = 
                "Lmil/darpa/immortals/core/annotations/Dfu;";
        
        public static final String FunctionalDfuAspect =
                "Lmil/darpa/immortals/core/annotations/FunctionalDfuAspect;";
        
        public static final String ResourceDependencies = 
                "Lmil/darpa/immortals/core/annotations/ResourceDependencies;";
        
    }
    
    //////////////////////////////////////////////
    // Content below copied from semantics.java //
    //////////////////////////////////////////////
    private static class Semantics {
        private static final String ROOT_URI = "file://darpa.mil/immortals/r1.0/";

        // Base URIs
        private static final String DATATYPE_BASE_URI = ROOT_URI + "Datatypes#";
        private static final String FUNCTIONALITY_BASE_URI = ROOT_URI + "Functionality#";
        private static final String SENSOR_BASE_URI = ROOT_URI + "Sensors#";
        private static final String ECOSYSTEM_BASE_URI = ROOT_URI + "Ecosystem#";
        private static final String LIBRARY_BASE_URI = ROOT_URI + "Library#";

        // DFU Identifier declarations 
        public static final String Functionality_LocationProvider_AndroidGPS = FUNCTIONALITY_BASE_URI + "LocationProvider" + "AndroidGPS"; 
        public static final String Functionality_LocationProvider_SAASM = FUNCTIONALITY_BASE_URI + "LocationProvider" + "SAASM"; 
        public static final String Functionality_LocationProvider_Manual = FUNCTIONALITY_BASE_URI + "LocationProvider" + "Manual"; 
        public static final String Functionality_LocationProvider_Simulated = FUNCTIONALITY_BASE_URI + "LocationProvider" + "Simulated"; 
        public static final String Functionality_LocationProvider_USBGPS = FUNCTIONALITY_BASE_URI + "LocationProvider" + "USBGPS"; 
        public static final String Functionality_LocationProvider_BluetoothGPS = FUNCTIONALITY_BASE_URI + "LocationProvider" + "BluetoothGPS"; 

        // Datatype declarations
        public static final String Datatype_Coordinates = DATATYPE_BASE_URI + "Coordinates";
        public static final String Datatype_CotMessage = DATATYPE_BASE_URI + "CotMessage";
        // A stream of sequential CotMessage data (such as what would be received packet-by-packet from the network
        public static final String Datatype_CotMessageStream = DATATYPE_BASE_URI + "CotMessageStream";
        // An image and its associate location data (be it EXIF, a wrapper class containing the two, or something else)
        public static final String Datatype_LocationImage = DATATYPE_BASE_URI + "LocationImage";

        // Data to access the file such as a java "File" object or an absolute file path
        public static final String Datatype_ImageDescriptor = DATATYPE_BASE_URI + "ImageJPEGDescriptor";
        // A complete image, including any metadata inferring its format
        public static final String Datatype_Image = DATATYPE_BASE_URI + "Image";

        // Functionality declarations and environment dependencies
        public static final String Functionality_LocationProvider = FUNCTIONALITY_BASE_URI + "LocationProvider";
        public static final String Functionality_LocationProvider_LastKnown = Functionality_LocationProvider + "_lastKnownLocationAspect";

        public static final String Functionality_ResizeImagePreserveAspect = FUNCTIONALITY_BASE_URI + "ResizeImagePreserveAspect";
        public static final String Functionality_ResizeImageIgnoreAspect = FUNCTIONALITY_BASE_URI + "ResizeImageIgnoreAspect";


        // Sensor declarations
        public static final String Sensor_Latitude = SENSOR_BASE_URI + "Latitude";
        public static final String Sensor_Longitude = SENSOR_BASE_URI + "Longitude";
        public static final String Sensor_Altitude_MSL = SENSOR_BASE_URI + "Altitude_MSL";
        public static final String Sensor_Accuracy_GDOP = SENSOR_BASE_URI + "Accuracy_GDOP";
        public static final String Sensor_SensorReadTime = SENSOR_BASE_URI + "SensorReadTime";


        // Ecosystem-Predicate declarations
        public static final String Predicate_DependsOn = ECOSYSTEM_BASE_URI + "DependsOn";
        public static final String Ecosystem_Platform_Android = ECOSYSTEM_BASE_URI + "PlatformIsAndroid";
        public static final String Ecosystem_Hardware_EmbeddedGPS = ECOSYSTEM_BASE_URI + "HardwareContainsEmbeddedGPS";
        public static final String Ecosystem_Hardware_USBInput = ECOSYSTEM_BASE_URI + "HardwareUSBInput";
        public static final String Ecosystem_Hardware_Bluetooth = ECOSYSTEM_BASE_URI + "HardwareBluetooth";
        public static final String Ecosystem_Data_SAASMCryptoKey = ECOSYSTEM_BASE_URI + "DataSAASMCryptoKey";
        public static final String Ecosystem_Hardware_UserInterface = ECOSYSTEM_BASE_URI + "HardwareUserInterface";


//        public static final String Ecosystem_Hardware_SAASMGPS = ECOSYSTEM_BASE_URI + "SAASMLocationModule";
        public static final String Ecosystem_Environment_GPSSatellites = ECOSYSTEM_BASE_URI + "EnvironmentGPSSatellites";

        // Library dependency declarations. These are ultimately expected to come from code analysis, but I have included them here for modules that aren't "drop in and use" when all other dependencies aren't met
        // Using gradle maven dependency identifiers for the library identifiers
        public static final String Library_dom4j = LIBRARY_BASE_URI + "dom4j:dom4j:1.6.1";
        // Not gradle, but it's there to make sure it is obvious when a class that requires it can't be used (such as in Android)
        public static final String Library_awt = LIBRARY_BASE_URI + "java.awt";
    }
}
