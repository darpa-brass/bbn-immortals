package mil.darpa.immortals.flitcons;

import ch.qos.logback.classic.util.ContextInitializer;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainerFactory;
import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.OrientVertexDataSource;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.mdl.XmlElementDataSource;
import mil.darpa.immortals.flitcons.mdl.validation.PortMapping;
import mil.darpa.immortals.flitcons.mdl.validation.PortMappingValidator;
import mil.darpa.immortals.orientdbserver.OdbEmbeddedServer;
import mil.darpa.immortals.orientdbserver.TestScenario;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public class DevhelperMain {


	@CommandLine.Option(names = "--input-rules-xls", description = "The input Excel file to use for the rules. Otherwise, the rules within the resources directory will be used.")
	private File inputXls = new File("/home/awellman/Documents/workspaces/immortals/primary/git/immortals/docs/CP/phase3/cp_05/rules/CombinedValidationRules.xls");

	@CommandLine.Option(names = "--output-rules-drl", description = "The location where the input rules should be written out to if they were provided")
	private File outputDrl = null;

	@CommandLine.Option(names = {"--challenge-problems-root"}, description = "If provided, validation will be done on the specified SwRI 'challenge-problems' repository root directory.")
	private Path challengeProblemsRoot = null;

	@CommandLine.Option(names = {"-d", "--debug"}, description = "Displays additional debug information")
	private boolean debugMode = false;

	@CommandLine.Option(names = {"-i", "--dau-inventory"})
	private File dauInventory = globalDauInventory;

	@CommandLine.Option(names = {"-m", "--mdl-root"})
	private File mdlRoot = globalMdlRoot;

	private static File globalMdlRoot =
//			null;
			new File(
//					"/home/awellman/Documents/workspaces/immortals/primary/git/swri/challenge-problems/Scenarios/FlightTesting/Scenario_5//BRASS_Scenario5_BeforeAdaptation.xml"
//					"/home/awellman/Documents/workspaces/immortals/primary/git/swri/challenge-problems/Scenarios/FlightTesting/Scenario_5/Examples/Example_1/BRASS_Scenario5_Example1_TTC.xml"
//					"/home/awellman/Documents/workspaces/immortals/primary/git/swri/challenge-problems/Scenarios/FlightTesting/Scenario_5/Examples/Example_2/BRASS_Scenario5_Example2_TTC.xml"
//					"/home/awellman/Documents/workspaces/immortals/primary/git/swri/challenge-problems/Scenarios/FlightTesting/Scenario_5/Examples/Example_3/BRASS_Scenario5_Example3_Acra.xml"
//					"/home/awellman/Documents/workspaces/immortals/primary/git/swri/challenge-problems/Scenarios/FlightTesting/Scenario_5/Examples/Example_4/BRASS_Scenario5_Example4_TTC.xml"
//					"/home/awellman/Documents/workspaces/immortals/primary/git/swri/challenge-problems/Scenarios/FlightTesting/Scenario_5/Examples/Example_5/BRASS_Scenario5_Example5_TTC.xml"
					"/home/awellman/Documents/workspaces/immortals/primary/git/swri/challenge-problems/Scenarios/FlightTesting/Scenario_5/Examples/Example_6/BRASS_Scenario5_Example6_TTC.xml"
//					"/home/awellman/Documents/workspaces/immortals/primary/git/immortals/shared/tools/odb/resources/dummy_data/s5_input_mdlRoot.xml"
			);

	private static File globalDauInventory =
//			null;
			new File(
					"/home/awellman/Documents/workspaces/immortals/primary/git/immortals/shared/tools/odb/resources/dummy_data/s5_dauInventory.xml"
//					"/home/awellman/Documents/workspaces/immortals/primary/git/swri/challenge-problems/Scenarios/FlightTesting/Scenario_5/Examples/Inventory_1/BRASS_Scenario5_Inventory1.xml"
			);

	private static final String dslPath = "/home/awellman/Documents/workspaces/immortals/primary/git/immortals/dsl/resource-dsl/";
	private static final String odbTarget =  "remote:127.0.0.1:2424/IMMORTALS_s5";
//	private static final String odbTarget =  "remote:127.0.0.1:2424/IMMORTALS_tmp";

	public static void main(String[] args) {
		try {
			System.setProperty(EnvironmentConfiguration.ODB_TARGET.javaArg, odbTarget);
			System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback-validator.xml");
			DevhelperMain m = new DevhelperMain();

			CommandLine.populateCommand(m, args);

//		m.validate(new XmlElementDataSource(globalMdlRoot));
//		m.validate(new XmlElementDataSource(globalDauInventory));


			AbstractDataTarget<OrientVertex> ds = new OrientVertexDataSource(odbTarget);

			PortMappingValidator validator = new PortMappingValidator(ds.getPortMappingDetails());
			validator.validateInitialData();

			m.validate(ds);

			SolverConfiguration sc = SolverConfiguration.getInstance().setDslPath(dslPath).setStopOnFinish(true);

			SimpleSolver ss = new SimpleSolver().loadData(ds);
			DynamicObjectContainer solution = ss.solve();
			new SolutionInjector(ds, solution).injectSolution();

			ds.restart();

			validator.validateInitialData();
			validator.validateResultData(ds.getPortMappingDetails());

//		OdbEmbeddedServer oes = new OdbEmbeddedServer(TestScenario.)

//		m.execute();
		} catch (NestedPathException e) {
			throw new RuntimeException(e);
		}
	}

	public void validate(@Nonnull AbstractDataSource ds) {
		if (ds.isInputConfiguration()) {
			MdlDataValidator validator = new MdlDataValidator(inputXls, null, ds);
			validator.validateConfiguration(ValidationScenario.InputConfigurationUsage);
			validator.validateConfiguration(ValidationScenario.InputConfigurationRequirements);
		}
		if (ds.isDauInventory()) {
			MdlDataValidator validator2 = new MdlDataValidator(inputXls, null, ds);
			validator2.validateConfiguration(ValidationScenario.DauInventory);
		}
	}

	private void execute() {
		if (!dauInventory.exists() || !mdlRoot.exists()) {
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

		if (!dauInventory.exists()) {
			dauInventory = challengeProblemsRoot.resolve(dauInventory.toPath()).toFile();
			if (!dauInventory.exists()) {
				throw new RuntimeException("Could not resolve dauInventory at '" + dauInventory.toString() + "'!");
			}
		}

		if (!mdlRoot.exists()) {
			mdlRoot = challengeProblemsRoot.resolve(mdlRoot.toPath()).toFile();
			if (!mdlRoot.exists()) {
				throw new RuntimeException("Could not resolve mdlRoot at '" + mdlRoot.toString() + "'!");
			}
		}

		try {
			File[] inputFiles = new File[]{
					dauInventory,
					mdlRoot
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
