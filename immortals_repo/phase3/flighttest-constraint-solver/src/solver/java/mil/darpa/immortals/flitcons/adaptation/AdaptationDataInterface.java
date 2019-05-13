package mil.darpa.immortals.flitcons.adaptation;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;

import java.util.TreeMap;

public interface AdaptationDataInterface {
	HierarchicalIdentifier getIdentifier();

	TreeMap<String, Object> getValues();

	HierarchicalData getRawData();

	HierarchicalData getTransformedData();

	HierarchicalData getSupersededData();
}
