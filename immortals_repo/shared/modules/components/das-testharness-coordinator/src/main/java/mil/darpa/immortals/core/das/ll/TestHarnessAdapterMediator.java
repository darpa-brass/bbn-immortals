package mil.darpa.immortals.core.das.ll;

import com.google.gson.Gson;
import mil.darpa.immortals.core.Configuration;
import mil.darpa.immortals.core.api.ll.phase2.EnableDas;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.*;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;
import mil.darpa.immortals.core.api.ll.phase2.result.status.VerdictOutcome;
import mil.darpa.immortals.core.das.CoordinatorMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 9/29/17.
 */
public class TestHarnessAdapterMediator {

    enum ChallengeProblem {
        P2CP1,
        P2CP2,
        P2CP3
    }

    private static class TestHarnessAdapterStateContainer {

        public final SubmissionModel submissionModel;
        public final TestAdapterState testAdapterState;
        public final ChallengeProblem challengeProblem;

        public TestHarnessAdapterStateContainer(SubmissionModel submissionModel, TestAdapterState testAdapterState,
                                                ChallengeProblem challengeProblem) {
            this.submissionModel = submissionModel;
            this.testAdapterState = testAdapterState;
            this.challengeProblem = challengeProblem;
        }
    }


    private final TestHarnessSubmissionInterface submissionInterface;

    private TestHarnessAdapterStateContainer state;

    public boolean dasReady = false;

    //    private SubmissionModel currentSubmissionModel;
//    private TestAdapterState currentState;
    private final Gson gson = new Gson();

    private final Logger logger = LoggerFactory.getLogger(TestHarnessAdapterMediator.class);

    private boolean dasEnabled = true;
    private boolean validationEnabled = true;

    private TestHarnessDasPerturbationInterface dasHandler;

    private TestHarnessValidationPerturbationInterface scenarioConductorHandler;

    public synchronized void signalReady() {
        try {
            submissionInterface.ready().execute();
        } catch (IOException e) {
            reportException(e);
        }
    }

    public synchronized void updateDasStatus(AdaptationDetails adaptationDetails) {
        innerUpdateDasStatus(adaptationDetails, true);
    }


    private synchronized void innerUpdateDasStatus(AdaptationDetails adaptationDetails, boolean sendUpdate) {
        state.testAdapterState.adaptation.details = adaptationDetails;
        DasOutcome previousOutcome = state.testAdapterState.adaptation.adaptationStatus;
        state.testAdapterState.adaptation.adaptationStatus = adaptationDetails.dasOutcome;

        switch (adaptationDetails.dasOutcome) {
            case PENDING:
                reportError("Initial state of the DAS should not be PENDING!");
                break;

            case RUNNING:
                if (previousOutcome != DasOutcome.PENDING && previousOutcome != DasOutcome.RUNNING) {
                    reportError("Unexpected DAS state transition from terminal state '" + previousOutcome.name() +
                            " to non-terminal state '" + adaptationDetails.dasOutcome.name() + "'!");
                }
                if (sendUpdate) {
                    try {
                        submissionInterface.status(state.testAdapterState).execute();
                    } catch (IOException e) {
                        reportException(e);
                    }
                }
                break;


            case ERROR:
            case NOT_POSSIBLE:
                if (previousOutcome != DasOutcome.PENDING && previousOutcome != DasOutcome.RUNNING) {
                    reportError("Unexpected DAS state transition from terminal state '" + previousOutcome.name() +
                            " to another terminal state '" + adaptationDetails.dasOutcome.name() + "'!");
                }
                state.testAdapterState.validation.verdictOutcome = VerdictOutcome.INCONCLUSIVE;
                if (sendUpdate) {
                    try {
                        submissionInterface.done(state.testAdapterState).execute();
                    } catch (IOException e) {
                        reportException(e);
                    }
                }

            case SUCCESS:
            case NOT_APPLICABLE:
                if (previousOutcome != DasOutcome.PENDING && previousOutcome != DasOutcome.RUNNING) {
                    reportError("Unexpected DAS state transition from terminal state '" + previousOutcome.name() +
                            " to another terminal state '" + adaptationDetails.dasOutcome.name() + "'!");
                }
                List<TestDetails> initialTestDetails = null;
               
                switch (state.challengeProblem) {

                    case P2CP1:
                        initialTestDetails = scenarioConductorHandler.startP2CP1(state.submissionModel);
                        break;
                    case P2CP2:
                        initialTestDetails = scenarioConductorHandler.startP2CP2(state.submissionModel);
                        break;
                    case P2CP3:
                        initialTestDetails = scenarioConductorHandler.startP2CP3(state.submissionModel);
                        break;
                }
                innerUpdateValidatorStatus(initialTestDetails, false);


                state.testAdapterState.validation.verdictOutcome = VerdictOutcome.RUNNING;
                if (sendUpdate) {
                    try {
                        submissionInterface.status(state.testAdapterState).execute();
                    } catch (IOException e) {
                        reportException(e);
                    }
                }
                break;

            default:
                reportError("Unexpected DAS state '" + adaptationDetails.dasOutcome.name() + "'!");
                break;
        }

    }

    public synchronized void updateValidatorStatus(List<TestDetails> testDetails) {
        innerUpdateValidatorStatus(testDetails, true);

    }

    public void innerUpdateValidatorStatus(List<TestDetails> testDetails, boolean sendUpdate) {
        for (TestDetails newDetails : testDetails) {
            TestStateObject currentDetails = null;
            for (TestStateObject candidateDetails : state.testAdapterState.validation.executedTests) {
                if (candidateDetails.testIdentifier.equals(newDetails.testIdentifier)) {
                    currentDetails = candidateDetails;
                    break;
                }
            }

            if (currentDetails == null) {
                state.testAdapterState.validation.executedTests.add(
                        new TestStateObject(
                                newDetails.testIdentifier,
                                "DummyIntent",
                                TestOutcome.COMPLETE,
                                newDetails.currentState,
                                newDetails
                        )
                );
            } else {
                TestOutcome previousTO = currentDetails.actualStatus;
                TestOutcome currentTO = newDetails.currentState;

                if (previousTO != TestOutcome.PENDING && previousTO != TestOutcome.RUNNING) {
                    if (currentTO == TestOutcome.PENDING || currentTO == TestOutcome.RUNNING) {
                        reportError("Unexpected transition of test '" + currentDetails.testIdentifier
                                + "' state from terminal state '" + previousTO.name() +
                                " to non-terminal state '" + currentTO.name() + "'!");
                    } else {
                        reportError("Unexpected transition of test '" + currentDetails.testIdentifier
                                + "' state from terminal state '" + previousTO.name() +
                                " to another terminal state '" + currentTO.name() + "'!");
                    }
                }

                currentDetails.actualStatus = newDetails.currentState;
                currentDetails.details = newDetails;
            }
        }

        VerdictOutcome previousVO = state.testAdapterState.validation.verdictOutcome;
        VerdictOutcome vo = VerdictOutcome.RUNNING;
//        boolean terminalState = true;
        testIter:
        for (TestStateObject tso : state.testAdapterState.validation.executedTests) {
            // TODO: Sort out which means which in terms of the final representative state with mixed test outcomes
            switch (tso.actualStatus) {
                case PENDING:
                    vo = VerdictOutcome.ERROR;
                    reportError("Initial state of the Validation should not be PENDING!");
                    break testIter;

                case NOT_APPLICABLE:
                    vo = VerdictOutcome.ERROR;
                    reportError("No validators should be NOT_APPLICABLE!");
                    break testIter;

                case RUNNING:
                    if (previousVO != VerdictOutcome.PENDING && previousVO != VerdictOutcome.RUNNING) {
                        if (vo == VerdictOutcome.PENDING || vo == VerdictOutcome.RUNNING) {
                            reportError("Unexpected verdict outcome transition from terminal state '"
                                    + previousVO.name() + " to non-terminal state '" + vo.name() + "'!");
                        } else {
                            reportError("Unexpected verdict outcome transition from terminal state '"
                                    + previousVO.name() + " to terminal state '" + vo.name() + "'!");
                        }
                    }

                    vo = VerdictOutcome.RUNNING;
//                    terminalState = false;
                    break testIter;

                case ERROR:
                    vo = VerdictOutcome.ERROR;
                    break testIter;

                case INVALID:
                    vo = VerdictOutcome.INCONCLUSIVE;
                    break testIter;

                case INCOMPLETE:
                case COMPLETE:
                    vo = VerdictOutcome.PASS;
                    break;
            }
        }

        state.testAdapterState.validation.verdictOutcome = vo;
        // Switch verdict outcome
        switch (vo) {
            case PENDING:
                reportError("VerdictOutcome should never be PENDING!");
                break;

            case RUNNING:
                if (sendUpdate) {
                    try {
                        submissionInterface.status(state.testAdapterState).execute();
                    } catch (IOException e) {
                        reportException(e);
                    }
                }
                break;

            case PASS:
            case DEGRADED:
            case FAIL:
            case INCONCLUSIVE:
            case INAPPLICABLE:
            case ERROR:
                if (sendUpdate) {
                    try {
                        submissionInterface.done(state.testAdapterState).execute();
                        state = null;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
        }
    }

    public void setDasHandler(TestHarnessDasPerturbationInterface dasHandler) {
        if (this.dasHandler != null) {
            reportError("Overriding an already set das handler!");
        }
        this.dasHandler = dasHandler;
    }

    public void setValidatorHandler(TestHarnessValidationPerturbationInterface validatorHandler) {
        if (this.scenarioConductorHandler != null) {
            reportError("Overriding an already set validator handler!");
        }
        this.scenarioConductorHandler = validatorHandler;
    }

//    public synchronized Response cp1(SubmissionModel submissionModel) {
//        if (!readyForSubmission()) {
//            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
//        }
//
////        SubmissionModel sm = gson.fromJson(submissionModel, SubmissionModel.class);
//        TestAdapterState tas = dasHandler.thPostP2CP1(submissionModel);
//        return Response.ok(tas).build();
//    }
//
//    public synchronized Response cp2(SubmissionModel submissionModel) {
//        // TODO: Validation
//        if (!readyForSubmission()) {
//            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
//        }
//
////        SubmissionModel sm = gson.fromJson(submissionModel, SubmissionModel.class);
//        TestAdapterState tas = dasHandler.thPostP2CP2(submissionModel);
//        return Response.ok(tas).build();
//    }
//
//    public synchronized Response cp3(SubmissionModel submissionModel) {
//        // TODO: Validation
//        if (!readyForSubmission()) {
//            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
//        }
////        SubmissionModel sm = gson.fromJson(submissionModel, SubmissionModel.class);
//        TestAdapterState tas = dasHandler.thPostP2CP3(submissionModel);
//        return Response.ok(tas).build();
//    }

    public synchronized Response thGetAlive() {
        // TODO: Check DAS and Pymmortals
        return Response.ok().build();
    }

    public synchronized Response thGetQuery() {
        if (state.testAdapterState == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(state.testAdapterState).build();
        }
    }

    public synchronized Response thPostEnabled(EnableDas enableDas) {
        if (dasEnabled == enableDas.dasEnabled) {
            return Response.ok().build();
        } else if (state == null) {
            dasEnabled = enableDas.dasEnabled;
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    private final static TestHarnessAdapterMediator testHarnessMediator = new TestHarnessAdapterMediator();

    public static synchronized TestHarnessAdapterMediator getInstance() {
        return testHarnessMediator;
    }

    public void reportException(Throwable t) {
        try {
            submissionInterface.error(t.getMessage()).execute();
        } catch (Exception e) {
            //ignore
        }
        throw new RuntimeException(t);
    }

    public void reportError(String error) {
        try {
            submissionInterface.error(error);
        } catch (Exception e) {
            //ignore
        }

        throw new RuntimeException(error);
    }

//    private boolean readyForSubmission() {
//        if (currentState == null) {
//            return true;
//        }
//
//        if (currentState.adaptation.adaptationStatus == DasOutcome.PENDING ||
//                currentState.adaptation.adaptationStatus == DasOutcome.RUNNING) {
//            return false;
//        }
//
//        if (currentState.validation.verdictOutcome == VerdictOutcome.PENDING ||
//                currentState.validation.verdictOutcome == VerdictOutcome.RUNNING) {
//            return false;
//        }
//        return true;
//    }

    private TestHarnessAdapterMediator() {
        // Start the rest client 
        Configuration.TestHarnessConfiguration thc = Configuration.getInstance().testHarness;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(thc.protocol + thc.url + ":" + thc.port)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        submissionInterface = retrofit.create(TestHarnessSubmissionInterface.class);
        Thread.setDefaultUncaughtExceptionHandler(CoordinatorMain.exceptionHandler);

//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread t, Throwable e) {
//                e.pri
//                reportException(e);
//            }
//        });
    }

    public synchronized Response thPostP2CP1(SubmissionModel submissionModel) {
        return challengeProblemRunner(submissionModel, ChallengeProblem.P2CP1);
    }

    public Response challengeProblemRunner(SubmissionModel submissionModel, ChallengeProblem challengeProblem) {
        // If something is currently running, submit a 503
        if (state != null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        String sessionIdentifier = "S" + String.valueOf(System.currentTimeMillis());

        state = new TestHarnessAdapterStateContainer(
                // TODO: Set baseline
                submissionModel,
                new TestAdapterState(
                        System.currentTimeMillis(),
                        sessionIdentifier,
                        new AdaptationStateObject(DasOutcome.PENDING, null),
                        new ValidationStateObject(VerdictOutcome.PENDING, new LinkedList<>())
                ),
                challengeProblem
        );

        if (dasEnabled) {
            AdaptationDetails initialDetails = null;
            switch (challengeProblem) {

                case P2CP1:
                    initialDetails = dasHandler.startP2CP1(submissionModel);
                    break;
                case P2CP2:
                    initialDetails = dasHandler.startP2CP2(submissionModel);
                    break;
                case P2CP3:
                    initialDetails = dasHandler.startP2CP3(submissionModel);
                    break;
            }

            innerUpdateDasStatus(initialDetails, false);

            switch (state.testAdapterState.adaptation.details.dasOutcome) {
                case PENDING:
                    reportError("Initial state of the DAS should not be PENDING!");
                    break;

                case RUNNING:
                    return Response.ok(state.testAdapterState).build();

                case NOT_APPLICABLE:
                case NOT_POSSIBLE:
                case ERROR:
                    return Response.ok(state.testAdapterState).build();

                case SUCCESS:
                    state.testAdapterState.validation = new ValidationStateObject(VerdictOutcome.RUNNING, new LinkedList<>());
                    break;
            }


        } else {
            state.testAdapterState.adaptation.adaptationStatus = DasOutcome.NOT_APPLICABLE;
            List<TestDetails> details = null;
            switch (challengeProblem) {

                case P2CP1:
                    details = scenarioConductorHandler.startP2CP1(submissionModel);
                    break;
                case P2CP2:
                    details = scenarioConductorHandler.startP2CP2(submissionModel);
                    break;
                case P2CP3:
                    details = scenarioConductorHandler.startP2CP3(submissionModel);
                    break;
            }
            innerUpdateValidatorStatus(details, false);
        }

        return Response.ok(state.testAdapterState).build();
    }

    public synchronized Response thPostP2CP2(SubmissionModel submissionModel) {
        return challengeProblemRunner(submissionModel, ChallengeProblem.P2CP2);
    }

    public synchronized Response thPostP2CP3(SubmissionModel submissionModel) {
        return challengeProblemRunner(submissionModel, ChallengeProblem.P2CP3);
    }

}

