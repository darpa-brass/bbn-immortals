package mil.darpa.immortals.flitcons.validation;

import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.Utils;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValue;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueException;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueMultiplicity;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ValidationDataContainer {

	public static final String COLORLESS_FLAG = "--colorless-mode";

	public final HierarchicalIdentifier identifier;

	public final String name;

	private final TreeMap<String, ValidationData> children = new TreeMap<>();

	private final TreeMap<String, ValidationData> debugChildren = new TreeMap<>();

	private final String debugIdentifier;

	public ValidationData get(@Nonnull String value) {
		return children.get(value);
	}

	private void putData(@Nonnull String key, @Nullable DynamicValue val, @Nonnull Map<String, ValidationData> targetContainer, @Nonnull Configuration.ValidationConfiguration configuration) throws DynamicValueException {
		try {
			if (val == null) {
				targetContainer.put(key, new ValidationData(this, key, DynamicValueMultiplicity.NullValue, null));

			} else if (val.multiplicity == DynamicValueMultiplicity.Set && val.valueArray.length > 0 && val.valueArray[0] instanceof DynamicObjectContainer) {
				Object[] arr = new Object[val.valueArray.length];
				Class clazz = val.valueArray[0].getClass();
				for (int i = 0; i < val.valueArray.length; i++) {
					if (!val.valueArray[i].getClass().isAssignableFrom(clazz)) {
						throw new RuntimeException("All values in the array must be of the same type!");
					}
					arr[i] = new ValidationDataContainer((DynamicObjectContainer) val.valueArray[i], configuration);
				}
				targetContainer.put(key, new ValidationData(this, key, val.multiplicity, arr));

			} else {
				targetContainer.put(key, new ValidationData(this, key, val.multiplicity, val.getValue()));
			}
		} catch (DynamicValueException e) {
			throw e;
		}
	}

	public ValidationDataContainer(@Nonnull DynamicObjectContainer source, Configuration.ValidationConfiguration configuration) throws DynamicValueException {

		Map<String, Set<String>> bootstrapData = configuration.defaultPropertyList;

		this.identifier = source.identifier;
		this.name = this.identifier.getNodeType();

		for (String key : source.keySet()) {
			putData(key, source.get(key), children, configuration);
		}

		for (String key : source.debugAttributes.keySet()) {
			putData(key, source.debugAttributes.get(key), debugChildren, configuration);
		}

		if (bootstrapData.containsKey(this.name)) {
			for (String value : bootstrapData.get(this.name)) {

				if (!children.containsKey(value)) {
					try {
						children.put(value, new ValidationData(this, value, DynamicValueMultiplicity.NullValue, null));
					} catch (DynamicValueException e) {
						e.addPathParent(this.name);
						throw e;
					}
				}
			}
		}

		StringBuilder debugLabel = null;
		for (String label : configuration.debugIdentificationValues) {
			if (source.debugAttributes.containsKey(label)) {
				if (debugLabel != null) {
					debugLabel.append("/").append(source.debugAttributes.get(label).getValue().toString());
				} else {
					debugLabel = Optional.ofNullable(source.debugAttributes.get(label).getValue().toString()).map(StringBuilder::new).orElse(null);
				}

			}
		}
		debugIdentifier = (debugLabel == null ? null : debugLabel.toString());
	}

	private void collectNestedValidationData(ValidationDataContainer container, Set<ValidationData> result) {
		for (ValidationData data : container.children.values()) {
			switch (data.multiplicity) {

				case SingleValue:
					if (data.value instanceof ValidationDataContainer) {
						collectNestedValidationData((ValidationDataContainer) data.value, result);
					} else {
						result.add(data);
					}
					break;

				case Set:
					Object[] values = (Object[]) data.value;
					for (Object value : values) {
						if (value instanceof ValidationDataContainer) {
							collectNestedValidationData((ValidationDataContainer) value, result);
						} else {
							result.add(data);
						}
					}
					break;

				case Range:
				case NullValue:
					result.add(data);
					break;
			}
		}
	}

	public Set<ValidationData> getAllDataInHierarchy() {
		Set<ValidationData> rval = new HashSet<>();
		collectNestedValidationData(this, rval);
		return rval;
	}

	private static void makeResultMap(@Nonnull ValidationDataContainer container, @Nullable String parent,
	                                  @Nonnull TreeMap<String, TreeMap<String, Object>> valueContainer,
	                                  @Nonnull TreeMap<String, TreeMap<String, Boolean>> validityContainer) {
		for (Map.Entry<String, ValidationData> child : container.children.entrySet()) {
			ValidationData data = child.getValue();

			if (data.value instanceof ValidationDataContainer) {
				String name = parent == null ? child.getKey() : parent + "." + child.getKey();
				makeResultMap((ValidationDataContainer) data.value, name, valueContainer, validityContainer);

			} else if (data.value instanceof Object[] && ((Object[]) data.value)[0] instanceof ValidationDataContainer) {
				Object[] values = (Object[]) data.value;

				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					StringBuilder sb = new StringBuilder(parent == null ? child.getKey() : parent + "." + child.getKey());

					if (value instanceof ValidationDataContainer) {
						ValidationDataContainer containerValue = (ValidationDataContainer) value;

						makeResultMap((ValidationDataContainer) value, sb.toString() + "[" +
								(containerValue.debugIdentifier == null ? i : (i + "(" + containerValue.debugIdentifier + ")")) +
								"]", valueContainer, validityContainer);

					} else {
						throw new RuntimeException("All values in the array must be of the same type!");
					}

				}

			} else {
				TreeMap<String, Object> valueRow = valueContainer.computeIfAbsent(parent, k -> new TreeMap<>());
				TreeMap<String, Boolean> validityRow = validityContainer.computeIfAbsent(parent, k -> new TreeMap<>());

				if (data.value instanceof Object[]) {
					Object[] valueArray = (Object[]) data.value;
					StringBuilder sb = new StringBuilder("[");
					for (int i = 0; i < valueArray.length; i++) {
						sb.append(valueArray[i]);

						if (i + 1 != valueArray.length) {
							sb.append(",");
						}
					}
					sb.append("]");
					valueRow.put(data.name, sb.toString());

				} else {
					valueRow.put(data.name, data.value == null ? "" : data.value.toString());
				}
				validityRow.put(data.name, data.isValid());
			}
		}
	}

	private List<String> makeCombinedColorlessChart() {
		TreeMap<String, TreeMap<String, Object>> valueMap = new TreeMap<>();
		TreeMap<String, TreeMap<String, Boolean>> validityMap = new TreeMap<>();
		makeResultMap(this, null, valueMap, validityMap);
		return Utils.makeChart(valueMap, null, validityMap);
	}

	private List<String> makeCombinedColorChart() {
		TreeMap<String, TreeMap<String, Object>> valueMap = new TreeMap<>();
		TreeMap<String, TreeMap<String, Boolean>> validityMap = new TreeMap<>();
		makeResultMap(this, null, valueMap, validityMap);

		TreeMap<String, TreeMap<String, String>> ansiColorMap = new TreeMap<>();
		for (Map.Entry<String, TreeMap<String, Boolean>> rowEntry : validityMap.entrySet()) {
			String rowIdentifier = rowEntry.getKey();
			TreeMap<String, String> targetRow = new TreeMap<>();
			ansiColorMap.put(rowIdentifier, targetRow);

			for (Map.Entry<String, Boolean> columnEntry : rowEntry.getValue().entrySet()) {
				String columnIdentifier = columnEntry.getKey();

				boolean isValid = columnEntry.getValue();
				Object value = valueMap.get(rowIdentifier).get(columnIdentifier);
				boolean hasValue = value != null && !value.equals("");


				if (isValid) {
					if (hasValue) {
						targetRow.put(columnIdentifier, "32");
					} else {
						targetRow.put(columnIdentifier, "42");
					}

				} else {
					if (hasValue) {
						targetRow.put(columnIdentifier, "31");
					} else {
						targetRow.put(columnIdentifier, "41");
					}
				}
			}
		}


		return Utils.makeChart(valueMap, ansiColorMap, null);
	}

	public boolean isValid() {
		TreeMap<String, TreeMap<String, Object>> valueMap = new TreeMap<>();
		TreeMap<String, TreeMap<String, Boolean>> validityMap = new TreeMap<>();
		makeResultMap(this, null, valueMap, validityMap);

		for (Map.Entry<String, TreeMap<String, Boolean>> rowEntry : validityMap.entrySet()) {
			for (Map.Entry<String, Boolean> columnEntry : rowEntry.getValue().entrySet()) {
				if (!columnEntry.getValue()) {
					return false;
				}
			}
		}
		return true;
	}

	public void printResults(String scenarioTitle, boolean useColor) {
		try {

			File invalidValuesFile = new File(scenarioTitle + "-invalidValues.txt");

			List<String> resultLines = makeCombinedColorlessChart();
			FileUtils.writeLines(invalidValuesFile, resultLines);

			System.out.println(Utils.padCenter(scenarioTitle + " Validation Result", 80, '#'));
			if (useColor) {
				resultLines = makeCombinedColorChart();
			}
			for (String str : resultLines) {
				System.out.println(str);
			}
			System.out.println("################################################################################\n");

			if (useColor) {
				System.out.println("The above chart is intended to be displayed in an ANSI-capable terminal and values should be displayed in green or red depending on a value's validity. " +
						"If it does not display in color you can use the '" + COLORLESS_FLAG + "' flag to utilize a colorless chart.");
			}

			System.out.println("The invalid value results for this execution have also been written to the file '" + invalidValuesFile.getAbsolutePath() + "'.");
		} catch (
				IOException e) {
			throw new RuntimeException(e);
		}
	}
}
