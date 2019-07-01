package mil.darpa.immortals.flitcons;

import ch.qos.logback.classic.util.ContextInitializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueeException;
import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.OrientVertexDataSource;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.mdl.XmlElementDataSource;
import mil.darpa.immortals.orientdbserver.ImmortalsOdbServerMain;
import mil.darpa.immortals.orientdbserver.OdbEmbeddedServer;
import mil.darpa.immortals.orientdbserver.TestScenario;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DevhelperMain {

	@CommandLine.Option(names = "--input-rules-xls", description = "The input Excel file to use for the rules. Otherwise, the rules within the resources directory will be used.")
	private File inputXls = null;

	@CommandLine.Option(names = "--output-rules-drl", description = "The location where the input rules should be written out to if they were provided")
	private File outputDrl = null;

	@CommandLine.Option(names = {"--challenge-problems-root"}, description = "If provided, validation will be done on the specified SwRI 'challenge-problems' repository root directory.")
	private Path challengeProblemsRoot = null;

	@CommandLine.Option(names = {"-d", "--debug"}, description = "Displays additional debug information")
	private boolean debugMode = false;

	@CommandLine.Option(names = {"-i", "--dau-inventory"})
	private Path dauInventory =
//			null;
			Paths.get(
					"/home/awellman/Documents/workspaces/immortals/primary/git/immortals/shared/tools/odb/resources/dummy_data/s5_dauInventory.xml"
//					"Examples/Inventory_1/BRASS_Scenario5_Inventory1.xml"
			);


	@CommandLine.Option(names = {"-m", "--mdl-root"})
	private Path mdlRoot =
//			null;
			Paths.get(
					"/home/awellman/Documents/workspaces/immortals/primary/git/swri/challenge-problems/Scenarios/FlightTesting/Scenario_5/Examples/Example_1/BRASS_Scenario5_Example1_TTC.xml"
//					"/home/awellman/Documents/workspaces/immortals/primary/git/immortals/shared/tools/odb/resources/dummy_data/s5_input_mdlRoot.xml"
//					"BRASS_Scenario5_BeforeAdaptation.xml"
//								"Examples/Example_1/BRASS_Scenario5_Example1_TTC.xml"
//			"Examples/Example_2/BRASS_Scenario5_Example2_TTC_Stripped.xml"
//					"Examples/Example_2/BRASS_Scenario5_Example2_TTC_Verbose.xml"
//			"Examples/Example_2/BRASS_Scenario5_Example2_TTC.xml"
//			"Examples/Example_3/BRASS_Scenario5_Example3_Acra.xml"
//			"Examples/Example_4/BRASS_Scenario5_Example4_TTC.xml"
//			"Examples/Example_5/BRASS_Scenario5_Example5_TTC.xml"
//			"Examples/Example_6/BRASS_Scenario5_Example6_TTC.xml"
			);

	public static void main(String[] args) {
		System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback-validator.xml");

		DevhelperMain m = new DevhelperMain();
		CommandLine.populateCommand(m, args);
		m.validateOdb();
//		m.validateXml();
//		m.execute();

	}

	public void validateXml() {
		XmlElementDataSource ds = new XmlElementDataSource(mdlRoot.toFile());
		MdlDataValidator validator = new MdlDataValidator(null, null, ds);
		validator.validateConfiguration(ValidationScenario.InputConfigurationRequirements, true);


//		File[] inputFiles = new File[]{
//				dauInventory.toFile(),
//				mdlRoot.toFile()
//		};
//		ValidatorConfiguration vc = ValidatorConfiguration.getInstance();
//		vc.inputFiles = inputFiles;
//		vc.inputXls = inputXls;
//		vc.debugMode = true;
//		ValidatorMain.execute(vc);
	}

	public void validateOdb() {
//		System.setProperty(EnvironmentConfiguration.ODB_TARGET.javaArg, "remote:127.0.0.1:2424/IMMORTALS_s5");
//		OrientVertexDataSource ds = new OrientVertexDataSource();
		XmlElementDataSource ds = new XmlElementDataSource(mdlRoot.toFile());

		MdlDataValidator validator = new MdlDataValidator(null, null, ds);
		validator.validateConfiguration(ValidationScenario.InputConfigurationUsage, true);
		validator.validateConfiguration(ValidationScenario.InputConfigurationRequirements, true);

		XmlElementDataSource ds2 = new XmlElementDataSource(dauInventory.toFile());
		MdlDataValidator validator2 = new MdlDataValidator(null, null, ds2);
		validator2.validateConfiguration(ValidationScenario.DauInventory, true);

		try {
			DynamicObjectContainer doc = Utils.createDslInterchangeFormat(ds2.getTransformedDauInventory(true));
			Utils.difGson.toJson(doc, new FileWriter(new File("TestFile.json")));
		} catch (DynamicValueeException | IOException e) {
			throw new RuntimeException(e);
		}


	}

	public void validateXmlAndOdb() {
		validateXml();
		validateOdb();
	}

	private void execute() {

		if (!Files.exists(dauInventory) || !Files.exists(mdlRoot)) {
			if (challengeProblemsRoot == null) {
				throw new RuntimeException("Please use absolute filepaths or provide a challenge-problems-root for them!");
			}
		}


		if (challengeProblemsRoot == null) {
			System.err.println("challengeProblemsRoot is required!");
			System.exit(-1);
		}

		if (!Files.exists(challengeProblemsRoot)) {
			throw new RuntimeException("specified challenge-problems repository root '" + challengeProblemsRoot + "' does not exist!");
		}
		challengeProblemsRoot = challengeProblemsRoot.resolve("Scenarios").resolve("FlightTesting").resolve("Scenario_5");
		if (!Files.exists(challengeProblemsRoot)) {
			throw new RuntimeException("Expected 'Scenarios/FlightTesting/Scenario_5/Examples' structure does not exist in the challenge-problems root '" + challengeProblemsRoot + "'!");
		}

		if (!Files.exists(dauInventory)) {
			dauInventory = challengeProblemsRoot.resolve(dauInventory);
			if (!Files.exists(dauInventory)) {
				throw new RuntimeException("Could not resolve dauInventory at '" + dauInventory.toString() + "'!");
			}
		}

		if (!Files.exists(mdlRoot)) {
			mdlRoot = challengeProblemsRoot.resolve(mdlRoot);
			if (!Files.exists(mdlRoot)) {
				throw new RuntimeException("Could not resolve mdlRoot at '" + mdlRoot.toString() + "'!");
			}
		}

		try {
			File[] inputFiles = new File[]{
					dauInventory.toFile(),
					mdlRoot.toFile()
			};
			ValidatorConfiguration vc = ValidatorConfiguration.getInstance();
			vc.inputFiles = inputFiles;
			vc.inputXls = inputXls;
			vc.debugMode = true;
			ValidatorMain.execute(vc);
		} catch (Exception e) {
			throw new RuntimeException(e);
//			e.printStackTrace(System.err);
		}

		ArrayList<String> expectedStatusSequence = new ArrayList<>();
		expectedStatusSequence.add("AdaptationSuccessful");

		TestScenario ts = new TestScenario(
				"devTest",
				"Developer Test Scenario",
				"Scenario5",
				60000,
				dauInventory.toString(),
				mdlRoot.toString(),
				null,
				expectedStatusSequence,
				null
				);

//		OdbEmbeddedServer server = new OdbEmbeddedServer(ts);
//		server.init();
//		server.waitForShutdown();
	}
}
