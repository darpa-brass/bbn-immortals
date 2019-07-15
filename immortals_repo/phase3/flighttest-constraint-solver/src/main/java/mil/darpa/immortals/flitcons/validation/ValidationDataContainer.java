package mil.darpa.immortals.flitcons.validation;

import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.NestedPathException;
import mil.darpa.immortals.flitcons.Utils;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValue;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueMultiplicity;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ValidationDataContainer {

	public static final String COLORLESS_FLAG = "--colorless-mode";

	public final HierarchicalIdentifier identifier;

	public final String name;

	private final TreeMap<String, ValidationData> children = new TreeMap<>();

	private final String debugIdentifier;

	public ValidationData get(@Nonnull String value) {
		return children.get(value);
	}

	private void putData(@Nonnull String key, @Nullable DynamicValue val, @Nonnull Map<String, ValidationData> targetContainer, @Nonnull Configuration.ValidationConfiguration configuration) throws NestedPathException {
		if (val == null) {
			targetContainer.put(key, new ValidationData(this, key, DynamicValueMultiplicity.NullValue, null));

		} else if (val.multiplicity == DynamicValueMultiplicity.Set && val.valueArray.length > 0 && val.valueArray[0] instanceof DynamicObjectContainer) {
			Object[] arr = new Object[val.valueArray.length];
			Class clazz = val.valueArray[0].getClass();
			for (int i = 0; i < val.valueArray.length; i++) {
				if (!val.valueArray[i].getClass().isAssignableFrom(clazz)) {
					throw new NestedPathException(key, "All values in the array must be of the same type!");
				}
				arr[i] = new ValidationDataContainer((DynamicObjectContainer) val.valueArray[i], configuration, true);
			}
			targetContainer.put(key, new ValidationData(this, key, val.multiplicity, arr));

		} else {
			targetContainer.put(key, new ValidationData(this, key, val.multiplicity, val.getValue()));
		}
	}

	public static ValidationDataContainer createContainer(@Nonnull DynamicObjectContainer source, @Nonnull Configuration.ValidationConfiguration configuration) throws NestedPathException {
		return new ValidationDataContainer(source, configuration, false);

	}

	/**
	 * @param source        The source of the data
	 * @param configuration The configuration to use when validating
	 * @param isWellDefined whether or not it is fully defined with a formal node type and source
	 * @throws NestedPathException
	 */
	private ValidationDataContainer(@Nonnull DynamicObjectContainer source, @Nonnull Configuration.ValidationConfiguration configuration, boolean isWellDefined) throws NestedPathException {

		Map<String, Set<String>> bootstrapData = configuration.defaultPropertyList;

		this.identifier = source.identifier;

		for (String key : source.keySet()) {
			putData(key, source.get(key), children, configuration);
		}

		if (isWellDefined) {
			name = identifier.getNodeType();
			if (bootstrapData.containsKey(identifier.getNodeType())) {
				for (String value : bootstrapData.get(identifier.getNodeType())) {

					if (!children.containsKey(value)) {
						try {
							children.put(value, new ValidationData(this, value, DynamicValueMultiplicity.NullValue, null));
						} catch (NestedPathException e) {
							e.addPathParent(identifier.getNodeType());
							throw e;
						}
					}
				}
			}
		} else {
			name = null;
		}

		debugIdentifier = source.debugLabel;
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
	                                  @Nonnull TreeMap<String, Map<String, Object>> valueContainer,
	                                  @Nonnull TreeMap<String, Map<String, Boolean>> validityContainer) {
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
						throw AdaptationnException.input("All values in the array must be of the same type!");
					}

				}

			} else {
				Map<String, Object> valueRow = valueContainer.computeIfAbsent(parent, k -> new TreeMap<>());
				Map<String, Boolean> validityRow = validityContainer.computeIfAbsent(parent, k -> new TreeMap<>());

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

	private static void makeResultMapOld(@Nonnull ValidationDataContainer container, @Nullable String parent,
	                                     @Nonnull TreeMap<String, TreeMap<String, Object>> valueContainer,
	                                     @Nonnull TreeMap<String, TreeMap<String, Boolean>> validityContainer) {
		for (Map.Entry<String, ValidationData> child : container.children.entrySet()) {
			ValidationData data = child.getValue();

			if (data.value instanceof ValidationDataContainer) {
				String name = parent == null ? child.getKey() : parent + "." + child.getKey();
				makeResultMapOld((ValidationDataContainer) data.value, name, valueContainer, validityContainer);

			} else if (data.value instanceof Object[] && ((Object[]) data.value)[0] instanceof ValidationDataContainer) {
				Object[] values = (Object[]) data.value;

				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					StringBuilder sb = new StringBuilder(parent == null ? child.getKey() : parent + "." + child.getKey());

					if (value instanceof ValidationDataContainer) {
						ValidationDataContainer containerValue = (ValidationDataContainer) value;

						makeResultMapOld((ValidationDataContainer) value, sb.toString() + "[" +
								(containerValue.debugIdentifier == null ? i : (i + "(" + containerValue.debugIdentifier + ")")) +
								"]", valueContainer, validityContainer);

					} else {
						throw AdaptationnException.input("All values in the array must be of the same type!");
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
		TreeMap<String, Map<String, Object>> valueMap = new TreeMap<>();
		TreeMap<String, Map<String, Boolean>> validityMap = new TreeMap<>();
		makeResultMap(this, null, valueMap, validityMap);
		return Utils.makeChart(valueMap, null, validityMap);
	}

	private List<String> makeCombinedColorChart() {
		TreeMap<String, Map<String, Object>> valueMap = new TreeMap<>();
		TreeMap<String, Map<String, Boolean>> validityMap = new TreeMap<>();
		makeResultMap(this, null, valueMap, validityMap);
		return Utils.makeChart(valueMap, validityMap, null);
	}

	public boolean isValid() {
		TreeMap<String, Map<String, Object>> valueMap = new TreeMap<>();
		TreeMap<String, Map<String, Boolean>> validityMap = new TreeMap<>();
		makeResultMap(this, null, valueMap, validityMap);

		for (Map.Entry<String, Map<String, Boolean>> rowEntry : validityMap.entrySet()) {
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

			String invalidValuesFile = "validator-" + scenarioTitle.replaceAll(" ", "_") + ".txt";
			List<String> resultLines = makeCombinedColorlessChart();
			String targetFile = EnvironmentConfiguration.storeFile(
					"invalid_fields-" + scenarioTitle.replaceAll(" ", "_") + ".txt",
					String.join("\n", resultLines).getBytes());

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

			System.out.println("The invalid value results for this execution have also been written to the file '" + targetFile + "'.");
		} catch (
				Exception e) {
			throw AdaptationnException.internal(e);
		}
	}
}
