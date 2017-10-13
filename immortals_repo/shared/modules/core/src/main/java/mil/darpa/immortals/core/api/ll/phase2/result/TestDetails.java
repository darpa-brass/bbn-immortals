package mil.darpa.immortals.core.api.ll.phase2.result;

import com.google.gson.Gson;
import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
@Unstable
@Description("The current state of a test execution")
public class TestDetails {

    private static transient Gson gson = new Gson();

    @Description("An identifier for the test")
    public String testIdentifier;
    @Description("The current state for the test")
    public TestOutcome currentState;
    @Description("Messages indicating the reasons for failure")
    public LinkedList<String> errorMessages;
    @Description("Messages indicating the reasons for success")
    public LinkedList<String> detailMessages;

    public TestDetails() {
    }

    public TestDetails(String testIdentifier, TestOutcome currentState, LinkedList<String> errorMessages, LinkedList<String> detailMessages) {
        this.testIdentifier = testIdentifier;
        this.currentState = currentState;
        this.errorMessages = (errorMessages == null ? new LinkedList<String>() : new LinkedList<>(errorMessages));
        this.detailMessages = (detailMessages == null ? new LinkedList<String>() : new LinkedList<>(detailMessages));
    }

    public String toString() {
        return gson.toJson(this, TestDetails.class);
    }
}
