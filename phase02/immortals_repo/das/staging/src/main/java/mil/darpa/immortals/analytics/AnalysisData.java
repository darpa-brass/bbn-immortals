package mil.darpa.immortals.analytics;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 11/1/16.
 */
public class AnalysisData {

    public final String sessionIdentifier;

    public final int pipelinePosition;

    public final LinkedList<AnalysisSnapshot> results;

    public AnalysisData(@Nonnull String sessionIdentifier, int pipelinePosition, @Nonnull List<AnalysisSnapshot> results) {
        this.sessionIdentifier = sessionIdentifier;
        this.pipelinePosition = pipelinePosition;
        this.results = new LinkedList<>(results);
    }
}
