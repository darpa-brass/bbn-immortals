from .serializable import Serializable


# noinspection PyPep8Naming
class TestResult(Serializable):
    """
    :type validatorIdentifier: str
    :type currentState: str
    :type errorMessages: list[str]
    :type detailMessages: list[str]
    """

    _types = {}

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


# noinspection PyPep8Naming
class ValidationResults(Serializable):
    """
    :type testDurationMS: long
    :type results list[TestResult]
    """

    _types = {
        'results': (list, TestResult)
    }

    def __init__(self,
                 testDurationMS,
                 results
                 ):
        self.testDurationMS = testDurationMS
        self.results = results
