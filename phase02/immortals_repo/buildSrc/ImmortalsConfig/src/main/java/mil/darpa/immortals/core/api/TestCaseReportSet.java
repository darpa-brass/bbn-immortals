package mil.darpa.immortals.core.api;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by awellman@bbn.com on 4/12/18.
 */
public class TestCaseReportSet extends CopyOnWriteArraySet<TestCaseReport> {

    private String padString(int length, @Nonnull String s) {
        return String.format("%1$-" + length + "s", s);
    }

    public String toChart(@Nonnull List<String>... columns) {
        StringBuilder sb = new StringBuilder();

        int[] columnWidths = new int[columns.length];

        for (int row = 0; row < columns[0].size(); row++) {
            for (int column = 0; column < columns.length; column++) {
                columnWidths[column] = Math.max(columnWidths[column], columns[column].get(row).length());
            }
        }

        for (int row = 0; row < columns[0].size(); row++) {
            for (int column = 0; column < columns.length; column++) {
                sb.append(" | ").append(padString(columnWidths[column], columns[column].get(row)));
            }
            sb.append(" |\n");
        }

        return sb.toString();
    }

    public TestCaseReportSet() {
        super();
    }

    public TestCaseReportSet(Collection<TestCaseReport> collection) {
        super(collection);
    }

    public TestCaseReportSet getFailures() {
        TestCaseReportSet failures = new TestCaseReportSet();

        for (TestCaseReport report : this) {
            if (report.getFailureMessage() != null) {
                failures.add(report);
            }
        }
        return failures;
    }

    public Set<TestCaseReport> getSuccesses() {
        TestCaseReportSet successes = new TestCaseReportSet();

        for (TestCaseReport report : this) {
            if (report.getFailureMessage() == null) {
                successes.add(report);
            }
        }
        return successes;
    }

    public String getResultChart() {
        List<String> targetApps = new LinkedList<>();
        List<String> testIdentifiers = new LinkedList<>();
        List<String> results = new LinkedList<>();
        List<String> functionality = new LinkedList<>();

        HashSet<TestCaseReport> threadSafetyFirst = new HashSet<>(this);
        for (TestCaseReport tcr : threadSafetyFirst) {
            targetApps.add(tcr.getTestCaseTarget());
            testIdentifiers.add(tcr.getTestCaseIdentifier());
            results.add(tcr.getFailureMessage() == null ? "PASS" : "FAIL");

            Set<String> vf = tcr.getValidatedFunctionality();

            if (vf == null || vf.size() == 0) {
                functionality.add("N/A");
            } else {
                List<String> res = new LinkedList<>();

                for (String f : vf) {
                    res.add(f.substring(f.lastIndexOf("#") + 1, f.length()));
                }
                functionality.add(String.join(",", res));
            }
        }
        return toChart(targetApps, testIdentifiers, results, functionality);
    }
}
