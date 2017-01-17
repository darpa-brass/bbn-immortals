from ..data.base.adaptationresult import AdaptationResult
from ..data.base.serializable import Serializable
from ..data.base.validationresults import TestResult


# noinspection PyPep8Naming
class Configuration(Serializable):
    """
    :type minimumTestDurationMS: int
    """

    _types = {}

    def __init__(self, minimumTestDurationMS):
        self.minimumTestDurationMS = minimumTestDurationMS


# noinspection PyClassHasNoInit
class Status:
    PENDING = 'PENDING'
    NOT_APPLICABLE = 'NOT_APPLICABLE'
    RUNNING = 'RUNNING'
    SUCCESS = 'SUCCESS'
    FAILURE = 'FAILURE'


# noinspection PyPep8Naming
class AdaptationState(Serializable):
    """
    :type adaptationStatus: str
    :type details: AdaptationResult
    """

    _types = {
        'details': AdaptationResult
    }

    def __init__(self,
                 adaptationStatus,
                 details):
        self.adaptationStatus = adaptationStatus
        self.details = details


# noinspection PyPep8Naming
class TestDetails(Serializable):
    """
    :type testIdentifier: str
    :type expectedStatus: str
    :type actualStatus: str
    :type details: TestResult
    """

    _types = {
        'details': TestResult
    }

    def __init__(self, testIdentifier, expectedStatus, actualStatus, details):
        self.testIdentifier = testIdentifier
        self.expectedStatus = expectedStatus
        self.actualStatus = actualStatus
        self.details = details


# noinspection PyPep8Naming
class ValidationState(Serializable):
    """
    :type executedTests: list[TestDetails]
    :type overallIntentStatus: str
    """

    _types = {
        'executedTests': (list, TestDetails)
    }

    def __init__(self, executedTests, overallIntentStatus):
        self.executedTests = executedTests
        self.overallIntentStatus = overallIntentStatus


# noinspection PyPep8Naming
class AnalyticsEvent(Serializable):
    """
    :type type: str
    :type eventSource: str
    :type eventTime: long
    :type eventRemoteSource: str
    :type dataType: str
    :type eventId: long
    :type data: str
    """

    _types = {}

    # noinspection PyShadowingBuiltins
    def __init__(self, type, eventSource, eventTime, eventRemoteSource, dataType, eventId, data):
        self.type = type
        self.eventSource = eventSource
        self.eventTime = eventTime
        self.eventRemoteSource = eventRemoteSource
        self.dataType = dataType
        self.eventId = eventId
        self.data = data


# noinspection PyPep8Naming
class TestAdapterState(Serializable):
    """
    :type identifier: string
    :type adaptation: AdaptationState
    :type validation: ValidationState
    :type rawLogData: list[AnalyticsEvent]
    """

    _types = {
        'adaptation': AdaptationState,
        'validation': ValidationState,
        'rawLogData': (list, AnalyticsEvent)
    }

    def __init__(self,
                 identifier,
                 adaptation,
                 validation,
                 rawLogData
                 ):
        self.identifier = identifier
        self.adaptation = adaptation
        self.validation = validation
        self.rawLogData = rawLogData
