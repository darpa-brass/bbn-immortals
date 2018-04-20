package mil.darpa.immortals.core.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by awellman@bbn.com on 4/10/18.
 */
public class TestCaseReport {

    private final String testCaseTarget;
    private final String testCaseIdentifier;
    private final double duration;
    private final String failureMessage;
    private final CopyOnWriteArraySet<String> validatedFunctionality;

    public TestCaseReport(@Nonnull String testCaseTarget, @Nonnull String testCaseIdentifier, double duration,
                          @Nullable String failureMessage, @Nullable Collection<String> validatedFunctionality) {
        this.testCaseTarget = testCaseTarget;
        this.testCaseIdentifier = testCaseIdentifier;
        this.duration = duration;
        this.failureMessage = failureMessage;

        if (validatedFunctionality == null) {
            this.validatedFunctionality = new CopyOnWriteArraySet<>();
        } else {
            this.validatedFunctionality = new CopyOnWriteArraySet<>(validatedFunctionality);
        }
    }

    public String getTestCaseTarget() {
        return testCaseTarget;
    }

    public String getTestCaseIdentifier() {
        return testCaseIdentifier;
    }

    public double getDuration() {
        return duration;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public synchronized CopyOnWriteArraySet<String> getValidatedFunctionality() {
        return validatedFunctionality;
    }

    public TestCaseReport clone() {
        return new TestCaseReport(testCaseTarget, testCaseIdentifier, duration, failureMessage, validatedFunctionality);
    }
}
