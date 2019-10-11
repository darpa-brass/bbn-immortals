package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.NestedPathException;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Set;

public class MdlHacks {

	public static void fixHierarchicalData(@Nonnull Iterator<HierarchicalData> hierarchicalDataIterator) {
//		while (hierarchicalDataIterator.hasNext()) {
//			HierarchicalData data = hierarchicalDataIterator.next();
//			if (data.getNodeType().equals("PortType")) {
//				HierarchicalData parent = data.getParentData();
//				List<String> path = new LinkedList<>();
//				path.add("GenericParameter");
//				HierarchicalData genericParameter = parent.getChildNodeByPath(path);
//				if (((genericParameter != null && genericParameter.getAttribute("BBNPortFunctionality").equals("ThermocoupleConditioner")) ||
//						data.getAttribute("Thermocouple") != null) &&
//						(data.getAttribute("PortType") == null || data.getAttribute("PortType").equals(""))) {
//					System.err.println("PortType Found with no value! HotPatching!");
//					data.getAttributes().put("PortType", "Thermocouple");
//				}
//			}
//		}
	}

	public static void removeInvalidThermocoupleValuesFromSolution(@Nonnull HierarchicalDataContainer original,
	                                                               @Nonnull HierarchicalDataContainer solutionContainer,
	                                                               @Nonnull DynamicObjectContainer solution) {
		try {
			Set<DynamicObjectContainer> solutionDaus = solution.get("daus").parseDynamicObjectContainerArray();
			for (DynamicObjectContainer solutionDau : solutionDaus) {
				Set<DynamicObjectContainer> ports = solutionDau.get("Port").parseDynamicObjectContainerArray();
				for (DynamicObjectContainer port : ports) {
					if (port.get("Thermocouple") != null) {
						HierarchicalIdentifier portIdentifier = HierarchicalIdentifier.getByStringIdentifier(
								port.get("SupersededGloballyUniqueId").parseString());
						HierarchicalData originalNode = original.getNode(portIdentifier);

						HierarchicalData portType = originalNode.getChildNodeByPath("PortTypes", "PortType");
						Object thermocouple = null;
						if (portType != null) {
							thermocouple = portType.getAttribute("Thermocouple");
						}

						if (thermocouple == null) {
							port.remove("Thermocouple");
						}
					}

				}
			}


		} catch (NestedPathException e) {
			throw AdaptationnException.internal(e);
		}
	}

	public static void injectExcitationPortDefaults(@Nonnull HierarchicalDataContainer combinedDataContainer) {
		for (HierarchicalData dauNode : combinedDataContainer.getDauRootNodes()) {
			Iterator<HierarchicalData> portIterator = dauNode.getChildrenDataIterator("Port");
			while (portIterator.hasNext()) {
				HierarchicalData currentPort = portIterator.next();
				if (currentPort.getAttribute("Excitation") == null) {
					currentPort.getAttributes().put("Excitation", false);
				}
			}
		}
	}

	public static void injectPortTypeDefaults(@Nonnull HierarchicalDataContainer combinedDataContainer) {
		for (HierarchicalData dauNode : combinedDataContainer.getDauRootNodes()) {
			Iterator<HierarchicalData> portIterator = dauNode.getChildrenDataIterator("Port");
			while (portIterator.hasNext()) {
				HierarchicalData currentPort = portIterator.next();
				if (currentPort.getAttribute("PortType") == null) {
					currentPort.getAttributes().put("PortType", "_N0_V4LU3_");
				}
			}
		}
	}

	public static void cleanseDslOutput(@Nonnull DynamicObjectContainer dslOutput) {
//		try {
//
//			for (Object dauObject : dslOutput.get("daus").valueArray) {
//				DynamicObjectContainer dau = (DynamicObjectContainer) dauObject;
//				ArrayList<DynamicObjectContainer> connectedPorts = new ArrayList<>();
//				for (Object portObject : dau.get("Port").valueArray) {
//					DynamicObjectContainer port = (DynamicObjectContainer) portObject;
//					DynamicValue supersededPort = port.get("SupersededGloballyUniqueId");
//					if (supersededPort != null && supersededPort.singleValue != null && !(supersededPort.singleValue instanceof String && ((String) supersededPort.singleValue).trim().equals(""))) {
//						connectedPorts.add(port);
//
//
//						Map<String, DynamicValue> updatedValues = new HashMap<>();
//						for (Map.Entry<String, DynamicValue> attributeEntry : port.children.entrySet()) {
//							DynamicValue attributeValue = attributeEntry.getValue();
//							if (attributeValue.multiplicity == DynamicValueMultiplicity.SingleValue &&
//									attributeValue.singleValue instanceof String && ((String) attributeValue.singleValue).startsWith(":")) {
//								String singleValue = (String) ((String) attributeValue.singleValue).replace(":", "");
//								Range range = attributeValue.range;
//								Object[] valueArray = attributeValue.valueArray;
//								HierarchicalIdentifier dataSource = attributeValue.dataSource;
//								Range newRange = range == null ? null : duplicateObject(range);
//								Object[] newValueArray = null;
//
//								if (valueArray != null) {
//									newValueArray = new Object[valueArray.length];
//									for (int i = 0; i < valueArray.length; i++) {
//										newValueArray[i] = duplicateObject(valueArray[i]);
//									}
//								}
//								updatedValues.put(attributeEntry.getKey(),
//										new DynamicValue(dataSource, newRange, newValueArray, singleValue));
//							}
//						}
//						for (Map.Entry<String, DynamicValue> attributeEntry : updatedValues.entrySet()) {
//							port.put(attributeEntry.getKey(), attributeEntry.getValue());
//						}
//					}
//				}
//				dau.remove("Port");
//				dau.put("Port", DynamicValue.fromValueArray(dau.identifier, connectedPorts.toArray()));
//
//			}
//
//		} catch (NestedPathException e) {
//			throw AdaptationnException.internal(e);
//		}
	}

//	public static class MeasurementValueWrapper {
//
//		public void removeBadMeasurementValues(DynamicObjectContainer inputConfiguration,
//		                                              DynamicObjectContainer dauInventory) {
////			Map<String, Range> dataLengthRanges = new HashMap<>();
////			Map<String, Range> dataRateRanges = new HashMap<>();
////			Map<String, Range> sampleRateRanges = new HashMap<>();
////
////			Object[] daus = inputConfiguration.get("daus").valueArray;
////			for (Object o : daus) {
////				DynamicObjectContainer dau = (DynamicObjectContainer) o;
////				Object[] ports = dau.get("Port").valueArray;
////				for (Object o2 : ports) {
////					DynamicObjectContainer port = (DynamicObjectContainer) o2;
////					String portIdentifier = (String) port.get("GloballyUniqueId").singleValue;
////
////					DynamicValue measurementContainer = port.get("Measurement");
////
////					if (measurementContainer.singleValue != null) {
////						DynamicObjectContainer measurement = (DynamicObjectContainer) measurementContainer.getValue();
////
////
////					} else if (measurementContainer.valueArray != null && measurementContainer.valueArray.length == 1) {
////						DynamicObjectContainer measurement = (DynamicObjectContainer) measurementContainer.valueArray[0];
////						dataRateRanges.put(portIdentifier, measurement.get("DataRate").range);
////						sampleRateRanges.put(portIdentifier, measurement.get("SampleRate").range);
////						dataLengthRanges.put(portIdentifier, measurement.get("DataLength").range);
////
////					} else {
////						throw new RuntimeException("Bad Measurement value!");
////					}
////				}
////			}
//
//			Object[] daus = dauInventory.get("daus").valueArray;
//			for (Object o : daus) {
//				DynamicObjectContainer dau = (DynamicObjectContainer) o;
//				Object[] ports = dau.get("Port").valueArray;
//
//				List<Object> portsToRemove = new LinkedList<>();
//
//				for (Object o2 : ports) {
//					DynamicObjectContainer port = (DynamicObjectContainer) o2;
////					String portIdentifier = (String) port.get("GloballyUniqueId").singleValue;
//
//					DynamicValue measurementContainer = port.get("Measurement");
//
//					if (measurementContainer.valueArray != null) {
//						for (Object o3 : measurementContainer.valueArray) {
//							DynamicObjectContainer measurement = (DynamicObjectContainer) o3;
//							Long dataLength = null;
//							Long sampleRate = null;
//
//							// TODO: Consider multiple values per value, persent data rate, and missing others
//
//							if (measurement.containsKey("DataLength")) {
//								dataLength = (Long) measurement.get("DataLength").singleValue;
//							}
//
//							if (measurement.containsKey("SampleRate")) {
//								sampleRate = (Long) measurement.get("SampleRate").singleValue;
//							}
//
//						}
//
//					} else {
//						throw new RuntimeException("Bad Measurement value!");
//					}
//					port.remove(("Measurement"));
//					port.put("Measurement", DynamicValue.fromRange(
//							)
//
//					))
//
////					if (measurementContainer.singleValue != null) {
////						DynamicObjectContainer measurement = (DynamicObjectContainer) measurementContainer.getValue();
////
////
////					} else if (measurementContainer.valueArray != null && measurementContainer.valueArray.length == 1) {
////						DynamicObjectContainer measurement = (DynamicObjectContainer) measurementContainer.valueArray[0];
//////						dataRateRanges.put(portIdentifier, measurement.get("DataRate").range);
//////						sampleRateRanges.put(portIdentifier, measurement.get("SampleRate").range);
//////						dataLengthRanges.put(portIdentifier, measurement.get("DataLength").range);
////
////					} else {
////						throw new RuntimeException("Bad Measurement value!");
////					}
//				}
//			}
//
//
//		}
//	}
////	public static void inflateMeasurementValues(DynamicObjectContainer inputConfiguration,
////	                                            DynamicObjectContainer dauInventory) {
////
////	}
}
