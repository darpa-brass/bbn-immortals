package mil.darpa.immortals.core.api.ll.phase2.result.status;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Result;

/**
 * Created by awellman@bbn.com on 9/19/17.
 */
@Result
@Description("The outcome of a test")
public enum TestOutcome {
    PENDING("Indicates the specified action is pending (non-terminal)"),
    RUNNING("Indicates the specified action is running (non-terminal"),
    NOT_APPLICABLE("Indicates the specified action is not applicable"),
    COMPLETE("See LL Evaluation Methodology"),
    INVALID("See LL Evaluation Methodology"),
    INCOMPLETE("See LL Evaluation Methodology"),
    ERROR("See LL Evaluation Methodology");

    public final String description;

    TestOutcome(String description) {
        this.description = description;
    }
}
