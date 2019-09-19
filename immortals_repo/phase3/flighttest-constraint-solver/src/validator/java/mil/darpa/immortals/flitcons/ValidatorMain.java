package mil.darpa.immortals.flitcons;

import com.google.gson.stream.JsonWriter;
import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainerFactory;
import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.OrientVertexDataSource;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.mdl.XmlElementDataSource;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;
import mil.darpa.immortals.orientdbserver.OdbEmbeddedServer;
import mil.darpa.immortals.orientdbserver.TestScenario;
import mil.darpa.immortals.orientdbserver.TestScenarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class ValidatorMain {

	private static final Logger logger = LoggerFactory.getLogger(ValidatorMain.class);

	public static void main(String[] args) {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		ValidatorConfiguration config = ValidatorConfiguration.getInstance();

		CommandLine.populateCommand(config, args);

		if (config.helpRequested) {
			CommandLine.usage(config, System.out);
		} else {
			execute(config);
		}
	}

	public static class ValidationResultContainer {
		public final LinkedList<ValidationResult> results = new LinkedList<>();
	}

	public static class ValidationResult {
		public String scenarioIdentifier;
		public String fileName;
		public String databaseName;
		public String validationPerformed;
		public boolean failure;
		public String details;

		public String toString() {
			return "{\n\t\"scenarioIdentifier\": \"" + scenarioIdentifier + "\"," +
					"\n\t\"fileName\": \"" + fileName + "\"," +
					"\n\t\"databaseName\": \"" + databaseName + "\"," +
					"\n\t\"validationPerformed\": \"" + validationPerformed + "\"," +
					"\n\t\"failure\": \"" + failure + "\"," +
					"\n\t\"details\": \"" + details + "\"," + "\n}";

		}
	}

	private static ValidationResult innerValidateScenario(@Nonnull AbstractDataSource ds,
	                                                      @Nullable String fileName,
	                                                      @Nullable String databaseName,
	                                                      @Nonnull ValidatorConfiguration config,
	                                                      @Nonnull ValidationScenario validationScenario,
	                                                      @Nullable String scenarioTag) {
		ValidationResult result = new ValidationResult();
		result.scenarioIdentifier = null;
		result.fileName = fileName;
		result.databaseName = databaseName;
		result.validationPerformed = validationScenario.name();

		MdlDataValidator validator = new MdlDataValidator(config.getInputXls(), config.getOutputDrl(), ds);

		validator.setSaveResults(!EnvironmentConfiguration.isDefaultArtifactDirectory());
		boolean verbose = config.verbose;

		logger.info(Utils.padCenter("Validating " + validationScenario.title, 80, '-'));
		if (fileName != null) {
			logger.info("Filename: '" + result.fileName.replaceAll(EnvironmentConfiguration.getChallengeProblemsRoot().toString(), ""));
		}

		if (databaseName != null) {
			logger.info("Database: '" + result.databaseName + "'");
		}

		ValidationDataContainer vdc = null;
		try {
			vdc = validator.validateConfiguration(validationScenario, false, config.lightValidation, scenarioTag);
			result.failure = !vdc.isValid();
			result.details = String.join("\n", vdc.makeResultsChart(vdc.name, true));

			// No Exception, so it passed
			if (config.lightValidation) {
				logger.info("Result: PASS");
				logger.info(Utils.padCenter("", 80, '-') + "\n\n");
			}
		} catch (AdaptationnException e) {
			result.failure = true;
			if (e.result == ResultEnum.PerturbationInputInvalid) {
				logger.info("Result: FAIL");
				result.details = e.getMessage();
				logger.info("Details: " + e.getMessage());
				logger.info(Utils.padCenter("", 80, '-') + "\n\n");
			} else {
				throw AdaptationnException.internal(e);
			}
		}

		if (vdc != null && vdc.isValid()) {
			logger.info("Result: PASS");

			if (verbose) {
				logger.info("Details: ");
				vdc.printResults(validationScenario.title);
			}

			logger.info(Utils.padCenter("", 80, '-') + "\n\n");

		} else {
			result.failure = true;
			if (vdc != null) {
				logger.info("Result: FAIL");
				logger.info("Details: ");
				vdc.printResults(validationScenario.title);
				logger.info(Utils.padCenter("", 80, '-') + "\n\n");
			}
		}

		if (result.failure && config.haltOnFailure) {
			System.exit(-1);
		}
		return result;
	}

	private static List<ValidationResult> validateOdbTestScenarios(@Nonnull List<TestScenario> testScenarios, @Nonnull OdbEmbeddedServer server, @Nonnull ValidatorConfiguration config) {
		List<ValidationResult> results = new LinkedList<>();
		for (TestScenario testScenario : testScenarios) {
			String label = testScenario.getShortName();
			String odbPath = server.getOdbPath(testScenario);
			results.addAll(validateOdbTestSession(odbPath, label, config));
		}
		return results;
	}

	private static void compareOdbTestScenariosToXml() {
		OdbEmbeddedServer server = null;
		try {
			List<TestScenario> scenarios = TestScenarios.getAllScenario5TestScenarios();
			server = new OdbEmbeddedServer(scenarios.toArray(new TestScenario[0]));
			server.init(OdbEmbeddedServer.OdbDeploymentMode.BackupsOnly);


			for (TestScenario testScenario : scenarios) {
				String label = testScenario.getShortName();
				String odbPath = server.getOdbPath(testScenario);

				OrientVertexDataSource ovds = new OrientVertexDataSource(odbPath);

				DynamicObjectContainer odbRequestData =
						DynamicObjectContainerFactory.create(ovds.getInterconnectedTransformedFaultyConfiguration(true));
				EnvironmentConfiguration.storeFile(
						label + "-request-odb.json", Utils.difGson.toJson(odbRequestData).getBytes());
				String odbRequestString = odbRequestData.toString(false);
				EnvironmentConfiguration.storeFile(
						label + "-request-odb-str.txt", odbRequestString.getBytes());

				DynamicObjectContainer odbInventoryData =
						DynamicObjectContainerFactory.create(ovds.getTransformedDauInventory(true));
				EnvironmentConfiguration.storeFile(label + "-inventory-odb.json", Utils.difGson.toJson(odbInventoryData).getBytes());
				String odbInventoryString = odbInventoryData.toString(false);
				EnvironmentConfiguration.storeFile(label + "-inventory-odb-str.txt", odbInventoryString.getBytes());

				XmlElementDataSource requestXeds = new XmlElementDataSource(testScenario.getXmlMdlrootInputPath());
				DynamicObjectContainer xmlRequestData =
						DynamicObjectContainerFactory.create(requestXeds.getInterconnectedTransformedFaultyConfiguration(true));
				EnvironmentConfiguration.storeFile(
						label + "-request-xml.json", Utils.difGson.toJson(xmlRequestData).getBytes());
				String xmlRequestString = xmlRequestData.toString(false);
				EnvironmentConfiguration.storeFile(label + "-request-xml-str.txt", xmlRequestString.getBytes());


				XmlElementDataSource inventoryXeds = new XmlElementDataSource(testScenario.getXmlInventoryPath());
				DynamicObjectContainer xmlInventoryData =
						DynamicObjectContainerFactory.create(inventoryXeds.getTransformedDauInventory(true));
				EnvironmentConfiguration.storeFile(
						label + "-inventory-xml.json", Utils.difGson.toJson(xmlInventoryData).getBytes());
				String xmlInventoryString = xmlInventoryData.toString(false);
				EnvironmentConfiguration.storeFile(label + "-inventory-xml-str.txt", xmlInventoryString.getBytes());

				if (odbRequestString.equals(xmlRequestString)) {
					System.out.println("The ODB and XML request data for the scenario '" + label + "' is equivalent!");
				} else {
					System.err.println("ERROR!! The ODB and XML request data for the scenario '" + label + "' is not equivalent!");
				}

				if (odbInventoryString.equals(xmlInventoryString)) {
					System.out.println("The ODB and XML inventory data for the scenario '" + label + "' is equivalent!");
				} else {
					System.err.println("ERROR!! The ODB and XML inventory data for the scenario '" + label + "' is not equivalent!");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (server != null) {
				server.shutdown();
			}
		}
	}

	private static List<ValidationResult> validateOdbTestSession(@Nonnull String odbPath, @Nonnull String label, @Nonnull ValidatorConfiguration config) {
		List<ValidationResult> results = new LinkedList<>();

		try {
			OrientVertexDataSource ovds = new OrientVertexDataSource(odbPath);

			results.add(innerValidateScenario(ovds, null, ovds.getServerPath(), config, ValidationScenario.InputConfigurationRequirements, label));
			results.add(innerValidateScenario(ovds, null, ovds.getServerPath(), config, ValidationScenario.DauInventory, label));

			if (config.fullValidation || config.validateMdlrootUsage) {
				results.add(innerValidateScenario(ovds, null, ovds.getServerPath(), config, ValidationScenario.InputConfigurationUsage, label));
			}

		} catch (AdaptationnException e) {
			if (config.haltOnFailure) {
				throw e;
			} else {
				switch (e.result) {
					case ReadyForAdaptation:
					case AdaptationSuccessful:
					case AdaptationNotRequired:
					case PerturbationInputInvalid:
					case AdaptationUnsuccessful:
					case AdaptationPartiallySuccessful:
						e.printStackTrace(System.err);
						logger.error(e.getMessage());
						break;

					case AdaptationInternalError:
					case AdaptationUnexpectedError:
						throw e;
				}
			}
		}
		return results;
	}

	private static List<ValidationResult> validate_single_file(@Nonnull File xmlFile, @Nonnull ValidatorConfiguration config) {
		List<ValidationResult> results = new LinkedList<>();

		try {
			XmlElementDataSource xec = new XmlElementDataSource(xmlFile);

			if (xec.isInputConfiguration()) {
				results.add(innerValidateScenario(xec, xec.getSourceFile(), null, config, ValidationScenario.InputConfigurationRequirements, null));

				if (config.fullValidation || config.validateMdlrootUsage) {
					results.add(innerValidateScenario(xec, xec.getSourceFile(), null, config, ValidationScenario.InputConfigurationUsage, null));
				}

			} else if (xec.isDauInventory()) {
				results.add(innerValidateScenario(xec, xec.getSourceFile(), null, config, ValidationScenario.DauInventory, null));

			} else {
				logger.error("XML File did not contain a 'DAUInventory' or 'MDLRoot' in the root!");
				System.exit(1);
			}
		} catch (AdaptationnException e) {
			if (config.haltOnFailure) {
				throw e;
			} else {
				switch (e.result) {
					case ReadyForAdaptation:
					case AdaptationSuccessful:
					case AdaptationNotRequired:
					case PerturbationInputInvalid:
					case AdaptationUnsuccessful:
					case AdaptationPartiallySuccessful:
						e.printStackTrace(System.err);
						logger.error(e.getMessage());
						break;

					case AdaptationInternalError:
					case AdaptationUnexpectedError:
						throw e;
				}
			}
		}
		return results;
	}

	public static void execute(@Nonnull ValidatorConfiguration config) {
		config.validateParams();

		ValidationResultContainer vrc = new ValidationResultContainer();
		OdbEmbeddedServer.OdbDeploymentMode deploymentMode = config.getDeploymentMode();
		String odbTarget = config.getOdbTarget();

		if (odbTarget != null) {
			vrc.results.addAll(validateOdbTestSession(odbTarget, "ServerSession", config));

		} else if (deploymentMode != null) {
			List<TestScenario> testScenarios = config.getScenariosToValidate();
			OdbEmbeddedServer server = new OdbEmbeddedServer(testScenarios.toArray(new TestScenario[0]));
			server.init(deploymentMode);
			vrc.results.addAll(validateOdbTestScenarios(testScenarios, server, config));
			server.shutdown();

		} else {
			List<File> xmlFilesToValidate = config.getXmlFilesToValidate();

			for (File xmlFile : xmlFilesToValidate) {
				vrc.results.addAll(validate_single_file(xmlFile, config));
			}
		}

		if (config.compareXMlToOdbAll) {
			compareOdbTestScenariosToXml();
			System.exit(0);
		}

		if (config.outputPath != null) {
			try {
				FileWriter writer = new FileWriter(new File(config.outputPath));
				JsonWriter jw = new JsonWriter(writer);
				Utils.getGson().toJson(vrc, ValidationResultContainer.class, jw);
				jw.flush();
				jw.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
