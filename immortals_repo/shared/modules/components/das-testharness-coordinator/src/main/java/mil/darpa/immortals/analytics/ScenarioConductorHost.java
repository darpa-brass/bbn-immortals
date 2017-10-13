package mil.darpa.immortals.analytics;

import mil.darpa.immortals.core.analytics.EndpointReceiver;
import mil.darpa.immortals.core.analytics.EndpointSender;
import mil.darpa.immortals.core.api.applications.ApplicationDeploymentDetails;
import mil.darpa.immortals.core.api.websockets.CreateApplicationInstanceData;
import mil.darpa.immortals.core.api.websockets.WebsocketEndpoint;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import mil.darpa.immortals.das.sourcecomposer.SourceComposer;
import mil.darpa.immortals.websockets.ImmortalsWebsocketConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by awellman@bbn.com on 8/28/17.
 */
public class ScenarioConductorHost {

    private Logger logger = LoggerFactory.getLogger(AnalyticsHost.class);

    private ImmortalsWebsocketConnectionListener websocketListener;

    private final EndpointSender<ApplicationDeploymentDetails> createApplicationInstanceResponder;

    private final EndpointReceiver<CreateApplicationInstanceData> createApplicationInstanceReceiver =
            new EndpointReceiver<CreateApplicationInstanceData>() {
                @Override
                public void receive(CreateApplicationInstanceData data) {
                    try {
                        SourceComposer sc = new SourceComposer(data.sessionIdentifier);
                        SourceComposer.ApplicationInstance ai = sc.initializeApplicationInstance(
                                EnvironmentConfiguration.CompositionTarget.valueOf(data.applicationType.name()));
                        ApplicationDeploymentDetails add =
                                ai.profileConfiguration.getAsApplicationDeploymentDetails(data.sessionIdentifier);
                        createApplicationInstanceResponder.send(add);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                        createApplicationInstanceResponder.send(null);
                    }

                }
            };

    public ScenarioConductorHost(int port) throws URISyntaxException {
        websocketListener = ImmortalsWebsocketConnectionListener.getInstance(port);

        EnvironmentConfiguration.initializeDefaultEnvironmentConfiguration();
        WebsocketEndpoint createApplicationInstance = WebsocketEndpoint.SOURCECOMPOSER_CREATE_APPLICATION_INSTANCE;
        
        websocketListener.registerEndpointRequestListener(createApplicationInstance,
                createApplicationInstanceReceiver, CreateApplicationInstanceData.class);

        createApplicationInstanceResponder = websocketListener.getEndpointResponder(createApplicationInstance,
                ApplicationDeploymentDetails.class);

        logger.debug("ScenarioConductorHost: Created");
    }

    public void start() {
        websocketListener.start();
        logger.info("ScenarioConductorHost: Started");
    }

    public void shutdown() {
        stop();
    }

    public void stop() {
        websocketListener.stop();
        logger.info("WebsocketAnalyticsHost: Stopped");
    }
}
