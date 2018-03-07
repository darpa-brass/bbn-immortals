package mil.darpa.immortals.core.das.ll;

import mil.darpa.immortals.core.api.ll.phase2.EnableDas;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.*;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;
import mil.darpa.immortals.core.api.ll.phase2.result.status.VerdictOutcome;
import mil.darpa.immortals.core.das.AdaptedApplicationDeployer;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import mil.darpa.immortals.das.testcoordinators.P2CP1TestCoordinator;
import mil.darpa.immortals.das.testcoordinators.P2CP2TestCoordinator;
import mil.darpa.immortals.das.testcoordinators.P2CP3TestCoordinator;
import mil.darpa.immortals.testadapter.SubmissionServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 9/29/17.
 */
public class TestHarnessAdapterMediator {

    public enum ChallengeProblem {
        P2CP1,
        P2CP2,
        P2CP3
    }

    private static class TestHarnessAdapterStateContainer {

        final SubmissionModel submissionModel;
        final TestAdapterState testAdapterState;
        final ChallengeProblem challengeProblem;

        TestHarnessAdapterStateContainer(SubmissionModel submissionModel, TestAdapterState testAdapterState,
                                         ChallengeProblem challengeProblem) {
            this.submissionModel = submissionModel;
            this.testAdapterState = testAdapterState;
            this.challengeProblem = challengeProblem;
        }
    }


    private final TestHarnessSubmissionInterface submissionInterface;

    private TestHarnessAdapterStateContainer state;

    private final Logger logger = LoggerFactory.getLogger(TestHarnessAdapterMediator.class);

    private boolean dasDisabledByTestharness = false;

    private AdaptedApplicationDeployer applicationDeployer;

    public synchronized void signalReady() {
        try {
            submissionInterface.ready().execute();
        } catch (IOException e) {
            ImmortalsErrorHandler.reportFatalException(e);
        }
    }

    public synchronized Response updateAdaptationStatus(AdaptationDetails adaptationDetails) {
        innerUpdateDasStatus(adaptationDetails, true);
        // TODO: This should probably be more verbose...
        return Response.ok().build();
    }


    private synchronized void innerUpdateDasStatus(AdaptationDetails adaptationDetails, boolean sendUpdate) {
        state.testAdapterState.adaptation.details = adaptationDetails;
        DasOutcome previousOutcome = state.testAdapterState.adaptation.adaptationStatus;
        state.testAdapterState.adaptation.adaptationStatus = adaptationDetails.dasOutcome;

        switch (adaptationDetails.dasOutcome) {
            case PENDING:
                ImmortalsErrorHandler.reportFatalError("Initial state of the DAS should not be PENDING!");
                break;

            case RUNNING:
                if (previousOutcome != DasOutcome.PENDING && previousOutcome != DasOutcome.RUNNING) {
                    ImmortalsErrorHandler.reportFatalError("Unexpected DAS state transition from terminal state '" + previousOutcome.name() +
                            " to non-terminal state '" + adaptationDetails.dasOutcome.name() + "'!");
                }
                if (sendUpdate) {
                    try {
                        submissionInterface.status(state.testAdapterState).execute();
                    } catch (IOException e) {
                        ImmortalsErrorHandler.reportFatalException(e);
                    }
                }
                break;


            case ERROR:
            case NOT_POSSIBLE:
                if (previousOutcome != DasOutcome.PENDING && previousOutcome != DasOutcome.RUNNING) {
                    ImmortalsErrorHandler.reportFatalError("Unexpected DAS state transition from terminal state '" + previousOutcome.name() +
                            " to another terminal state '" + adaptationDetails.dasOutcome.name() + "'!");
                }
                state.testAdapterState.validation.verdictOutcome = VerdictOutcome.INCONCLUSIVE;
                if (sendUpdate) {
                    try {
                        submissionInterface.done(state.testAdapterState).execute();
                    } catch (IOException e) {
                        ImmortalsErrorHandler.reportFatalException(e);
                    }
                }

            case SUCCESS:
            case NOT_APPLICABLE:
                if (previousOutcome != DasOutcome.PENDING && previousOutcome != DasOutcome.RUNNING) {
                    ImmortalsErrorHandler.reportFatalError("Unexpected DAS state transition from terminal state '" + previousOutcome.name() +
                            " to another terminal state '" + adaptationDetails.dasOutcome.name() + "'!");
                }
                List<TestDetails> initialTestDetails = null;

                switch (state.challengeProblem) {

                    case P2CP1:
                        initialTestDetails = applicationDeployer.startP2CP1(state.submissionModel);
                        break;
                    case P2CP2:
                        initialTestDetails = applicationDeployer.startP2CP2(state.submissionModel);
                        break;
                    case P2CP3:
                        initialTestDetails = applicationDeployer.startP2CP3(state.submissionModel);
                        break;
                }
                innerUpdateValidatorStatus(initialTestDetails, false);


                state.testAdapterState.validation.verdictOutcome = VerdictOutcome.RUNNING;
                if (sendUpdate) {
                    try {
                        submissionInterface.status(state.testAdapterState).execute();
                    } catch (IOException e) {
                        ImmortalsErrorHandler.reportFatalException(e);
                    }
                }
                break;

            default:
                ImmortalsErrorHandler.reportFatalError("Unexpected DAS state '" + adaptationDetails.dasOutcome.name() + "'!");
                break;
        }

    }

    public synchronized Response updateDeploymentTestStatus(List<TestDetails> testDetails) {
        innerUpdateValidatorStatus(testDetails, true);
        // TODO: This should probably be more verbose...
        return Response.ok().build();
    }

    private void innerUpdateValidatorStatus(List<TestDetails> testDetails, boolean sendUpdate) {
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
                                TestOutcome.INVALID,
                                newDetails.currentState,
                                newDetails
                        )
                );
            } else {
                TestOutcome previousTO = currentDetails.actualStatus;
                TestOutcome currentTO = newDetails.currentState;

                if (previousTO != TestOutcome.PENDING && previousTO != TestOutcome.RUNNING) {
                    if (currentTO == TestOutcome.PENDING || currentTO == TestOutcome.RUNNING) {
                        ImmortalsErrorHandler.reportFatalError("Unexpected transition of test '" + currentDetails.testIdentifier
                                + "' state from terminal state '" + previousTO.name() +
                                " to non-terminal state '" + currentTO.name() + "'!");
                    } else {
                        ImmortalsErrorHandler.reportFatalError("Unexpected transition of test '" + currentDetails.testIdentifier
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
                    ImmortalsErrorHandler.reportFatalError("Initial state of the Validation should not be PENDING!");
                    break testIter;

                case NOT_APPLICABLE:
                    vo = VerdictOutcome.ERROR;
                    ImmortalsErrorHandler.reportFatalError("No validators should be NOT_APPLICABLE!");
                    break testIter;

                case RUNNING:
                    if (previousVO != VerdictOutcome.PENDING && previousVO != VerdictOutcome.RUNNING) {
                        if (vo == VerdictOutcome.PENDING || vo == VerdictOutcome.RUNNING) {
                            ImmortalsErrorHandler.reportFatalError("Unexpected verdict outcome transition from terminal state '"
                                    + previousVO.name() + " to non-terminal state '" + vo.name() + "'!");
                        } else {
                            ImmortalsErrorHandler.reportFatalError("Unexpected verdict outcome transition from terminal state '"
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
                    break;

                case COMPLETE_PASS:
                    vo = VerdictOutcome.PASS;
                    break;

                case COMPLETE_DEGRADED:
                    vo = VerdictOutcome.DEGRADED;
                    break;

                case COMPLETE_FAIL:
                    vo = VerdictOutcome.FAIL;
                    break;
            }
        }

        state.testAdapterState.validation.verdictOutcome = vo;
        // Switch verdict outcome
        switch (vo) {
            case PENDING:
                ImmortalsErrorHandler.reportFatalError("VerdictOutcome should never be PENDING!");
                break;

            case RUNNING:
                if (sendUpdate) {
                    try {
                        submissionInterface.status(state.testAdapterState).execute();
                    } catch (IOException e) {
                        ImmortalsErrorHandler.reportFatalException(e);
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
        if (dasDisabledByTestharness != enableDas.dasEnabled) {
            return Response.ok().build();
        } else if (state == null) {
            dasDisabledByTestharness = !enableDas.dasEnabled;
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    private final static TestHarnessAdapterMediator testHarnessMediator = new TestHarnessAdapterMediator();

    public static synchronized TestHarnessAdapterMediator getInstance() {
        return testHarnessMediator;
    }

    private TestHarnessAdapterMediator() {
        this.applicationDeployer = new AdaptedApplicationDeployer();
        submissionInterface = SubmissionServices.getTestHarnessSubmitter();
    }

    public Response submitSubmissionModel(SubmissionModel submissionModel, ChallengeProblem challengeProblem) {
        // If something is currently running, submit a 503
        if (state != null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        if (submissionModel.sessionIdentifier == null) {
            submissionModel.sessionIdentifier = "I" + System.currentTimeMillis();
        }

        state = new TestHarnessAdapterStateContainer(
                // TODO: Set baseline
                submissionModel,
                new TestAdapterState(
                        System.currentTimeMillis(),
                        submissionModel.sessionIdentifier,
                        new AdaptationStateObject(DasOutcome.PENDING, null),
                        new ValidationStateObject(VerdictOutcome.PENDING, new LinkedList<>())
                ),
                challengeProblem
        );

        if (!dasDisabledByTestharness) {
            AdaptationDetails initialDetails = null;

            switch (challengeProblem) {
                case P2CP1:
                    P2CP1TestCoordinator p2cp1testCoordinator = new P2CP1TestCoordinator();
                    initialDetails = p2cp1testCoordinator.execute(submissionModel);
                    break;

                case P2CP2:
                    P2CP2TestCoordinator p2cp2testCoordinator = new P2CP2TestCoordinator();
                    initialDetails = p2cp2testCoordinator.execute(submissionModel);
                    break;

                case P2CP3:
                    P2CP3TestCoordinator p2cp3TestCoordinator = new P2CP3TestCoordinator();
                    initialDetails = p2cp3TestCoordinator.execute(submissionModel);
                    break;
            }
            innerUpdateDasStatus(initialDetails, false);

            switch (state.testAdapterState.adaptation.details.dasOutcome) {
                case PENDING:
                    ImmortalsErrorHandler.reportFatalError("Initial state of the DAS should not be PENDING!");
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
            switch (challengeProblem) {

                case P2CP1:
                    innerUpdateValidatorStatus(applicationDeployer.startP2CP1(submissionModel), false);
                    break;
                case P2CP2:
                    innerUpdateValidatorStatus(applicationDeployer.startP2CP2(submissionModel), false);
                    break;
                case P2CP3:
                    innerUpdateValidatorStatus(applicationDeployer.startP2CP3(submissionModel), false);
                    break;
            }
        }
        return Response.ok(state.testAdapterState).build();
    }
}

