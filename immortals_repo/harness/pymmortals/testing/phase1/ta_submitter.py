import copy
import json
import logging
import time
from typing import List, Callable, Union

import requests

from pymmortals.datatypes.deployment_model import LLP1Input
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.test_harness_api import LLTestActionSubmission
from pymmortals.datatypes.testing_p1 import Endpoint, SubmissionFlow
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.submissionmodel import SubmissionModel

_URL_TEMPLATE = get_configuration().testAdapter.protocol + \
                get_configuration().testAdapter.url + \
                ':' + str(get_configuration().testAdapter.port) + '{path}'

JSON_HEADERS = {'Content-Type': 'application/json'}

_logger = logging.getLogger()


def set_logger(logger):
    global _logger
    _logger = logger


def post(endpoint: Endpoint, body: dict):
    global _logger
    _logger.sending_post_listener(endpoint=endpoint, body_dict=body)

    url = _URL_TEMPLATE.format(path=endpoint.path)
    if body is None:
        r = requests.post(url=url)

    else:
        r = requests.post(url=url,
                          headers=JSON_HEADERS,
                          data=json.dumps(body))

    return_code = r.status_code
    return_body = r.text

    _logger.sent_post_ack_listener(endpoint=endpoint,
                                   response_code=return_code,
                                   body_str=return_body)


def send_baseline_validation(deployment_model: LLP1Input):
    body = LLTestActionSubmission(ARGUMENTS=deployment_model).to_dict(include_metadata=False)
    post(endpoint=Endpoint.ACTION_VALIDATE_BASELINE, body=body)


def send_adapt_and_validate(deployment_model: LLP1Input):
    body = LLTestActionSubmission(ARGUMENTS=deployment_model).to_dict(include_metadata=False)
    post(endpoint=Endpoint.ACTION_ADAPT_AND_VALIDATE, body=body)


class TestAdapterSubmission:
    def __init__(self, ll_input: Union[LLP1Input, None], method: Callable):
        self._method_kwargs = {
            'deployment_model': ll_input
        }
        self._method = method

    def execute(self):
        self._method(**self._method_kwargs)


def produce_test_adapter_submissions(scenario_flow: SubmissionFlow,
                                     ll_input: SubmissionModel) -> List[TestAdapterSubmission]:
    l = []

    scd: LLP1Input = copy.deepcopy(ll_input)

    if scd.sessionIdentifier is not None:
        session_identifier = scd.sessionIdentifier
        scd.sessionIdentifier = None
    else:
        timestamp = str(int(time.time() * 1000))
        session_identifier = 'I' + timestamp

    if scenario_flow == SubmissionFlow.BASELINE:
        scd_copy = copy.deepcopy(scd)
        scd_copy.sessionIdentifier = session_identifier + 'baseline'
        l.append(TestAdapterSubmission(
            ll_input=scd_copy,
            method=send_baseline_validation
        ))

    elif scenario_flow == SubmissionFlow.CHALLENGE:
        scd_copy = copy.deepcopy(scd)
        scd_copy.sessionIdentifier = session_identifier + 'challenge'
        l.append(TestAdapterSubmission(
            ll_input=scd_copy,
            method=send_adapt_and_validate
        ))

    elif scenario_flow == SubmissionFlow.ALL:
        scd_copy = copy.deepcopy(scd)
        scd_copy.sessionIdentifier = session_identifier + 'baseline'
        l.append(TestAdapterSubmission(
            ll_input=scd_copy,
            method=send_baseline_validation
        ))

        scd_copy = copy.deepcopy(scd)
        scd_copy.sessionIdentifier = session_identifier + 'challenge'
        l.append(TestAdapterSubmission(
            ll_input=scd_copy,
            method=send_adapt_and_validate
        ))

    return l
