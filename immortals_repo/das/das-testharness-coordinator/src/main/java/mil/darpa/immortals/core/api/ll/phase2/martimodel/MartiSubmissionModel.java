package mil.darpa.immortals.core.api.ll.phase2.martimodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP1;
import mil.darpa.immortals.core.api.annotations.P2CP2;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.RequirementsInterface;
import mil.darpa.immortals.core.api.ll.phase2.ResourceInterface;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModelInterface;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP1
@P2CP2
@P2CP3
@Description("The model of adaptation for the Marti server")
public class MartiSubmissionModel implements SubmissionModelInterface {

    @P2CP1
    @P2CP3
    @Description("Requirements for the Marti server")
    public MartiRequirements requirements;

    @P2CP2
    @Description("Available Resources for the Marti server that may be empty or omitted")
    public JavaResource[] resources;

    public MartiSubmissionModel() {
    }

    public MartiSubmissionModel(MartiRequirements requirements, JavaResource[] resources) {
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
        return "Marti";
    }
}
