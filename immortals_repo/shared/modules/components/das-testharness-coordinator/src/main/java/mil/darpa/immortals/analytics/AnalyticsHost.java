package mil.darpa.immortals.analytics;

import mil.darpa.immortals.analytics.validators.ValidatorManager;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.EndpointReceiver;
import mil.darpa.immortals.core.analytics.EndpointSender;
import mil.darpa.immortals.core.api.validation.ValidationStartData;
import mil.darpa.immortals.core.api.validation.results.ValidationResults;
import mil.darpa.immortals.core.api.websockets.WebsocketAck;
import mil.darpa.immortals.core.api.websockets.WebsocketEndpoint;
import mil.darpa.immortals.websockets.ImmortalsWebsocketConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

/**
 * Created by awellman@bbn.com on 8/28/17.
 */
public class AnalyticsHost implements AnalyticsEndpointInterface {

    private Logger logger = LoggerFactory.getLogger(AnalyticsHost.class);

    private ImmortalsWebsocketConnectionListener websocketListener;

    private final EndpointSender<WebsocketAck> requestResponder;

    private final EndpointSender<WebsocketAck> stopResponder;

    // Define a startReceiver
    private final EndpointReceiver<ValidationStartData> startReceiver = new EndpointReceiver<ValidationStartData>() {
        @Override
        public void receive(ValidationStartData data) {
            validatorManager = new ValidatorManager(data);
            logger.info("Received init with data: : " + data.clientIdentifiers);
            logger.info("Received init with validators: " + data.validatorIdentifiers);
            validatorManager.setEndpointSender(resultSender).start();
            validatorManager.start();
            requestResponder.send(WebsocketAck.OK);
        }
    };

    private final EndpointReceiver<String> stopReceiver = new EndpointReceiver<String>() {
        @Override
        public void receive(String data) {
            if (validatorManager != null) {
                validatorManager.shutdown();
            }
            stopResponder.send(WebsocketAck.OK);

        }
    };

//    private final EndpointReceiver<String> attemptFinishReceiver = new EndpointReceiver<String>() {
//        @Override
//        public void receive(String data) {
//            if (validatorManager != null) {
//                validatorManager.attempt_validation();
//            }
//        }
//    };

    private final EndpointSender<ValidationResults> resultSender;

    private ValidatorManager validatorManager;

    public AnalyticsHost(int port) throws URISyntaxException {

        // Create and start the websocket client
        websocketListener = ImmortalsWebsocketConnectionListener.getInstance(port);

        // Define the endpoint the start instructions are received from
        WebsocketEndpoint startEndpoint = WebsocketEndpoint.VALIDATION_START;

        // Define the endpoint to send the results to
        WebsocketEndpoint resultsEndpoint = WebsocketEndpoint.VALIDATION_RESULTS;


        // Get the request ack endpoint
        requestResponder = websocketListener.getEndpointResponder(startEndpoint, WebsocketAck.class);

        // Register the startReceiver with the websocket
        websocketListener.registerEndpointRequestListener(startEndpoint, startReceiver, ValidationStartData.class);

        // Register the stopReceiver with the websocket
        websocketListener.registerEndpointRequestListener(WebsocketEndpoint.VALIDATION_STOP, stopReceiver, String.class);
        stopResponder = websocketListener.getEndpointResponder(WebsocketEndpoint.VALIDATION_STOP, WebsocketAck.class);

        // Get the result sender
        resultSender = websocketListener.getEndpointSender(resultsEndpoint, ValidationResults.class);
        logger.debug("WebsocketAnalyticsHost: Created");
    }


    public void start() {
        websocketListener.start();
        logger.info("WebsocketAnalyticsHost: Started");
    }

    @Override
    public void log(AnalyticsEvent event) {
        if (validatorManager != null) {
            validatorManager.log(event);
        }

    }

    @Override
    public void shutdown() {
        stop();
    }

    public void stop() {
        if (validatorManager != null) {
            validatorManager.shutdown();
        }
        websocketListener.stop();
        validatorManager = null;
        logger.info("WebsocketAnalyticsHost: Stopped");
    }
}
