package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;

import javax.annotation.Nonnull;

public abstract class AbstractDataTarget<T> extends AbstractDataSource<T> {
	protected abstract void commit();

	protected abstract void update_rewireNode(@Nonnull T originalNode, @Nonnull T replacementNode);

	protected abstract void update_removeNodeTree(T node);

	protected abstract void update_insertNodeAsChild(@Nonnull T newNode, @Nonnull T parentNode);

	protected abstract void update_NodeAttribute(@Nonnull T node, @Nonnull String attributeName, @Nonnull Object attributeValue);


}
