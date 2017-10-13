package mil.darpa.immortals.websockets;

import mil.darpa.immortals.core.analytics.EndpointReceiver;
import mil.darpa.immortals.core.analytics.EndpointSender;
import mil.darpa.immortals.core.api.validation.ValidationStartData;
import mil.darpa.immortals.core.api.websockets.WebsocketAck;
import mil.darpa.immortals.core.api.websockets.WebsocketEndpoint;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by awellman@bbn.com on 8/29/17.
 */
public class WebsocketTests {

    // Define the endpoint (Stored as an enum for clear coordination between different components
    private static final WebsocketEndpoint we = WebsocketEndpoint.VALIDATION_START;

    private static final LinkedList<String> validatorIdentifierSource = new LinkedList<>();

    static {
        validatorIdentifierSource.add("ONE");
        validatorIdentifierSource.add("TWO");
        validatorIdentifierSource.add("THREE");
        validatorIdentifierSource.add("FOUR");
    }

    private static final LinkedList<String> clientIdentifierSource = new LinkedList<>();

    static {
        clientIdentifierSource.add("one");
        clientIdentifierSource.add("two");
        clientIdentifierSource.add("three");
        clientIdentifierSource.add("four");
    }

    private final AtomicBoolean serverReceivedRequest = new AtomicBoolean(false);
    private final AtomicBoolean clientReceivedAck = new AtomicBoolean(false);

    private EndpointSender<ValidationStartData> setupClient(AbstractImmortalsWebsocket client) {
        final EndpointSender<ValidationStartData> sender = client.getEndpointSender(we, ValidationStartData.class);
        // Define a receiver
        EndpointReceiver<WebsocketAck> receiver = new EndpointReceiver<WebsocketAck>() {
            @Override
            public void receive(WebsocketAck data) {
                System.out.println("Client received Reply: " + data.name());
                clientReceivedAck.set(true);
            }
        };
        client.registerEndpointResponseListener(we, receiver, WebsocketAck.class);

        return sender;
    }

    private void setupServer(AbstractImmortalsWebsocket server) {
        final EndpointSender<WebsocketAck> serverResponder = server.getEndpointResponder(we, WebsocketAck.class);
        final EndpointReceiver<ValidationStartData> serverReceiver = new EndpointReceiver<ValidationStartData>() {
            @Override
            public void receive(ValidationStartData data) {
                System.out.println("SERVER RECEIVED: ");
                System.out.println("Received init with clients : " + data.clientIdentifiers);
                System.out.println("Received init with validators: " + data.validatorIdentifiers);

                serverReceivedRequest.set(true);
                serverResponder.send(WebsocketAck.OK);
            }
        };
        server.registerEndpointRequestListener(we, serverReceiver, ValidationStartData.class);
    }

    private void execute(EndpointSender<ValidationStartData> clientSender) throws InterruptedException {
//            public ValidationStartData(LinkedList<String> validatorIdentifiers, LinkedList<String> clientIdentifiers,
//                String sessionIdentifier, int maxRuntimeMS, int minRuntimeMS) {
        ValidationStartData vsd = new ValidationStartData(
                validatorIdentifierSource, clientIdentifierSource,
                "sessionIdentifier", 60000, 80000);
        clientSender.send(vsd);

        long timeoutMS = 5000;

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() < (startTime + timeoutMS)) && !(serverReceivedRequest.get() && clientReceivedAck.get())) {
            Thread.sleep(100);
        }
    }

    @Test
    public void connectionListenerServerConnectorClient() {
        try {
            serverReceivedRequest.set(false);
            clientReceivedAck.set(false);

            ImmortalsWebsocketConnectionListener client = ImmortalsWebsocketConnectionListener.getInstance(9595);
            EndpointSender<ValidationStartData> clientSender = setupClient(client);

            ImmortalsWebsocketConnector server = ImmortalsWebsocketConnector.getInstance(9595);
            setupServer(server);

            client.start();
            server.start();

            Thread.sleep(1000);

            execute(clientSender);

            Assert.assertTrue(serverReceivedRequest.get());
            Assert.assertTrue(clientReceivedAck.get());

            server.stop();
            client.stop();

        } catch (InterruptedException e) {
            Assert.fail("Exception Raised!");
            throw new RuntimeException(e);
        } finally {
            ImmortalsWebsocketConnectionListener.dispose();
            ImmortalsWebsocketConnector.dispose();
        }
    }

    @Test
    public void connectionListenerServerConnectorClientEarlyStart() {
        try {
            serverReceivedRequest.set(false);
            clientReceivedAck.set(false);

            ImmortalsWebsocketConnectionListener client = ImmortalsWebsocketConnectionListener.getInstance(9595);
            ImmortalsWebsocketConnector server = ImmortalsWebsocketConnector.getInstance(9595);

            client.start();
            server.start();

            EndpointSender<ValidationStartData> clientSender = setupClient(client);

            setupServer(server);

            Thread.sleep(1000);

            execute(clientSender);

            Assert.assertTrue(serverReceivedRequest.get());
            Assert.assertTrue(clientReceivedAck.get());

            server.stop();
            client.stop();

        } catch (InterruptedException e) {
            Assert.fail("Exception Raised!");
            throw new RuntimeException(e);
        } finally {
            ImmortalsWebsocketConnectionListener.dispose();
            ImmortalsWebsocketConnector.dispose();
        }
    }

    @Test
    public void connectionListenerClientConnectorServer() {
        try {
            serverReceivedRequest.set(false);
            clientReceivedAck.set(false);

            ImmortalsWebsocketConnector client = ImmortalsWebsocketConnector.getInstance(9595);
            EndpointSender<ValidationStartData> clientSender = setupClient(client);

            ImmortalsWebsocketConnectionListener server = ImmortalsWebsocketConnectionListener.getInstance(9595);
            setupServer(server);

            server.start();
            client.start();

            Thread.sleep(1000);

            execute(clientSender);

            Assert.assertTrue(serverReceivedRequest.get());
            Assert.assertTrue(clientReceivedAck.get());

            client.stop();
            server.stop();

        } catch (InterruptedException e) {
            Assert.fail("Exception Raised!");
            throw new RuntimeException(e);
        } finally {
            ImmortalsWebsocketConnectionListener.dispose();
            ImmortalsWebsocketConnector.dispose();
        }
    }


    @Test
    public void connectionListenerClientConnectorServerEarlyStart() {
        try {
            serverReceivedRequest.set(false);
            clientReceivedAck.set(false);

            ImmortalsWebsocketConnector client = ImmortalsWebsocketConnector.getInstance(9595);
            ImmortalsWebsocketConnectionListener server = ImmortalsWebsocketConnectionListener.getInstance(9595);

            server.start();
            client.start();

            EndpointSender<ValidationStartData> clientSender = setupClient(client);
            setupServer(server);

            Thread.sleep(1000);

            execute(clientSender);

            Assert.assertTrue(serverReceivedRequest.get());
            Assert.assertTrue(clientReceivedAck.get());

            client.stop();
            server.stop();

        } catch (InterruptedException e) {
            Assert.fail("Exception Raised!");
            throw new RuntimeException(e);
        } finally {
            ImmortalsWebsocketConnectionListener.dispose();
            ImmortalsWebsocketConnector.dispose();
        }
    }
}
