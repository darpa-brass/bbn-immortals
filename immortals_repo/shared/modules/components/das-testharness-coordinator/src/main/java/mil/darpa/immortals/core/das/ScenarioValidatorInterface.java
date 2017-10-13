package mil.darpa.immortals.core.das;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;
import mil.darpa.immortals.core.das.ll.TestHarnessAdapterMediator;
import mil.darpa.immortals.core.das.ll.TestHarnessValidationPerturbationInterface;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 9/29/17.
 */
public class ScenarioValidatorInterface implements TestHarnessValidationPerturbationInterface {

    private TestHarnessAdapterMediator thm;

    private List<TestDetails> currentTests;

    public ScenarioValidatorInterface() {
        this.thm = TestHarnessAdapterMediator.getInstance();
    }

    private List<TestDetails> dummyStartSubmission(SubmissionModel submissionModel, String cpIdentifier) {
        // TODO: Log error if running already
        currentTests = new LinkedList<>();
        currentTests.add(new TestDetails(
                cpIdentifier + "TestZero",
                TestOutcome.RUNNING,
                new LinkedList<>(),
                new LinkedList<>()
        ));

        currentTests.add(new TestDetails(
                cpIdentifier + "TestOne",
                TestOutcome.RUNNING,
                new LinkedList<>(),
                new LinkedList<>()
        ));

        currentTests.add(new TestDetails(
                cpIdentifier + "TestTwo",
                TestOutcome.RUNNING,
                new LinkedList<>(),
                new LinkedList<>()
        ));

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(300);
                for (TestDetails td : currentTests) {
                    td.currentState = TestOutcome.COMPLETE;
                }
                thm.updateValidatorStatus(currentTests);

            } catch (InterruptedException e) {
                thm.reportException(e);
            }

        });

        t.start();

        return currentTests;
    }

    @Override
    public synchronized List<TestDetails> startP2CP1(SubmissionModel submissionModel) {
        return dummyStartSubmission(submissionModel, "P2CP1");
    }


    @Override
    public synchronized List<TestDetails> startP2CP2(SubmissionModel submissionModel) {
        return dummyStartSubmission(submissionModel, "P2CP2");
    }

    @Override
    public synchronized List<TestDetails> startP2CP3(SubmissionModel submissionModel) {
        return dummyStartSubmission(submissionModel, "P2CP3");
    }
}
