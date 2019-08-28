package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.DataType;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataTransformer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.mdl.MdlHacks;
import mil.darpa.immortals.flitcons.mdl.validation.PortMapping;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractDataSource<T> {

	private HierarchicalDataContainer rawInventoryContainer;
	private HierarchicalDataContainer rawInputConfigurationContainer;
	private HierarchicalDataContainer rawExternalDataContainer;
	private Configuration config;

	protected final String nullValuePlaceholder;

	private void _init() {
		init();
		List<String> errors = validateRelationalIntegrity();
		if (errors != null && !errors.isEmpty()) {
			throw AdaptationnException.input(String.join("\n", errors));
		}
	}

	public AbstractDataSource() {
		config = Configuration.getInstance();
		nullValuePlaceholder = config.nullValuePlaceholder;
	}

	private synchronized HierarchicalDataContainer getRawInputConfiguration() {
		_init();
		if (rawInputConfigurationContainer == null) {
			LinkedHashMap<T, List<T>> dauVertices = collectRawInputData();
			rawInputConfigurationContainer = createContainer(DataType.RawInputConfigurationData, dauVertices, config.dataCollectionInstructions);
			MdlHacks.fixHierarchicalData(rawInputConfigurationContainer.getDataIterator());
			if (rawInputConfigurationContainer.dataType != DataType.RawInputConfigurationData) {
				throw new RuntimeException("BAD TYPE '" + rawInputConfigurationContainer.dataType.name() + "'!");
			}
			rawInputConfigurationContainer.validate();
		}
		return rawInputConfigurationContainer;
	}

	private synchronized HierarchicalDataContainer getRawFaultyConfigurationExternalsContainer() {
		_init();
		if (rawExternalDataContainer == null) {
			LinkedHashMap<T, List<T>> measurementVertices = collectRawExternalData();
			rawExternalDataContainer = createContainer(DataType.RawInputExternalData, measurementVertices, config.dataCollectionInstructions);
			MdlHacks.fixHierarchicalData(rawExternalDataContainer.getDataIterator());
			rawExternalDataContainer.validate();
		}
		if (rawExternalDataContainer.dataType != DataType.RawInputExternalData) {
			throw new RuntimeException("BAD TYPE '" + rawExternalDataContainer.dataType.name() + "'!");
		}

		return rawExternalDataContainer;
	}

	public synchronized HierarchicalDataContainer getInterconnectedFaultyConfiguration() {
		HierarchicalDataTransformer transformer = createInputTransformer(false);
		HierarchicalDataContainer rval = transformer.produceInterconnectedResult();

		if (rval.dataType != DataType.InputInterconnectedData) {
			throw new RuntimeException("BAD TYPE '" + rval.dataType.name() + "'!");
		}
		return rval;
	}

	public synchronized HierarchicalDataContainer getRawInventoryContainer() {
		_init();
		if (rawInventoryContainer == null) {
			LinkedHashMap<T, List<T>> dauInventoryVertices = collectRawInventoryData();
			rawInventoryContainer = createContainer(DataType.RawInventory, dauInventoryVertices, config.dataCollectionInstructions);
			MdlHacks.fixHierarchicalData(rawInventoryContainer.getDataIterator());
		}
		if (rawInventoryContainer.dataType != DataType.RawInventory) {
			throw new RuntimeException("BAD TYPE '" + rawInventoryContainer.dataType.name() + "'!");
		}
		rawInventoryContainer.validate();
		return rawInventoryContainer;
	}

	private synchronized HierarchicalDataTransformer createInputTransformer(boolean preserveDebugRelations) {
		_init();
		if (!isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "The provided data is not an input configuration!");
		}

		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> relationMap = convertOneToManyMap(collectRawInputExternalDataIndirectRelations());

		return new HierarchicalDataTransformer(
				getRawInputConfiguration(), preserveDebugRelations, getRawFaultyConfigurationExternalsContainer(), relationMap);
	}

	public synchronized HierarchicalDataContainer getInterconnectedTransformedFaultyConfiguration(boolean preserveDebugRelations) {
		HierarchicalDataTransformer transformer = createInputTransformer(preserveDebugRelations);
		HierarchicalDataContainer rval = transformer.produceInterconnectedRequirements();
		if (rval.dataType != DataType.InputInterconnectedRequirementsData) {
			throw new RuntimeException("BAD TYPE '" + rval.dataType.name() + "'!");
		}
		return rval;
	}

	public synchronized HierarchicalDataContainer getInterconnectedTransformedInputConfigurationUsage(boolean preserveDebugRelations) {
		HierarchicalDataTransformer transformer = createInputTransformer(preserveDebugRelations);
		HierarchicalDataContainer rval = transformer.produceInterconnectedUsage();
		if (rval.dataType != DataType.InputInterconnectedUsageData) {
			throw new RuntimeException("BAD TYPE!");
		}
		return rval;
	}

	public HierarchicalDataContainer getTransformedDauInventory(boolean preserveDebugRelations) {
		_init();

		HierarchicalDataTransformer transformer = new HierarchicalDataTransformer(
				getRawInventoryContainer(), preserveDebugRelations, null, null);

		HierarchicalDataContainer rval = transformer.produceInterconnectedRequirements();
		if (rval.dataType != DataType.InventoryRequirementsData) {
			throw new RuntimeException("BAD TYPE '" + rval.dataType.name() + "'!");
		}
		return rval;

	}

	protected abstract HierarchicalIdentifier createIdentifier(@Nonnull T src);

	protected abstract void init();

	public abstract void shutdown();

	public abstract void restart();

	protected abstract LinkedHashMap<T, List<T>> collectRawInputData();

	protected abstract LinkedHashMap<T, List<T>> collectRawExternalData();

	protected abstract Map<T, Set<T>> collectRawInputExternalDataIndirectRelations();

	protected abstract LinkedHashMap<T, List<T>> collectRawInventoryData();

	public abstract boolean isDauInventory();

	public abstract boolean isInputConfiguration();

	public abstract List<String> validateRelationalIntegrity();

	protected abstract HierarchicalDataContainer createContainer(@Nonnull DataType mode, @Nonnull LinkedHashMap<T, List<T>> input, @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions);

	private Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> convertOneToManyMap(@Nonnull Map<T, Set<T>> indirectRelations) {
		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> rval = new HashMap<>();

		for (Map.Entry<T, Set<T>> entry : indirectRelations.entrySet()) {
			HierarchicalIdentifier primaryNode = createIdentifier(entry.getKey());
			Set<HierarchicalIdentifier> relationSet = rval.computeIfAbsent(primaryNode, k -> new HashSet<>());
			relationSet.addAll(entry.getValue().stream().map(this::createIdentifier).collect(Collectors.toSet()));
		}
		return rval;
	}

	public abstract Map<String, PortMapping> getPortMappingDetails();
}

