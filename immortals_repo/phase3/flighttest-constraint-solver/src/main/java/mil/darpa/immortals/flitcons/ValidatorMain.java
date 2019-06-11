package mil.darpa.immortals.flitcons;

import ch.qos.logback.classic.util.ContextInitializer;
import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.mdl.XmlElementDataSource;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static mil.darpa.immortals.schemaevolution.ProvidedData.JARGS_ARTIFACT_DIRECTORY;
import static mil.darpa.immortals.schemaevolution.ProvidedData.JARGS_EVAL_ODB;


public class ValidatorMain {

	@CommandLine.Parameters(arity = "0..*", description = "XML Files to Validate")
	private File[] inputFiles;

	@CommandLine.Option(names = "--input-rules-xls", description = "The input Excel file to use for the rules. Otherwise, the rules within the resources directory will be used.")
	private File inputXls = null;

	@CommandLine.Option(names = "--output-rules-drl", description = "The location where the input rules should be written out to if they were provided")
	private File outputDrl = null;

	@CommandLine.Option(names = {"-C", ValidationDataContainer.COLORLESS_FLAG}, description = "Results in the validation result only displaying invalid values instead of displaying all values as green or red depending on pass or fail")
	private boolean colorlessMode = false;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	private boolean helpRequested = false;

	@CommandLine.Option(names = {"-s", "--challenge-problems-root"}, description = "If provided, validation will be done on the specified SwRI 'challenge-problems' repository root directory.")
	private String challengeProblemsRoot = null;

	@CommandLine.Option(names = {"-d", "--debug"}, description = "Displays additional debug information")
	private boolean debugMode = false;

	public static void main(String[] args) {

		System.setProperty(JARGS_ARTIFACT_DIRECTORY, new File("").getAbsolutePath());
		System.setProperty(JARGS_EVAL_ODB, "UNDEFINED");
		ValidatorMain m = new ValidatorMain();

		CommandLine.populateCommand(m, args);

		if (m.debugMode) {
			System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");
			Logger rootLogger = LogManager.getLogManager().getLogger("");
			for (Handler h : rootLogger.getHandlers()) {
				h.setLevel(Level.ALL);
			}
			rootLogger.setLevel(Level.ALL);

		} else {
			System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback-validator.xml");
		}
		m.execute();
	}

	public String[] challengeProblemFiles = new String[]{
//			"Example_1/BRASS_Scenario5_Example1_TTC.xml",
//			"Example_1/BRASS_Scenario5_Example1_VendorB_After.xml",
//			"Example_1/BRASS_Scenario5_Example1_VendorB_Before.xml",
//			"Example_2/BRASS_Scenario5_Example2_TTC_Stripped.xml",
			"Example_2/BRASS_Scenario5_Example2_TTC_Verbose.xml",
//			"Example_2/BRASS_Scenario5_Example2_TTC.xml",
			"Example_3/BRASS_Scenario5_Example3_Acra.xml",
			"Example_4/BRASS_Scenario5_Example4_TTC.xml",
			"Example_5/BRASS_Scenario5_Example5_TTC.xml",
			"Example_6/BRASS_Scenario5_Example6_TTC.xml",
			"Inventory_1/BRASS_Scenario5_Inventory1.xml"
	};

	private void execute() {
		if (helpRequested) {
			CommandLine.usage(this, System.out);
			return;
		}

		if (inputFiles == null && challengeProblemsRoot == null) {
			System.err.println("Input files or a challenge problems root must be provided!");
			System.exit(-1);
		} else if (inputFiles != null && challengeProblemsRoot != null) {
			System.err.println("Only list of input files or a challenge-problems root can be provided, not both!");
			System.exit(-1);
		}

		if (challengeProblemsRoot != null) {
			Path examplesDirectory = Paths.get(challengeProblemsRoot);
			if (!Files.exists(examplesDirectory)) {
				throw new RuntimeException("specified challenge-problems repository root '" + examplesDirectory + "' does not exist!");
			}
			examplesDirectory = examplesDirectory.resolve("Scenarios").resolve("FlightTesting").resolve("Scenario_5").resolve("Examples");
			if (!Files.exists(examplesDirectory)) {
				throw new RuntimeException("Expected 'Scenarios/FlightTesting/Scenario_5/Examples' structure does not exist in the challenge-problems root '" + challengeProblemsRoot + "'!");
			}
			final Path examplesPath = examplesDirectory;
			List<File> cpFiles = Arrays.stream(challengeProblemFiles).map(x -> examplesPath.resolve(x).toFile()).collect(Collectors.toList());
			inputFiles = cpFiles.toArray(new File[0]);
		}

		for (File xmlFile : inputFiles) {
			try {
				System.out.println("File: " + xmlFile.getAbsolutePath());
				XmlElementDataSource xec = new XmlElementDataSource(xmlFile);
				MdlDataValidator validator = new MdlDataValidator(inputXls, outputDrl, xec);
				validator.setSaveResults(false);

				if (xec.isInputConfiguration()) {
					validator.validateConfiguration(ValidationScenario.InputConfigurationRequirements, !colorlessMode);

				} else if (xec.isDauInventory()) {
					validator.validateConfiguration(ValidationScenario.DauInventory, !colorlessMode);

				} else {
					System.err.println("XML File did not contain a 'DAUInventory' or 'MDLRoot' in the root!");
					System.exit(1);
				}
			} catch (AdaptationnException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
