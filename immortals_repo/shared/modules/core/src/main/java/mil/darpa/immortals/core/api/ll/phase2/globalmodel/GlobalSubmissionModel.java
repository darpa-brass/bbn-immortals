package mil.darpa.immortals.core.api.ll.phase2.globalmodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP2;

/**
 * Created by awellman@bbn.com on 9/18/17.
 */
@P2CP2
@Description("The model of application that is applicable to the entire System Under Test (SUT)")
public class GlobalSubmissionModel {
    @P2CP2
    @Description("Requirements for the entire SUT")
    public GlobalRequirements requirements;

    public GlobalSubmissionModel() {
    }

    public GlobalSubmissionModel(GlobalRequirements requirements) {
        this.requirements = requirements;
    }
}
