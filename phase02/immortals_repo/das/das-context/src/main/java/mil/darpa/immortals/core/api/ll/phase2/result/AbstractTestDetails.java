package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
@Unstable
@Description("The current state of a test execution")
public abstract class AbstractTestDetails {

    @Description("A timestamp for the status update")
    public long timestamp;

    @Description("The current state for the test")
    public TestOutcome currentState;

    @Description("An identifier for the test")
    public String testIdentifier;

    @Description("The identifier for the adaptation instance")
    public String adaptationIdentifier;
    
    @Description("The functionality this test validates")
    public HashSet<String> validatedFunctionality;

    public AbstractTestDetails() {
    }

    public AbstractTestDetails(@Nonnull String testIdentifier, @Nonnull TestOutcome currentState, 
                               @Nonnull String adaptationIdentifier, @Nonnull Collection<String> validatedFunctionality) {
        this.timestamp = System.currentTimeMillis();
        this.testIdentifier = testIdentifier;
        this.currentState = currentState;
        this.adaptationIdentifier = adaptationIdentifier;
        this.validatedFunctionality = new HashSet<>(validatedFunctionality);
    }
}
