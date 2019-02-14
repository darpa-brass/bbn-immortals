package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.HierarchicalNode;
import mil.darpa.immortals.flitcons.datatypes.ScenarioData;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScenarioDataTransformer {

	private final static Set<String> transplantPropertiesToParent = Configuration.instance.dslTransformation.transplantPropertiesToParentNode;
	private final static Set<String> shortNodesToParent = Configuration.instance.dslTransformation.shortNodesToParent;
	private final static Set<String> ignoreNodes = Configuration.instance.dslTransformation.ignoreNodes;
	private final static Set<String> tagNodes = Configuration.instance.dslTransformation.taggedNodes;

	/**
	 * Removes nodes that are of specific types of nodes that should be ignored
	 */
	private static void trimIgnoredNodes(ScenarioData sd) {
		Set<HierarchicalData> nodesToRemove = new HashSet<>();
		for (String identifier : sd.getDauNodeData().getNodeIdentifiers()) {
			HierarchicalData node = sd.getDauNodeData().getNode(identifier);
				if (ignoreNodes.contains(node.nodeClass)) {
					nodesToRemove.add(node);
				}
				if (node.getAttributes().containsKey("BBNModuleFunctionality") &&
						node.getAttributes().get("BBNModuleFunctionality").equals("IgnoredModule")) {
					nodesToRemove.add(node);

				} else if (node.getAttributes().containsKey("BBNPortFunctionality") &&
						node.getAttributes().get("BBNPortFunctionality").equals("IgnoredPort")) {
					nodesToRemove.add(node);
				}
		}
		for (HierarchicalData node : nodesToRemove) {
			sd.getDauNodeData().removeNodeTree(node);
		}
	}

	/**
	 * Removes nodes from the path that are redundant to the DSL and make it more difficult to convert
	 */
	private static void shortRedundantNodes(ScenarioData sd) {
		Set<String> originalIdentifierList = new HashSet<>(sd.getDauNodeData().getNodeIdentifiers());
		for (String identifier : originalIdentifierList) {
			if (sd.getDauNodeData().containsNodeWithIdentifier(identifier)) {
				HierarchicalData node = sd.getDauNodeData().getNode(identifier);
				TreeSet<Integer> indicesFlaggedForRemoval = new TreeSet<>();
				if (!node.isRootNode) {
					List<HierarchicalData> parentPath = node.getPathAsData();
					for (int i = 0; i < parentPath.size(); i++) {
						if (shortNodesToParent.contains(node.getPathAsData().get(i).nodeClass)) {
							indicesFlaggedForRemoval.add(i);

						}
					}
					for (int idx : indicesFlaggedForRemoval.descendingSet()) {
						HierarchicalData nodeToRemove = parentPath.get(idx);
						sd.getDauNodeData().removeAndShortParentAndChildNodes(nodeToRemove);
					}
				}
			}
		}
	}

	/**
	 * recursively squashes the properties of specific nodes into their parent to remove redundant layering
	 */
	private static void squashData(ScenarioData sd) {

		HashSet<HierarchicalData> markedForRemoval = new HashSet<>();
		boolean modified = true;

		while (modified) {
			modified = false;
			Set<String> originalIdentifierList = new HashSet<>(sd.getDauNodeData().getNodeIdentifiers());
			for (String identifier : originalIdentifierList) {
				HierarchicalData hd = sd.getDauNodeData().getNode(identifier);
				if (transplantPropertiesToParent.contains(hd.node.nodeType) &&
						!markedForRemoval.contains(hd)) {
					HierarchicalData parent = hd.getPathAsData().get(hd.getPathAsData().size() - 1);


					if (!parent.getAttributes().keySet().containsAll(hd.getAttributes().keySet())) {
						parent.getAttributes().putAll(hd.getAttributes());
						markedForRemoval.remove(parent);
					}

					modified = true;
					markedForRemoval.add(hd);
				}
			}
		}

		for (HierarchicalData nodeToRemove : markedForRemoval) {
			sd.getDauNodeData().removeAndShortParentAndChildNodes(nodeToRemove);
		}
	}

	private static void tagData(ScenarioData sd) {
		for (String nodeIdentifier : sd.getDauNodeData().getNodeIdentifiers()) {
			HierarchicalData node = sd.getDauNodeData().getNode(nodeIdentifier);
			if (tagNodes.contains(node.nodeClass)) {
				node.getAttributes().put("GloballyUniqueId", node.sourceIdentifier);
			}
		}
	}

	public static ScenarioData postprocess(@Nonnull ScenarioData inputScenarioData) {
		// TODO: Raise appropriate errors when trying to execute these functions on data a second time

		ScenarioData clone = inputScenarioData.duplicate();
//		System.out.println("######## Initially Collected Data ########");
//		ScenarioDataTransformer.displayDauNodeData(clone, new File("CollectedData.txt"));

		pullExternalDataIntoDaus(clone);
//		System.out.println("######## External Data Pulled In ########");
//		ScenarioDataTransformer.displayDauNodeData(clone, new File("ExternalPulledInData.txt"));


		squashData(clone);
//		System.out.println("\n\n\n\n######## Properties Squashed Into Relevant Parent ########");
//		ScenarioDataTransformer.displayDauNodeData(clone, new File("PropsSquished.txt"));

		trimIgnoredNodes(clone);
//		System.out.println("\n\n\n\n######## Ignored Nodes Removed ########");
//		ScenarioDataTransformer.displayDauNodeData(clone, new File("IgnoredRemoved.txt"));


		shortRedundantNodes(clone);
//		System.out.println("\n\n\n\n######## Redundant Nodes Collapsed ########");
//		ScenarioDataTransformer.displayDauNodeData(clone, new File("RundandancyCollapsed.txt"));


		tagData(clone);
//		System.out.println("\n\n\n\n######## Required Inidividual Tagging Complete ########");
//		ScenarioDataTransformer.displayDauNodeData(clone, new File("TaggingComplete.txt"));


		return clone;
	}

	private static void pullExternalDataIntoDaus(@Nonnull ScenarioData inputScenarioData) {
		// For each DAU node
		for (HierarchicalData dauNode : inputScenarioData.getDauNodeData().getDauRootNodes()) {

			// Get all child node data related to that dau node
			Collection<HierarchicalData> dauNodeSet = inputScenarioData.getDauNodeData().getDauNodes(dauNode);

			// And for each one
			for (HierarchicalData nodeData : dauNodeSet) {

				// For any inbound reference
				for (HierarchicalNode hn : nodeData.getInboundReferencesSet()) {

					// That does not reference another child of the same DAU
					if (dauNodeSet.stream().noneMatch(t -> t.node.equals(hn))) {

						// If no external relation can be found, throw an exception
						if (!inputScenarioData.getIndirectRelations().containsKey(hn)) {
							throw new RuntimeException("Reference edge does not map to any corresponding references to known data!");
						}

						// Otherwise, get the related nodes
						Set<HierarchicalNode> relatedNodes = inputScenarioData.getIndirectRelations().get(hn);
						for (HierarchicalNode referencedNode : relatedNodes) {
							// And for each one, confirm they have related data
							if (!inputScenarioData.getExternalNodeData().containsNodeWithIdentifier(referencedNode.identifier)) {
								throw new RuntimeException("Could not find a matching external data node for the node '" + referencedNode + "'!");
							}

							// Get the related data
							Collection<HierarchicalData> relatedDataSet = inputScenarioData.getExternalNodeData().getDauNodes(
									inputScenarioData.getExternalNodeData().getNode(referencedNode.identifier));

							// And for each piece of information, add it to the properties of the DAU node
							for (HierarchicalData relatedData : relatedDataSet) {
								nodeData.getAttributes().putAll(relatedData.getAttributes());
							}
						}
					}
				}

				// For any outbound reference
				for (HierarchicalNode hn : nodeData.getOutboundReferencesSet()) {
					if (!inputScenarioData.getDauNodeData().containsNodeWithIdentifier(hn.identifier)) {
						// Throw an exception because that is not yet supported.
						throw new RuntimeException("Reference from DAU Node '" + nodeData.toString() + "' to external node '" + hn.toString() + "' detected! This is not supported!");
					}
				}
			}
		}
	}

	public static void displayDauNodeData(ScenarioData scenarioData, File targetFile) {
		List<String> lines = new LinkedList<>();
		for (HierarchicalData dauNode : scenarioData.getDauNodeData().getDauRootNodes()) {
			System.out.println(dauNode.toString());
			lines.add(dauNode.toString());
			Collection<HierarchicalData> nds = scenarioData.getDauNodeData().getDauNodes(dauNode);

			for (HierarchicalData nd : nds) {
				StringBuilder pathStringBuilder = new StringBuilder();

				for (HierarchicalData hn : nd.getPathAsData()) {
					pathStringBuilder.append(hn.toString()).append(".");
				}
				String pathString = pathStringBuilder.append(nd.toString()).append(".").toString();

				for (Map.Entry<String, Object> e : nd.getAttributes().entrySet()) {
					lines.add("\t" + pathString + e.getKey() + " = " + e.getValue());
					System.out.println("\t" + pathString + e.getKey() + " = " + e.getValue());
				}
			}
		}
		try {
			Collections.sort(lines);
			FileUtils.writeLines(targetFile, lines);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
