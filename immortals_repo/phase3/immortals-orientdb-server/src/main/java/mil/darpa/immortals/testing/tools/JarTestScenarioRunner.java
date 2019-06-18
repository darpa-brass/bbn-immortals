package mil.darpa.immortals.testing.tools;

import mil.darpa.immortals.orientdbserver.ProvidedTestingData;
import mil.darpa.immortals.orientdbserver.TestScenario;
import mil.darpa.immortals.schemaevolution.ProvidedData;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JarTestScenarioRunner extends TestScenarioRunner {

	private Process adaptationServiceProcess;

	private JarTestScenarioRunner(@Nonnull TestScenario testScenario) {
		super(testScenario);
	}

	public static TestScenarioRunner createScenario5Runner(@Nonnull String shortTestLabel) {
		return new JarTestScenarioRunner(TestScenario.getScenario5TestScenario(shortTestLabel));
	}

	public static TestScenarioRunner createScenario6Runner(@Nonnull String shortTestLabel) {
		return new JarTestScenarioRunner(TestScenario.getScenario6TestScenario(shortTestLabel));
	}

	@Override
	protected synchronized void startAdaptationService() {
		if (adaptationServiceProcess != null) {
			if (adaptationServiceProcess.isAlive()) {
				throw new RuntimeException("Adaptation service is already running!");
			}
		}
		try {
			Path artifactDirectory = ProvidedData.getEvaluationArtifactDirectory();

			if (!Files.exists(artifactDirectory)) {
				Files.createDirectory(artifactDirectory);
			}

			String[] cmd;

			if (scenario.getScenarioType().equals("Scenario5")) {
				cmd = new String[]{
						"bash", ProvidedTestingData.getImmortalsRoot().resolve("phase3").resolve("start.sh").toString(),
						"--odb-url", odbServer.getOdbPath().replace("plocal", "remote"),
						"--scenario", "5",
						"--artifact-directory", artifactDirectory.toAbsolutePath().toString()
				};

			} else if (scenario.getScenarioType().equals("Scenario6")) {
				cmd = new String[]{
						"bash", ProvidedTestingData.getImmortalsRoot().resolve("phase3").resolve("start.sh").toString(),
						"--odb-url", odbServer.getOdbPath(),
						"--scenario", "6",
						"--artifact-directory", artifactDirectory.toAbsolutePath().toString()
				};

			} else {
				if (scenario.getScenarioType() == null) {
					throw new RuntimeException("Scenario type is null!");
				}
				throw new RuntimeException("Unexpected Scenario type '" + scenario.getScenarioType() + "'!");
			}

			System.out.println("CMD: [" + String.join(" ", cmd) + "]");

			ProcessBuilder pb = new ProcessBuilder()
					.redirectOutput(ProcessBuilder.Redirect.INHERIT)
					.redirectError(ProcessBuilder.Redirect.INHERIT)
					.directory(ProvidedTestingData.getImmortalsRoot().resolve("phase3").toFile())
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
