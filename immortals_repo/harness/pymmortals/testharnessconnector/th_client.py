import json

import requests

import pymmortals.immortalsglobals as ig
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.routing import EventType, EventTag, EventTags
from pymmortals.datatypes.test_harness_api import LLTestActionDone, LLDasReady, LLDasStatusEvent, LLDasErrorEvent
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testadapterstate import TestAdapterState
from pymmortals.routing.eventrouter import EventTransformer
from pymmortals.utils import get_th_timestamp

_BASE_URL = get_configuration().testHarness.protocol + get_configuration().testHarness.url + ':' + \
            str(get_configuration().testHarness.port)

_JSON_HEADERS = {'Content-Type': 'application/json'}

URL_TH_ERROR = _BASE_URL + '/error'
URL_TH_READY = _BASE_URL + '/ready'
URL_TH_STATUS = _BASE_URL + '/status'
URL_TH_ACTION = _BASE_URL + '/action'
URL_TH_DONE = URL_TH_ACTION + '/done'


def _post_network_request(event_tag: EventTag, url: str, data: str):
    details_str = 'TA SENDING POST ' + url.replace(_BASE_URL, '') + ' with '

    if event_tag.event_type == EventType.STATUS:
        if url.startswith(URL_TH_ACTION):
            details_str += '/action/' + url[url.rfind('/') + 1:]
            send_data = data

        elif event_tag == EventTags.THSubmitReady:
            details_str += json.dumps(json.loads(data), indent=4, separators=(',', ': '))
            send_data = data

        else:
            send_data = data
            details_str += 'STATUS: ' + json.loads(data)['STATUS']

    elif event_tag.event_type == EventType.ERROR:
        details_str += 'ERROR: ' + json.loads(data)['ERROR']
        send_data = json.dumps(data)

    else:
        raise Exception('Unknown combination of routable data submitted for network sending! Details: '
                        'url=' + url + ', eventTag=' + str(event_tag))

    # TODO: objectify after comparison with prior build
    ig.get_event_router().submit(EventTags.NetworkSentPost, details_str)

    response = requests.post(url=url,
                             headers=_JSON_HEADERS,
                             data=send_data)

    ig.get_event_router().submit(EventTags.NetworkReceivedPostResponse, 'TA RECEIVED ACK: ' + str(response.status_code))


class THErrorStringTransformer(EventTransformer):
    @staticmethod
    def transform(event_tag: EventTag, data: str):
        return {
            'event_tag': event_tag,
            'data': json.dumps({
                'TIME': get_th_timestamp(),
                'ERROR': event_tag.th_status_tag,
                'MESSAGE': data
            })
        }


def process_error(event_tag: EventTag, data: str):
    # Since LL may poweroff right after receiving the message

    if event_tag.th_status_tag is not None:
        if not isinstance(data, str):
            raise Exception('Invalid data type "' + str(type(data)) +
                            '" provided for url "' + URL_TH_DONE + '"!')

        _post_network_request(event_tag=event_tag,
                              url=URL_TH_ERROR,
                              data=LLDasErrorEvent(event_tag.th_status_tag, data).to_json_str(include_metadata=False))


def process_status(event_tag: EventTag, data: str):
    if event_tag == EventTags.THSubmitReady:
        if data is not None:
            raise Exception('No data is expected for the event type ' + EventTags.THSubmitReady.identifier + '!')
        _post_network_request(event_tag=event_tag,
                              url=URL_TH_READY,
                              data=LLDasReady().to_json_str(include_metadata=False))

    elif event_tag == EventTags.THSubmitDone:
        if not isinstance(data, TestAdapterState):
            raise Exception('Invalid data type "' + str(type(data)) +
                            '" provided for url "' + URL_TH_DONE + '"!')

        _post_network_request(event_tag=event_tag,
                              url=URL_TH_DONE,
                              data=LLTestActionDone(data).to_json_str(include_metadata=False))

    elif event_tag.th_status_tag is not None:
        if not isinstance(data, TestAdapterState):
            raise Exception('Invalid data type "' + str(type(data)) +
                            '" provided for url "' + URL_TH_STATUS + '"!')
        _post_network_request(event_tag=event_tag,
                              url=URL_TH_STATUS,
                              data=LLDasStatusEvent(STATUS=event_tag.th_status_tag,
                                                    MESSAGE=data.to_json_str(
                                                        include_metadata=False)).to_json_str(include_metadata=False))
