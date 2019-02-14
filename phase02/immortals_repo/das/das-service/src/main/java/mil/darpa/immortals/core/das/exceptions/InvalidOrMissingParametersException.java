package mil.darpa.immortals.core.das.exceptions;

public class InvalidOrMissingParametersException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidOrMissingParametersException() {
		super(INVALID_OR_MISSING_PARAMETERS);
	}
	
	public InvalidOrMissingParametersException(String message) {
		super(message);
	}
	
	@Override
	public String getMessage() {
		return INVALID_OR_MISSING_PARAMETERS;
	}
	
	private static final String INVALID_OR_MISSING_PARAMETERS = "Invalid or missing parameters.";

}
