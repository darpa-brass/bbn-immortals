# noinspection PyPep8Naming
class AdaptationResult:
    """
    :type adaptationStatusValue: str
    :type details: str
    :type selectedDfu: str
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
                d['adaptationStatusValue'],
                d['details'],
                d['selectedDfu']
        )

    def __init__(self,
                 adaptationStatusValue,
                 details,
                 selectedDfu
                 ):
        self.adaptationStatusValue = adaptationStatusValue
        self.details = details
        self.selectedDfu = selectedDfu

    def to_dict(self):
        return {
            'adaptationStatusValue': self.adaptationStatusValue,
            'details': self.details,
            'selectedDfu': self.selectedDfu
        }
