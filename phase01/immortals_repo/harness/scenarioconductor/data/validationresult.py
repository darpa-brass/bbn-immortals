# noinspection PyPep8Naming
class TestResult:
    """
    :type validatorIdentifier: str
    :type currentState: str
    :type errorMessages: list[str]
    :type detailMessages: list[str]
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
                validatorIdentifier=d['validatorIdentifier'],
                currentState=d['currentState'],
                errorMessages=d['errorMessages'],
                detailMessages=d['detailMessages']
        )

    def __init__(self,
                 validatorIdentifier,
                 currentState,
                 errorMessages,
                 detailMessages
                 ):
        self.validatorIdentifier = validatorIdentifier
        self.currentState = currentState
        self.errorMessages = errorMessages
        self.detailMessages = detailMessages

    def to_dict(self):
        return {
            'validatorIdentifier': self.validatorIdentifier,
            'currentState': self.currentState,
            'errorMessages': self.errorMessages,
            'detailMessages': self.detailMessages
        }


# noinspection PyPep8Naming
class ValidationResult:
    """
    :type testDurationMS: long
    :type results TestResult
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
                testDurationMS=d['testDurationMS'],
                results=map(lambda c: TestResult.from_dict(c), d['results'])
        )

    def __init__(self,
                 testDurationMS,
                 results
                 ):
        self.testDurationMS = testDurationMS
        self.results = results

    def to_dict(self):
        return {
            'testDurationMS': self.testDurationMS,
            'results': None if self.results is None else map(lambda c: TestResult.to_dict(c), self.results)
        }
