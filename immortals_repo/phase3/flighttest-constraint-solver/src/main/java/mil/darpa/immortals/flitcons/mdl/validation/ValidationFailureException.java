package mil.darpa.immortals.flitcons.mdl.validation;

import javax.annotation.Nonnull;

public class ValidationFailureException extends Exception {
	public ValidationFailureException(@Nonnull String message) {
		super(message);
	}
}
