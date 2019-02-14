package mil.darpa.immortals.flitcons.datatypes;

import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import mil.darpa.immortals.flitcons.Configuration;

import javax.annotation.Nonnull;
import java.util.*;

public class ScenarioData implements DuplicateInterface<ScenarioData> {

	private static final Configuration REPLACEMENT_CONFIG = Configuration.instance;

	private final OrientGraphDataContainer dauNodes;
	private final OrientGraphDataContainer externalNodes;

	private final Map<HierarchicalNode, Set<HierarchicalNode>> indirectRelations;

	public OrientGraphDataContainer getExternalNodeData() {
		return externalNodes;
	}

	public Map<HierarchicalNode, Set<HierarchicalNode>> getIndirectRelations() {
		return indirectRelations;
	}

	public OrientGraphDataContainer getDauNodeData() {
		return dauNodes;
	}

	public ScenarioData(@Nonnull Collection<OrientVertex> designatedDauVertices, Collection<OrientVertex> externalNodes) {
		this.indirectRelations = new HashMap<>();
		this.dauNodes = new OrientGraphDataContainer(designatedDauVertices, REPLACEMENT_CONFIG.adaptation.collectedDauProperties);
		this.externalNodes = new OrientGraphDataContainer(externalNodes, REPLACEMENT_CONFIG.adaptation.indirectCollectedProperties);

	}

	private ScenarioData(@Nonnull OrientGraphDataContainer dauNodes, @Nonnull OrientGraphDataContainer externalNodes, @Nonnull Map<HierarchicalNode, Set<HierarchicalNode>> indirectRelations) {
		this.dauNodes = dauNodes;
		this.externalNodes = externalNodes;
		this.indirectRelations = indirectRelations;
	}

	private static <T, V extends DuplicateInterface<V>> Map<T, Set<V>> duplicateSetMap(Map<T, Set<V>> source) {
		Map<T, Set<V>> rval = new HashMap<>();

		for (Map.Entry<T, Set<V>> dauNodeDataEntry : source.entrySet()) {
			Set<V> duplicateHierarchicalData = new HashSet<>();
			rval.put(dauNodeDataEntry.getKey(), duplicateHierarchicalData);

			for (V vd : dauNodeDataEntry.getValue()) {
				duplicateHierarchicalData.add(vd.duplicate());
			}
		}
		return rval;
	}

	public ScenarioData duplicate() {
		OrientGraphDataContainer dauNodesClone = this.dauNodes.duplicate();
		OrientGraphDataContainer externalNodesClone = this.externalNodes.duplicate();

		Map<HierarchicalNode, Set<HierarchicalNode>> indirectRelationsCLone = duplicateSetMap(indirectRelations);

		return new ScenarioData(
				dauNodesClone,
				externalNodesClone,
				indirectRelationsCLone
		);
	}
}
