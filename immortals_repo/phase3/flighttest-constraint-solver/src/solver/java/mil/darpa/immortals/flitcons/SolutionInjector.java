package mil.darpa.immortals.flitcons;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import mil.darpa.immortals.flitcons.adaptation.AdaptationDataInterface;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static mil.darpa.immortals.flitcons.Utils.CHILD_LABEL;
import static mil.darpa.immortals.flitcons.Utils.PARENT_LABEL;

public class SolutionInjector {

	private static final Logger logger = LoggerFactory.getLogger(SolutionInjector.class);

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

		// TODO: Abstract this somehow so the SolutionInjector stays clean of MDL
		if (nodeValues.containsKey("PortType") && nodeValues.get("PortType").equals("Thermocouple")) {
			if (nodeValues.containsKey("Thermocouple")) {
				// If a Thermocouple PortType is necessary

				// Remove the existing "PortType" attribute if it exists
				dataSource.update_removeAttribute(rawNodeData.associatedObject, "PortType", "PortTypes");

				// Add the protType object containing the Thermocouple data
				TreeMap<String, Object> values = new TreeMap<>();
				values.put("Thermocouple", nodeValues.get("Thermocouple"));
				values.put("PortType", nodeValues.get("PortType"));
				List<String> childPath = new LinkedList<>();
				childPath.add("PortTypes");
				childPath.add("PortType");
				dataSource.update_createOrUpdateChildWithAttributes((OrientVertex) rawNodeData.associatedObject, childPath, values);

				// And remove the values forom the list to be updated
				nodeValues.remove("Thermocouple");
				nodeValues.remove("PortType");

			} else {
				// Otherwise
				List<String> childNodePath = new LinkedList<>();
				childNodePath.add("PortTypes");
				childNodePath.add("PortType");
				// Check if there is a "PortType" node
				HierarchicalData childNode = rawNodeData.getChildNodeByPath(childNodePath);
				if (childNode != null) {
					// And remove it if so
					dataSource.update_removeNodeTree(childNode.associatedObject);

					// Add the attribute to the parent
					childNodePath.remove(1);
					HierarchicalData parentNode = rawNodeData.getChildNodeByPath(childNodePath);
					dataSource.add_NodeAttribute(parentNode.associatedObject, "PortType", nodeValues.get("PortType"));

					// And remove the value from the attributes to update
					nodeValues.remove("PortType");

				}
			}
		}

		for (String attrName : nodeValues.keySet()) {
			List<List<String>> candidateAttributeRemappingPaths;
			boolean childAccountedFor = false;

			LinkedList<Configuration.AttributeRemappingConfiguration> remappingConfigurationList;

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
			} else if ((remappingConfigurationList = config.attributeRemappingInstructions.get(rawNodeType)) != null) {
				childAccountedFor = true;
				for (Configuration.AttributeRemappingConfiguration originalremappingConfiguration : remappingConfigurationList) {
					Configuration.AttributeRemappingConfiguration remappingConfiguration = originalremappingConfiguration.duplicate();
					LinkedList<String> sourceList = remappingConfiguration.sourceAttributePath;

					Object currentValue;

					if (attrName.equals(sourceList.remove(0))) {
						currentValue = nodeValues.get(attrName);

						while (!sourceList.isEmpty() && currentValue != null) {
							if ((currentValue instanceof Map)) {
								Map currentMap = (Map) currentValue;
								String nextVal = sourceList.remove(0);
								if (currentMap.containsKey(nextVal)) {
									currentValue = currentMap.get(nextVal);
								} else {
									currentValue = null;
									break;
								}
							} else {
								currentValue = null;
								break;
							}
						}
						if (sourceList.isEmpty() && currentValue != null) {

							HierarchicalData supsersededData = node.getSupersededData();
							HierarchicalData realParentNode = supsersededData.getChildNodeByPath(remappingConfiguration.targetAttributePath.subList(0, remappingConfiguration.targetAttributePath.size() - 1));
							String nodeName = remappingConfiguration.targetAttributePath.getLast();
							if (realParentNode != null) {
								try {
									// TODO: Handle deconfliction properly
									if (realParentNode.originalNodeClonedFrom != null) {

										HierarchicalData originalNode = realParentNode.originalNodeClonedFrom;
										Map<String, Object> newValues = new HashMap<>();

										for (HierarchicalData clone : originalNode.clones) {
											if (newValues.isEmpty()) {
												newValues.putAll(clone.getAttributes());
											} else {
												for (String name : clone.getAttributeNames()) {
													if (!newValues.containsKey(name) ||
															newValues.get(name) != clone.getAttribute(name)) {
														throw new RuntimeException("Conflict detected!");
													}

												}
											}
										}


										dataSource.update_NodeAttribute((OrientVertex) realParentNode.originalNodeClonedFrom.associatedObject, nodeName, currentValue);
									} else {
										dataSource.update_NodeAttribute((OrientVertex) realParentNode.associatedObject, nodeName, currentValue);
									}
									continue;
								} catch (Exception e) {
									throw new RuntimeException(e);
								}

							}
						}
					}
//					childAccountedFor = false;
				}


			}

			if (!childAccountedFor) {
				throw AdaptationnException.internal("Unexpected unaccounted for attribute '" + attrName + "'!");
			}
		}
	}

	public void injectSolution() {
		logger.info(Utils.padCenter("Initial PortMapping Configuration", 80, '#'));
		logger.info(Utils.padCenter("", 80, '#'));


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

		Set<HierarchicalData> nodeTreesToRemove = new HashSet<>();

		// 2.  Replace the old nodes with the inventory nodes

		for (SolutionPreparer.ParentAdaptationData parent : adaptationData) {
			// For each child
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

			nodeTreesToRemove.addAll(parentOriginals);
		}

		// And remove the old parent nodes
		for (HierarchicalData data : nodeTreesToRemove) {
			dataSource.update_removeNodeTree((OrientVertex) data.associatedObject);
		}

		if (!SolverConfiguration.getInstance().isNoCommit()) {
			dataSource.commit();
		}

		dataSource.shutdown();

		dataSource.restart();

		logger.info(Utils.padCenter("", 80, '#'));
		dataSource.shutdown();
	}
}
