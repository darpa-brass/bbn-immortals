package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;
import picocli.CommandLine;

public class SolverConfiguration {

	private static SolverConfiguration instance;

	public synchronized static SolverConfiguration getInstance() {
		if (instance == null) {
			instance = new SolverConfiguration();
		}
		return instance;
	}

	private SolverConfiguration() {

	}

	@CommandLine.Option(names = "--validate", description = "Validates the OrientDB setup instead of adapting it")
	public boolean validateOrientdb = false;

	@CommandLine.Option(names = {"-C", ValidationDataContainer.COLORLESS_FLAG}, description = "Results in the validation result only displaying invalid values instead of displaying all values as green or red depending on pass or fail")
	public boolean colorlessMode = false;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	public boolean helpRequested = false;

	@CommandLine.Option(names = "--no-commit", description = "Performs the adaptation but does not commit it to the OrientDB instance")
	public boolean noCommit = false;

	@CommandLine.Option(names = "--resource-dsl-path", description = "The directory where the Resource DSL is located")
	public String dslPath = null;

	@CommandLine.Option(hidden = true, names = {"--simple-solver"})
	public boolean useSimpleSolver = false;

	public String evaluationIdentifier;
}
