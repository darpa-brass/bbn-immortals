from integrationtest.datatypes.serializable import Serializable
from typing import List


# noinspection PyPep8Naming
class AndroidEmulatorRequirement(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 androidVersion: int = None,
                 externallyAccessibleUrls: List[str] = None,
                 superuserAccess: bool = None,
                 uploadBandwidthLimitKilobitsPerSecond: int = None):
        super().__init__()
        self.androidVersion = androidVersion
        self.externallyAccessibleUrls = externallyAccessibleUrls
        self.superuserAccess = superuserAccess
        self.uploadBandwidthLimitKilobitsPerSecond = uploadBandwidthLimitKilobitsPerSecond


# noinspection PyPep8Naming
class ChallengeProblemRequirements(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 androidEmulators: List[AndroidEmulatorRequirement] = None,
                 challengeProblemUrl: str = None):
        super().__init__()
        self.androidEmulators = androidEmulators
        self.challengeProblemUrl = challengeProblemUrl


# noinspection PyPep8Naming
class DASPrerequisites(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 cp1: ChallengeProblemRequirements = None,
                 cp2: ChallengeProblemRequirements = None,
                 cp3: ChallengeProblemRequirements = None):
        super().__init__()
        self.cp1 = cp1
        self.cp2 = cp2
        self.cp3 = cp3
