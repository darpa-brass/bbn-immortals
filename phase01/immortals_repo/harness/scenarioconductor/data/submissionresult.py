from adaptationresult import AdaptationResult
from validationresult import ValidationResult


# noinspection PyPep8Naming
class SubmissionResult:
    """
    :type identifier: string
    :type adaptationFinished: bool
    :type validationFinished: bool
    :type adaptationResult: AdaptationResult
    :type validationResult: ValidationResult
    """

    def __init__(self,
                 identifier,
                 adaptationFinished,
                 validationFinished,
                 adaptationResult,
                 validationResult
                 ):
        self.identifier = identifier
        self.adaptationFinished = adaptationFinished
        self.validationFinished = validationFinished
        self.adaptationResult = adaptationResult
        self.validationResult = validationResult

    def to_dict(self):
        return {
            'identifier': self.identifier,
            'adaptationFinished': self.adaptationFinished,
            'validationFinished': self.validationFinished,
            'adaptationResult': None if self.adaptationResult is None else self.adaptationResult.to_dict(),
            'validationResult': None if self.validationResult is None else self.validationResult.to_dict()
        }
