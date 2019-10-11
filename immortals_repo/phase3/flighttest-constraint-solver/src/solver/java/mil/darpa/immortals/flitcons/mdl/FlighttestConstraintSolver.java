package mil.darpa.immortals.flitcons.mdl;

import com.google.gson.JsonObject;
import mil.darpa.immortals.flitcons.*;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.mdl.validation.PortMappingValidator;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;

public class FlighttestConstraintSolver {

	private final AbstractDataTarget dataSource;
	private final MdlDataValidator dataValidator;
	private final PortMappingValidator portMappingValidator;

	private final SolverInterface solver;

	public FlighttestConstraintSolver() {
		dataSource = new OrientVertexDataSource();
		dataValidator = new MdlDataValidator(null, null, dataSource);
		portMappingValidator = new PortMappingValidator(dataSource.getPortMappingDetails());

		if (SolverConfiguration.getInstance().isUseSimpleSolver()) {
			solver = new SimpleSolver();
		} else {
			solver = new DslSolver();
		}
	}

	public void solve() {
		try {
			dataValidator.validateConfiguration(ValidationScenario.InputConfigurationUsage, true, false);
			dataValidator.validateConfiguration(ValidationScenario.InputConfigurationRequirements, true, false);
			dataValidator.validateConfiguration(ValidationScenario.DauInventory, false, true);

			portMappingValidator.validateInitialData();

			solver.loadData(dataSource);
			DynamicObjectContainer solution = solver.solve();
			JsonObject metrics = solver.getMetrics();


			if (solution == null) {
				throw new AdaptationnException(ResultEnum.AdaptationUnsuccessful, "Could not find a valid adaptation.");
			}

			SolutionInjector injector = new SolutionInjector(dataSource, solution);

			injector.injectSolution();

			dataSource.restart();
			portMappingValidator.validateResultData(dataSource.getPortMappingDetails());

			dataValidator.validateConfiguration(ValidationScenario.OutputConfigurationUsage, true, false);

		} catch (NestedPathException e) {
			throw AdaptationnException.internal(e);
		}
	}

	public JsonObject getMetrics() {
		return solver.getMetrics();
	}

	public void shutdown() {
		dataSource.shutdown();
	}
}
