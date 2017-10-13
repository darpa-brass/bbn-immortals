package mil.darpa.immortals.websockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.core.analytics.EndpointReceiver;
import mil.darpa.immortals.core.analytics.EndpointSender;
import mil.darpa.immortals.core.api.websockets.WebsocketEndpoint;
import mil.darpa.immortals.core.api.websockets.WebsocketObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by awellman@bbn.com on 8/28/17.
 */
public abstract class AbstractImmortalsWebsocket {

    protected final Logger logger = LoggerFactory.getLogger(AbstractImmortalsWebsocket.class);

    protected static final String ENDPOINT_REGISTRATION_ENDPOINT = "/immortals/websocket/endpoint_registration";

    static Gson gson = new GsonBuilder().create();

    protected AtomicBoolean isRunning = new AtomicBoolean(false);

    protected final HashMap<String, EndpointReceiver<String>> receiverMap = new HashMap<>();

    public synchronized <C> void registerEndpointResponseListener(final WebsocketEndpoint endpoint, final EndpointReceiver<C> receiver, final Class<C> rxType) {
        synchronized (receiverMap) {
            if (receiverMap.containsKey(endpoint.path)) {
                throw new RuntimeException("Endpoint already registered to the endpoint \"" + endpoint.path + "\"!");

            } else {
                assert (endpoint.ackType == rxType);

                receiverMap.put(endpoint.path, new EndpointReceiver<String>() {
                    @Override
                    public void receive(String data) {
                        receiver.receive(gson.fromJson(data, rxType));
                    }
                });
            }
        }
    }


    public synchronized <P> void registerEndpointRequestListener(final WebsocketEndpoint endpoint, final EndpointReceiver<P> receiver, final Class<P> rxType) {
        synchronized (receiverMap) {
            if (receiverMap.containsKey(endpoint.path)) {
                throw new RuntimeException("Endpoint already registered to the endpoint \"" + endpoint.path + "\"!");

            } else {
                assert (endpoint.postType == rxType);

                receiverMap.put(endpoint.path, new EndpointReceiver<String>() {
                    @Override
                    public void receive(String data) {
                        receiver.receive(gson.fromJson(data, rxType));
                    }
                });
            }
            if (isRunning.get()) {
                logger.info("Ready to take submissions to endpoint '" + endpoint.path + "'.");
            }
        }
    }

    public synchronized <T> EndpointSender<T> getEndpointSender(final WebsocketEndpoint endpoint, final Class<T> sendType) {
        assert (endpoint.postType == sendType);

        return new EndpointSender<T>() {
            @Override
            public void send(T data) {
                WebsocketObject wso = new WebsocketObject(endpoint.path, gson.toJson(data));
                sendMessage(wso);
            }
        };
    }

    public synchronized <T> EndpointSender<T> getEndpointResponder(final WebsocketEndpoint endpoint, final Class<T> sendType) {
        assert (endpoint.ackType == sendType);

        return new EndpointSender<T>() {
            @Override
            public void send(T data) {
                WebsocketObject wso = new WebsocketObject(endpoint.path, gson.toJson(data));
                sendMessage(wso);
            }
        };
    }


    void receiveMessage(String message) {
        WebsocketObject wso = gson.fromJson(message, WebsocketObject.class);
        receiveMessage(wso);
    }

    void receiveMessage(WebsocketObject wso) {
        EndpointReceiver<String> er;
        synchronized (receiverMap) {
            if (receiverMap.containsKey(wso.endpoint)) {
                er = receiverMap.get(wso.endpoint);
            } else {
                throw new RuntimeException("Message received for unregistered endpoint " + wso.endpoint + "!");
            }
        }
        er.receive(wso.data);
    }


    protected abstract void sendMessage(WebsocketObject wso);

    public abstract void start();

    public abstract void stop();
}
