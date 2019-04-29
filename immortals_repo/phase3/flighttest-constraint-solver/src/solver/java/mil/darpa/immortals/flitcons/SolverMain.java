package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.mdl.FlighttestConstraintSolver;
import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import picocli.CommandLine;

import java.util.UUID;

public class SolverMain {

	@CommandLine.Option(names = "--validate", description = "Validates the OrientDB setup instead of adapting it")
	boolean validateOrientdb = false;

	@CommandLine.Option(names = {"-C", ValidationDataContainer.COLORLESS_FLAG}, description = "Results in the validation result only displaying invalid values instead of displaying all values as green or red depending on pass or fail")
	boolean colorlessMode = false;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	private boolean helpRequested = false;

	@CommandLine.Option(hidden = true, names = {"--simple-solver"})
	private boolean useSimpleSolver = false;

	public static void main(String[] args) {
		SolverMain m = new SolverMain();
		CommandLine.populateCommand(m, args);
		m.execute();
	}

	private void execute() {
		ChallengeProblemBridge cpb = new ChallengeProblemBridge();
		String evaluationInstanceIdentifier = UUID.randomUUID().toString();

		try {
			if (helpRequested) {
				CommandLine.usage(this, System.out);
				return;
			}


			if (!validateOrientdb) {
				FlighttestConstraintSolver fcs = new FlighttestConstraintSolver(useSimpleSolver);
				fcs.solve(!colorlessMode);
			} else {
				FlighttestConstraintSolver fcs = new FlighttestConstraintSolver(useSimpleSolver);
				boolean inputIsValid = fcs.validateInputConfiguration(!colorlessMode).isValid();
				boolean inventoryIsValid = fcs.validateDauInventory(!colorlessMode).isValid();

				String inputResult = inputIsValid ? "PASSED" : "FAILED";
				String inventoryResult = inventoryIsValid ? "PASSED" : "FAILED";

				boolean somethingFailed = inputResult.equals("FAILED") || inventoryResult.equals("FAILED");

				String result = "\n\tInput Configuration: " + inputResult + "\n\tDAU Inventory: " + inventoryResult;

				if (somethingFailed) {
					System.err.println("VALIDATION FAILED:" + result);
					System.exit(101);
				} else {
					System.out.println("Validation Succeeded:" + result);
				}
			}

		} catch (Exception e) {
			try {
				String msg = e.getMessage();

				if (msg == null) {
					System.err.println("EXCEPTION WITH NO MESSAGE!!");
					e.printStackTrace();
					cpb.postError(evaluationInstanceIdentifier, "UNDEFINED", null);

				} else {
					System.err.println(e.getMessage());
					e.printStackTrace();
					cpb.postError(evaluationInstanceIdentifier, e.getMessage(), null);
				}
			} catch (Exception e2) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.err.println();
				System.err.println();
				System.out.println();
				System.out.println();
				System.err.println(e2.getMessage());
				e2.printStackTrace();
			}
			System.exit(-1);
		}
	}
}
