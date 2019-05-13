package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.DataType;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;

import javax.annotation.Nonnull;
import java.util.*;

public interface DataSourceInterface<T> {
	void init();

	void commit();

	void shutdown();

	void restart();

	LinkedHashMap<T, List<T>> collectRawDauData();

	LinkedHashMap<T, List<T>> collectRawMeasurementData();

	Map<T, Set<T>> collectRawDauMeasurementIndirectRelations();

	LinkedHashMap<T, List<T>> collectRawDauInventoryData();

	void update_rewireNode(@Nonnull T originalNode, @Nonnull T replacementNode);

	void update_removeNodeTree(T node);

	void update_insertNodeAsChild(@Nonnull T newNode, @Nonnull T parentNode);

	void update_NodeAttribute(@Nonnull T node, @Nonnull String attributeName, @Nonnull Object attributeValue);

	boolean isDauInventory();

	boolean isInputConfiguration();

	HierarchicalDataContainer createContainer(@Nonnull DataType mode, @Nonnull LinkedHashMap<T, List<T>> input, @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions);

	HierarchicalData createData(@Nonnull T src, boolean isRootObject, @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions);

	Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> convertOneToManyMap(@Nonnull Map<T, Set<T>> indirectRelations);

	TreeMap<String, TreeMap<String, Object>> getPortMappingChartData();
}
