package mil.darpa.immortals.core;

/**
 * Created by awellman@bbn.com on 2/10/16.
 */
@Deprecated
public class Semantics {
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


//    public static final String Ecosystem_Hardware_SAASMGPS = ECOSYSTEM_BASE_URI + "SAASMLocationModule";
    public static final String Ecosystem_Environment_GPSSatellites = ECOSYSTEM_BASE_URI + "EnvironmentGPSSatellites";

    // Library dependency declarations. These are ultimately expected to come from code analysis, but I have included them here for modules that aren't "drop in and use" when all other dependencies aren't met
    // Using gradle maven dependency identifiers for the library identifiers
    public static final String Library_dom4j = LIBRARY_BASE_URI + "dom4j:dom4j:1.6.1";
    // Not gradle, but it's there to make sure it is obvious when a class that requires it can't be used (such as in Android)
    public static final String Library_awt = LIBRARY_BASE_URI + "java.awt";

    // Temporary uris that are being used for things that aren't properly defined yet
    public static final String Datatype_SerializableData = DATATYPE_BASE_URI + "SerializableDataSource";
//    public static final String Datatype_SerializableDataTarget = DATATYPE_BASE_URI + "SerializableDataTarget";
    public static final String Datatype_SerializableDataSourceStream = DATATYPE_BASE_URI + "SerializableDataSourceStream";
    public static final String Datatype_SerializableDataTargetStream = DATATYPE_BASE_URI + "SerializableDataTargetStream";
    public static final String Datatype_InputFilepath = DATATYPE_BASE_URI + "InputFilepath";
    public static final String Datatype_OutputFilepath = DATATYPE_BASE_URI + "OutputFilepath";
    public static final String Datatype_EncryptionKey = DATATYPE_BASE_URI + "EncryptionKey";

    public static final String Functionality_Crypto_Encrypt = FUNCTIONALITY_BASE_URI + "Crypto_encrypt";
    public static final String Functionality_Crypto_Decrypt = FUNCTIONALITY_BASE_URI + "Crypto_decrypt";
    public static final String Functionality_Crypto_JCE_AES = FUNCTIONALITY_BASE_URI + "CryptoAesJce";
    public static final String Functionality_Crypto_JCE_BLOWFISH = FUNCTIONALITY_BASE_URI + "CryptoBlowfishJce";
    public static final String Functionality_Crypto_BC_AES = FUNCTIONALITY_BASE_URI + "CryptoAesBouncyCastle";
}
