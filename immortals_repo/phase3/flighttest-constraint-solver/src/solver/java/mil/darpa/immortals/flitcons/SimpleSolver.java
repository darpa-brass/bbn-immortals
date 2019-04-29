package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValue;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueException;
import mil.darpa.immortals.flitcons.datatypes.dynamic.Range;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;

import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
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

			FileWriter fw = new FileWriter(new File("simplesolver-output.json"));
			difGson.toJson(solution, fw);
			fw.flush();
			fw.close();
			return solution;

		} catch (DynamicValueException | IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static class GroupingsContainer {
		public final Set<GroupingsChildContainer> allParentContainers = new HashSet<>();

		public DynamicObjectContainer produceSolution(@Nonnull GroupingsContainer inputConfiguration,
		                                              @Nonnull Map<String, Configuration.Calculation> calculations) throws DynamicValueException {
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
								result = inventoryChildContainer.attemptCreateReplacement(inputGroup, inputValue, calculations);
								if (result != null) {
									parent = inventoryChildContainer.parent;
									break;
								}
							}

							if (result == null) {
								throw new RuntimeException("Could not find a solution for grouping '" + inputGroup + "'!");
							}

							oldChildOldParentMap.put(inputValue, inputParent);
							newOldObjectMap.put(result, inputValue);
							Set<DynamicObjectContainer> resultChildren = resultParentChildren.computeIfAbsent(parent, k -> new HashSet<>());
							resultChildren.add(result);
						}
					}
				}
			}

			List<DynamicObjectContainer> parents = new LinkedList<>(resultParentChildren.keySet());

			DynamicObjectContainer doc = new DynamicObjectContainer(HierarchicalIdentifier.UNDEFINED);
			Object[] parentArray = new Object[parents.size()];
			doc.put(PARENT_LABEL, DynamicValue.fromValueArray(HierarchicalIdentifier.UNDEFINED, parentArray));

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

				parentArray[i] = DynamicValue.fromSingleValue(newParent.identifier, newParent);

				newParent.put("Port", DynamicValue.fromValueArray(newParent.identifier, children.toArray()));
			}

			return doc;
		}

		public static GroupingsContainer create(DynamicObjectContainer dauInventory, Map<String, Configuration.Calculation> calculations) {
			GroupingsContainer rval = new GroupingsContainer();

			Set<DynamicObjectContainer> parentSet = dauInventory.get(PARENT_LABEL).parseDynamicObjectContainerArray();
			for (DynamicObjectContainer parent : parentSet) {
				GroupingsChildContainer targetChildContainer = new GroupingsChildContainer(parent);
				rval.allParentContainers.add(targetChildContainer);

				for (DynamicObjectContainer child : parent.get(CHILD_LABEL).parseDynamicObjectContainerArray()) {
					Set<String> calculationValues = calculations.get(CHILD_LABEL).substitutionValues;
					Set<String> groupingValues = child.createGroupingHashes(calculationValues);
					for (String value : groupingValues) {
						targetChildContainer.add(value, child);
					}
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

		public void add(@Nonnull String group, @Nonnull DynamicObjectContainer value) {
			groupValueMap.computeIfAbsent(group, k -> new LinkedList<>()).add(value);
			valueGroupMap.computeIfAbsent(value, k -> new LinkedList<>()).add(group);
		}

		private DynamicObjectContainer get(@Nonnull String group) {
			List<DynamicObjectContainer> candidates = groupValueMap.get(group);
			if (candidates.size() > 0) {
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

		public DynamicObjectContainer attemptCreateReplacement(@Nonnull String grouping,
		                                                       @Nonnull DynamicObjectContainer original,
		                                                       @Nonnull Map<String, Configuration.Calculation> calculations) throws DynamicValueException {
			DynamicObjectContainer candidate = get(grouping);
			if (candidate == null) {
				return null;
			}
			return attemptCreateChildContainer(original, candidate, calculations);
		}

		public Map<String, Long> attemptValueComputation(@Nonnull DynamicObjectContainer originalChildContainer,
		                                                 @Nonnull DynamicObjectContainer inventoryContainer,
		                                                 @Nonnull Configuration.Calculation calculation) {

			// Get the valid ranges
			Map<String, Range> validSubstitutionRanges =
					originalChildContainer.entrySet().stream().filter(x -> calculation.substitutionValues.contains(x.getKey()))
							.collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().parseLongRange()));

			Optional<Map.Entry<String, DynamicValue>> tmpRange = originalChildContainer.entrySet().stream().filter(x -> calculation.targetValueIdentifier.equals(x.getKey())).findFirst();
			if (!tmpRange.isPresent()) {
				throw new RuntimeException("The targetValueIdentifier in the calculation was not found!");
			}

			Range validTargetRange = tmpRange.get().getValue().parseLongRange();

			// Get the potential values
			Map<String, List<Long>> potentialValues =
					inventoryContainer.keySet().stream().filter(x -> calculation.substitutionValues.contains(x))
							.collect(Collectors.toMap(x -> x, x -> new ArrayList<>(inventoryContainer.get(x).parseLongSingleValueOrSet())));


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
						expression = expression.replace(key, Long.toString(combination.get(key)));
					}
					try {
						Object output = javascriptEngine.eval(expression);
						Long value;

						if (output instanceof Integer) {
							value = Long.parseLong(Integer.toString((Integer) output));

						} else if (output instanceof Long) {
							value = (Long) output;

						} else {
							throw new RuntimeException("Unexpected output '" + output + "' from javascript evaluation of calculation '" + expression + "'!");
						}

						if (validTargetRange.fits(value)) {
							combination.put(calculation.targetValueIdentifier, value);
							return combination;
						}
					} catch (ScriptException e) {
						throw new RuntimeException(e);
					}

				}

			}
			return null;
		}


		public DynamicObjectContainer attemptCreateChildContainer(@Nonnull DynamicObjectContainer originalContainer,
		                                                          @Nonnull DynamicObjectContainer inventoryContainer,
		                                                          @Nonnull Map<String, Configuration.Calculation> calculations) throws DynamicValueException {
			Map<String, Long> calculatedValues = new HashMap<>();

			if (calculations.containsKey(originalContainer.identifier.getNodeType())) {
				Configuration.Calculation calculation = calculations.get(originalContainer.identifier.getNodeType());
				calculatedValues = attemptValueComputation(originalContainer, inventoryContainer, calculation);
			}

			DynamicObjectContainer newContainer = inventoryContainer.duplicate();

			for (Map.Entry<String, DynamicValue> entry : newContainer.entrySet()) {
				String key = entry.getKey();
				DynamicValue value = entry.getValue();

				if (calculatedValues.containsKey(key)) {
					newContainer.put(key, DynamicValue.fromSingleValue(inventoryContainer.get(key).dataSource, calculatedValues.remove(key)));

				} else if (value.getValue() instanceof Object[]) {
					Set<Object> valueSet = new HashSet<>(Arrays.asList((Object[]) value.getValue()));

					DynamicValue desiredValue = originalContainer.get(key);

					if (!valueSet.contains(desiredValue.getValue())) {
						throw new RuntimeException("Value '" + desiredValue + "' for attribute '" + key + "' Does not match the corresponding value from the inventory!");
					} else {
						newContainer.put(key, DynamicValue.fromSingleValue(inventoryContainer.get(key).dataSource, desiredValue));
					}

				} else if (value.getValue() instanceof Range) {
					throw new RuntimeException("Ranges not supported in DAU Inventory at this time!");
				}
			}

			// If any remaining, add them to the value
			for (String key : calculatedValues.keySet()) {
				newContainer.put(key, DynamicValue.fromSingleValue(inventoryContainer.identifier, calculatedValues.get(key)));

			}

			DynamicValue supersededId = originalContainer.get(GLOBALLY_UNIQUE_ID);
			newContainer.put(SUPERSEDED_GLOBALLY_UNIQUE_ID, DynamicValue.fromSingleValue(supersededId.dataSource, supersededId.parseString()));

			return newContainer;
		}
	}

}
