package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Result;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;

import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Result
@Description("The state of the DAS adaptation")
public class AdaptationStateObject {

    @Result
    @Description("Indicates the current state of the DAS")
    public DasOutcome adaptationStatus = DasOutcome.PENDING;
    
    @Result
    @Description("A POJO object detailing the behavior of the DAS")
    @Unstable
    @Nullable
    public AdaptationDetails details;

    public AdaptationStateObject() {
    }
}
