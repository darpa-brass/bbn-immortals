package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.*;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueException;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class FlighttestConstraintSolver {

	public static File solverInputFile = new File("solver-input-configuration.json");
	public static File solverDauInventoryFile = new File("solver-dau-inventory.json");
	public static File solverOutputFIle = new File("solver-output.configuration.json");

	private HierarchicalDataContainer adaptationTarget;

	private final AbstractDataCollector collector;

	private final SolverInterface solver;

	public FlighttestConstraintSolver(boolean useSimpleSolver) {
		Configuration config = Configuration.getInstance();

		collector = new OrientVertexCollector(
				config.dataCollectionInstructions,
				config.transformation);

		if (useSimpleSolver) {
			solver = new SimpleSolver();
		} else {
			solver = new DslSolver();
		}
	}

	private ValidationDataContainer validateScenario(@Nonnull ValidationScenario scenario, @Nonnull DynamicObjectContainer dynamicData, boolean useColor) throws DynamicValueException {
		MdlDataValidator dv = new MdlDataValidator(null, null);
		dv.init();
		ValidationDataContainer results = dv.validate(scenario, dynamicData);
		results.printResults(scenario.title, useColor);
		return results;
	}

	private DynamicObjectContainer getInputScenario() throws DynamicValueException {
		adaptationTarget = collector.getInputConfiguration();
		return Utils.createDslInterchangeFormat(adaptationTarget);
	}

	private DynamicObjectContainer getDauInventory() throws DynamicValueException {
		HierarchicalDataContainer dauInventory = collector.getDauInventory();
		return Utils.createDslInterchangeFormat(dauInventory);
	}

	public ValidationDataContainer validateDauInventory(boolean useColor) throws DynamicValueException {
		DynamicObjectContainer dauInventory = getDauInventory();
		return validateScenario(ValidationScenario.DauInventory, dauInventory, useColor);
	}

	public ValidationDataContainer validateInputConfiguration(boolean useColor) throws DynamicValueException {
		DynamicObjectContainer inputConfiguration = getInputScenario();
		return validateScenario(ValidationScenario.InputConfiguration, inputConfiguration, useColor);
	}

	public void solve(boolean useColor) throws DynamicValueException, IOException {
		DynamicObjectContainer input = getInputScenario();
		FileUtils.writeStringToFile(solverInputFile, Utils.difGson.toJson(input), Charset.defaultCharset());
		validateScenario(ValidationScenario.InputConfiguration, input, useColor);

		DynamicObjectContainer inventory = getDauInventory();
		FileUtils.writeStringToFile(solverDauInventoryFile, Utils.difGson.toJson(inventory), Charset.defaultCharset());
		validateScenario(ValidationScenario.DauInventory, inventory, useColor);

		solver.loadData(input, inventory);
		DynamicObjectContainer solution = solver.solve();

		FileUtils.writeStringToFile(solverOutputFIle, Utils.difGson.toJson(solution), Charset.defaultCharset());

		System.out.println("MEH");
	}
}
