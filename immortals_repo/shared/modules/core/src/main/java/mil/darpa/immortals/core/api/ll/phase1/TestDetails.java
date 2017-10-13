package mil.darpa.immortals.core.api.ll.phase1;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
public class TestDetails {
    public final String testIdentifier;
    public final Status expectedStatus;
    public final Status actualStatus;
    public final TestResult details;

    public TestDetails(String testIdentifier, Status expectedStatus, Status actualStatus, TestResult details) {
        this.testIdentifier = testIdentifier;
        this.expectedStatus = expectedStatus;
        this.actualStatus = actualStatus;
        this.details = details;
    }
}
