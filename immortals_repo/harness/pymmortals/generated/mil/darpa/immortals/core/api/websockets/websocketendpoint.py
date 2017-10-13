from enum import Enum
from pymmortals.generated.mil.darpa.immortals.core.api.applications.applicationdeploymentdetails import ApplicationDeploymentDetails
from pymmortals.generated.mil.darpa.immortals.core.api.validation.results.validationresults import ValidationResults
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validationstartdata import ValidationStartData
from pymmortals.generated.mil.darpa.immortals.core.api.websockets.createapplicationinstancedata import CreateApplicationInstanceData
from pymmortals.generated.mil.darpa.immortals.core.api.websockets.websocketack import WebsocketAck
from typing import FrozenSet
from typing import Type


# noinspection PyPep8Naming
class WebsocketEndpoint(Enum):
    def __init__(self, ackType: Type, path: str, postType: Type):
        self.ackType: Type = ackType
        self.path: str = path
        self.postType: Type = postType

    VALIDATION_START = (
        WebsocketAck,
        "/validation/start",
        ValidationStartData)

    VALIDATION_STOP = (
        WebsocketAck,
        "/validation/stop",
        str)

    VALIDATION_RESULTS = (
        WebsocketAck,
        "/validation/results",
        ValidationResults)

    SOURCECOMPOSER_CREATE_APPLICATION_INSTANCE = (
        ApplicationDeploymentDetails,
        "/applications/createInstance",
        CreateApplicationInstanceData)

    @classmethod
    def all_ackType(cls) -> FrozenSet[Type]:
        return frozenset([k.ackType for k in list(cls)])

    @classmethod
    def all_path(cls) -> FrozenSet[str]:
        return frozenset([k.path for k in list(cls)])

    @classmethod
    def all_postType(cls) -> FrozenSet[Type]:
        return frozenset([k.postType for k in list(cls)])