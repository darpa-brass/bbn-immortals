package mil.darpa.immortals.modulerunner.reporting;

import mil.darpa.immortals.modulerunner.configuration.AnalysisConfig;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 6/8/16.
 */
public class ExecutionData {

    public Long unprocessedFileSize;
    public Long processedFileSize;
    public Long fileSizeDifference;
    public Boolean filesAreEqual;
    private AnalysisConfig analysisConfig;
    private transient List<Long> executionDurations = new LinkedList<>();

    public ExecutionData(@Nonnull AnalysisConfig analysisConfig) {
        this.analysisConfig = analysisConfig;
    }

    public void addTimeDifference(long startTime, long endTime) {
        executionDurations.add(endTime - startTime);
    }

    public void setFileSizeDifference(long rawSize, long processedSize) {
        unprocessedFileSize = rawSize;
        processedFileSize = processedSize;
        fileSizeDifference = processedSize - rawSize;
    }

    public void setFilesAreEqual(boolean areEqual) {
        filesAreEqual = areEqual;
    }
}
