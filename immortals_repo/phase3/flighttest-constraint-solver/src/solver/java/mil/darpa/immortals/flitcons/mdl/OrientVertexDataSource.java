package mil.darpa.immortals.flitcons.mdl;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import mil.darpa.immortals.flitcons.AbstractOrientVertexDataSource;
import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.datatypes.DataType;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.validation.DebugData;

import javax.annotation.Nonnull;
import java.util.*;

import static mil.darpa.immortals.flitcons.Utils.FLAGGED_FOR_REPLACEMENT;

public class OrientVertexDataSource extends AbstractOrientVertexDataSource {

	public OrientVertexDataSource() {
		super(null);
	}

	public OrientVertexDataSource(@Nonnull String orientDbServerPath) {
		super(orientDbServerPath);
	}

	@Override
	public void update_NodeAttribute(@Nonnull OrientVertex node, @Nonnull String attributeName, @Nonnull Object attributeValue) {
		if (node.getProperty(attributeName) == null) {
			throw AdaptationnException.internal("Node '" + node.getId().toString() + "' does not contain attribute named '" + attributeName + "'! Refusing to add the value!");
		}
		node.setProperty(attributeName, attributeValue);
	}

	@Override
	public void update_rewireNode(@Nonnull OrientVertex originalNode, @Nonnull OrientVertex replacementNode) {
		for (Edge e : originalNode.getEdges(Direction.BOTH, "Reference")) {
			if (e.getVertex(Direction.IN).equals(originalNode)) {
				OrientVertex outNode = (OrientVertex) e.getVertex(Direction.OUT);
				outNode.addEdge("Reference", replacementNode);
				outNode.removeProperty("IDREF");
				outNode.setProperty("IDREF", replacementNode.getProperty("ID"));
				e.remove();
			}
			if (e.getVertex(Direction.OUT).equals(originalNode)) {
				OrientVertex inNode = (OrientVertex) e.getVertex(Direction.IN);
				replacementNode.addEdge("Reference", inNode);
				originalNode.removeProperty("IDREF");
				originalNode.setProperty("IDREF", inNode.getProperty("ID"));
				e.remove();
			}
		}
	}

	@Override
	public void update_removeNodeTree(@Nonnull OrientVertex vertex) {
		// TODO: This is probably slow. But otherwise errors sometimes pop up regarding stale references. I'm sure there is a better way to handle it...
		OrientVertex node = (OrientVertex) vertex.getGraph().getElement(vertex.getId());

		Edge childContainmentToSelf = null;
		for (Edge containmentEdge : node.getEdges(Direction.BOTH, "Containment")) {
			OrientVertex in = (OrientVertex) containmentEdge.getVertex(Direction.IN);
			OrientVertex out = (OrientVertex) containmentEdge.getVertex(Direction.OUT);
			if (in.equals(node)) {
				update_removeNodeTree(out);
			} else {
				if (childContainmentToSelf != null) {
					throw AdaptationnException.input("Node has multiple parents!");
				}
				childContainmentToSelf = containmentEdge;
			}
		}

		if (childContainmentToSelf != null) {
			childContainmentToSelf.remove();
		}

		// TODO: Add dangling reference detection. Since references aren't hierarchical it isn't trivial to check...
//		if (node.getEdges(Direction.BOTH, "Reference").iterator().hasNext()) {
//			throw new RuntimeException("Dangling Reference Edge Found!");
//		}
		node.remove();
	}

	@Override
	public void update_insertNodeAsChild(@Nonnull OrientVertex newNode, @Nonnull OrientVertex parentNode) {
		for (Edge containmentEdge : newNode.getEdges(Direction.OUT, "Containment")) {
			containmentEdge.remove();
		}
		if (newNode.getType().toString().equals("NetworkNode")) {

			for (Vertex vertex : newNode.getVertices(Direction.IN, "Containment")) {
				OrientVertex chld = (OrientVertex) vertex;
				if (chld.getType().toString().equals("GenericParameter")) {
					// TODO: This might not be the best way to do this...
					newNode.getGraph().command(new OCommandSQL(
							"UPDATE GenericParameter SET " + FLAGGED_FOR_REPLACEMENT + " = NULL WHERE @rid == '" + chld.getId().toString() + "'"
					)).execute();
				}
			}
			newNode.addEdge("Containment", parentNode);
		}
	}

	@Override
	public boolean isDauInventory() {
		// Return true since OrientDB contains both in one graph
		return true;
	}

	@Override
	public boolean isInputConfiguration() {
		// Return true since OrientDB contains both in one graph
		return true;
	}

	@Override
	public HierarchicalDataContainer createContainer(@Nonnull DataType dataType, @Nonnull LinkedHashMap<OrientVertex, List<OrientVertex>> input, @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions) {

		LinkedHashMap<HierarchicalIdentifier, HierarchicalData> identifierDataMap = new LinkedHashMap<>();
		LinkedHashMap<HierarchicalData, List<HierarchicalData>> rootChildMap = new LinkedHashMap<>();

		Set<OrientVertex> parentVertices = input.keySet();

		for (OrientVertex parentVertex : parentVertices) {
			List<OrientVertex> dauVertices = input.get(parentVertex);
			HierarchicalData parentData = createData(parentVertex, true, collectionInstructions, dauVertices);
			identifierDataMap.put(parentData.node, parentData);
			List<HierarchicalData> children = new LinkedList<>();
			rootChildMap.put(parentData, children);

			for (OrientVertex childVertex : dauVertices) {
				if (!parentVertices.contains(childVertex)) {
					HierarchicalData childData = createData(childVertex, false, collectionInstructions, dauVertices);
					identifierDataMap.put(childData.node, childData);

					if (children.contains(childData)) {
						throw AdaptationnException.internal("Duplicate node found!");
					}
					children.add(childData);
				}
			}
		}
		return new HierarchicalDataContainer(dataType, identifierDataMap, rootChildMap);
	}

	private void collectProps(@Nonnull OrientVertex src, String value, HashMap<String, Object> collectionBucket) {
		if (src.getProperty(value) instanceof List) {
			collectionBucket.put(value, new LinkedList<>(src.getProperty(value)));
		} else {
			if (src.getProperty(value) == null || src.getProperty(value).equals("")) {
				collectionBucket.put(value, nullValuePlaceholder);
			} else {
				collectionBucket.put(value, src.getProperty(value));
			}
		}
	}

	private HierarchicalData createData(@Nonnull OrientVertex src, boolean isRootObject, @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions,
	                                    List<OrientVertex> otherDauVertices) {
		// Create identification information
		HierarchicalIdentifier identifier = createIdentifier(src);

		DebugData debugData = new DebugData(identifier.getNodeType(), identifier.getSourceIdentifier());

		Map<String, Set<String>> interestedProperties = collectionInstructions.collectedChildProperties;
		Map<String, Set<String>> debugProperties = collectionInstructions.collectedDebugProperties;

		HashMap<String, Object> collectedProps = new HashMap<>();
		if ((interestedProperties != null && interestedProperties.containsKey(identifier.getNodeType())) ||
				debugProperties != null && debugProperties.containsKey(identifier.getNodeType())) {
			Set<String> interestedProps = interestedProperties == null ? null : interestedProperties.get(identifier.getNodeType());
			Set<String> debugPropSet = debugProperties == null ? null : debugProperties.get(identifier.getNodeType());

			for (String value : src.getPropertyKeys()) {
				if (interestedProps != null && interestedProps.contains(value)) {
					collectProps(src, value, collectedProps);
				} else if (debugPropSet != null && debugPropSet.contains(value)) {
					debugData.addAttribute(value, src.getProperty(value));
				}
			}
		}

		Iterator<Edge> parents = src.getEdges(Direction.OUT, "Containment").iterator();

		OrientVertex parentVertex = (OrientVertex) parents.next().getVertex(Direction.IN);
		if (!isRootObject && parentVertex == null) {
			throw AdaptationnException.internal("Node with identifier '" + identifier + "' is a non-root node with no parents!");
		} else if (parents.hasNext()) {
			throw AdaptationnException.internal("Node with identifier '" + identifier + "' has multiple parents!");
		}

		HierarchicalIdentifier parent = isRootObject ? null : createIdentifier(parentVertex);

		Map<String, Set<HierarchicalIdentifier>> childNodeMap = new HashMap<>();
		Iterable<Edge> children = src.getEdges(Direction.IN, "Containment");
		for (Edge e : children) {
			OrientVertex child = (OrientVertex) e.getVertex(Direction.OUT);

			HierarchicalIdentifier childIdentifier = createIdentifier(child);
			Set<HierarchicalIdentifier> childrenSet = childNodeMap.computeIfAbsent(childIdentifier.getNodeType(), k -> new HashSet<>());
			childrenSet.add(childIdentifier);
		}

		Set<HierarchicalIdentifier> inboundReferences = new HashSet<>();
		Set<HierarchicalIdentifier> outboundReferences = new HashSet<>();
		for (Edge e : src.getEdges(Direction.BOTH, "Reference")) {
			if (e.getVertex(Direction.IN).equals(src)) {
				OrientVertex outNode = (OrientVertex) e.getVertex(Direction.OUT);
				if (!otherDauVertices.contains(outNode)) {
					inboundReferences.add(createIdentifier(outNode));
				}
			}
			if (e.getVertex(Direction.OUT).equals(src)) {
				OrientVertex inNode = (OrientVertex) e.getVertex(Direction.IN);
				if (!otherDauVertices.contains(inNode)) {
					outboundReferences.add(createIdentifier(inNode));
				}
			}
		}

		if (debugData.getAttributeSize() == 0 && !Configuration.getInstance().getGlobalTransformationInstructions().taggedNodes.contains(identifier.getNodeType())) {
			debugData = null;
		}

		return new HierarchicalData(
				identifier,
				collectedProps,
				src,
				isRootObject,
				parent,
				inboundReferences,
				outboundReferences,
				childNodeMap,
				debugData);
	}

	@Override
	protected HierarchicalIdentifier createIdentifier(@Nonnull OrientVertex src) {
		return HierarchicalIdentifier.produceTraceableNode("I" + src.getIdentity().toString(), src.getProperty("@class"));
	}

	@Override
	protected void update_removeAttribute(@Nonnull OrientVertex parentNode, @Nonnull String attributeName, String... childPath) {
		if (childPath == null) {
			// If there is no child path, remove the node from the parent if it exists
			parentNode.removeProperty(attributeName);
		} else {
			// Otherwise, search through the child structure
			LinkedList<String> remainingChildren = new LinkedList(Arrays.asList(childPath));

			OrientVertex currentChild = parentNode;

			// For each child node
			while (!remainingChildren.isEmpty()) {
				OrientVertex originalChild = currentChild;

				// Get the Contianment edges for the children
				Iterator edgeIterator = currentChild.getEdges(Direction.IN, "Containment").iterator();

				while (edgeIterator.hasNext() && originalChild.equals(currentChild) && !remainingChildren.isEmpty()) {
					// And iterate through them looking for a match to the current step in the child path
					OrientVertex child = (OrientVertex) ((Edge) edgeIterator.next()).getVertex(Direction.OUT);
					if (child.getLabel().equals(remainingChildren.get(0))) {
						// If a match is found, set the currentChild to the new child, and remove the child since it has been found
						try {
							remainingChildren.remove(0);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
						currentChild = child;
					}
				}
				// If at the end of looking through the edges we haven't found a matching child, break the loop looking for a matching path since one cannot be found
				if (originalChild.equals(currentChild)) {
					break;
				}
			}

			if (remainingChildren.isEmpty()) {
				// Then, if we have arrived at the target node and no children have been found, try and remove the attribute,
				currentChild.removeProperty(attributeName);
			}
		}
	}

	@Override
	protected void update_createOrUpdateChildWithAttributes(@Nonnull OrientVertex parentNode, @Nonnull List<String> childPath,
	                                                        @Nonnull Map<String, Object> attributes) {
		List<String> remainingChildren = new LinkedList<>(childPath);

		OrientVertex currentChild = parentNode;

		// For each child node
		while (!remainingChildren.isEmpty()) {
			OrientVertex originalChild = currentChild;

			// Get the Contianment edges for the children
			Iterator edgeIterator = currentChild.getEdges(Direction.IN, "Containment").iterator();

			while (edgeIterator.hasNext() && originalChild.equals(currentChild) && !remainingChildren.isEmpty()) {
				// And iterate through them looking for a match to the current step in the child path
				OrientVertex child = (OrientVertex) ((Edge) edgeIterator.next()).getVertex(Direction.OUT);
				if (child.getLabel().equals(remainingChildren.get(0))) {
					// If a match is found, set the currentChild to the new child, and remove the child since it has been found
					remainingChildren.remove(0);
					currentChild = child;
				}
			}
			// If at the end of looking through the edges we haven't found a matching child, break the loop looking for a matching path since one cannot be found
			if (originalChild.equals(currentChild)) {
				break;
			}
		}

		while (!remainingChildren.isEmpty()) {
			String nodeType = remainingChildren.remove(0);
			OrientVertex newChild = parentNode.getGraph().addVertex(nodeType, (String) null);
			newChild.addEdge("Containment", currentChild);
			currentChild = newChild;
		}
		for (Map.Entry<String, Object> attributeEntry : attributes.entrySet()) {
			currentChild.setProperty(attributeEntry.getKey(), attributeEntry.getValue());
		}
	}
}
