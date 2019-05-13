package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.*;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueeException;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import mil.darpa.immortals.flitcons.validation.DataValidator;
import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import static mil.darpa.immortals.flitcons.Utils.*;

public class FlighttestConstraintSolver {

	private static File solverInputFile = new File("solver-input-configuration.json");
	private static File solverDauInventoryFile = new File("solver-dau-inventory.json");
	private static File solverOutputFile = new File("solver-output.configuration.json");

	private final DataSourceInterface dataSource;
	private final DataCollector collector;
	private final MdlDataValidator validator;

	private final SolverInterface solver;

	public FlighttestConstraintSolver() {
		Configuration config = Configuration.getInstance();

		dataSource = new OrientVertexDataSource();
		collector = new DataCollector(dataSource, config.transformation);
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
			validator.validateConfiguration(ValidationScenario.InputConfiguration, useColor);
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

			FileUtils.writeStringToFile(solverOutputFile, Utils.difGson.toJson(solution), Charset.defaultCharset());

			SolutionPreparer preparer = new SolutionPreparer(
					collector.getInterconnectedFaultyConfiguration(),
					collector.getInterconnectedTransformedFaultyConfiguration(false),
					collector.getRawDauInventoryContainer(),
					collector.getTransformedDauInventory(false));

			Set<SolutionPreparer.ParentAdaptationData> adaptation = preparer.prepare(solution, PARENT_LABEL, CHILD_LABEL);

			SolutionInjector injector = new SolutionInjector(dataSource, adaptation);
			injector.injectSolution();
		} catch (DynamicValueeException | IOException e) {
			throw AdaptationnException.internal(e);
		}
	}

	public void shutdown() {
		dataSource.shutdown();
	}
}
