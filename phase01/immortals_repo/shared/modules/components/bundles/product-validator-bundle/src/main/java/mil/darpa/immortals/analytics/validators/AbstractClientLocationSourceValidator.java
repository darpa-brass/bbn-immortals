package mil.darpa.immortals.analytics.validators;

import com.google.gson.Gson;
import mil.darpa.immortals.analytics.validators.result.ValidatorResult;
import mil.darpa.immortals.analytics.validators.result.ValidatorState;
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

    private ValidatorResult state = new ValidatorResult(getValidatorName(), ValidatorState.RUNNING, null, null);

    private final HashMap<String, LinkedList<String>> howMap = new HashMap<>();

    abstract List<String> getValidLocationTags();

    String locationTagString = null;

    private final Gson gson = new Gson();

    public AbstractClientLocationSourceValidator(@Nonnull Set<String> clientIdentifiers) {
        for (String str : clientIdentifiers) {
            howMap.put(str, new LinkedList<>());
        }
    }

    @Override
    public synchronized void processEvent(AnalyticsEvent event) {
        if (event.type == EVENT_TYPE) {
            if (!howMap.containsKey(event.eventSource)) {
                howMap.put(event.eventSource, new LinkedList<>());
            }
            Coordinates data = gson.fromJson(event.data, Coordinates.class);
            if (!howMap.get(event.eventSource).contains(data.getProvider())) {
                howMap.get(event.eventSource).add(data.getProvider());
            }
        }
    }


    @Override
    public synchronized ValidatorResult attemptValidation(boolean terminalState) {
        if (state.currentState == ValidatorState.RUNNING) {
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
                            state = new ValidatorResult(getValidatorName(), ValidatorState.FAILURE, validationErrors, detailMessages);
                            return state;
                        } else {
                            detailMessages.add(clientIdentifier + "-[" + how + "]->");
                        }
                    }
                }
            }

            state = new ValidatorResult(
                    getValidatorName(),
                    (
                            !terminalState ? ValidatorState.RUNNING :
                                    validationErrors.isEmpty() ? ValidatorState.SUCCESS : ValidatorState.FAILURE
                    ),
                    validationErrors,
                    detailMessages
            );
        }
        return state;
    }

    public String toString() {
        return getValidatorName();
    }
}
