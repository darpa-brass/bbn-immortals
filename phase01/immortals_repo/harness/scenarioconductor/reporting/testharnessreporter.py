#!/usr/bin/env python2

import logging
import os

from .abstractreporter import get_timestamp, AbstractReporter
from .. import threadprocessrouter as tpr
from ..data.base.testharnessconfiguration import TestHarnessConfiguration
from ..packages import commentjson as json
from ..packages import requests
from ..threadprocessrouter import LoggingEndpoint

_JSON_HEADERS = {'Content-Type': 'application/json'}


class TestHarnessReporter(AbstractReporter):
    """
    :type _urlTemplate: str
    :type _offline_endpoint LoggingEndpoint
    """

    def __init__(self, log_filepath, artifact_dirpath, host_config):
        """
        :param str log_filepath:
        :param str artifact_dirpath:
        :param TestHarnessConfiguration host_config:
        """

        if not os.path.exists(artifact_dirpath):
            os.makedirs(artifact_dirpath)

        self._offline_endpoint = tpr.get_logging_endpoint(
            os.path.join(artifact_dirpath, 'network_log.txt'), True)  # type: tpr.LoggingEndpoint

        self._urlTemplate = host_config.protocol + host_config.url + ':' + str(host_config.port) + '/{path}'

        AbstractReporter.__init__(self, log_filepath, artifact_dirpath, log_error_to_net=True)

    # noinspection PyMethodMayBeStatic
    def _submit_action(self, action, arguments, event_time_s=None):
        url = self._urlTemplate.format(path='action/' + action)

        self._offline_endpoint.write('TA SENDING POST /action/' + action)

        r = requests.post(url=url,
                          headers=_JSON_HEADERS,
                          data=json.dumps({
                              'TIME': get_timestamp(event_time_s),
                              'ARGUMENTS': arguments
                          }))

        self._offline_endpoint.write('TA RECEIVED ACK: ' + str(r.status_code))

    # noinspection PyMethodMayBeStatic
    def _submit_status(self, status, message, event_time_s=None):
        url = self._urlTemplate.format(path='status')

        self._offline_endpoint.write('TA SENDING POST /status with STATUS: ' + status)

        r = requests.post(url=url,
                          headers=_JSON_HEADERS,
                          data=json.dumps({
                              'TIME': get_timestamp(event_time_s),
                              'STATUS': status,
                              'MESSAGE': message
                          }))

        self._offline_endpoint.write('TA RECEIVED ACK: ' + str(r.status_code))

    # noinspection PyMethodMayBeStatic
    def _submit_ready(self, event_time_s=None):
        url = self._urlTemplate.format(path='ready')

        self._offline_endpoint.write('TA SENDING POST /ready')

        # TODO: Try catch server connection issues
        r = requests.post(url=url,
                          headers=_JSON_HEADERS,
                          data=json.dumps({
                              'TIME': get_timestamp(event_time_s),
                          }))

        self._offline_endpoint.write('TA RECEIVED ACK: ' + str(r.status_code))

    # noinspection PyMethodMayBeStatic
    def _submit_error(self, error, message, event_time_s=None):
        # Since LL may poweroff right after receiving the message
        tpr.flush_logging_endpoints()

        url = self._urlTemplate.format(path='error')

        self._offline_endpoint.write('TA SENDING POST /error with ERROR: ' + error)

        r = requests.post(url=url,
                          headers=_JSON_HEADERS,
                          data=json.dumps({
                              'TIME': get_timestamp(event_time_s),
                              'ERROR': error,
                              'MESSAGE': message
                          }))

        self._offline_endpoint.write('TA RECEIVED ACK: ' + str(r.status_code))


class FakeTestHarnessReporter(AbstractReporter):
    """
    :type _offline_endpoint LoggingEndpoint
    """

    def __init__(self, log_filepath, artifact_dirpath):
        """
        :param str log_filepath:
        :param str artifact_dirpath:
        """

        self._offline_endpoint = tpr.get_logging_endpoint(
            os.path.join(artifact_dirpath, 'network_log.txt'))  # type: tpr.LoggingEndpoint

        AbstractReporter.__init__(self, log_filepath, artifact_dirpath)

    # noinspection PyMethodMayBeStatic
    def _submit_action(self, action, arguments, event_time_s=None):
        self._offline_endpoint.write('TA would SEND POST /action/' + action + ' with BODY: ' +
                                     json.dumps({
                                         'TIME': get_timestamp(event_time_s),
                                         'ARGUMENTS': arguments
                                     }))

    # noinspection PyMethodMayBeStatic
    def _submit_status(self, status, message, event_time_s=None):
        self._offline_endpoint.write('TA would SEND POST /status with BODY: ' +
                                     json.dumps({
                                         'TIME': get_timestamp(event_time_s),
                                         'STATUS': status,
                                         'MESSAGE': message
                                     }))

    # noinspection PyMethodMayBeStatic
    def _submit_ready(self, event_time_s=None):
        self._offline_endpoint.write('TA would SEND POST /ready with BODY: ' +
                                     json.dumps({
                                         'TIME': get_timestamp(event_time_s),
                                     }))

    # noinspection PyMethodMayBeStatic
    def _submit_error(self, error, message, event_time_s=None):
        self._offline_endpoint.write('TA would SEND POST /error with BODY: ' +
                                     json.dumps({
                                         'TIME': get_timestamp(event_time_s),
                                         'ERROR': error,
                                         'MESSAGE': message
                                     }))


class ConsoleHarnessLogger(AbstractReporter):
    def _submit_error(self, error, message, event_time_s=None):
        logging.error(message)
        pass

    def _submit_status(self, status, message, event_time_s=None):
        logging.info(message)
        pass

    def _submit_action(self, action, arguments, event_time_s=None):
        logging.error(arguments)
        pass

    def _submit_ready(self, event_time_s=None):
        logging.info("READY")
        pass
