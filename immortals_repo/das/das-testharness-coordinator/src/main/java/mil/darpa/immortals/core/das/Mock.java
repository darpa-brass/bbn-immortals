package mil.darpa.immortals.core.das;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.TestAdapterState;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;
import mil.darpa.immortals.core.das.ll.TestHarnessAdapterMediator;
import mil.darpa.immortals.core.das.ll.TestHarnessSubmissionInterface;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import mil.darpa.immortals.das.context.MockServices;
import mil.darpa.immortals.das.context.TestAdapterSubmitter;
import mil.darpa.immortals.testadapter.restendpoints.DasSubmissionInterface;
import org.slf4j.Logger;
import retrofit2.Call;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 12/20/17.
 */
public class Mock {
    public static AdaptationDetails dasSubmission(SubmissionModel submissionModel, String cpIdentifier) {

        final AdaptationDetails ad = new AdaptationDetails(
                DasOutcome.RUNNING,
                submissionModel.sessionIdentifier,
                "Phase1DetailsObjectToBeUpdatedToPhase2"
        );

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(300);
                AdaptationDetails ad2 = ad.clone();
                ad2.details = "Did a little more stuff for " + cpIdentifier;
                ad2.dasOutcome = DasOutcome.SUCCESS;
                TestHarnessAdapterMediator.getInstance().updateAdaptationStatus(ad2);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();

        return ad;
    }


    public static List<TestDetails> appDeploymentSubmission(SubmissionModel submissionModel, String cpIdentifier) {
        TestHarnessAdapterMediator thm = TestHarnessAdapterMediator.getInstance();

        // TODO: Log error if running already
        List<TestDetails> currentTests = new LinkedList<>();
        currentTests.add(new TestDetails(
                cpIdentifier + "TestZero",
                TestOutcome.RUNNING,
                submissionModel.sessionIdentifier,
                null,
                null
        ));

        currentTests.add(new TestDetails(
                cpIdentifier + "TestOne",
                TestOutcome.RUNNING,
                submissionModel.sessionIdentifier,
                null,
                null
        ));

        currentTests.add(new TestDetails(
                cpIdentifier + "TestTwo",
                TestOutcome.RUNNING,
                submissionModel.sessionIdentifier,
                null,
                null
        ));

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(300);
                List<TestDetails> updatedTests = new LinkedList<>();
                for (TestDetails td : currentTests) {
                    TestDetails td2 = td.clone();
                    td2.currentState = TestOutcome.COMPLETE_PASS;
                    updatedTests.add(td2);
                }
                thm.updateDeploymentTestStatus(updatedTests);

            } catch (InterruptedException e) {
                ImmortalsErrorHandler.reportFatalException(e);
            }

        });

        t.start();

        return currentTests;
    }

    public static class MockTestHarness implements TestHarnessSubmissionInterface {

        private Logger logger;
        private Gson gson = new GsonBuilder().setPrettyPrinting().create();

        public MockTestHarness() {
            logger = MockServices.getLogger(MockTestHarness.class);
        }

        @Override
        public Call<Void> ready() {
            return new MockServices.MockRetrofitAction<Void>(logger, "Mock TH received POST to endpoint /ready", null);
        }

        @Override
        public Call<Void> error(String value) {
            return new MockServices.MockRetrofitAction<Void>(logger, "Mock TH received POST to endpoint /error with body:\n" + value, null);
        }

        @Override
        public Call<Void> status(TestAdapterState testAdapterState) {
            return new MockServices.MockRetrofitAction<Void>(logger, "Mock TH received POST to /status with body:\n" + gson.toJson(testAdapterState), null);
        }

        @Override
        public Call<Void> done(TestAdapterState testAdapterState) {
            return new MockServices.MockRetrofitAction<Void>(logger, "Mock TH received POST to /done with body:\n" + gson.toJson(testAdapterState), null);
        }
    }

    public static class MockDas implements DasSubmissionInterface {

        private Logger logger;
        private Gson gson = new GsonBuilder().setPrettyPrinting().create();

        public MockDas() {
            logger = MockServices.getLogger(MockDas.class);
        }

        @Override
        public Call<AdaptationDetails> submitAdaptationRequest(String rdf) {
            logger.info("Mock DAS received POST to /deployment-model with body:\n" + rdf);

            final AdaptationDetails ad = new AdaptationDetails(
                    DasOutcome.RUNNING,
                    rdf, // Not normally an adaptationIdentifier, but useful for mock adaptations until proper extraction is added
                    "Phase1DetailsObjectToBeUpdatedToPhase2"
            );

            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(300);
                    AdaptationDetails ad2 = ad.clone();
                    ad2.details = "Did a little more stuff for " + rdf;
                    ad2.dasOutcome = DasOutcome.SUCCESS;
                    TestAdapterSubmitter.updateAdaptationStatus(ad2);
//                    TestHarnessAdapterMediator.getInstance().updateAdaptationStatus(ad);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();

            return new MockServices.MockRetrofitAction<AdaptationDetails>(logger, "Mock DAS received POST to /deployment-model with body:\n" + rdf, ad);
        }
    }

}
