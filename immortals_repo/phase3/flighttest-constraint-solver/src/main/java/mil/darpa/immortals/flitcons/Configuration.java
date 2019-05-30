package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.util.*;

public class Configuration {

	public static transient Configuration instance;


	public static Configuration getInstance() {
		if (instance == null) {
			instance = Utils.getGson().fromJson(
					new InputStreamReader(
							Configuration.class.getClassLoader().getResourceAsStream("configuration.json")
					),
					Configuration.class);
		}
		return instance;
	}

	/**
	 * Known types of object that will be encountered
	 */
	public static class KnownTypes {
		/**
		 * MDL DAU types we are aware of.
		 */
		public Set<String> dauTypes = new HashSet<>();

		/**
		 * Module functionality types we are aware of.
		 */
		public Set<String> moduleFunctionalityTypes = new HashSet<>();

		/**
		 * Port functionality types we are aware of.
		 */
		public Set<String> portFunctionalityTypes = new HashSet<>();
	}

	/**
	 * A calculation that must be performed to determined undefined values
	 */
	public static class Calculation implements DuplicateInterface<Calculation> {

		/**
		 * The identifier for the value that will be calculated
		 */
		public String targetValueIdentifier;

		/**
		 * The identifiers for the values that are expected to be filled in by data defined elsewhere
		 */
		public Set<String> substitutionValues;

		/**
		 * A javascript equation that consists of variables defined in {@link Calculation#substitutionValues}
		 */
		public String equation;

		@Override
		public Calculation duplicate() {
			Calculation rval = new Calculation();
			rval.targetValueIdentifier = targetValueIdentifier;
			rval.substitutionValues = new HashSet<>(substitutionValues);
			rval.equation = equation;
			return rval;
		}
	}

	/**
	 * Details needed to perform an adapation
	 */
	public static class AdaptationConfiguration {
		/**
		 * A NodeType-AttributeNames dictionary of attributes that shouldn't be inserted into the final solution
		 */
		Map<String, Set<String>> ignoredAttributes;

		/**
		 * The intent of this is to remap values attached to a transformed result configuration obtained from the
		 * solver and remap them to fields within the same graph structure as the inventory
		 * <p>
		 * First Key: Parent Node
		 * Second Key: Attribute Name
		 * Outer List: A list of paths relative to the parent node
		 * Inner List: A single path relative to the parent node
		 */
		Map<String, Map<String, List<List<String>>>> directNodeAttributeRemappingOptions;

		/**
		 * The intent of this is to remap values attached to a transformed result configuration obtained from the
		 * solver and remap them to fields within the same graph structure as the input graph
		 * <p>
		 * First Key: Parent Node
		 * Second Key: Attribute Name
		 * Outer List: A list of paths relative to the parent node
		 * Inner List: A single path relative to the parent node
		 */
		Map<String, Map<String, List<List<String>>>> indirectNodeAttributeRemappingOptions;

		// TODO: Do away with these once we track the resultant valid data properly
		private static List<List<String>> getMappings(@Nonnull String nodeType, @Nonnull String attributeName, @Nonnull Map<String, Map<String, List<List<String>>>> remappingSet) {
			Map<String, List<List<String>>> nodeRemappingSet;
			if ((nodeRemappingSet = remappingSet.get(nodeType)) != null) {
				return nodeRemappingSet.get(attributeName);
			}
			return null;
		}

		public List<List<String>> getDirectChildRemappingOptions(@Nonnull String nodeType, @Nonnull String attributeName) {
			return getMappings(nodeType, attributeName, directNodeAttributeRemappingOptions);
		}

		public List<List<String>> getIndirectChildRemappingOptions(@Nonnull String nodeType, @Nonnull String attributeName) {
			return getMappings(nodeType, attributeName, indirectNodeAttributeRemappingOptions);
		}
	}


	/**
	 * This class pertains to data that must be gathered and handed to the DSL for determining replacement DAUs
	 */
	public static class PropertyCollectionInstructions {

		/**
		 * A mapping indicating for which objects we should collect which properties
		 */
		public Map<String, Set<String>> collectedChildProperties;


		/**
		 * Data that is collected for debugging but will be removed prior to being send to the DSL
		 */
		public Map<String, Set<String>> collectedDebugProperties;

		/**
		 * Item types contained in this list will default to true if they have no value set
		 */
		public Set<String> valuesToDefaultToTrue;
	}

	/**
	 * {@link KnownTypes} that we are aware of and fully support
	 */
	public KnownTypes supported;


	/**
	 * If true, an exception will be raised if we encounter an unexpected MDL DAU type. Otherwise, it will flow
	 * through freely to the DSL
	 */
	public boolean haltOnUnsupportedDauType;

	/**
	 * If true, an exception will be raised if we encounter unexpected module functionality. Otherwise, it will flow
	 * through freely to the DSL
	 */
	public boolean haltOnUnsupportedModuleFunctionality;

	/**
	 * If true, an exception will be raised if we encounter unexpected port functionality. Otherwise, it will flow
	 * through freely to the DSL
	 */
	public boolean haltOnUnsupportedPortFunctionality;

	/**
	 * Rules used to govern trimming down the data collected through the graph for much easier conversion into something
	 * for the DSL.
	 */
	public static class TransformationInstructions implements DuplicateInterface<TransformationInstructions> {
		/**
		 * Any MDL types in this set will have their attributes and children linked to the parent and then be removed
		 * from the graph
		 */
		public Map<String, Set<String>> shortNodesToParent;

		/**
		 * Any MDL types and their children in this set will be iremoved prior to DSL conversion, thus being "ignored"
		 * by the DSL.
		 */
		public List<List<String>> ignoredNodes;

		/**
		 * A List of node paths followed by an attribute name. If a path and attribute name is found it should be
		 * removed when collecting requirements data.
		 */
		public List<List<String>> ignoredAttributes;

		/**
		 * Calculations that will be used to fill in values
		 */
		public Map<String, List<Calculation>> calculations;

		/**
		 * Any parent (key) node types that do not have the corresponding chain of children will be ignored.
		 */
		public Map<String, List<String>> ignoreParentsWithoutProperties;

		/**
		 * Any MDL Types that should be tagged with a completely unique identifier
		 */
		public Set<String> taggedNodes;

		/**
		 * If the Path the key maps to exists and the last value is an attribute, rename it to the key value
		 */
		public Map<String, List<String>> renameAttributes;

		/**
		 * for an object type of the first key value, if it contains multiple children of the second key value,
		 * combine each listed child identifier into individual sets of values
		 */
		public Map<String, Map<String, Set<String>>> combineSquashedChildNodeAttributes;

		private static TransformationInstructions mergeTransformationInstructions(
				@Nonnull TransformationInstructions globalInstructions,
				@Nonnull TransformationInstructions specificInstructions) {
			TransformationInstructions rval = globalInstructions.duplicate();

			if (specificInstructions.shortNodesToParent != null) {
				for (String key : specificInstructions.shortNodesToParent.keySet()) {
					Set<String> nodeSet = rval.shortNodesToParent.computeIfAbsent(key, k -> new HashSet<>());
					nodeSet.addAll(specificInstructions.shortNodesToParent.get(key));
				}
			}

			if (specificInstructions.ignoredNodes != null) {
				for (List<String> l : specificInstructions.ignoredNodes) {
					rval.ignoredNodes.add(new LinkedList<>(l));
				}
			}

			if (specificInstructions.ignoredAttributes != null) {
				for (List<String> l : specificInstructions.ignoredAttributes) {
					rval.ignoredAttributes.add(new LinkedList<>(l));
				}
			}

			if (specificInstructions.calculations != null) {
				for (String key : specificInstructions.calculations.keySet()) {
					List<Calculation> targetCalculationList = rval.calculations.computeIfAbsent(key, k -> new LinkedList<>());
					for (Calculation c : specificInstructions.calculations.get(key)) {
						targetCalculationList.add(c.duplicate());
					}
				}
			}

			if (specificInstructions.renameAttributes != null) {
				for (String key : specificInstructions.renameAttributes.keySet()) {
					rval.renameAttributes.put(key, new LinkedList<>(specificInstructions.renameAttributes.get(key)));
				}
			}

			if (specificInstructions.ignoreParentsWithoutProperties != null) {
				throw new RuntimeException("Not adding support for this unless needed!");
			}

			if (specificInstructions.taggedNodes != null) {
				throw new RuntimeException("Not adding support for this unless needed!");
			}

			if (specificInstructions.combineSquashedChildNodeAttributes != null) {
				throw new RuntimeException("Not adding support for this unless needed!");
			}
			return rval;
		}

		@Override
		public TransformationInstructions duplicate() {
			TransformationInstructions rval = new TransformationInstructions();

			rval.shortNodesToParent = Utils.duplicateSetMap(shortNodesToParent);
			rval.ignoredNodes = Utils.duplicateListList(ignoredNodes);
			rval.ignoredAttributes = Utils.duplicateListList(ignoredAttributes);
			rval.calculations = Utils.duplicateMap(calculations);
			rval.ignoreParentsWithoutProperties = Utils.duplicateMap(ignoreParentsWithoutProperties);
			rval.taggedNodes = new HashSet<>(taggedNodes);
			rval.renameAttributes = Utils.duplicateListMap(renameAttributes);

			rval.combineSquashedChildNodeAttributes = new HashMap<>();

			for (String parentObjType : combineSquashedChildNodeAttributes.keySet()) {
				Map<String, Set<String>> origChildObjMap = combineSquashedChildNodeAttributes.get(parentObjType);
				Map<String, Set<String>> cloneChildObjMap = new HashMap<>();
				rval.combineSquashedChildNodeAttributes.put(parentObjType, cloneChildObjMap);

				for (String childObjType : origChildObjMap.keySet()) {
					cloneChildObjMap.put(childObjType, new HashSet<>(origChildObjMap.get(childObjType)));
				}
			}
			return rval;
		}
	}

	/**
	 * A configuration to be used for validation purposes
	 */
	public static class ValidationConfiguration {
		/**
		 * The list of attributes that must be present for a specified node
		 */
		public Map<String, Set<String>> defaultPropertyList;

		/**
		 * A list of attribute values that should be stored separately for debugging but shouldn't be provided to the
		 * solver
		 */
		public List<String> debugIdentificationValues;
	}

	/**
	 * Data related to preparing for dataCollectionInstructions.
	 */
	public PropertyCollectionInstructions dataCollectionInstructions;

	/**
	 * Data related to the DSL Transformation
	 */
	private TransformationInstructions globalTransformation;
	private TransformationInstructions inventoryTransformation;
	private TransformationInstructions usageTransformation;
	private TransformationInstructions requirementsTransformation;


	public TransformationInstructions getInventoryTransformationInstructions() {
		return TransformationInstructions.mergeTransformationInstructions(globalTransformation, inventoryTransformation);
	}

	public TransformationInstructions getUsageTransformationInstructions() {
		return TransformationInstructions.mergeTransformationInstructions(globalTransformation, usageTransformation);
	}

	public TransformationInstructions getRequirementsTransformationInstructions() {
		return TransformationInstructions.mergeTransformationInstructions(globalTransformation, requirementsTransformation);
	}

	public TransformationInstructions getGlobalTransformationInstructions() {
		return globalTransformation.duplicate();
	}


	public ValidationConfiguration validation;

	public AdaptationConfiguration adaptation;
}
