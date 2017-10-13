"""
The API data sent to the Test Harness must adhere to
"""

from pymmortals.datatypes.deployment_model import LLP1Input
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testadapterstate import TestAdapterState
from pymmortals.utils import get_th_timestamp
from .serializable import Serializable


# noinspection PyPep8Naming
class Configuration(Serializable):
    _validator_values = {}

    def __init__(self,
                 minimumTestDurationMS: int):
        super().__init__()
        self.minimumTestDurationMS = minimumTestDurationMS


# noinspection PyPep8Naming
class LLDasErrorEvent(Serializable):
    _validator_values = {}

    # noinspection PyPep8Naming
    def __init__(self,
                 ERROR: str,
                 MESSAGE: str,
                 TIME: str = None):
        super().__init__()
        self.ERROR = ERROR
        self.MESSAGE = MESSAGE
        self.TIME = TIME if TIME is not None else get_th_timestamp()


# noinspection PyPep8Naming
class LLDasReady(Serializable):
    _validator_values = {}

    def __init__(self,
                 TIME: str = None):
        super().__init__()
        self.TIME = TIME if TIME is not None else get_th_timestamp()


# noinspection PyPep8Naming
class LLDasStatusEvent(Serializable):
    _validator_values = {}

    def __init__(self,
                 STATUS: str,
                 MESSAGE: str,
                 TIME: str = None):
        super().__init__()
        self.STATUS = STATUS
        self.MESSAGE = MESSAGE
        self.TIME = TIME if TIME is not None else get_th_timestamp()


# noinspection PyPep8Naming
class LLTestActionDone(Serializable):
    _validator_values = {}

    def __init__(self,
                 ARGUMENTS: TestAdapterState,
                 TIME: str = None):
        super().__init__()
        self.ARGUMENTS = ARGUMENTS
        self.TIME = TIME if TIME is not None else get_th_timestamp()


# noinspection PyPep8Naming
class LLTestActionSubmission(Serializable):
    _validator_values = {}

    def __init__(self,
                 ARGUMENTS: LLP1Input,
                 TIME: str = None):
        super().__init__()
        self.ARGUMENTS = ARGUMENTS
        self.TIME = TIME if TIME is not None else get_th_timestamp()


# noinspection PyPep8Naming
class LLTestActionResult(Serializable):
    _validator_values = {}

    def __init__(self,
                 RESULT: TestAdapterState,
                 TIME: str = None):
        super().__init__()
        self.RESULT = RESULT
        self.TIME = TIME if TIME is not None else get_th_timestamp()


# noinspection PyPep8Naming
class LLLogDasError(Serializable):
    _validator_values = {}

    def __init__(self,
                 TYPE: str,
                 MESSAGE: str,
                 TIME: str = None):
        super().__init__()
        self.TYPE = TYPE
        self.MESSAGE = MESSAGE
        self.TIME = TIME if TIME is not None else get_th_timestamp()


# noinspection PyPep8Naming
class LLLogDasInfo(Serializable):
    _validator_values = {}

    def __init__(self,
                 MESSAGE: str,
                 TIME: str = None):
        super().__init__()
        self.TYPE = 'INFO'
        self.MESSAGE = MESSAGE
        self.TIME = TIME if TIME is not None else get_th_timestamp()
