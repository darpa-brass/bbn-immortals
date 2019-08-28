package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.datatypes.dynamic.*;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;

import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static mil.darpa.immortals.flitcons.Utils.*;

public class SimpleSolver implements SolverInterface<SimpleSolver> {


	private static final String SWAP_REQUEST = "simple-swap-request.json";
	private static final String SWAP_INVENTORY = "simple-swap-inventory.json";
	private static final String SWAP_RESPONSE = "simple-swap-response.json";

	private DynamicObjectContainer dauInventory;

	private static boolean verboseLogging = false;

	private DynamicObjectContainer inputConfiguration;

	public SimpleSolver() {
		verboseLogging = System.getenv().containsKey("mil.darpa.immortals.simplesolver.verbose");
	}

	public SimpleSolver loadData(@Nonnull AbstractDataSource dataSource) throws NestedPathException {
		this.inputConfiguration = DynamicObjectContainerFactory.create(dataSource.getInterconnectedTransformedFaultyConfiguration(false));
		this.dauInventory = DynamicObjectContainerFactory.create(dataSource.getTransformedDauInventory(false));

		try {
			EnvironmentConfiguration.storeFile(SWAP_REQUEST, Utils.difGson.toJson(inputConfiguration).getBytes());
			EnvironmentConfiguration.storeFile(SWAP_INVENTORY, Utils.difGson.toJson(dauInventory).getBytes());
		} catch (Exception e) {
			throw AdaptationnException.internal(e);
		}
		return this;
	}

	@Override
	public DynamicObjectContainer solveFromJsonFiles(@Nonnull Path inputJsonFile, @Nonnull Path inventoryJsonFile) {
		try {
			this.inputConfiguration = difGson.fromJson(new FileReader(inputJsonFile.toFile()), DynamicObjectContainer.class);
			this.dauInventory = difGson.fromJson(new FileReader(inventoryJsonFile.toFile()), DynamicObjectContainer.class);
			return this.solve();
		} catch (Exception e) {
			throw AdaptationnException.internal(e);
		}
	}

	@Override
	public DynamicObjectContainer solve() {
		try {
			GroupingsContainer dauInventoryPorts = GroupingsContainer.create(dauInventory);

			GroupingsContainer inputConfigurationPorts = GroupingsContainer.create(inputConfiguration);

			DynamicObjectContainer solution = dauInventoryPorts.produceSolution(inputConfigurationPorts);

			if (solution == null) {
				throw new AdaptationnException(ResultEnum.AdaptationUnsuccessful, "Could not produce a solution!");
			}

			System.out.println("Solution found.");

			String outValue = difGson.toJson(solution);
			try {
				String outputPath = EnvironmentConfiguration.storeFile(SWAP_RESPONSE, outValue.getBytes());

				FileReader fr = new FileReader(new File(outputPath));
				System.out.println("Solution found.");
				return difGson.fromJson(fr, DynamicObjectContainer.class);
			} catch (Exception e) {
				throw AdaptationnException.internal(e);
			}

		} catch (NestedPathException e) {
			throw AdaptationnException.input(e);
		}
	}

	public static class GroupingsContainer {
		public final Set<GroupingsChildContainer> allParentContainers = new HashSet<>();

		public DynamicObjectContainer produceSolution(@Nonnull GroupingsContainer inputConfiguration) {
			Map<DynamicObjectContainer, Set<DynamicObjectContainer>> resultParentChildren = new HashMap<>();
			Map<DynamicObjectContainer, DynamicObjectContainer> newOldObjectMap = new HashMap<>();
			Map<DynamicObjectContainer, DynamicObjectContainer> oldChildOldParentMap = new HashMap<>();
			Set<GroupingsChildContainer> inventoryParentContainers = allParentContainers;
			Set<GroupingsChildContainer> inputParentContainers = inputConfiguration.allParentContainers;

			// For each input DAU
			dauIterator:
			for (GroupingsChildContainer inputChildContainer : inputParentContainers) {
				System.out.println("Testing DAU...");
				DynamicObjectContainer inputParent = inputChildContainer.parent;

				if (inputParent.containsKey(FLAGGED_FOR_REPLACEMENT)) {

					// For each Input DAU Port
					for (String inputGroup : inputChildContainer.getGroups()) {
						List<DynamicObjectContainer> inputGroupValues = inputChildContainer.getValues(inputGroup);
						System.out.println("\tTesting Input Group '" + inputGroup + "' with " + inputGroupValues.size() + " children");
						portGroup:
						for (DynamicObjectContainer inputValue : inputGroupValues) {

							DynamicObjectContainer result = null;
							DynamicObjectContainer parent = null;
							// For each Inventory DAU
							for (GroupingsChildContainer inventoryChildContainer : inventoryParentContainers) {
								try {
									// Try to get a matching Port
									result = inventoryChildContainer.attemptCreateAndRemoveReplacement(inputGroup, inputValue);
									if (result != null) {
										parent = inventoryChildContainer.parent;
										break;
									}
								} catch (NestedPathException e) {
									e.addPathParent(inputParent.identifier.toString());
									throw AdaptationnException.internal(e);
								}
							}

							if (result == null) {
								return null;
							}

							System.out.println("\t\tMatch found");
							oldChildOldParentMap.put(inputValue, inputParent);
							newOldObjectMap.put(result, inputValue);
							Set<DynamicObjectContainer> resultChildren = resultParentChildren.computeIfAbsent(parent, k -> new HashSet<>());
							resultChildren.add(result);
						}
					}
				}
			}

			try {
				List<DynamicObjectContainer> parents = new LinkedList<>(resultParentChildren.keySet());
				if (parents.size() == 0) {
					return null;
				}

				DynamicObjectContainer doc = new DynamicObjectContainer(HierarchicalIdentifier.createBlankNode(), null);
				Object[] parentArray = new Object[parents.size()];
				doc.put(PARENT_LABEL, DynamicValue.fromValueArray(HierarchicalIdentifier.createBlankNode(), parentArray));

				for (int i = 0; i < parents.size(); i++) {
					DynamicObjectContainer inventoryParent = parents.get(i);
					DynamicObjectContainer newParent = inventoryParent.duplicate();

					TreeSet<String> supersededParents = new TreeSet<>();
					HashSet<DynamicObjectContainer> children = new HashSet<>();
					newParent.remove(CHILD_LABEL);
					for (DynamicObjectContainer newChild : resultParentChildren.get(inventoryParent)) {
						DynamicObjectContainer oldChild = newOldObjectMap.get(newChild);
						supersededParents.add(oldChildOldParentMap.get(oldChild).get(GLOBALLY_UNIQUE_ID).parseString());
						children.add(newChild);
					}

					newParent.put(SUPERSEDED_GLOBALLY_UNIQUE_IDS, DynamicValue.fromValueArray(newParent.identifier, supersededParents.toArray()));

					parentArray[i] = newParent;

					newParent.put("Port", DynamicValue.fromValueArray(newParent.identifier, children.toArray()));
				}

				return doc;
			} catch (NestedPathException e) {
				throw AdaptationnException.internal(e);
			}

		}

		public static GroupingsContainer create(DynamicObjectContainer dauInventory) throws NestedPathException {
			GroupingsContainer rval = new GroupingsContainer();

			Set<DynamicObjectContainer> parentSet = dauInventory.get(PARENT_LABEL).parseDynamicObjectContainerArray();
			for (DynamicObjectContainer parent : parentSet) {
				try {
					GroupingsChildContainer targetChildContainer = new GroupingsChildContainer(parent);
					rval.allParentContainers.add(targetChildContainer);

					for (DynamicObjectContainer child : parent.get(CHILD_LABEL).parseDynamicObjectContainerArray()) {
						try {
							Set<String> groupingValues = child.createGroupingHashes();
							for (String value : groupingValues) {
								targetChildContainer.add(value, child);
							}
						} catch (NestedPathException e) {
							e.addPathParent(child.identifier.toString());
							throw e;
						}
					}
				} catch (NestedPathException e) {
					e.addPathParent(parent.identifier.toString());
					throw e;
				}
			}
			return rval;
		}

	}

	public static class GroupingsChildContainer {

		private static final ScriptEngine javascriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");

		public final DynamicObjectContainer parent;
		private final TreeMap<String, List<DynamicObjectContainer>> groupValueMap = new TreeMap<>();
		private final HashMap<DynamicObjectContainer, List<String>> valueGroupMap = new HashMap<>();

		public GroupingsChildContainer(@Nonnull DynamicObjectContainer parent) {
			this.parent = parent;
		}

		public synchronized void add(@Nonnull String group, @Nonnull DynamicObjectContainer value) {
			groupValueMap.computeIfAbsent(group, k -> new LinkedList<>()).add(value);
			valueGroupMap.computeIfAbsent(value, k -> new LinkedList<>()).add(group);
		}

		public synchronized void remove(@Nonnull DynamicObjectContainer value) {
			for (List<DynamicObjectContainer> valueList : groupValueMap.values()) {
				valueList.remove(value);
			}
			valueGroupMap.remove(value);
		}

		private List<DynamicObjectContainer> get(@Nonnull String group) {
			List<DynamicObjectContainer> candidates = groupValueMap.get(group);
			if (candidates != null && candidates.size() > 0) {
				return candidates;
			}
			return null;
		}

		public Set<String> getGroups() {
			return groupValueMap.keySet();
		}

		public List<DynamicObjectContainer> getValues(@Nonnull String group) {
			return groupValueMap.get(group);
		}

		public synchronized DynamicObjectContainer attemptCreateAndRemoveReplacement(@Nonnull String grouping,
		                                                                             @Nonnull DynamicObjectContainer originalContainer) throws NestedPathException {
			// TODO: This area is probably key for doing an exhaustive search that is not greedy
			List<DynamicObjectContainer> candidateInventoryContainers = get(grouping);
			if (candidateInventoryContainers == null) {
				return null;
			}

			if (verboseLogging) {
				System.out.println("\t\tFound " + candidateInventoryContainers.size() + " matching groups...");
			}

			for (DynamicObjectContainer candidateInventoryContainer : candidateInventoryContainers) {
				DynamicObjectContainer rval = attemptCreateAndRemoveChildContainer(originalContainer, candidateInventoryContainer);
				if (rval != null) {
					if (verboseLogging) {
						System.out.println("\t\tMatch Found!");
					}
					remove(candidateInventoryContainer);
					return rval;
				}
			}
			return null;
		}

		public synchronized Map<HierarchicalIdentifier, Map<String, Long>> attemptConvolutedValueComputations(
				@Nonnull DynamicObjectContainer originalChildContainer,
				@Nonnull DynamicObjectContainer inventoryContainer) throws NestedPathException {

			Map<HierarchicalIdentifier, Map<String, Long>> rval = new HashMap<>();

			DynamicObjectContainer originalChildMeasurementContainer = null;
			List<DynamicObjectContainer> portMeasurementContainers = new LinkedList<>();

			if (originalChildContainer.children.containsKey("Measurement")) {
				DynamicValue measurementValue = originalChildContainer.children.get("Measurement");
				if (measurementValue.multiplicity == DynamicValueMultiplicity.Set) {
					if (measurementValue.valueArray.length == 1) {
						originalChildMeasurementContainer = (DynamicObjectContainer) measurementValue.valueArray[0];

					} else {
						throw new NestedPathException("Measurement", "Expected only a single value!");
					}
				} else if (measurementValue.multiplicity == DynamicValueMultiplicity.SingleValue) {
					originalChildMeasurementContainer = (DynamicObjectContainer) measurementValue.singleValue;

				} else {
					throw new NestedPathException("Measurement", "Unexpected value multiplicity '" + measurementValue.multiplicity + "'!");
				}
			}

			if (inventoryContainer.children.containsKey("Measurement")) {
				DynamicValue measurementValue = inventoryContainer.children.get("Measurement");
				if (measurementValue.multiplicity == DynamicValueMultiplicity.Set) {
					for (Object value : measurementValue.valueArray) {
						portMeasurementContainers.add((DynamicObjectContainer) value);
					}
				} else if (measurementValue.multiplicity == DynamicValueMultiplicity.SingleValue) {
					portMeasurementContainers.add((DynamicObjectContainer) measurementValue.singleValue);
				}
			}

			if (originalChildMeasurementContainer == null || portMeasurementContainers.isEmpty()) {
				return null;
			} else {

				Iterator<DynamicObjectContainer> portMeasurementIterator = portMeasurementContainers.iterator();
				while (portMeasurementIterator.hasNext()) {
					DynamicObjectContainer inventoryMeasurementContainer = portMeasurementIterator.next();
					Map<String, Long> valueComputations = attemptValueComputations(originalChildMeasurementContainer, inventoryMeasurementContainer);

					if (valueComputations != null && valueComputations.size() > 0) {
						rval.put(inventoryMeasurementContainer.identifier, valueComputations);
					}
				}
			}
			if (rval.size() > 0) {
				return rval;
			}
			return null;
		}

		public synchronized Map<String, Long> attemptValueComputations(@Nonnull DynamicObjectContainer originalChildContainer,
		                                                               @Nonnull DynamicObjectContainer inventoryContainer) throws NestedPathException {

			Map<String, Long> rval = new HashMap<>();

			// TODO: This shouldn't be hard coded...
			Map<String, String> equationsMap = inventoryContainer.children.keySet().stream()
					.filter(x -> inventoryContainer.children.get(x).getValue().equals("@SampleRate * @DataLength"))
					.collect(Collectors.toMap(x -> x, x -> (String) inventoryContainer.children.get(x).getValue()));

			for (String targetVariable : equationsMap.keySet()) {
				Equation equation = new Equation(equationsMap.get(targetVariable));
				Set<String> variables = equation.getVariables();
				Range targetRange = originalChildContainer.children.get(targetVariable).parseLongRange();

				Map<String, List<String>> valueMap = new HashMap<>();
				Map<String, Range> rangeMap = new HashMap<>();

				for (String variable : variables) {
					if (!inventoryContainer.children.containsKey(variable)) {
						throw new RuntimeException("The equation does not have a variable!");
					} else if (!originalChildContainer.children.containsKey(variable) && originalChildContainer.children.get(variable).getValue() instanceof Range) {
						throw new RuntimeException("Could not find a range value for '" + variable + "' in input configuration!");
					}


					rangeMap.put(variable, (Range) originalChildContainer.children.get(variable).getValue());

					DynamicValue value = inventoryContainer.get(variable);

					switch (value.multiplicity) {

						case Range:
							throw AdaptationnException.input("Found a range value within the inventory! Ranges are only valid in the input configuration!");

						case NullValue:
							throw AdaptationnException.input("Found a null value within the inventory for '" + variable + "'!");

						case SingleValue:
							valueMap.computeIfAbsent(variable, k -> new LinkedList<>()).add(value.getValue().toString());
							break;

						case Set:
							valueMap.computeIfAbsent(variable, k -> new LinkedList<>()).addAll(
									value.parseLongArray().stream().map(Object::toString).collect(Collectors.toSet()));
							break;
					}
				}

				Set<Map<String, String>> valueCombinationSet = new HashSet<>();

				for (String key : valueMap.keySet()) {

					if (valueMap.get(key).size() == 1) {
						if (valueCombinationSet.isEmpty()) {
							Map<String, String> tmpMap = new HashMap<>();
							tmpMap.put(key, valueMap.get(key).get(0));
							valueCombinationSet.add(tmpMap);

						} else {
							for (Map<String, String> map : valueCombinationSet) {
								map.put(key, valueMap.get(key).get(0));
							}
						}

					} else {
						Set<Map<String, String>> tempValueCombinationSet = valueCombinationSet;
						valueCombinationSet = new HashSet<>();

						for (String value : valueMap.get(key)) {
							if (tempValueCombinationSet.isEmpty()) {
								Map<String, String> innerValueMap = new HashMap<>();
								innerValueMap.put(key, value);
								valueCombinationSet.add(innerValueMap);

							} else {
								for (Map<String, String> val : tempValueCombinationSet) {
									Map<String, String> tmpMap = new HashMap<>(val);
									tmpMap.put(key, value);
									valueCombinationSet.add(new HashMap<>(tmpMap));
								}
							}
						}
					}
				}

				for (Map<String, String> valueSet : valueCombinationSet) {
					String javascriptEquation = equation.Equation;
					Map<String, String> potentialValues = new HashMap<>();
					boolean executeEquation = true;

					for (Map.Entry<String, String> entry : valueSet.entrySet()) {
						String variableName = entry.getKey();
						String variableValue = entry.getValue();
						try {
							if (rangeMap.get(variableName).fits(Long.parseLong(variableValue))) {
								javascriptEquation = javascriptEquation.replaceAll("@" + entry.getKey(), variableValue);
								potentialValues.put(entry.getKey(), variableValue);
							} else {
								executeEquation = false;
								break;
							}
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					if (executeEquation) {
						try {
							Object output = javascriptEngine.eval(javascriptEquation);
							Long value;

							if (output instanceof Integer) {
								value = Long.parseLong(Integer.toString((Integer) output));

							} else if (output instanceof Long) {
								value = (Long) output;

							} else {
								throw new NestedPathException(inventoryContainer.identifier.toString() + "." + targetVariable,
										"Unexpected output '" + output + "' from javascript evaluation of calculation '" + javascriptEquation + "'!");
							}

							if (targetRange.fits(value)) {
								rval.put(targetVariable, value);
								for (Map.Entry<String, String> variableValue : potentialValues.entrySet()) {
									rval.put(variableValue.getKey(), Long.parseLong(variableValue.getValue()));
								}
								break;
							}
						} catch (ScriptException e) {
							throw AdaptationnException.internal(e);
						}
					}
				}
			}

			return rval;
		}

		public Set<String> determineMissingOrInvalidValues(@Nonnull DynamicObjectContainer originalContainer,
		                                                   @Nonnull DynamicObjectContainer candidateInventoryContainer) {
			Set<String> missingOrInvalidValues = new HashSet<>();

			// For each original value
			for (String key : originalContainer.keySet()) {
				// If there isn't a corresponding value in the result, add it as a missing value
				if (!candidateInventoryContainer.containsKey(key)) {
					missingOrInvalidValues.add(key);
					if (verboseLogging) {
						System.out.println("\t\t\t\t Value '" + key + "' is missing needs resolving.");
					}

				} else {
					// Or if it is not a reserved value and either has the wrong multiplicity or the value is not equal, add it as an incorrect value
					DynamicValue candidateValue = candidateInventoryContainer.get(key);
					DynamicValue previousValue = originalContainer.get(key);

					if (candidateValue.multiplicity != previousValue.multiplicity || (!key.equals(GLOBALLY_UNIQUE_ID) && !candidateValue.getValue().equals(previousValue.getValue()))) {
						if (verboseLogging) {
							System.out.println("\t\t\t\t Value '" + key + "' doesn't have a straightforward mapping and needs resolving.");
						}
						missingOrInvalidValues.add(key);
					}
				}
			}

			return missingOrInvalidValues;
		}

		public synchronized DynamicObjectContainer attemptCreateAndRemoveChildContainer(@Nonnull DynamicObjectContainer originalInputContainer,
		                                                                                @Nonnull DynamicObjectContainer candidateInventoryContainer) throws NestedPathException {
			if (verboseLogging) {
				System.out.println("\t\t\t Testing candidate group...");
			}
			try {
				// Determine attributes that are not suitable in their current form
				Set<String> problematicValues = determineMissingOrInvalidValues(originalInputContainer, candidateInventoryContainer);

				// Create a possible result container
				DynamicObjectContainer possibleResult = candidateInventoryContainer.duplicate();

				// Calculate the values for any attributes that have corresponding resolution strategies
				// TODO: How to handle multiple possible values?
				Map<HierarchicalIdentifier, Map<String, Long>> calculatedValuesMap = attemptConvolutedValueComputations(originalInputContainer, candidateInventoryContainer);
				HierarchicalIdentifier candidateValueIdentifier = (calculatedValuesMap == null || calculatedValuesMap.size() == 0) ? null : calculatedValuesMap.keySet().iterator().next();
				Map<String, Long> calculatedValues = candidateValueIdentifier == null ? null : calculatedValuesMap.get(candidateValueIdentifier);

				// Then for each problematic value, try to make it work.
				for (String key : problematicValues) {
					DynamicValue candidateValue = candidateInventoryContainer.get(key);

					// If the value is a measurement
					if (key.equals("Measurement")) {
						if (candidateValueIdentifier == null) {
							if (verboseLogging) {
								System.out.println("\t\t\t\t\tValue is a measurement and no calculations could be found. Fail.");
							}
							// But no calculated value could be found, return null since it could not be calculated
							return null;
						} else {
							// Otherwise, update it in the possible result and remove it from the problematic values list
							DynamicValue originalMeasurementDynamicValue = originalInputContainer.get(key);
							DynamicObjectContainer target = (DynamicObjectContainer) (originalMeasurementDynamicValue.singleValue != null ? originalMeasurementDynamicValue.singleValue : originalMeasurementDynamicValue.valueArray[0]);

							// Otherwise, get the matching candidate object
							DynamicValue candidateMeasurementDynamicValue = possibleResult.get("Measurement");
							Optional<Object> candidateObject = Arrays.stream(candidateMeasurementDynamicValue.valueArray).filter(x -> x instanceof DynamicObjectContainer && ((DynamicObjectContainer) x).identifier.equals(candidateValueIdentifier)).findFirst();
							DynamicObjectContainer candidate = (DynamicObjectContainer) candidateObject.orElse(null);

							for (String valueKey : calculatedValues.keySet()) {
								candidate.put(valueKey, DynamicValue.fromSingleValue(candidate.identifier, calculatedValues.get(valueKey)));
							}

							possibleResult.remove("Measurement");
							possibleResult.put("Measurement", DynamicValue.fromSingleValue(target.identifier, candidate));
							if (verboseLogging) {
								System.out.println("\t\t\t\tValue 'Measurement' resolved.");
							}

							DynamicValue possibleMeasurementDynamicValue = possibleResult.get("Measurement");
							if (possibleMeasurementDynamicValue.singleValue == null && (possibleMeasurementDynamicValue.valueArray == null || possibleMeasurementDynamicValue.valueArray.length != 1)) {
								throw AdaptationnException.internal("Unexpected value multiplicity for the input Measurement value!");
							}
						}
					}


					// And it has a calculated value
					else if (calculatedValues != null && calculatedValues.containsKey(key)) {
						Long calculatedValue = calculatedValues.get(key);

						// If the value is null, return null as the value could not be calculated
						if (calculatedValue == null) {
							return null;
						} else {
							// Otherwise, update it in the possible result and remove it from the problematic values list
							possibleResult.put(key, DynamicValue.fromSingleValue(possibleResult.identifier, calculatedValues.get(key)));
							if (verboseLogging) {
								System.out.println("\t\t\t\tValue '" + key + "' resolved.");
							}
						}

					} else if (candidateValue.getValue() instanceof Object[]) {
						// Otherwise if the original value is a set, validate the chosen value is part of that set and then replace the set with the chosen value
						Set<Object> valueSet = new HashSet<>(Arrays.asList((Object[]) candidateValue.getValue()));

						DynamicValue desiredValue = originalInputContainer.get(key);

						if (!valueSet.contains(desiredValue.getValue())) {
							throw new NestedPathException(candidateInventoryContainer.identifier.toString() + "." + key, "Value '" + key + "' Does not match the corresponding value from the inventory!");
						} else {
							possibleResult.put(key, DynamicValue.fromSingleValue(candidateInventoryContainer.get(key).dataSource, desiredValue));
							if (verboseLogging) {
								System.out.println("\t\t\t\tValue '" + key + "' resolved.");
							}
						}

					} else if (candidateValue.getValue() instanceof Range) {
						// Otherwise, If the original value is a range, it is not supported and throw an exception
						throw new NestedPathException(candidateInventoryContainer.identifier.toString() + "." + key, "Ranges not supported in DAU Inventory at this time!");

					} else {
						// Otherwise, we have no resolution strategy
						throw new NestedPathException(candidateInventoryContainer.identifier.toString() + "." + key, "No resolution strategy could be found!");

					}
				}

				// Add the tagging information
				DynamicValue supersededId = originalInputContainer.get(GLOBALLY_UNIQUE_ID);
				possibleResult.put(SUPERSEDED_GLOBALLY_UNIQUE_ID, DynamicValue.fromSingleValue(supersededId.dataSource, supersededId.parseString()));

				return possibleResult;
			} catch (NestedPathException e) {
				e.addPathParent("{original=" + originalInputContainer.identifier.toString() + ", replacement=" + candidateInventoryContainer.identifier.toString() + "}");
				throw e;
			}
		}
	}

}
