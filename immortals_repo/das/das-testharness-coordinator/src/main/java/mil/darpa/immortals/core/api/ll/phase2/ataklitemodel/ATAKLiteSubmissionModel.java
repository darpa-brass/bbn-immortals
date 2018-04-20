package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.RequirementsInterface;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModelInterface;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP3
@Description("The model of adaptation for all ATAKLite Clients")
public class ATAKLiteSubmissionModel implements SubmissionModelInterface {
    @P2CP3
    @Description("Requirements for all operating ATAKLite clients")
    public AtakliteRequirements requirements;

    public ATAKLiteSubmissionModel() {
    }

    public ATAKLiteSubmissionModel(AtakliteRequirements requirements) {
        this.requirements = requirements;
    }

    @Override
    public RequirementsInterface getRequirements() {
        return requirements;
    }
}
