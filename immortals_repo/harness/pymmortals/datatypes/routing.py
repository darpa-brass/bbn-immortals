import time
from enum import Enum
from typing import Set, List

from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testadapterstate import TestAdapterState
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testresult import TestResult
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.validationstate import ValidationState
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validationstartreturndata import \
    ValidationStartReturnData


def _get_timestamp(time_seconds: int = None) -> str:
    if time_seconds is None:
        time_seconds = time.time()
    return time.strftime("%Y-%m-%dT%H:%m:%S", time.gmtime(time_seconds)) + '.' + str(time_seconds % 1)[2:5]


# TODO: Make Enums in this file!!


class EventType(Enum):
    ANALYSIS = 'ANALYSIS'
    ERROR = 'ERROR'
    STATUS = 'STATUS'
    NETWORK_ACTIVITY = 'NETWORK_ACTIVITY'
    ARCHIVE_FILE = 'ARCHIVE_FILE'
    ARCHIVE_TO_FILE = 'ARCHIVE_TO_FILE'


class EventTag:
    def __init__(self,
                 event_type: EventType,
                 identifier: str,
                 expected_data_type: type,
                 th_status_tag: str = None,
                 th_file_log_type: str = None):
        self.event_type = event_type
        self.identifier = identifier
        self.th_status_tag = th_status_tag
        self.th_file_log_type = th_file_log_type
        self._expected_data_type = expected_data_type
        self._hash = hash(identifier)

    def __eq__(self, other):
        return self.__hash__() == other.__hash__()

    def __ne__(self, other):
        return not self.__eq__(other)

    def __hash__(self):
        return self._hash

    def display_string(self) -> str:
        # TODO: Implement
        pass


_type_tag_dictionary = {}
_all_tags: Set[EventTag] = set()


# noinspection PyClassHasNoInit
class EventTags:
    @staticmethod
    def get_all_tags_of_type(event_type: EventType) -> Set[EventTag]:
        return set(_type_tag_dictionary[event_type])

    @staticmethod
    def get_all_tags() -> Set[EventTag]:
        return set(_all_tags)

    THErrorTestDataFile = \
        EventTag(event_type=EventType.ERROR,
                 identifier='THErrorTestDataFile',
                 th_status_tag='TEST_DATA_FILE_ERROR',
                 th_file_log_type='ERROR',
                 expected_data_type=str)

    THErrorTestDataFormat = \
        EventTag(event_type=EventType.ERROR,
                 identifier='THErrorTestDataFormat',
                 th_status_tag='TEST_DATA_FORMAT_ERROR',
                 th_file_log_type='ERROR',
                 expected_data_type=str)
    THErrorDasLogFile = \
        EventTag(event_type=EventType.ERROR,
                 identifier='THErrorDasLogFile',
                 th_status_tag='DAS_LOG_FILE_ERROR',
                 th_file_log_type='ERROR',
                 expected_data_type=str)

    THErrorGeneral = \
        EventTag(event_type=EventType.ERROR,
                 identifier='THErrorGeneral',
                 th_status_tag='DAS_OTHER_ERROR',
                 th_file_log_type='ERROR',
                 expected_data_type=str)

    THStatusPerturbationDetected = \
        EventTag(event_type=EventType.STATUS,
                 identifier='THStatusPerturbationDetected',
                 th_status_tag='PERTURBATION_DETECTED',
                 th_file_log_type='INFO',
                 expected_data_type=TestAdapterState)

    THStatusMissionSuspended = \
        EventTag(event_type=EventType.STATUS,
                 identifier='THStatusMissionSuspended',
                 th_status_tag='MISSION_SUSPENDED',
                 th_file_log_type='INFO',
                 expected_data_type=TestAdapterState)

    THStatusMissionHalted = \
        EventTag(event_type=EventType.STATUS,
                 identifier='THStatusMissionHalted',
                 th_status_tag='MISSION_HALTED',
                 th_file_log_type='INFO',
                 expected_data_type=TestAdapterState)

    THStatusMissionAborted = \
        EventTag(event_type=EventType.STATUS,
                 identifier='THStatusMissionAborted',
                 th_status_tag='MISSION_ABORTED',
                 th_file_log_type='INFO',
                 expected_data_type=TestAdapterState)

    THStatusAdapting = \
        EventTag(event_type=EventType.STATUS,
                 identifier='THStatusAdapting',
                 th_status_tag='ADAPTING',
                 th_file_log_type='INFO',
                 expected_data_type=TestAdapterState)

    THStatusAdaptationCompleted = \
        EventTag(event_type=EventType.STATUS,
                 identifier='THStatusAdaptationCompleted',
                 th_status_tag='ADAPTATION_COMPLETED',
                 th_file_log_type='INFO',
                 expected_data_type=TestAdapterState)

    THStatusMissionResumed = \
        EventTag(event_type=EventType.STATUS,
                 identifier='THStatusMissionResumed',
                 th_status_tag='MISSION_RESUMED',
                 th_file_log_type='INFO',
                 expected_data_type=TestAdapterState)

    THStatusDasInfo = \
        EventTag(event_type=EventType.STATUS,
                 identifier='THStatusDasInfo',
                 th_status_tag=None,
                 th_file_log_type='INFO',
                 expected_data_type=str)

    THSubmitReady = EventTag(event_type=EventType.STATUS,
                             identifier='THSubmitReady',
                             th_status_tag=None,
                             th_file_log_type=None,
                             expected_data_type=None)

    THSubmitDone = EventTag(event_type=EventType.STATUS,
                            identifier='THSubmitDone',
                            th_status_tag=None,
                            th_file_log_type=None,
                            expected_data_type=TestAdapterState)

    DeploymentModelLoaded = EventTag(event_type=EventType.STATUS,
                                     identifier='DeploymentModelLoaded',
                                     th_status_tag=None,
                                     th_file_log_type=None,
                                     expected_data_type=GmeInterchangeFormat)

    NetworkSentPost = \
        EventTag(event_type=EventType.NETWORK_ACTIVITY,
                 identifier='NetworkSentPost',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=str)

    NetworkReceivedPostResponse = \
        EventTag(event_type=EventType.NETWORK_ACTIVITY,
                 identifier='NetworkReceivedPostResponse,',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=str)

    NetworkReceivedPost = \
        EventTag(event_type=EventType.NETWORK_ACTIVITY,
                 identifier='NetworkReceivedPost',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=str)

    NetworkAcknowledgedPost = \
        EventTag(event_type=EventType.NETWORK_ACTIVITY,
                 identifier='NetworkAcknowledgedPost',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=str)

    AnalyticsEventReceived = \
        EventTag(event_type=EventType.ANALYSIS,
                 identifier='AnalyticsEventReceived',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=str)

    AnalyticsEventServerNetworkTrafficMeasuredBytes = \
        EventTag(event_type=EventType.ANALYSIS,
                 identifier='AnalyticsEventServerNetworkTrafficMeasuredBytes',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=None)

    AnalyticsEventServerNetworkTrafficCalculatedBytesPerSec = \
        EventTag(event_type=EventType.ANALYSIS,
                 identifier='AnalyticsEventServerNetworkTrafficCalculatedBytesPerSec',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=None)

    ValidationStarted = \
        EventTag(event_type=EventType.STATUS,
                 identifier='ValidationStarted',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=ValidationStartReturnData)

    ValidationTestResultsProduced = \
        EventTag(event_type=EventType.STATUS,
                 identifier='ValidationTestResultProduced',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=List[TestResult])

    ValidationTestsFinished = \
        EventTag(event_type=EventType.STATUS,
                 identifier='ValidationTestsFinished',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=ValidationState)

    DASStatusMessage = \
        EventTag(event_type=EventType.STATUS,
                 identifier='DASStatusMessage',
                 th_status_tag=None,
                 th_file_log_type=None,
                 expected_data_type=str)


for key in list(EventTags.__dict__.keys()):
    if not key.startswith('__') and isinstance(EventTags.__dict__[key], EventTag):
        tag = EventTags.__dict__[key]  # type: EventTag

        if tag.event_type not in _type_tag_dictionary:
            _type_tag_dictionary[tag.event_type] = set()

        _type_tag_dictionary[tag.event_type].add(tag)
        _all_tags.add(tag)
