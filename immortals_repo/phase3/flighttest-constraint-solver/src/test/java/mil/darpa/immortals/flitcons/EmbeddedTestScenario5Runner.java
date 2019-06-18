package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.orientdbserver.TestScenario;
import mil.darpa.immortals.testing.tools.TestScenarioRunner;

import javax.annotation.Nonnull;

public class EmbeddedTestScenario5Runner extends TestScenarioRunner {

	private Thread solverThread;

	private EmbeddedTestScenario5Runner(@Nonnull TestScenario testScenario) {
		super(testScenario);
	}

	public static TestScenarioRunner createScenario5Runner(@Nonnull String shortTestLabel) {
		return new EmbeddedTestScenario5Runner(TestScenario.getScenario5TestScenario(shortTestLabel));
	}

	@Override
	protected synchronized void startAdaptationService() {
		if (solverThread != null) {
			if (solverThread.isAlive()) {
				throw new RuntimeException("Adaptation thread is already running!");
			}
		}
		SolverConfiguration.getInstance().useSimpleSolver = true;
		solverThread = new Thread(() -> {
			System.out.println("Starting Embedded Scenario 5 Adaptation Session");
			SolverMain.execute();
			System.out.println("Finished Embedded Scenario 5 Adaptation Session");
		});
		solverThread.start();
	}

	@Override
	protected void killAdaptationService() {
		solverThread.interrupt();
	}
}
