package mil.darpa.immortals.core.das;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.*;
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
import java.util.HashSet;
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
                AdaptationDetailsList adl = new AdaptationDetailsList();
                adl.add(ad2);

                TestHarnessAdapterMediator.getInstance().updateAdaptationStatus(adl);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();

        return ad;
    }


    public static TestDetailsList appDeploymentSubmission(SubmissionModel submissionModel, String cpIdentifier) {
        TestHarnessAdapterMediator thm = TestHarnessAdapterMediator.getInstance();

        // TODO: Log error if running already
        TestDetailsList tdl = new TestDetailsList();
//        List<TestDetails> currentTests = new LinkedList<>();

        TestDetails td0 = new TestDetails(
                new TestCaseReport(
                        "DummyTarget",
                        "TestZero",
                        3.145,
                        null,
                        new HashSet<>(Arrays.asList("NoFunc"))
                ), submissionModel.sessionIdentifier);
        tdl.add(td0);

        TestDetails td1 = new TestDetails(
                new TestCaseReport(
                        "DummyTarget",
                        "TestOne",
                        3.145,
                        null,
                        new HashSet<>(Arrays.asList("NoFunc"))
                ), submissionModel.sessionIdentifier);
        tdl.add(td1);

        TestDetails td2 = new TestDetails(
                new TestCaseReport(
                        "DummyTarget",
                        "TestTwo",
                        3.145,
                        null,
                        new HashSet<>(Arrays.asList("NoFunc"))
                ), submissionModel.sessionIdentifier);
        tdl.add(td2);


        Thread t = new Thread(() -> {
            try {
                Thread.sleep(300);
                TestDetailsList updatedTests = new TestDetailsList();
                for (TestDetails td : tdl) {

                    TestDetails tdx = new TestDetails(new TestCaseReport(
                            "DummyTarget",
                            td.testIdentifier,
                            3.145,
                            null,
                            null
                    ), submissionModel.sessionIdentifier);
                    tdx.currentState = TestOutcome.COMPLETE_PASS;
                    updatedTests.add(tdx);
                }
                thm.updateDeploymentTestStatus(updatedTests);

            } catch (InterruptedException e) {
                ImmortalsErrorHandler.reportFatalException(e);
            }

        });

        t.start();

        return tdl;
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
                    AdaptationDetailsList adl = new AdaptationDetailsList();
                    adl.add(ad);
                    TestAdapterSubmitter.updateAdaptationStatus(adl);

                    Thread.sleep(300);

                    TestCaseReport tcr0 = new TestCaseReport(
                            "DummyTarget",
                            "DummyTest",
                            1.337,
                            null,
                            Arrays.asList("Functionality0", "Functionality1"));
                    TestDetails td0 = new TestDetails(tcr0, "DummyAdaptationIdentifier");
                    TestDetailsList td = new TestDetailsList();
                    td.add(td0);
                    
                    TestAdapterSubmitter.updateValidationStatus(td.producePendingList());

                    Thread.sleep(300);

                    ad = ad.produceUpdate(DasOutcome.RUNNING, null, null);
                    adl.clear();
                    adl.add(ad);
                    TestAdapterSubmitter.updateAdaptationStatus(adl);
                    Thread.sleep(300);
                    ad = ad.produceUpdate(DasOutcome.SUCCESS, null, null);
                    
                    adl.clear();
                    adl.add(ad);
                    TestAdapterSubmitter.updateAdaptationStatus(adl);

                    Thread.sleep(300);

                    TestAdapterSubmitter.updateValidationStatus(td.producePendingList());

                    TestAdapterSubmitter.updateValidationStatus(td.produceRunningList());
                    Thread.sleep(300);
                    td0 = td0.produceUpdate(desiredTestOutcome);
                    td.clear();
                    td.add(td0);
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

                    TestCaseReport tcr0;
                    if (desiredTestOutcome == TestOutcome.COMPLETE_PASS) {
                        tcr0 = new TestCaseReport(
                                "DummyTarget",
                                "DummyTest",
                                1.337,
                                null,
                                Arrays.asList("Functionality0", "Functionality1"));
                    } else {
                        tcr0 = new TestCaseReport(
                                "DummyTarget",
                                "DummyTest",
                                1.337,
                                "FAILED",
                                Arrays.asList("Functionality0", "Functionality1"));
                    }
                    TestCaseReportSet tcrs = new TestCaseReportSet();
                    tcrs.add(tcr0);
                    
                    TestDetailsList tdl = TestDetailsList.fromTestCaseReportSet("DummyAdaptationIdentifier", tcrs);
                    
                    Thread.sleep(200);
                    TestAdapterSubmitter.updateValidationStatus(tdl.producePendingList());
                    
                    Thread.sleep(300);

                    TestAdapterSubmitter.updateValidationStatus(tdl.produceRunningList());
                    Thread.sleep(300);

                    TestAdapterSubmitter.updateValidationStatus(tdl);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();

            return new MockServices.MockRetrofitAction<String>("/bbn/das/submitValidationRequest", mockLogger, rdf, "DummyValidationRequest");
        }
    }

}
