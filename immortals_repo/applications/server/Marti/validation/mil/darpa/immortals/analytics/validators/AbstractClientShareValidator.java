package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.core.api.ll.phase1.TestResult;
import mil.darpa.immortals.core.api.ll.phase1.Status;
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
    private TestResult state = new TestResult(getValidatorIdentifier(), Status.PENDING, null, null);

    private final Map<String, HashMap<String, Boolean>> clientReceivedMap = new HashMap<>();
    private final int expectedClientCount;
    private final boolean haltUponSuccessfulValidation;

    AbstractClientShareValidator(@Nonnull Set<String> clientIdentifiers, boolean haltUponSuccessfulValidation) {
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
        this.haltUponSuccessfulValidation = haltUponSuccessfulValidation;
    }

    protected abstract AnalyticsEventType getDesiredEventType();

    public synchronized void processEvent(AnalyticsEvent event) {
        if (state.currentState == Status.RUNNING && event.type == getDesiredEventType()) {
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
    public synchronized TestResult attemptValidation(boolean terminalState) {
        if (state.currentState == Status.RUNNING) {
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
            
            Status newState;
            
            if (terminalState) {
                newState = validationErrors.isEmpty() ? Status.SUCCESS : Status.FAILURE;
                
            } else if (haltUponSuccessfulValidation && validationErrors.isEmpty()) {
                newState = Status.SUCCESS;
                
            } else {
                newState = Status.RUNNING;
            }
            
            state = new TestResult(
                    getValidatorIdentifier(),
                    newState,
                    validationErrors,
                    detailMessages
            );
        }
        return state;
    }

    @Override
    public void start() {
        state = new TestResult(getValidatorIdentifier(), Status.RUNNING, null, null);
    }

    @Override
    public String getValidatorIdentifier() {
        return Validators.byClass(this.getClass()).identifier;
    }

    public String toString() {
        return getValidatorIdentifier();
    }
}
