package mil.darpa.immortals.testing.tools;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import mil.darpa.immortals.orientdbserver.*;
import mil.darpa.immortals.schemaevolution.BBNEvaluationData;
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import mil.darpa.immortals.schemaevolution.ProvidedData;
import mil.darpa.immortals.schemaevolution.TerminalStatus;
import org.testng.Assert;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestScenarioRunner {
	private void startScenario(@Nonnull TestScenario scenario, @Nonnull AbstractOdbServer server) throws IOException, InterruptedException {

		Path artifactDirectory = ProvidedData.getEvaluationArtifactDirectory();

		if (!Files.exists(artifactDirectory)) {
			Files.createDirectory(artifactDirectory);
		}

		String[] cmd;
		long timeoutMS;

		if (scenario.getScenarioType().equals("Scenario5")) {
			timeoutMS = 60000;
			String[] s5cmd = {
					"bash", ProvidedTestingData.getSutStartupScript(),
					"--odb-url", server.getOdbPath(),
					"--scenario", "5",
					"--stop-on-finish",
					"--artifact-directory", artifactDirectory.toAbsolutePath().toString()
			};
			cmd = s5cmd;

		} else if (scenario.getScenarioType().equals("Scenario6")) {
			timeoutMS = 1200000;
			String[] s6cmd = {
					"bash", ProvidedTestingData.getSutStartupScript(),
					"--odb-url", server.getOdbPath(),
					"--scenario", "6",
					"--artifact-directory", artifactDirectory.toAbsolutePath().toString()
			};
			cmd = s6cmd;

		} else {
			if (scenario.getScenarioType() == null) {
				throw new RuntimeException("Scenario type is null!");
			}
			throw new RuntimeException("Unexpected Scenario type '" + scenario.getScenarioType() + "'!");
		}

		System.out.println("CMD: [" + String.join(" ", cmd) + "]");

		// TODO: Be smarter with the timeout

		ProcessBuilder pb = new ProcessBuilder()
				.redirectOutput(ProcessBuilder.Redirect.INHERIT)
				.redirectError(ProcessBuilder.Redirect.INHERIT)
				.directory(Paths.get(ProvidedTestingData.getSutStartupScript()).getParent().toFile())
				.command(cmd);
		Process p = pb.start();

		p.waitFor(timeoutMS, TimeUnit.MILLISECONDS);
		if (p.isAlive()) {
			throw new RuntimeException("Process did not complete in the expected time!");
		}
	}

	private AbstractOdbServer createServerInstance(@Nonnull TestScenario scenario) {

		if (ProvidedTestingData.getOdbExternalServerHost() == null) {
			if (ProvidedTestingData.getOdbExternalServerPort() == null) {
				return new OdbEmbeddedServer(scenario);
			} else {
				return new OdbRemoteServer(scenario, ProvidedTestingData.getOdbExternalServerPort());
			}
		} else {
			if (ProvidedTestingData.getOdbExternalServerPort() == null) {
				return new OdbRemoteServer(scenario, ProvidedTestingData.getOdbExternalServerHost());
			} else {
				return new OdbRemoteServer(scenario, ProvidedTestingData.getOdbExternalServerHost(), ProvidedTestingData.getOdbExternalServerPort());
			}
		}
	}

	public static void runScenario5Test(@Nonnull String shortLabel) {
		try {
			TestScenarioRunner runner = new TestScenarioRunner();
			runner.runTest(TestScenario.getScenario5TestScenario(shortLabel));
		} catch (Exception e) {
			Assert.fail("Unexpected Exception thrown within test harness!", e);
		}
	}

	public static void runScenario6Test(@Nonnull String shortLabel) {
		try {
			TestScenarioRunner runner = new TestScenarioRunner();
			runner.runTest(TestScenario.getScenario6TestScenario(shortLabel));
		} catch (Exception e) {
			Assert.fail("Unexpected Exception thrown within test harness!", e);
		}
	}

	public void runTest(@Nonnull TestScenario scenario) {
		AbstractOdbServer server = createServerInstance(scenario);

		try {
			List<String> expectedStates = new ArrayList<>(scenario.getExpectedStatusSequence());
			if (expectedStates.size() > 1) {
				Assert.fail("Cannot execute a scenario with multiple expected terminal states!");

			}
			server.init();
			ChallengeProblemBridge cpb = new ChallengeProblemBridge();
			TerminalStatus expectedState;

			expectedState = TerminalStatus.valueOf(expectedStates.remove(0));
			startScenario(scenario, server);
			BBNEvaluationData resultData = cpb.getCurrentEvaluationDataNoSave();
			Assert.assertEquals(resultData.getCurrentState(), expectedState.name());
			String resultJsonString = resultData.getOutputJsonData();
			if (resultJsonString != null) {
				Gson gson = new Gson();
				JsonObject resultJson = gson.fromJson(resultJsonString, JsonObject.class);
				scenario.validateJsonOutputStructure(resultJson);
			}

		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			server.shutdown();
		}
	}

}
