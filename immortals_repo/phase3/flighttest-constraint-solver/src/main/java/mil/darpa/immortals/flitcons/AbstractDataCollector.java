package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataTransformer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.mdl.MdlHacks;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractDataCollector<T> {

	private final Map<String, Set<String>> collectedPrimaryProperties;
	private final Map<String, Set<String>> collectedExternalProperties;
	private final Map<String, Set<String>> collectedDebugProperties;
	private final Set<String> valuesToDefaultToTrue;
	private final Configuration.TransformationInstructions transformationInstructions;

	protected AbstractDataCollector(@Nonnull Configuration.PropertyCollectionInstructions collectionInstructions,
	                                @Nonnull Configuration.TransformationInstructions transformationInstructions) {
		this.collectedPrimaryProperties = collectionInstructions.collectedPrimaryProperties;
		this.collectedExternalProperties = collectionInstructions.collectedExternalProperties;
		this.collectedDebugProperties = collectionInstructions.collectedDebugProperties;
		this.valuesToDefaultToTrue = collectionInstructions.valuesToDefaultToTrue;
		this.transformationInstructions = transformationInstructions;
	}

	protected abstract void init();

	public abstract boolean isDauInventory();

	public abstract boolean isInputConfiguration();

	protected abstract LinkedHashMap<T, List<T>> collectRawDauData();

	protected abstract LinkedHashMap<T, List<T>> collectRawMeasurementData();

	protected abstract Map<T, Set<T>> getRawDauMeasurementIndirectRelations();

	protected abstract LinkedHashMap<T, List<T>> collectRawDauInventoryData();

	public HierarchicalDataContainer getInputConfiguration() {
		init();

		if (!isInputConfiguration()) {
			throw new RuntimeException("The provided data is not an input configuration!");
		}

		LinkedHashMap<T, List<T>> dauVertices = collectRawDauData();
		LinkedHashMap<T, List<T>> measurementVertices = collectRawMeasurementData();
		Map<T, Set<T>> dauMeasurementRelations = getRawDauMeasurementIndirectRelations();

		HierarchicalDataContainer dauContainer = createContainer(dauVertices, valuesToDefaultToTrue, collectedPrimaryProperties, collectedDebugProperties);
		MdlHacks.fixHierarchicalData(dauContainer.getDataIterator());
		HierarchicalDataContainer measurementContainer = createContainer(measurementVertices, valuesToDefaultToTrue, collectedExternalProperties, collectedDebugProperties);
		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> relationMap = convertOneToManyMap(dauMeasurementRelations);


		HierarchicalDataTransformer transformer = new HierarchicalDataTransformer(
				dauContainer, transformationInstructions, measurementContainer, relationMap);

		return transformer.produceResult();
	}

	public HierarchicalDataContainer getDauInventory() {
		init();

		LinkedHashMap<T, List<T>> dauInventoryVertices = collectRawDauInventoryData();

		HierarchicalDataContainer dauInventoryContainer = createContainer(dauInventoryVertices, valuesToDefaultToTrue, collectedPrimaryProperties, collectedDebugProperties);
		MdlHacks.fixHierarchicalData(dauInventoryContainer.getDataIterator());
		HierarchicalDataTransformer transformer = new HierarchicalDataTransformer(
				dauInventoryContainer, transformationInstructions, null, null);

		return transformer.produceResult();
	}

	protected abstract HierarchicalDataContainer createContainer(@Nonnull LinkedHashMap<T, List<T>> input, @Nonnull Set<String> valuesToDefaultToTrue, @Nonnull Map<String, Set<String>> interestedProperties, @Nonnull Map<String, Set<String>> debugProperties);

	protected abstract HierarchicalData createData(@Nonnull T src, boolean isRootObject, @Nonnull Set<String> valuesToDefaultToTrue, @Nonnull Map<String, Set<String>> interestedProperties, @Nonnull Map<String, Set<String>> debugProperties);

	protected abstract Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> convertOneToManyMap(@Nonnull Map<T, Set<T>> indirectRelations);

}
