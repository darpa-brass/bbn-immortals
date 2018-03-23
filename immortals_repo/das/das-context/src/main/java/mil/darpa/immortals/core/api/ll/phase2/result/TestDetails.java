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

    public TestDetails(@Nonnull String testIdentifier, @Nonnull TestOutcome currentState, @Nonnull String adaptationIdentifier) {
        super(testIdentifier, currentState, adaptationIdentifier);
        this.errorMessages = new LinkedList<>();
        this.detailMessages = new LinkedList<>();
    }

    public TestDetails(@Nonnull String testIdentifier, @Nonnull TestOutcome currentState, @Nonnull String adaptationIdentifier, @Nonnull LinkedList<String> errorMessages, @Nonnull LinkedList<String> detailMessages) {
        super(testIdentifier, currentState, adaptationIdentifier);
        this.errorMessages = new LinkedList<>(errorMessages);
        this.detailMessages = new LinkedList<>(detailMessages);
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

    private TestDetails duplicate() {
        return new TestDetails(testIdentifier, currentState, adaptationIdentifier, errorMessages, detailMessages);
    }
    
    public  synchronized TestDetails produceUpdate(@Nonnull TestOutcome currentState) {
        TestDetails td = duplicate();
        td.currentState = currentState;
        return td;
    }

    public synchronized TestDetails produceUpdate(@Nonnull TestOutcome currentState, @Nullable LinkedList<String> errorMessages, @Nullable LinkedList<String> detailMessages) {
        TestDetails ad = duplicate();
        ad.currentState = currentState;
        if (errorMessages != null) {
            ad.errorMessages.addAll(errorMessages);
        }
        if (detailMessages != null) {
            ad.detailMessages.addAll(detailMessages);
        }
        return ad;
    }


    public synchronized TestDetails produceUpdate(@Nonnull TestOutcome currentState, @Nullable LinkedList<String> errorMessages, @Nullable String detailMessage) {
        TestDetails ad = duplicate();
        ad.currentState = currentState;
        if (errorMessages != null) {
            ad.errorMessages.addAll(errorMessages);
        }
        if (detailMessage != null) {
            ad.detailMessages.add(detailMessage);
        }
        return ad;
    }


    public synchronized TestDetails produceUpdate(@Nonnull TestOutcome currentState, @Nullable String errorMessage, @Nullable LinkedList<String> detailMessages) {
        TestDetails ad = duplicate();
        ad.currentState = currentState;
        if (errorMessage != null) {
            ad.errorMessages.add(errorMessage);
        }
        if (detailMessages != null) {
            ad.detailMessages.addAll(detailMessages);
        }
        return ad;
    }


    public synchronized TestDetails produceUpdate(@Nonnull TestOutcome currentState, @Nullable String errorMessage, @Nullable String detailMessage) {
        TestDetails ad = duplicate();
        ad.currentState = currentState;
        if (errorMessage != null) {
            ad.errorMessages.add(errorMessage);
        }
        if (detailMessage != null) {
            ad.detailMessages.add(detailMessage);
        }
        return ad;
    }
}
