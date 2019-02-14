import copy
import json
import logging
import os
import time
from typing import List, Callable, Union, Optional

import requests

from integrationtest.immortalsglobals import get_configuration
from integrationtest.datatypes.testing import Phase2SubmissionFlow, PerturbationScenario
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.enabledas import EnableDas
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.submissionmodel import SubmissionModel
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.testadapterendpoint import TestAdapterEndpoint
from integrationtest import threadprocessrouter as tpr

_URL_TEMPLATE = get_configuration().testAdapter.protocol + '://' + \
                get_configuration().testAdapter.url + \
                ':' + str(get_configuration().testAdapter.port) + '{path}'

JSON_HEADERS = {'Content-Type': 'application/json'}

_logger = logging.getLogger()


def set_logger(logger):
    global _logger
    _logger = logger


def post(endpoint: TestAdapterEndpoint, body: dict):
    global _logger

    _logger.sending_post_listener(endpoint=endpoint, body_dict=body)

    url = _URL_TEMPLATE.format(path=endpoint.path)
    if body is None:
        r = requests.post(url=url)

    else:
        r = requests.post(url=url,
                          headers=JSON_HEADERS,
                          data=json.dumps(body),
                          timeout=1)

    return_code = r.status_code
    return_body = r.text

    _logger.sent_post_ack_listener(endpoint=endpoint,
                                   response_code=return_code,
                                   body_str=return_body)


def send_cp1(submission_model: Optional[SubmissionModel] = None):
    if submission_model is None:
        body = None
    else:
        body = submission_model.to_dict(include_metadata=False, strip_nulls=True)
        submission_model.to_file_pretty(os.path.join(
            get_configuration().globals.immortalsRoot, 'DAS_DEPLOYMENT/input.json'), include_metadata = False)
    tpr.start_thread(thread_method=post, thread_args=[TestAdapterEndpoint.CP1, body])


def send_cp2(submission_model: Optional[SubmissionModel] = None):
    if submission_model is None:
        body = None
    else:
        body = submission_model.to_dict(include_metadata=False, strip_nulls=True)
        submission_model.to_file_pretty(os.path.join(
            get_configuration().globals.immortalsRoot, 'DAS_DEPLOYMENT/input.json'), include_metadata = False)
    tpr.start_thread(thread_method=post, thread_args=[TestAdapterEndpoint.CP2, body])


def send_cp3(submission_model: Optional[SubmissionModel] = None):
    if submission_model is None:
        body = None
    else:
        body = submission_model.to_dict(include_metadata=False, strip_nulls=True)
        submission_model.to_file_pretty(os.path.join(
            get_configuration().globals.immortalsRoot, 'DAS_DEPLOYMENT/input.json'), include_metadata = False)
    tpr.start_thread(thread_method=post, thread_args=[TestAdapterEndpoint.CP3, body])


def send_disable_das():
    disable = EnableDas(dasEnabled=False)
    body = disable.to_dict(include_metadata=False, strip_nulls=True)
    tpr.start_thread(thread_method=post, thread_args=[TestAdapterEndpoint.ENABLED, body])


class TestAdapterSubmission:
    def __init__(self, ll_input: Union[SubmissionModel, None], method: Callable):
        if ll_input is None:
            self._method_kwargs = {}
        else:
            self._method_kwargs = {
                'submission_model': ll_input
            }
        self._method = method

    def execute(self):
        self._method(**self._method_kwargs)


def produce_test_adapter_submissions(ll_input: SubmissionModel,
                                     submission_flow: Phase2SubmissionFlow,
                                     perturbation_scenario: PerturbationScenario) -> List[TestAdapterSubmission]:
    rval = list()  # type: List[TestAdapterSubmission]

    scd = copy.deepcopy(ll_input)  # type: SubmissionModel

    if scd is not None and scd.sessionIdentifier is not None:
        session_identifier = scd.sessionIdentifier
        scd.sessionIdentifier = None
    else:
        timestamp = str(int(time.time() * 1000))
        session_identifier = 'I' + timestamp

    if submission_flow == Phase2SubmissionFlow.BaselineA or submission_flow == Phase2SubmissionFlow.BaselineB:
        rval.append(
            TestAdapterSubmission(
                ll_input=None,
                method=send_disable_das
            )
        )

    # if submission_flow == Phase2SubmissionFlow.BaselineB or submission_flow == Phase2SubmissionFlow.Challenge:
    scd_copy = copy.deepcopy(scd)
    if scd_copy is not None:
        scd_copy.sessionIdentifier = session_identifier + submission_flow.name

    if perturbation_scenario.endpoint == TestAdapterEndpoint.CP1:
        rval.append(
            TestAdapterSubmission(
                ll_input=(None if submission_flow == Phase2SubmissionFlow.BaselineA else scd_copy),
                method=send_cp1
            )
        )

    elif perturbation_scenario.endpoint == TestAdapterEndpoint.CP2:
        rval.append(
            TestAdapterSubmission(
                ll_input=(None if submission_flow == Phase2SubmissionFlow.BaselineA else scd_copy),
                method=send_cp2
            )
        )

    elif perturbation_scenario.endpoint == TestAdapterEndpoint.CP3:
        rval.append(
            TestAdapterSubmission(
                ll_input=(None if submission_flow == Phase2SubmissionFlow.BaselineA else scd_copy),
                method=send_cp3
            )
        )

    else:
        raise Exception('Unexpected endpoint "' + perturbation_scenario.endpoint.name + '"!')

    return rval
