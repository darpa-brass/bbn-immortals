package mil.darpa.immortals.analytics.validators.result;

import com.google.gson.Gson;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
public class ValidatorResult {

    private static transient final Gson gson = new Gson();

    public final String validatorIdentifier;
    public final ValidatorState currentState;
    public final LinkedList<String> errorMessages;
    public final LinkedList<String> detailMessages;

    public ValidatorResult(String validatorIdentifier, ValidatorState currentState, LinkedList<String> errorMessages, LinkedList<String> detailMessages) {
        this.validatorIdentifier = validatorIdentifier;
        this.currentState = currentState;
        this.errorMessages = (errorMessages == null ? new LinkedList<>() : new LinkedList<>(errorMessages));
        this.detailMessages = (detailMessages == null ? new LinkedList<>() : new LinkedList<>(detailMessages));
    }

    public String toString() {
        return gson.toJson(this, ValidatorResult.class);
    }
}
