package mil.darpa.immortals.modulerunner.reporting;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 6/8/16.
 */
public class ExecutionData {

    public Long unprocessedDataSize;
    public Long processedDataSize;
    public Long dataSizeDifference;
    public Boolean dataIsEqual;
    private transient List<Long> executionDurations = new LinkedList<>();

    public ExecutionData() {
    }

    public void addTimeDifference(long startTime, long endTime) {
        executionDurations.add(endTime - startTime);
    }

    public void setFileSizeDifference(long rawSize, long processedSize) {
        unprocessedDataSize = rawSize;
        processedDataSize = processedSize;
        dataSizeDifference = processedSize - rawSize;
    }

    public void setDataIsEqual(boolean areEqual) {
        dataIsEqual = areEqual;
    }
}
