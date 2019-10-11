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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ValidationDataContainer {

	public static final Logger logger = LoggerFactory.getLogger(ValidationDataContainer.class);

	public final HierarchicalIdentifier identifier;

	public final String name;

	private final TreeMap<String, ValidationData> children = new TreeMap<>();

	private final DebugData debugData;

	private static final int maxLabelDepth = Configuration.getInstance().validation.labelDepth;

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
	 * @throws NestedPathException if an exception occurs
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

		debugData = source.debugData;
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

	private static void fillChartData(@Nonnull ValidationDataContainer container, @Nullable List<String> parentLabelData,
	                                  @Nonnull Utils.ChartData chartData) throws NestedPathException {
		for (Map.Entry<String, ValidationData> child : container.children.entrySet()) {
			ValidationData data = child.getValue();
			if (data.value instanceof ValidationDataContainer) {
				throw new NestedPathException(data.name, "Directly nested ValidationDataContainers are not supported!");

			} else if (data.value instanceof Object[] && ((Object[]) data.value)[0] instanceof ValidationDataContainer) {
				Object[] values = (Object[]) data.value;

				// TODO: Add check to configuration to validate if multiple values are valid
				Map<String, List<ValidationDataContainer>> sortedChildValues = new HashMap<>();

				for (Object value : values) {
					if (!(value instanceof ValidationDataContainer)) {
						throw new NestedPathException(data.name, "All values in the array must be of the same type!");
					}
					ValidationDataContainer containerValue = (ValidationDataContainer) value;

					List<ValidationDataContainer> objectList =
							sortedChildValues.computeIfAbsent(
									containerValue.identifier.getNodeType(), k -> new LinkedList<>());

					objectList.add(containerValue);

				}

				for (List<ValidationDataContainer> containerList : sortedChildValues.values()) {
					int idx = 0;
					for (ValidationDataContainer containerValue : containerList) {
						List<String> labelData;
						if (parentLabelData == null) {
							labelData = new LinkedList<>();
						} else {
							labelData = new LinkedList<>(parentLabelData);
						}
						if (labelData.size() < maxLabelDepth) {
							String addition = containerValue.debugData != null ? containerValue.debugData.toString() : containerValue.name;
							if (containerList.size() > 0) {
								addition += ("[" + idx++ + "]");
							}
							if (addition != null) {
								labelData.add(addition);
							}
						}
						fillChartData(containerValue, labelData, chartData);
					}
				}

			} else {
				if (parentLabelData == null) {
					throw new NestedPathException(data.name, "Missing label data for validation charts!");
				}
				Map<String, Object> valueRow = chartData.rowColumnData.computeIfAbsent(String.join("/", parentLabelData), k -> new TreeMap<>());
				Map<String, Boolean> validityRow = chartData.validityMap.computeIfAbsent(String.join("/", parentLabelData), k -> new TreeMap<>());

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
					if (valueRow.containsKey(data.name) && validityRow.get(data.name) != null) {
						throw new NestedPathException(data.name, "Cannot overwrite existing data '" + valueRow.get(data.name) + "' with '" + data.value + "'!");
					}
					valueRow.put(data.name, sb.toString());

				} else {
					if (valueRow.containsKey(data.name) && valueRow.get(data.name) != null && data.value != null) {
						throw new NestedPathException(data.name, "Cannot clobber value '" + valueRow.get(data.name) + "' with '" + data.value + "'!");
					}
					valueRow.put(data.name, data.value == null ? "" : data.value.toString());
				}
				validityRow.put(data.name, data.isValid());
			}
		}
	}

	public List<String> makeResultsChart(@Nonnull String title, boolean useBasicDisplayScheme) {
		try {
			Utils.ChartData chartData = new Utils.ChartData(title + " Validation Result", useBasicDisplayScheme);
			fillChartData(this, null, chartData);
			return Utils.makeChart(chartData);
		} catch (NestedPathException e) {
			throw AdaptationnException.internal(e);
		}
	}

	public boolean isValid() {
		Utils.ChartData cd = new Utils.ChartData("DUMMY CHART", true);
		try {
			fillChartData(this, null, cd);

			for (Map.Entry<String, Map<String, Boolean>> rowEntry : cd.validityMap.entrySet()) {
				for (Map.Entry<String, Boolean> columnEntry : rowEntry.getValue().entrySet()) {
					if (!columnEntry.getValue()) {
						return false;
					}
				}
			}
			return true;
		} catch (NestedPathException e) {
			throw AdaptationnException.internal(e);
		}
	}

	public void printResults(@Nonnull String scenarioTitle) {
		boolean isBasic = EnvironmentConfiguration.isBasicDisplayMode();
		try {
			List<String> displayLines = makeResultsChart(scenarioTitle, true);
			if (isBasic) {
				for (String str : displayLines) {
					logger.info(str);
				}
			} else {
				String targetFile = EnvironmentConfiguration.storeFile(
						"invalid_fields-" + scenarioTitle.replaceAll(" ", "_") + ".txt",
						String.join("\n", displayLines).getBytes());

				displayLines = makeResultsChart(scenarioTitle, false);

				for (String str : displayLines) {
					logger.info(str);
				}
				logger.info("A monochrome version of the above chart has been been written to the file '" + targetFile + "'.");
			}
		} catch (Exception e) {
			throw AdaptationnException.internal(e);
		}
	}
}
