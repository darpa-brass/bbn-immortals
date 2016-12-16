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
import java.util.Set;

/**
 * Created by awellman@bbn.com on 11/10/16.
 */
public class TrustedLocationValidator implements ValidatorInterface {

    private final AnalyticsEventType EVENT_TYPE = AnalyticsEventType.MyLocationProduced;

    private ValidatorResult state = new ValidatorResult(getValidatorName(), ValidatorState.RUNNING, null, null);

    private final HashMap<String, LinkedList<String>> howMap = new HashMap<>();


    private final Gson gson = new Gson();

    public TrustedLocationValidator(@Nonnull Set<String> clientIdentifiers) {
        for (String str : clientIdentifiers) {
            howMap.put(str, new LinkedList<>());
        }

//        expectedClients = new HashSet<>(clientIdentifiers);
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
    public String getValidatorName() {
        return "client-location-trusted";
    }

    @Override
    public synchronized ValidatorResult attemptValidation() {
        if (state.currentState == ValidatorState.RUNNING) {
            LinkedList<String> validationErrors = new LinkedList<>();
            LinkedList<String> detailMessages = new LinkedList<>();

            for (String clientIdentifier : howMap.keySet()) {
                LinkedList<String> howList = howMap.get(clientIdentifier);

                if (howList.isEmpty()) {
                    validationErrors.add(clientIdentifier + " has not produced any locations!");
                } else {
                    for (String how : howList) {
                        if (!how.startsWith("m-r-p") && !how.startsWith("m-r-e") && !how.startsWith("m-r-t")) {
                            validationErrors.add(clientIdentifier + " has produced an untrusted location of type " + how + "!");
                            state = new ValidatorResult(getValidatorName(), ValidatorState.FAILED, validationErrors, detailMessages);
                            return state;
                        } else {
                            detailMessages.add(clientIdentifier + "-[" + how + "]->");
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
