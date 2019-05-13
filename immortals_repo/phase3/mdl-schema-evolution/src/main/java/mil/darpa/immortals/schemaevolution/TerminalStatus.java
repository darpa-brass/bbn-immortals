package mil.darpa.immortals.schemaevolution;

public enum TerminalStatus {
	ReadyForAdaptation(false),
	AdaptationSuccessful(false),
	AdaptationNotRequired(true),
	PerturbationInputInvalid(true),
	AdaptationUnexpectedError(true),
	AdaptationUnsuccessful(true),
	AdaptationPartiallySuccessful(false),
	Halt(true);

	private final boolean terminal;

	TerminalStatus(boolean terminal) {
		this.terminal = terminal;
	}

	public boolean isTerminal() {
		return terminal;
	}
}
