package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP3
@Description("The model of adaptation for all ATAKLite Clients")
public class ATAKLiteSubmissionModel {
    @P2CP3
    @Description("Requirements for all operating ATAKLite clients")
    public AtakliteRequirements requirements;

    public ATAKLiteSubmissionModel() {
    }

    public ATAKLiteSubmissionModel(AtakliteRequirements requirements) {
        this.requirements = requirements;
    }
}
