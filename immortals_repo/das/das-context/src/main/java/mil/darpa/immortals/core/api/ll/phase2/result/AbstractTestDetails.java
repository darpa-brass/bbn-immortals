package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
@Unstable
@Description("The current state of a test execution")
public abstract class AbstractTestDetails {


    @Description("The current state for the test")
    public TestOutcome currentState;

    @Description("An identifier for the test")
    public String testIdentifier;

    public String adaptationIdentifier;

    public AbstractTestDetails() {
    }

    public AbstractTestDetails(@Nonnull String testIdentifier, @Nonnull TestOutcome currentState, @Nonnull String adaptationIdentifier) {
        this.testIdentifier = testIdentifier;
        this.currentState = currentState;
        this.adaptationIdentifier = adaptationIdentifier;
    }
}
