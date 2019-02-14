package mil.darpa.immortals.flitcons.datatypes;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class OrientGraphDataContainer extends HierarchicalDataContainer<OrientVertex> {

	public OrientGraphDataContainer(@Nonnull Collection<OrientVertex> dauNodes, @Nonnull Map<String, Set<String>> interestedDauPropertes) {
		super(dauNodes, interestedDauPropertes);
	}

	private OrientGraphDataContainer(@Nonnull Map<String, Set<String>> interestedProperties) {
		super(interestedProperties);
	}


	@Override
	protected HierarchicalData addOrGetDataInternal(OrientVertex v, @Nullable List<OrientVertex> path, boolean isRootObject) {
		if (existingDataIdentifierMap.containsKey(v.getId().toString())) {
			return existingDataIdentifierMap.get(v.getId().toString());
		} else {
			String rootParentIdentifier;
			if (isRootObject) {
				rootParentIdentifier = v.getIdentity().toString();

			} else {
				if (path == null) {
					throw new RuntimeException("An item that is not a root object such as a DAU or Measurement must have a parent path!");

				} else {
					List<String> parentSet = path.stream().filter(t ->
							existingDataIdentifierMap.containsKey(t.getIdentity().toString()) &&
									superParentChildElements.containsKey(existingDataIdentifierMap.get(t.getIdentity().toString())))
							.map(t -> t.getIdentity().toString())
							.collect(Collectors.toList());
//							.filter(Objects::nonNull).collect(Collectors.toList());
//					List<OrientVertex> parentSet = path.stream().filter(t -> interestedDauPropertes.containsKey(t.getProperty("@class"))).collect(Collectors.toList());

					if (parentSet.size() == 0) {
						throw new RuntimeException("The node '" + v.getId() + "' does not have a known parent node!");
					} else if (parentSet.size() > 1) {
						throw new RuntimeException("The node '" + v.getId() + "' has multiple known parent nodes!");
					}
					rootParentIdentifier = parentSet.get(0);
				}

			}

			String className = v.getProperty("@class");
			Set<String> desiredProps = interestedDauPropertes.get(className);
			HashMap<String, Object> collectedProps = new HashMap<>();

			if (desiredProps != null) {

				for (String value : v.getPropertyKeys()) {
					if (desiredProps.contains(value)) {
						collectedProps.put(value, v.getProperty(value));
					}
				}

			}

			HierarchicalData newData = new HierarchicalData(className,
					collectedProps,
					v.getIdentity().toString(),
					v,
					isRootObject);


			for (Edge e : v.getEdges(Direction.BOTH, "Reference")) {
				if (e.getVertex(Direction.IN).equals(v)) {
					OrientVertex outNode = (OrientVertex) e.getVertex(Direction.OUT);
					newData.getInboundReferencesSet().add(new HierarchicalNode(outNode.getIdentity().toString(), outNode.getProperty("@class")));
				}

				if (e.getVertex(Direction.OUT).equals(v)) {
					OrientVertex inNode = (OrientVertex) e.getVertex(Direction.IN);
					newData.getOutboundReferencesSet().add(new HierarchicalNode(inNode.getIdentity().toString(), inNode.getProperty("@class")));
				}
			}

			existingDataIdentifierMap.put(v.getId().toString(), newData);

			if (isRootObject) {
				superParentChildElements.put(newData, new HashSet<>());

			} else {
				for (OrientVertex pathVal : path) {
					if (!existingDataIdentifierMap.keySet().contains(pathVal.getId().toString())) {
						throw new RuntimeException("Should not be missing parent elements with the depth first traversial!");
					}
				}

				HierarchicalData directParentNode = existingDataIdentifierMap.get(path.get(path.size() - 1).getId().toString());
				newData.setParentNode(directParentNode);
			}

			superParentChildElements.get(existingDataIdentifierMap.get(rootParentIdentifier)).add(newData);

			return newData;
		}
	}

	@Override
	public OrientGraphDataContainer duplicate() {
		OrientGraphDataContainer target = new OrientGraphDataContainer(this.interestedDauPropertes);
		HierarchicalDataContainer.duplicate(this, target);
		return target;
	}
}
