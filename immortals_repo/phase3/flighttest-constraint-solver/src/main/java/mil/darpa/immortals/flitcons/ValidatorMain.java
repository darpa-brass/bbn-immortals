package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.mdl.XmlElementDataSource;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ValidatorMain {

	private static final Logger logger = LoggerFactory.getLogger(ValidatorMain.class);

	public static void main(String[] args) {


		ValidatorConfiguration config = ValidatorConfiguration.getInstance();

		CommandLine.populateCommand(config, args);

		if (config.helpRequested) {
			CommandLine.usage(config, System.out);
		} else {
			execute(config);
		}
	}

	private static void innerValidateScenario(@Nonnull XmlElementDataSource xec,
	                                          @Nonnull ValidatorConfiguration config,
	                                          @Nonnull ValidationScenario validationScenario) {
		MdlDataValidator validator = new MdlDataValidator(config.getInputXls(), config.getOutputDrl(), xec);
		validator.setSaveResults(false);
		boolean verbose = config.verbose;
		boolean failed = false;

		logger.info(Utils.padCenter("Validating " + validationScenario.title, 80, '-'));
		logger.info("Filename: '" + xec.getSourceFile().replaceAll(EnvironmentConfiguration.getChallengeProblemsRoot().toString(), ""));

		ValidationDataContainer vdc = null;
		try {
			vdc = validator.validateConfiguration(validationScenario, false, config.lightValidation);

			// No Exception, so it passed
			if (config.lightValidation) {
				logger.info("Result: PASS");
				logger.info(Utils.padCenter("", 80, '-') + "\n\n");
			}
		} catch (AdaptationnException e) {
			failed = true;
			if (e.result == ResultEnum.PerturbationInputInvalid) {
				logger.info("Result: FAIL");
				logger.info("Details: " + e.getMessage());
				logger.info(Utils.padCenter("", 80, '-') + "\n\n");
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
			failed = true;
			if (vdc != null) {
				logger.info("Result: FAIL");
				logger.info("Details: ");
				vdc.printResults(validationScenario.title);
				logger.info(Utils.padCenter("", 80, '-') + "\n\n");
			}
		}
		if (failed && config.haltOnFailure) {
			System.exit(-1);
		}
	}

	private static void validate(@Nonnull File xmlFile, @Nonnull ValidatorConfiguration config) {
		try {
			XmlElementDataSource xec = new XmlElementDataSource(xmlFile);

			if (xec.isInputConfiguration()) {
				if (config.fullValidation) {
					innerValidateScenario(xec, config, ValidationScenario.InputConfigurationRequirements);
					innerValidateScenario(xec, config, ValidationScenario.InputConfigurationUsage);
				} else {
					innerValidateScenario(xec, config, ValidationScenario.InputConfigurationRequirements);
				}

			} else if (xec.isDauInventory()) {
				innerValidateScenario(xec, config, ValidationScenario.DauInventory);

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
						logger.error(e.getMessage());
						break;

					case AdaptationInternalError:
					case AdaptationUnexpectedError:
						throw e;
				}
			}
		}
	}

	public static void execute(@Nonnull ValidatorConfiguration config) {
		config.validateParams();

		if (config.inputFiles != null) {
			for (File xmlFile : config.inputFiles) {
				validate(xmlFile, config);
			}
		}

		ValidatorConfiguration.Scenario[] scenarios = null;

		if (config.scenarioCandidate != null) {
			scenarios = new ValidatorConfiguration.Scenario[]{ValidatorConfiguration.Scenario.valueOf(config.scenarioCandidate)};
		} else if (config.validateBbnXml) {
			ArrayList<ValidatorConfiguration.Scenario> scenarioList = new ArrayList<>(Arrays.stream(ValidatorConfiguration.Scenario.values()).filter(ValidatorConfiguration.Scenario::isBbn).collect(Collectors.toList()));
			scenarios = (ValidatorConfiguration.Scenario[])scenarioList.toArray(new ValidatorConfiguration.Scenario[0]);

		} else if (config.validateSwriXml) {
			ArrayList<ValidatorConfiguration.Scenario> scenarioList = new ArrayList<>(Arrays.stream(ValidatorConfiguration.Scenario.values()).filter(ValidatorConfiguration.Scenario::isSwri).collect(Collectors.toList()));
			scenarios = (ValidatorConfiguration.Scenario[])scenarioList.toArray(new ValidatorConfiguration.Scenario[0]);
		} else if (config.validateAllXml) {
			scenarios = ValidatorConfiguration.Scenario.values();
		} else {
			logger.error("No action provided!");
			System.exit(-1);
		}

		for (ValidatorConfiguration.Scenario scenario : scenarios) {
			if (scenario.hasXmlInput()) {
				validate(scenario.getXmlInput(), config);
			}
			if (scenario.hasXmlInventory()) {
				validate(scenario.getXmlInventory(), config);
			}
		}
	}
}
