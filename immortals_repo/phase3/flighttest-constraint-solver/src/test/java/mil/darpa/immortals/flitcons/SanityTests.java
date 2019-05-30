package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.schemaevolution.BBNEvaluationData;
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import mil.darpa.immortals.schemaevolution.ProvidedData;
import mil.darpa.immortals.schemaevolution.TerminalStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SanityTests {

	public static final String JARGS_EXTERNAL_SERVER_PORT = "mil.darpa.immortals.flitcons.test.server.external.port";
	public static final String JARGS_EXTERNAL_SERVER_HOST = "mil.darpa.immortals.flitcons.test.server.external.host";

	public AbstractFlitconsOdbServer createServerInstance(@Nonnull FlitconsTestScenario scenario) {

		System.out.println("PATH: " + Paths.get("").toAbsolutePath().toString());

		if (System.getProperty(JARGS_EXTERNAL_SERVER_HOST) == null) {
			if (System.getProperty(JARGS_EXTERNAL_SERVER_PORT) == null) {
				return new FlitconsOdbEmbeddedServer(scenario);
			} else {
				return new FlitconsOdbRemoteServer(scenario, Integer.valueOf(System.getProperty(JARGS_EXTERNAL_SERVER_PORT)));
			}
		} else {
			if (System.getProperty(JARGS_EXTERNAL_SERVER_PORT) == null) {
				return new FlitconsOdbRemoteServer(scenario, System.getProperty(JARGS_EXTERNAL_SERVER_HOST));
			} else {
				return new FlitconsOdbRemoteServer(scenario, System.getProperty(JARGS_EXTERNAL_SERVER_HOST),
						Integer.valueOf(System.getProperty(JARGS_EXTERNAL_SERVER_PORT)));
			}
		}
	}

	public void runTest(@Nonnull FlitconsTestScenario scenario, boolean useSimpleSolver,
	                    @Nullable Integer serverDelay) {
		String baseIdentifier = "TEST-" + scenario.getShortName();
		int iterationCount = 0;

		AbstractFlitconsOdbServer server = createServerInstance(scenario);


		try {
			List<String> expectedStates = new ArrayList<>(scenario.getExpectedStatusSequence());
			server.init();

			TerminalStatus expectedState;
			TerminalStatus currentState;

			do {
				String evaluationId = baseIdentifier + "-iteration" + iterationCount++;
				ChallengeProblemBridge cpb = ProvidedData.initializeChallengeProblemBridge(evaluationId);

				expectedState = TerminalStatus.valueOf(expectedStates.remove(0));

				BBNEvaluationData data = cpb.getCurrentEvaluationData(evaluationId);
				Assert.assertEquals(data.getCurrentState(), TerminalStatus.ReadyForAdaptation.name());
				Assert.assertNull(data.getCurrentStateInfo());
				Assert.assertNull(data.getInputJsonData());
				Assert.assertNull(data.getOutputJsonData());

				SolverConfiguration config = SolverConfiguration.getInstance();
				config.useSimpleSolver = useSimpleSolver;
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

				SolverMain.execute(cpb, evaluationId);

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

	@Test
	public void testServerWaitForReady() {
		runTest(FlitconsTestScenario.getTestScenario("s5"), true, 4000);
	}

	@Test
	public void testServerReady() {
		runTest(FlitconsTestScenario.getTestScenario("s5"), true, null);
	}

	@Test
	public void testServerSequential() {
		runTest(FlitconsTestScenario.getTestScenario("s5s"), true, null);
	}
}
