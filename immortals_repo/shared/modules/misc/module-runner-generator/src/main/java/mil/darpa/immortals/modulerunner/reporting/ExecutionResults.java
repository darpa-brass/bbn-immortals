package mil.darpa.immortals.modulerunner.reporting;

import mil.darpa.immortals.modulerunner.configuration.AnalysisModuleConfiguration;
import mil.darpa.immortals.modulerunner.configuration.ModuleCompositionConfiguration;
import mil.darpa.immortals.modulerunner.generators.ControlPointFormat;
import mil.darpa.immortals.modulerunner.generators.DeploymentPlatform;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 9/16/16.
 */
public class ExecutionResults extends ModuleCompositionConfiguration {

    public static class ExecutionData {

        public final Long unprocessedDataSize;
        public final String unprocessedFilepath;
        public final Long startTime;

        public Long processedDataSize = null;
        public String processedFilepath = null;
        public Long endTime = null;
//        public Boolean dataIsEqual;
//        private transient List<Long> executionDurations = new LinkedList<>();

//        public void startExecution() {
//            unprocessedDataSize = dataSize;
//            unprocessedFilepath = filepath;
//            startTime = time;
//
//        }

        public void endExecution(@Nonnull long processedDataSize, @Nonnull long endTime, @Nonnull String processedFilepath) {
            this.processedDataSize = processedDataSize;
            this.endTime = endTime;
            this.processedFilepath = processedFilepath;
        }

        public ExecutionData(@Nonnull long unprocessedDataSize, @Nonnull long startTime, @Nonnull String unprocessedFilepath) {
            this.unprocessedDataSize = unprocessedDataSize;
            this.startTime = startTime;
            this.unprocessedFilepath = unprocessedFilepath;
        }

//        public void addTimeDifference(long startTime, long endTime) {
//            executionDurations.add(endTime - startTime);
//        }
//
//        public void setFileSizeDifference(long rawSize, long processedSize) {
//            unprocessedDataSize = rawSize;
//            processedDataSize = processedSize;
//            dataSizeDifference = processedSize - rawSize;
//        }

//        public void setDataIsEqual(boolean areEqual) {
//            dataIsEqual = areEqual;
//        }
    }

    public final LinkedList<ExecutionData> executionData;
    public transient ExecutionData currentExecutionData;

    public ExecutionResults(@Nonnull ControlPointFormat controlPointFormat, @Nonnull DeploymentPlatform deploymentPlatform, @Nonnull String compositionIdentifier, @Nonnull ArrayList<AnalysisModuleConfiguration> compositionSequence) {
        super(controlPointFormat, deploymentPlatform, compositionIdentifier, compositionSequence);
        this.executionData = new LinkedList<>();
    }

    public synchronized void startExecution(@Nonnull long unprocessedDataSize, @Nonnull long startTime, @Nonnull String unprocessedFilepath) {
        if (currentExecutionData != null) {
            throw new RuntimeException("Cannot start an execution when the previous one has not been ended!");
        }
        currentExecutionData = new ExecutionData(unprocessedDataSize, startTime, unprocessedFilepath);
        executionData.add(currentExecutionData);

    }

    public synchronized void endExecution(@Nonnull long processedDataSize, @Nonnull long endTime, @Nonnull String processedFilepath) {
        if (currentExecutionData == null) {
            throw new RuntimeException("Cannot end an execution that has not yet been started!");
        }
        currentExecutionData.endExecution(processedDataSize, endTime, processedFilepath);
        currentExecutionData = null;

    }


}
