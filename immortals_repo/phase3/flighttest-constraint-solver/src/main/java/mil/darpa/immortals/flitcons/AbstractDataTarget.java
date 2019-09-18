package mil.darpa.immortals.flitcons;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public abstract class AbstractDataTarget<T> extends AbstractDataSource<T> {
	protected abstract void commit();

	protected abstract void update_rewireNode(@Nonnull T originalNode, @Nonnull T replacementNode);

	protected abstract void update_removeNodeTree(T node);

	protected abstract void update_insertNodeAsChild(@Nonnull T newNode, @Nonnull T parentNode);

	protected abstract void update_NodeAttribute(@Nonnull T node, @Nonnull String attributeName, @Nonnull Object attributeValue);

	protected abstract void add_NodeAttribute(@Nonnull T node, @Nonnull String attributeName, @Nonnull Object attributeValue);

	protected abstract void update_createOrUpdateChildWithAttributes(@Nonnull T parentNode, @Nonnull List<String> childPath,
	                                                                 @Nonnull Map<String, Object> attributes);

	protected abstract void update_removeAttribute(@Nonnull T parentNode, @Nonnull String attributeName, String... childPath);
}
