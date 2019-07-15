package mil.darpa.immortals.flitcons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.flitcons.datatypes.dynamic.*;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.Immutable;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static mil.darpa.immortals.flitcons.Utils.Ansi.*;

/**
 * Created by awellman@bbn.com on 1/11/18.
 */
public class Utils {

	public static class Sym {
		public static final String LT = " < ";
		public static final String NLT = " ≮ "; //"</";
		public static final String GT = " > ";
		public static final String NGT = " ≯ "; //">/";
		public static final String LTE = " ≤ "; //"<=";
		public static final String NLTE = " ≰ "; //"<=/";
		public static final String GTE = " ≥ "; //">=";
		public static final String NGTE = " ≱ "; //">=/";
		public static final String EE = " ∈ ";
		public static final String NEE = " ∉ ";
		public static final String TE = " ∃ ";
		public static final String NTE = " ∄ ";
		public static final String EQT = " ≍ ";
		public static final String NEQT = " ≭ ";
	}

	public static class Ansi {
		public static final String RED_FG = "31";
		public static final String RED_BG = "41";
		public static final String GREEN_FG = "32";
		public static final String GREEN_BG = "42";
	}

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

	public static <T> LinkedList<T> duplicateList(List<T> source) {
		LinkedList<T> rval = new LinkedList<>();

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

	public static List<String> makeChart(@Nonnull TreeMap<String, Map<String, Object>> rowColumnData,
	                                     @Nullable TreeMap<String, Map<String, Boolean>> passing,
	                                     @Nullable TreeMap<String, Map<String, Boolean>> hideMap) {
		return makeChart(rowColumnData, passing, hideMap, null);
	}

	public static List<String> makeChart(@Nonnull TreeMap<String, Map<String, Object>> rowColumnData,
	                                     @Nullable TreeMap<String, Map<String, Boolean>> passing,
	                                     @Nullable TreeMap<String, Map<String, Boolean>> hideMap,
	                                     @Nullable String title) {
		// First gather some information on all known columns and their max necessary width
		TreeMap<String, Integer> columnSizeMap = new TreeMap<>();
		int rowZeroSize = 0;


		int maxColumns = 0;
		for (Map.Entry<String, Map<String, Object>> topEntry : rowColumnData.entrySet()) {
			rowZeroSize = Math.max(rowZeroSize, topEntry.getKey().length());

			Map<String, Object> v = topEntry.getValue();
			for (Map.Entry<String, Object> entry : v.entrySet()) {
				maxColumns = Math.max(maxColumns, v.size());
				String columnName = entry.getKey();
				int knownMaxStringSize = columnSizeMap.computeIfAbsent(columnName, k -> columnName.length());
				String entryString = entry.getValue().toString();
				int maxStringSize = Math.max(entry.getKey().length(), entryString.length());
				if (maxStringSize > knownMaxStringSize) {
					columnSizeMap.put(columnName, maxStringSize);
				}
			}
		}

		// Then fill in the missing columns for each row so that it will result in a proper Y*X grid
		for (Map<String, Object> row : rowColumnData.values()) {
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

		if (title != null) {
			rval.add(Utils.padCenter(title, header.length(), '#'));
		}
		rval.add(header.toString());

		// Then convert them to rows
		for (String rowKey : rowColumnData.keySet()) {
			Map<String, Object> rowValues = rowColumnData.get(rowKey);

			StringBuilder sb = new StringBuilder("| " + padRight(rowKey, rowZeroSize + 1) + "|");

			for (String columnKey : columnSizeMap.keySet()) {
				Object columnValue = rowValues.get(columnKey);
				boolean showValue = (hideMap == null || (
						hideMap.get(rowKey).get(columnKey) != null &&
								!hideMap.get(rowKey).get(columnKey)));
				String valueString = showValue ? columnValue.toString() : "";

				String colorString;

				if (passing == null) {
					colorString = null;
					if (showValue && valueString.trim().equals("")) {
						valueString = "MISSING";
					}

				} else {
					Boolean pass = passing.get(rowKey).get(columnKey);


					boolean hasValue = !(valueString == null || valueString.equals(""));

					if (pass == null) {
						if (hasValue) {
							throw AdaptationnException.internal("String values in the chart must have a corresponding ANSI color code!");
						} else {
							colorString = null;
						}

					} else {
						String ansiCode;

						if (pass) {
							if (hasValue) {
								ansiCode = GREEN_FG;
							} else {
								ansiCode = GREEN_BG;
							}
						} else {
							if (hasValue) {
								ansiCode = RED_FG;
							} else {
								ansiCode = RED_BG;
							}
						}
						colorString = (char) 27 + "[" + ansiCode + "m";
					}
				}

				if (colorString == null) {
					sb.append(" ")
							.append(padRight(valueString, columnSizeMap.get(columnKey) + 1))
							.append("|");
				} else {

					sb.append(" ")
							.append(colorString)
							.append(padRight(valueString, columnSizeMap.get(columnKey) + 1))
							.append((char) 27 + "[0m")
							.append("|");
				}
			}
			rval.add(sb.toString());
		}
		rval.add(Utils.padCenter("#", header.length(), '#'));

		return rval;
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
