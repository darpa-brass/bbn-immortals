package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Result;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Result
@Description("The overall status of the Test Adapter used for all done, status, and perturbation responses")
public class TestAdapterState {

    @Result
    @Description("Last Updated time (equivalent to Java 'System.currentTimeMillis()')")
    public long timestamp;
    
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

    public TestAdapterState(long timestamp, String identifier, AdaptationStateObject adaptation, ValidationStateObject validation) {
        this.timestamp = timestamp;
        this.identifier = identifier;
        this.adaptation = adaptation;
        this.validation = validation;
    }
}
