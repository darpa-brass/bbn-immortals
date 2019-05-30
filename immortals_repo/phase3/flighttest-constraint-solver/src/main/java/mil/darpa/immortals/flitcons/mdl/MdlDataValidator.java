package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.DataCollector;
import mil.darpa.immortals.flitcons.DataSourceInterface;
import mil.darpa.immortals.flitcons.Utils;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueeException;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import mil.darpa.immortals.flitcons.validation.DataValidator;
import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;
import mil.darpa.immortals.schemaevolution.ProvidedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

import static mil.darpa.immortals.flitcons.Utils.*;

public class MdlDataValidator extends DataValidator {

	private final DataCollector collector;

	public MdlDataValidator(@Nullable File inputExcelFile, @Nullable File outputDrlFile, DataSourceInterface dataSource) {
		super(inputExcelFile, outputDrlFile);
		this.collector = new DataCollector(dataSource);

	}

	public void init() {
		super.init();
	}


	public ValidationDataContainer validateConfiguration(@Nonnull ValidationScenario scenario, boolean useColor) {
		try {
			HierarchicalDataContainer data;
			String outputFile;

			switch (scenario) {

				case InputConfigurationUsage:
					data = collector.getInterconnectedTransformedInputConfigurationUsage(true);
					outputFile = SOLVER_INPUT_USAGE_FILE;
					break;

				case InputConfigurationRequirements:
					data = collector.getInterconnectedTransformedFaultyConfiguration(true);
					outputFile = SOLVER_INPUT_REQUIREMENTS_FILE;
					if (data.getDauRootNodes().stream().noneMatch(x -> x.getAttribute(FLAGGED_FOR_REPLACEMENT) != null)) {
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
					data = collector.getInterconnectedInputConfigurationUsage(true);
					outputFile = SOLVER_OUTPUT_USAGE_FILE;
					break;

				default:
					throw AdaptationnException.internal("Invalid scenario type '" + scenario.name() + "'!");
			}

			DynamicObjectContainer dynamicData = Utils.createDslInterchangeFormat(data);
			String dynamnicDataString = difGson.toJson(dynamicData);

			try {
				ProvidedData.storeFile(outputFile, dynamnicDataString.getBytes());
			} catch (Exception e) {
				throw AdaptationnException.internal(e);
			}

			init();
			ValidationDataContainer results = super.validate(scenario.filter, dynamicData);
			results.printResults(scenario.title, useColor);
			return results;
		} catch (DynamicValueeException e) {
			throw AdaptationnException.input(e);
		}
	}
}
