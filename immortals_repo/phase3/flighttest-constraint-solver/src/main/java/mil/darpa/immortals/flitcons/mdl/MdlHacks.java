package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class MdlHacks {

	public static void fixHierarchicalData(@Nonnull Iterator<HierarchicalData> hierarchicalDataIterator) {
		while (hierarchicalDataIterator.hasNext()) {
			HierarchicalData data = hierarchicalDataIterator.next();
			if (data.getNodeType().equals("PortType") && data.getAttribute("Thermocouple") != null) {
				data.getAttributes().put("PortType", "Thermocouple");
			}
		}
	}
}
