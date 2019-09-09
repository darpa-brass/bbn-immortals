package mil.darpa.immortals.orientdbserver;

import mil.darpa.immortals.EnvironmentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;

public class JarTestScenarioRunner extends TestScenarioRunner {

	private static final Logger logger = LoggerFactory.getLogger(JarTestScenarioRunner.class);

	private Process adaptationServiceProcess;

	public boolean useSimpleSolver = false;

	private JarTestScenarioRunner(@Nonnull TestScenario testScenario) {
		super(testScenario);
	}

	public static TestScenarioRunner createScenario5Runner(@Nonnull String shortTestLabel) {
		return new JarTestScenarioRunner(TestScenarios.getTestScenario(shortTestLabel));
	}

	public static TestScenarioRunner createScenario6Runner(@Nonnull String shortTestLabel) {
		return new JarTestScenarioRunner(TestScenarios.getTestScenario(shortTestLabel));
	}

	@Override
	protected synchronized void startAdaptationService() {
		if (adaptationServiceProcess != null) {
			if (adaptationServiceProcess.isAlive()) {
				throw new RuntimeException("Adaptation service is already running!");
			}
		}
		try {
			logger.info("Starting adaptation service from jar file...");
			String[] cmd;

			if (scenario.getScenarioType().isScenario5) {
				cmd = new String[]{
						"bash", EnvironmentConfiguration.getImmortalsRoot().resolve("phase3").resolve("start.sh").toString(),
						"--odb-url", odbServer.getOdbPath(scenario).replace("plocal", "remote"),
						"--scenario", "5",
						"--artifact-directory", EnvironmentConfiguration.getArtifactDirectory().toString(),
						"--debug-mode",
						"--monochrome-mode"
				};
				if (useSimpleSolver) {
					cmd = Arrays.copyOf(cmd, cmd.length + 1);
					cmd[cmd.length - 1] = "--simple-solver";
				}

			} else if (scenario.getScenarioType().isScenario6) {
				cmd = new String[]{
						"bash", EnvironmentConfiguration.getImmortalsRoot().resolve("phase3").resolve("start.sh").toString(),
						"--odb-url", odbServer.getOdbPath(scenario),
						"--scenario", "6",
						"--artifact-directory", EnvironmentConfiguration.getArtifactDirectory().toString()
				};

			} else {
				if (scenario.getScenarioType() == null) {
					throw new RuntimeException("Scenario type is null!");
				}
				throw new RuntimeException("Unexpected Scenario type '" + scenario.getScenarioType() + "'!");
			}

			logger.info("CMD: [" + String.join(" ", cmd) + "]");

			ProcessBuilder pb = new ProcessBuilder()
					.inheritIO()
					.redirectOutput(ProcessBuilder.Redirect.INHERIT)
					.redirectError(ProcessBuilder.Redirect.INHERIT)
					.directory(EnvironmentConfiguration.getImmortalsRoot().resolve("phase3").toFile())
					.command(cmd);
			adaptationServiceProcess = pb.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	protected void killAdaptationService() {
		try {
			adaptationServiceProcess.destroy();
			adaptationServiceProcess.wait(12000);
			adaptationServiceProcess.destroyForcibly();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
