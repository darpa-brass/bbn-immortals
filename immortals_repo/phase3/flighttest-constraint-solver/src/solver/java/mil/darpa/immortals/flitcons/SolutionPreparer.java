package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.adaptation.AdaptationDataInterface;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValue;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueMultiplicity;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.mdl.MdlHacks;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static mil.darpa.immortals.flitcons.Utils.*;

public class SolutionPreparer {

	private HierarchicalDataContainer rawInterconnectedTarget;
	private HierarchicalDataContainer transformedTarget;
	private HierarchicalDataContainer rawInventory;
	private HierarchicalDataContainer transformedInventory;

	public SolutionPreparer(@Nonnull HierarchicalDataContainer rawInterconnectedTarget,
	                        @Nonnull HierarchicalDataContainer transformedTarget,
	                        @Nonnull HierarchicalDataContainer rawInventory,
	                        @Nonnull HierarchicalDataContainer transformedInventory) {
		this.rawInterconnectedTarget = rawInterconnectedTarget;
		this.transformedTarget = transformedTarget;
		this.rawInventory = rawInventory;
		this.transformedInventory = transformedInventory;
	}

	public Set<ParentAdaptationData> prepare(@Nonnull DynamicObjectContainer solution, @Nonnull String parentLabel, @Nonnull String childLabel) {
		try {
			MdlHacks.removeInvalidThermocoupleValuesFromSolution(rawInterconnectedTarget, transformedInventory, solution);

			Set<ParentAdaptationData> adaptationData = new HashSet<>();

			DynamicValue dausDynamicValue = solution.get(parentLabel);

			Set<DynamicObjectContainer> daus = dausDynamicValue.parseDynamicObjectContainerArray();

			for (DynamicObjectContainer dauContainer : daus) {
				try {
					String dauIdentifier = dauContainer.get(GLOBALLY_UNIQUE_ID).parseString();
					Set<String> supersededDauIdentifiers = dauContainer.get(SUPERSEDED_GLOBALLY_UNIQUE_IDS).parseStringArray();
					ParentAdaptationData dau = new ParentAdaptationData(dauIdentifier, supersededDauIdentifiers);
					adaptationData.add(dau);

					for (String key : dauContainer.keySet()) {
						if (!key.equals(GLOBALLY_UNIQUE_ID) && !key.equals(SUPERSEDED_GLOBALLY_UNIQUE_IDS) && !key.equals(childLabel)) {
							dau.values.put(key, dauContainer.get(key));
						}
					}

					Set<DynamicObjectContainer> portContainers = dauContainer.get(childLabel).parseDynamicObjectContainerArray();

					for (DynamicObjectContainer portContainer : portContainers) {
						String portIdentifier = portContainer.get(GLOBALLY_UNIQUE_ID).parseString();
						String supersededPortIdentifier = portContainer.get(SUPERSEDED_GLOBALLY_UNIQUE_ID).parseString();
						ChildAdaptationData port = new ChildAdaptationData(portIdentifier, supersededPortIdentifier);

						try {
							for (String key : portContainer.keySet()) {
								if (!key.equals(GLOBALLY_UNIQUE_ID) & !key.equals(SUPERSEDED_GLOBALLY_UNIQUE_ID)) {
									DynamicValue portContainerValue = portContainer.get(key);
									if (portContainerValue.getValue() instanceof DynamicObjectContainer) {
										Map<String, Object> childContainerValues = new HashMap<>();
										DynamicObjectContainer childContainer = (DynamicObjectContainer) portContainerValue.getValue();
										try {
											for (Map.Entry<String, DynamicValue> entry : childContainer.children.entrySet()) {
												try {
													String entryName = entry.getKey();
													if (!entryName.equals(GLOBALLY_UNIQUE_ID) & !entryName.equals(SUPERSEDED_GLOBALLY_UNIQUE_ID)) {
														DynamicValue entryValue = entry.getValue();
														if (entryValue.multiplicity == DynamicValueMultiplicity.SingleValue) {
															Object value = entryValue.singleValue;
															if (!isSupportedPrimitive(value)) {
																throw new NestedPathException("", "Non-Primitive values not yet supported for nested solution values!");
															}
															childContainerValues.put(entryName, value);
														} else {
															throw new NestedPathException("", "Only Single values currently supported for nested solution values!");
														}

													}
												} catch (NestedPathException e) {
													e.addPathParent(entry.getKey());
													throw e;
												}
											}
										} catch (NestedPathException e) {
											e.addPathParent(childContainer.identifier.toString());
											throw e;
										}
										port.values.put(key, childContainerValues);

									} else {
										port.values.put(key, portContainer.get(key).getValue());
									}
								}
							}
							dau.ports.add(port);
						} catch (NestedPathException e) {
							e.addPathParent(portContainer.identifier.toString());
							throw e;
						}
					}
				} catch (NestedPathException e) {
					e.addPathParent(dauContainer.identifier.toString());
					throw e;
				}
			}

			return adaptationData;
		} catch (NestedPathException e) {
			throw AdaptationnException.internal(e);
		}
	}

	private HierarchicalData getRawNode(@Nonnull HierarchicalIdentifier identifier) {
		if (rawInventory.containsNode(identifier)) {
			return rawInventory.getNode(identifier);
		} else {
			return rawInterconnectedTarget.getNode(identifier);
		}
	}

	private HierarchicalData getTransformedNode(@Nonnull HierarchicalIdentifier identifier) {
		if (transformedInventory.containsNode(identifier)) {
			return transformedInventory.getNode(identifier);
		} else {
			return transformedTarget.getNode(identifier);
		}
	}

	public class ChildAdaptationData implements AdaptationDataInterface {
		public final HierarchicalIdentifier identifier;
		public final HierarchicalData rawData;
		public final HierarchicalData transformedData;

		public final HierarchicalIdentifier supersededIdentifier;
		public final HierarchicalData supersededRawData;
		public final HierarchicalData supersededTransformedData;

		public final TreeMap<String, Object> values;

		public ChildAdaptationData(@Nonnull String globallyUniqueId, @Nonnull String supersededPortUniqueId) {
			this.identifier = HierarchicalIdentifier.produceTraceableNode(globallyUniqueId, null);
			if (identifier.getSourceIdentifier() == null) {
				throw AdaptationnException.internal("No source identifier for adaptation found for '" + this.identifier.toString() + "'!");
			}

			this.supersededIdentifier = HierarchicalIdentifier.produceTraceableNode(supersededPortUniqueId, null);
			if (supersededIdentifier.getSourceIdentifier() == null) {
				throw AdaptationnException.internal("No source identifier for adaptation found for '" + this.supersededIdentifier.toString() + "'!");
			}

			rawData = getRawNode(identifier);
			transformedData = getTransformedNode(identifier);

			supersededRawData = getRawNode(supersededIdentifier);
			supersededTransformedData = getTransformedNode(supersededIdentifier);

			values = new TreeMap<>();
		}

		public HierarchicalData getSupersededData() {
			return supersededRawData;
		}

		@Override
		public HierarchicalIdentifier getIdentifier() {
			return identifier;
		}

		@Override
		public TreeMap<String, Object> getValues() {
			return values;
		}

		@Override
		public HierarchicalData getRawData() {
			return rawData;
		}

		@Override
		public HierarchicalData getTransformedData() {
			return transformedData;
		}
	}

	public class ParentAdaptationData implements AdaptationDataInterface {
		public final HierarchicalIdentifier identifier;
		public final HierarchicalData rawData;
		public final HierarchicalData transformedData;
		public final Set<HierarchicalIdentifier> supersededDauIdentifiers = new HashSet<>();
		public final Set<HierarchicalData> supersededDauRawData;
		public final TreeMap<String, Object> values;
		public final Set<ChildAdaptationData> ports;

		public ParentAdaptationData(@Nonnull String globallyUniqueId, @Nonnull Set<String> supersededDausUniqueIds) {
			this.identifier = HierarchicalIdentifier.produceTraceableNode(globallyUniqueId, null);
			if (identifier.getSourceIdentifier() == null) {
				throw AdaptationnException.internal("No source identifier for adaptation found for '" + this.identifier.toString() + "'!");
			}

			for (String supersededId : supersededDausUniqueIds) {
				HierarchicalIdentifier supersededIdentifier = HierarchicalIdentifier.produceTraceableNode(supersededId, null);
				if (supersededIdentifier.getSourceIdentifier() == null) {
					throw AdaptationnException.internal("No source identifier for adaptation found for '" + supersededIdentifier.toString() + "'!");
				}
				supersededDauIdentifiers.add(supersededIdentifier);
			}

			this.values = new TreeMap<>();
			this.ports = new HashSet<>();

			this.rawData = getRawNode(identifier);
			this.supersededDauRawData = supersededDausUniqueIds.stream().map(
					x -> HierarchicalIdentifier.produceTraceableNode(x, null)).map(SolutionPreparer.this::getRawNode).collect(Collectors.toSet());
			this.transformedData = getTransformedNode(identifier);
		}

		@Override
		public HierarchicalIdentifier getIdentifier() {
			return identifier;
		}

		@Override
		public TreeMap<String, Object> getValues() {
			return values;
		}

		@Override
		public HierarchicalData getRawData() {
			return rawData;
		}

		@Override
		public HierarchicalData getTransformedData() {
			return transformedData;
		}

		/**
		 * This seems redundant with how it is used. But it is intended to protect against blindly
		 * assuming all nodes have the same parent.
		 *
		 * @return the data this value is replacing
		 */
		@Override
		public HierarchicalData getSupersededData() {
			// This seems pointless. But due to how this could be used it would be a problem if the
			// other superseded nodes had different parents.
			HierarchicalData parent = null;
			for (HierarchicalData supersededData : supersededDauRawData) {
				if (parent == null) {
					parent = supersededData.getParentData();
				} else {
					if (parent != supersededData.getParentData()) {
						throw AdaptationnException.internal("Parent data is not equal!");
					}
				}
			}
			return supersededDauRawData.iterator().next();
		}
	}
}
