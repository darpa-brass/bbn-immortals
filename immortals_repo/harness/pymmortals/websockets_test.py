import time
from threading import Thread
from types import FunctionType
from typing import List

from pymmortals.generated.mil.darpa.immortals.core.api.validation.validationstartdata import ValidationStartData
from pymmortals.generated.mil.darpa.immortals.core.api.websockets.websocketack import WebsocketAck
from pymmortals.generated.mil.darpa.immortals.core.api.websockets.websocketendpoint import WebsocketEndpoint
from pymmortals.websocket import AbstractImmortalsWebsocket, ThreadedWebsocketConnectionListener, \
    ThreadedWebsocketConnector

server_received_request: bool = False
client_received_ack: bool = False

validator_identifiers: List[str] = [
    'client-location-source-usb',
    'client-location-produce',
    'client-image-produce'
]

client_identifiers: List[str] = [
    'one',
    'two',
    'three',
    'four'
]


def setup_server(ws: AbstractImmortalsWebsocket):
    server_responder = ws.get_endpoint_responder(WebsocketEndpoint.VALIDATION_START)

    def server_receiver(data: ValidationStartData):
        global server_received_request
        print('SERVER RECEIVED: ')
        print('Received init with clients: ' + str(data.clientIdentifiers))
        print('Received init with validators: ' + str(data.validatorIdentifiers))
        server_received_request = True
        server_responder(WebsocketAck.OK)

    ws.register_endpoint_request_listener(endpoint=WebsocketEndpoint.VALIDATION_START, listener=server_receiver)


def setup_client(ws: AbstractImmortalsWebsocket) -> FunctionType:
    client_sender = ws.get_endpoint_sender(WebsocketEndpoint.VALIDATION_START)

    def client_receiver(data: WebsocketAck):
        global client_received_ack
        print('Client received reply: ' + data.name)
        client_received_ack = True

    ws.register_endpoint_response_listener(endpoint=WebsocketEndpoint.VALIDATION_START, listener=client_receiver)

    return client_sender


def execute_async_client_request(ws: AbstractImmortalsWebsocket):
    global server_received_request, client_received_ack

    vsd = ValidationStartData(
        validatorIdentifiers=validator_identifiers,
        clientIdentifiers=client_identifiers,
        sessionIdentifier='sessionIdentifier',
        minRuntimeMS=60000,
        maxRuntimeMS=600000)

    return_data = ws.make_asynchronous_request(WebsocketEndpoint.VALIDATION_START, vsd)
    client_received_ack = return_data == WebsocketAck.OK


def execute(client_sender: FunctionType):
    global server_received_request, client_received_ack

    vsd = ValidationStartData(
        validatorIdentifiers=validator_identifiers,
        clientIdentifiers=client_identifiers,
        sessionIdentifier='sessionIdentifier',
        minRuntimeMS=60000,
        maxRuntimeMS=600000)

    client_sender(vsd)

    timeout_secs = 5
    start_time_secs = time.time()

    while time.time() < (start_time_secs + timeout_secs) and not (client_received_ack and server_received_request):
        time.sleep(0.1)


def connection_listener_client_connector_server():
    global server_received_request, client_received_ack
    server_received_request = False
    client_received_ack = False

    client = ThreadedWebsocketConnector("127.0.0.1", 9696)
    client_sender = setup_client(client)

    server = ThreadedWebsocketConnectionListener(9696)
    setup_server(server)

    t0 = Thread(target=server.start)
    t0.setDaemon(True)
    t0.start()

    t1 = Thread(target=client.start)
    t1.setDaemon(True)
    t1.start()
    # time.sleep(1)

    execute(client_sender=client_sender)
    # time.sleep(1)

    assert server_received_request
    assert client_received_ack

    server.stop()
    client.stop()


def connection_listener_client_connector_server_early_start():
    global server_received_request, client_received_ack
    server_received_request = False
    client_received_ack = False

    client = ThreadedWebsocketConnector("127.0.0.1", 9696)
    server = ThreadedWebsocketConnectionListener(9696)

    t0 = Thread(target=server.start)
    t0.setDaemon(True)
    t0.start()

    t1 = Thread(target=client.start)
    t1.setDaemon(True)
    t1.start()

    client_sender = setup_client(client)
    setup_server(server)

    execute(client_sender=client_sender)

    assert server_received_request
    assert client_received_ack

    server.stop()
    client.stop()


def connection_listener_server_connector_client():
    global server_received_request, client_received_ack
    server_received_request = False
    client_received_ack = False

    client = ThreadedWebsocketConnectionListener(9696)
    client_sender = setup_client(client)

    server = ThreadedWebsocketConnector("127.0.0.1", 9696)
    setup_server(server)

    t0 = Thread(target=client.start)
    t0.setDaemon(True)
    t0.start()

    t1 = Thread(target=server.start)
    t1.setDaemon(True)
    t1.start()

    execute(client_sender=client_sender)

    assert server_received_request
    assert client_received_ack

    server.stop()
    client.stop()


def connection_listener_server_connector_client_early_start():
    global server_received_request, client_received_ack
    server_received_request = False
    client_received_ack = False

    client = ThreadedWebsocketConnectionListener(9696)
    server = ThreadedWebsocketConnector("127.0.0.1", 9696)

    t0 = Thread(target=client.start)
    t0.setDaemon(True)
    t0.start()

    t1 = Thread(target=server.start)
    t1.setDaemon(True)
    t1.start()

    client_sender = setup_client(client)
    setup_server(server)

    execute(client_sender=client_sender)

    assert server_received_request
    assert client_received_ack

    server.stop()
    client.stop()


def async_connector_test():
    global server_received_request, client_received_ack
    server_received_request = False
    client_received_ack = False

    client = ThreadedWebsocketConnector("127.0.0.1", 9696)
    # client_sender = setup_client(client)

    server = ThreadedWebsocketConnectionListener(9696)
    setup_server(server)

    t0 = Thread(target=server.start)
    t0.setDaemon(True)
    t0.start()

    t1 = Thread(target=client.start)
    t1.setDaemon(True)
    t1.start()
    # time.sleep(1)

    execute_async_client_request(client)

    # execute(client_sender=client_sender)
    # time.sleep(1)

    assert server_received_request
    assert client_received_ack

    server.stop()
    client.stop()


if __name__ == '__main__':
    connection_listener_client_connector_server()
    time.sleep(0.2)
    connection_listener_server_connector_client()
    time.sleep(0.2)
    connection_listener_server_connector_client_early_start()
    time.sleep(0.2)
    connection_listener_client_connector_server_early_start()
    time.sleep(0.2)
    async_connector_test()
