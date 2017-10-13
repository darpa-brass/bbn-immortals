package mil.darpa.immortals.core.api.websockets;


import mil.darpa.immortals.core.api.applications.ApplicationDeploymentDetails;
import mil.darpa.immortals.core.api.validation.ValidationStartData;
import mil.darpa.immortals.core.api.validation.results.ValidationResults;

/**
 * Created by awellman@bbn.com on 8/17/17.
 */
public enum WebsocketEndpoint {
    VALIDATION_START("/validation/start", ValidationStartData.class, WebsocketAck.class),
    //    VALIDATION_ATTEMPT_FINISH("/validation/attempt_finish", String.class, WebsocketAck.class),
    // TODO: Allow empty messages to be sent instead of empty strings!
    VALIDATION_STOP("/validation/stop", String.class, WebsocketAck.class),
    VALIDATION_RESULTS("/validation/results", ValidationResults.class, WebsocketAck.class),
    SOURCECOMPOSER_CREATE_APPLICATION_INSTANCE("/applications/createInstance", CreateApplicationInstanceData.class, ApplicationDeploymentDetails.class);


    public final String path;
    public final Class postType;
    public final Class ackType;

    WebsocketEndpoint(String path, Class<?> postType, Class<?> ackType) {
        this.path = path;
        this.postType = postType;
        this.ackType = ackType;
    }
}
