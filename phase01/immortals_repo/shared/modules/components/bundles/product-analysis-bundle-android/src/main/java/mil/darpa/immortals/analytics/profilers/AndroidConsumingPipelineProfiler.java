package mil.darpa.immortals.analytics.profilers;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * Created by awellman@bbn.com on 10/20/16.
 */
public class AndroidConsumingPipelineProfiler extends GenericConsumingPipelineProfiler {
    @Override
    public synchronized <InputType> ConsumingPipe<InputType> insertPipe(int pipelineIndex, ConsumingPipe<InputType> next) {
        GenericConsumingPipeProfiler profiler = new AndroidConsumingPipeProfiler(pipelineIndex, identifier, next);
        analysisProfiles.add(profiler);
        return profiler;
    }

    public AndroidConsumingPipelineProfiler(String identifier) {
        super(identifier);
    }
}
