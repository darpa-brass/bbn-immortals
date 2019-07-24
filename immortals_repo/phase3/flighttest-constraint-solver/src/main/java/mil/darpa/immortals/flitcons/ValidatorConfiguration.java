package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;
import picocli.CommandLine;

import java.io.File;

public class ValidatorConfiguration {

	private static ValidatorConfiguration instance;

	public synchronized static ValidatorConfiguration getInstance() {
		if (instance == null) {
			instance = new ValidatorConfiguration();
		}
		return instance;
	}

	private ValidatorConfiguration() {

	}

	@CommandLine.Parameters(arity = "0..*", description = "XML Files to Validate")
	public File[] inputFiles;

	@CommandLine.Option(names = "--input-rules-xls", description = "The input Excel file to use for the rules. Otherwise, the rules within the resources directory will be used.")
	public File inputXls = null;

	@CommandLine.Option(names = "--output-rules-drl", description = "The location where the input rules should be written out to if they were provided")
	public File outputDrl = null;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	public boolean helpRequested = false;
}
