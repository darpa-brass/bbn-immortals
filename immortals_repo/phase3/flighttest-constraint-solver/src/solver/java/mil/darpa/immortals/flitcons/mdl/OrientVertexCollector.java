package mil.darpa.immortals.flitcons.mdl;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import mil.darpa.immortals.flitcons.AbstractDataCollector;
import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.QueryHelper;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class OrientVertexCollector extends AbstractDataCollector<OrientVertex> {

	public OrientVertexCollector(@Nonnull Configuration.PropertyCollectionInstructions collectionInstructions,
	                             @Nonnull Configuration.TransformationInstructions transformationInstructions) {
		super(collectionInstructions, transformationInstructions);
	}

	private QueryHelper queryHelper;

	@Override
	protected synchronized void init() {
		if (queryHelper == null) {
			queryHelper = new QueryHelper();
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
	protected LinkedHashMap<OrientVertex, List<OrientVertex>> collectRawDauData() {
		init();
		return queryHelper.collectDauData();
	}

	@Override
	protected LinkedHashMap<OrientVertex, List<OrientVertex>> collectRawMeasurementData() {
		init();
		return queryHelper.collectMeasurementData();
	}

	@Override
	protected Map<OrientVertex, Set<OrientVertex>> getRawDauMeasurementIndirectRelations() {
		init();
		return queryHelper.getDauMeasurementIndirectRelations();
	}

	@Override
	protected LinkedHashMap<OrientVertex, List<OrientVertex>> collectRawDauInventoryData() {
		init();
		return queryHelper.collectDauInventoryData();
	}

	@Override
	protected HierarchicalDataContainer createContainer(@Nonnull LinkedHashMap<OrientVertex, List<OrientVertex>> input, @Nonnull Set<String> valuesToDefaultToTrue, @Nullable Map<String, Set<String>> interestedProperties, @Nullable Map<String, Set<String>> debugProperties) {
		LinkedHashMap<HierarchicalIdentifier, HierarchicalData> identifierDataMap = new LinkedHashMap<>();
		LinkedHashMap<HierarchicalData, List<HierarchicalData>> rootChildMap = new LinkedHashMap<>();

		Set<OrientVertex> parentVertices = input.keySet();

		for (OrientVertex parentVertex : parentVertices) {
			HierarchicalData parentData = createData(parentVertex, true, valuesToDefaultToTrue, interestedProperties, debugProperties);
			identifierDataMap.put(parentData.node, parentData);
			List<HierarchicalData> children = new LinkedList<>();
			rootChildMap.put(parentData, children);
			for (OrientVertex childVertex : input.get(parentVertex)) {
				if (!parentVertices.contains(childVertex)) {

					HierarchicalData childData = createData(childVertex, false, valuesToDefaultToTrue, interestedProperties, debugProperties);
					identifierDataMap.put(childData.node, childData);

					if (children.contains(childData)) {
						throw new RuntimeException("Duplicate node found!");
					}
					children.add(childData);
				}
			}

		}

		return new HierarchicalDataContainer(identifierDataMap, rootChildMap);
	}

	@Override
	protected HierarchicalData createData(@Nonnull OrientVertex src, boolean isRootObject, @Nonnull Set<String> valuesToDefaultToTrue, @Nullable Map<String, Set<String>> interestedProperties, @Nullable Map<String, Set<String>> debugProperties) {
		// Create identification information
		HierarchicalIdentifier identifier = createIdentifier(src);

		HashMap<String, Object> collectedProps = new HashMap<>();
		HashMap<String, Object> debugProps = new HashMap<>();
		if ((interestedProperties != null && interestedProperties.containsKey(identifier.getNodeType())) ||
				debugProperties != null && debugProperties.containsKey(identifier.getNodeType())) {
			Set<String> interestedProps = interestedProperties == null ? null : interestedProperties.get(identifier.getNodeType());
			Set<String> debugPropSet = debugProperties == null ? null : debugProperties.get(identifier.getNodeType());


			for (String value : src.getPropertyKeys()) {
				if (interestedProps != null && interestedProps.contains(value)) {
					if (src.getProperty(value) instanceof List) {
						if (src.getProperty(value) == null && valuesToDefaultToTrue.contains(value)) {
							throw new RuntimeException("A list value cannot utilize a default value!");
						}
						collectedProps.put(value, new LinkedList<>(src.getProperty(value)));
					} else {
						if ((src.getProperty(value) == null || src.getProperty(value).equals("")) && valuesToDefaultToTrue.contains(value)) {
							collectedProps.put(value, true);
						} else {
							collectedProps.put(value, src.getProperty(value));
						}
					}
				} else if (debugPropSet != null && debugPropSet.contains(value)) {
					if (src.getProperty(value) instanceof List) {
						if (src.getProperty(value) == null && valuesToDefaultToTrue.contains(value)) {
							throw new RuntimeException("A list value cannot utilize a default value!");
						}
						debugProps.put(value, new LinkedList<>(src.getProperty(value)));
					} else {
						if ((src.getProperty(value) == null || src.getProperty(value).equals("")) && valuesToDefaultToTrue.contains(value)) {
							debugProps.put(value, true);
						} else {
							debugProps.put(value, src.getProperty(value));
						}
					}
				}
			}
		}


		Iterator<Edge> parents = src.getEdges(Direction.OUT, "Containment").iterator();
		OrientVertex parentVertex = (OrientVertex) parents.next().getVertex(Direction.IN);
		if (!isRootObject && parentVertex == null) {
			throw new RuntimeException("Node with identifier '" + identifier + "' is a non-root node with no parents!");
		} else if (parents.hasNext()) {
			throw new RuntimeException("Node with identifier '" + identifier + "' has multiple parents!");
		}

		HierarchicalIdentifier parent = isRootObject ? null : createIdentifier(parentVertex);

		Map<String, Set<HierarchicalIdentifier>> childNodeMap = new HashMap<>();
		Iterable<Edge> children = src.getEdges(Direction.IN, "Containment");
		for (Edge e : children) {
			OrientVertex child = (OrientVertex) e.getVertex(Direction.OUT);

			if (child.getProperty("@class").equals("PortType")) {
				System.out.println("MEH");
			}

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
				debugProps);
	}

	private static HierarchicalIdentifier createIdentifier(@Nonnull OrientVertex src) {
		return new HierarchicalIdentifier(src.getIdentity().toString(), src.getProperty("@class"));

	}

	@Override
	protected Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> convertOneToManyMap(@Nonnull Map<OrientVertex, Set<OrientVertex>> indirectRelations) {
		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> rval = new HashMap<>();

		for (Map.Entry<OrientVertex, Set<OrientVertex>> entry : indirectRelations.entrySet()) {
			HierarchicalIdentifier primaryNode = createIdentifier(entry.getKey());
			Set<HierarchicalIdentifier> relationSet = rval.computeIfAbsent(primaryNode, k -> new HashSet<>());
			relationSet.addAll(entry.getValue().stream().map(OrientVertexCollector::createIdentifier).collect(Collectors.toSet()));
		}
		return rval;
	}

}
