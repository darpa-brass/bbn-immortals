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
				e.remove();
			}
			if (e.getVertex(Direction.OUT).equals(originalNode)) {
				OrientVertex inNode = (OrientVertex) e.getVertex(Direction.IN);
				replacementNode.addEdge("Reference", inNode);
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
			HierarchicalData parentData = createData(parentVertex, true, collectionInstructions);
			identifierDataMap.put(parentData.node, parentData);
			List<HierarchicalData> children = new LinkedList<>();
			rootChildMap.put(parentData, children);
			for (OrientVertex childVertex : input.get(parentVertex)) {
				if (!parentVertices.contains(childVertex)) {

					HierarchicalData childData = createData(childVertex, false, collectionInstructions);
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

	private HierarchicalData createData(@Nonnull OrientVertex src, boolean isRootObject, @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions) {
		// Create identification information
		HierarchicalIdentifier identifier = createIdentifier(src);

		StringBuilder debugLabelBuilder = null;

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
					if (debugLabelBuilder == null) {
						debugLabelBuilder = new StringBuilder(
								"v(" + identifier.getNodeType() + ")[" + identifier.getSourceIdentifier() +
										"]{" + src.getProperty(value));
					} else {
						debugLabelBuilder.append("/").append(src.getProperty(value).toString());
					}
				}
			}
		}

		if (debugLabelBuilder != null) {
			debugLabelBuilder.append("}");
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
				inboundReferences.add(createIdentifier(outNode));
			}
			if (e.getVertex(Direction.OUT).equals(src)) {
				OrientVertex inNode = (OrientVertex) e.getVertex(Direction.IN);
				outboundReferences.add(createIdentifier(inNode));
			}
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
				debugLabelBuilder == null ? null : debugLabelBuilder.toString());
	}

	@Override
	protected HierarchicalIdentifier createIdentifier(@Nonnull OrientVertex src) {
		return HierarchicalIdentifier.produceTraceableNode(src.getIdentity().toString(), src.getProperty("@class"));

	}


}
