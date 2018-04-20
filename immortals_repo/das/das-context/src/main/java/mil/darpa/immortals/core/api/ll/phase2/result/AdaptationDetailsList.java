package mil.darpa.immortals.core.api.ll.phase2.result;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Directly using a "LinkedList<TestDetails>" object type as the body in REST bodies has issues.
 * This also adds a timestamp
 */
public class AdaptationDetailsList extends HashSet<AdaptationDetails> {

    private final long timestamp;

    public AdaptationDetailsList() {
        super();
        this.timestamp = System.currentTimeMillis();
    }

    public AdaptationDetailsList(Collection<AdaptationDetails> adaptationDetails) {
        super(adaptationDetails);
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
