#!/usr/bin/env python2
import json
import os
import shutil
import time
import traceback

from .reportinginterface import ReportingInterface
from .. import immortalsglobals as ig
from .. import threadprocessrouter as tpr
from ..utils import path_helper as ph


def get_timestamp(time_seconds=None):
    if time_seconds is None:
        time_seconds = time.time()
    return time.strftime("%Y-%m-%dT%H:%m:%S", time.gmtime(time_seconds)) + '.' + str(time_seconds % 1)[2:5]


class AbstractReporter(ReportingInterface):
    def adapting(self, message, event_time_s=None):
        self._submit_status(status='ADAPTING', message=message, event_time_s=event_time_s)

    def mission_resumed(self, message, event_time_s=None):
        self._submit_status(status='MISSION_RESUMED', message=message, event_time_s=event_time_s)

    def mission_suspended(self, message, event_time_s=None):
        self._submit_status(status='MISSION_SUSPENDED', message=message, event_time_s=event_time_s)

    def adaptation_completed(self, message, event_time_s=None):
        self._submit_status(status='ADAPTATION_COMPLETED', message=message, event_time_s=event_time_s)

    def __init__(self, log_filepath, artifact_dirpath, log_error_to_net=False):
        self.log_filepath = log_filepath
        self.artifact_dirpath = artifact_dirpath

        if not os.path.exists(artifact_dirpath):
            os.makedirs(artifact_dirpath)

        self._log = tpr.get_logging_endpoint(log_filepath=log_filepath, das_log_file_error=log_error_to_net)

        self._is_ready = False

    def _submit_error(self, error, message, event_time_s=None):
        raise NotImplementedError

    def _submit_ready(self, event_time_s=None):
        raise NotImplementedError

    def _submit_status(self, status, message, event_time_s=None):
        raise NotImplementedError

    def _submit_action(self, action, arguments, event_time_s=None):
        raise NotImplementedError

    def log_das_info(self, message, event_time_s=None):
        self._log.write(json.dumps({
            'TIME': get_timestamp(event_time_s),
            'TYPE': 'INFO',
            'MESSAGE': message
        }))

    def log_das_error(self, message, event_time_s=None):
        self._log.write(json.dumps({
            'TIME': get_timestamp(event_time_s),
            'TYPE': 'RUNTIME_ERROR' if self._is_ready else 'STARTUP_ERROR',
            'MESSAGE': message
        }))

    def submit_action(self, action, arguments, event_time_s=None):
        return self._submit_action(action=action, arguments=arguments, event_time_s=event_time_s)

    def error_test_data_file(self, message, event_time_s=None):
        self._submit_error(error='TEST_DATA_FILE_ERROR', message=message, event_time_s=event_time_s)

    def error_test_data_format(self, message, event_time_s=None):
        self._submit_error(error='TEST_DATA_FORMAT_ERROR', message=message, event_time_s=event_time_s)

    def error_das_log_file(self, message, event_time_s=None):
        self._submit_error(error='DAS_LOG_FILE_ERROR', message=message, event_time_s=event_time_s)

    def error_das(self, message, event_time_s=None):
        e0 = None
        tb0 = None
        e1 = None
        tb1 = None

        try:
            self.log_das_error(message=message, event_time_s=event_time_s)
        except Exception as e:
            e0 = e
            tb0 = traceback.format_exc()

        try:
            self._submit_error(error='DAS_OTHER_ERROR', message=message, event_time_s=event_time_s)
        except Exception as e:
            e1 = e
            tb1 = traceback.format_exc()

        if e0 is not None:
            self._submit_error(error='DAS_OTHER_ERROR', message=tb0, event_time_s=event_time_s)
            raise e0
        elif e1 is not None:
            self.log_das_error(message=tb1, event_time_s=event_time_s)
            raise e1

    def das_ready(self, event_time_s=None):
        self._submit_ready(event_time_s)
        self._is_ready = True

    def perturbation_detected(self, message, event_time_s=None):
        self._submit_status(status='PERTURBATION_DETECTED', message=message, event_time_s=event_time_s)

    def mission_halted(self, message, event_time_s=None):
        # Since LL may poweroff right after receiving the message
        tpr.flush_logging_endpoints()
        self._submit_status(status='MISSION_HALTED', message=message, event_time_s=event_time_s)

    def mission_aborted(self, message, event_time_s=None):
        # Since LL may poweroff right after receiving the message
        tpr.flush_logging_endpoints()
        self._submit_status(status='MISSION_ABORTED', message=message, event_time_s=event_time_s)

    def mission_test_error(self, message, event_time_s=None):
        # Since LL may poweroff right after receiving the message
        tpr.flush_logging_endpoints()
        self._submit_status(status='TEST_ERROR', message=message, event_time_s=event_time_s)

    def artifact_file(self, source_filepath, target_subpath=None):
        source = ph(True, ig.IMMORTALS_ROOT, source_filepath)

        if target_subpath is None:
            target = ph(False, self.artifact_dirpath, source_filepath[source_filepath.rfind('/') + 1:])
            # target = ph(False, .artifactRoot, source[source.rfind('/') + 1:])
        else:
            target_path = os.path.join(self.artifact_dirpath, target_subpath)
            if not os.path.exists(target_path):
                os.makedirs(target_path)

            target = ph(False,
                        self.artifact_dirpath,
                        os.path.join(target_path,
                                     source_filepath[source_filepath.rfind('/') + 1:]))

        shutil.copyfile(source, target)

    def write_artifact_to_file(self, str_to_write, target_subpath, clobber_existing=True):
        """
        :param str str_to_write:
        :param str target_subpath:
        :param bool clobber_existing:
        """
        target_path = os.path.join(self.artifact_dirpath, target_subpath)
        if not os.path.exists(target_path):
            os.makedirs(target_path[target_path.rfind('/') + 1:])

        f = open(target_path, 'w' if clobber_existing else 'a')
        f.write(str_to_write)
        f.flush()
        f.close()
