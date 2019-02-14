package mil.darpa.immortals.flitcons.datastores.xml;

import mil.darpa.immortals.flitcons.QueryHelper;
import mil.darpa.immortals.flitcons.datastores.DauInventoryValidator;
import mil.darpa.immortals.flitcons.datatypes.HierarchicalData;

import java.util.Map;
import java.util.Set;

public class OrientdbDauInventoryValidator extends DauInventoryValidator {

	public OrientdbDauInventoryValidator() {
		super();
	}

	@Override
	public Map<HierarchicalData, Set<HierarchicalData>> getTestInventoryDaus() {
		QueryHelper qh = new QueryHelper();
		return qh.getTestInventoryDauData();
	}

	@Override
	public String getSourceIdentifier() {
		return "OrientDB";
	}
}
