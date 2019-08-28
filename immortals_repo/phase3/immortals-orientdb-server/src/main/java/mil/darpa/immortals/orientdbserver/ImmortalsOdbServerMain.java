package mil.darpa.immortals.orientdbserver;

import mil.darpa.immortals.schemaevolution.TerminalStatus;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImmortalsOdbServerMain {

	public static void main(String[] args) {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		ImmortalsOdbServerConfiguration config = ImmortalsOdbServerConfiguration.getInstance();
		ImmortalsOdbServerMain server = new ImmortalsOdbServerMain();
		CommandLine.populateCommand(config, args);
		config.validate();
		server.execute();
	}

	private void execute() {
		ImmortalsOdbServerConfiguration config = ImmortalsOdbServerConfiguration.getInstance();
		OdbEmbeddedServer server;

		if (config.isRegenerateScenario5bbn() || config.isRegenerateScenario5swri() || config.isRegenerateScenario6() ||
				config.getScenarioToRegenerate() != null) {
			ArrayList<TestScenario> scenarios = new ArrayList<>();

			if (config.isRegenerateScenario5bbn()) {
				List<TestScenario> s5Scenarios = TestScenario.getBbnScenario5TestScenarioIdentifiers().stream().map(
						TestScenario::getScenario5TestScenario).collect(Collectors.toList());
				scenarios.addAll(s5Scenarios);
			}

			if (config.getScenarioToRegenerate() != null) {
				TestScenario scenario = TestScenario.getScenario5TestScenario(config.getScenarioToRegenerate());
				scenarios.add(scenario);
			}

			if (config.isRegenerateScenario5swri()) {
				List<TestScenario> s5Scenarios = TestScenario.getSwriScenario5TestScenarioIdentifiers().stream().map(
						TestScenario::getScenario5TestScenario).collect(Collectors.toList());
				scenarios.addAll(s5Scenarios);
			}

			if (config.isRegenerateScenario6()) {
				List<TestScenario> s6Scenarios = TestScenario.getScenario6TestScenarioIdentifiers().stream().map(
						TestScenario::getScenario6TestScenario).collect(Collectors.toList());
				scenarios.addAll(s6Scenarios);
			}

			server = new OdbEmbeddedServer(scenarios.toArray(new TestScenario[0]));
			server.init(false);
			if (!config.keepRunning) {
				server.shutdown();
			}
		}

		if (config.getDauInventoryXmlPath() != null || config.getInputMdlrooXmlPath() != null || config.getScenarioToStart() != null) {

			ArrayList<TestScenario> testScenarios = new ArrayList<>();
			if (config.getDauInventoryXmlPath() != null || config.getInputMdlrooXmlPath() != null) {
				List<String> expectedStatusSequence = new ArrayList<>(2);
				expectedStatusSequence.add("AdaptationSuccessful");
				expectedStatusSequence.add("AdaptationUnsuccessful");
				TestScenario ts = new TestScenario(
						"tmp",
						"Temporary XML Scenario",
						"Scenario5",
						6000000,
						config.getDauInventoryXmlPath(),
						config.getInputMdlrooXmlPath(),
						null,
						expectedStatusSequence,
						null
				);
				testScenarios.add(ts);
			}

			String scenarioToStart = config.getScenarioToStart();
			if (scenarioToStart != null) {
				if (TestScenario.getAllScenario5TestScenarioIdentifiers().contains(scenarioToStart)) {
					testScenarios.add(TestScenario.getScenario5TestScenario(scenarioToStart));

				} else if (TestScenario.getScenario6TestScenarioIdentifiers().contains(scenarioToStart)) {
					testScenarios.add(TestScenario.getScenario6TestScenario(scenarioToStart));

				} else {
					List<String> scenarioList = TestScenario.getAllTestScenarioIdentifiers();
					String scenarioListString = String.join("\n\t", scenarioList);
					System.err.println("Invalid scenario identifier '" + scenarioToStart + "'.\nValid Identifiers:\n\t" + scenarioListString);
					System.exit(-1);
				}
			}

			server = new OdbEmbeddedServer(testScenarios.toArray(new TestScenario[0]));
			server.init();

			for (TestScenario scenario : testScenarios) {
				server.setState(scenario, TerminalStatus.ReadyForAdaptation, false);
			}
			server.waitForShutdown();
		}
	}
}
