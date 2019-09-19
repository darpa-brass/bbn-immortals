package mil.darpa.immortals.orientdbserver;

import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.schemaevolution.BBNEvaluationData;
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import mil.darpa.immortals.schemaevolution.TerminalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TestScenarioRunner {

	private static final Logger logger = LoggerFactory.getLogger(TestScenarioRunner.class);

	protected final TestScenario scenario;

	protected OdbEmbeddedServer odbServer;

	protected abstract void startAdaptationService();

	protected abstract void killAdaptationService();

	private void kill() {
		System.err.print("Killing OrientDB Server...");
		odbServer.shutdown();
		System.err.println("Done");
		System.err.print("Killing Adaptation Service...");
		killAdaptationService();
		System.err.println("Done");
	}

	protected TestScenarioRunner(@Nonnull TestScenario scenario) {
		this.scenario = scenario;
	}

	public void execute() {
		execute(null);
	}

	public void execute(@Nullable Integer odbServerStartDelay) {
		AtomicInteger atomicStartDelay = new AtomicInteger(odbServerStartDelay == null ? 0 : odbServerStartDelay);

		String baseIdentifier = "TEST-" + scenario.getShortName();
		int iterationCount = 0;

		odbServer = new OdbEmbeddedServer(scenario);

		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				logger.info("TEST FAILURE: Test exceeded timeout!");
				kill();
				Assert.fail("Test exceeded timout!");

			}
		}, scenario.getTimeoutMS());

		try {
			List<String> expectedStates = new ArrayList<>(scenario.getExpectedStatusSequence());
			odbServer.init(OdbEmbeddedServer.OdbDeploymentMode.BackupsOnly);

			TerminalStatus expectedState;
			TerminalStatus currentState;

			String evaluationId = baseIdentifier + "-iteration" + iterationCount++;
			ChallengeProblemBridge cpb = EnvironmentConfiguration.initializeChallengeProblemBridge(evaluationId);

			boolean adaptationServerStarted = false;

			do {
				expectedState = TerminalStatus.valueOf(expectedStates.remove(0));

				BBNEvaluationData data = cpb.getCurrentEvaluationData(evaluationId);
				Assert.assertNull(data.getCurrentStateInfo());
				Assert.assertNull(data.getOutputJsonData());

				if (scenario.getScenarioType().isScenario5) {
					Assert.assertNull(data.getInputJsonData());
				} else {
					Assert.assertNotNull(data.getInputJsonData());
				}

				int startDelay = atomicStartDelay.get();
				if (startDelay > 0) {
					odbServer.clearState(scenario);
					data = cpb.getCurrentEvaluationData(evaluationId);
					Assert.assertNull(data.getCurrentState());

					logger.info("Delaying server ready by " + startDelay + " ms.");

					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							odbServer.setState(scenario, TerminalStatus.ReadyForAdaptation, false);

							atomicStartDelay.set(0);
						}
					}, startDelay);
				} else {
					odbServer.setState(scenario, TerminalStatus.ReadyForAdaptation, false);
				}

				if (!adaptationServerStarted) {
					startAdaptationService();
					adaptationServerStarted = true;
				}

				odbServer.waitForEvaluatorTurn(scenario);

				BBNEvaluationData resultData = cpb.getCurrentEvaluationData(evaluationId);
				currentState = TerminalStatus.valueOf(resultData.getCurrentState());

				if (resultData.getCurrentState().equals(expectedState.name())) {
					logger.info("TEST STATE '" + resultData.getCurrentState() + "' == '" + expectedState.name() + "'");
				} else {
					String msg = "State '" + resultData.getCurrentState() + "' != '" + expectedState.name() + "'!";
					logger.info("TEST FAILURE: " + msg);
					Assert.fail(msg);
				}

				evaluationId = baseIdentifier + "-iteration" + iterationCount++;
				cpb = EnvironmentConfiguration.initializeChallengeProblemBridge(evaluationId);

				odbServer.setState(scenario, TerminalStatus.ReadyForAdaptation, true);

			} while (!currentState.isTerminal() && expectedStates.size() > 0);

			if (expectedStates.size() != 0) {
				String msg = "Terminal state reached while expected states [" + String.join(",", expectedStates) + "] remain!";
				logger.info("TEST FAILURE: " + msg);
				Assert.fail(msg);
			}

			Assert.assertEquals(expectedStates.size(), 0, "In terminal state while more expected states exist!");

		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			timer.cancel();
			odbServer.shutdown();
		}
	}
}
