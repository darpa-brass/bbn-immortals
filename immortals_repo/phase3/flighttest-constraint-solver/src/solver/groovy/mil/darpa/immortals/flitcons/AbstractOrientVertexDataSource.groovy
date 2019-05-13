package mil.darpa.immortals.flitcons


import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientVertex
import com.tinkerpop.gremlin.groovy.Gremlin
import com.tinkerpop.pipes.Pipe
import com.tinkerpop.pipes.branch.LoopPipe
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier
import mil.darpa.immortals.flitcons.reporting.AdaptationnException
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge

abstract class AbstractOrientVertexDataSource implements DataSourceInterface<OrientVertex> {

	static {
		Gremlin.load()
	}

	private OrientGraph testFlightConfigurationGraph

	AbstractOrientVertexDataSource() {
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

	@Override
	LinkedHashMap<OrientVertex, List<OrientVertex>> collectRawDauData() {
		LinkedHashMap<OrientVertex, List<OrientVertex>> rval = new LinkedHashMap<>()

		Set<OrientVertex> dauVertices = testFlightConfigurationGraph.V.has("@class", "MDLRoot").get("NetworkDomain").get("Networks").get("Network").get("NetworkNodes").get("NetworkNode").as("dau").get("TmNSApps").get("TmNSApp").has("TmNSDAU").back("dau").toSet()
		for (OrientVertex dauVertex : dauVertices) {
			rval.put(dauVertex, testFlightConfigurationGraph.V.has("@rid", dauVertex.getId().toString()).iterateObjectTree().toList())
		}
		return rval
	}

	@Override
	LinkedHashMap<OrientVertex, List<OrientVertex>> collectRawMeasurementData() {
		LinkedHashMap<OrientVertex, List<OrientVertex>> rval = new LinkedHashMap<>()

		Set<OrientVertex> dauVertices = testFlightConfigurationGraph.V.has("@class", "MDLRoot").get("MeasurementDomains").get("MeasurementDomain").get("Measurements").get("Measurement").toSet()
		for (OrientVertex dauVertex : dauVertices) {
			rval.put(dauVertex, testFlightConfigurationGraph.V.has("@rid", dauVertex.getId().toString()).iterateObjectTree().toList())
		}
		return rval
	}

	@Override
	Map<OrientVertex, Set<OrientVertex>> collectRawDauMeasurementIndirectRelations() {
		Map<OrientVertex, Set<OrientVertex>> indirectRelations = new HashMap<>()
		testFlightConfigurationGraph.V.fillPortmap(indirectRelations).iterate()
		return indirectRelations
	}

	@Override
	LinkedHashMap<OrientVertex, List<OrientVertex>> collectRawDauInventoryData() {
		LinkedHashMap<OrientVertex, List<OrientVertex>> rval = new LinkedHashMap<>()

		Set<OrientVertex> dauVertices = testFlightConfigurationGraph.V.has("@class", "DAUInventory").get("NetworkNode").toSet()
		for (OrientVertex dauVertex : dauVertices) {
			rval.put(dauVertex, testFlightConfigurationGraph.V.has("@rid", dauVertex.getId().toString()).iterateObjectTree().toList())
		}
		return rval
	}

	@Override
	TreeMap<String, TreeMap<String, Object>> getPortMappingChartData() {
		TreeMap<String, TreeMap<String, Object>> chartData = new TreeMap<>()

		TreeMap<String, Object> currentRow = null
		String referenceTitle
		String targetTitle

		int measurementRefCounter = 0
		int measurementCounter = 0
		int portRefCounter = 0
		int portCounter = 0

		testFlightConfigurationGraph.V.has("@class", "PortMapping").as("portMappings").sideEffect { it ->
			currentRow = new TreeMap<>()
			chartData.put(it.id.toString(), currentRow)
			portCounter = 0
			portRefCounter = 0
			measurementCounter = 0
			measurementRefCounter = 0
		}
				.in("@class", "Containment").has("@class", "PortRef").as("portRefs").sideEffect {
			referenceTitle = "Pr" + portRefCounter++
			currentRow.put(referenceTitle + "#", it.id.toString())
			currentRow.put(referenceTitle + ".IDREF", it.getProperty("IDREF"))
		}.out("@class", "Reference").has("@class", "Port").sideEffect {
			targetTitle = referenceTitle + ".P" + portCounter++
			currentRow.put(targetTitle + "#", it.id.toString())
			currentRow.put(targetTitle + ".ID", it.getProperty("ID"))
		}

				.back("portMappings").in("@class", "Containment").has("@class", "MeasurementRefs").in("@class", "Containment").has("@class", "MeasurementRef").sideEffect {
			referenceTitle = "Mr" + measurementRefCounter++
			currentRow.put(referenceTitle + "#", it.id.toString())
			currentRow.put(referenceTitle + ".IDREF", it.getProperty("IDREF"))
		}.out("@class", "Reference").has("@class", "Measurement").sideEffect {
			targetTitle = referenceTitle + ".M" + measurementCounter++
			currentRow.put(targetTitle + "#", it.id.toString())
			currentRow.put(targetTitle + ".ID", it.getProperty("ID"))
		}.in("@class", "Containment").has("@class", "DataAttributes").as("dataAttributes")

				.in("@class", "Containment").has("@class", "DigitalAttributes").as("digitalAttributes")
				.in("@class", "Containment").has("@class", "SampleRate").in("@class", "Containment").has("@class", "ConditionParameter").sideEffect {
			currentRow.put(targetTitle + ".SR", it.getProperty("ConditionValue"))
		}
				.back("digitalAttributes").in("@class", "Containment").has("@class", "DataRate").in("@class", "Containment").has("@class", "ConditionParameter").sideEffect {
			currentRow.put(targetTitle + ".DR", it.getProperty("ConditionValue"))
		}
				.back("digitalAttributes").in("@class", "Containment").has("@class", "DataLength").in("@class", "Containment").has("@class", "ConditionParameter").sideEffect {
			currentRow.put(targetTitle + ".DL", it.getProperty("ConditionValue"))
		}
				.iterate()

		return chartData
	}

	@Override
	void init() {
		if (testFlightConfigurationGraph == null) {
			String odbUser = ChallengeProblemBridge.getEvaluationUser()
			String odbPassword = ChallengeProblemBridge.getEvaluationPassword()
			String odbTarget = ChallengeProblemBridge.getEvaluationTarget()

			System.out.println("Connecting to '" + odbTarget + "'.")

			testFlightConfigurationGraph = new OrientGraph(
					odbTarget,
					odbUser == null ? "admin" : odbUser,
					odbPassword == null ? "admin" : odbPassword)
//			testFlightConfigurationGraph.setKeepInMemoryReferences(true)
		}
	}

	@Override
	void commit() {
		try {
			if (!SolverConfiguration.getInstance().noCommit) {
				testFlightConfigurationGraph.commit()
			}
		} catch (Exception e) {
			testFlightConfigurationGraph.rollback()
			throw AdaptationnException.internal(e)
		}
	}

	@Override
	void shutdown() {
		if (testFlightConfigurationGraph != null) {
			testFlightConfigurationGraph.shutdown(false)
			testFlightConfigurationGraph = null
		}
	}

	@Override
	void restart() {
		shutdown()
		init()
	}

}
