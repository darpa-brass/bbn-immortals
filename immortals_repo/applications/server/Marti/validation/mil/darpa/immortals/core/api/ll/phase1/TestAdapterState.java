package mil.darpa.immortals.core.api.ll.phase1;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
public class TestAdapterState {

    public final String identifier;
    public final AdaptationState adaptation;
    public final ValidationState validation;
    public final LinkedList<AnalyticsEvent> rawLogData;

    public TestAdapterState(String identifier, AdaptationState adaptation, ValidationState validation,
                            LinkedList<AnalyticsEvent> rawLogData) {
        this.identifier = identifier;
        this.adaptation = adaptation;
        this.validation = validation;
        this.rawLogData = rawLogData;
    }
}
