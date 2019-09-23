package mil.darpa.immortals.flitcons

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientVertex
import com.tinkerpop.gremlin.groovy.Gremlin
import com.tinkerpop.pipes.Pipe
import com.tinkerpop.pipes.branch.LoopPipe
import mil.darpa.immortals.EnvironmentConfiguration
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier
import mil.darpa.immortals.flitcons.mdl.validation.*
import mil.darpa.immortals.flitcons.reporting.AdaptationnException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.Nullable

abstract class AbstractOrientVertexDataSource extends AbstractDataTarget<OrientVertex> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractOrientVertexDataSource.class)

	static {
		Gremlin.load()
	}

	private OrientGraph testFlightConfigurationGraph

	private String serverPath

	public String getServerPath() {
		return serverPath;
	}

	AbstractOrientVertexDataSource(@Nullable String serverPath) {
		super()

		if (serverPath == null) {
			this.serverPath = EnvironmentConfiguration.odbTarget
		} else {
			this.serverPath = serverPath

		}

		Gremlin.defineStep("get", [Pipe, Pipe], { String className ->
			_().in("Containment").has("@class", className)
		})

		Gremlin.defineStep("getParent", [Pipe, Pipe], { String className ->
			_().out("Containment").has("@class", className)
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
					.get("PortRef").as("dauPortRef").references("Port").getParent("Ports").getParent("Module").back("dauPortRef").groupBy(portMeasurementMap)
					{
						it
					}
					{
						List l = tmp.get("MeasurementRefs").get("MeasurementRef").references("Measurement").toList()
						l.addAll(tmp.get("DataStreamRefs").get("DataStreamRef").references("DataStream").toList())
						l.addAll(tmp.get("PortRef").references("Port").as("devicePort").getParent("Ports").getParent("DeviceModule").back("devicePort").toList())
						return l
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
	protected LinkedHashMap<OrientVertex, List<OrientVertex>> collectRawInputData() {
		LinkedHashMap<OrientVertex, List<OrientVertex>> rval = new LinkedHashMap<>()

		Set<OrientVertex> dauVertices = testFlightConfigurationGraph.V.has("@class", "MDLRoot").get("NetworkDomain").get("Networks").get("Network").get("NetworkNodes").get("NetworkNode").as("dau").get("TmNSApps").get("TmNSApp").has("TmNSDAU").back("dau").toSet()
		for (OrientVertex dauVertex : dauVertices) {
			rval.put(dauVertex, testFlightConfigurationGraph.V.has("@rid", dauVertex.getId().toString()).iterateObjectTree().toList())
		}
		return rval
	}

	@Override
	protected LinkedHashMap<OrientVertex, List<OrientVertex>> collectRawExternalData() {
		LinkedHashMap<OrientVertex, List<OrientVertex>> rval = new LinkedHashMap<>()

		Set<OrientVertex> measurementVertices = testFlightConfigurationGraph.V.has("@class", "MDLRoot").get("MeasurementDomains").get("MeasurementDomain").get("Measurements").get("Measurement").toSet()
		measurementVertices.addAll(testFlightConfigurationGraph.V.has("@class", "MDLRoot").get("MeasurementDomains").get("MeasurementDomain").get("DataStreams").get("DataStream").toSet())
		measurementVertices.addAll(testFlightConfigurationGraph.V.has("@class", "MDLRoot").get("NetworkDomain").get("Networks").get("Network").get("Devices").get("Device").toSet())


		for (OrientVertex dauVertex : measurementVertices) {
			rval.put(dauVertex, testFlightConfigurationGraph.V.has("@rid", dauVertex.getId().toString()).iterateObjectTree().toList())
		}
		return rval
	}

	@Override
	protected Map<OrientVertex, Set<OrientVertex>> collectRawInputExternalDataIndirectRelations() {
		Map<OrientVertex, Set<OrientVertex>> indirectRelations = new HashMap<>()
		testFlightConfigurationGraph.V.fillPortmap(indirectRelations).iterate()
		return indirectRelations
	}

	@Override
	protected LinkedHashMap<OrientVertex, List<OrientVertex>> collectRawInventoryData() {
		LinkedHashMap<OrientVertex, List<OrientVertex>> rval = new LinkedHashMap<>()

		Set<OrientVertex> dauVertices = testFlightConfigurationGraph.V.has("@class", "DAUInventory").get("NetworkNode").toSet()
		for (OrientVertex dauVertex : dauVertices) {
			rval.put(dauVertex, testFlightConfigurationGraph.V.has("@rid", dauVertex.getId().toString()).iterateObjectTree().toList())
		}
		return rval
	}

	private static OrientVertex getParent(OrientVertex vertex) {
		return vertex.getEdges(Direction.OUT, "Containment").iterator().next().getVertex(Direction.IN)
	}

	@Override
	public List<String> validateRelationalIntegrity() {
		Map<String, Integer> errorCount = new HashMap<>()

		List<String> seenIds = new LinkedList<>()
		List<OrientVertex> vertices = testFlightConfigurationGraph.V.has("ID").toList();

		for (OrientVertex vertex : vertices) {
			String value = vertex.getProperty("ID")
			if (seenIds.contains(value)) {
				// TODO: Resolve
				String err = "The ID value '" + value + "' is assigned to multiple elements!";
				if (errorCount.containsKey(err)) {
					errorCount.put(err, errorCount.get(err) + 1)
				} else {
					errorCount.put(err, 1)
				}
			} else {
				seenIds.add(value)
			}
		}

		vertices = testFlightConfigurationGraph.V.has("IDREF").toList()
		List<String> seenPortRefValues = new LinkedList<>();

		for (OrientVertex vertex : vertices) {
			OrientVertex parentVertex = getParent(vertex)
			String value = vertex.getProperty("IDREF");

		}

		for (OrientVertex vertex : vertices) {
			OrientVertex parentVertex = getParent(vertex)
			String value = vertex.getProperty("IDREF");

			if (!seenIds.contains(value) && !value.equals("dscp-0")) {
				String err = "The IDREF value of '" + value + "' references a non-existing ID!";
				if (errorCount.containsKey(err)) {
					errorCount.put(err, errorCount.get(err) + 1);
				} else {
					errorCount.put(err, 1);
				}
			}

			if (parentVertex.getProperty("@class").equals("PortRef")) {
				OrientVertex parentParentVertex = getParent(parentVertex)
				if (parentParentVertex.getProperty("@class").equals("PortMapping")) {
					if (seenPortRefValues.contains(value)) {
						String err = "The IDREF value of '" + value + "' is used by multiple PortMapping PortRefs!";
						if (errorCount.containsKey(err)) {
							errorCount.put(err, errorCount.get(err) + 1);
						} else {
							errorCount.put(err, 1);
						}
					} else {
						seenPortRefValues.add(value);
					}
				}
			}
		}
		if (!errorCount.isEmpty()) {
			List<String> rval = new LinkedList<>();
			for (Map.Entry<String, Integer> entry : errorCount.entrySet()) {
				rval.add(entry.getKey() + " [" + entry.getValue() + " Occurrences]");
			}
			return rval
		}
		return null;
	}

	@Override
	Map<String, PortMapping> getPortMappingDetails() {
		init()

		Map<String, PortMapping> rval = new HashMap()

		PortMapping currentPortMapping = null
		Measurement currentMeasurement = null
		DataStream currentDataStream = null
		DauPort currentDauPort = null
		DevicePort currentDevicePort = null

		Set<OrientVertex> portrefs = null
		Set<OrientVertex> daurefs = null
		Set<OrientVertex> devicerefs = null


		testFlightConfigurationGraph.V.has("@class", "MDLRoot")
				.in.has("@class", "NetworkDomain")
				.in.has("@class", "Networks")
				.in.has("@class", "Network")
				.in.has("@class", "PortMappings")
				.in.has("@class", "PortMapping")
				.sideEffect {
					String portMappingId = (String) it['id']
					currentPortMapping = new PortMapping(it['ID'] as String)

					portrefs = new HashSet<>()
					daurefs = new HashSet<>()
					devicerefs = new HashSet<>()

					rval.put(portMappingId, currentPortMapping)
					it.in.has('@class', 'PortRef').aggregate(portrefs).iterate()
				}
				.sideEffect {
					String dauId
					boolean flaggedForReplacement = false
					it.in.has('@class', 'PortRef').as('pr')
							.out.has('@class', 'Port').as('p')
							.out.has('@class', 'Ports').out.has('@class', 'Module').out.has('@class', 'Modules')
							.out.has('@class', 'InternalStructure').out.has('@class', 'NetworkNode')
							.sideEffect { dauId = it['ID'] }
							.in.has('@class', 'GenericParameter').sideEffect {
						flaggedForReplacement = it.propertyKeys.contains('BBNDauFlaggedForReplacement')
					}
							.back('p')
							.sideEffect {
								String dauPortId = it['ID'] as String
								currentDauPort = new DauPort(dauPortId, dauId)
								currentDauPort.isFlaggedForRemoval = flaggedForReplacement
								currentPortMapping.dauPorts.put(dauPortId, currentDauPort)
								currentDauPort.direction = it['PortDirection'] as String
							}
							.copySplit(
									_().in.has('@class', 'PortTypes').has('PortType').sideEffect {
										currentDauPort.portType = it['PortType'] as String
									},
									_().in.has('@class', 'PortTypes').in.has('@class', 'PortType').sideEffect {
										currentDauPort.portType = it['PortType'] as String
										currentDauPort.thermocouple = it['Thermocouple'] as String
									},
									_().in.has('@class', 'GenericParameter').sideEffect {
										currentDauPort.excitationPortIsPresent = it.propertyKeys.contains('ExcitationPortIsPresent')
									},
									_().in.has('@class', 'GenericParameter').in.has('@class', 'PortType').has('Thermocouple').sideEffect {
										currentDauPort.requirements.validThermocouples.add(it['Thermocouple'] as String)
									},
									_().in.has('@class', 'GenericParameter').in.has('@class', 'Measurement').sideEffect {
										DauPort.PortMeasurementCombination measurementCombination = new DauPort.PortMeasurementCombination()
										currentDauPort.requirements.validMeasurementCombinations.add(measurementCombination)

										// Check if has measurement
										if (it.propertyKeys.contains('SampleRate')) {
											measurementCombination.sampleRate = Long.valueOf((String) it['SampleRate'])
										}

										if (it.propertyKeys.contains('DataRate')) {
											measurementCombination.dataRate = Long.valueOf((String) it['DataRate'])
										}

										if (it.propertyKeys.contains('DataLength')) {
											measurementCombination.dataLength = Long.valueOf((String) it['DataLength'])
										}
									}
							).fairMerge
							.back('pr').aggregate(daurefs)
							.iterate()
				}
				.sideEffect {
					String excitationSource
					it.in.has('@class', 'PortRef').as('pr')
							.out.has('@class', 'Port').as('p')
							.out.has('@class', 'Ports').out.has('@class', 'DeviceModule')
							.sideEffect { excitationSource = it['ExcitationSource'] }
							.back('p')
							.sideEffect {
								String devicePortId = it['ID'] as String
								currentDevicePort = new DevicePort(devicePortId)
								currentDevicePort.excitationSource = excitationSource
								currentDevicePort.direction = it['PortDirection']
								currentPortMapping.devicePorts.put(devicePortId, currentDevicePort)
							}
							.back('pr').aggregate(devicerefs)
							.iterate()
				}
				.sideEffect {
					it.in.has('@class', 'MeasurementRefs').in.has('@class', 'MeasurementRef')
							.out.has('@class', 'Measurement')

							.sideEffect {
								String measurementId = it['ID'] as String
								currentMeasurement = new Measurement(measurementId);
								currentPortMapping.measurements.put(measurementId, currentMeasurement)
							}

							.sideEffect {
								it.in.has('@class', 'GenericParameter')
										.copySplit(
												_().in.has("@class", "SampleRate").sideEffect {
													currentMeasurement.requirements.minSampleRate = it.getProperty('Min') as Long
													currentMeasurement.requirements.maxSampleRate = it.getProperty('Max') as Long
												},
												_().in.has('@class', 'DataRate').sideEffect {
													currentMeasurement.requirements.minDataRate = it.getProperty('Min') as Long
													currentMeasurement.requirements.maxDataRate = it.getProperty('Max') as Long

												},
												_().in.has('@class', 'DataLength').sideEffect {
													currentMeasurement.requirements.minDataLength = it.getProperty('Min') as Long
													currentMeasurement.requirements.maxDataLength = it.getProperty('Max') as Long
												}
										).fairMerge.iterate()
							}

					.in.has('@class', 'DataAttributes').in.has('@class', 'DigitalAttributes')
							.copySplit(
									_().in.has('@class', 'DataLength').in.has('@class', 'ConditionParameter').sideEffect {
										String value = it.getProperty('ConditionValue')
										currentMeasurement.dataLength = value as Long
									},
									_().in.has('@class', 'DataRate').in.has('@class', 'ConditionParameter').sideEffect {
										String value = it.getProperty('ConditionValue')
										currentMeasurement.dataRate = value as Long
									},
									_().in.has('@class', 'SampleRate').in.has('@class', 'ConditionParameter').sideEffect {
										String value = it.getProperty('ConditionValue')
										currentMeasurement.sampleRate = value as Long
									}
							).fairMerge.iterate()
				}
				.sideEffect {
					it.in.has('@class', 'DataStreamRefs').in.has('@class', 'DataStreamRef').as('dr')
							.out.has('@class', 'DataStream')
							.sideEffect {
								String dataStreamId = it['ID'] as String
								currentDataStream = new DataStream(dataStreamId);
								currentPortMapping.dataStreams.put(dataStreamId, currentDataStream)
							}
							.back('dr')
							.iterate()
				}
				.sideEffect {
					int dar = daurefs.size()
					int der = devicerefs.size()
					int pr = portrefs.size()
					int ar = dar + der
					if (pr != ar) {
						throw new RuntimeException(
								"Out of " + pr + " total PortRefs in PortMapping with ID='"
										+ it['ID'] + "', " + dar +
										" are connected to Dau Module Ports and " + der +
										" are connected to Device DeviceModule Ports, leaving " +
										(pr - ar) + " unaccounted for!")
					}
				}.iterate()
		return rval
	}

	@Override
	void init() {
		if (testFlightConfigurationGraph == null) {
			logger.info("Connecting to '" + serverPath + "'.")

			testFlightConfigurationGraph = new OrientGraph(
					serverPath,
					EnvironmentConfiguration.odbUser,
					EnvironmentConfiguration.odbPassword)
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
		super.restart()
		shutdown()
		init()
	}

	Map<String, List<String>> checkForMysteriousEmptyValues() {
		init()

		Map<String, List<String>> invalidValues = new HashMap<>();

		def checkValuesIdMappings = [
				["Port", "PortDirection"],
				["Port", "PortTypes", "PortType", "Thermocouple"],
				["Port", "PortTypes", "PortType"],
				["Port", "PortTypes", "PortType", "PortType"],
				["Port", "PortTypes", "Thermocouple"],
				["Port", "GenericParameter", "BBNPortFunctionality"],
				["NetworkNode", "GenericParameter", "BBNDauMonetaryCost"],
				["Port", "GenericParameter", "Measurement", "SampleRate"],
				["Port", "GenericParameter", "Measurement", "DataLength"],
				["Port", "GenericParameter", "Measurement", "DataRate"],
				["Measurement", "GenericParameter", "SampleRate", "Min"],
				["Measurement", "GenericParameter", "SampleRate", "Max"],
				["Measurement", "GenericParameter", "DataLength", "Min"],
				["Measurement", "GenericParameter", "DataLength", "Max"],
				["Measurement", "GenericParameter", "DataRate", "Min"],
				["Measurement", "GenericParameter", "DataRate", "Max"],
				["DeviceModule", "ExcitationSource"]
		]

		for (def attributePath : checkValuesIdMappings) {

			def root = attributePath[0]
			def attribute = attributePath[attributePath.size() - 1]
			def intermediates = attributePath.subList(1, attributePath.size() - 1)


			def pipe = testFlightConfigurationGraph.V.has("@class", root).as("root")
			for (def intermediate : intermediates) {
				pipe = pipe.in("Containment").has("@class", intermediate)
			}
			def result = pipe.has(attribute, "").back("root").toList()
			if (result != null && result.size() > 0) {
				String label = String.join(":", attributePath)
				def resultList = new LinkedList()
				invalidValues.put(label, resultList)
				for (OrientVertex ov : result) {
					resultList.add(ov.getProperty("ID"))
				}
			}
			System.out.println("MEH")
		}

		def values = invalidValues
		return values
	}

}
