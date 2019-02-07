#!/usr/bin/env python3

import asyncio
import json
import traceback
import uuid
from typing import Dict
from urllib import parse

import websockets
from sanic import Sanic
from websockets import WebSocketServerProtocol

from buildsystem import testbedmanager, REST_PORT, WEBSOCKET_PORT
from buildsystem.datatypes import BuildServiceException, \
    ActivityStatus, dictify, StatusHandler, StatusNotification
from buildsystem.testbedmanager import DasTestbed

_loop = asyncio.get_event_loop()

# vh = VineHelper()

_stdout_sockets = dict()  # type: Dict[str,WebSocketServerProtocol]


async def handler(websocket: WebSocketServerProtocol, path: str):
    # TODO: Add testbed name verification server-side
    # noinspection PyBroadException
    try:
        print("PATH: " + path)

        req = parse.urlparse(path)

        if req.path == '/stdoutListener':
            websocket_identifier = str(uuid.uuid4())
            _stdout_sockets[websocket_identifier] = websocket
            await websocket.send(websocket_identifier)
            await websocket.wait_for_connection_lost()

        params = parse.parse_qsl(req.query)

        method_params = dict()

        for p in params:
            if p[0] == 'include_android':
                method_params[p[0]] = p[1] == 'True'
            else:
                method_params[p[0]] = p[1]

        if 'stdout_identifier' in method_params:
            stdout_socket = _stdout_sockets[method_params['stdout_identifier']]
            listener = WebsocketStatusHandler(websocket, stdout_listener=stdout_socket)
            method_params.pop('stdout_identifier')
        else:
            listener = WebsocketStatusHandler(websocket)

        method_params['listener'] = listener

        # noinspection PyBroadException
        try:
            if req.path == '/claimBuildpoolPlainTestbed':
                notification = await testbedmanager.claim_buildsystem_plain_testbed(**method_params)
                await listener.send_notification_and_close(notification)

            if req.path == '/claimBuildpoolAndroidTestbed':
                notification = await testbedmanager.claim_buildsystem_android_testbed(**method_params)
                await listener.send_notification_and_close(notification)

            if req.path == '/createTestbed':
                notification = await testbedmanager.construct_unmanaged_testbed(**method_params)
                await listener.send_notification_and_close(notification)

            elif req.path == '/deleteTestbed':
                notification = await testbedmanager.delete_testbed(**method_params)
                await listener.send_notification_and_close(notification)

            elif req.path == '/addPlainTestbedToBuildPool':
                notification = await  testbedmanager.add_buildsystem_testbed_to_pool(listener, False)
                await listener.send_notification_and_close(notification)

            elif req.path == '/addAndroidTestbedToBuildPool':
                notification = await  testbedmanager.add_buildsystem_testbed_to_pool(listener, True)
                await listener.send_notification_and_close(notification)

            elif req.path == '/replaceTestbedInBuildPool':
                method_params['validate'] = True
                notification = await testbedmanager.destroy_and_trigger_testbed_replacement(**method_params)
                await listener.send_notification_and_close(notification)

            elif req.path == '/replaceTestbedInBuildPoolNoWait':
                method_params['validate'] = False
                notification = await testbedmanager.destroy_and_trigger_testbed_replacement(**method_params)
                await listener.send_notification_and_close(notification)

            elif req.path == '/updateRepo':
                testbed = testbedmanager.get_testbed(method_params['testbed_name'])
                branch = None if 'branch' not in method_params else method_params['branch']
                notification = await testbed.das_repo_update(listener, branch)
                await listener.send_notification_and_close(notification)

            elif req.path == '/dasDeploy':
                testbed = testbedmanager.get_testbed(method_params['testbed_name'])
                branch = None if 'branch' not in method_params else method_params['branch']
                cp_profile = None if 'cp_profile' not in method_params else method_params['cp_profile']
                notification = await testbed.das_deploy(listener, cp_profile, branch)
                await listener.send_notification_and_close(notification)

            elif req.path == '/dasExecuteTest':
                testbed = testbedmanager.get_testbed(method_params['testbed_name'])
                branch = None if 'branch' not in method_params else method_params['branch']
                cp_profile = None if 'cp_profile' not in method_params else method_params['cp_profile']
                await testbed.das_execute_test(listener, method_params['test_identifier'], cp_profile, branch)

            elif req.path == '/getExistingTestbed':
                testbed = testbedmanager.get_testbed(method_params['testbed_name'])
                testbed_d = dictify(testbed, DasTestbed)
                await listener.send_and_close(testbed.status, method_params['testbed_name'], "Fetched testbed",
                                              data=testbed_d)
            #
            # elif req.path == '/rebuildPredeployImage':
            #     notification = await vh.rebuild_predeploy_das_image(**method_params)
            #     await listener.send_notification_and_close(notification)

        except BuildServiceException as be:
            await listener.send_notification_and_close(be)
        except Exception:
            traceback.print_exc()
            await listener.send_and_close(
                ActivityStatus.SERVER_ERROR, "null", "An unexpected exception has occurred trying to handle " +
                                                     " the request. Please see the server logs for details.")

    except Exception:
        traceback.print_exc()
        websocket.send(json.dumps(
            {
                "status": "SERVER_ERROR",
                "testbed_name": "null",
                "message": "An error occured trying to route the request. Please see the server logs for details!"
            }))
        websocket.close(reason="An error occured trying to route the request. Please see the server logs for details!")


app = Sanic()


def main():
    loop = asyncio.get_event_loop()

    start_server = websockets.serve(handler, '0.0.0.0', WEBSOCKET_PORT, timeout=86400)
    asyncio.get_event_loop().run_until_complete(start_server)

    app.static('/resultData', 'results')
    webserver = app.create_server('0.0.0.0', port=REST_PORT)
    asyncio.ensure_future(webserver)

    loop.run_forever()


class WebsocketStatusHandler(StatusHandler):
    def __init__(self, websocket, stdout_listener: WebSocketServerProtocol = None):
        self._websocket = websocket
        self._stdout_listener = stdout_listener

    async def send_notification(self, status_notification: StatusNotification):
        msg = json.dumps(dictify(status_notification, StatusNotification))
        print(msg)
        await self._websocket.send(msg)

    async def stdout(self, message: str):
        if self._stdout_listener is not None:
            await self._stdout_listener.send(message)

    async def send_notification_and_close(self, status_notification: StatusNotification):
        msg = json.dumps(dictify(status_notification, StatusNotification))
        print(msg)
        await self._websocket.send(msg)
        if self._stdout_listener is not None:
            self._stdout_listener.close()
        self._websocket.close()
