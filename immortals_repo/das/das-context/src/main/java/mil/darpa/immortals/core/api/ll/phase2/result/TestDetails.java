package mil.darpa.immortals.core.api.ll.phase2.result;

import com.google.gson.Gson;
import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
@Unstable
@Description("The current state of a test execution")
public class TestDetails extends AbstractTestDetails {

    private static transient Gson gson = new Gson();
    
    @Description("Messages indicating the reasons for failure")
    public LinkedList<String> errorMessages;
    @Description("Messages indicating the reasons for success")
    public LinkedList<String> detailMessages;

    public TestDetails() {
    }

    public TestDetails(@Nonnull String testIdentifier, @Nonnull TestOutcome currentState, @Nonnull String adaptationIdentifier, @Nullable LinkedList<String> errorMessages, @Nullable LinkedList<String> detailMessages) {
        super(testIdentifier, currentState, adaptationIdentifier);
        this.errorMessages = (errorMessages == null ? new LinkedList<>() : new LinkedList<>(errorMessages));
        this.detailMessages = (detailMessages == null ? new LinkedList<>() : new LinkedList<>(detailMessages));
    }

    public String toString() {
        return gson.toJson(this, TestDetails.class);
    }

    /**
     * Convenience method to encourage treating these atomically to avoid modification of objects in-transit
     *
     * @return Cloned {@link TestDetails object}
     */
    public TestDetails clone() {
        return new TestDetails(testIdentifier, currentState, adaptationIdentifier, errorMessages, detailMessages);
    }
}
