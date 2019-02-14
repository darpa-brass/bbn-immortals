package mil.darpa.immortals.analytics.profilers;

import mil.darpa.immortals.analytics.AnalysisData;
import mil.darpa.immortals.analytics.AnalysisSnapshot;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 10/18/16.
 */
class GenericConsumingPipeProfiler<InputType> implements ConsumingPipe<InputType> {

    final transient ConsumingPipe<InputType> next;
    private final String sessionIdentifier;
    private final int pipelineIndex;
    public final LinkedList<AnalysisSnapshot[]> analysisSnapshots;

    public GenericConsumingPipeProfiler(int pipelineIndex, String sessionIdentifier, ConsumingPipe<InputType> next) {
        this.pipelineIndex = pipelineIndex;
        this.sessionIdentifier = sessionIdentifier;
        this.next = next;
        this.analysisSnapshots = new LinkedList<>();
    }

    @Override
    public final void consume(InputType input) {
        List<AnalysisSnapshot> rawData = Arrays.asList(analyze(input));
        AnalysisData data = new AnalysisData(
                "?",
                this.pipelineIndex,
                rawData
        );

        Analytics.log(
                Analytics.newEvent(
                        AnalyticsEventType.Analysis_EventOccurred,
                        sessionIdentifier,
                        data
                )
        );


        next.consume(input);
    }

    @Override
    public final void flushPipe() {
        next.flushPipe();
    }

    @Override
    public final void closePipe() {
        next.closePipe();
    }

    AnalysisSnapshot[] analyze(InputType subject) {
        return new AnalysisSnapshot[0];
    }
}
