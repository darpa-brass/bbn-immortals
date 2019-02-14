package mil.darpa.immortals.flitcons;


import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import com.tinkerpop.pipes.branch.LoopPipe;
import mil.darpa.immortals.flitcons.datatypes.ScenarioData;
import mil.darpa.immortals.flitcons.datatypes.HierarchicalData;

import javax.annotation.Nonnull;
import java.util.*;

public class ScenarioDataCollector {

	// TODO: Add input validation

	private static final Configuration REPLACEMENT_CONFIG = Configuration.instance;



	public final ScenarioData data;


	public ScenarioDataCollector(Collection<OrientVertex> designatedDauVertices, Collection<OrientVertex> measurementVertices) {
		for (OrientVertex ov : designatedDauVertices) {
			String nodeType = (String) ov.getProperty("@class");

			if (REPLACEMENT_CONFIG.haltOnUnsupportedDauType && !REPLACEMENT_CONFIG.supported.dauTypes.contains(nodeType)) {
				throw new RuntimeException("Unexpected Dau Type '" + nodeType + "'!");
			}
		}

		data = new ScenarioData(designatedDauVertices, measurementVertices);

	}

	public void analyzeDauNode(@Nonnull LoopPipe.LoopBundle<OrientVertex> bundle) {
		HierarchicalData node = data.getDauNodeData().addOrGetData(bundle.getObject(), bundle.getPath());

	}

	public void analyzeExternalNode(@Nonnull LoopPipe.LoopBundle<OrientVertex> bundle) {
		HierarchicalData node = data.getExternalNodeData().addOrGetData(bundle.getObject(), bundle.getPath());
	}
}
