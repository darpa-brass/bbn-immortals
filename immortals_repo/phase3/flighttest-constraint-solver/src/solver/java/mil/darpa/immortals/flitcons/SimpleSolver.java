package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValue;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueeException;
import mil.darpa.immortals.flitcons.datatypes.dynamic.Range;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;

import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static mil.darpa.immortals.flitcons.Utils.*;

public class SimpleSolver implements SolverInterface {

	private DynamicObjectContainer dauInventory;

	private DynamicObjectContainer inputConfiguration;

	public SimpleSolver() {
	}

	@Override
	public void loadData(@Nonnull DynamicObjectContainer inputConfiguration, @Nonnull DynamicObjectContainer inventory) {
		this.inputConfiguration = inputConfiguration;
		this.dauInventory = inventory;
	}

	@Override
	public DynamicObjectContainer solve() {
		try {
			Map<String, Configuration.Calculation> calculations = Configuration.getInstance().adaptation.calculations;

			GroupingsContainer dauInventoryPorts = GroupingsContainer.create(dauInventory, calculations);

			GroupingsContainer inputConfigurationPorts = GroupingsContainer.create(inputConfiguration, calculations);

			DynamicObjectContainer solution = dauInventoryPorts.produceSolution(inputConfigurationPorts, calculations);

			if (solution == null) {
				throw new AdaptationnException(ResultEnum.AdaptationUnsuccessful, "Could not produce a solution!");
			}

			FileWriter fw = new FileWriter(new File("simplesolver-output.json"));
			difGson.toJson(solution, fw);
			fw.flush();
			fw.close();

			FileReader fr = new FileReader(new File("simplesolver-output.json"));
			return difGson.fromJson(fr, DynamicObjectContainer.class);

		} catch (DynamicValueeException e) {
			throw AdaptationnException.input(e);

		} catch (IOException e) {
			throw AdaptationnException.internal(e);
		}
	}

	public static class GroupingsContainer {
		public final Set<GroupingsChildContainer> allParentContainers = new HashSet<>();

		public DynamicObjectContainer produceSolution(@Nonnull GroupingsContainer inputConfiguration,
		                                              @Nonnull Map<String, Configuration.Calculation> calculations) {
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
									result = inventoryChildContainer.attemptCreateAndRemoveReplacement(inputGroup, inputValue, calculations);
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

		public static GroupingsContainer create(DynamicObjectContainer dauInventory, Map<String, Configuration.Calculation> calculations) throws DynamicValueeException {
			GroupingsContainer rval = new GroupingsContainer();

			Set<DynamicObjectContainer> parentSet = dauInventory.get(PARENT_LABEL).parseDynamicObjectContainerArray();
			for (DynamicObjectContainer parent : parentSet) {
				try {
					GroupingsChildContainer targetChildContainer = new GroupingsChildContainer(parent);
					rval.allParentContainers.add(targetChildContainer);

					for (DynamicObjectContainer child : parent.get(CHILD_LABEL).parseDynamicObjectContainerArray()) {
						Set<String> calculationValues = calculations.get(CHILD_LABEL).substitutionValues;
						try {
							Set<String> groupingValues = child.createGroupingHashes(calculationValues);
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
		                                                                             @Nonnull DynamicObjectContainer originalContainer,
		                                                                             @Nonnull Map<String, Configuration.Calculation> calculations) throws DynamicValueeException {
			DynamicObjectContainer candidateInventoryContainer = get(grouping);
			if (candidateInventoryContainer == null) {
				return null;
			}


			DynamicObjectContainer rval = attemptCreateAndRemoveChildContainer(originalContainer, candidateInventoryContainer, calculations);
			if (rval != null) {
				remove(candidateInventoryContainer);
			}
			return rval;
		}

		public synchronized Map<String, Long> attemptValueComputations(@Nonnull DynamicObjectContainer originalChildContainer,
		                                                               @Nonnull DynamicObjectContainer inventoryContainer,
		                                                               @Nonnull Configuration.Calculation calculation) throws DynamicValueeException {
			// Get the valid ranges
			Map<String, Range> validSubstitutionRanges =
					originalChildContainer.entrySet().stream().filter(x -> calculation.substitutionValues.contains(x.getKey()))
							.collect(
									Collectors.toMap(
											Map.Entry::getKey,
											x -> {
												try {
													return x.getValue().parseLongRange();
												} catch (DynamicValueeException e) {
													throw AdaptationnException.internal(e);
												}
											}));

			Optional<Map.Entry<String, DynamicValue>> tmpRange = originalChildContainer.entrySet().stream().filter(x -> calculation.targetValueIdentifier.equals(x.getKey())).findFirst();
			if (!tmpRange.isPresent()) {
				throw new DynamicValueeException(inventoryContainer.identifier.toString() + "." + calculation.targetValueIdentifier,
						"Superseded node '" + originalChildContainer.identifier.toString() + "Does not contain this attribute!");
			}

			Range validTargetRange = tmpRange.get().getValue().parseLongRange();

			// Get the potential values
			Map<String, List<Long>> potentialValues =
					inventoryContainer.keySet().stream().filter(x -> calculation.substitutionValues.contains(x))
							.collect(Collectors.toMap(x -> x, x ->
							{
								try {
									return new ArrayList<>(inventoryContainer.get(x).parseLongSingleValueOrSet());
								} catch (DynamicValueeException e) {
									throw AdaptationnException.internal(e);
								}
							}));


			// Generate the possible combinations of values

			List<Map<String, Long>> valueCombinationList = new LinkedList<>();

			for (String key : potentialValues.keySet()) {
				if (valueCombinationList.isEmpty()) {
					for (Long value : potentialValues.get(key)) {
						Map<String, Long> newMap = new HashMap<>();
						newMap.put(key, value);
						valueCombinationList.add(newMap);
					}
				} else {
					List<Map<String, Long>> oldList = valueCombinationList;
					List<Map<String, Long>> newList = new LinkedList<>();

					for (Long value : potentialValues.get(key)) {
						for (Map<String, Long> oldMap : oldList) {
							Map<String, Long> newMap = new HashMap<>(oldMap);
							newMap.put(key, value);
							newList.add(newMap);
						}
					}
					valueCombinationList = newList;
				}
			}

			// Test the possible combinations of values
			for (Map<String, Long> combination : valueCombinationList) {
				boolean isValid = true;
				for (String key : validSubstitutionRanges.keySet()) {
					if (!validSubstitutionRanges.get(key).fits(combination.get(key))) {
						isValid = false;
						break;

					}

				}
				if (isValid) {
					String expression = calculation.equation;

					for (String key : validSubstitutionRanges.keySet()) {
						expression = expression.replace("@" + key, Long.toString(combination.get(key)));
					}
					try {
						Object output = javascriptEngine.eval(expression);
						Long value;

						if (output instanceof Integer) {
							value = Long.parseLong(Integer.toString((Integer) output));

						} else if (output instanceof Long) {
							value = (Long) output;

						} else {
							throw new DynamicValueeException(inventoryContainer.identifier.toString() + "." + calculation.targetValueIdentifier,
									"Unexpected output '" + output + "' from javascript evaluation of calculation '" + expression + "'!");
						}

						if (validTargetRange.fits(value)) {
							combination.put(calculation.targetValueIdentifier, value);
							return combination;
						} else {
							combination.put(calculation.targetValueIdentifier, null);
						}
					} catch (ScriptException e) {
						throw AdaptationnException.internal(e);
					}
				}

			}

			Map<String, Long> rval = new HashMap<>();
			rval.put(calculation.targetValueIdentifier, null);
			for (String value : calculation.substitutionValues) {
				rval.put(value, null);
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
		                                                                                @Nonnull DynamicObjectContainer candidateInventoryContainer,
		                                                                                @Nonnull Map<String, Configuration.Calculation> calculations) throws DynamicValueeException {

			try {
				// Determine attributes that are not suitable in their current form
				Set<String> problematicValues = determineMissingOrInvalidValues(originalContainer, candidateInventoryContainer);

				// Create a possible result container
				DynamicObjectContainer possibleResult = candidateInventoryContainer.duplicate();

				// Calculate the values for any attributes that have corresponding resolution strategies
				Map<String, Long> calculatedValues = new HashMap<>();
				if (calculations.containsKey(originalContainer.identifier.getNodeType())) {
					Configuration.Calculation calculation = calculations.get(originalContainer.identifier.getNodeType());
					calculatedValues = attemptValueComputations(originalContainer, candidateInventoryContainer, calculation);
				}

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
