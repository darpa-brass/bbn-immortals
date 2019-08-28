package mil.darpa.immortals.orientdbserver;

import picocli.CommandLine;

import java.util.List;

public class ImmortalsOdbServerConfiguration {
	private static ImmortalsOdbServerConfiguration instance;

	@CommandLine.Option(names = "--regen-scenario5-bbn",
			description = "Regenerates the server data for BBN authored Scenario 5  examples into the designated storage directory")
	private boolean regenerateScenario5bbn = false;
	@CommandLine.Option(names = "--regen-scenario5-swri",
			description = "Regenerates the server data for SwRI authrored Scenario 5  examples into the designated storage directory")
	private boolean regenerateScenario5swri = false;
	@CommandLine.Option(names = "--regen-scenario6",
			description = "Regenerates the server data for Scenario 6 into the designated storage directory")
	private boolean regenerateScenario6 = false;
	@CommandLine.Option(names = {"--regen-scenario"},
			description = "Regenerates the specified scenario")
	private String scenarioToRegenerate = null;
	@CommandLine.Option(names = {"-s", "--start"}, description = "Start the server with the specified scenario identifier")
	private String scenarioToStart;
	@CommandLine.Option(names = {"-i", "--dau-inventory-xml-path"}, description = "The path of the DAU Inventory")
	private String dauInventoryXmlPath;
	@CommandLine.Option(names = {"-r", "--input-mdlroot-xml-path"}, description = "The path of the input MDL configuration")
	private String inputMdlrooXmlPath;
	@CommandLine.Option(names = {"--keep-running", "-k"}, description = "If true and provided with a regen option, the server will continue running after generating the new scenario.")
	public boolean keepRunning = false;
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
		if (isHelpRequested() || (scenarioToStart == null &&
				dauInventoryXmlPath == null && inputMdlrooXmlPath == null && scenarioToRegenerate == null &&
				!regenerateScenario5bbn && !regenerateScenario5swri && !regenerateScenario6)) {
			CommandLine.usage(this, System.out);
			System.exit(1);
		}

		if (scenarioToStart != null) {
			if (!TestScenario.getAllScenario5TestScenarioIdentifiers().contains(scenarioToStart) &&
					!TestScenario.getScenario6TestScenarioIdentifiers().contains(scenarioToStart)) {
				List<String> scenarioList = TestScenario.getAllTestScenarioIdentifiers();
				String scenarioListString = String.join("\n\t", scenarioList);
				System.err.println("Invalid scenario identifier '" + scenarioToStart + "'.\nValid Identifiers:\n\t" + scenarioListString);
				System.exit(-1);
			}
		}
	}

	public String getScenarioToRegenerate() {
		return scenarioToRegenerate;
	}

	public boolean isRegenerateScenario5bbn() {
		return regenerateScenario5bbn;
	}

	public boolean isRegenerateScenario5swri() {
		return regenerateScenario5swri;
	}

	public ImmortalsOdbServerConfiguration setRegenerateScenario5bbn(boolean regenerateScenario5bbn) {
		this.regenerateScenario5bbn = regenerateScenario5bbn;
		return this;
	}

	public ImmortalsOdbServerConfiguration setRegenerateScenario5swri(boolean regenerateScenario5swri) {
		this.regenerateScenario5swri = regenerateScenario5swri;
		return this;
	}
}
