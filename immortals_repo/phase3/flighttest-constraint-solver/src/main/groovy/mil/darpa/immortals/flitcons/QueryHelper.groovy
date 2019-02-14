package mil.darpa.immortals.flitcons

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientVertex
import com.tinkerpop.gremlin.groovy.Gremlin
import com.tinkerpop.pipes.Pipe
import com.tinkerpop.pipes.branch.LoopPipe
import mil.darpa.immortals.flitcons.datatypes.HierarchicalNode
import mil.darpa.immortals.flitcons.datatypes.ScenarioData
import mil.darpa.immortals.flitcons.datatypes.HierarchicalData
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge

class QueryHelper {

	static {
		Gremlin.load()
	}

	public final OrientGraph testFlightConfigurationGraph
	public final OrientGraph dauInventoryGraph
	public final OrientGraph otherGraph

	private static HierarchicalData getOrCreateNodeData(OrientVertex vertex, Map<OrientVertex, HierarchicalData> targetDataSet, boolean isRootNode) {
		HierarchicalData primaryNodeData
		if (!targetDataSet.containsKey(vertex)) {

			primaryNodeData = new HierarchicalData(
					vertex.getLabel(),
					vertex.getProperties(),
					vertex.toString(),
					vertex,
					isRootNode
			)

			targetDataSet.put(vertex, primaryNodeData)

		} else {
			primaryNodeData = targetDataSet.get(vertex)
		}
		return primaryNodeData
	}

	static HierarchicalNode vertexToHierarchicalNode(OrientVertex v) {
		return new HierarchicalNode(v.getIdentity().toString(), (String) v.getProperty("@class"))
	}

	public static class VertexComparer {
		Map<Vertex, String> seenNodeMap = new HashMap();
	}

//	public List<String> compareGraphs(OrientGraph g0, OrientGraph g1, String referenceNodeType) {
//		int seenCount = 0;
//		Set<Vertex> seenNodes0 = new HashSet<>()
//		Set<Vertex> seenNodes1 = new HashSet<>()
//
//
//		List<Vertex> values0 = g0.V.toList();
//		List<Vertex> values1 = g1.V.toList();
//
//		List<Vertex> v0 = values0.findAll { Vertex t ->
//			t.getProperty("@class").equals(referenceNodeType)
//		}
//
//		List<Vertex> v1 = values1.findAll { Vertex t ->
//			t.getProperty("@class").equals(referenceNodeType)
//		}
//
//		System.out.println("MEH");
//	}

//	public void compareVertices(Vertex v0, Vertex v1, List<String> path, List<String> errors) {
//		if (v0.properties.size() != v1.properties.size()) {
//			errors.add(String.join(".", path) + ": Differing parameter count! Vertex 0: " + v0.properties.size() + ", Vertex 1: " + v1.properties.size())
//			return;
//		}
//		for (String propertyIdentifier : v0.properties.keySet()) {
//			assert (v1.properties.containsKey(propertyIdentifier))
//		}
//
//		List<Edge> v0InEdges = v0.getEdges(Direction.IN).toList()
//		List<Edge> v0OutEdges = v0.getEdges(Direction.OUT).toList()
//		List<Edge> v1InEdges = v1.getEdges(Direction.IN).toList()
//		List<Edge> v1OutEdges = v1.getEdges(Direction.OUT).toList()
//
//		if (v0InEdges.size() == v1InEdges.size()) {
//
//		} else {
//			errors.add("v0InEdges=" + v0InEdges.size() + ", v1InEdges=" + v1InEdges.size() + "!")
//			return
//		}
//
//		if (v0OutEdges.size() == v1OutEdges.size()) {
//
//		} else {
//			errors.add("v0OutEdges=" + v0OutEdges.size() + ", v1OutEdges=" + v1OutEdges.size() + "!")
//		}
//
//
//	}

	QueryHelper() {

//        testFlightConfigurationGraph = new OrientGraph("remote:127.0.0.1/BRASS_Scenario5_BeforeAdaptation", "reader", "reader")
		String odbUser = System.getenv(ChallengeProblemBridge.ENV_VAR_EVAL_USER)
		String odbPassword = System.getenv(ChallengeProblemBridge.ENV_VAR_EVAL_PASSWORD)
		testFlightConfigurationGraph = new OrientGraph(
				System.getenv(ChallengeProblemBridge.ENV_VAR_EVAL_ODB),
				odbUser == null ? "admin" : odbUser,
				odbPassword == null ? "admin" : odbPassword)
//		dauInventoryGraph = new OrientGraph("remote:127.0.0.1/BRASS_Scenario5_AfterAdaptation", "reader", "reader")
//		dauInventoryGraph = new OrientGraph("remote:127.0.0.1/TestArticleNetworkInventory", "reader", "reader")

//		otherGraph = new OrientGraph("remote:127.0.0.1/OtherGraph", "reader", "reader")

		Gremlin.defineStep("get", [Pipe, Pipe], { String className ->
			_().in("Containment").has("@class", className)
		})

		Gremlin.defineStep("references", [Pipe, Pipe], { String className ->
			_().out("Reference").has("@class", className)
		})

		Gremlin.defineStep("iterateObjectTree", [Pipe, Pipe], { loopBundleFunc ->
			_().copySplit(
					_()
					,
					_().as("parent").sideEffect
					{
						if (loopBundleFunc != null) {
							loopBundleFunc(new LoopPipe.LoopBundle<Vertex>(it, [it], 0))
						}
					}
					.as("nn").in("Containment").loop("nn") {
						if (loopBundleFunc != null) {
							loopBundleFunc(it)
						}
						it.object.both.hasNext()
					} {
						if (loopBundleFunc != null) {
							loopBundleFunc(it)
						}
						true
					}.enablePath
			).fairMerge
		})

//		Gremlin.defineStep("getConfigurationDaus", [Pipe, Pipe], {
//			_().as("dau").get("TmNSApps").get("TmNSApp").has("TmNSDAU").back("dau")
//		})

		Gremlin.defineStep("getInventoryDaus", [Pipe, Pipe], {
			_().has("@class", "DAUInventory").in
		})

		Gremlin.defineStep("getMeasurements", [Pipe, Pipe], {
			_().get("MeasurementDomains").get("MeasurementDomain").get("Measurements").get("Measurement")
		})

		Gremlin.defineStep("getExternalReferences", [Pipe, Pipe], {
			def dau_nodes = []

			_().iterateObjectTree.aggregate(dau_nodes).bothE("Reference").filter({
				!(dau_nodes.contains(it.outV.next()) && dau_nodes.contains(it.inV.next()))
			})
		})

//        Gremlin.defineStep("fillExternalReferencesMap", [Pipe, Pipe], { Map<HierarchicalNode, Set<Edge>> externalReferenceMap ->
//            _().getConfigurationDaus.groupBy(externalReferenceMap)
//                    {
//                        new HierarchicalNode(it.id.toString(), it["@class"])
////                        it.id
//                    }
//                    {
//                        it.getExternalReferences
//                    }
//                    {
//                        new HashSet(it)
//                    }
//        })

		Gremlin.defineStep("fillPortmap", [Pipe, Pipe], { Map<HierarchicalNode, Set<HierarchicalNode>> portMeasurementMap ->
			def tmp
			_().has("@class", "PortMapping").sideEffect { tmp = it }
					.get("PortRef").groupBy(portMeasurementMap)
					{
						new HierarchicalNode(it.id.toString(), it["@class"])
					}
					{
						tmp.get("MeasurementRefs").get("MeasurementRef").references("Measurement").toList()
					}
					{
						Set<HierarchicalNode> result = new HashSet<>()

						for (Collection c : it) {
							for (Vertex val : c) {
								result.add(new HierarchicalNode(val.id.toString(), val["@class"]))
							}
						}
						return result
					}
		})
	}

	protected Pipe getTestInventoryDausPipe() {
		return testFlightConfigurationGraph.V.has("@class", "DAUInventory").in
	}

	protected Pipe getConfigurationDausPipe() {
		// TODO: Make more flexible
		return testFlightConfigurationGraph.V.has("@class", "MDLRoot").get("NetworkDomain").get("Networks").get("Network").get("NetworkNodes").get("NetworkNode").as("dau").get("TmNSApps").get("TmNSApp").has("TmNSDAU").back("dau")
	}

	protected Pipe getConfigurationMeasurementsPipe() {
		return testFlightConfigurationGraph.V.has("@class", "MDLRoot").get("MeasurementDomains").get("MeasurementDomain").get("Measurements").get("Measurement")
	}

	protected synchronized void serializeRootObjectStructureIntoSet(OrientVertex ov, Map<OrientVertex, HierarchicalData> existingNodeSet) {
		int nodeCount = 0
		Set<Vertex> seenNodes = new HashSet<>()

		ov.graph.v(ov.identity.toString())
				.iterateObjectTree({
			OrientVertex primaryVertex = it.object

			if (!seenNodes.contains(primaryVertex)) {
				nodeCount++
				seenNodes.add(primaryVertex)
			}

			HierarchicalData primaryNodeData = getOrCreateNodeData(primaryVertex, existingNodeSet, primaryVertex == ov)

			List<Edge> edges = primaryVertex.getEdges(Direction.OUT, "Containment").toList()
			if (edges.size() > 1) {
				throw new RuntimeException("Each node is expected to only have one parent!")
			} else if (edges.size() == 1) {
				OrientVertex parentVertex = (OrientVertex) edges.get(0).getVertex(Direction.IN)
				HierarchicalData parentNodeData = getOrCreateNodeData(parentVertex, existingNodeSet, parentVertex == ov)
				if (primaryNodeData.parentNode == null) {
					primaryNodeData.setParentNode(parentNodeData)

				} else if (parentNodeData != primaryNodeData.parentNode) {
					throw new RuntimeException("Node already has a parent assigned to it!")
				}
			}

		}).iterate()

		System.out.println("Node Count: " + Integer.toString(nodeCount))
	}

	synchronized Map<HierarchicalData, Set<HierarchicalData>> getTestInventoryDauData() {
		Map<HierarchicalData, Set<HierarchicalData>> rval = new HashMap<>()

		Set<OrientVertex> dauVertices = testFlightConfigurationGraph.V.getInventoryDaus.toSet()

		for (OrientVertex dau : dauVertices) {
			Map<OrientVertex, HierarchicalData> vertexGraphDataMap = new HashMap<>()
			HierarchicalData dauGraphData = getOrCreateNodeData(dau, vertexGraphDataMap, true)

			serializeRootObjectStructureIntoSet(dau, vertexGraphDataMap)

			rval.put(dauGraphData, new HashSet<>(vertexGraphDataMap.values()))
		}

		return rval
	}

	ScenarioData getRawScenarioData() {
		Set <OrientVertex> dauVertices = getConfigurationDausPipe().toSet()
		Set<OrientVertex> measurementVertices = getConfigurationMeasurementsPipe().toSet()
		ScenarioDataCollector sdc = new ScenarioDataCollector(dauVertices, measurementVertices)

		getConfigurationDausPipe().iterateObjectTree({ sdc.analyzeDauNode(it) }).iterate()
		getConfigurationMeasurementsPipe().iterateObjectTree({ sdc.analyzeExternalNode(it) }).iterate()

		testFlightConfigurationGraph.V.fillPortmap(sdc.data.indirectRelations).iterate()
		return sdc.data
	}
}
