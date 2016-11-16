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

    public ValidatorResult(String validatorIdentifier, ValidatorState currentState, LinkedList<String> errorMessages) {
        this.validatorIdentifier = validatorIdentifier;
        this.currentState = currentState;
        if (errorMessages != null) {
            this.errorMessages = new LinkedList<>(errorMessages);
        } else {
            this.errorMessages = new LinkedList<>();
        }
    }

    public String toString() {
        return gson.toJson(this, ValidatorResult.class);
    }
}
