package mil.darpa.immortals.flitcons.datatypes.hierarchical;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public interface HierarchicalDataConverter<T> {

	HierarchicalDataContainer createContainer(@Nonnull Map<T, Set<T>> input, @Nullable Map<String, Set<String>> interestedProperties);

	HierarchicalData createData(@Nonnull T src, boolean isRootObject, @Nullable Map<String, Set<String>> interestedProperties);

	HierarchicalIdentifier createIdentifier(@Nonnull T src);

	Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> convertOneToManyMap(@Nonnull Map<T, Set<T>> indirectRelations);
}
