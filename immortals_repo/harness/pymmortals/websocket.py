import asyncio
import logging
import random
import time
import traceback
from concurrent.futures import ThreadPoolExecutor
from enum import Enum
from threading import RLock, Event, Thread
from types import FunctionType
from typing import Type, Callable

import janus
import websockets
from websockets import WebSocketCommonProtocol, WebSocketClientProtocol, ConnectionClosed
from websockets.py35.client import Connect
from websockets.server import WebSocketServer

from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.serializable import Serializable, deserialize
from pymmortals.generated.mil.darpa.immortals.core.api.applications.applicationdeploymentdetails import \
    ApplicationDeploymentDetails
from pymmortals.generated.mil.darpa.immortals.core.api.validation.results.validationresults import ValidationResults
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validationstartdata import ValidationStartData
from pymmortals.generated.mil.darpa.immortals.core.api.websockets.createapplicationinstancedata import \
    CreateApplicationInstanceData
from pymmortals.generated.mil.darpa.immortals.core.api.websockets.websocketack import WebsocketAck
from pymmortals.generated.mil.darpa.immortals.core.api.websockets.websocketendpoint import WebsocketEndpoint
from pymmortals.generated.mil.darpa.immortals.core.api.websockets.websocketobject import WebsocketObject
from pymmortals import threadprocessrouter as tpr

gen: random.Random = random.Random()

_logger = logging.getLogger("AbstractImmortalsWebsocket")

_ENDPOINT_REGISTRATION_ENDPOINT = "/immortals/websocket/endpoint_registration"


class AbstractImmortalsWebsocket:
    def __init__(self, max_worker_threads, event_loop: asyncio.AbstractEventLoop = None):
        self._start_loop = (event_loop is None)
        self._receiver_map: [str, FunctionType] = dict()
        self._endpoint_connection_map: [str, WebSocketCommonProtocol] = dict()
        self._lock = RLock()
        if event_loop is None:
            self._loop: asyncio.AbstractEventLoop = asyncio.new_event_loop()
        else:
            self._loop: asyncio.AbstractEventLoop = event_loop
        # self._loop.set_debug(True)
        asyncio.set_event_loop(self._loop)
        self._send_queue: janus.Queue = janus.Queue(loop=self._loop)
        self._thread_pool: ThreadPoolExecutor = ThreadPoolExecutor(max_workers=max_worker_threads)

    def _ping(self, endpoint_path: str):
        self._send_queue.sync_q.put(
            '{"endpoint": "' + _ENDPOINT_REGISTRATION_ENDPOINT + '", "data": "' + endpoint_path + '"}')

    async def _producer_handler(self, websocket: WebSocketCommonProtocol):
        while websocket.open:
            try:
                message = await self._send_queue.async_q.get()
                self._send_queue.async_q.task_done()
                await websocket.send(message)

            except ConnectionClosed:
                _logger.debug('Producer Handler Connection Closed')

            except asyncio.CancelledError:
                _logger.debug('Producer Handler Cancelled')
                return

            except Exception as e:
                traceback.print_exc()
                raise e

    async def _consumer_handler(self, websocket: WebSocketCommonProtocol):
        while websocket.open:
            try:
                message = await websocket.recv()
                wso: WebsocketObject = WebsocketObject.from_json_str(message, do_replacement=False)

                with self._lock:
                    if wso.endpoint == _ENDPOINT_REGISTRATION_ENDPOINT:
                        if wso.data not in self._endpoint_connection_map:
                            _logger.debug('Registering endpoint: ' + wso.data)
                            self._endpoint_connection_map[wso.data] = websocket

                    else:
                        if wso.endpoint not in self._endpoint_connection_map:
                            self._endpoint_connection_map[wso.endpoint] = websocket

                        self._receiver_map[wso.endpoint](wso.data)

            except asyncio.CancelledError:
                _logger.debug('Consumer Handler Cancelled')
                return

            except ConnectionClosed:
                _logger.debug('Consumer Handler Connection Closed')
                return
                # pass

            except Exception as e:
                _logger.exception(e)
                traceback.print_exc()
                raise e

    def _halt(self):
        self._loop.stop()

        for t in asyncio.Task.all_tasks(self._loop):
            self._loop.call_soon_threadsafe(t.cancel)

        while self._loop.is_running():
            time.sleep(0.1)

        self._loop.close()

    def register_endpoint_response_listener(self, endpoint: WebsocketEndpoint, listener: FunctionType):
        with self._lock:
            if endpoint.path in self._receiver_map:
                raise Exception('Endpoint "' + endpoint.path + '" already registered!')

            def fun(data: str):
                listener(deserialize(endpoint.ackType, data))

            self._receiver_map[endpoint.path] = fun
            self._ping(endpoint.path)

    def replace_endpoint_response_listener(self, endpoint: WebsocketEndpoint, old_listener: FunctionType,
                                           new_listener: FunctionType):
        with self._lock:
            if endpoint.path in self._receiver_map:
                if self._receiver_map[endpoint.path] == old_listener:

                    def fun(data: str):
                        new_listener(deserialize(endpoint.ackType, data))

                    self._receiver_map[endpoint.path] = fun

                else:
                    raise Exception(
                        "Cannot replace endpoint response listener unless the original listener is also provided!")

    def register_endpoint_request_listener(self, endpoint: WebsocketEndpoint, listener: FunctionType):
        with self._lock:
            if endpoint.path in self._receiver_map:
                raise Exception('Endpoint "' + endpoint.path + '" already registered!')

            def fun(data: str):
                listener(deserialize(endpoint.postType, data))

            self._receiver_map[endpoint.path] = fun
            self._ping(endpoint.path)

    def replace_endpoint_request_listener(self, endpoint: WebsocketEndpoint, old_listener: FunctionType,
                                          new_listener: FunctionType):
        with self._lock:
            if endpoint.path in self._receiver_map:
                if self._receiver_map[endpoint.path] == old_listener:
                    def fun(data: str):
                        new_listener(deserialize(endpoint.postType, data))

                    self._receiver_map[endpoint.path] = fun

            else:
                raise Exception(
                    "Cannot replace endpoint response listener unless the original listener is also provided!")

    def get_endpoint_responder(self, endpoint: WebsocketEndpoint) -> FunctionType:
        return _create_serializer(endpoint_path=endpoint.path, input_type=endpoint.ackType, queue=self._send_queue)

    def get_endpoint_sender(self, endpoint: WebsocketEndpoint) -> FunctionType:
        return _create_serializer(endpoint_path=endpoint.path, input_type=endpoint.postType, queue=self._send_queue)

    def make_asynchronous_request(self, endpoint: WebsocketEndpoint, send_data) -> object:
        event: Event = Event()
        return_data = None

        with self._lock:
            if endpoint.path in self._receiver_map:
                raise Exception(
                    'Endpoint already registered for "' + endpoint.path + '"! Please use that to make this request!')
            else:
                def listener(listener_data):
                    nonlocal event, return_data
                    return_data = listener_data
                    event.set()

                self.register_endpoint_response_listener(endpoint=endpoint, listener=listener)

        self.get_endpoint_sender(endpoint)(send_data)

        event.wait()

        with self._lock:
            self._receiver_map.pop(endpoint.path)

        return return_data

    def start(self):
        raise NotImplementedError

    def stop(self):
        raise NotImplementedError

    def is_running(self) -> bool:
        raise NotImplementedError


def _create_serializer(endpoint_path: str, input_type: Type, queue: janus.Queue) -> FunctionType:
    if input_type == bool:
        def fun(data: bool):
            if data:
                value = 'true'
            else:
                value = 'false'

            wso = WebsocketObject(endpoint=endpoint_path, data=value)
            queue.sync_q.put(wso.to_json_str(False))

        return fun

    elif input_type == int:
        def fun(data: int):
            value = str(data)
            wso = WebsocketObject(endpoint=endpoint_path, data=value)
            queue.sync_q.put(wso.to_json_str(False))

        return fun

    elif input_type == float:
        def fun(data: float):
            value = str(data)
            wso = WebsocketObject(endpoint=endpoint_path, data=value)
            queue.sync_q.put(wso.to_json_str(False))

        return fun

    elif input_type == str:
        def fun(data: str):
            wso = WebsocketObject(endpoint=endpoint_path, data=data)
            queue.sync_q.put(wso.to_json_str(False))

        return fun

    elif issubclass(input_type, Enum):
        def fun(data: Enum):
            value = data.name
            wso = WebsocketObject(endpoint=endpoint_path, data=value)
            queue.sync_q.put(wso.to_json_str(False))

        return fun

    elif issubclass(input_type, Serializable):
        def fun(data: Serializable):
            value = data.to_json_str(False)
            wso = WebsocketObject(endpoint=endpoint_path, data=value)
            queue.sync_q.put(wso.to_json_str(False))

        return fun

    else:
        raise Exception('Cannot create a serializer for the object of type "' + input_type.__name__ + '"!')


class ThreadedWebsocketConnectionListener(AbstractImmortalsWebsocket):
    def __init__(self, port: int, max_worker_threads: int = 4):
        """
        Starts a new Threaded websocket server

        :param port: The port to run it on
        :param max_worker_threads: [optional] The maximum number of worker threads to use (default: 4)
        """
        super().__init__(max_worker_threads=max_worker_threads)
        self.do_stop = False
        self.port: int = port
        self._is_running: bool = False
        self._server: WebSocketServer = websockets.serve(
            ws_handler=self._handler,
            host='127.0.0.1',
            port=self.port,
            loop=self._loop)

    def start(self):
        do_start = False
        with self._lock:
            if not self._is_running:
                do_start = True
                self._is_running = True

        if do_start:
            srv = websockets.serve(
                ws_handler=self._handler,
                host='127.0.0.1',
                port=self.port,
                loop=self._loop)

            self._server = self._loop.run_until_complete(srv)
            try:
                self._loop.run_forever()

            except Exception as e:
                traceback.print_exc(e)
                self._is_running = False

    def stop(self, stop_loop: bool = True):
        do_stop = False
        with self._lock:
            if self._is_running:
                do_stop = True
                self._is_running = False

        if do_stop:
            self._loop.call_soon_threadsafe(self._server.close)

            pending = asyncio.Task.all_tasks(loop=self._loop)
            for t in pending:
                self._loop.call_soon_threadsafe(t.cancel)

            finished: bool = False
            while not finished:
                finished = True
                for t in pending:
                    if not t.done():
                        finished = False

                time.sleep(0.1)

            self._thread_pool.shutdown()
            self._loop.stop()
            self._server.wait_closed()

    async def _handler(self, websocket, path):
        _logger.debug('Client connected to "' + path + '"')

        consumer_task: asyncio.Future = asyncio.ensure_future(self._consumer_handler(websocket=websocket))
        producer_task: asyncio.Future = asyncio.ensure_future(self._producer_handler(websocket=websocket))

        done, pending = await asyncio.wait(
            [consumer_task, producer_task],
            loop=self._loop,
            return_when=asyncio.FIRST_COMPLETED)

        for task in pending:
            task.cancel()

    def is_running(self):
        return self._is_running


class ThreadedWebsocketConnector(AbstractImmortalsWebsocket):
    def __init__(self, target_addr: str, target_port: int, max_worker_threads: int = 2):
        """
        Starts a new Threaded websocket client

        :param target_addr: The target address to connect to
        :param target_port: The target port to connect to
        :param max_worker_threads: [optional] The maximum number of worker threads to use (default: 4)
        """
        super().__init__(max_worker_threads=max_worker_threads)
        self._addr: str = target_addr
        self._port: int = target_port
        self._websocket: WebSocketClientProtocol = None
        self._is_running: bool = False

    def start(self):
        do_start = False
        with self._lock:
            if not self._is_running:
                do_start = True
                self._is_running = True

        if do_start:
            connection: Connect = websockets.connect('ws://' + self._addr + ':' + str(self._port))
            self._websocket = self._loop.run_until_complete(connection)
            asyncio.ensure_future(self._producer_handler(self._websocket), loop=self._loop)
            asyncio.ensure_future(self._consumer_handler(self._websocket), loop=self._loop)
            self._loop.run_forever()

    def stop(self):
        do_stop = False
        with self._lock:
            if self._is_running:
                do_stop = True
                self._is_running = False

        if do_stop:
            self._websocket.close_connection(force=True)
            self._websocket.close()

            pending = asyncio.Task.all_tasks(loop=self._loop)
            for t in pending:  # type: asyncio.Task
                self._loop.call_soon_threadsafe(t.cancel)

            finished: bool = False
            while not finished:
                finished = True
                for t in pending:  # type: asyncio.Task
                    if not t.done():
                        finished = False

                time.sleep(0.1)

            self._thread_pool.shutdown()
            self._loop.stop()

    def is_running(self):
        return self._is_running


class DasBridge:
    def start(self):
        raise NotImplementedError

    def start_in_thread(self):
        raise NotImplementedError

    def stop(self):
        raise NotImplementedError

    def createApplicationInstance(self, data: CreateApplicationInstanceData) -> ApplicationDeploymentDetails:
        raise NotImplementedError

    def validationStart(self, data: ValidationStartData) -> WebsocketAck:
        raise NotImplementedError

    def validationStop(self):
        raise NotImplementedError

    def set_validation_results_listener(self, listener: Callable[[ValidationResults], None]):
        raise NotImplementedError


class _DasBridgeImpl(DasBridge):
    def start(self):
        self._websocket.start()

    def start_in_thread(self):
        tpr.start_thread(thread_method=self._websocket.start, shutdown_method=self._websocket.stop)

    def stop(self):
        self._websocket.stop()

    def __init__(self, target_addr: str, target_port: int):
        self._websocket: ThreadedWebsocketConnector = ThreadedWebsocketConnector(target_addr=target_addr,
                                                                                 target_port=target_port)
        self._validation_results_listener = None
        self._lock: RLock = RLock()
        self._run_thread: Thread = None

    def createApplicationInstance(self, data: CreateApplicationInstanceData) -> ApplicationDeploymentDetails:
        return_data = self._websocket.make_asynchronous_request(
            WebsocketEndpoint.SOURCECOMPOSER_CREATE_APPLICATION_INSTANCE, data)
        assert (isinstance(return_data, ApplicationDeploymentDetails))
        return return_data

    def validationStart(self, data: ValidationStartData):
        result: WebsocketAck = self._websocket.make_asynchronous_request(WebsocketEndpoint.VALIDATION_START, data)
        if result != WebsocketAck.OK:
            _logger.error('validationStart received Ack of ' + result.name + '!')

    def validationStop(self):
        result: WebsocketAck = self._websocket.make_asynchronous_request(WebsocketEndpoint.VALIDATION_STOP, "")
        if result != WebsocketAck.OK:
            _logger.error('validationStop received Ack of ' + result.name + '!')

    def set_validation_results_listener(self, listener: Callable[[ValidationResults], None]):
        with self._lock:
            if self._validation_results_listener is not None:
                raise Exception('An event listener for validation results is already registered!')

            self._websocket.register_endpoint_request_listener(
                endpoint=WebsocketEndpoint.VALIDATION_RESULTS, listener=listener)


_das_bridge_init_lock: RLock = RLock()
_das_bridge: _DasBridgeImpl = None


def get_das_bridge() -> DasBridge:
    global _das_bridge
    with _das_bridge_init_lock:
        if _das_bridge is None:
            _das_bridge = _DasBridgeImpl(target_addr='127.0.0.1',
                                         target_port=get_configuration().dasService.websocketPort)
            _das_bridge.start_in_thread()
    return _das_bridge
