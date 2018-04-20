package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Directly using a "LinkedList<TestDetails>" object type as the body in REST bodies has issues.
 * This also adds a timestamp
 */
public class TestDetailsList extends HashSet<TestDetails> {

    private final long timestamp;

    public TestDetailsList() {
        super();
        this.timestamp = System.currentTimeMillis();
    }

    public TestDetailsList(Collection<TestDetails> testDetails) {
        super(testDetails);
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public TestDetailsList producePendingList() {
        TestDetailsList rval = new TestDetailsList();
        for (TestDetails td : this) {
            rval.add(td.produceUpdate(TestOutcome.PENDING));
        }
        return rval;
    }

    public TestDetailsList produceRunningList() {
        TestDetailsList rval = new TestDetailsList();
        for (TestDetails td : this) {
            rval.add(td.produceUpdate(TestOutcome.RUNNING));
        }
        return rval;
    }

    public static TestDetailsList fromTestCaseReportSet(@Nonnull String adaptationIdentifier, @Nonnull TestCaseReportSet testCaseReports) {
        TestDetailsList rval = new TestDetailsList();

        for (TestCaseReport tcr : testCaseReports) {
            TestDetails td = new TestDetails(tcr, adaptationIdentifier);
            rval.add(td);
        }
        return rval;
    }
}
