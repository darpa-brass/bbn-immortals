package mil.darpa.immortals.orientdbserver;

import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class ImmortalsOdbServerConfiguration {
	private static ImmortalsOdbServerConfiguration instance;

	@CommandLine.Option(names = "--regen-scenario5-bbn",
			description = "Regenerates the server data for BBN authored Scenario 5  examples into the designated storage directory")
	private boolean regenerateScenario5bbn = false;

	@CommandLine.Option(names = "--regen-scenario5-swri",
			description = "Regenerates the server data for SwRI authrored Scenario 5  examples into the designated storage directory if the source files have changed.")
	private boolean regenerateScenario5swri = false;

	@CommandLine.Option(names = "--regen-scenario6",
			description = "Regenerates the server data for Scenario 6 into the designated storage directory")
	private boolean regenerateScenario6 = false;

	@CommandLine.Option(names = {"--regen-scenario"},
			description = "Regenerates the specified scenario(s)")
	private List<String> scenariosToRegenerate = new LinkedList<>();

	@CommandLine.Option(names = {"-s", "--start"}, description = "Start the server with the specified scenarios")
	private List<String> scenariosToStart = new LinkedList<>();

	static class DeploymentModes extends ArrayList<String> {
		DeploymentModes() {
			super(Arrays.stream(OdbEmbeddedServer.OdbDeploymentMode.values()).map(OdbEmbeddedServer.OdbDeploymentMode::name).collect(Collectors.toList()));
		}
	}

	@CommandLine.Option(names = {"--deployment-mode"}, completionCandidates = DeploymentModes.class, defaultValue = "BackupsWithUpdatedXmlIfAvailable",
			description = "The mode for deployment. Default: '${DEFAULT-VALUE}'. Valid values: ${COMPLETION-CANDIDATES}")
	private OdbEmbeddedServer.OdbDeploymentMode deploymentMode;

	@CommandLine.Option(names = {"--keep-running", "-k"}, description = "If true and provided with a regen option, the server will continue running after generating the new scenario.")
	public boolean keepRunning = false;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	private boolean helpRequested = false;

	@CommandLine.Option(names = {"--regen-scenarios-from-file"}, description = "Regenerates the scenarios based on the configuration file")
	private String scenarioRegenerationFile = null;

	private ImmortalsOdbServerConfiguration() {
	}

	public synchronized static ImmortalsOdbServerConfiguration getInstance() {
		if (instance == null) {
			instance = new ImmortalsOdbServerConfiguration();
		}
		return instance;
	}

//	public static void setInstance(ImmortalsOdbServerConfiguration instance) {
//		ImmortalsOdbServerConfiguration.instance = instance;
//	}

	public List<TestScenario> getScenariosToStart() {
		LinkedList<TestScenario> scenarios = new LinkedList<>();
		for (String shortName : scenariosToStart) {
			TestScenario scenario = TestScenarios.getTestScenario(shortName);
			if (scenario == null) {
				throw new RuntimeException("Invalid test scenario '" + shortName + "'!");
			}
			scenarios.add(scenario);
		}
		return scenarios;
	}

	public OdbEmbeddedServer.OdbDeploymentMode getDeploymentMode() {
		return deploymentMode;
	}

	public void validate() {
		if (helpRequested || (getScenariosToRegenerate().size() == 0 && getScenariosToStart().size() == 0)) {
			CommandLine.usage(this, System.out);
			System.exit(1);
		}
	}

	public List<TestScenario> getScenariosToRegenerate() {
		List<TestScenario> scenarios = new LinkedList<>();

		if (regenerateScenario5bbn) {
			scenarios.addAll(TestScenarios.getBbnScenario5TestScenarios());
		}

		if (regenerateScenario5swri) {
			scenarios.addAll(TestScenarios.getSwriScenario5TestScenarios());
		}

		if (regenerateScenario6) {
			scenarios.addAll(TestScenarios.getAllScenario6TestScenarios());
		}

		if (scenariosToRegenerate.size() > 0) {
			for (String shortName : scenariosToRegenerate) {
				if (scenarios.stream().noneMatch(x -> x.getShortName().equals(shortName))) {
					TestScenario scenario = TestScenarios.getTestScenario(shortName);
					if (scenario == null) {
						throw new RuntimeException("Invalid test scenario '" + shortName + "'!");
					}
					scenarios.add(scenario);
				}
			}
		}

		if (scenarioRegenerationFile != null) {
			TestScenarios additionalScenarios = new TestScenarios(new File(scenarioRegenerationFile));
			for (TestScenario scenario : additionalScenarios.scenarios) {
				if (scenarios.stream().noneMatch(x -> x.getShortName().equals(scenario.getShortName()))) {
					scenarios.add(scenario);
				}
			}
		}
		return scenarios;
	}
}
