package mil.darpa.immortals.websockets;

import mil.darpa.immortals.core.api.websockets.WebsocketObject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 8/28/17.
 */
public class ImmortalsWebsocketConnectionListener extends AbstractImmortalsWebsocket {

    private final WebSocketServer wss;
    
    private final int port;

    private final HashMap<String, WebSocket> connectionMap;

    private Logger logger = LoggerFactory.getLogger(ImmortalsWebsocketConnectionListener.class);
    
    private static ImmortalsWebsocketConnectionListener instance;

    @Override
    protected void sendMessage(WebsocketObject wso) {
        WebSocket ws;
        synchronized (this.connectionMap) {
            if (this.connectionMap.containsKey(wso.endpoint)) {
                ws = this.connectionMap.get(wso.endpoint);
            } else {
                throw new RuntimeException("No client is registered to the endpoint \"" + wso.endpoint + "\"!");
            }
        }
        ws.send(gson.toJson(wso));
    }

    @Override
    public synchronized void start() {
        if (!isRunning.get()) {
            wss.start();
            isRunning.set(true);
            logger.info("Ready to take connections at ws://127.0.0.1:" + wss.getPort());
        }
    }

    @Override
    public synchronized void stop() {
        if (isRunning.get()) {
            try {
                wss.stop();
                isRunning.set(false);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static synchronized void dispose() {
        if (instance != null) {
            instance.stop();
            instance = null;
        }
    }
    
    public synchronized static ImmortalsWebsocketConnectionListener getInstance(int port) {
        if (instance == null) {
            instance = new ImmortalsWebsocketConnectionListener(port);
        } else if (instance.port != port) {
            throw new RuntimeException("Cannot create a new ImmortalsWebsocketConnectionListener on port " + port +
                    " since it is already set up to use port " + instance.port + "!");
        }
        return instance;
    }

    private ImmortalsWebsocketConnectionListener(int port) {
        this.connectionMap = new HashMap<>();
        this.port = port;

        final ImmortalsWebsocketConnectionListener that = this;
        wss = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                logger.info("Opened Connection: " + handshake);
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                logger.info("Closed Connection: code: " + code + ", reason: " + reason);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                WebsocketObject wso = gson.fromJson(message, WebsocketObject.class);
                synchronized (that.connectionMap) {
                    if (!that.connectionMap.containsKey(wso.endpoint)) {
                        if (ENDPOINT_REGISTRATION_ENDPOINT.equals(wso.endpoint)) {
                            logger.debug("Registering endpoint: " + wso.data);
                            that.connectionMap.put(wso.data, conn);
                            return;
                        } else {
                            that.connectionMap.put(wso.endpoint, conn);
                        }
                    }
                }
                receiveMessage(wso);
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onStart() {

            }
        };

    }
}
