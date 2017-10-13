package mil.darpa.immortals.core.api.ll.phase1;

import com.google.gson.Gson;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
public class TestResult {

    private static transient final Gson gson = new Gson();

    public final String validatorIdentifier;
    public final Status currentState;
    public final LinkedList<String> errorMessages;
    public final LinkedList<String> detailMessages;

    public TestResult(String validatorIdentifier, Status currentState, LinkedList<String> errorMessages, LinkedList<String> detailMessages) {
        this.validatorIdentifier = validatorIdentifier;
        this.currentState = currentState;
        this.errorMessages = (errorMessages == null ? new LinkedList<String>() : new LinkedList<>(errorMessages));
        this.detailMessages = (detailMessages == null ? new LinkedList<String>() : new LinkedList<>(detailMessages));
    }

    public String toString() {
        return gson.toJson(this, TestResult.class);
    }
}
