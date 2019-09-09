package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.AbstractDataSource;
import mil.darpa.immortals.flitcons.NestedPathException;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainerFactory;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import mil.darpa.immortals.flitcons.validation.DataValidator;
import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

import static mil.darpa.immortals.flitcons.Utils.FLAGGED_FOR_REPLACEMENT;
import static mil.darpa.immortals.flitcons.Utils.difGson;

public class MdlDataValidator extends DataValidator {

	private final AbstractDataSource collector;

	private boolean saveResults = true;


	public MdlDataValidator(@Nullable File inputExcelFile, @Nullable File outputDrlFile, AbstractDataSource dataSource) {
		super(inputExcelFile, outputDrlFile);
		this.collector = dataSource;
	}

	public MdlDataValidator(AbstractDataSource dataSource) {
		super(null, null);
		this.collector = dataSource;
	}

	public void setSaveResults(boolean value) {
		saveResults = value;
	}

	public void init() {
		super.init();
	}


	public ValidationDataContainer validateConfiguration(@Nonnull ValidationScenario scenario, boolean verbose, boolean lightMode) {
		return validateConfiguration(scenario, verbose, lightMode, null);
	}

	public ValidationDataContainer validateConfiguration(@Nonnull ValidationScenario scenario, boolean verbose, boolean lightMode, @Nullable String fileTag) {
		try {
			HierarchicalDataContainer data;
			String outputFile;

			ValidationDataContainer results = null;

			switch (scenario) {

				case InputConfigurationUsage:
					data = collector.getInterconnectedTransformedInputConfigurationUsage(true);
					outputFile = SOLVER_INPUT_USAGE_FILE;
					break;

				case InputConfigurationRequirements:
					data = collector.getInterconnectedTransformedFaultyConfiguration(true);
					outputFile = SOLVER_INPUT_REQUIREMENTS_FILE;
					if (data.getDauRootNodes().stream().noneMatch(x -> x.getAttributeNames().contains(FLAGGED_FOR_REPLACEMENT))) {
						throw new AdaptationnException(ResultEnum.AdaptationNotRequired, "No DAUs have been flagged for replacement!");
					}
					break;

				case DauInventory:
					data = collector.getTransformedDauInventory(true);
					outputFile = SOLVER_DAUINVENTORY_FILE;
					if (data.getDauRootNodes().isEmpty()) {
						throw new AdaptationnException(ResultEnum.AdaptationUnsuccessful, "DAU Inventory appears to be empty. Cannot solve.");
					}
					break;

				case OutputConfigurationUsage:
					data = collector.getInterconnectedTransformedInputConfigurationUsage(true);
					outputFile = SOLVER_OUTPUT_USAGE_FILE;
					break;

				default:
					throw AdaptationnException.internal("Invalid scenario type '" + scenario.name() + "'!");
			}

			if (!lightMode) {
				DynamicObjectContainer dynamicData = DynamicObjectContainerFactory.create(data);
				String dynamnicDataString = difGson.toJson(dynamicData);

				try {
					if (saveResults) {
						EnvironmentConfiguration.storeFile(
								fileTag == null ? outputFile : fileTag + '-' + outputFile,
								dynamnicDataString.getBytes());
					}
				} catch (Exception e) {
					throw AdaptationnException.internal(e);
				}

				results = super.validate(scenario.filter, dynamicData);
				if (verbose) {
					results.printResults(scenario.title);
				}
			}
			return results;
		} catch (NestedPathException e) {
			throw AdaptationnException.input(e);
		}
	}
}
