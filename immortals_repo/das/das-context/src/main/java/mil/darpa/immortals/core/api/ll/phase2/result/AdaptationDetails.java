package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Unstable
public class AdaptationDetails extends AbstractAdaptationDetails {

    public String details;

    public AdaptationDetails() {
        super();
    }

    public AdaptationDetails(DasOutcome dasOutcome, String adaptationIdentifier, String details) {
        super(dasOutcome, adaptationIdentifier);
        this.details = details;
    }

    /**
     * Convenience method to encourage treating these atomically to avoid modification of objects in-transit
     *
     * @return Cloned {@link AdaptationDetails object}
     */
    public AdaptationDetails clone() {
        return new AdaptationDetails(dasOutcome, adaptationIdentifier, details);
    }
}
