package mil.darpa.immortals.core.das.ll;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.EnableDas;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.*;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;
import mil.darpa.immortals.core.api.ll.phase2.result.status.VerdictOutcome;
import mil.darpa.immortals.core.das.AdaptedApplicationDeployer;
import mil.darpa.immortals.core.das.Mock;
import mil.darpa.immortals.das.TestCoordinatorExecutionInterface;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import mil.darpa.immortals.das.testcoordinators.DummyTestCoordinator;
import mil.darpa.immortals.das.testcoordinators.P2CP1TestCoordinator;
import mil.darpa.immortals.das.testcoordinators.P2CP2TestCoordinator;
import mil.darpa.immortals.das.testcoordinators.P2CP3TestCoordinator;
import mil.darpa.immortals.testadapter.SubmissionServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 9/29/17.
 */
public class TestHarnessAdapterMediator {


    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public enum ChallengeProblem {
        P2CP1,
        P2CP2,
        P2CP3
    }


    private final TestHarnessSubmissionInterface submissionInterface;

    @Nullable
    private TestHarnessAdapterStateContainer state;

    private final Logger logger = LoggerFactory.getLogger(TestHarnessAdapterMediator.class);

    private boolean performAdaptation = true;

    private AdaptedApplicationDeployer applicationDeployer;

    public synchronized void signalReady() {
        try {
            submissionInterface.ready().execute();
        } catch (IOException e) {
            ImmortalsErrorHandler.reportFatalException(e);
        }
    }

    public synchronized Response updateAdaptationStatus(@Nonnull AdaptationDetailsList adaptationDetailsList) {
        // TODO: Handle multiple adaptations
        AdaptationDetails ad = adaptationDetailsList.iterator().next();
        logger.info("Received AD: " + ad.dasOutcome.name());
        innerUpdateDasStatus(ad, true);
        // TODO: This should probably be more verbose...
        return Response.ok().build();
    }


    private synchronized void innerUpdateDasStatus(@Nonnull AdaptationDetails adaptationDetails, boolean sendUpdate) {
        state.testAdapterState.adaptation.details = adaptationDetails;
        DasOutcome previousOutcome = state.testAdapterState.adaptation.adaptationStatus;
        state.testAdapterState.adaptation.adaptationStatus = adaptationDetails.dasOutcome;

        logger.debug("Received AdaptationDetails with dasOutcome of '" + adaptationDetails.dasOutcome.name());

        switch (adaptationDetails.dasOutcome) {
            case PENDING:
                if (previousOutcome != DasOutcome.PENDING) {
                    ImmortalsErrorHandler.reportFatalError("Initial state of the DAS should not be PENDING!");
                }
                if (sendUpdate) {
                    try {
                        submissionInterface.status(state.testAdapterState).execute();
                    } catch (Exception e) {
                        ImmortalsErrorHandler.reportFatalException(e);
                    }
                }
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

    public synchronized Response updateDeploymentTestStatus(TestDetailsList testDetails) {
        innerUpdateValidatorStatus(testDetails, true);
        // TODO: This should probably be more verbose...
        return Response.ok().build();
    }

    private synchronized void innerUpdateValidatorStatus(TestDetailsList testDetails, boolean sendUpdate) {
        if (logger.isTraceEnabled()) {
            // TODO: Update to handle multiple test details
            String td;
            try {
                td = gson.toJson(testDetails);
            } catch (Exception e) {
                td = "PARSE ERROR!";
            }
            String val = "innerUpdateValidatorStatus:state: \n" +
                    (state == null ? "NULL" : state.getDisplayableState()) +
                    "testDetails: " + td;
            logger.trace(val);
        }

        // For each new test
        for (TestDetails newDetails : testDetails) {
            // Attempt to fetch an existing instance
            TestStateObject currentDetails = state.testAdapterState.validation.executedTests.stream()
                    .filter(t -> t.testIdentifier.equals(newDetails.testIdentifier)).findAny().orElse(null);

            if (currentDetails == null) {
                // And if it could not fetch an existing instance, add it as a new instance
                state.testAdapterState.validation.executedTests.add(new TestStateObject(newDetails));
            } else {
                // Otherwise update the existing test
                currentDetails.update(newDetails);
            }
        }

        // Then, if it is not the initial state, determine the resultant state
        VerdictOutcome vo = VerdictOutcome.PENDING;

        if (!state.initialValidationUpdate) {
            VerdictOutcome previousVO = state.testAdapterState.validation.verdictOutcome;

            testIter:
            for (TestStateObject tso : state.testAdapterState.validation.executedTests) {
                // TODO: Sort out which means which in terms of the final representative state with mixed test outcomes
                switch (tso.actualStatus) {
                    case PENDING:
                        vo = VerdictOutcome.PENDING;
//                        if (previousVO != VerdictOutcome.PENDING) {
//                            vo = VerdictOutcome.ERROR;
//                            ImmortalsErrorHandler.reportFatalError("Invalid validatation state transition for validator '" + tso.testIdentifier + "' from " + previousVO.name() + "to " + tso.actualStatus + "!");
//                            break testIter;
//                        }
                        break;

                    case NOT_APPLICABLE:
                        vo = VerdictOutcome.ERROR;
                        ImmortalsErrorHandler.reportFatalError("No validators should be NOT_APPLICABLE!");
                        break testIter;

                    case RUNNING:
//                        if (previousVO != VerdictOutcome.PENDING && previousVO != VerdictOutcome.RUNNING) {
//                            if (vo == VerdictOutcome.PENDING || vo == VerdictOutcome.RUNNING) {
//                                ImmortalsErrorHandler.reportFatalError("Unexpected verdict outcome transition from terminal state '"
//                                        + previousVO.name() + " to non-terminal state '" + vo.name() + "'!");
//                            } else {
//                                ImmortalsErrorHandler.reportFatalError("Unexpected verdict outcome transition from terminal state '"
//                                        + previousVO.name() + " to terminal state '" + vo.name() + "'!");
//                            }
//                        }

                        vo = VerdictOutcome.RUNNING;
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
                        break testIter;
                }
            }
        }

        state.testAdapterState.validation.verdictOutcome = vo;
        // Switch verdict outcome
        switch (vo) {
            case PENDING:
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
        state.initialValidationUpdate = false;
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

    public synchronized Response thPostEnabled(@Nonnull EnableDas enableDas) {
        if (performAdaptation == enableDas.dasEnabled) {
            return Response.ok().build();
        } else if (state == null) {
            performAdaptation = enableDas.dasEnabled;
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

    @Nullable
    private TestCoordinatorExecutionInterface getCoordinator(@Nonnull ChallengeProblem challengeProblem) {
        switch (challengeProblem) {
            case P2CP1:
                return new P2CP1TestCoordinator();

            case P2CP2:
                return new P2CP2TestCoordinator();

            case P2CP3:
                return new P2CP3TestCoordinator();

            default:
                return null;
        }
    }

    public synchronized Response submitSubmissionModel(@Nullable SubmissionModel submissionModel, @Nonnull ChallengeProblem challengeProblem) {
        // If something is currently running, submit a 503
        if (state != null) {
            String msg = "SubmissionModel submitted while a previous submission is currently running!";
            logger.error(msg);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(msg).build();
        }

        if (submissionModel == null) {
            logger.info("Empty submission model submitted. Assuming baseline. where adaptation is NOT_APPLICABLE.");
            // If null, assume baseline scenario with no adaptation
            submissionModel = new SubmissionModel();
            submissionModel.sessionIdentifier = "I" + System.currentTimeMillis();
            logger.info("Assigning adaptationIdentifier '" + submissionModel.sessionIdentifier + "' to baseline submission.");
            state = new TestHarnessAdapterStateContainer(submissionModel, challengeProblem);
            state.testAdapterState.adaptation.adaptationStatus = DasOutcome.NOT_APPLICABLE;
        } else {
            if (submissionModel.sessionIdentifier == null) {
                submissionModel.sessionIdentifier = "I" + System.currentTimeMillis();
                logger.info("Provided SubmissionModel has no identifier. Assigning adaptationIdentifier '" + submissionModel.sessionIdentifier + "'.");
            }
            state = new TestHarnessAdapterStateContainer(submissionModel, challengeProblem);

            if (!performAdaptation) {
                logger.info("Adaptation programatically disabled. Assuming adaptation is NOT_APPLICABLE.");
                state.testAdapterState.adaptation.adaptationStatus = DasOutcome.NOT_APPLICABLE;
            }
        }

        logger.debug("STATE HAS BEEN SET WITH VALUE: \n" + state.getDisplayableState());

        Response initialResponse;

        boolean environmentPerturbed =
                (submissionModel.martiServerModel != null &&
                        submissionModel.martiServerModel.requirements != null &&
                        (submissionModel.martiServerModel.requirements.libraryUpgrade != null ||
                                submissionModel.martiServerModel.requirements.partialLibraryUpgrade != null ||
                                submissionModel.martiServerModel.requirements.postgresqlPerturbation != null)
                );
                
        logger.debug("Submission model has no perturbations.");

        if (ImmortalsConfig.getInstance().debug.isUseMockTestCoordinators()) {
            if (environmentPerturbed && !performAdaptation) {
                Mock.desiredTestOutcome = TestOutcome.COMPLETE_FAIL;
            }
            initialResponse = new DummyTestCoordinator().execute(submissionModel, performAdaptation);
        } else {
            TestCoordinatorExecutionInterface testCoordinator = getCoordinator(challengeProblem);
            if (testCoordinator == null) {
                return Response.serverError().entity("Unexpected challenge problem identifier '" + challengeProblem.name() + "'!").build();
            }
            initialResponse = testCoordinator.execute(submissionModel, performAdaptation);
        }

        if (initialResponse.getStatus() > 399) {
            return initialResponse;
        } else {
            return Response.ok().entity(state.testAdapterState).build();
        }
    }
}

