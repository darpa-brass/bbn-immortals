package mil.darpa.immortals.flitcons;

import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display Help")
	private boolean helpRequested = false;

	@CommandLine.Option(names = "--no-commit", description = "Performs the adaptation but does not commit it to the OrientDB instance")
	private boolean noCommit = false;

	@CommandLine.Option(hidden = true, names = {"--simple-solver"})
	private boolean useSimpleSolver = false;

	@CommandLine.Option(hidden = true, names = {"--json-inventory-path"})
	private String jsonInventoryPath = null;

	@CommandLine.Option(hidden = true, names = {"--json-request-path"})
	private String jsonRequestPath = null;

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

	public boolean isUseSimpleSolver() {
		return useSimpleSolver;
	}

	public Path getJsonInventoryPath() {
		if (jsonInventoryPath == null) {
			return null;
		}
		Path p = Paths.get(jsonInventoryPath).toAbsolutePath();
		if (!Files.exists(p)) {
			throw new RuntimeException("The provided '--json-inventory-path' argument '" + jsonInventoryPath + "' does not resolve to a file relative to the execution directory!");
		}
		return p;
	}

	public Path getJsonRequestPath() {
		if (jsonRequestPath == null) {
			return null;
		}
		Path p = Paths.get(jsonRequestPath).toAbsolutePath();
		if (!Files.exists(p)) {
			throw new RuntimeException("The provided '--json-request-path' argument '" + jsonInventoryPath + "' does not resolve to a file relative to the execution directory!");
		}
		return p;

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
