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
import java.util.*;
import java.util.stream.Collectors;

import static mil.darpa.immortals.flitcons.Utils.*;

public class SimpleSolver implements SolverInterface {


	private static final String SWAP_REQUEST = "simple-swap-request.json";
	private static final String SWAP_INVENTORY = "simple-swap-inventory.json";
	private static final String SWAP_RESPONSE = "simple-swap-response.json";

	private DynamicObjectContainer dauInventory;

	private DynamicObjectContainer inputConfiguration;

	public SimpleSolver() {
	}

	@Override
	public void loadData(@Nonnull DynamicObjectContainer inputConfiguration, @Nonnull DynamicObjectContainer inventory) {
		this.inputConfiguration = inputConfiguration;
		this.dauInventory = inventory;

		DynamicObjectContainer inputConfigurationClone = inputConfiguration.duplicate();
		DynamicObjectContainer inventoryClone = inventory.duplicate();

		try {
			EnvironmentConfiguration.storeFile(SWAP_REQUEST, Utils.difGson.toJson(inputConfigurationClone).getBytes());
			EnvironmentConfiguration.storeFile(SWAP_INVENTORY, Utils.difGson.toJson(inventoryClone).getBytes());
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

			String outValue = difGson.toJson(solution);
			String outputPath = EnvironmentConfiguration.storeFile(SWAP_RESPONSE, outValue.getBytes());

			FileReader fr = new FileReader(new File(outputPath));
			return difGson.fromJson(fr, DynamicObjectContainer.class);

		} catch (DynamicValueeException e) {
			throw AdaptationnException.input(e);

		} catch (Exception e) {
			throw AdaptationnException.internal(e);
		}
	}

	public static class GroupingsContainer {
		public final Set<GroupingsChildContainer> allParentContainers = new HashSet<>();

		public DynamicObjectContainer produceSolution(@Nonnull GroupingsContainer inputConfiguration) {
			Map<DynamicObjectContainer, Set<DynamicObjectContainer>> resultParentChildren = new HashMap<>();
			Map<DynamicObjectContainer, DynamicObjectContainer> newOldObjectMap = new HashMap<>();
			Map<DynamicObjectContainer, DynamicObjectContainer> oldChildOldParentMap = new HashMap<>();

			for (GroupingsChildContainer inputChildContainer : inputConfiguration.allParentContainers) {
				DynamicObjectContainer inputParent = inputChildContainer.parent;

				if (inputParent.containsKey(FLAGGED_FOR_REPLACEMENT)) {

					for (String inputGroup : inputChildContainer.getGroups()) {
						for (DynamicObjectContainer inputValue : inputChildContainer.getValues(inputGroup)) {

							DynamicObjectContainer result = null;
							DynamicObjectContainer parent = null;
							for (GroupingsChildContainer inventoryChildContainer : allParentContainers) {
								try {
									result = inventoryChildContainer.attemptCreateAndRemoveReplacement(inputGroup, inputValue);
									if (result != null) {
										parent = inventoryChildContainer.parent;
										break;
									}
								} catch (DynamicValueeException e) {
									e.addPathParent(inputParent.identifier.toString());
									throw AdaptationnException.internal(e);
								}
							}

							if (result == null) {
								System.err.println("Could not produce a solution for grouping '" + inputGroup + "'!");
								return null;
							}

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
			} catch (DynamicValueeException e) {
				throw AdaptationnException.internal(e);
			}

		}

		public static GroupingsContainer create(DynamicObjectContainer dauInventory) throws DynamicValueeException {
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
						} catch (DynamicValueeException e) {
							e.addPathParent(child.identifier.toString());
							throw e;
						}
					}
				} catch (DynamicValueeException e) {
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

		private DynamicObjectContainer get(@Nonnull String group) {
			List<DynamicObjectContainer> candidates = groupValueMap.get(group);
			if (candidates != null && candidates.size() > 0) {
				return candidates.get(0);
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
		                                                                             @Nonnull DynamicObjectContainer originalContainer) throws DynamicValueeException {
			DynamicObjectContainer candidateInventoryContainer = get(grouping);
			if (candidateInventoryContainer == null) {
				return null;
			}


			DynamicObjectContainer rval = attemptCreateAndRemoveChildContainer(originalContainer, candidateInventoryContainer);
			if (rval != null) {
				remove(candidateInventoryContainer);
			}
			return rval;
		}

		public synchronized Map<String, Long> attemptValueComputations(@Nonnull DynamicObjectContainer originalChildContainer,
		                                                               @Nonnull DynamicObjectContainer inventoryContainer) throws DynamicValueeException {

			Map<String, Long> rval = new HashMap<>();

			Map<String, Equation> equationsMap = inventoryContainer.children.keySet().stream()
					.filter(x -> inventoryContainer.children.get(x).getValue() instanceof Equation)
					.collect(Collectors.toMap(x -> x, x -> (Equation) inventoryContainer.children.get(x).getValue()));

//			Set<Equation> equations = inventoryContainer.children.values().stream().filter(x -> x.getValue() instanceof Equation).map(x -> (Equation) x.getValue()).collect(Collectors.toSet());

			for (String targetVariable : equationsMap.keySet()) {
				Equation equation = equationsMap.get(targetVariable);
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

				Set<Map<String, String>> tempValueCombinationSet;
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
						tempValueCombinationSet = valueCombinationSet;
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
						if (rangeMap.get(variableName).fits(Long.parseLong(variableValue))) {
							javascriptEquation = javascriptEquation.replaceAll("@" + entry.getKey(), variableValue);
							potentialValues.put(entry.getKey(), variableValue);
						} else {
							executeEquation = false;
							break;
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
								throw new DynamicValueeException(inventoryContainer.identifier.toString() + "." + targetVariable,
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
				if (!rval.containsKey(targetVariable)) {
					throw new AdaptationnException(ResultEnum.AdaptationUnsuccessful, "Could not find valid values matching the constraints for a '" + targetVariable + "' usage!");
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

				} else {
					// Or if it is not a reserved value and either has the wrong multiplicity or the value is not equal, add it as an incorrect value
					DynamicValue candidateValue = candidateInventoryContainer.get(key);
					DynamicValue previousValue = originalContainer.get(key);

					if (candidateValue.multiplicity != previousValue.multiplicity || (!key.equals(GLOBALLY_UNIQUE_ID) && !candidateValue.getValue().equals(previousValue.getValue()))) {
						missingOrInvalidValues.add(key);
					}
				}
			}

			return missingOrInvalidValues;
		}

		public synchronized DynamicObjectContainer attemptCreateAndRemoveChildContainer(@Nonnull DynamicObjectContainer originalContainer,
		                                                                                @Nonnull DynamicObjectContainer candidateInventoryContainer) throws DynamicValueeException {
			try {
				// Determine attributes that are not suitable in their current form
				Set<String> problematicValues = determineMissingOrInvalidValues(originalContainer, candidateInventoryContainer);

				// Create a possible result container
				DynamicObjectContainer possibleResult = candidateInventoryContainer.duplicate();

				// Calculate the values for any attributes that have corresponding resolution strategies
				Map<String, Long> calculatedValues = attemptValueComputations(originalContainer, candidateInventoryContainer);

				// Then for each problematic value, try to make it work.
				for (String key : problematicValues) {
					DynamicValue candidateValue = candidateInventoryContainer.get(key);

					// And it has a calculated value
					if (calculatedValues.containsKey(key)) {
						Long calculatedValue = calculatedValues.get(key);

						// If the value is null, return null as the value could not be calculated
						if (calculatedValue == null) {
							return null;
						} else {
							// Otherwise, update it in the possible result and remove it from the problematic values list
							possibleResult.put(key, DynamicValue.fromSingleValue(possibleResult.identifier, calculatedValues.get(key)));
						}

					} else if (candidateValue.getValue() instanceof Object[]) {
						// Otherwise if the original value is a set, validate the chosen value is part of that set and then replace the set with the chosen value
						Set<Object> valueSet = new HashSet<>(Arrays.asList((Object[]) candidateValue.getValue()));

						DynamicValue desiredValue = originalContainer.get(key);

						if (!valueSet.contains(desiredValue.getValue())) {
							throw new DynamicValueeException(candidateInventoryContainer.identifier.toString() + "." + key, "Value '" + key + "' Does not match the corresponding value from the inventory!");
						} else {
							possibleResult.put(key, DynamicValue.fromSingleValue(candidateInventoryContainer.get(key).dataSource, desiredValue));
						}

					} else if (candidateValue.getValue() instanceof Range) {
						// Otherwise, If the original value is a range, it is not supported and throw an exception
						throw new DynamicValueeException(candidateInventoryContainer.identifier.toString() + "." + key, "Ranges not supported in DAU Inventory at this time!");

					} else {
						// Otherwise, we have no resolution strategy
						throw new DynamicValueeException(candidateInventoryContainer.identifier.toString() + "." + key, "No resolution strategy could be found!");

					}
				}

				// Add the tagging information
				DynamicValue supersededId = originalContainer.get(GLOBALLY_UNIQUE_ID);
				possibleResult.put(SUPERSEDED_GLOBALLY_UNIQUE_ID, DynamicValue.fromSingleValue(supersededId.dataSource, supersededId.parseString()));

				return possibleResult;
			} catch (DynamicValueeException e) {
				e.addPathParent("{original=" + originalContainer.identifier.toString() + ", replacement=" + candidateInventoryContainer.identifier.toString() + "}");
				throw e;
			}
		}
	}

}
