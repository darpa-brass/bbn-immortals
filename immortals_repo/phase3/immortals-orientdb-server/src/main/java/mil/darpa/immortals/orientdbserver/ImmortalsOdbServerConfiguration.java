package mil.darpa.immortals.orientdbserver;

import picocli.CommandLine;

import java.util.List;

public class ImmortalsOdbServerConfiguration {
	private static ImmortalsOdbServerConfiguration instance;

	@CommandLine.Option(names = "--regen-scenario5",
			description = "Regenerates the server data for Scenario 5 into the designated storage directory")
	private boolean regenerateScenario5 = false;
	@CommandLine.Option(names = "--regen-scenario6",
			description = "Regenerates the server data for Scenario 6 into the designated storage directory")
	private boolean regenerateScenario6 = false;
	@CommandLine.Option(names = {"-s", "--start"}, description = "Start the server with the specified scenario identifier")
	private String scenarioToStart;
	@CommandLine.Option(names = {"-i", "--dau-inventory-xml-path"}, description = "The path of the DAU Inventory")
	private String dauInventoryXmlPath;
	@CommandLine.Option(names = {"-r", "--input-mdlroot-xml-path"}, description = "The path of the input MDL configuration")
	private String inputMdlrooXmlPath;
	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	private boolean helpRequested = false;

	private ImmortalsOdbServerConfiguration() {
	}

	public synchronized static ImmortalsOdbServerConfiguration getInstance() {
		if (instance == null) {
			instance = new ImmortalsOdbServerConfiguration();
		}
		return instance;
	}

	public static void setInstance(ImmortalsOdbServerConfiguration instance) {
		ImmortalsOdbServerConfiguration.instance = instance;
	}

	public String getDauInventoryXmlPath() {
		return dauInventoryXmlPath;
	}

	public ImmortalsOdbServerConfiguration setDauInventoryXmlPath(String dauInventoryXmlPath) {
		this.dauInventoryXmlPath = dauInventoryXmlPath;
		return this;
	}

	public boolean isRegenerateScenario6() {
		return regenerateScenario6;
	}

	public ImmortalsOdbServerConfiguration setRegenerateScenario6(boolean regenerateScenario6) {
		this.regenerateScenario6 = regenerateScenario6;
		return this;
	}

	public String getInputMdlrooXmlPath() {
		return inputMdlrooXmlPath;
	}

	public ImmortalsOdbServerConfiguration setInputMdlrooXmlPath(String inputMdlrooXmltPath) {
		this.inputMdlrooXmlPath = inputMdlrooXmltPath;
		return this;
	}

	public String getScenarioToStart() {
		return scenarioToStart;
	}

	public ImmortalsOdbServerConfiguration setScenarioToStart(String scenarioToStart) {
		this.scenarioToStart = scenarioToStart;
		return this;
	}


	public boolean isHelpRequested() {
		return helpRequested;
	}

	public ImmortalsOdbServerConfiguration setHelpRequested(boolean helpRequested) {
		this.helpRequested = helpRequested;
		return this;
	}

	public void validate() {
		if (isHelpRequested() || (scenarioToStart == null && dauInventoryXmlPath == null && inputMdlrooXmlPath == null && !regenerateScenario5 && !regenerateScenario6)) {
			CommandLine.usage(this, System.out);
			System.exit(1);
		}

		if (scenarioToStart != null) {
			if (!TestScenario.getScenario5TestScenarioIdentifiers().contains(scenarioToStart) &&
					!TestScenario.getScenario6TestScenarioIdentifiers().contains(scenarioToStart)) {
				List<String> scenarioList = TestScenario.getAllTestScenarioIdentifiers();
				String scenarioListString = String.join("\n\t", scenarioList);
				System.err.println("Invalid scenario identifier '" + scenarioToStart + "'.\nValid Identifiers:\n\t" + scenarioListString);
				System.exit(-1);
			}
		}
	}

	public boolean isRegenerateScenario5() {
		return regenerateScenario5;
	}

	public ImmortalsOdbServerConfiguration setRegenerateScenario5(boolean regenerateScenario5) {
		this.regenerateScenario5 = regenerateScenario5;
		return this;
	}
}
