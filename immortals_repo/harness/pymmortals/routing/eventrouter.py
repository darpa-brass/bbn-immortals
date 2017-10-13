import json
import logging
import os
import shutil
import time
import traceback
from abc import abstractmethod
from threading import RLock
from typing import Dict, IO, Set, Callable, Type

from pymmortals import threadprocessrouter as tpr
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.routing import EventTag, EventTags, EventType
from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.analyticsevent import AnalyticsEvent
from pymmortals.utils import path_helper


# noinspection PyUnusedLocal
def _pretty_print_to_string(event_tag: EventTag, data: Serializable or Dict or str) -> str:
    if isinstance(data, Serializable):
        return data.to_json_str_pretty()

    elif isinstance(data, dict) or isinstance(data, list):
        return json.dumps(data)

    elif isinstance(data, str) or isinstance(data, int) or isinstance(data, bool) or isinstance(data, float):
        return str(data)

    else:
        raise Exception('Unexpected data type "' + type(data) + '"!')


class EventTransformer:
    @staticmethod
    @abstractmethod
    def transform(event_tag: EventTag, data: str):
        raise NotImplementedError


# noinspection PyClassHasNoInit
class EventReceiverInterface:
    def receive_event(self, event_tag: EventTag, data: object):
        raise NotImplementedError


class _FileLogger(EventReceiverInterface):
    _init_lock: RLock = RLock()
    _filepath_file_map: Dict[str, IO] = {}
    _filepath_lock_map: Dict[str, RLock] = {}

    @staticmethod
    def flush():
        for f in list(_FileLogger._filepath_file_map.values()):
            f.flush()

    def __init__(self, filepath: str, transformer: EventTransformer = None):
        """
        The transformer must be a function that has the same method signature as
         EventReceiverInterface.receive_event and returns the kwargs to be put into a method with the same signature.
        """
        with _FileLogger._init_lock:
            afp = os.path.abspath(filepath)
            if afp in _FileLogger._filepath_file_map:
                the_file = _FileLogger._filepath_file_map[afp]
                the_lock = _FileLogger._filepath_lock_map[afp]
            else:
                the_file = open(afp, 'w')
                the_lock = RLock()
                _FileLogger._filepath_file_map[afp] = the_file
                _FileLogger._filepath_lock_map[afp] = the_lock

        self._lock: RLock = the_lock
        self._file: IO = the_file

        self.transformer: Callable = None if transformer is None else transformer.transform

    def receive_event(self, event_tag: EventTag, data: object):
        """
        :type event_tag: EventTag
        :type data: object
        """
        with self._lock:
            if self.transformer is None:
                self._file.write(_pretty_print_to_string(event_tag=event_tag, data=data))
            else:
                self._file.write(_pretty_print_to_string(**self.transformer(event_tag, data)))


# noinspection PyClassHasNoInit
class EventRouter:
    def __init__(self):
        self._lock: RLock = RLock()
        self._raw_event_tag_subscriptions: Dict[EventTag, Set] = {k: set() for k in EventTags.get_all_tags()}
        self._raw_event_type_subscriptions: Dict[EventType, Set] = {k: set() for k in EventType}
        self._event_tag_submission_listeners: Dict[EventTag, Set] = {}
        self._file_loggers: Set[_FileLogger] = set()

    def submit(self, event_tag: EventTag, data: object):
        self._submit(event_tag=event_tag, data=data, recurse_errors=True)

    def submit_asynchronously(self, event_tag: EventTag, data: object):
        tpr.start_thread(
            thread_method=self._submit,
            thread_args=[event_tag, data, True])

    # noinspection PyBroadException,PyPep8
    def _submit(self, event_tag: EventTag, data: object, recurse_errors: bool):
        with self._lock:
            if get_configuration().debug.routing:
                if isinstance(data, Serializable):
                    msg = 'EVENT ' + event_tag.identifier + ' submitted with data: \n' + data.to_json_str_pretty()
                elif isinstance(data, dict):
                    msg = 'EVENT ' + event_tag.identifier + ' submitted with data: \n' + json.dumps(data,
                                                                                                    indent=4,
                                                                                                    separators=(
                                                                                                        ',', ': '))
                else:
                    msg = 'EVENT ' + event_tag.identifier + ' submitted with data: ' + str(data)

                logging.debug(msg)
            submission_listeners = self._event_tag_submission_listeners

        # Errors need to bubble up somehow at all cost, so care must be taken to make sure all listeners execute
        if event_tag.event_type == EventType.ERROR:
            try:
                for l in submission_listeners[event_tag]:
                    try:
                        l(event_tag, data)
                        _FileLogger.flush()
                    except Exception:
                        if recurse_errors:
                            try:
                                self._submit(event_tag=EventTags.THErrorGeneral,
                                             data=traceback.format_exc(),
                                             recurse_errors=False)
                            except:
                                pass
            except:
                pass

        else:
            if event_tag in submission_listeners:
                for l in submission_listeners[event_tag]:
                    if get_configuration().debug.routing:
                        logging.debug('    SENDING TO ' + str(l))
                        l(event_tag, data)
                        logging.debug('    ' + str(l) + ' Finished')
                    else:
                        l(event_tag, data)

    def _regenerate_listener_map(self):
        # First, copy all the tag listeners

        event_tag_submission_listeners = {}
        for tag in self._raw_event_tag_subscriptions:
            event_tag_submission_listeners[tag] = set(self._raw_event_tag_subscriptions[tag])

        # Then, for each event tag
        for event_tag in self._raw_event_tag_subscriptions:
            # Iterate through the types
            for event_type in self._raw_event_type_subscriptions:
                # And if the types match, add the type listeners for the tag
                if event_tag.event_type == event_type:
                    event_tag_submission_listeners[event_tag] = \
                        self._raw_event_tag_subscriptions[event_tag].union(
                            self._raw_event_type_subscriptions[event_type])

        self._event_tag_submission_listeners = event_tag_submission_listeners

    def subscribe_listener(self, event_tag_or_type: EventTag or EventType, listener: object):
        with self._lock:
            if isinstance(event_tag_or_type, EventType):
                self._raw_event_type_subscriptions[event_tag_or_type].add(listener)

            elif isinstance(event_tag_or_type, EventTag):
                self._raw_event_tag_subscriptions[event_tag_or_type].add(listener)

            self._regenerate_listener_map()

    def unsubscribe_listener(self, event_tag_or_type: EventTag or EventType, listener: object):
        with self._lock:
            if isinstance(event_tag_or_type, EventType):
                if listener in self._raw_event_type_subscriptions[event_tag_or_type]:
                    self._raw_event_type_subscriptions[event_tag_or_type].remove(listener)

            elif isinstance(event_tag_or_type, EventTag):
                if listener in self._raw_event_tag_subscriptions[event_tag_or_type]:
                    self._raw_event_tag_subscriptions[event_tag_or_type].remove(listener)

            self._regenerate_listener_map()

    def _log_event_to_logging_dot_info(self, event_tag: EventTag, data: object):
        with self._lock:
            logging.info(_pretty_print_to_string(event_tag=event_tag, data=data))

    def set_log_events_to_file(self, event_tag_or_type: EventTag or EventType,
                               filepath: str, transformer: Type[EventTransformer] = None):
        with self._lock:

            # TODO: There is nothing here to prevent duplicate logging to files!
            fp = os.path.abspath(filepath)
            file_logger = _FileLogger(filepath=fp, transformer=transformer)
            self._file_loggers.add(file_logger)

            if isinstance(event_tag_or_type, EventType):
                self._raw_event_type_subscriptions[event_tag_or_type].add(file_logger.receive_event)

            elif isinstance(event_tag_or_type, EventTag):
                self._raw_event_tag_subscriptions[event_tag_or_type].add(file_logger.receive_event)

            else:
                raise Exception(
                    'Object "' + str(event_tag_or_type) + '" is not a valid routing EventTag or EventType!')

            self._regenerate_listener_map()

    def set_log_events_to_stdout(self, event_tag_or_type: EventTag or EventType):
        with self._lock:
            if isinstance(event_tag_or_type, EventType):
                self._raw_event_type_subscriptions[event_tag_or_type].add(EventRouter._log_event_to_logging_dot_info)
                pass

            elif isinstance(event_tag_or_type, EventTag):
                self._raw_event_tag_subscriptions[event_tag_or_type].add(EventRouter._log_event_to_logging_dot_info)

            else:
                raise Exception(
                    'Object "' + str(event_tag_or_type) + '" is not a valid routing EventTag or EventType!')

            self._regenerate_listener_map()

    # noinspection PyMethodMayBeStatic
    def archive_file(self, source_filepath: str, target_subpath: str = None):
        artifact_root = get_configuration().artifactRoot
        source = path_helper(True, get_configuration().immortalsRoot, source_filepath)

        if target_subpath is None:
            target = path_helper(False, artifact_root, source_filepath[source_filepath.rfind('/') + 1:])
        else:
            target_path = os.path.join(artifact_root, target_subpath)
            if not os.path.exists(target_path):
                os.makedirs(target_path)

            target = path_helper(False,
                                 artifact_root,
                                 os.path.join(target_path, source_filepath[source_filepath.rfind('/') + 1:]))

        shutil.copyfile(source, target)

    # noinspection PyMethodMayBeStatic
    def archive_to_file(self, str_to_write: str, target_subpath: str, clobber_existing: bool = True):
        target_path = os.path.join(get_configuration().artifactRoot, target_subpath)
        full_target_path = target_path[:target_path.rfind('/')]
        if not os.path.exists(full_target_path):
            os.makedirs(full_target_path)

        f = open(target_path, 'w' if clobber_existing else 'a')
        f.write(str_to_write)
        f.flush()
        f.close()

    def log_time_delta_0(self, event_source, event):
        ae = AnalyticsEvent(
            type="timeMarkerStart",
            eventSource=event_source,
            eventTime=time.time() * 1000,
            eventRemoteSource='global',
            dataType='java.lang.Long',
            eventId=event,
            data=''
        )
        self.submit(EventTags.AnalyticsEventReceived, ae)

    def log_time_delta_1(self, event_source, event):
        ae = AnalyticsEvent(
            type="timeMarkerEnd",
            eventSource=event_source,
            eventTime=time.time() * 1000,
            eventRemoteSource='global',
            dataType='java.lang.Long',
            eventId=event,
            data=''
        )
        self.submit(EventTags.AnalyticsEventReceived, ae)


class AbstractEventReceiver:
    def _receive_event(self, event_tag: EventTag, data: object):
        raise NotImplementedError

    def __init__(self, event_router: EventRouter):
        self._event_router: EventRouter = event_router
        self._monitored_events: Set[EventTag or EventType] = set()
        self._subscription_lock: RLock = RLock()

    def disconnect(self):
        with self._subscription_lock:
            for tag in self._monitored_events:
                self._event_router.unsubscribe_listener(tag, self._receive_event)

            self._monitored_events = set()

    def subscribe(self, event_tag_or_type: EventTag or EventType):
        with self._subscription_lock:
            if event_tag_or_type not in self._monitored_events:
                self._monitored_events.add(event_tag_or_type)
                self._event_router.subscribe_listener(event_tag_or_type=event_tag_or_type,
                                                      listener=self._receive_event)
