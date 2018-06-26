package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP2;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.RequirementsInterface;
import mil.darpa.immortals.core.api.ll.phase2.ResourceInterface;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModelInterface;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP3
@P2CP2
@Description("The model of adaptation for all ATAKLite Clients")
public class ATAKLiteSubmissionModel implements SubmissionModelInterface {
    @P2CP3
    @Description("Requirements for all operating ATAKLite clients")
    public AtakliteRequirements requirements;

    @P2CP2
    @Description("Available Resources for all operating ATAKLite clients that may be empty or omitted")
    public AndroidResource[] resources;

    public ATAKLiteSubmissionModel() {
    }

    public ATAKLiteSubmissionModel(AtakliteRequirements requirements, AndroidResource[] resources) {
        this.requirements = requirements;
        this.resources = resources;
    }

    @Override
    public RequirementsInterface getRequirements() {
        return requirements;
    }

    @Override
    public ResourceInterface[] getResources() {
        return resources;
    }

    @Override
    public String getIdentifier() {
        return "ATAKLite";
    }
}
