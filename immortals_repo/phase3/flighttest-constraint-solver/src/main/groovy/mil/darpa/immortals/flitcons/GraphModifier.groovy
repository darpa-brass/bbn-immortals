package mil.darpa.immortals.flitcons


import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientVertex
import mil.darpa.immortals.flitcons.datatypes.HierarchicalData

class GraphModifier extends QueryHelper {

//	Map<Vertex, SimpleGraphData> _originalVertexDataMap = new HashMap<>()

//	private static SimpleGraphData getOrCreateNodeData(OrientVertex vertex, Map<Vertex, SimpleGraphData> nodeDataMap) {
//		SimpleGraphData primaryNodeData
//		if (!nodeDataMap.containsKey(vertex)) {
//			primaryNodeData = SimpleGraphData.createFromVertex(vertex)
//			nodeDataMap.put(vertex, primaryNodeData)
//
//		} else {
//			primaryNodeData = nodeDataMap.get(vertex)
//		}
//		return primaryNodeData
//	}

	public void swapReference(OrientVertex oldVertex, OrientVertex newVertex) {

	}

	public void getPort(OrientVertex oldVertex, OrientVertex newVertex) {

	}


	private static synchronized Map<HierarchicalData, Vertex> insertNodesDataIntoGraph(Map<Vertex, HierarchicalData> nodeDataMap, OrientGraph targetGraph) {
		Map<HierarchicalData, Vertex> graphDataNewVertexMap = new HashMap<>()

		for (HierarchicalData gd : nodeDataMap.values()) {
			Map<String, Object> newAttributes = new HashMap<>(gd.attributes)
			newAttributes.put("@class", gd.nodeClass)
			OrientVertex newVertex = targetGraph.addVertex(null, newAttributes)
			graphDataNewVertexMap.put(gd, newVertex)
		}

		for (HierarchicalData gd : graphDataNewVertexMap.keySet()) {
			if (gd.parentNode != null) {
				Vertex childVertex = graphDataNewVertexMap.get(gd)
				Vertex parentVertex = graphDataNewVertexMap.get(gd.parentNode)
				targetGraph.addEdge(null, childVertex, parentVertex, "Containment")
			}
		}

		targetGraph.commit()
		return graphDataNewVertexMap
	}

	GraphModifier() {

	}



//	private static synchronized Map<Vertex, SimpleGraphData> serializeDauToMap(OrientGraph sourceGraph, String dauManufacturer, String dauModel) {
//		Map<Vertex, SimpleGraphData> vertexDataMap = new HashMap<>()
//		int nodeCount = 0
//		Set<Vertex> seenNodes = new HashSet<>()
//
//		Graph g = sourceGraph
//		OrientVertex originalDauVertex = null
//
//		g.V.has("Manufacturer", dauManufacturer).has("Model", dauModel)
//				.sideEffect {
//			if (originalDauVertex != null) {
//				throw new RuntimeException("Only one DAU with the specified manufacturer and model should be in the DAU Inventory!")
//			}
//			originalDauVertex = it
//		}
//		.iterateObjectTree({
//			OrientVertex primaryVertex = it.object
//
//			if (!seenNodes.contains(primaryVertex)) {
//				nodeCount++
//				seenNodes.add(primaryVertex)
//			}
//
//			SimpleGraphData primaryNodeData = getOrCreateNodeData(primaryVertex, vertexDataMap)
//
//			List<Edge> edges = primaryVertex.getEdges(Direction.OUT, "Containment").toList()
//			if (edges.size() > 1) {
//				throw new RuntimeException("Each node is expected to only have one parent!")
//			} else if (edges.size() == 1) {
//				OrientVertex parentVertex = (OrientVertex) edges.get(0).getVertex(Direction.IN)
//				SimpleGraphData parentNodeData = getOrCreateNodeData(parentVertex, vertexDataMap)
//				primaryNodeData.parentNode = parentNodeData
//			}
//
//		}).iterate()
//
//		System.out.println("Node Count: " + Integer.toString(nodeCount))
//
//		return vertexDataMap
//	}

//	static synchronized Set<SimpleGraphData> serializeDau(OrientGraph sourceGraph, String dauManufacturer, String dauModel) {
//		return serializeDauToMap(sourceGraph, dauManufacturer, dauModel).values()
//	}

//	synchronized void cloneDauFromInventoryToTestFlightConfiguration(String dauManufacturer, String dauModel) {
//		OrientVertex dau = getConfigurationDausPipe().has("Manufacturer", dauManufacturer).has("Model", dauModel).toList().get(0)
//
//		Map<OrientVertex, SimpleGraphData> dauStructure = new HashMap<>()
//		serializeObjectStructureIntoSet(dau, dauStructure)
//
//		insertNodesDataIntoGraph(dauStructure, testFlightConfigurationGraph)
//
//		System.out.println("Inserted Node Count: " + Integer.toString(dauStructure.size()))
//	}
}
