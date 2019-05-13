package mil.darpa.immortals.flitcons.reporting;

import javax.annotation.Nonnull;

public class AdaptationnException extends RuntimeException {

	public ResultEnum result;

	public AdaptationnException(@Nonnull ResultEnum result, @Nonnull String message) {
		super(message);
		this.result = result;
	}

	public AdaptationnException(@Nonnull ResultEnum result, @Nonnull Throwable t) {
		super(t);
		this.result = result;
	}

	public final void updateResultIfUnset(@Nonnull ResultEnum result) {
		if (result == ResultEnum.AdaptationInternalError) {
			this.result = result;
		}
	}

	public static AdaptationnException input(@Nonnull String message) {
		return new AdaptationnException(ResultEnum.PerturbationInputInvalid, message);
	}

	public static AdaptationnException input(@Nonnull Throwable t) {
		return new AdaptationnException(ResultEnum.PerturbationInputInvalid, t);
	}

	public static AdaptationnException internal(@Nonnull String message) {
		return new AdaptationnException(ResultEnum.AdaptationInternalError, message);
	}

	public static AdaptationnException internal(@Nonnull Throwable t) {
		return new AdaptationnException(ResultEnum.AdaptationInternalError, t);
	}
}
