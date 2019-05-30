package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.DataType;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataTransformer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.mdl.MdlHacks;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataCollector<T> {


	private HierarchicalDataContainer rawDauInventoryContainer;
	private HierarchicalDataContainer rawInputConfigurationContainer;
	private HierarchicalDataContainer rawMeasurementContainer;
	private Configuration config;

	private final DataSourceInterface<T> dataSource;

	public DataCollector(@Nonnull DataSourceInterface<T> dataSource) {
		config = Configuration.getInstance();
		this.dataSource = dataSource;
	}

	private synchronized HierarchicalDataContainer getRawInputConfiguration() {
		dataSource.init();
		if (rawInputConfigurationContainer == null) {
			LinkedHashMap<T, List<T>> dauVertices = dataSource.collectRawDauData();
			rawInputConfigurationContainer = dataSource.createContainer(DataType.RawInputConfigurationData, dauVertices, config.dataCollectionInstructions);
			MdlHacks.fixHierarchicalData(rawInputConfigurationContainer.getDataIterator());
			if (rawInputConfigurationContainer.dataType != DataType.RawInputConfigurationData) {
				throw new RuntimeException("BAD TYPE!");
			}
		}
		return rawInputConfigurationContainer;
	}

	private synchronized HierarchicalDataContainer getRawFaultyConfigurationExternalsContainer() {
		dataSource.init();
		if (rawMeasurementContainer == null) {
			LinkedHashMap<T, List<T>> measurementVertices = dataSource.collectRawMeasurementData();
			rawMeasurementContainer = dataSource.createContainer(DataType.RawInputExternalData, measurementVertices, config.dataCollectionInstructions);
			MdlHacks.fixHierarchicalData(rawMeasurementContainer.getDataIterator());
		}
		if (rawMeasurementContainer.dataType != DataType.RawInputExternalData) {
			throw new RuntimeException("BAD TYPE!");
		}
		return rawMeasurementContainer;
	}

	public synchronized HierarchicalDataContainer getInterconnectedFaultyConfiguration() {
		dataSource.init();
		if (!dataSource.isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "The provided data is not an input configuration!");
		}

		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> relationMap = dataSource.convertOneToManyMap(dataSource.collectRawDauMeasurementIndirectRelations());

		HierarchicalDataTransformer transformer = new HierarchicalDataTransformer(
				getRawInputConfiguration(), false, getRawFaultyConfigurationExternalsContainer(), relationMap);

		HierarchicalDataContainer rval = transformer.produceInterconnectedResult();
		if (rval.dataType != DataType.InputInterconnectedData) {
			throw new RuntimeException("BAD TYPE!");
		}
		return rval;
	}

	public synchronized HierarchicalDataContainer getRawDauInventoryContainer() {
		dataSource.init();
		if (rawDauInventoryContainer == null) {
			LinkedHashMap<T, List<T>> dauInventoryVertices = dataSource.collectRawDauInventoryData();
			rawDauInventoryContainer = dataSource.createContainer(DataType.RawInventory, dauInventoryVertices, config.dataCollectionInstructions);
			MdlHacks.fixHierarchicalData(rawDauInventoryContainer.getDataIterator());
		}
		if (rawDauInventoryContainer.dataType != DataType.RawInventory) {
			throw new RuntimeException("BAD TYPE!");
		}
		return rawDauInventoryContainer;
	}

	public synchronized HierarchicalDataContainer getInterconnectedTransformedFaultyConfiguration(boolean preserveDebugRelations) {
		dataSource.init();
		if (!dataSource.isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "The provided data is not an input configuration!");
		}

		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> relationMap = dataSource.convertOneToManyMap(dataSource.collectRawDauMeasurementIndirectRelations());

		HierarchicalDataTransformer transformer = new HierarchicalDataTransformer(
				getRawInputConfiguration(), preserveDebugRelations, getRawFaultyConfigurationExternalsContainer(), relationMap);

		HierarchicalDataContainer rval = transformer.produceInterconnectedRequirements();
		if (rval.dataType != DataType.InputInterconnectedRequirementsData) {
			throw new RuntimeException("BAD TYPE!");
		}
		return rval;
	}

	public synchronized HierarchicalDataContainer getInterconnectedInputConfigurationUsage(boolean preserveDebugRelations) {
		dataSource.init();
		if (!dataSource.isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "The provided data is not an input configuration!");
		}

		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> relationMap = dataSource.convertOneToManyMap(dataSource.collectRawDauMeasurementIndirectRelations());

		HierarchicalDataTransformer transformer = new HierarchicalDataTransformer(
				getRawInputConfiguration(), preserveDebugRelations, getRawFaultyConfigurationExternalsContainer(), relationMap);

		HierarchicalDataContainer rval = transformer.produceInterconnectedUsage();
		if (rval.dataType != DataType.InputInterconnectedUsageData) {
			throw new RuntimeException("BAD TYPE!");
		}
		return rval;
	}

	public synchronized HierarchicalDataContainer getInterconnectedTransformedInputConfigurationUsage(boolean preserveDebugRelations) {
		dataSource.init();
		if (!dataSource.isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "The provided data is not an input configuration!");
		}

		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> relationMap = dataSource.convertOneToManyMap(dataSource.collectRawDauMeasurementIndirectRelations());

		HierarchicalDataTransformer transformer = new HierarchicalDataTransformer(
				getRawInputConfiguration(), preserveDebugRelations, getRawFaultyConfigurationExternalsContainer(), relationMap);

		HierarchicalDataContainer rval = transformer.produceInterconnectedUsage();
		if (rval.dataType != DataType.InputInterconnectedUsageData) {
			throw new RuntimeException("BAD TYPE!");
		}
		return rval;
	}

//	public synchronized HierarchicalDataContainer getInterconnectedTransformedValidConfiguration(boolean preserveDebugRelations) {
//		dataSource.init();
//		if (!dataSource.isInputConfiguration()) {
//			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "The provided data is not an input configuration!");
//		}
//
//		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> relationMap = dataSource.convertOneToManyMap(dataSource.collectRawDauMeasurementIndirectRelations());
//
//		HierarchicalDataTransformer transformer = new HierarchicalDataTransformer(
//				getRawValidConfiguration(), transformationInstructions, preserveDebugRelations, getRawFaultyConfigurationExternalsContainer(), relationMap);
//
//		HierarchicalDataContainer rval = transformer.produceTransformedInterconnectedResult();
//		if (rval.dataType != DataType.InputInterconnectedUsageData) {
//			throw new RuntimeException("BAD TYPE!");
//		}
//		return rval;
//	}

	public HierarchicalDataContainer getTransformedDauInventory(boolean preserveDebugRelations) {
		dataSource.init();

		HierarchicalDataTransformer transformer = new HierarchicalDataTransformer(
				getRawDauInventoryContainer(), preserveDebugRelations, null, null);

		HierarchicalDataContainer rval = transformer.produceInterconnectedRequirements();
		if (rval.dataType != DataType.InventoryRequirementsData) {
			throw new RuntimeException("BAD TYPE!");
		}
		return rval;
	}
}
