package mil.darpa.immortals.flitcons;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import mil.darpa.immortals.flitcons.adaptation.AdaptationDataInterface;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static mil.darpa.immortals.flitcons.Utils.CHILD_LABEL;
import static mil.darpa.immortals.flitcons.Utils.PARENT_LABEL;

public class SolutionInjector {

	public SolutionInjector(@Nonnull AbstractDataTarget dataTarget, @Nonnull DynamicObjectContainer solution) {
		this.dataSource = dataTarget;

		SolutionPreparer preparer = new SolutionPreparer(
				dataTarget.getInterconnectedFaultyConfiguration(),
				dataTarget.getInterconnectedTransformedFaultyConfiguration(false),
				dataTarget.getRawInventoryContainer(),
				dataTarget.getTransformedDauInventory(false));

		this.adaptationData = preparer.prepare(solution, PARENT_LABEL, CHILD_LABEL);
	}

	private final Set<SolutionPreparer.ParentAdaptationData> adaptationData;
	private final AbstractDataTarget dataSource;
	Configuration.AdaptationConfiguration config = Configuration.getInstance().adaptation;

	public SolutionInjector(@Nonnull AbstractDataTarget dataSource, @Nonnull Set<SolutionPreparer.ParentAdaptationData> adaptationData) {
		this.dataSource = dataSource;
		this.adaptationData = adaptationData;
	}

	private void updateNode(AdaptationDataInterface node) {
		// Get the actual data
		HierarchicalData rawNodeData = node.getRawData();
		String rawNodeType = rawNodeData.getNodeType();

		TreeMap<String, Object> nodeValues = new TreeMap<>(node.getValues());

		// And for each attribute in that child
		for (String attrName : rawNodeData.getAttributeNames()) {
			// If the node has a custom value
			if (nodeValues.containsKey(attrName)) {
				// Get the source node (which may be another parent prior to squashing)
				HierarchicalData realParent = node.getSupersededData();

				// And update it
				dataSource.update_NodeAttribute((OrientVertex) realParent.associatedObject, attrName, nodeValues.remove(attrName));
			}
		}

		// Then, for any remaining attributes
		for (String attrName : nodeValues.keySet()) {
			// TODO: Add port type properly once the posted issue is resolved
			if (attrName.equals("PortType") || attrName.equals("Thermocouple")) continue;
			List<List<String>> candidateAttributeRemappingPaths;
			boolean childAccountedFor = false;

			if (config.ignoredAttributes.containsKey(rawNodeType) &&
					config.ignoredAttributes.get(rawNodeType).contains(attrName)) {
				// Pass, It should be ignored
				childAccountedFor = true;

			} else if ((candidateAttributeRemappingPaths = config.getDirectChildRemappingOptions(rawNodeType, attrName)) != null) {
				for (List<String> childNodeAttributePath : candidateAttributeRemappingPaths) {
					// Otherwise, it must be remapped using the same data set as the target node
					HierarchicalData realParentNode = rawNodeData.getChildNodeByPath(childNodeAttributePath.subList(0, childNodeAttributePath.size() - 1));
					if (realParentNode != null) {
						dataSource.update_NodeAttribute((OrientVertex) realParentNode.associatedObject, childNodeAttributePath.get(childNodeAttributePath.size() - 1), nodeValues.get(attrName));
						childAccountedFor = true;
						break;
					}
				}

			} else if ((candidateAttributeRemappingPaths = config.getIndirectChildRemappingOptions(rawNodeType, attrName)) != null) {
				for (List<String> childNodeAttributePath : candidateAttributeRemappingPaths) {
					// Otherwise, it must be remapped using the parent data set of the input configuration
					HierarchicalData supsersededData = node.getSupersededData();
					HierarchicalData realParentNode = supsersededData.getChildNodeByPath(childNodeAttributePath.subList(0, childNodeAttributePath.size() - 1));
					if (realParentNode != null) {
						dataSource.update_NodeAttribute((OrientVertex) realParentNode.associatedObject, childNodeAttributePath.get(childNodeAttributePath.size() - 1), nodeValues.get(attrName));
						childAccountedFor = true;
						break;
					}
				}
			}

			if (!childAccountedFor) {
				throw AdaptationnException.internal("Unexpected unaccounted for attribute '" + attrName + "'!");
			}
		}
	}

	public void injectSolution() {
		System.out.println(Utils.padCenter("Initial PortMapping Configuration", 80, '#'));
		System.out.println(Utils.padCenter("", 80, '#'));


		//// 1.  Update the inventory node attributes to match the chosen values
		// For each parent
		for (SolutionPreparer.ParentAdaptationData parent : adaptationData) {
			// Update it
			updateNode(parent);

			// And for each child
			for (SolutionPreparer.ChildAdaptationData child : parent.ports) {
				// Update it
				updateNode(child);
			}
		}

		// 2.  Replace the old nodes with the inventory nodes

		for (SolutionPreparer.ParentAdaptationData parent : adaptationData) {
			// TODO: Add partial rewiring from old DAU to new DAU?

			// For each child
			// TODO: Verify it
			for (SolutionPreparer.ChildAdaptationData child : parent.ports) {
				// Rewire the original node into where the old node was
				dataSource.update_rewireNode(
						(OrientVertex) child.supersededRawData.associatedObject,
						(OrientVertex) child.rawData.associatedObject);
			}

			// Then for the parent, get the replacement data node
			HierarchicalData parentReplacement = parent.rawData;

			// The original nodes
			Set<HierarchicalData> parentOriginals = parent.supersededDauRawData;

			// And the original nodes parents
			Set<HierarchicalData> originalsParentSet = parentOriginals.stream().map(HierarchicalData::getParentData).collect(Collectors.toSet());

			// Making sure they all share the same parent.
			if (originalsParentSet.size() == 0) {
				throw AdaptationnException.internal("No parent nodes found!");
			} else if (originalsParentSet.size() > 1) {
				throw AdaptationnException.internal("Multiple parent nodes found for multiple DAUs being replaced by a single DAU! The proper parent is indeterminate!");
			}

			// Insert the replacement node as a child of the original parent
			OrientVertex newParentParentNode = (OrientVertex) ((OrientVertex) parentOriginals.iterator().next().associatedObject).getEdges(Direction.OUT, "Containment").iterator().next().getVertex(Direction.IN);
			dataSource.update_insertNodeAsChild((OrientVertex) parentReplacement.associatedObject, newParentParentNode);

			// And remove the old parent nodes
			for (HierarchicalData data : parentOriginals) {
				dataSource.update_removeNodeTree((OrientVertex) data.associatedObject);
			}

			if (!SolverConfiguration.getInstance().isNoCommit()) {
				dataSource.commit();
			}

			dataSource.shutdown();
		}

		dataSource.restart();

		System.out.println(Utils.padCenter("", 80, '#'));
		dataSource.shutdown();
	}
}
