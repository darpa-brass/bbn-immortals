import logging
from typing import FrozenSet, Type

from pymmortals.datatypes.interfaces import ValidatorInterface
from pymmortals.datatypes.intermediary.deploymentmodelproperty import \
    DeploymentModelProperty
from pymmortals.datatypes.intermediary.deploymentmodelresource import \
    DeploymentModelResource
from pymmortals.datatypes.serializable import Serializable, ValuedEnum
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validators import Validators
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validatortype import ValidatorType
from pymmortals.utils import load_class_by_classpath

_logger = logging.getLogger('validation')


def load_python_validator_class(validator: Validators) -> Type[ValidatorInterface]:
    if validator.validatorType == ValidatorType.PYTHON:
        clazz = load_class_by_classpath(validator.validatorClasspath)
        assert (issubclass(clazz, ValidatorInterface))
        return clazz

    elif validator.validatorType == ValidatorType.JAVA:
        raise Exception('Java validators not yet supported!: Validator: "' + validator.validatorType + '"!')

    else:
        raise Exception('Unexpected validator type "' + validator.validatorType + '"!')


# noinspection PyPep8Naming
class Coordinates(Serializable):
    @classmethod
    def _validator_values(cls):
        return dict()

    def __init__(self,
                 latitude: float,
                 longitude: float,
                 altitude: float,
                 accuracy: float,
                 acquisitionTime: float,
                 hasAltitude: bool,
                 hasAccuracy: bool,
                 provider: str):
        super().__init__()
        self.latitude = latitude
        self.longitude = longitude
        self.altitude = altitude
        self.accuracy = accuracy
        self.acquisitionTime = acquisitionTime
        self.hasAltitude = hasAltitude
        self.hasAccuracy = hasAccuracy
        self.provider = provider


# TODO: Change to enum!

class AnalyticsEventType(ValuedEnum):
    MY_IMAGE_SENT = 'MyImageSent',
    FIELD_IMAGE_RECEIVED = 'FieldImageReceived',
    MY_LOCATION_PRODUCED = 'MyLocationProduced',
    FIELD_LOCATION_UPDATED = 'FieldLocationUpdated',
    CLIENT_START = 'ClientStart',
    CLIENT_SHUTDOWN = 'ClientShutdown',
    DFU_MISSMATCH_ERROR = 'DfuMissmatchError',
    UNEXEPCTED_IGNORABLE_ERROR = 'UnexepctedIgnorableError',
    TOOLING_VALIDATION_SERVER_STARTED = 'Tooling_ValidationServerStarted',
    TOOLING_VALIDATION_SERVER_STOPPED = 'Tooling_ValidationServerStopped',
    TOOLING_VALIDATION_SERVER_CLIENT_CONNECTED = 'Tooling_ValidationServerClientConnected',
    TOOLING_VALIDATION_SERVER_CLIENT_DISCONNECTED = 'Tooling_ValidationServerClientDisconnected',
    TOOLING_VALIDATION_FINISHED = 'Tooling_ValidationFinished',
    ANALYSIS_EVENT_OCCURRED = 'Analysis_EventOccurred'

    @property
    def tag(self) -> str:
        return self._value_

    @classmethod
    def all_tags(cls) -> FrozenSet[str]:
        return cls._values()


"""
This indicates the tests that must have a specified state in order to pass an intent check.
"""
intent_satisfaction_tests = {
    # The baseline set of tests that must pass in order to pass the basic mission intent of sending LatestSA
    'baseline': frozenset([
        Validators.CLIENT_LOCATION_PRODUCE,
        Validators.CLIENT_LOCATION_SHARE,
        Validators.CLIENT_IMAGE_PRODUCE,
        Validators.CLIENT_IMAGE_SHARE,
        Validators.BANDWIDTH_MAXIMUM_VALIDATOR
    ]),
    # Additional properties that can be specified to add new mission requirements to the scenario
    DeploymentModelProperty.trustedLocations: frozenset([
        Validators.CLIENT_LOCATION_TRUSTED
    ])
}

# Lists the mandatory requirements to pass each resource usage validator
resource_test_dependencies = {
    Validators.CLIENT_LOCATION_SOURCE_TRUSTED: frozenset(
        [DeploymentModelResource.gpsSatellites, DeploymentModelResource.usb]),
    Validators.CLIENT_LOCATION_SOURCE_USB: frozenset(
        [DeploymentModelResource.gpsSatellites, DeploymentModelResource.usb]),
    Validators.CLIENT_LOCATION_SOURCE_BLUETOOTH: frozenset(
        [DeploymentModelResource.gpsSatellites, DeploymentModelResource.bluetooth]),
    Validators.CLIENT_LOCATION_SOURCE_ANDROIDGPS: frozenset(
        [DeploymentModelResource.gpsSatellites, DeploymentModelResource.internalGps]),
    Validators.CLIENT_LOCATION_SOURCE_MANUAL: frozenset([DeploymentModelResource.userInterface])
}

"""
Test sets in which if one passes, the others must fail
"""
mutually_exclusive_validator_sets = {
    'client-location-source': frozenset([
        Validators.CLIENT_LOCATION_SOURCE_TRUSTED,
        Validators.CLIENT_LOCATION_SOURCE_USB,
        Validators.CLIENT_LOCATION_SOURCE_BLUETOOTH,
        Validators.CLIENT_LOCATION_SOURCE_ANDROIDGPS,
        Validators.CLIENT_LOCATION_SOURCE_MANUAL
    ])
}
