package mil.darpa.immortals.flitcons.datatypes.dynamic;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static mil.darpa.immortals.flitcons.Utils.GLOBALLY_UNIQUE_ID;

public class DynamicObjectContainer extends TreeMap<String, DynamicValue> implements DuplicateInterface<DynamicObjectContainer> {

	public final HierarchicalIdentifier identifier;

	private final int hashCode = UUID.randomUUID().hashCode();

	public final TreeMap<String, DynamicValue> debugAttributes = new TreeMap<>();

//	public final String name;

	public DynamicObjectContainer(HierarchicalIdentifier identifier) {
		super();
		this.identifier = identifier;
	}

	public DynamicObjectContainer(HierarchicalIdentifier identifier, SortedMap<String, ? extends DynamicValue> sortedMap) {
		super(sortedMap);
		this.identifier = identifier;
	}

	public Set<String> createGroupingHashes(@Nonnull Collection<String> equationValues) {
		try {

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
						references.peek().append(key).append("=").append("VALUE");
					} else {

						DynamicValue val = get(key);

						switch (val.multiplicity) {

							case SingleValue:
								references.peek().append(key).append("=").append(URLEncoder.encode(val.getValue().toString(), StandardCharsets.UTF_8.toString())).append(",");
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
								break;

							default:
								throw new RuntimeException("Invalid multiplicity '" + val.multiplicity.name() + "' detected!");

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
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public DynamicObjectContainer duplicate() {
		DynamicObjectContainer newDoc = new DynamicObjectContainer(identifier);
		for (Map.Entry<String, DynamicValue> attrEntry : entrySet()) {
			newDoc.put(attrEntry.getKey(), attrEntry.getValue().duplicate());
		}
		return newDoc;
	}
}
