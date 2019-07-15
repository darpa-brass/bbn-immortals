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

	@CommandLine.Option(names = "--validate", description = "Validates the OrientDB setup instead of adapting it")
	private boolean validateOrientdb = false;

	@CommandLine.Option(names = {"-C", ValidationDataContainer.COLORLESS_FLAG}, description = "Results in the validation result only displaying invalid values instead of displaying all values as green or red depending on pass or fail")
	private boolean colorlessMode = false;

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	private boolean helpRequested = false;

	@CommandLine.Option(names = "--no-commit", description = "Performs the adaptation but does not commit it to the OrientDB instance")
	private boolean noCommit = false;

	@CommandLine.Option(names = "--resource-dsl-path", description = "The directory where the Resource DSL is located")
	private String dslPath = null;

	@CommandLine.Option(hidden = true, names = {"--simple-solver"})
	private boolean useSimpleSolver = false;

	@CommandLine.Option(names = "--stop-on-finish", description = "Indicates it should always stop when it has completed a single adaptation.")
	private boolean stopOnFinish;

	private String evaluationIdentifier;

	private SolverConfiguration() {
	}

	public boolean isValidateOrientdb() {
		return validateOrientdb;
	}

	public SolverConfiguration setValidateOrientdb(boolean validateOrientdb) {
		this.validateOrientdb = validateOrientdb;
		return this;
	}

	public boolean isColorlessMode() {
		return colorlessMode;
	}

	public SolverConfiguration setColorlessMode(boolean colorlessMode) {
		this.colorlessMode = colorlessMode;
		return this;
	}

	public boolean isHelpRequested() {
		return helpRequested;
	}

	public SolverConfiguration setHelpRequested(boolean helpRequested) {
		this.helpRequested = helpRequested;
		return this;
	}

	public boolean isNoCommit() {
		return noCommit;
	}

	public SolverConfiguration setNoCommit(boolean noCommit) {
		this.noCommit = noCommit;
		return this;
	}

	public String getDslPath() {
		return dslPath;
	}

	public SolverConfiguration setDslPath(String dslPath) {
		this.dslPath = dslPath;
		return this;
	}

	public boolean isUseSimpleSolver() {
		return useSimpleSolver;
	}

	public SolverConfiguration setUseSimpleSolver(boolean useSimpleSolver) {
		this.useSimpleSolver = useSimpleSolver;
		return this;
	}

	public boolean isStopOnFinish() {
		return stopOnFinish;
	}

	public SolverConfiguration setStopOnFinish(boolean stopOnFinish) {
		this.stopOnFinish = stopOnFinish;
		return this;
	}

	public String getEvaluationIdentifier() {
		return evaluationIdentifier;
	}

	public SolverConfiguration setEvaluationIdentifier(String evaluationIdentifier) {
		this.evaluationIdentifier = evaluationIdentifier;
		return this;
	}
}
