package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.adaptation.ChildAdaptationData;
import mil.darpa.immortals.flitcons.adaptation.ParentAdaptationData;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValue;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;

import javax.annotation.Nonnull;
import java.util.*;

import static mil.darpa.immortals.flitcons.Utils.*;

public class SolutionInjector {

	private final Set<ParentAdaptationData> adaptationData;
	private final Map<String, HierarchicalData> identifierDataMap = new HashMap<>();
	private final HierarchicalDataContainer target;

	public SolutionInjector(@Nonnull DynamicObjectContainer solution, @Nonnull String parentLabel, @Nonnull String childLabel, @Nonnull HierarchicalDataContainer target) {
		this.adaptationData = new HashSet<>();
		this.target = target;

		DynamicValue dausDynamicValue = solution.get(parentLabel);

		Set<DynamicObjectContainer> daus = dausDynamicValue.parseDynamicObjectContainerArray();

		for (DynamicObjectContainer dauContainer : daus) {
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
				ChildAdaptationData port = new ChildAdaptationData(dauIdentifier, portIdentifier, supersededPortIdentifier);

				for (String key : portContainer.keySet()) {
					if (!key.equals(GLOBALLY_UNIQUE_ID) & !key.equals(SUPERSEDED_GLOBALLY_UNIQUE_ID)) {
						port.values.put(key, portContainer.get(key).getValue());
					}
				}
				dau.ports.add(port);
			}
		}
		for (HierarchicalIdentifier identifier : target.getNodeIdentifiers()) {
			identifierDataMap.put(identifier.getIdentifier(), target.getNode(identifier));
		}
	}

	public void injectSolution() {
		for (ParentAdaptationData parent : adaptationData) {
			HierarchicalData target = identifierDataMap.get(parent.globallyUniqueId);
			System.out.println("MEH");
		}
	}



	public void updateAttribute(@Nonnull HierarchicalIdentifier target, @Nonnull String key, @Nonnull Object value) {

	}

}
