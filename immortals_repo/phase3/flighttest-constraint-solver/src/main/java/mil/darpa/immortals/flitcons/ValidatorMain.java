package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueException;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.mdl.XmlElementCollector;
import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;
import picocli.CommandLine;

import java.io.File;


public class ValidatorMain {
	@CommandLine.Parameters(arity = "1..*", description = "XML Files to Validate")
	private File[] inputFiles;

	@CommandLine.Option(names = "--input-rules-xls", description = "The input Excel file to use for the rules. Otherwise, the rules within the resources directory will be used.")
	private File inputXls = null;

	@CommandLine.Option(names = "--output-rules-drl", description = "The location where the input rules should be written out to if they were provided")
	private File outputDrl = null;

	@CommandLine.Option(names = {"-C", ValidationDataContainer.COLORLESS_FLAG}, description = "Results in the validation result only displaying invalid values instead of displaying all values as green or red depending on pass or fail")
	private boolean colorlessMode = false;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	private boolean helpRequested = false;

	public static void main(String[] args) {
		ValidatorMain m = new ValidatorMain();
		CommandLine.populateCommand(m, args);
		m.execute();
	}

	private void execute() {
		if (helpRequested) {
			CommandLine.usage(this, System.out);
			return;
		}

		try {
			Configuration config = Configuration.getInstance();
			for (File xmlFile : inputFiles) {
				XmlElementCollector xec = new XmlElementCollector(
						xmlFile,
						config.dataCollectionInstructions,
						config.transformation);

				if (xec.isInputConfiguration()) {
					HierarchicalDataContainer dataContainer = xec.getInputConfiguration();
					DynamicObjectContainer objectContainer = Utils.createDslInterchangeFormat(dataContainer);
					MdlDataValidator validator = new MdlDataValidator(inputXls, outputDrl);
					ValidationDataContainer result = validator.validate(ValidationScenario.InputConfiguration, objectContainer);
					result.printResults(ValidationScenario.InputConfiguration.name(), !colorlessMode);

				} else if (xec.isDauInventory()) {
					HierarchicalDataContainer dataContainer = xec.getDauInventory();
					DynamicObjectContainer objectContainer = Utils.createDslInterchangeFormat(dataContainer);
					MdlDataValidator validator = new MdlDataValidator(inputXls, outputDrl);
					ValidationDataContainer result = validator.validate(ValidationScenario.DauInventory, objectContainer);
					result.printResults(ValidationScenario.DauInventory.name(), !colorlessMode);

				} else {
					System.err.println("XML File did not contain a 'DAUInventory' or 'MDLRoot' in the root!");
					System.exit(1);
				}
			}
		} catch (DynamicValueException e) {
			throw new RuntimeException(e);
		}
	}
}
