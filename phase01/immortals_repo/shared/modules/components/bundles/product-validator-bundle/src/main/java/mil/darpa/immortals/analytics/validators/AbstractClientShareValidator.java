package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.analytics.validators.result.ValidatorResult;
import mil.darpa.immortals.analytics.validators.result.ValidatorState;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Abstract validator for events sent from all clients to all clients minus the sender client
 * <p>
 * Created by awellman@bbn.com on 11/11/16.
 */
public abstract class AbstractClientShareValidator implements ValidatorInterface {
    private ValidatorResult state = new ValidatorResult(getValidatorName(), ValidatorState.RUNNING, null, null);

    private final Map<String, HashMap<String, Boolean>> clientReceivedMap = new HashMap<>();
    private final int expectedClientCount;

    public AbstractClientShareValidator(@Nonnull Set<String> clientIdentifiers) {
        for (String recipient : clientIdentifiers) {
            HashMap<String, Boolean> map = new HashMap<>();
            clientReceivedMap.put(recipient, map);

            for (String sender : clientIdentifiers) {
                if (!recipient.equals(sender)) {
                    map.put(sender, false);
                }
            }
        }
        expectedClientCount = clientIdentifiers.size();
    }

    protected abstract AnalyticsEventType getDesiredEventType();

    public synchronized void processEvent(AnalyticsEvent event) {
        if (event.type == getDesiredEventType()) {
            if (!clientReceivedMap.containsKey(event.eventSource)) {
                HashMap<String, Boolean> clientMap = new HashMap<>();

                for (String recipient : clientReceivedMap.keySet()) {
                    clientMap.put(recipient, false);
                }
                clientReceivedMap.put(event.eventSource, clientMap);
            }
            clientReceivedMap.get(event.eventSource).put(event.eventRemoteSource, true);
        }
    }

    @Override
    public synchronized ValidatorResult attemptValidation() {
        if (state.currentState == ValidatorState.RUNNING) {
            LinkedList<String> validationErrors = new LinkedList<>();
            LinkedList<String> detailMessages = new LinkedList<>();

            if (clientReceivedMap.keySet().size() != expectedClientCount) {
                validationErrors.add(clientReceivedMap.keySet().size() + "/" + expectedClientCount + " of expected clients came online!");
            } else {
                for (String receiver : clientReceivedMap.keySet()) {
                    HashMap<String, Boolean> receivedMap = clientReceivedMap.get(receiver);

                    for (String sender : receivedMap.keySet()) {
                        if (!receivedMap.get(sender)) {
                            validationErrors.add(receiver + " has not received a " + getDesiredEventType().name() + " event from " + sender + "!");
                        } else {
                            detailMessages.add(receiver + "<-" + getDesiredEventType().name() + "-" + sender);
                        }
                    }
                }
            }

            if (validationErrors.isEmpty()) {
                ValidatorResult vs = new ValidatorResult(getValidatorName(), ValidatorState.PASSED, validationErrors, detailMessages);
                state = vs;
                return vs;

            } else {
                state = new ValidatorResult(getValidatorName(), ValidatorState.RUNNING, validationErrors, detailMessages);
            }
        }
        return state;
    }

    public String toString() {
        return getValidatorName();
    }
}
