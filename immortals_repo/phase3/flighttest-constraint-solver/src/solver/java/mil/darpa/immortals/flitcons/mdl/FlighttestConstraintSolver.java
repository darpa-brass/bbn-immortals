package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.*;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueeException;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;

import java.util.Set;

import static mil.darpa.immortals.flitcons.Utils.CHILD_LABEL;
import static mil.darpa.immortals.flitcons.Utils.PARENT_LABEL;

public class FlighttestConstraintSolver {

	private final DataSourceInterface dataSource;
	private final DataCollector collector;
	private final MdlDataValidator validator;

	private final SolverInterface solver;

	public FlighttestConstraintSolver() {
		dataSource = new OrientVertexDataSource();
		collector = new DataCollector(dataSource);
		validator = new MdlDataValidator(null, null, dataSource);

		if (SolverConfiguration.getInstance().useSimpleSolver) {
			solver = new SimpleSolver();
		} else {
			solver = new DslSolver();
		}
	}

	public void solve() {
		boolean useColor = !SolverConfiguration.getInstance().colorlessMode;

		try {

			validator.validateConfiguration(ValidationScenario.InputConfigurationUsage, useColor);

			validator.validateConfiguration(ValidationScenario.InputConfigurationRequirements, useColor);
			validator.validateConfiguration(ValidationScenario.DauInventory, useColor);

			HierarchicalDataContainer inputContainer = collector.getInterconnectedTransformedFaultyConfiguration(false);
			DynamicObjectContainer input = Utils.createDslInterchangeFormat(inputContainer);

			HierarchicalDataContainer inventoryContainer = collector.getTransformedDauInventory(false);
			DynamicObjectContainer inventory = Utils.createDslInterchangeFormat(inventoryContainer);

			solver.loadData(input, inventory);
			DynamicObjectContainer solution = solver.solve();

			if (solution == null) {
				throw new AdaptationnException(ResultEnum.AdaptationUnsuccessful, "Could not find a valid adaptation.");
			}

			SolutionPreparer preparer = new SolutionPreparer(
					collector.getInterconnectedFaultyConfiguration(),
					collector.getInterconnectedTransformedFaultyConfiguration(false),
					collector.getRawDauInventoryContainer(),
					collector.getTransformedDauInventory(false));

			Set<SolutionPreparer.ParentAdaptationData> adaptation = preparer.prepare(solution, PARENT_LABEL, CHILD_LABEL);

			SolutionInjector injector = new SolutionInjector(dataSource, adaptation);
			injector.injectSolution();

			validator.validateConfiguration(ValidationScenario.OutputConfigurationUsage, useColor);

		} catch (DynamicValueeException e) {
			throw AdaptationnException.internal(e);
		}
	}

	public void shutdown() {
		dataSource.shutdown();
	}
}
