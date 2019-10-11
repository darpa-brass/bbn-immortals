package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.orientdbserver.TestScenario;
import mil.darpa.immortals.orientdbserver.TestScenarios;
import mil.darpa.immortals.orientdbserver.TestScenarioRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class EmbeddedTestScenario5Runner extends TestScenarioRunner {

	private final Logger logger = LoggerFactory.getLogger(EmbeddedTestScenario5Runner.class);

	private Thread solverThread;

	private EmbeddedTestScenario5Runner(@Nonnull TestScenario testScenario) {
		super(testScenario);
	}

	public static TestScenarioRunner createScenario5Runner(@Nonnull String shortTestLabel) {
		return new EmbeddedTestScenario5Runner(TestScenarios.getTestScenario(shortTestLabel));
	}

	@Override
	protected synchronized void startAdaptationService() {
		if (solverThread != null) {
			if (solverThread.isAlive()) {
				throw new RuntimeException("Adaptation thread is already running!");
			}
		}
		SolverConfiguration.getInstance().setUseSimpleSolver(true);
		solverThread = new Thread(() -> {
			logger.info("Starting Embedded Scenario 5 Adaptation Session");
			SolverMain.execute();
			logger.info("Finished Embedded Scenario 5 Adaptation Session");
		});
		solverThread.start();
	}

	@Override
	protected void killAdaptationService() {
		solverThread.interrupt();
	}

	@Override
	public TestScenarioRunner setMaxDauCount(int count) {
		// No-op. Doesn't matter for the Simple Solver
		return this;
	}
}
