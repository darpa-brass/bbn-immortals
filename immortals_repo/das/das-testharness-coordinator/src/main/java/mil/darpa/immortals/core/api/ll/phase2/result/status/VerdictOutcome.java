package mil.darpa.immortals.core.api.ll.phase2.result.status;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Result;

/**
 * Created by awellman@bbn.com on 9/19/17.
 */
@Result
@Description("See LL Evaluation Methodology")
public enum VerdictOutcome {
    PENDING("The verdict outcome is pending"),
    RUNNING("The validation is running"),
    PASS("See LL Evaluation Methodology"),
    DEGRADED("See LL Evaluation Methodology"),
    FAIL("See LL Evaluation Methodology"),
    INCONCLUSIVE("See LL Evaluation Methodology"),
    INAPPLICABLE("See LL Evaluation Methodology"),
    ERROR("See LL Evaluation Methodology");
    
    public final String description;
    
    VerdictOutcome(String description) {
        this.description = description;
    }
}
