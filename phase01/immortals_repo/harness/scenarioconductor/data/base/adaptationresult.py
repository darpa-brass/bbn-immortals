from .serializable import Serializable


# noinspection PyPep8Naming
class AdaptationResult(Serializable):
    """
    :type adaptationStatusValue: str
    :type audits: list[str]
    :type auditsAsString: str
    :type details: str
    :type selectedDfu: str
    :type sessionIdentifier: str
    """

    _types = {}

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self,
                 adaptationStatusValue,
                 audits,
                 auditsAsString,
                 details,
                 selectedDfu,
                 sessionIdentifier
                 ):
        self.adaptationStatusValue = adaptationStatusValue
        self.audits = audits
        self.auditsAsString = auditsAsString
        self.details = details
        self.selectedDfu = selectedDfu
        self.sessionIdentifier = sessionIdentifier
