package mil.darpa.immortals.orientdbserver;

import picocli.CommandLine;

import java.io.File;
import java.util.List;

public class ImmortalsOdbServerMain {

	@CommandLine.Option(names = "--scenario5-regen", description = "Regenerates the server data for Scenario 5 tests to the target folder.")
	private File scenario5RegenerationTarget;

	@CommandLine.Option(names = "--scenario6-regen", description = "Regenerates the server data for Scenario 6 tests to the target folder.")
	private File scenario6RegenerationTarget;

	@CommandLine.Option(names = "--start", description = "Start the server with the specified scenario identifier")
	private String scenarioToStart;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	private boolean helpRequested = false;

	public static void main(String[] args) {
		ImmortalsOdbServerMain server = new ImmortalsOdbServerMain();
		CommandLine.populateCommand(server, args);
		server.execute();
	}

	private void execute() {
		if (helpRequested || (scenario5RegenerationTarget == null && scenario6RegenerationTarget == null && scenarioToStart == null)) {
			CommandLine.usage(this, System.out);
			return;
		}

		if (scenario5RegenerationTarget != null) {
			for (String scenarioName : TestScenario.getScenario5TestScenarioIdentifiers()) {
				TestScenario scenario = TestScenario.getScenario5TestScenario(scenarioName);
				OdbEmbeddedServer server = new OdbEmbeddedServer(scenario);
				server.shutdown();
			}
		}

		if (scenario6RegenerationTarget != null) {
			for (String scenarioName : TestScenario.getScenario6TestScenarioIdentifiers()) {
				TestScenario scenario = TestScenario.getScenario6TestScenario(scenarioName);
				OdbEmbeddedServer server = new OdbEmbeddedServer(scenario);
				server.shutdown();
			}
		}

		if (scenarioToStart != null) {
			if (TestScenario.getScenario5TestScenarioIdentifiers().contains(scenarioToStart)) {
				OdbEmbeddedServer server = new OdbEmbeddedServer(TestScenario.getScenario5TestScenario(scenarioToStart));
				server.init();
				server.waitForShutdown();

			} else if (TestScenario.getScenario6TestScenarioIdentifiers().contains(scenarioToStart)) {
				OdbEmbeddedServer server = new OdbEmbeddedServer(TestScenario.getScenario5TestScenario(scenarioToStart));
				server.init();
				server.waitForShutdown();

			} else {
				List<String> scenarioList = TestScenario.getAllTestScenarioIdentifiers();
				String scenarioListString = String.join("\n\t", scenarioList);
				System.err.println("Invalid scenario identifier '" + scenarioToStart + "'.\nValid Identifiers:\n\t" + scenarioListString);
				System.exit(-1);
			}
		}
	}
}
