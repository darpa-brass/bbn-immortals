package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Result;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Result
@Description("The overall status of the Test Adapter used for all done, status, and perturbation responses")
public class TestAdapterState {

    @Result
    @Description("Sequence number to preserve ordering regardless of network delay")
    public int sequence = -1;

    @Result
    @Description("The internal identifier used to bind this perturbation to any artifacts produced")
    public String identifier;

    @Result
    @Description("The state of the DAS adaptation")
    public AdaptationStateObject adaptation;

    @Result
    @Description("The state of the validation")
    public ValidationStateObject validation;

    public TestAdapterState() {
    }

    public TestAdapterState(@Nonnull String identifier) {
        this.sequence = sequence;
        this.identifier = identifier;
        this.adaptation = new AdaptationStateObject();
        this.validation = new ValidationStateObject();
    }
}
