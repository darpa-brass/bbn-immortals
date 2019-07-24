package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.mdl.XmlElementDataSource;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import java.io.File;


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

	public static void execute(@Nonnull ValidatorConfiguration config) {
		if (config.inputFiles == null) {
			System.err.println("Input files must be provided!");
			System.exit(-1);
		}

		for (File xmlFile : config.inputFiles) {
			try {
				XmlElementDataSource xec = new XmlElementDataSource(xmlFile);
				MdlDataValidator validator = new MdlDataValidator(config.inputXls, config.outputDrl, xec);
				validator.setSaveResults(false);

				if (xec.isInputConfiguration()) {
					validator.validateConfiguration(ValidationScenario.InputConfigurationRequirements);

				} else if (xec.isDauInventory()) {
					validator.validateConfiguration(ValidationScenario.DauInventory);

				} else {
					System.err.println("XML File did not contain a 'DAUInventory' or 'MDLRoot' in the root!");
					System.exit(1);
				}
			} catch (AdaptationnException e) {
				if (logger.isDebugEnabled()) {
					throw e;
				} else {
					switch (e.result) {
						case ReadyForAdaptation:
						case AdaptationSuccessful:
						case AdaptationNotRequired:
						case PerturbationInputInvalid:
						case AdaptationUnsuccessful:
						case AdaptationPartiallySuccessful:
							System.err.println(e.getMessage());
							break;

						case AdaptationInternalError:
						case AdaptationUnexpectedError:
							throw e;
					}
				}
			}
		}
	}
}
