package mil.darpa.immortals.flitcons.datastores.xml;

import mil.darpa.immortals.flitcons.datastores.DauInventoryValidator;
import mil.darpa.immortals.flitcons.datatypes.HierarchicalData;

import java.util.Map;
import java.util.Set;

public class XmlDauInventoryValidator extends DauInventoryValidator {
	@Override
	public Map<HierarchicalData, Set<HierarchicalData>> getTestInventoryDaus() {
		return null;
	}

	@Override
	public String getSourceIdentifier() {
		return null;
	}
}
