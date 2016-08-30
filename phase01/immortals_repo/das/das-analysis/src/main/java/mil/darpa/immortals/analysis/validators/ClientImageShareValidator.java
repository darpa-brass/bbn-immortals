package mil.darpa.immortals.analysis.validators;

import mil.darpa.immortals.analysis.analytics.ValidatorAppender;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by awellman@bbn.com on 7/27/16.
 */
public class ClientImageShareValidator extends ValidatorAppender {

    final Set<String> clientIdentifiers = new HashSet<>();

    final Map<String, HashMap<String, Boolean>> clientReceivedMap;

    int expectedClientCount = 2;

    public ClientImageShareValidator(@Nonnull Set<String> clientIdentifiers) {
        int clientCount = clientIdentifiers.size();
        clientReceivedMap = new HashMap<>(clientCount);
        // TODO: Reenable this!
//
//        for (String identifier : clientIdentifiers) {
//            clientReceivedMap.put(identifier, new HashMap<String,>(clientIdentifiers));
//        }
    }

    @Override
    protected synchronized void processEvent(AnalyticsEvent event) {
        String eventSource = event.eventSource;
        if (event.type == AnalyticsEventType.ClientStart) {
            clientIdentifiers.add(event.eventSource);
            HashMap<String, Boolean> eventMap = new HashMap<>();

            if (!clientReceivedMap.containsKey(eventSource)) {
                clientReceivedMap.put(eventSource, eventMap);
            }

            for (String target : clientReceivedMap.keySet()) {
                if (!target.equals(eventSource)) {
                    eventMap.put(target, false);
                    if (!clientReceivedMap.get(target).containsKey(eventSource)) {
                        clientReceivedMap.get(target).put(eventSource, false);
                    }
                }
            }

        } else if (event.type == AnalyticsEventType.FieldImageReceived) {
            clientReceivedMap.get(event.eventSource).put(event.eventRemoteSource, true);
        }
    }

    @Override
    public String getValidatorName() {
        return "client-image-share";
    }

    @Override
    public boolean validate() {
        return clientReceivedMap.isEmpty();
    }

    @Override
    public synchronized List<String> getValidationErrors() {
        List<String> validationErrors = new LinkedList<>();

        if (clientIdentifiers.size() != expectedClientCount) {
            validationErrors.add("Only " + clientIdentifiers.size() + "/" + expectedClientCount + " clients have come online.");

        } else {
            for (String receiver : clientReceivedMap.keySet()) {
                HashMap<String, Boolean> receivedMap = clientReceivedMap.get(receiver);

                for (String sender : receivedMap.keySet()) {
                    if (!receivedMap.get(sender)) {
                        validationErrors.add(receiver + "<-![image]-" + sender);
                    }
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
