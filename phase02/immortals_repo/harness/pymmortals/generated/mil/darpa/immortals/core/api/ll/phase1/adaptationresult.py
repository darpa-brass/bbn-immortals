from pymmortals.datatypes.serializable import Serializable
from typing import List


# noinspection PyPep8Naming
class AdaptationResult(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptationStatusValue: str = None,
                 audits: List[str] = None,
                 auditsAsString: str = None,
                 details: str = None,
                 selectedDfu: str = None,
                 sessionIdentifier: str = None):
        super().__init__()
        self.adaptationStatusValue = adaptationStatusValue
        self.audits = audits
        self.auditsAsString = auditsAsString
        self.details = details
        self.selectedDfu = selectedDfu
        self.sessionIdentifier = sessionIdentifier
