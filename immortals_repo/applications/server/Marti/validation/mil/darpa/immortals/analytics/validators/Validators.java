package mil.darpa.immortals.analytics.validators;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 12/19/16.
 */
public enum Validators {
    ClientLocationProduce("client-location-produce", ClientLocationProduceValidator.class),
    ClientLocationShare("client-location-share", ClientLocationShareValidator.class),
//    ClientLocationSourceTrusted("client-location-source-trusted", ClientLocationSourceTrustedValidator.class),
//    ClientLocationTrusted("client-location-trusted", ClientLocationTrustedValidator.class),
//    ClientLocationSourceUsb("client-location-source-usb", ClientLocationSourceUsbValidator.class),
//    ClientLocationSourceBluetooth("client-location-source-bluetooth", ClientLocationSourceBluetoothValidator.class),
//    ClientLocationSourceAndroidGps("client-location-source-androidgps", ClientLocationSourceAndroidGpsValidator.class),
//    ClientLocationSourceManual("client-location-source-manual", ClientLocationSourceManualValidator.class),
    ClientImageProduce("client-image-produce", ClientImageProduceValidator.class),
    ClientImageShare("client-image-share", ClientImageShareValidator.class);

    private static LinkedList<String> validatorIdentifierList = new LinkedList<>();


    private static HashMap<Class<? extends ValidatorInterface>, Validators> classMap;
    private static HashMap<String, Validators> identifierMap;


    public final String identifier;
    public final Class validatorClass;

    Validators(String identifier, Class validatorClass) {
        this.identifier = identifier;
        this.validatorClass = validatorClass;
    }

    private static synchronized void init() {
        if (identifierMap == null) {
            identifierMap = new HashMap<>();
            for (Validators ve : Validators.values()) {
                identifierMap.put(ve.identifier, ve);
            }
        }

        if (classMap == null) {
            classMap = new HashMap<>();
            for (Validators ve : Validators.values()) {
                classMap.put(ve.validatorClass, ve);
            }
        }
    }

    public static synchronized Validators byIdentifier(String identifier) {
        init();
        return identifierMap.get(identifier);
    }

    public static synchronized Validators byClass(Class<? extends ValidatorInterface> clazz) {
        init();
        return classMap.get(clazz);
    }

    public static synchronized Set<String> getValidatorIdentifierList() {
        init();
        return identifierMap.keySet();
    }

    public ValidatorInterface construct(@Nonnull Set<String> clientIdentifiers, boolean haltOnSuccessfulValidation) {
        // An exception should not be possible 
        try {
            Constructor[] c = validatorClass.getConstructors();
            return (ValidatorInterface) validatorClass.getDeclaredConstructor(Set.class, boolean.class).newInstance(clientIdentifiers, haltOnSuccessfulValidation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
