package mil.darpa.immortals.core.das;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.das.ll.TestHarnessAdapterMediator;
import mil.darpa.immortals.core.das.ll.TestHarnessDasPerturbationInterface;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 9/28/17.
 */
public class DasInterface implements TestHarnessDasPerturbationInterface {

    private TestHarnessAdapterMediator thm;

    public DasInterface() {
        this.thm = TestHarnessAdapterMediator.getInstance();
        thm.setDasHandler(this);
        thm.setValidatorHandler(new ScenarioValidatorInterface());
    }

    public TestHarnessAdapterMediator getMediator() {
        return thm;
    }

    private AdaptationDetails dummyStartSubmission(SubmissionModel submissionModel, String cpIdentifier) {

        AdaptationDetails ad = new AdaptationDetails(
                DasOutcome.RUNNING,
                "Phase1DetailsObjectToBeUpdatedToPhase2",
                new LinkedList<>(),
                "Phase1DetailsObjectToBeUpdatedToPhase2",
                "Phase1DetailsObjectToBeUpdatedToPhase2",
                "Phase1DetailsObjectToBeUpdateadToPhase2",
                "Phase1DetailsObjectToBeUpdatedToPhase2"
        );


        Thread t = new Thread(() -> {
            try {
                Thread.sleep(300);
                ad.audits.add("Did a little more stuff for " + cpIdentifier);
                ad.dasOutcome = DasOutcome.SUCCESS;
                thm.updateDasStatus(ad);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();

        return ad;
    }

    @Override
    public synchronized AdaptationDetails startP2CP1(SubmissionModel submissionModel) {
        return dummyStartSubmission(submissionModel, "P2CP1");

    }

    @Override
    public AdaptationDetails startP2CP2(SubmissionModel submissionModel) {
        return dummyStartSubmission(submissionModel, "P2CP2");

    }

    @Override
    public AdaptationDetails startP2CP3(SubmissionModel submissionModel) {
        return dummyStartSubmission(submissionModel, "P2CP3");
    }

}
