package mil.darpa.immortals.testing.tools;

import mil.darpa.immortals.orientdbserver.OdbEmbeddedServer;
import mil.darpa.immortals.orientdbserver.TestScenario;
import mil.darpa.immortals.schemaevolution.BBNEvaluationData;
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import mil.darpa.immortals.schemaevolution.ProvidedData;
import mil.darpa.immortals.schemaevolution.TerminalStatus;
import org.testng.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class TestScenarioRunner {

	protected final TestScenario scenario;

	protected OdbEmbeddedServer odbServer;

	private static final Timer timer = new Timer(true);

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
		String baseIdentifier = "TEST-" + scenario.getShortName();
		int iterationCount = 0;

		odbServer = new OdbEmbeddedServer(scenario);

		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				kill();
				Assert.fail("Test exceeded timout!");
			}
		};
		timer.schedule(task, scenario.getTimeoutMS());

		try {

			List<String> expectedStates = new ArrayList<>(scenario.getExpectedStatusSequence());
			odbServer.init();

			TerminalStatus expectedState;
			TerminalStatus currentState;

			String evaluationId = baseIdentifier + "-iteration" + iterationCount++;
			ChallengeProblemBridge cpb = ProvidedData.initializeChallengeProblemBridge(evaluationId);

			boolean adaptationServerStarted = false;

			do {
				expectedState = TerminalStatus.valueOf(expectedStates.remove(0));

				BBNEvaluationData data = cpb.getCurrentEvaluationData(evaluationId);
				Assert.assertEquals(data.getCurrentState(), TerminalStatus.ReadyForAdaptation.name());
				Assert.assertNull(data.getCurrentStateInfo());
				Assert.assertNull(data.getOutputJsonData());

				if (scenario.getScenarioType().equals("Scenario5")) {
					Assert.assertNull(data.getInputJsonData());
				} else {
					Assert.assertNotNull(data.getInputJsonData());
				}


				if (odbServerStartDelay != null) {
					odbServer.clearState();
					data = cpb.getCurrentEvaluationData(evaluationId);
					Assert.assertNull(data.getCurrentState());

					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							odbServer.setReady();
						}
					}, odbServerStartDelay);
				}

				if (!adaptationServerStarted) {
					startAdaptationService();
					adaptationServerStarted = true;
				}

				odbServer.waitForEvaluatorTurn();

				BBNEvaluationData resultData = cpb.getCurrentEvaluationData(evaluationId);
				currentState = TerminalStatus.valueOf(resultData.getCurrentState());
				Assert.assertEquals(resultData.getCurrentState(), expectedState.name());

				evaluationId = baseIdentifier + "-iteration" + iterationCount++;
				cpb = ProvidedData.initializeChallengeProblemBridge(evaluationId);

				odbServer.setReady();

			} while (!currentState.isTerminal() && expectedStates.size() > 0);

			Assert.assertEquals(expectedStates.size(), 0, "In terminal state while more expected states exist!");

		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			task.cancel();
			odbServer.shutdown();
		}
	}
}
