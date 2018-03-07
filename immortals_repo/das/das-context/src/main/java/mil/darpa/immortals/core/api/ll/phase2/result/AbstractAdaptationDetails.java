package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Unstable
public abstract class AbstractAdaptationDetails {

    /**
     * Mandatory. Do not remove!
     */
    public DasOutcome dasOutcome;

    /**
     * Mandatory. Do not remove!
     */
    public String adaptationIdentifier;

    public AbstractAdaptationDetails() {
    }

    public AbstractAdaptationDetails(DasOutcome dasOutcome, String adaptationIdentifier) {
        this.dasOutcome = dasOutcome;
        this.adaptationIdentifier = adaptationIdentifier;
    }
}
