from adaptationresult import AdaptationResult
from validationresult import ValidationResult


# noinspection PyPep8Naming
class ValidationStateContainer:
    """
    :type status: string
    :type details: ValidationResult
    """

    def __init__(self,
                 status,
                 details):
        self.status = status
        self.details = details

    def to_dict(self):
        return {
            'status': self.status,
            'details': None if self.details is None else self.details.to_dict()
        }


# noinspection PyPep8Naming
class AdaptationStateContainer:
    """
    :type status: string
    :type details: AdaptationResult
    """

    def __init__(self,
                 status,
                 details):
        self.status = status
        self.details = details

    def to_dict(self):
        return {
            'status': self.status,
            'details': None if self.details is None else self.details.to_dict()
        }


# noinspection PyPep8Naming
class SubmissionResult:
    """
    :type identifier: string
    :type adaptation: AdaptationStateContainer
    :type validation: ValidationStateContainer
    """

    def __init__(self,
                 identifier,
                 adaptation,
                 validation
                 ):
        self.identifier = identifier
        self.adaptation = adaptation
        self.validation = validation

    def to_dict(self):
        return {
            'identifier': self.identifier,
            'adaptation': None if self.adaptation is None else self.adaptation.to_dict(),
            'validation': None if self.adaptation is None else self.validation.to_dict()
        }
