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
import java.io.File;

public class DevhelperMain {

	public static class DevhelperConfiguration extends ValidatorConfiguration {
		protected DevhelperConfiguration() {
			super();
			if (this.getInputXls() == null) {
				inputXls = "${IMMORTALS_ROOT}/docs/CP/phase3/cp_05/rules/CombinedValidationRules.xls";
			}
			scenarioCandidate = "se2";
		}
	}

	private PortMappingValidator portMappingValidator;
	private final DevhelperConfiguration config;
	private final File inputXls;
	private final File outputDrl;

	private DevhelperMain(@Nonnull DevhelperConfiguration config) {
		this.config = config;
		this.inputXls = config.getInputXls();
		this.outputDrl = config.getOutputDrl();
	}

	public static void main(String[] args) {
		try {
			DevhelperConfiguration config = new DevhelperConfiguration();
			CommandLine.populateCommand(config, args);
			DevhelperMain m = new DevhelperMain(config);
			m.execute();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void validate(@Nonnull AbstractDataSource ds) {
		if (ds.isInputConfiguration()) {
			MdlDataValidator validator = new MdlDataValidator(inputXls, outputDrl, ds);
			validator.validateConfiguration(ValidationScenario.InputConfigurationUsage, true, false);
			validator.validateConfiguration(ValidationScenario.InputConfigurationRequirements, true, false);
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
			MdlDataValidator validator2 = new MdlDataValidator(inputXls, outputDrl, ds);
			validator2.validateConfiguration(ValidationScenario.DauInventory, true, false);
		}
	}

	private void execute() throws Exception {
		if (config.helpRequested) {
			CommandLine.usage(config, System.out);
			System.exit(0);
		}

		if (config.scenarioCandidate == null) {
			System.err.println("No Scenario selected!\n\n");
			CommandLine.usage(config, System.err);
			System.exit(1);
		}

		ValidatorConfiguration.Scenario scenario = ValidatorConfiguration.Scenario.valueOf(config.scenarioCandidate);
		File xmlInputFile = scenario.hasXmlInput() ? scenario.getXmlInput() : null;
		File xmlInventoryFile = scenario.hasXmlInventory() ? scenario.getXmlInventory() : null;
		String odbTarget = scenario.hasOdbTarget() ? scenario.getOdbTarget() : null;

		if (odbTarget != null) {
			System.setProperty(EnvironmentConfiguration.ODB_TARGET.javaArg, odbTarget);
		}

		if (xmlInputFile == null && xmlInventoryFile == null) {
			if (odbTarget == null) {
				throw new RuntimeException("No input provided!");
			}
			OrientVertexDataSource ds = new OrientVertexDataSource(odbTarget);
			validate(ds);

			SolverConfiguration.getInstance().setStopOnFinish(true);
			SimpleSolver ss = new SimpleSolver().loadData(ds);
			DynamicObjectContainer solution = ss.solve();
			new SolutionInjector(ds, solution).injectSolution();
			ds.restart();
			validate(ds);
		} else {
			if (xmlInputFile != null) {
				XmlElementDataSource ds = new XmlElementDataSource(xmlInputFile);
				validate(ds);
			}
			if (xmlInventoryFile != null) {
				XmlElementDataSource ds = new XmlElementDataSource(xmlInventoryFile);
				validate(ds);
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
