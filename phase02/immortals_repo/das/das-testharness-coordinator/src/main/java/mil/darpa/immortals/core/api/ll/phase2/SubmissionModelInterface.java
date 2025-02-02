package mil.darpa.immortals.core.api.ll.phase2;

import java.util.List;

/**
 * Created by awellman@bbn.com on 4/6/18.
 */
public interface SubmissionModelInterface {

    String getIdentifier();

    RequirementsInterface getRequirements();

    ResourceInterface[] getResources();
}
