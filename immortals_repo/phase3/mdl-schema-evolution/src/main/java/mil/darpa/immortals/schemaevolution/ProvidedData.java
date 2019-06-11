package mil.darpa.immortals.schemaevolution;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class ProvidedData {

	private static final Logger logger = Logger.getLogger(ProvidedData.class.getName());

	private static final String ENV_VAR_EVAL_ODB = "ORIENTDB_EVAL_TARGET";
	private static final String ENV_VAR_EVAL_USER = "ORIENTDB_EVAL_USER";
	private static final String ENV_VAR_EVAL_PASSWORD = "ORIENTDB_EVAL_PASSWORD";
	private static final String ENV_VAR_ARTIFACT_DIRECTORY = "IMMORTALS_ARTIFACT_DIRECTORY";
	public static final String JARGS_ARTIFACT_DIRECTORY = "mil.darpa.immortals.artifactdirectory";
	public static final String JARGS_EVAL_ODB = "mil.darpa.immortals.evaluationserver";


	public static final String getOdbEvaluationTarget() {
		if (System.getProperty(JARGS_EVAL_ODB) != null) {
			return System.getProperty(JARGS_EVAL_ODB);
		} else if (System.getenv().containsKey(ENV_VAR_EVAL_ODB)) {
			return System.getenv(ENV_VAR_EVAL_ODB);
		} else {
			throw new RuntimeException(
					"No Evaluation OrientDB server could be set! Please set the environment variable '" +
							ENV_VAR_EVAL_ODB + "' or the JVM argument '" + JARGS_EVAL_ODB + "' to a server url!");
		}
	}

	public static final String getOdbEvaluationUser() {
		if (System.getenv().containsKey(ENV_VAR_EVAL_USER)) {
			return System.getenv(ENV_VAR_EVAL_USER);
		} else {
			return "admin";
		}
	}

	public static final String getOdbEvaluationPassword() {
		if (System.getenv().containsKey(ENV_VAR_EVAL_PASSWORD)) {
			return System.getenv(ENV_VAR_EVAL_PASSWORD);
		} else {
			return "admin";
		}
	}

	public static final Path getEvaluationArtifactDirectory() {
		Path evaluationArtifactDirectory;
		if (System.getProperty(JARGS_ARTIFACT_DIRECTORY) != null) {
			evaluationArtifactDirectory = Paths.get(System.getProperty(JARGS_ARTIFACT_DIRECTORY));

		} else if (System.getenv(ENV_VAR_ARTIFACT_DIRECTORY) != null) {
			evaluationArtifactDirectory = Paths.get(System.getenv(ENV_VAR_ARTIFACT_DIRECTORY));
		} else {
			evaluationArtifactDirectory = Paths.get("DEFAULT_ARTIFACT_DIRECTORY").toAbsolutePath();
		}

		if (!Files.exists(evaluationArtifactDirectory.getParent())) {
			throw new RuntimeException("The specified artifact directory '" + evaluationArtifactDirectory.toAbsolutePath().toString() + "' does not exist!");
		}

		if (!Files.exists(evaluationArtifactDirectory)) {
			try {
				Files.createDirectory(evaluationArtifactDirectory);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return evaluationArtifactDirectory;
	}

	private static String evaluationIdentifier;
	private static ChallengeProblemBridge challengeProblemBridge;

	public static final ChallengeProblemBridge initializeChallengeProblemBridge(@Nonnull String evaluationIdentifier) {
		challengeProblemBridge = new ChallengeProblemBridge();
		ProvidedData.evaluationIdentifier = evaluationIdentifier;
		return challengeProblemBridge;
	}

	public static final String storeFile(@Nonnull String filename, byte[] data) throws Exception {
		if (challengeProblemBridge != null) {
			return challengeProblemBridge.saveToFile(evaluationIdentifier, data, filename);
		} else {
			Path target = getEvaluationArtifactDirectory().resolve(filename);
			Files.write(target, data);
			return target.toString();
		}
	}

	static {
		logger.config("---------------------------------INIT VARIABLES---------------------------------");
		logger.config("odbEvaluationTarget='" + getOdbEvaluationTarget() + "'");
		logger.config("odbEvaluationUser='" + getOdbEvaluationUser() + "'");
		logger.config("odbEvaluationPassword='" + getOdbEvaluationPassword() + "'");
		logger.config("evaluationArtifactDirectory='" + getEvaluationArtifactDirectory() + "'");
		logger.config("--------------------------------------------------------------------------------");
	}
}
