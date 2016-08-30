package mil.darpa.immortals.analysis.validators;

import mil.darpa.immortals.analysis.analytics.ValidatorAppender;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public class ClientImageProduceValidator extends ValidatorAppender {

    final HashMap<String, Boolean> clientLocationUpdatedMap;

    int expectedClientCount = 2;

    public ClientImageProduceValidator(@Nonnull Set<String> clientIdentifiers) {
        clientLocationUpdatedMap = new HashMap<>();

        for (String clientIdentifier : clientIdentifiers) {
            clientLocationUpdatedMap.put(clientIdentifier, false);
        }
    }

    @Override
    public synchronized void processEvent(AnalyticsEvent event) {
        if (event.type == AnalyticsEventType.ClientStart) {
            if (!clientLocationUpdatedMap.containsKey(event.eventSource)) {
                clientLocationUpdatedMap.put(event.eventSource, false);
            }

        } else if (event.type == AnalyticsEventType.MyImageSent) {
            clientLocationUpdatedMap.put(event.eventSource, true);
        }
    }

    @Override
    public String getValidatorName() {
        return "client-image-produce";
    }

    @Override
    public boolean validate() {
        return getValidationErrors() == null;
    }

    @Override
    public synchronized List<String> getValidationErrors() {
        List<String> validationErrors = new LinkedList<>();

        if (clientLocationUpdatedMap.size() != expectedClientCount) {
            validationErrors.add("Only " + clientLocationUpdatedMap.size() + "/" + expectedClientCount + " clients have come online.");

        } else {
            for (String clientIdentifier : clientLocationUpdatedMap.keySet()) {
                if (!clientLocationUpdatedMap.get(clientIdentifier)) {
                    validationErrors.add(clientIdentifier + " has not produced an image!");
                }
            }
        }
        if (validationErrors.isEmpty()) {
            return null;
        } else {
            return validationErrors;
        }
    }
}
