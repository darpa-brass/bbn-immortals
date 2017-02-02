package mil.darpa.immortals.analytics.validators;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 12/19/16.
 */
public enum Validators {
    ClientLocationProduce("client-location-produce", ClientLocationProduceValidator.class),
    ClientLocationShare("client-location-share", ClientLocationShareValidator.class),
    ClientLocationSourceTrusted("client-location-source-trusted", ClientLocationSourceTrustedValidator.class),
    ClientLocationTrusted("client-location-trusted", ClientLocationTrustedValidator.class),
    ClientLocationSourceUsb("client-location-source-usb", ClientLocationSourceUsbValidator.class),
    ClientLocationSourceBluetooth("client-location-source-bluetooth", ClientLocationSourceBluetoothValidator.class),
    ClientLocationSourceAndroidGps("client-location-source-androidgps", ClientLocationSourceAndroidGpsValidator.class),
    ClientLocationSourceManual("client-location-source-manual", ClientLocationSourceManualValidator.class),
    ClientImageProduce("client-image-produce", ClientImageProduceValidator.class),
    ClientImageShare("client-image-share", ClientImageShareValidator.class);

    private static LinkedList<String> validatorIdentifierList = new LinkedList<>();

    private final String label;
    private final Class clazz;

    Validators(String label, Class clazz) {
        this.label = label;
        this.clazz = clazz;
    }

    public String getLabel() {
        return label;
    }

    public static Validators getByLabel(String label) {
        for (Validators v : Validators.values()) {
            if (v.label.equals(label)) {
                return v;
            }
        }
        throw new RuntimeException("ERROR: The specified identifier '" + label + "' is not defined in Validators.java!");
    }

    public static List<String> getValidatorIdentifierList() {
        synchronized (validatorIdentifierList) {
            if (validatorIdentifierList.isEmpty()) {
                for (Validators v : Validators.values()) {
                    validatorIdentifierList.add(v.label);
                }

            }
        }
        return new ArrayList<>(validatorIdentifierList);
    }


    public ValidatorInterface construct(@Nonnull Set<String> clientIdentifiers) {
        // An exception should not be possible 
        try {
            return (ValidatorInterface) clazz.getDeclaredConstructor(Set.class).newInstance(clientIdentifiers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
