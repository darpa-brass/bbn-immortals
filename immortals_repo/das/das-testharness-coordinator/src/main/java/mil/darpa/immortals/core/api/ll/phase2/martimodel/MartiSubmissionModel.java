package mil.darpa.immortals.core.api.ll.phase2.martimodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP1;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.RequirementsInterface;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModelInterface;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP1
@P2CP3
@Description("The model of adaptation for the Marti server")
public class MartiSubmissionModel implements SubmissionModelInterface {

    @P2CP1
    @P2CP3
    @Description("Requirements for the Marti server")
    public MartiRequirements requirements;

    public MartiSubmissionModel() {
    }

    public MartiSubmissionModel(MartiRequirements requirements) {
        this.requirements = requirements;
    }

    @Override
    public RequirementsInterface getRequirements() {
        return requirements;
    }
    
    @Override
    public String getIdentifier() {
        return "Marti";
    }
}
