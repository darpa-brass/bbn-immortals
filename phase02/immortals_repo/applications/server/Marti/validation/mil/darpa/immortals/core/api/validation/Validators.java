package mil.darpa.immortals.core.api.validation;

/**
 * Created by awellman@bbn.com on 8/15/17.
 */
public enum Validators {
    CLIENT_LOCATION_PRODUCE(
            "client-location-produce",
            ValidatorType.JAVA,
            "mil.darpa.immortals.analytics.validators.ClientLocationProduceValidator"),

    CLIENT_IMAGE_PRODUCE(
            "client-image-produce",
            ValidatorType.JAVA,
            "mil.darpa.immortals.analytics.validators.ClientImageProduceValidator"),

    CLIENT_LOCATION_SHARE(
            "client-location-share",
            ValidatorType.JAVA,
            "mil.darpa.immortals.analytics.validators.ClientLocationShareValidator"),

    CLIENT_IMAGE_SHARE(
            "client-image-share",

            ValidatorType.JAVA,
            "mil.darpa.immortals.analytics.validators.ClientImageShareValidator"),

    CLIENT_LOCATION_SOURCE_TRUSTED(
            "client-location-source-trusted",
            ValidatorType.JAVA,
            "mil.darpa.immortals.analytics.validators.ClientLocationSourceTrustedValidator"),

    CLIENT_LOCATION_SOURCE_USB(
            "client-location-source-usb",
            ValidatorType.JAVA,
            "mil.darpa.immortals.analytics.validators.ClientLocationSourceUsbValidator"),

    CLIENT_LOCATION_SOURCE_BLUETOOTH(
            "client-location-source-bluetooth",
            ValidatorType.JAVA,
            "mil.darpa.immortals.analytics.validators.ClientLocationSourceBluetoothValidator"),

    CLIENT_LOCATION_SOURCE_ANDROIDGPS(
            "client-location-source-androidgps",
            ValidatorType.JAVA,
            "mil.darpa.immortals.analytics.validators.ClientLocationSourceAndroidGpsValidator"),

    CLIENT_LOCATION_SOURCE_MANUAL(
            "client-location-source-manual",
            ValidatorType.JAVA,
            "mil.darpa.immortals.analytics.validators.ClientLocationSourceManualValidator"),

    CLIENT_LOCATION_TRUSTED(
            "client-location-trusted",
            ValidatorType.JAVA,
            "mil.darpa.immortals.analytics.validators.ClientLocationTrustedValidator"),

    BANDWIDTH_MAXIMUM_VALIDATOR(
            "bandwidth-maximum-validator",
            ValidatorType.PYTHON,
            "pymmortals.validators.bandwidth_validator.BandwidthValidator");

    public final String identifier;
    public final ValidatorType validatorType;
    public final String validatorClasspath;

    Validators(String identifier, ValidatorType validatorType, String validatorClasspath) {
        this.identifier = identifier;
        this.validatorType = validatorType;
        this.validatorClasspath = validatorClasspath;
    }
}
