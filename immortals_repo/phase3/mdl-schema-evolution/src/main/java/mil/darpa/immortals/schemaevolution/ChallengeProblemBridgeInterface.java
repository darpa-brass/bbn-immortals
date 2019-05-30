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
	 * This method fetches the configuration from the evaluation OrientDB server, stores it in the persistence server,
	 * and returns the configuration.
	 *
	 * @param evaluationInstanceIdentifier An identifier for the adaptation.
	 * @return A JSON String that can be converted to a suitable documented configuration format.
	 * @throws Exception
	 */
	String getConfigurationJson(@Nonnull String evaluationInstanceIdentifier) throws Exception;

	/**
	 * Posts the results of an evaluation to the evaluation and persistence server.
	 *
	 * @param evaluationInstanceIdentifier A unique Identifier to act as a key for this evaluation execution
	 * @param finishStatus                 The ultimate result of the adaptation
	 * @param results                      A JSON String that can be converted into a suitable documented result format.
	 */
	void postResultsJson(@Nonnull String evaluationInstanceIdentifier, @Nonnull TerminalStatus finishStatus, @Nonnull String results) throws Exception;

	/**
	 * Posts The error Finish state to the persistent and/or evaluation server. The caller should shut down after this is done!
	 *
	 * @param evaluationInstanceIdentifier A unique Identifier to act as a key for this evaluation execution
	 * @param errorDescription             A brief description of the error
	 * @param errorData                    Additional information to help debug the error, if available
	 * @throws Exception
	 */
	void postError(@Nonnull String evaluationInstanceIdentifier, @Nonnull String errorDescription, @Nullable String errorData) throws Exception;

	/**
	 * Posts The invalid input error Finish state to the persistent and/or evaluation server. The caller should shut down after this is done!
	 *
	 * @param evaluationInstanceIdentifier A unique Identifier to act as a key for this evaluation execution
	 * @param errorDescription             A brief description of the error
	 * @param errorData                    Additional information to help debug the error, if available
	 * @throws Exception
	 */
	void postInvalidInputError(@Nonnull String evaluationInstanceIdentifier, @Nonnull String errorDescription, @Nullable String errorData) throws Exception;

	/**
	 * Used to store large binary data in excess of a couple megabytes.
	 *
	 * @param evaluationInstanceIdentifier A unique Identifier to act as a key for this evaluation execution
	 * @param artifactIdentifier           A unique identifier used to identify this artifact within the evaluation instance
	 * @param binaryData                   The data to store
	 * @throws Exception
	 */
	void storeLargeBinaryData(@Nonnull String evaluationInstanceIdentifier, @Nonnull String artifactIdentifier, @Nonnull byte[] binaryData) throws Exception;

}
