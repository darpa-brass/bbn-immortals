package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.*;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.mdl.validation.PortMapping;
import mil.darpa.immortals.flitcons.mdl.validation.PortMappingValidator;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;

import java.util.Map;

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
			dataValidator.validateConfiguration(ValidationScenario.InputConfigurationUsage);
			dataValidator.validateConfiguration(ValidationScenario.InputConfigurationRequirements);
			dataValidator.validateConfiguration(ValidationScenario.DauInventory);

			portMappingValidator.validateInitialData();

			solver.loadData(dataSource);
			DynamicObjectContainer solution = solver.solve();

			if (solution == null) {
				throw new AdaptationnException(ResultEnum.AdaptationUnsuccessful, "Could not find a valid adaptation.");
			}

			SolutionInjector injector = new SolutionInjector(dataSource, solution);

			injector.injectSolution();

			dataSource.restart();
			portMappingValidator.validateResultData(dataSource.getPortMappingDetails());

			dataValidator.validateConfiguration(ValidationScenario.OutputConfigurationUsage);

		} catch (NestedPathException e) {
			throw AdaptationnException.internal(e);
		}
	}

	public void shutdown() {
		dataSource.shutdown();
	}
}
