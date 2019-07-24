package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.*;

public class Configuration {

	private static transient Configuration instance;

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

	public static class NodeAttributeRelation implements DuplicateInterface {
		public final LinkedList<String> nodePath;
		public final String attribute;

		public NodeAttributeRelation(@Nonnull List<String> nodePath, @Nonnull String attribute) {
			this.nodePath = new LinkedList<>(nodePath);
			this.attribute = attribute;
		}

		@Override
		public NodeAttributeRelation duplicate() {
			return new NodeAttributeRelation(nodePath, attribute);
		}
	}

	/**
	 * A potential solution for a conflict.
	 */
	public static class ResolutionStrategySolution implements DuplicateInterface {
		/**
		 * The possible value for node #1
		 */
		public final String node1Value;
		/**
		 * The set of possible values for node #2
		 */
		public final List<String> node2Values;
		/**
		 * The valid result if an only if node #1 has the specified value and node #2 has one of the specified values
		 */
		public final String result;

		public ResolutionStrategySolution(@Nonnull String node1Value, @Nonnull List<String> node2Values, @Nonnull String result) {
			this.node1Value = node1Value;
			this.node2Values = node2Values;
			this.result = result;
		}

		@Override
		public ResolutionStrategySolution duplicate() {
			return new ResolutionStrategySolution(node1Value, new LinkedList<>(node2Values), result);
		}
	}

	/**
	 * The strategy for resolving a conflict when two nodes to be merged together have a conflict
	 */
	public static class ResolutionStrategy implements DuplicateInterface {
		/**
		 * The full or partial path of the node with a single specific value
		 */
		public final LinkedList<String> node1Path;
		/**
		 * The full or partial path of the node that can have a couple different values
		 */
		public final LinkedList<String> node2Path;
		public final String attributeLabel;
		public final List<ResolutionStrategySolution> resolutionStrategySolutions;

		public ResolutionStrategy(@Nonnull List<String> node1Path, @Nonnull List<String> node2Path, @Nonnull String attributeLabel, @Nonnull List<ResolutionStrategySolution> resolutionStrategySolutions) {
			this.node1Path = new LinkedList<>(node1Path);
			this.node2Path = new LinkedList<>(node2Path);
			this.attributeLabel = attributeLabel;
			this.resolutionStrategySolutions = resolutionStrategySolutions;
		}

		@Override
		public ResolutionStrategy duplicate() {
			return new ResolutionStrategy(
					Utils.duplicateList(node1Path),
					Utils.duplicateList(node2Path),
					attributeLabel,
					Utils.duplicateList(resolutionStrategySolutions));
		}
	}

	public static class ParentChildAttributeRelation implements DuplicateInterface<ParentChildAttributeRelation> {
		public final String attribute;
		public final LinkedList<String> parentPath;
		public final LinkedList<String> childPath;
		private final List<String> fullPath;

		private ParentChildAttributeRelation(@Nonnull List<String> parentPath, @Nonnull List<String> childPath, @Nonnull String attribute) {
			this.parentPath = new LinkedList<>(parentPath);
			this.childPath = new LinkedList<>(childPath);
			this.attribute = attribute;
			this.fullPath = new ArrayList<>(parentPath.size() + childPath.size());
			fullPath.addAll(parentPath);
			fullPath.addAll(childPath);
		}

		@Override
		public ParentChildAttributeRelation duplicate() {
			return new ParentChildAttributeRelation(parentPath, childPath, attribute);
		}
	}

	/**
	 * A calculation that must be performed to determined undefined values
	 */
	public static class Calculation implements DuplicateInterface<Calculation> {

		public Calculation(@Nonnull List<String> parentPath, @Nonnull List<NodeAttributeRelation> children,
		                   @Nonnull String parentTargetValueIdentifier, @Nonnull String equation) {
			this.parentPath = new LinkedList<>(parentPath);
			this.children = Utils.duplicateList(children);
			this.parentTargetValueIdentifier = parentTargetValueIdentifier;
			this.equation = equation;
		}

		/**
		 * The parent node path that contains the
		 */
		public LinkedList<String> parentPath;

		/**
		 * The identifier for the value that will be calculated
		 */
		public String parentTargetValueIdentifier;

		/**
		 * The paths of the children and their attributes that are necessary to calculate the solution
		 */
		public List<NodeAttributeRelation> children;

		/**
		 * A javascript equation that consists of variables prefixed with "@" defined in {@link Calculation#children}
		 */
		public String equation;

		@Override
		public Calculation duplicate() {
			return new Calculation(parentPath, children, parentTargetValueIdentifier, equation);
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
	 * A single object meant to capture most of the more complex manipulations that can be applied to an MDLRoot
	 * to mutate it into something the DSL can ingest
	 */
	public static class ValueRemappingInstructions implements DuplicateInterface<ValueRemappingInstructions> {
		/**
		 * The path of the parent node
		 */
		public LinkedList<String> parentPath;

		/**
		 * The child attribute name
		 */
		public String childAttributeName;

		/**
		 * THe new name for the child attribute, if applicable
		 */
		public String optionalNewChildAttributeName;

		/**
		 * A map of the transformations that can be applied to the child attribute value. All possible original values
		 * must be captured. If the target value is a set
		 * {@link ValueRemappingInstructions#optionalChildAttributeValueSelectionRemap} should be used instead.
		 */
		public Map<String, Object> optionalChildAttributeValueRemap;

		/**
		 * A map of the transformations that can be applied to the child attribute value. All possible original values
		 * must be captured. If the target value is a single value
		 * {@link ValueRemappingInstructions#optionalChildAttributeValueRemap} should be used instead.
		 */
		public Map<String, List<Object>> optionalChildAttributeValueSelectionRemap;

		/**
		 * The value to set if the attribute is missing.
		 */
		public Object optionalValueToCreateIfMissing;

		public ValueRemappingInstructions(
				@Nonnull List<String> parentPath, @Nonnull String childAttributeName, @Nullable String optionalNewChildAttributeName,
				@Nullable Map<String, Object> optionalChildAttributeValueRemap, @Nullable Map<String, List<Object>> optionalChildAttributeValueSelectionRemap,
				@Nullable Object optionalValueToCreateIfMissing) {
			this.parentPath = new LinkedList<>(parentPath);
			this.childAttributeName = childAttributeName;
			this.optionalNewChildAttributeName = optionalNewChildAttributeName;
			this.optionalChildAttributeValueRemap = optionalChildAttributeValueRemap == null ? null : Utils.duplicateMap(optionalChildAttributeValueRemap);
			this.optionalChildAttributeValueSelectionRemap = optionalChildAttributeValueSelectionRemap == null ? null : Utils.duplicateListMap(optionalChildAttributeValueSelectionRemap);
			this.optionalValueToCreateIfMissing = optionalValueToCreateIfMissing;
		}

		@Override
		public ValueRemappingInstructions duplicate() {
			return new ValueRemappingInstructions(parentPath, childAttributeName, optionalNewChildAttributeName,
					optionalChildAttributeValueRemap, optionalChildAttributeValueSelectionRemap, optionalValueToCreateIfMissing);
		}
	}

	/**
	 * This class pertains to data that must be gathered and handed to the DSL for determining replacement DAUs
	 */
	public static class PropertyCollectionInstructions {

		/**
		 * The node which will be replaced, and everything should be from the perspective of
		 */
		public String primaryNode;

		/**
		 * A mapping indicating for which objects we should collect which properties
		 */
		public Map<String, Set<String>> collectedChildProperties;


		/**
		 * Data that is collected for debugging but will be removed prior to being send to the DSL
		 */
		public Map<String, Set<String>> collectedDebugProperties;

	}

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
		 * Any nodes that match the paths within this value will be removed as part of the DSL conversion.
		 */
		public List<List<String>> ignoredNodePaths;

		/**
		 * Moves the child attribute from {@link ParentChildAttributeRelation#attribute} in the
		 * {@link ParentChildAttributeRelation#parentPath} to instances of the
		 * {@link ParentChildAttributeRelation#childPath} children.
		 */
		// TODO: This really should go with the other remapping details somehow to prevent having to reference the transformed output in the configuration
		public List<ParentChildAttributeRelation> transferAttributeToChildren;

		/**
		 * A List of node paths followed by an attribute name. If a path and attribute name is found it should be
		 * removed when collecting requirements data.
		 */
		public List<List<String>> ignoredAttributePaths;

		/**
		 * Calculations that will be used to fill in values
		 */
		public LinkedList<Calculation> calculations;

		/**
		 * Ignore nodes at {@link ParentChildAttributeRelation#parentPath} path that do not contain a child at path
		 * {@link ParentChildAttributeRelation#childPath} with the attribute {@link ParentChildAttributeRelation#attribute}
		 */
		public List<ParentChildAttributeRelation> ignoreParentsWithoutChildAttributes;

		/**
		 * Any MDL Types that should be tagged with a completely unique identifier
		 */
		public Set<String> taggedNodes;

		/**
		 * Instructions that can be used to transform the data into a form more palatable for the DSL
		 */
		public Set<ValueRemappingInstructions> valueRemappingInstructions;

		/**
		 * for an object type of the first key value, if it contains multiple children of the second key value,
		 * combine each listed child identifier into individual sets of values
		 */
		public Map<String, Map<String, Set<String>>> combineSquashedChildNodeAttributes;

		/**
		 * Provides a list of strategies for resolving conflicts.
		 */
		public List<ResolutionStrategy> resolutionStrategies;

		private static TransformationInstructions mergeTransformationInstructions(
				@Nonnull TransformationInstructions globalInstructions,
				@Nonnull TransformationInstructions specificInstructions) {
			TransformationInstructions targetInstructions = globalInstructions.duplicate();

			if (specificInstructions.shortNodesToParent != null) {
				for (String key : specificInstructions.shortNodesToParent.keySet()) {
					Set<String> nodeSet = targetInstructions.shortNodesToParent.computeIfAbsent(key, k -> new HashSet<>());
					nodeSet.addAll(specificInstructions.shortNodesToParent.get(key));
				}
			}

			if (specificInstructions.ignoredNodePaths != null) {
				targetInstructions.ignoredNodePaths.addAll(Utils.duplicateListList(specificInstructions.ignoredNodePaths));
			}

			if (specificInstructions.ignoredAttributePaths != null) {
				targetInstructions.ignoredAttributePaths.addAll(Utils.duplicateListList(specificInstructions.ignoredAttributePaths));
			}

			if (specificInstructions.calculations != null) {
				targetInstructions.calculations.addAll(Utils.duplicateList(specificInstructions.calculations));
			}

			if (specificInstructions.valueRemappingInstructions != null) {
				targetInstructions.valueRemappingInstructions.addAll(Utils.duplicateSet(specificInstructions.valueRemappingInstructions));
			}

			if (specificInstructions.transferAttributeToChildren != null) {
				targetInstructions.transferAttributeToChildren.addAll(Utils.duplicateList(specificInstructions.transferAttributeToChildren));
			}

			if (specificInstructions.ignoreParentsWithoutChildAttributes != null) {
				targetInstructions.ignoreParentsWithoutChildAttributes.addAll(Utils.duplicateList(specificInstructions.ignoreParentsWithoutChildAttributes));
			}

			if (specificInstructions.taggedNodes != null) {
				targetInstructions.taggedNodes.addAll(specificInstructions.taggedNodes);
			}

			if (specificInstructions.resolutionStrategies != null) {
				throw new RuntimeException("Not adding support for this unless needed!");
			}

			if (specificInstructions.combineSquashedChildNodeAttributes != null) {
				throw new RuntimeException("Not adding support for this unless needed!");
			}
			return targetInstructions;
		}

		@Override
		public TransformationInstructions duplicate() {
			TransformationInstructions rval = new TransformationInstructions();

			rval.shortNodesToParent = Utils.duplicateSetMap(shortNodesToParent);
			rval.ignoredNodePaths = Utils.duplicateListList(ignoredNodePaths);
			rval.ignoredAttributePaths = Utils.duplicateListList(ignoredAttributePaths);
			rval.calculations = Utils.duplicateList(calculations);
			rval.ignoreParentsWithoutChildAttributes = Utils.duplicateList(ignoreParentsWithoutChildAttributes);
			rval.taggedNodes = new HashSet<>(taggedNodes);
			rval.valueRemappingInstructions = Utils.duplicateSet(valueRemappingInstructions);
			rval.transferAttributeToChildren = Utils.duplicateList(transferAttributeToChildren);
			rval.resolutionStrategies = Utils.duplicateList(resolutionStrategies);

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
		 * If true, simple shortened labels will be used in the charts, omitting path structure details
		 */
		public boolean useSimpleLabels;

		/**
		 * The depth of the labels
		 * <p>
		 * For example, 1 would provide the DAU name, 2 would provide the DAU name and Port name, 3 would provide
		 * additional nested labels if available
		 */
		public int labelDepth = 9999;
	}

	/**
	 * An intermediary value to be used to define null. The intent is to standardize null regardless of input format
	 * (where it may mean or be interpreted in a different way). This is specifically useful when creating
	 * {@link ValueRemappingInstructions}
	 */
	public String nullValuePlaceholder;

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
