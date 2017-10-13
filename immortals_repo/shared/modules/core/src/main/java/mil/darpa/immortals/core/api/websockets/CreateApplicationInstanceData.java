package mil.darpa.immortals.core.api.websockets;

import mil.darpa.immortals.core.api.applications.ApplicationType;

/**
 * Created by awellman@bbn.com on 9/7/17.
 */
public class CreateApplicationInstanceData {

    public final String sessionIdentifier;
    public final ApplicationType applicationType;

    public CreateApplicationInstanceData(String sessionIdentifier, ApplicationType applicationType) {
        this.sessionIdentifier = sessionIdentifier;
        this.applicationType = applicationType;
    }

}
