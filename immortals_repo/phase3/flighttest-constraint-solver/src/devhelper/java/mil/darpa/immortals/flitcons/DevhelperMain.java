package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.OrientVertexDataSource;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.mdl.XmlElementDataSource;
import mil.darpa.immortals.flitcons.mdl.validation.PortMappingValidator;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DevhelperMain {

	protected static enum Scenario {
		bs5("shared/tools/odb/resources/dummy_data/s5_input_mdlRoot.xml", null,
				null, null,
				null),
		sb(null, null,
				"Scenarios/FlightTesting/Scenario_5//BRASS_Scenario5_BeforeAdaptation.xml", null,
				null),
		se1(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_1/BRASS_Scenario5_Example1_TTC.xml", null,
				null),
		se2(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_2/BRASS_Scenario5_Example2_TTC.xml", null,
				null),
		se3(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_3/BRASS_Scenario5_Example3_Acra.xml", null,
				null),
		se4(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_4/BRASS_Scenario5_Example4_TTC.xml", null,
				null),
		se5(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_5/BRASS_Scenario5_Example5_TTC.xml", null,
				null),
		se6(null, null,
				"Scenarios/FlightTesting/Scenario_5/Examples/Example_6/BRASS_Scenario5_Example6_TTC.xml", null,
				null),
		odb(null, null,
				null, null,
				"remote:127.0.0.1:2424/IMMORTALS_s5");

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

	private static class ScenarioCandidates extends ArrayList<String> {
		ScenarioCandidates() {
			super(Arrays.stream(Scenario.values()).map(Scenario::name).collect(Collectors.toList()));
		}
	}

	@CommandLine.Option(names = "--input-rules-xls", description = "The input Excel file to use for the rules. Otherwise, the rules within the resources directory will be used.")
	private String inputRulesXls = "${IMMORTALS_ROOT}/docs/CP/phase3/cp_05/rules/CombinedValidationRules.xls";


	@CommandLine.Option(names = "--output-rules-drl", description = "The location where the input rules should be written out to if they were provided")
	private String outputRulesDrl = null;

	@CommandLine.Option(names = {"-s", "--scenario"}, completionCandidates = ScenarioCandidates.class, description = "Known scenarios that can be executed. Valid values: [${COMPLETION-CANDIDATES}]")
	private String scenarioCandidate;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
	private boolean help;

	private PortMappingValidator portMappingValidator;

	public static void main(String[] args) {
		try {
			DevhelperMain m = new DevhelperMain();
			CommandLine.populateCommand(m, args);
			m.execute();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void validate(@Nonnull AbstractDataSource ds, @Nullable File inputRulesXls, @Nullable File outputRulesDrl) {
		if (ds.isInputConfiguration()) {
			MdlDataValidator validator = new MdlDataValidator(inputRulesXls, outputRulesDrl, ds);
			validator.validateConfiguration(ValidationScenario.InputConfigurationUsage);
			validator.validateConfiguration(ValidationScenario.InputConfigurationRequirements);
			if (ds instanceof OrientVertexDataSource) {
				OrientVertexDataSource ovds = (OrientVertexDataSource) ds;
				if (portMappingValidator == null) {
					portMappingValidator = new PortMappingValidator(ovds.getPortMappingDetails());
					portMappingValidator.validateInitialData();
				} else {
					portMappingValidator.validateResultData(ovds.getPortMappingDetails());

				}
			}
		}
		if (ds.isDauInventory()) {
			MdlDataValidator validator2 = new MdlDataValidator(inputRulesXls, outputRulesDrl, ds);
			validator2.validateConfiguration(ValidationScenario.DauInventory);
		}
	}

	private void execute() throws Exception {
		if (help) {
			CommandLine.usage(this, System.out);
			System.exit(0);
		}

		File inputRulesFile = null;
		File outputRulesFile = null;

		if (inputRulesXls != null) {
			inputRulesFile = new File(inputRulesXls.replace("${IMMORTALS_ROOT}", EnvironmentConfiguration.getImmortalsRoot().toString())).getAbsoluteFile();
		}

		if (outputRulesDrl != null) {
			outputRulesFile = new File(outputRulesDrl.replace("${IMMORTALS_ROOT}", EnvironmentConfiguration.getImmortalsRoot().toString())).getAbsoluteFile();
		}

		if (scenarioCandidate == null) {
			System.err.println("No Scenario selected!\n\n");
			CommandLine.usage(this, System.err);
			System.exit(1);
		}

		Scenario scenario = Scenario.valueOf(scenarioCandidate);
		File xmlInputFile = scenario.hasXmlInput() ? scenario.getXmlInput() : null;
		File xmlInventoryFile = scenario.hasXmlInventory() ? scenario.getXmlInventory() : null;
		String odbTarget = scenario.hasOdbTarget() ? scenario.getOdbTarget() : null;

		if (odbTarget != null) {
			System.setProperty(EnvironmentConfiguration.ODB_TARGET.javaArg, odbTarget);
		}

		if (xmlInputFile == null && xmlInventoryFile == null) {
			OrientVertexDataSource ds = new OrientVertexDataSource(odbTarget);
			validate(ds, inputRulesFile, outputRulesFile);

			SolverConfiguration sc = SolverConfiguration.getInstance().setStopOnFinish(true);
			SimpleSolver ss = new SimpleSolver().loadData(ds);
			DynamicObjectContainer solution = ss.solve();
			new SolutionInjector(ds, solution).injectSolution();
			ds.restart();
			validate(ds, inputRulesFile, outputRulesFile);
		} else {
			if (xmlInputFile != null) {
				XmlElementDataSource ds = new XmlElementDataSource(xmlInputFile);
				validate(ds, inputRulesFile, outputRulesFile);
			}
			if (xmlInventoryFile != null) {
				XmlElementDataSource ds = new XmlElementDataSource(xmlInventoryFile);
				validate(ds, inputRulesFile, outputRulesFile);
			}
		}
	}
//		try {
//			File[] inputFiles = new File[]{
//					dauInventory,
//					mdlRoot
//			};
//			ValidatorConfiguration vc = ValidatorConfiguration.getInstance();
//			vc.inputFiles = inputFiles;
//			vc.inputXls = inputXls;
//			ValidatorMain.execute(vc);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
////			e.printStackTrace(System.err);
//		}
//
//		ArrayList<String> expectedStatusSequence = new ArrayList<>();
//		expectedStatusSequence.add("AdaptationSuccessful");
//
//		TestScenario ts = new TestScenario(
//				"devTest",
//				"Developer Test Scenario",
//				"Scenario5",
//				60000,
//				dauInventory.toString(),
//				mdlRoot.toString(),
//				null,
//				expectedStatusSequence,
//				null
//		);
//
////		OdbEmbeddedServer server = new OdbEmbeddedServer(ts);
////		server.init();
////		server.waitForShutdown();
}
