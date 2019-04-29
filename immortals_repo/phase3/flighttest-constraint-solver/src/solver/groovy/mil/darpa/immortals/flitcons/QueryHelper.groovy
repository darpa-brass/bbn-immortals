package mil.darpa.immortals.flitcons

import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientVertex
import com.tinkerpop.gremlin.groovy.Gremlin
import com.tinkerpop.pipes.Pipe
import com.tinkerpop.pipes.branch.LoopPipe
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge

class QueryHelper {

	static {
		Gremlin.load()
	}

	public final OrientGraph testFlightConfigurationGraph

	QueryHelper() {

		String odbUser = System.getenv(ChallengeProblemBridge.ENV_VAR_EVAL_USER)
		String odbPassword = System.getenv(ChallengeProblemBridge.ENV_VAR_EVAL_PASSWORD)
		testFlightConfigurationGraph = new OrientGraph(
				System.getenv(ChallengeProblemBridge.ENV_VAR_EVAL_ODB),
				odbUser == null ? "admin" : odbUser,
				odbPassword == null ? "admin" : odbPassword)

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

//        Gremlin.defineStep("fillExternalReferencesMap", [Pipe, Pipe], { Map<HierarchicalIdentifier, Set<Edge>> externalReferenceMap ->
//            _().getConfigurationDaus.groupBy(externalReferenceMap)
//                    {
//                        new HierarchicalIdentifier(it.id.toString(), it["@class"])
////                        it.id
//                    }
//                    {
//                        it.getExternalReferences
//                    }
//                    {
//                        new HashSet(it)
//                    }
//        })

		Gremlin.defineStep("fillPortmap", [Pipe, Pipe], { Map<Set<HierarchicalIdentifier>, Set<HierarchicalIdentifier>> portMeasurementMap ->
			def tmp
			_().has("@class", "PortMapping").sideEffect { tmp = it }
					.get("PortRef").groupBy(portMeasurementMap)
					{
						it
					}
					{
						tmp.get("MeasurementRefs").get("MeasurementRef").references("Measurement").toList()
					}
					{
						Set<Object> result = new HashSet<>()

						for (Collection c : it) {
							for (Object val : c) {
								result.add(val)
							}
						}
						return result
					}
		})
	}

	protected Pipe getTestInventoryDausPipe() {
		return testFlightConfigurationGraph.V.has("@class", "DAUInventory").get("NetworkNode")
	}

	protected Pipe getConfigurationDausPipe() {
		return testFlightConfigurationGraph.V.has("@class", "MDLRoot").get("NetworkDomain").get("Networks").get("Network").get("NetworkNodes").get("NetworkNode").as("dau").get("TmNSApps").get("TmNSApp").has("TmNSDAU").back("dau")
	}

	protected Pipe getConfigurationMeasurementsPipe() {
		return testFlightConfigurationGraph.V.has("@class", "MDLRoot").get("MeasurementDomains").get("MeasurementDomain").get("Measurements").get("Measurement")
	}

	LinkedHashMap<OrientVertex, List<OrientVertex>> collectDauData() {
		LinkedHashMap<OrientVertex, List<OrientVertex>> rval = new LinkedHashMap<>()

		Set<OrientVertex> dauVertices = getConfigurationDausPipe().toSet()
		for (OrientVertex dauVertex : dauVertices) {
			rval.put(dauVertex, testFlightConfigurationGraph.V.has("@rid", dauVertex.getId().toString()).iterateObjectTree().toList())
		}
		return rval
	}

	LinkedHashMap<OrientVertex, List<OrientVertex>> collectMeasurementData() {
		LinkedHashMap<OrientVertex, List<OrientVertex>> rval = new LinkedHashMap<>()

		Set<OrientVertex> dauVertices = getConfigurationMeasurementsPipe().toSet()
		for (OrientVertex dauVertex : dauVertices) {
			rval.put(dauVertex, testFlightConfigurationGraph.V.has("@rid", dauVertex.getId().toString()).iterateObjectTree().toList())
		}
		return rval
	}


	Map<OrientVertex, Set<OrientVertex>> getDauMeasurementIndirectRelations() {
		Map<OrientVertex, Set<OrientVertex>> indirectRelations = new HashMap<>()
		testFlightConfigurationGraph.V.fillPortmap(indirectRelations).iterate()
		return indirectRelations
	}

	LinkedHashMap<OrientVertex, List<OrientVertex>> collectDauInventoryData() {
		LinkedHashMap<OrientVertex, List<OrientVertex>> rval = new LinkedHashMap<>()

		Set<OrientVertex> dauVertices = getTestInventoryDausPipe().toSet()
		for (OrientVertex dauVertex : dauVertices) {
			rval.put(dauVertex, testFlightConfigurationGraph.V.has("@rid", dauVertex.getId().toString()).iterateObjectTree().toList())
		}
		return rval
	}
}
