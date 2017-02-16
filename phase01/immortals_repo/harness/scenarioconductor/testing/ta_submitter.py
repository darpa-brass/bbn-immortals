import copy
import json
import logging
import time

import requests

from ..data.base.root_configuration import load_configuration

config = load_configuration()
TH_PORT = config.testHarness.port
TH_URL = config.testHarness.url
TH_PROTOCOL = config.testHarness.protocol

TA_PORT = config.testAdapter.port
TA_URL = config.testAdapter.url
TA_PROTOCOL = config.testAdapter.protocol

URL_TEMPLATE = TA_PROTOCOL + TA_URL + ':' + str(TA_PORT) + '/{path}'

JSON_HEADERS = {'Content-Type': 'application/json'}

_network_logger = logging.getLogger()


def get_timestamp(time_seconds=None):
    if time_seconds is None:
        time_seconds = time.time()
    return time.strftime("%Y-%m-%dT%H:%m:%S", time.gmtime(time_seconds)) + '.' + str(time_seconds % 1)[2:5] + 'Z'


def set_logger(logging_logger):
    global _network_logger
    _network_logger = logging_logger


def send_baseline_validation(source_configuration_str=None):
    """
    :type source_configuration_str: str or None
    """

    _network_logger.info('TH SENDING POST /action/validateBaselineApplication\n')

    url = URL_TEMPLATE.format(path='action/validateBaselineApplication')
    if source_configuration_str is None:
        r = requests.post(url=url)
    else:
        r = requests.post(url=url,
                          headers=JSON_HEADERS,
                          data=source_configuration_str
                          )
    body = pt(r.text)

    _network_logger.info('TH RECEIVED ACK for POST /action/validateBaselineApplication of ' + str(r.status_code) +
                         ' with BODY: ' + body + '\n')


def send_adapt_and_validate(source_configuration_str):
    """
    :type source_configuration_str: str
    """

    _network_logger.info('TH SENDING POST /action/adaptAndValidateApplication\n')
    url = URL_TEMPLATE.format(path='action/adaptAndValidateApplication')
    r = requests.post(url=url,
                      headers=JSON_HEADERS,
                      data=source_configuration_str
                      )

    body = pt(r.text)

    _network_logger.info('TH RECEIVED ACK for POST /action/adaptAndValidateApplication of ' + str(r.status_code) +
                         ' with BODY: ' + body + '\n')


class TestAdapterSubmission:
    def __init__(self, source_configuration_dict, method):
        """
        :type source_configuration_dict: dict or None
        :param method: method
        """

        send_value = json.dumps({
            'TIME': get_timestamp(),
            'ARGUMENTS': None if source_configuration_dict is None else source_configuration_dict
        })
        self._method_kwargs = {
            'source_configuration_str': send_value
        }
        self._method = method
        self.results = None

    def execute(self):
        self._method(**self._method_kwargs)


def produce_test_adapter_submissions(scenario_flow, scenario_configuration_dict):
    """
    :type scenario_flow: str
    :type scenario_configuration_dict: dict or None
    :rtype: list[TestAdapterSubmission]
    """
    l = []

    scd = copy.deepcopy(scenario_configuration_dict)

    if 'sessionIdentifier' in scd:
        scd.pop('sessionIdentifier')

    if scenario_flow == 'baseline':
        l.append(TestAdapterSubmission(
            source_configuration_dict=scd,
            method=send_baseline_validation
        ))

    elif scenario_flow == 'challenge':
        l.append(TestAdapterSubmission(
            source_configuration_dict=scd,
            method=send_adapt_and_validate
        ))

    elif scenario_flow == 'all':
        l.append(TestAdapterSubmission(
            source_configuration_dict=None,
            method=send_baseline_validation
        ))

        l.append(TestAdapterSubmission(
            source_configuration_dict=scd,
            method=send_baseline_validation
        ))

        l.append(TestAdapterSubmission(
            source_configuration_dict=scd,
            method=send_adapt_and_validate
        ))

    return l


def pt(text):
    if text is None:
        return ''

    # noinspection PyBroadException
    try:
        return_body = json.dumps(json.loads(text), indent=4, separators=(',', ': '))
        return return_body
    except:
        return text
