package mil.darpa.immortals.core.api.ll.phase2.martimodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP1;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP1
@P2CP3
@Description("The model of adaptation for the Marti server")
public class MartiSubmissionModel {

    @P2CP1
    @P2CP3
    @Description("Requirements for the Marti server")
    public MartiRequirements requirements;

    public MartiSubmissionModel() {
    }

    public MartiSubmissionModel(MartiRequirements requirements) {
        this.requirements = requirements;
    }

}
