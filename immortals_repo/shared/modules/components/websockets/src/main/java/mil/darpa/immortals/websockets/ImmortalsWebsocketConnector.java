package mil.darpa.immortals.websockets;

import mil.darpa.immortals.core.analytics.EndpointReceiver;
import mil.darpa.immortals.core.analytics.EndpointSender;
import mil.darpa.immortals.core.api.websockets.WebsocketEndpoint;
import mil.darpa.immortals.core.api.websockets.WebsocketObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by awellman@bbn.com on 8/10/17.
 */
public class ImmortalsWebsocketConnector extends AbstractImmortalsWebsocket {

    private static ImmortalsWebsocketConnector instance;

    private final int port;

    private final WebSocketClient wsc;

    private Logger logger = LoggerFactory.getLogger(ImmortalsWebsocketConnector.class);

    @Override
    public synchronized <C> void registerEndpointResponseListener(WebsocketEndpoint endpoint, EndpointReceiver<C> receiver, Class<C> rxType) {
        super.registerEndpointResponseListener(endpoint, receiver, rxType);
        if (wsc.isOpen()) {
            ping(endpoint.path);
        }
    }

    @Override
    public synchronized <P> void registerEndpointRequestListener(WebsocketEndpoint endpoint, EndpointReceiver<P> receiver, Class<P> rxType) {
        super.registerEndpointRequestListener(endpoint, receiver, rxType);
        if (wsc.isOpen()) {
            ping(endpoint.path);
        }
    }

    @Override
    public synchronized <T> EndpointSender<T> getEndpointSender(WebsocketEndpoint endpoint, Class<T> sendType) {
        EndpointSender<T> rval = super.getEndpointSender(endpoint, sendType);
        if (wsc.isOpen()) {
            ping(endpoint.path);
        }
        return rval;
    }

    @Override
    public synchronized <T> EndpointSender<T> getEndpointResponder(WebsocketEndpoint endpoint, Class<T> sendType) {
        EndpointSender<T> rval = super.getEndpointResponder(endpoint, sendType);
        if (wsc.isOpen()) {
            ping(endpoint.path);
        }
        return rval;
    }

    @Override
    protected void sendMessage(WebsocketObject wso) {
        wsc.send(gson.toJson(wso));
    }

    public static ImmortalsWebsocketConnector getInstance(int port) {
        if (instance == null) {
            instance = new ImmortalsWebsocketConnector(port);
        } else if (instance.port != port) {
            throw new RuntimeException("Cannot create a new ImmortalsWebsocketConnectionListener on port " + port +
                    " since it is already set up to use port " + instance.port + "!");
        }
        return instance;
    }

    public static synchronized void dispose() {
        if (instance != null) {
            instance.stop();
            instance = null;
        }
    }

    private ImmortalsWebsocketConnector(int port) {
        this.port = port;
        try {
            URI serverUri = new URI("ws://127.0.0.1:" + port);
            wsc = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.info("Opened Connection: " + handshakedata);
                }

                @Override
                public void onMessage(String message) {
                    receiveMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.info("Closed Connection: code: " + code + ", reason: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void start() {
        try {
            wsc.connectBlocking();
            if (receiverMap.size() > 0) {
                for (String endpoint : receiverMap.keySet()) {
                    ping(endpoint);
                }

            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void ping(String endpoint) {
        wsc.send("{\"endpoint\": \"" + endpoint + "\", \"data\": \"" + ENDPOINT_REGISTRATION_ENDPOINT + "\"}");
    }

    @Override
    public void stop() {
        try {
            if (wsc.isOpen()) {
                wsc.closeBlocking();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
