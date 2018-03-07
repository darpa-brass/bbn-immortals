package mil.darpa.immortals.analytics.profilers;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 9/26/16.
 */
public class GenericConsumingPipelineProfiler {

    public final String identifier;

    public LinkedList<GenericConsumingPipeProfiler> analysisProfiles = new LinkedList<>();

    public synchronized <InputType> ConsumingPipe<InputType> insertPipe(int pipelineIndex, ConsumingPipe<InputType> next) {
        GenericConsumingPipeProfiler profiler = new GenericConsumingPipeProfiler(pipelineIndex, identifier, next);
        analysisProfiles.add(profiler);
        return profiler;
    }

    public GenericConsumingPipelineProfiler(String identifier) {
        this.identifier = identifier;
    }
}