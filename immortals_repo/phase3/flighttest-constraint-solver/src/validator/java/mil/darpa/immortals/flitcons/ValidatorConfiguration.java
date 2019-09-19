package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.orientdbserver.OdbEmbeddedServer;
import mil.darpa.immortals.orientdbserver.TestScenario;
import mil.darpa.immortals.orientdbserver.TestScenarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidatorConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(ValidatorConfiguration.class);

	public static class ScenarioCandidates extends ArrayList<String> {
		ScenarioCandidates() {
			super(TestScenarios.getAllScenario5TestScenarioIdentifiers());
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

	// General options

	@CommandLine.Option(names = "--input-rules-xls", description = "The input Excel file to use for the rules. Otherwise, the rules within the resources directory will be used.")
	protected String inputXls = null;

	@CommandLine.Option(names = "--output-rules-drl", description = "The location where the input rules should be written out to if they were provided")
	private String outputDrl = null;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	public boolean helpRequested = false;

	@CommandLine.Option(names = {"--halt-on-failure", "-k"}, description = "Halts validation if a validation failure is encountered.")
	public boolean haltOnFailure = false;

	@CommandLine.Option(names = {"--verbose", "-v"}, description = "Displays data for All validations even if they pass.")
	public boolean verbose = false;

	@CommandLine.Option(names = {"--full-validation", "-f"}, description = "If true, validation of inventory requirements and MDL usage will be done on the input MDLRoot")
	public boolean fullValidation = false;

	@CommandLine.Option(names = {"--light-validation", "-l"}, description = "Only performs basic validation such as port mapping consistency and doesn't validate the contents of the DAUs, Measurements, DataStreams, and Devices.")
	public boolean lightValidation = false;

	@CommandLine.Option(names = {"--output-path", "-O"}, description = "The file to store the results in")
	public String outputPath = null;

	@CommandLine.Option(names = {"--inventory-requirements", "-i"}, description = "Validates the values necessary to define inventory requirements.")
	public boolean validateInventoryRequirements = false;

	@CommandLine.Option(names = {"--mdlroot-requirements", "-m"}, description = "Validates the values necessary to define MDLRoot requirements.")
	public boolean validateMdlrootRequirements = false;

	@CommandLine.Option(names = {"--mdlroot-usage", "-u"}, description = "Validates the DAUs chosen match the MDLRoot requirements.")
	public boolean validateMdlrootUsage = false;


	// Predefined scenario validation options

	@CommandLine.Option(names = {"--validate-all", "-a"}, description = "Validates every known scenario.")
	private boolean validateAll = false;

	@CommandLine.Option(names = {"--validate-bbn", "-b"}, description = "Validates all known BBN curated examples.")
	private boolean validateBbn = false;

	@CommandLine.Option(names = {"--validate-swri", "-s"}, description = "Validates all known SwRI curated examples.")
	private boolean validateSwri = false;

	@CommandLine.Option(names = {"--validate-scenarios-from-file"}, description = "Validates the scenarios from the file")
	private File validationSource = null;

	@CommandLine.Option(names = {"--scenario"}, completionCandidates = ScenarioCandidates.class, description = "Known scenarios that can be executed. Valid values: [${COMPLETION-CANDIDATES}]")
	private List<String> scenarios = null;


	// XML-only options

	@CommandLine.Parameters(arity = "0..*", description = "XML Files to Validate")
	private List<File> inputFiles;


	// ODB-only options

	@CommandLine.Option(names = {"--odb-target"}, description = "The ODB target to validate")
	private String odbTarget = null;

	@CommandLine.Option(names = {"--use-odb"}, description = "Use ODB with backups")
	private boolean useOdbOptimized = false;

	@CommandLine.Option(names = {"--use-odb-with-selective-xml"}, description = "Use ODB, using XML files if the files have changed")
	private boolean useOdbSelectiveXml = false;

	@CommandLine.Option(names = {"--use-odb-with-xml"}, description = "Use ODB strictly with XML files")
	private boolean useOdbExclusivelyXml = false;

	@CommandLine.Option(names = {"--compare-xml-odb-all"}, description = "Loads the XML and OrientDB data for all known scenarios and compares the otuput")
	public boolean compareXMlToOdbAll = false;


	public String getOdbTarget() {
		return odbTarget;
	}

	public OdbEmbeddedServer.OdbDeploymentMode getDeploymentMode() {
		if (useOdbOptimized) {
			return OdbEmbeddedServer.OdbDeploymentMode.BackupsWithUpdatedXmlIfAvailable;
		} else if (useOdbSelectiveXml) {
			return OdbEmbeddedServer.OdbDeploymentMode.BackupsWithUpdatedXml;
		} else if (useOdbExclusivelyXml) {
			return OdbEmbeddedServer.OdbDeploymentMode.XmlOnly;
		}
		return null;
	}

	public List<File> getXmlFilesToValidate() {
		Set<File> xmlFilesToValidate = new HashSet<>();

		if (inputFiles != null) {
			xmlFilesToValidate.addAll(inputFiles);
		}

		List<TestScenario> testScenarios = getScenariosToValidate();
		for (TestScenario scenario : testScenarios) {
			if (scenario.hasXmlMdlrootInput()) {
				xmlFilesToValidate.add(scenario.getXmlMdlrootInputPath());
			}
			if (scenario.hasXmlInventoryInput()) {
				xmlFilesToValidate.add(scenario.getXmlInventoryPath());
			}
		}

		return new ArrayList<>(xmlFilesToValidate);
	}

	public List<TestScenario> getScenariosToValidate() {
		Set<TestScenario> testScenarioSet = new HashSet<>();

		if (validateAll) {
			testScenarioSet.addAll(TestScenarios.filterScenarios(TestScenario::isScenario5));
		} else {
			if (validateBbn) {
				testScenarioSet.addAll(TestScenarios.filterScenarios(TestScenario::isScenario5, TestScenario::isBbn));
			}
			if (validateSwri) {
				testScenarioSet.addAll(TestScenarios.filterScenarios(TestScenario::isScenario5, TestScenario::isSwri));
			}
		}

		if (scenarios != null) {
			testScenarioSet.addAll(scenarios.stream().map(TestScenarios::getTestScenario).collect(Collectors.toList()));
		}

		if (validationSource != null) {
			TestScenarios testScenarios = new TestScenarios(validationSource);
			testScenarioSet.addAll(testScenarios.scenarios);
		}
		return new ArrayList<>(testScenarioSet);
	}

	public void validateParams() {
		if (helpRequested || (inputFiles == null && scenarios == null && validationSource == null &&
				odbTarget == null && !validateAll && !validateBbn && !validateSwri && !compareXMlToOdbAll)) {
			CommandLine.usage(this, System.out);
			System.exit(-1);
		}

		if (useOdbOptimized || useOdbExclusivelyXml || useOdbSelectiveXml) {
			if (useOdbOptimized && useOdbExclusivelyXml || useOdbOptimized && useOdbSelectiveXml || useOdbExclusivelyXml && useOdbSelectiveXml) {
				logger.error("Only one of '--use-odb', '--use-odb-with-xml', and 'use-odb-with-selective-xml' can be used!");
				System.exit(-1);
			}
			if (odbTarget != null) {
				logger.error("cannot use '--odb-target' with odb server parameters!");
				System.exit(-1);
			}
			if (inputFiles != null) {
				logger.error("The '--use-odb' parameter cannot be used with XML file validation!");
				System.exit(-1);
			}
		}
	}
}
