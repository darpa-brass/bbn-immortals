package mil.darpa.immortals.flitcons;

import ch.qos.logback.classic.util.ContextInitializer;
import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.mdl.XmlElementDataSource;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class ValidatorMain {

	public static void main(String[] args) {
		ValidatorConfiguration config = ValidatorConfiguration.getInstance();

		CommandLine.populateCommand(config, args);

		if (config.helpRequested) {
			CommandLine.usage(config, System.out);
		} else {
			if (config.debugMode) {
				System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");
				Logger rootLogger = LogManager.getLogManager().getLogger("");
				for (Handler h : rootLogger.getHandlers()) {
					h.setLevel(Level.ALL);
				}
				rootLogger.setLevel(Level.ALL);

			} else {
				System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback-validator.xml");
			}
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
				System.out.println("File: " + xmlFile.getAbsolutePath());
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
				if (config.debugMode) {
					throw e;
				} else {
					System.err.println(e.getMessage());
				}
			}
		}
	}
}
