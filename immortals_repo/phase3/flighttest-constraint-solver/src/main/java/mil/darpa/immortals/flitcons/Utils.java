package mil.darpa.immortals.flitcons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.flitcons.datatypes.dynamic.*;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.*;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by awellman@bbn.com on 1/11/18.
 */
public class Utils {
	public static final String GLOBALLY_UNIQUE_ID = "GloballyUniqueId";
	public static final String SUPERSEDED_GLOBALLY_UNIQUE_ID = "SupersededGloballyUniqueId";
	public static final String SUPERSEDED_GLOBALLY_UNIQUE_IDS = "SupersededGloballyUniqueIds";

	public static final String PARENT_LABEL = "daus";
	public static final String CHILD_LABEL = "Port";
	public static final String FLAGGED_FOR_REPLACEMENT = "BBNDauFlaggedForReplacement";

	public static final String ATTRIBUTE_DEBUG_LABEL_IDENTIFIER = "a3ca039f1626_debugLabel";

	private static final Gson gson;
	private static final Gson nonHtmlEscapingGson;

	public static Gson getGson() {
		return gson;
	}

	public static Gson getNonHtmlEscapingGson() {
		return nonHtmlEscapingGson;
	}

	public static final Gson difGson;

	static {
		GsonBuilder builder = new GsonBuilder();
		gson = builder.setPrettyPrinting().create();

		builder = new GsonBuilder();
		nonHtmlEscapingGson = builder.setPrettyPrinting().disableHtmlEscaping().create();

		difGson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(DynamicValue.class, new DynamicValueSerializer())
				.registerTypeAdapter(DynamicValue.class, new DynamicValueDeserializer())
				.registerTypeAdapter(DynamicObjectContainer.class, new DynamicObjectContainerSerializer())
				.registerTypeAdapter(DynamicObjectContainer.class, new DynamicObjectContainerDeserializer())
				.create();

	}

	public static <T> T duplicateObject(T obj) {
		if (obj instanceof Integer || obj instanceof Long || obj instanceof Boolean || obj instanceof Double ||
				obj instanceof Float || obj instanceof String || obj instanceof Class ||
				obj.getClass().isAnnotationPresent(Immutable.class)) {
			return obj;

		} else if (obj instanceof DuplicateInterface) {
			return (T) ((DuplicateInterface<?>) obj).duplicate();

		} else if (obj instanceof List<?>) {
			List<?> objList = (List) obj;
			return (T) new LinkedList<>(objList);

		} else {
			throw AdaptationnException.internal(
					"Cannot duplicate object that does not extend DuplicateInterface or declare itself to be " +
							"immutable with the 'mil.darpa.immortals.flitcons.datatypes.hierarchical.Immutable' annotation!");
		}
	}

	public static <T, V> Map<T, Set<V>> duplicateSetMap(Map<T, Set<V>> source) {
		Map<T, Set<V>> rval = new HashMap<>();

		for (Map.Entry<T, Set<V>> nodeDataEntry : source.entrySet()) {
			Set<V> duplicateData = new HashSet<>();

			rval.put(duplicateObject(nodeDataEntry.getKey()), duplicateData);

			for (V vd : nodeDataEntry.getValue()) {
				duplicateData.add(duplicateObject(vd));
			}
		}
		return rval;
	}

	public static <T, V> Map<T, List<V>> duplicateListMap(Map<T, List<V>> source) {
		Map<T, List<V>> rval = new HashMap<>();

		for (Map.Entry<T, List<V>> nodeDataEntry : source.entrySet()) {
			List<V> duplicateData = new LinkedList<>();

			rval.put(duplicateObject(nodeDataEntry.getKey()), duplicateData);

			for (V vd : nodeDataEntry.getValue()) {
				duplicateData.add(duplicateObject(vd));
			}
		}
		return rval;
	}

	public static <T> List<List<T>> duplicateListList(List<List<T>> source) {
		List<List<T>> rval = new LinkedList<>();
		for (List<T> sourceData : source) {
			rval.add(duplicateObject(sourceData));
		}
		return rval;
	}

	public static <T, V> LinkedHashMap<T, V> duplicateMap(Map<T, V> source) {
		LinkedHashMap<T, V> rval = new LinkedHashMap<>();

		for (Map.Entry<T, V> entry : source.entrySet()) {
			rval.put(duplicateObject(entry.getKey()), duplicateObject(entry.getValue()));
		}
		return rval;
	}

	public static <T> Set<T> duplicateSet(Set<T> source) {
		Set<T> rval = new HashSet<>();

		for (T value : source) {
			rval.add(duplicateObject(value));
		}
		return rval;
	}

	public static String repeat(char character, int count) {
		return new String(new char[count]).replace("\0", String.valueOf(character));
	}

	private static String padRight(@Nonnull String str, int finalLength) {
		return str + repeat(' ', (finalLength - str.length()));
	}

	public static String padCenter(@Nonnull String value, int finalLength, char paddingChar) {
		int emptySpace = finalLength - value.length();

		if (emptySpace % 2 == 0) {
			return repeat(paddingChar, emptySpace / 2) + value + repeat(paddingChar, emptySpace / 2);
		} else {
			return repeat(paddingChar, emptySpace / 2) + value + repeat(paddingChar, 1 + emptySpace / 2);

		}
	}

	public static List<String> makeChart(@Nonnull TreeMap<String, TreeMap<String, Object>> rowColumnData,
	                                     @Nullable TreeMap<String, TreeMap<String, String>> ansiColorCodes,
	                                     @Nullable TreeMap<String, TreeMap<String, Boolean>> hideMap) {
		// First gather some information on all known columns and their max necessary width
		TreeMap<String, Integer> columnSizeMap = new TreeMap<>();
		int rowZeroSize = 0;


		int maxColumns = 0;
		for (Map.Entry<String, TreeMap<String, Object>> topEntry : rowColumnData.entrySet()) {
			rowZeroSize = Math.max(rowZeroSize, topEntry.getKey().length());

			TreeMap<String, Object> v = topEntry.getValue();
			for (Map.Entry<String, Object> entry : v.entrySet()) {
				maxColumns = Math.max(maxColumns, v.size());
				String columnName = entry.getKey();
				int knownMaxStringSize = columnSizeMap.computeIfAbsent(columnName, k -> 0);
				String entryString = entry.getValue().toString();
				int maxStringSize = Math.max(entry.getKey().length(), entryString.length());
				if (maxStringSize > knownMaxStringSize) {
					columnSizeMap.put(columnName, maxStringSize);
				}
			}
		}

		// Then fill in the missing columns for each row so that it will result in a proper Y*X grid
		for (TreeMap<String, Object> row : rowColumnData.values()) {
			for (String column : columnSizeMap.keySet()) {
				if (!row.containsKey(column)) {
					row.put(column, "");
				}
			}
		}

		List<String> rval = new ArrayList<>(rowColumnData.size());

		StringBuilder header = new StringBuilder("| " + padRight("", rowZeroSize + 1) + "|");
		for (Map.Entry<String, Integer> headerColumn : columnSizeMap.entrySet()) {
			header.append(" ").append(padRight(headerColumn.getKey(), headerColumn.getValue() + 1)).append("|");
		}
		rval.add(header.toString());

		// Then convert them to rows
		for (Map.Entry<String, TreeMap<String, Object>> rowEntry : rowColumnData.entrySet()) {
			StringBuilder sb = new StringBuilder("| " + padRight(rowEntry.getKey(), rowZeroSize + 1) + "|");

			for (Map.Entry<String, Object> columnEntry : rowEntry.getValue().entrySet()) {
				boolean showValue = (hideMap == null || (
						hideMap.get(rowEntry.getKey()).get(columnEntry.getKey()) != null &&
								!hideMap.get(rowEntry.getKey()).get(columnEntry.getKey())));
				String valueString = showValue ? columnEntry.getValue().toString() : "";

				String colorString;

				if (ansiColorCodes == null) {
					colorString = null;
					if (showValue && valueString.trim().equals("")) {
						valueString = "MISSING";
					}

				} else {
					String ansiCode = ansiColorCodes.get(rowEntry.getKey()).get(columnEntry.getKey());

					if (ansiCode == null) {
						if (valueString == null || valueString.equals("")) {
							colorString = null;

						} else {
							throw AdaptationnException.internal("String values in the chart must have a corresponding ANSI color code!");
						}

					} else {
						colorString = (char) 27 + "[" + ansiCode + "m";
					}
				}

				if (colorString == null) {
					sb.append(" ")
							.append(padRight(valueString, columnSizeMap.get(columnEntry.getKey()) + 1))
							.append("|");
				} else {

					sb.append(" ")
							.append(colorString)
							.append(padRight(valueString, columnSizeMap.get(columnEntry.getKey()) + 1))
							.append((char) 27 + "[0m")
							.append("|");
				}
			}
			rval.add(sb.toString());
		}

		return rval;
	}

	private static DynamicValue parseDynamicValueFromAttribute(HierarchicalData source, String attributeName) throws DynamicValueeException {
		Object value = source.getAttribute(attributeName);

		if (value instanceof Object[]) {
			try {
				return new DynamicValue(source.node, null, (Object[]) value, null);
			} catch (DynamicValueeException e) {
				e.addPathParent(attributeName + "[*]");
				throw e;
			}
		} else if (value instanceof String || value instanceof Number || value instanceof Boolean) {
			try {
				return new DynamicValue(source.node, null, null, value);
			} catch (DynamicValueeException e) {
				e.addPathParent(attributeName);
				throw e;
			}

		} else if (value instanceof List) {
			try {
				return new DynamicValue(source.node, null, ((List) value).toArray(), null);
			} catch (DynamicValueeException e) {
				e.addPathParent(attributeName);
				throw e;
			}
		} else {
			throw new DynamicValueeException(source.node.toString(), "Unsupported attribute type '" + value.getClass().toString() + "'!");
		}
	}

	private static DynamicObjectContainer produceDynamicObjectContainer(HierarchicalData source) throws DynamicValueeException {
		DynamicObjectContainer target = new DynamicObjectContainer(source.node, source.getDebugLabel());
		boolean containsData = false;

		for (String label : source.getAttributeNames()) {
			DynamicValue dynamicValue = parseDynamicValueFromAttribute(source, label);
			target.put(label, dynamicValue);
			containsData = true;
		}

		Iterator<String> childTypeIter = source.getChildrenClassIterator();
		while (childTypeIter.hasNext()) {

			boolean containsChildData = false;

			List<Object> values = new LinkedList<>();

			String childType = childTypeIter.next();

			Iterator<HierarchicalData> childIter = source.getChildrenDataIterator(childType);
			while (childIter.hasNext()) {
				HierarchicalData data = childIter.next();

				if (data.getAttribute("Min") != null && data.getAttribute("Max") != null) {
					Object min = data.getAttribute("Min");
					Object max = data.getAttribute("Max");
					if (min == null || max == null) {
						throw new DynamicValueeException(source.toString(), "A Min cannot be defined without a Max!");
					}
					if (data.getChildrenClassIterator().hasNext()) {
						throw new DynamicValueeException(source.toString(), "A Range object cannot have child attributes!");
					}
					target.put(childType, new DynamicValue(source.node, new Range(min, max), null, null));
					containsData = true;

				} else if (data.getAttribute("Equation") != null) {
					target.put(childType, new DynamicValue(source.node, null, null, new Equation((String) data.getAttribute("Equation"))));
					containsData = true;

				} else {
					Object value = produceDynamicObjectContainer(data);
					if (value != null) {
						values.add(value);
						containsData = true;
						containsChildData = true;
					}
				}
				if (containsChildData) {
					try {
						target.put(childType, new DynamicValue(source.node, null, values.toArray(), null));
					} catch (DynamicValueeException e) {
						e.addPathParent(childType);
						throw e;
					}
				}
			}
		}

		if (containsData) {
			return target;
		} else {
			return null;
		}
	}

	public static DynamicObjectContainer createDslInterchangeFormat(HierarchicalDataContainer inputData) throws DynamicValueeException {
		DynamicObjectContainer target = new DynamicObjectContainer(HierarchicalIdentifier.createBlankNode(), null);
		Object[] daus = new Object[inputData.getDauRootNodes().size()];

		int idx = 0;

		for (HierarchicalData dau : inputData.getDauRootNodes()) {
			try {
				daus[idx++] = Utils.produceDynamicObjectContainer(dau);
			} catch (DynamicValueeException e) {
				e.addPathParent("daus");
				throw e;
			}
		}

		try {
			target.put("daus", new DynamicValue(null, null, daus, null));
		} catch (DynamicValueeException e) {
			e.addPathParent("daus");
			throw e;
		}

		return target;
	}


	public static boolean stringListContains(@Nonnull Set<String> listToValidateAgainst, @Nonnull Object object) {
		if (object instanceof Object[]) {
			Object[] objArray = (Object[]) object;
			for (Object obj : objArray) {
				if (!(obj instanceof String)) {
					throw AdaptationnException.internal("Values being tested should be Strings!");
				}
				if (!stringListContains(listToValidateAgainst, obj)) {
					return false;
				}
			}

		} else if (object instanceof Collection) {
			Collection coll = (Collection) object;
			for (Object obj : coll) {
				if (!(obj instanceof String)) {
					throw AdaptationnException.internal("Values being tested should be Strings!");
				}
				if (!stringListContains(listToValidateAgainst, obj)) {
					return false;
				}
			}

		} else {
			if (!(object instanceof String)) {
				throw AdaptationnException.internal("Values being tested should be Strings!");
			}
			if (!listToValidateAgainst.contains(object)) {
				return false;
			}
		}
		return true;
	}
}
