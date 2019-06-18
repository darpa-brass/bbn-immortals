package mil.darpa.immortals.orientdbserver;

import javax.annotation.Nonnull;

public interface SolverExecutionInterface {
	public void execute(@Nonnull String evaluationIdentifier) throws Exception;
}
