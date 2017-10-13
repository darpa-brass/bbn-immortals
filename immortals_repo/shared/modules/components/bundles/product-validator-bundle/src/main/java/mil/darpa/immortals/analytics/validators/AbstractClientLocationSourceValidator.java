package mil.darpa.immortals.analytics.validators;

import com.google.gson.Gson;
import mil.darpa.immortals.core.api.ll.phase1.TestResult;
import mil.darpa.immortals.core.api.ll.phase1.Status;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 11/10/16.
 */
public abstract class AbstractClientLocationSourceValidator implements ValidatorInterface {

    private final AnalyticsEventType EVENT_TYPE = AnalyticsEventType.MyLocationProduced;

    private TestResult state = new TestResult(getValidatorIdentifier(), Status.PENDING, null, null);

    private final HashMap<String, LinkedList<String>> howMap = new HashMap<>();

    abstract List<String> getValidLocationTags();

    private String locationTagString = null;

    private final Gson gson = new Gson();

    AbstractClientLocationSourceValidator(@Nonnull Set<String> clientIdentifiers, boolean haltUponInitialValidation) {
        for (String str : clientIdentifiers) {
            howMap.put(str, new LinkedList<String>());
        }
    }

    @Override
    public synchronized void processEvent(AnalyticsEvent event) {
        if (state.currentState == Status.RUNNING && event.type == EVENT_TYPE) {
            if (!howMap.containsKey(event.eventSource)) {
                howMap.put(event.eventSource, new LinkedList<String>());
            }
            Coordinates data = gson.fromJson(event.data, Coordinates.class);
            if (!howMap.get(event.eventSource).contains(data.getProvider())) {
                howMap.get(event.eventSource).add(data.getProvider());
            }
        }
    }

    @Override
    public String getValidatorIdentifier() {
        return Validators.byClass(this.getClass()).identifier;
    }

    @Override
    public synchronized TestResult attemptValidation(boolean terminalState) {
        if (state.currentState == Status.RUNNING) {
            LinkedList<String> validationErrors = new LinkedList<>();
            LinkedList<String> detailMessages = new LinkedList<>();

            for (String clientIdentifier : howMap.keySet()) {
                LinkedList<String> howList = howMap.get(clientIdentifier);

                if (howList.isEmpty()) {
                    validationErrors.add(clientIdentifier + " has not produced any locations!");
                } else {
                    for (String how : howList) {
                        if (!getValidLocationTags().contains(how)) {
                            if (locationTagString == null) {
                                locationTagString = "[";
                                for (String tag : getValidLocationTags()) {
                                    locationTagString += (tag + ",");
                                }
                                locationTagString += "]";
                            }

                            validationErrors.add(clientIdentifier + " location of type " + how + " is not one of the valid location tags !");
                            state = new TestResult(getValidatorIdentifier(), Status.FAILURE, validationErrors, detailMessages);
                            return state;
                        } else {
                            detailMessages.add(clientIdentifier + "-[" + how + "]->");
                        }
                    }
                }
            }

            state = new TestResult(
                    getValidatorIdentifier(),
                    (
                            !terminalState ? Status.RUNNING :
                                    validationErrors.isEmpty() ? Status.SUCCESS : Status.FAILURE
                    ),
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

    public String toString() {
        return getValidatorIdentifier();
    }
}
