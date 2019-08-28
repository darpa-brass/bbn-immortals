package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.EnvironmentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ValidatorConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(ValidatorConfiguration.class);


	public enum Scenario {
		bs5("shared/tools/odb/resources/dummy_data/s5_input_mdlRoot.xml", null,
				null, null,
				null),
		bs5si(null, "shared/tools/odb/resources/dummy_data/s5s_dauInventory.xml",
				null, null,
				null),
		bs5i(null, "shared/tools/odb/resources/dummy_data/s5_dauInventory.xml",
				null, null,
				null),
		s5e1(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_1/BRASS_Scenario5_Example1_TTC.xml", null,
				null),
		s5e2(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_2/BRASS_Scenario5_Example2_TTC.xml", null,
				null),
		s5e3(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_3/BRASS_Scenario5_Example3_Acra.xml", null,
				null),
		s5e4(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_4/BRASS_Scenario5_Example4_TTC.xml", null,
				null),
		s5e5(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_5/BRASS_Scenario5_Example5_TTC.xml", null,
				null),
		s5e6(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_6/BRASS_Scenario5_Example6_TTC.xml", null,
				null),
		s5i1(null, null,
				null, "Scenarios/FlightTesting/Scenario_5/Examples/Inventory_1/BRASS_Scenario5_Inventory1.xml",
				null);

		private final String immortalsInputXmlPath;
		private final String immortalsInventoryXmlPath;
		private final String challengeProblemsInputXmlPath;
		private final String challengeProblemsInventoryXmlPath;
		private final String odbTarget;

		Scenario(@Nullable String immortalsInputXmlPath, @Nullable String immortalsInventoryXmlPath,
		         @Nullable String challengeProblemsInputXmlPath, @Nullable String challengeProblemsInventoryXmlPath,
		         @Nullable String odbTarget) {
			this.immortalsInputXmlPath = immortalsInputXmlPath;
			this.immortalsInventoryXmlPath = immortalsInventoryXmlPath;
			this.challengeProblemsInputXmlPath = challengeProblemsInputXmlPath;
			this.challengeProblemsInventoryXmlPath = challengeProblemsInventoryXmlPath;
			this.odbTarget = odbTarget;
		}

		public boolean isSwri() {
			return challengeProblemsInputXmlPath != null || challengeProblemsInventoryXmlPath != null;
		}

		public boolean isBbn() {
			return immortalsInputXmlPath != null || immortalsInventoryXmlPath != null;

		}

		public File getXmlInput() {
			if (immortalsInputXmlPath != null) {
				return EnvironmentConfiguration.getImmortalsRoot().resolve(immortalsInputXmlPath).toFile();
			}
			if (challengeProblemsInputXmlPath != null) {
				return EnvironmentConfiguration.getChallengeProblemsRoot().resolve(challengeProblemsInputXmlPath).toFile();
			}
			throw new RuntimeException("No XML Input value set for scenario '" + name() + "'!");
		}

		public File getXmlInventory() {
			if (immortalsInventoryXmlPath != null) {
				return EnvironmentConfiguration.getImmortalsRoot().resolve(immortalsInventoryXmlPath).toFile();
			}
			if (challengeProblemsInventoryXmlPath != null) {
				return EnvironmentConfiguration.getChallengeProblemsRoot().resolve(challengeProblemsInventoryXmlPath).toFile();
			}
			throw new RuntimeException("No XML Inventory value set for scenario '" + name() + "'!");
		}

		public String getOdbTarget() {
			if (odbTarget == null) {
				throw new RuntimeException("Scenario '" + name() + "' does not have an odb target!");
			}
			return odbTarget;
		}

		public boolean isOdbTarget() {
			return odbTarget != null;
		}

		public boolean hasXmlInventory() {
			return immortalsInventoryXmlPath != null || challengeProblemsInventoryXmlPath != null;
		}

		public boolean hasXmlInput() {
			return immortalsInputXmlPath != null || challengeProblemsInputXmlPath != null;
		}

		public boolean hasOdbTarget() {
			return odbTarget != null;
		}
	}

	public static class ScenarioCandidates extends ArrayList<String> {
		ScenarioCandidates() {
			super(Arrays.stream(Scenario.values()).map(Scenario::name).collect(Collectors.toList()));
		}
	}

	private static ValidatorConfiguration instance;

	public synchronized static ValidatorConfiguration getInstance() {
		if (instance == null) {
			instance = new ValidatorConfiguration();
		}
		return instance;
	}

	protected ValidatorConfiguration() {

	}

	public File getInputXls() {
		if (inputXls != null) {
			return new File(inputXls.replace("${IMMORTALS_ROOT}", EnvironmentConfiguration.getImmortalsRoot().toString())).getAbsoluteFile();
		} else {
			return null;
		}
	}

	public File getOutputDrl() {
		if (outputDrl != null) {
			return new File(outputDrl.replace("${IMMORTALS_ROOT}", EnvironmentConfiguration.getImmortalsRoot().toString())).getAbsoluteFile();
		} else {
			return null;
		}
	}

	@CommandLine.Parameters(arity = "0..*", description = "XML Files to Validate")
	public File[] inputFiles;

	@CommandLine.Option(names = "--input-rules-xls", description = "The input Excel file to use for the rules. Otherwise, the rules within the resources directory will be used.")
	protected String inputXls = null;

	@CommandLine.Option(names = "--output-rules-drl", description = "The location where the input rules should be written out to if they were provided")
	private String outputDrl = null;

	@CommandLine.Option(names = {"--scenario"}, completionCandidates = ScenarioCandidates.class, description = "Known scenarios that can be executed. Valid values: [${COMPLETION-CANDIDATES}]")
	public String scenarioCandidate;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	public boolean helpRequested = false;

	@CommandLine.Option(names = {"--halt-on-failure", "-f"}, description = "Halts validation if a validation failure is encountered.")
	public boolean haltOnFailure = false;

	@CommandLine.Option(names = {"--validate-all-xml", "-a"}, description = "Validates every known scenario XML file.")
	public boolean validateAllXml = false;

	@CommandLine.Option(names = {"--validate-bbn-xml", "-b"}, description = "Validates all known BBN curated examples.")
	public boolean validateBbnXml = false;

	@CommandLine.Option(names = {"--validate-swri-xml", "-s"}, description = "Validates all known SwRI curated examples.")
	public boolean validateSwriXml = false;


	@CommandLine.Option(names = {"--verbose", "-v"}, description = "Displays data for All validations even if they pass.")
	public boolean verbose = false;

	@CommandLine.Option(names = {"--full-validation", "-m"}, description = "If true, validation of inventory requirements and MDL usage will be done on the input MDLRoot")
	public boolean fullValidation = false;

	@CommandLine.Option(names = {"--light-validation", "-l"}, description = "Only performs basic validation such as port mapping consistency and doesn't validate the contents of the DAUs, Measurements, DataStreams, and Devices.")
	public boolean lightValidation = false;

	public void validateParams() {
		if (inputFiles == null && scenarioCandidate == null && !validateAllXml && !validateBbnXml && !validateSwriXml) {
			logger.error("Please provide '--input-files', '--scenario', '--validate-bbn-xml', '--validate-swri-xml', or '--validate-all-xml' values!");
			logger.error("Input files or a scenario candidate must be provided!");
			System.exit(-1);
		} else {
			String msg = "Only one of the parameters '--input-files', '--scenario', and '--validate-all-xml' can be used!";
			if ((inputFiles != null && (scenarioCandidate != null || validateAllXml)) ||
					(scenarioCandidate != null && validateAllXml)) {
				logger.error(msg);
				System.exit(-1);
			}
		}

		if ((validateAllXml && (validateBbnXml || validateSwriXml)) ||
				(validateBbnXml && validateSwriXml)) {
			logger.error("Only one of the parameters '--validate-all-xml', '--validate-bbn-xml', and '--validate-swri-xml' can be used!");
			System.exit(-1);
		}
	}
}
