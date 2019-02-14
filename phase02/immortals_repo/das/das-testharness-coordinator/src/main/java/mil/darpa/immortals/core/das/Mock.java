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
                AdaptationDetails ad2 = ad.produceUpdate(DasOutcome.SUCCESS, new LinkedList<>(), Arrays.asList("Did a little more stuff for " + cpIdentifier), 0, 0);
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
                AdaptationDetails ad0 = new AdaptationDetails(
                        "DummyAdapter",
                        DasOutcome.PENDING,
                        "DummyAdaptationIdentifier");
                AdaptationDetailsList adl0 = new AdaptationDetailsList();
                adl0.add(ad0);
                adl0.sequence = 0;

                TestCaseReport tcr0 = new TestCaseReport(
                        "DummyTarget",
                        "DummyTest",
                        1.337,
                        null,
                        Arrays.asList("Functionality0", "Functionality1"));
                TestDetails td0 = new TestDetails(tcr0, "DummyAdaptationIdentifier");
                TestDetailsList tdl0 = new TestDetailsList();
                tdl0.add(td0);
                TestDetailsList tdl1 = tdl0.producePendingList();
                tdl1.sequence = 1;
                AdaptationDetails ad1 = ad0.produceUpdate(DasOutcome.RUNNING, null, null, 0, 0);
                AdaptationDetailsList adl1 = new AdaptationDetailsList();
                adl1.add(ad1);
                adl1.sequence = 2;

                AdaptationDetails ad2 = ad1.produceUpdate(DasOutcome.SUCCESS, null, null, 0, 0);
                AdaptationDetailsList adl2 = new AdaptationDetailsList();
                adl2.add(ad2);
                adl2.sequence = 3;

                TestDetailsList tdl2 = tdl0.producePendingList();
                tdl2.sequence = 4;

                TestDetailsList tdl3 = tdl0.produceRunningList();
                tdl3.sequence = 5;

                TestDetails td4 = td0.produceUpdate(desiredTestOutcome);
                TestDetailsList tdl4 = new TestDetailsList();
                tdl4.add(td4);
                tdl4.sequence = 6;

                TestAdapterSubmitter.updateValidationStatus(tdl3, false);
                TestAdapterSubmitter.updateAdaptationStatus(adl1, false);
                TestAdapterSubmitter.updateAdaptationStatus(adl2, false);
                TestAdapterSubmitter.updateValidationStatus(tdl2, false);
                TestAdapterSubmitter.updateValidationStatus(tdl1, false);
                TestAdapterSubmitter.updateValidationStatus(tdl4, false);
                TestAdapterSubmitter.updateValidationStatus(tdl4, false);
                TestAdapterSubmitter.updateAdaptationStatus(adl0, false);
            });
            t.start();


            return new MockServices.MockRetrofitAction<String>("/bbn/das/submitAdaptationRequest", mockLogger, rdf, "DummyAdaptationRequest");
        }

        @Override
        public Call<String> submitValidationRequest(String rdf) {
            Thread t = new Thread(() -> {
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

                TestDetailsList tdlX = TestDetailsList.fromTestCaseReportSet("DummyAdaptationIdentifier", tcrs);

                
                TestDetailsList tdl0 = tdlX.producePendingList();
                tdl0.sequence = 0;
                TestDetailsList tdl1 = tdlX.produceRunningList();
                tdl1.sequence = 1;
                
                TestDetailsList tdl2 = tdlX;
                tdl2.sequence = 2;


                TestAdapterSubmitter.updateValidationStatus(tdl0, false);
                TestAdapterSubmitter.updateValidationStatus(tdl1, false);
                TestAdapterSubmitter.updateValidationStatus(tdl2, false);
            });
            t.start();

            return new MockServices.MockRetrofitAction<String>("/bbn/das/submitValidationRequest", mockLogger, rdf, "DummyValidationRequest");
        }
    }

}
