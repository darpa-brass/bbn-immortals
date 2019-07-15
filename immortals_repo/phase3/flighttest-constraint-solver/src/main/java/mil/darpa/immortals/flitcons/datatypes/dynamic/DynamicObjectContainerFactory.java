package mil.darpa.immortals.flitcons.datatypes.dynamic;

import mil.darpa.immortals.flitcons.NestedPathException;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DynamicObjectContainerFactory {

	private static DynamicValue parseDynamicValueFromAttribute(HierarchicalData source, String attributeName) throws NestedPathException {
		Object value = source.getAttribute(attributeName);

		if (value instanceof Object[]) {
			try {
				return new DynamicValue(source.node, null, (Object[]) value, null);
			} catch (NestedPathException e) {
				e.addPathParent(attributeName + "[*]");
				throw e;
			}
		} else if (value instanceof String || value instanceof Number || value instanceof Boolean) {
			try {
				return new DynamicValue(source.node, null, null, value);
			} catch (NestedPathException e) {
				e.addPathParent(attributeName);
				throw e;
			}

		} else if (value instanceof List) {
			try {
				return new DynamicValue(source.node, null, ((List) value).toArray(), null);
			} catch (NestedPathException e) {
				e.addPathParent(attributeName);
				throw e;
			}
		} else {
			throw new NestedPathException(source.node.toString(), "Unsupported attribute type '" + value.getClass().toString() + "'!");
		}
	}


	private static DynamicObjectContainer produceDynamicObjectContainer(HierarchicalData source) throws NestedPathException {
		DynamicObjectContainer target = new DynamicObjectContainer(source.node, source.getDebugLabel());
		boolean containsData = false;

		for (String label : source.getAttributeNames()) {
			DynamicValue dynamicValue = parseDynamicValueFromAttribute(source, label);
			target.put(label, dynamicValue);
			containsData = true;
		}

		Iterator<String> childTypeIter = source.getChildrenClassIterator();
		while (childTypeIter.hasNext()) {

			boolean containsChildData = false;

			List<Object> values = new LinkedList<>();

			String childType = childTypeIter.next();

			Iterator<HierarchicalData> childIter = source.getChildrenDataIterator(childType);
			while (childIter.hasNext()) {
				HierarchicalData data = childIter.next();

				if (data.getAttribute("Min") != null && data.getAttribute("Max") != null) {
					Object min = data.getAttribute("Min");
					Object max = data.getAttribute("Max");
					if (min == null || max == null) {
						throw new NestedPathException(source.toString(), "A Min cannot be defined without a Max!");
					}
					if (data.getChildrenClassIterator().hasNext()) {
						throw new NestedPathException(source.toString(), "A Range object cannot have child attributes!");
					}
					target.put(childType, new DynamicValue(source.node, new Range(min, max), null, null));
					containsData = true;

				} else if (data.getAttribute("Equation") != null) {
					target.put(childType, new DynamicValue(source.node, null, null, new Equation((String) data.getAttribute("Equation"))));
					containsData = true;

				} else {
					Object value = produceDynamicObjectContainer(data);
					if (value != null) {
						values.add(value);
						containsData = true;
						containsChildData = true;
					}
				}
				if (containsChildData) {
					try {
						target.put(childType, new DynamicValue(source.node, null, values.toArray(), null));
					} catch (NestedPathException e) {
						e.addPathParent(childType);
						throw e;
					}
				}
			}
		}

		if (containsData) {
			return target;
		} else {
			return null;
		}
	}


	public static DynamicObjectContainer create(HierarchicalDataContainer inputData) throws NestedPathException {
		DynamicObjectContainer target = new DynamicObjectContainer(HierarchicalIdentifier.createBlankNode(), (String) null);
		Object[] daus = new Object[inputData.getDauRootNodes().size()];

		int idx = 0;

		for (HierarchicalData dau : inputData.getDauRootNodes()) {
			try {
				daus[idx++] = produceDynamicObjectContainer(dau);
			} catch (NestedPathException e) {
				e.addPathParent("daus");
				throw e;
			}
		}

		try {
			target.put("daus", new DynamicValue(null, null, daus, null));
		} catch (NestedPathException e) {
			e.addPathParent("daus");
			throw e;
		}

		return target;
	}
}
