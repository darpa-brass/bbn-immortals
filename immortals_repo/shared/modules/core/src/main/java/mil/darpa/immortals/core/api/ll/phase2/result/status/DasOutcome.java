package mil.darpa.immortals.core.api.ll.phase2.result.status;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Result;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
@Result
@Description("The current state of the DAS")
public enum DasOutcome {
    PENDING("DAS execution is pending (non-terminal)"),
    RUNNING("DAS is executing analysis and augmentation (non-terminal)"),
    NOT_APPLICABLE("Baseline Submission - No DAS needed"),
    NOT_POSSIBLE("An invalid perturbation has been submitted"),
    SUCCESS("Augmentation Successful"),
    ERROR("An error has occured");

    public final String description;

    DasOutcome(String description) {
        this.description = description;
    }
}
