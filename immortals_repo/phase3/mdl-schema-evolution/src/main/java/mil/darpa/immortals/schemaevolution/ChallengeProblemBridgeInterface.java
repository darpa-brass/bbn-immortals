package mil.darpa.immortals.schemaevolution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The class this interface represents is intended to provide simplified read/write access to OrientDB.
 * <p>
 * All configuration options will be read from environment variables, so make sure they are properly passed to the
 * Process instance running this process if it is not running directly within the root evaluation harness.
 */
public interface ChallengeProblemBridgeInterface {

	/**
	 * Initializes the environemnt
	 *
	 * @throws Exception
	 */
	void init() throws Exception;

	/**
	 * This method fetches the configuration from the evaluation OrientDB server, stores it in the results directory,
	 * and returns the configuration.
	 *
	 * @param evaluationInstanceIdentifier An identifier for the adaptation.
	 * @return A JSON String that can be converted to a suitable documented configuration format.
	 * @throws Exception
	 */
	@Nullable
	String getConfigurationJson(@Nonnull String evaluationInstanceIdentifier) throws Exception;

	/**
	 * Posts the results of an evaluation to the evaluation server and the results directory
	 *
	 * @param evaluationInstanceIdentifier A unique Identifier to act as a key for this evaluation execution
	 * @param finishStatus                 The ultimate result of the adaptation
	 * @param results                      A JSON String that can be converted into a suitable documented result format.
	 */
	void postResultsJson(@Nonnull String evaluationInstanceIdentifier, @Nonnull TerminalStatus finishStatus, @Nonnull String results) throws Exception;

	/**
	 * Posts The error Finish state to the evaluation server and the results directory.
	 * The caller should shut down after this is done or it will be shut down forcibly!
	 *
	 * @param evaluationInstanceIdentifier A unique Identifier to act as a key for this evaluation execution
	 * @param errorDescription             A brief description of the error
	 * @param errorData                    Additional information to help debug the error, if available
	 * @throws Exception
	 */
	void postError(@Nonnull String evaluationInstanceIdentifier, @Nonnull String errorDescription, @Nullable String errorData) throws Exception;

	/**
	 * Posts The invalid input error Finish state to the evaluation server and results directory.
	 * The caller should shut down after this is done or it will be shut down forcibly!
	 *
	 * @param evaluationInstanceIdentifier A unique Identifier to act as a key for this evaluation execution
	 * @param errorDescription             A brief description of the error
	 * @param errorData                    Additional information to help debug the error, if available
	 * @throws Exception
	 */
	void postInvalidInputError(@Nonnull String evaluationInstanceIdentifier, @Nonnull String errorDescription, @Nullable String errorData) throws Exception;

	/**
	 * Used to store large binary data in excess of a couple megabytes to file in the details directory.
	 *
	 * @param evaluationInstanceIdentifier A unique identifier to act as a key for this evaluation execution
	 * @param artifactIdentifier           The filename to store the file under
	 * @param binaryData                   The data to store
	 * @throws Exception
	 */
	void storeLargeBinaryData(@Nonnull String evaluationInstanceIdentifier, @Nonnull String artifactIdentifier, @Nonnull byte[] binaryData) throws Exception;

	/**
	 * Used to store large binary data in excess of a couple megabytes to a file in the debug directory.
	 *
	 * @param evaluationInstanceIdentifier A unique Identifier to act as a key for this evaluation execution
	 * @param artifactIdentifier           The filename to store the file under
	 * @param binaryData                   The data to store
	 * @throws Exception
	 */
//	void storeLargeBinaryDebugData(@Nonnull String evaluationInstanceIdentifier, @Nonnull String artifactIdentifier, @Nonnull byte[] binaryData) throws Exception;

	/**
	 * Waits until the state of the OrientDB Server is {@link TerminalStatus#ReadyForAdaptation} indicating data is
	 * ready for adaptation or or {@link TerminalStatus#Halt} indicating the system should shutdown or be ready to
	 * shutdown
	 *
	 * @return The OrientDB Status
	 * @throws Exception
	 */
//	TerminalStatus waitForReadyOrHalt() throws Exception;

	/**
	 * Posts the results of an evaluation to the evaluation server and the results directory.
	 *
	 * @param evaluationInstanceIdentifier A unique Identifier to act as a key for this evaluation execution
	 * @param finishStatus                 The ultimate result of the adaptation
	 * @param description                  A Readable description of the results
	 * @param jsonResults                  A JSON String that can be machined parsed to analyze the results
	 * @throws Exception
	 */
//	void postResultsJson(@Nonnull String evaluationInstanceIdentifier, @Nonnull TerminalStatus finishStatus, @Nonnull String description, @Nonnull String jsonResults) throws Exception;

	/**
	 * Gets the current state on the server including all related data and logs it to the results directory
	 *
	 * @param evaluationInstanceIdentifier The instance to log this data to
	 * @return The current data
	 * @throws Exception
	 */
//	@Nullable
//	BBNEvaluationData getCurrentEvaluationData(@Nonnull String evaluationInstanceIdentifier) throws Exception;
}
