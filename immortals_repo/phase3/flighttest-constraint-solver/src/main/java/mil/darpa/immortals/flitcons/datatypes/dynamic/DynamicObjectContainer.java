package mil.darpa.immortals.flitcons.datatypes.dynamic;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static mil.darpa.immortals.flitcons.Utils.GLOBALLY_UNIQUE_ID;

public class DynamicObjectContainer implements DuplicateInterface<DynamicObjectContainer> {

	public static final Map<String, String> aliases = new HashMap<>();

	public final HierarchicalIdentifier identifier;

	private final int hashCode = UUID.randomUUID().hashCode();

	public final String debugLabel;

	public final TreeMap<String, DynamicValue> children = new TreeMap<>();

	public DynamicObjectContainer(@Nonnull HierarchicalIdentifier identifier, @Nullable String debugLabel) {
		this.debugLabel = debugLabel;
		this.identifier = identifier;
		if (debugLabel != null) {
			aliases.put(identifier.getUniqueSessionIdentifier(), debugLabel);
		}
	}

	DynamicObjectContainer(@Nonnull HierarchicalIdentifier identifier, @Nonnull SortedMap<String, ? extends DynamicValue> sortedMap) {
		children.putAll(sortedMap);
		this.identifier = identifier;
		this.debugLabel = aliases.get(identifier.getUniqueSessionIdentifier());
	}

	public Set<String> keySet() {
		return children.keySet();
	}

	public DynamicValue get(@Nonnull String key) {
		return children.get(key);
	}

	public Set<Map.Entry<String, DynamicValue>> entrySet() {
		return children.entrySet();
	}

	public DynamicValue put(@Nonnull String key, @Nonnull DynamicValue value) {
		return children.put(key, value);
	}

	public boolean containsKey(@Nonnull String key) {
		return children.containsKey(key);
	}

	public void remove(@Nonnull String key) {
		children.remove(key);
	}

	public Set<String> createGroupingHashes() throws DynamicValueeException {
		try {

			Set<Equation> equations = children.values().stream().filter(x -> x.getValue() instanceof Equation).map(x -> (Equation) x.getValue()).collect(Collectors.toSet());
			Set<String> equationValues = new HashSet<>();

			for (Equation eq : equations) {
				equationValues.addAll(eq.getVariables());
			}

			Set<String> rval = new HashSet<>();

			Stack<StringBuilder> references = new Stack<>();

			references.push(new StringBuilder());
			TreeMap<Integer, Integer> stackArrayIndices = new TreeMap<>();

			List<String> attributeIdentifiers = new LinkedList<>(keySet());
			Collections.sort(attributeIdentifiers);
			int attributesLength = attributeIdentifiers.size();

			mainloop:
			for (int i = 0; i < attributesLength; i++) {
				String key = attributeIdentifiers.get(i);
				if (!key.equals(GLOBALLY_UNIQUE_ID)) {

					if (equationValues.contains(key)) {
						references.peek().append(key).append("=VALUE,");
					} else {

						DynamicValue val = get(key);

						switch (val.multiplicity) {

							case SingleValue:
								if (val.getValue() instanceof Equation) {
									references.peek().append(key).append("=VALUE,");
								} else {
									references.peek().append(key).append("=").append(val.getValue()).append(",");
								}
								break;

							case Set:
								Object[] oArray = (Object[]) val.getValue();

								if (stackArrayIndices.containsKey(i)) {
									int idx = stackArrayIndices.get(i) + 1;
									if (idx == oArray.length) {
										stackArrayIndices.remove(i);
										if (stackArrayIndices.isEmpty()) {
											break mainloop;

										} else {
											i = stackArrayIndices.lastKey() - 1;
											references.pop();
										}
									} else {
										stackArrayIndices.put(i, idx);
										references.push(new StringBuilder(references.peek().toString() + key + "=" + URLEncoder.encode(oArray[idx].toString(), StandardCharsets.UTF_8.toString()) + ","));
									}

								} else {
									stackArrayIndices.put(i, 0);
									references.push(new StringBuilder(references.peek().toString() + key + "=" + URLEncoder.encode(oArray[0].toString(), StandardCharsets.UTF_8.toString()) + ","));

								}

								break;

							case Range:
								references.peek().append(key).append("=VALUE,");
								break;

							default:
								throw new DynamicValueeException(key, "Invalid multiplicity '" + val.multiplicity.name() + "' detected!");

						}
					}
				}


				if (i == (attributeIdentifiers.size() - 1)) {
					rval.add(references.pop().toString());

					if (!stackArrayIndices.isEmpty()) {
						i = stackArrayIndices.lastKey() - 1;
					}
				}
			}
			return rval;
		} catch (UnsupportedEncodingException e) {
			throw AdaptationnException.internal(e);
		}
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public DynamicObjectContainer duplicate() {
		DynamicObjectContainer newDoc = new DynamicObjectContainer(identifier, debugLabel);
		for (Map.Entry<String, DynamicValue> attrEntry : entrySet()) {
			newDoc.put(attrEntry.getKey(), attrEntry.getValue().duplicate());
		}
		return newDoc;
	}
}
