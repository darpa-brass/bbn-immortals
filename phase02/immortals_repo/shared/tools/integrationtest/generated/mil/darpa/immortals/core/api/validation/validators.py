from enum import Enum
from integrationtest.generated.mil.darpa.immortals.core.api.validation.validatortype import ValidatorType
from typing import FrozenSet


# noinspection PyPep8Naming
class Validators(Enum):
    def __init__(self, identifier: str, validatorClasspath: str, validatorType: ValidatorType):
        self.identifier: str = identifier
        self.validatorClasspath: str = validatorClasspath
        self.validatorType: ValidatorType = validatorType

    CLIENT_LOCATION_PRODUCE = (
        "client-location-produce",
        "mil.darpa.immortals.analytics.validators.ClientLocationProduceValidator",
        ValidatorType.JAVA)

    CLIENT_IMAGE_PRODUCE = (
        "client-image-produce",
        "mil.darpa.immortals.analytics.validators.ClientImageProduceValidator",
        ValidatorType.JAVA)

    CLIENT_LOCATION_SHARE = (
        "client-location-share",
        "mil.darpa.immortals.analytics.validators.ClientLocationShareValidator",
        ValidatorType.JAVA)

    CLIENT_IMAGE_SHARE = (
        "client-image-share",
        "mil.darpa.immortals.analytics.validators.ClientImageShareValidator",
        ValidatorType.JAVA)

    CLIENT_LOCATION_SOURCE_TRUSTED = (
        "client-location-source-trusted",
        "mil.darpa.immortals.analytics.validators.ClientLocationSourceTrustedValidator",
        ValidatorType.JAVA)

    CLIENT_LOCATION_SOURCE_USB = (
        "client-location-source-usb",
        "mil.darpa.immortals.analytics.validators.ClientLocationSourceUsbValidator",
        ValidatorType.JAVA)

    CLIENT_LOCATION_SOURCE_BLUETOOTH = (
        "client-location-source-bluetooth",
        "mil.darpa.immortals.analytics.validators.ClientLocationSourceBluetoothValidator",
        ValidatorType.JAVA)

    CLIENT_LOCATION_SOURCE_ANDROIDGPS = (
        "client-location-source-androidgps",
        "mil.darpa.immortals.analytics.validators.ClientLocationSourceAndroidGpsValidator",
        ValidatorType.JAVA)

    CLIENT_LOCATION_SOURCE_MANUAL = (
        "client-location-source-manual",
        "mil.darpa.immortals.analytics.validators.ClientLocationSourceManualValidator",
        ValidatorType.JAVA)

    CLIENT_LOCATION_TRUSTED = (
        "client-location-trusted",
        "mil.darpa.immortals.analytics.validators.ClientLocationTrustedValidator",
        ValidatorType.JAVA)

    BANDWIDTH_MAXIMUM_VALIDATOR = (
        "bandwidth-maximum-validator",
        "integrationtest.validators.bandwidth_validator.BandwidthValidator",
        ValidatorType.PYTHON)

    @classmethod
    def all_identifier(cls) -> FrozenSet[str]:
        return frozenset([k.identifier for k in list(cls)])

    @classmethod
    def all_validatorClasspath(cls) -> FrozenSet[str]:
        return frozenset([k.validatorClasspath for k in list(cls)])

    @classmethod
    def all_validatorType(cls) -> FrozenSet[ValidatorType]:
        return frozenset([k.validatorType for k in list(cls)])
