package mil.darpa.immortals.schemaevolution;

public enum TerminalStatus {
	ReadyForAdaptation(false, false, true),
	AdaptationSuccessful(false, true, false),
	AdaptationNotRequired(true, true, true),
	PerturbationInputInvalid(true, true, true),
	AdaptationUnexpectedError(true, true, true),
	AdaptationUnsuccessful(true, true, true),
	AdaptationPartiallySuccessful(false, true, false),
	Halt(true, true, true);

	private final boolean terminal;

	private final boolean adaptationServerBlocked;
	private final boolean evaluationServerBlocked;

	TerminalStatus(boolean terminal, boolean adaptationServerBlocked, boolean evaluationServerBlocked) {
		this.terminal = terminal;
		this.adaptationServerBlocked = adaptationServerBlocked;
		this.evaluationServerBlocked = evaluationServerBlocked;
	}

	public boolean isTerminal() {
		return terminal;
	}

	public boolean isAdaptationServerBlocked() {
		return adaptationServerBlocked;
	}

	public boolean isEvaluationServerBlocked() {
		return evaluationServerBlocked;
	}
}
