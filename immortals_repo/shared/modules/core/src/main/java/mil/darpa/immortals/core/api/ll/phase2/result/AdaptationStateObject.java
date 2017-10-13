package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Result;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Result
@Description("The state of the DAS adaptation")
public class AdaptationStateObject {

    @Result
    @Description("Indicates the current state of the DAS")
    public DasOutcome adaptationStatus;
    @Result
    @Description("A POJO object detailing the behavior of the DAS")
    @Unstable
    public AdaptationDetails details;

    public AdaptationStateObject() {
    }

    public AdaptationStateObject(DasOutcome adaptationStatus, AdaptationDetails details) {
        this.adaptationStatus = adaptationStatus;
        this.details = details;
    }
}
