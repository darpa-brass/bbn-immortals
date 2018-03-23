package mil.darpa.immortals.core.das;

import mil.darpa.immortals.ImmortalsUtils;
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
import retrofit2.Call;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 12/20/17.
 */
public class Mock {
    
    public static TestOutcome desiredTestOutcome = TestOutcome.COMPLETE_PASS;
    
    public static AdaptationDetails dasSubmission(SubmissionModel submissionModel, String cpIdentifier) {

        final AdaptationDetails ad = new AdaptationDetails(
                "DummyAdapter",
                DasOutcome.RUNNING,
                submissionModel.sessionIdentifier
        );

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(300);
                AdaptationDetails ad2 = ad.produceUpdate(DasOutcome.SUCCESS, new LinkedList<>(), Arrays.asList("Did a little more stuff for " + cpIdentifier));
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
                submissionModel.sessionIdentifier
        ));

        currentTests.add(new TestDetails(
                cpIdentifier + "TestOne",
                TestOutcome.RUNNING,
                submissionModel.sessionIdentifier
        ));

        currentTests.add(new TestDetails(
                cpIdentifier + "TestTwo",
                TestOutcome.RUNNING,
                submissionModel.sessionIdentifier
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

        private final ImmortalsUtils.NetworkLogger mockNetworkLogger = new ImmortalsUtils.NetworkLogger("MOCKTH", "TA");


        public MockTestHarness() {
        }

        @Override
        public Call<Void> ready() {
            return new MockServices.MockRetrofitAction<Void>("/ready", mockNetworkLogger, null, null);
        }

        @Override
        public Call<Void> error(String value) {
            return new MockServices.MockRetrofitAction<Void>("/error", mockNetworkLogger, value, null);
        }

        @Override
        public Call<Void> status(TestAdapterState testAdapterState) {
            return new MockServices.MockRetrofitAction<Void>("/status", mockNetworkLogger, testAdapterState, null);
        }

        @Override
        public Call<Void> done(TestAdapterState testAdapterState) {
            return new MockServices.MockRetrofitAction<Void>("/done", mockNetworkLogger, testAdapterState, null);
        }
    }

    public static class MockDas implements DasSubmissionInterface {

        private final ImmortalsUtils.NetworkLogger mockLogger = new ImmortalsUtils.NetworkLogger("MOCKDAS", "TA");

        public MockDas() {
        }

        @Override
        public Call<String> submitAdaptationRequest(String rdf) {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(300);
                    
                    AdaptationDetails ad = new AdaptationDetails(
                            "DummyAdapter",
                            DasOutcome.PENDING,
                            "DummyAdaptationIdentifier");
                    TestAdapterSubmitter.updateAdaptationStatus(ad);
                    
                    Thread.sleep(300);

                    TestDetails td = new TestDetails(
                            "DummyTest",
                            TestOutcome.PENDING,
                            "DummyAdaptationIdentifier"
                    );
                    TestAdapterSubmitter.updateValidationStatus(td);
                    
                    Thread.sleep(300);
                    
                    ad = ad.produceUpdate(DasOutcome.RUNNING, null, null);
                    TestAdapterSubmitter.updateAdaptationStatus(ad);
                    Thread.sleep(300);
                    ad = ad.produceUpdate(DasOutcome.SUCCESS, null, null);
                    TestAdapterSubmitter.updateAdaptationStatus(ad);
                    
                    
                    td = td.produceUpdate(TestOutcome.RUNNING);
                    TestAdapterSubmitter.updateValidationStatus(td);
                    Thread.sleep(300);
                    td = td.produceUpdate(TestOutcome.COMPLETE_PASS);
                    TestAdapterSubmitter.updateValidationStatus(td);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();

            
            
            return new MockServices.MockRetrofitAction<String>("/bbn/das/submitAdaptationRequest", mockLogger, rdf, "DummyAdaptationRequest");
        }

        @Override
        public Call<String> submitValidationRequest(String rdf) {
            Thread t = new Thread(() -> {
                try {
                    TestDetails td = new TestDetails(
                            "DummyTest",
                            TestOutcome.PENDING,
                            "DummyValidationIdentifier"
                    );
                    TestAdapterSubmitter.updateValidationStatus(td);
                    Thread.sleep(300);
                    
                    td = td.produceUpdate(TestOutcome.RUNNING);
                    TestAdapterSubmitter.updateValidationStatus(td);
                    Thread.sleep(300);
                    
                    td = td.produceUpdate(desiredTestOutcome);
                    TestAdapterSubmitter.updateValidationStatus(td);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();

            return new MockServices.MockRetrofitAction<String>("/bbn/das/submitValidationRequest", mockLogger, rdf,"DummyValidationRequest");
        }
    }

}
