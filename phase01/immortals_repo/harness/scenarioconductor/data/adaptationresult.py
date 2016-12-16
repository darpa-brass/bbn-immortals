# noinspection PyPep8Naming
class AdaptationResult:
    """
    :type adaptationStatusValue: str
    :type audits: list[str]
    :type auditsAsString: str
    :type details: str
    :type selectedDfu: str
    :type sessionIdentifier: str
    """

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

    def to_dict(self):
        return {
            'adaptationStatusValue': self.adaptationStatusValue,
            'audits': self.audits,
            'auditsAsString': self.auditsAsString,
            'details': self.details,
            'selectedDfu': self.selectedDfu,
            'sessionIdentifier': self.sessionIdentifier
        }
