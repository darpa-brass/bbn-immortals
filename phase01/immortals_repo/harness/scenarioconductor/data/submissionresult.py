from adaptationresult import AdaptationResult
from validationresult import ValidationResult


# noinspection PyPep8Naming
class SubmissionResult:
    """
    :type identifier: string
    :type synthesisFinished: bool
    :type validationFinished: bool
    :type adaptationResult: AdaptationResult
    :type validationResult: ValidationResult
    """

    def __init__(self,
                 identifier,
                 synthesisFinished,
                 validationFinished,
                 adaptationResult,
                 validationResult
                 ):
        self.identifier = identifier
        self.synthesisFinished = synthesisFinished
        self.validationFinished = validationFinished
        self.adaptationResult = adaptationResult
        self.validationResult = validationResult

    def to_dict(self):
        return {
            'identifier': self.identifier,
            'synthesisFinished': self.synthesisFinished,
            'validationFinished': self.validationFinished,
            'adaptationResult': None if self.adaptationResult is None else self.adaptationResult.to_dict(),
            'validationResult': None if self.validationResult is None else self.validationResult.to_dict()
        }
