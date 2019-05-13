package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataTransformer;
import mil.darpa.immortals.schemaevolution.BBNEvaluationData;
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import mil.darpa.immortals.schemaevolution.TerminalStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IntegrationTests {

	public static final String JARGS_EXTERNAL_SERVER_PORT = "mil.darpa.immortals.flitcons.test.server.external.port";
	public static final String JARGS_EXTERNAL_SERVER_HOST = "mil.darpa.immortals.flitcons.test.server.external.host";

	static {
		HierarchicalDataTransformer.ignoreEquations = true;
	}

	public AbstractOdbServer createServerInstance(@Nonnull TestScenario scenario) {

		if (System.getProperty(JARGS_EXTERNAL_SERVER_HOST) == null) {
			if (System.getProperty(JARGS_EXTERNAL_SERVER_PORT) == null) {
				return new OdbEmbeddedServer(scenario);
			} else {
				return new OdbRemoteServer(scenario, Integer.valueOf(System.getProperty(JARGS_EXTERNAL_SERVER_PORT)));
			}
		} else {
			if (System.getProperty(JARGS_EXTERNAL_SERVER_PORT) == null) {
				return new OdbRemoteServer(scenario, System.getProperty(JARGS_EXTERNAL_SERVER_HOST));
			} else {
				return new OdbRemoteServer(scenario, System.getProperty(JARGS_EXTERNAL_SERVER_HOST),
						Integer.valueOf(System.getProperty(JARGS_EXTERNAL_SERVER_PORT)));
			}
		}
	}

	public void runTest(@Nonnull TestScenario scenario,
	                    @Nullable Integer serverDelay) {
		AbstractOdbServer server = createServerInstance(scenario);


		try {
			List<String> expectedStates = new ArrayList<>(scenario.getExpectedStatusSequence());
			server.init();
			ChallengeProblemBridge cpb = new ChallengeProblemBridge();
			TerminalStatus expectedState;
			TerminalStatus currentState;

			do {
				String evaluationId = "I" + System.currentTimeMillis();
				expectedState = TerminalStatus.valueOf(expectedStates.remove(0));

				BBNEvaluationData data = cpb.getCurrentEvaluationData(evaluationId);
				Assert.assertEquals(data.getCurrentState(), TerminalStatus.ReadyForAdaptation.name());
				Assert.assertNull(data.getCurrentStateInfo());
				Assert.assertNull(data.getInputJsonData());
				Assert.assertNull(data.getOutputJsonData());

				SolverConfiguration config = SolverConfiguration.getInstance();
				config.useSimpleSolver = true;
				config.evaluationIdentifier = evaluationId;


				if (serverDelay != null) {
					server.clearState();
					data = cpb.getCurrentEvaluationData(evaluationId);
					Assert.assertNull(data.getCurrentState());
					Timer timer = new Timer(true);

					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							server.setReady();
						}
					}, serverDelay);
				}

				SolverMain.execute(config, cpb, evaluationId);

				BBNEvaluationData resultData = cpb.getCurrentEvaluationData(evaluationId);
				currentState = TerminalStatus.valueOf(resultData.getCurrentState());
				Assert.assertEquals(resultData.getCurrentState(), expectedState.name());
				server.setReady();

			} while (!currentState.isTerminal());

			Assert.assertEquals(expectedStates.size(), 0, "In terminal state while more expected states exist!");

		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			server.shutdown();
		}
	}

//	@Test
//	public void testInvalidInputPropagation() {
//
//	}
//
//	@Test
//	public void testUnexpectedErrorPropagation() {
//
//	}

	@Test
	public void testServerWaitForReady() {
		runTest(TestScenario.getTestScenario("s5"), 4000);
	}

	@Test
	public void testServerReady() {
		runTest(TestScenario.getTestScenario("s5"), null);
	}

	@Test
	public void testServerSequential() {
		runTest(TestScenario.getTestScenario("s5s"), null);
	}
}
