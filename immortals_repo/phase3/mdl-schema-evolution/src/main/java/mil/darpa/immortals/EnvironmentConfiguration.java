package mil.darpa.immortals;

import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public enum EnvironmentConfiguration {
	IMMORTALS_ROOT("IMMORTALS_ROOT", "mil.darpa.immortals.root", tryResolveRelativeToImmortalsRoot(false, "")),
	ODB_TARGET("ORIENTDB_EVAL_TARGET", "mil.darpa.immortals.odbTarget", null),
	ODB_USER("ORIENTDB_EVAL_USER", "mil.darpa.immortals.odbUser", "admin"),
	ODB_PASSWORD("ORIENTDB_EVAL_PASSWORD", "mil.darpa.immortals.odbPassword", "admin"),
	ARTIFACT_DIRECTORY("IMMORTALS_ARTIFACT_DIRECTORY", "mil.darpa.immortals.artifactdirectory",
			tryResolveRelativeToImmortalsRoot(true, "phase3", "DEFAULT_ARTIFACT_DIRECTORY"));

	private static final Logger logger = Logger.getLogger(EnvironmentConfiguration.class.getName());


	public static String getOdbTarget() {
		return ODB_TARGET.getValue();
	}

	public static String getOdbUser() {
		return ODB_USER.getValue();
	}

	public static String getOdbPassword() {
		return ODB_PASSWORD.getValue();
	}

	public static Path getArtifactDirectory() {
		return Paths.get(ARTIFACT_DIRECTORY.getValue()).toAbsolutePath();
	}

	public static Path getImmortalsRoot() {
		return Paths.get(IMMORTALS_ROOT.getValue()).toAbsolutePath();
	}

	private static ChallengeProblemBridge challengeProblemBridge;
	private static String evaluationIdentifier;

	public static ChallengeProblemBridge initializeChallengeProblemBridge(@Nonnull String evaluationIdentifier) {
		challengeProblemBridge = new ChallengeProblemBridge();
		EnvironmentConfiguration.evaluationIdentifier = evaluationIdentifier;
		return challengeProblemBridge;
	}

	public static String storeFile(@Nonnull String filename, byte[] data) throws Exception {
		if (challengeProblemBridge != null) {
			return challengeProblemBridge.saveToFile(evaluationIdentifier, data, filename);
		} else {
			Path target = getArtifactDirectory().resolve(filename);
			Files.write(target, data);
			return target.toString();
		}
	}

	static {
		logger.config("---------------------------------INIT VARIABLES---------------------------------");
		for (EnvironmentConfiguration var : EnvironmentConfiguration.values()) {
			try {
				logger.config(var.name() + "='" + var.getValue() + "'");
			} catch (Exception e) {
				logger.config(var.name() + "=UNDEFINED");
			}
		}
		logger.config("--------------------------------------------------------------------------------");
	}

	private static String tryResolveRelativeToImmortalsRoot(boolean createIfParentExists, String... desiredChildpath) {
		Path testPath = Paths.get("").toAbsolutePath();
		Path immortalsRoot = null;

		while (testPath != null && immortalsRoot == null) {
			if (Files.exists(testPath.resolve("dsl")) &&
					Files.exists(testPath.resolve("knowledge-repo")) &&
					Files.exists(testPath.resolve("phase3"))) {
				immortalsRoot = testPath;
			} else {
				testPath = testPath.getParent();
			}
		}

		if (immortalsRoot == null) {
			return null;
		} else {
			Path desired = immortalsRoot.resolve(Paths.get("", desiredChildpath));
			if (Files.exists(desired)) {
				return desired.toString();
			} else if (Files.exists(desired.getParent())) {
				if (createIfParentExists) {
					try {
						Files.createDirectory(desired);
						return desired.toString();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public final String envVar;
	public final String javaArg;
	private final String defaultValue;

	EnvironmentConfiguration(@Nonnull String envVar, @Nonnull String javaArg, @Nullable String defaultValue) {
		this.envVar = envVar;
		this.javaArg = javaArg;
		this.defaultValue = defaultValue;
	}

	public String getValue() {
		String rval;
		if ((rval = System.getProperty(javaArg)) != null) {
			return rval;
		} else if ((rval = System.getenv(envVar)) != null) {
			return rval;
		} else if (defaultValue != null) {
			return defaultValue;
		} else {
			throw new RuntimeException(
					"No value for '" + name() + "' has been set! Please set the environment variable '" +
							envVar + "' or the JVM argument '" + javaArg + "'!");
		}
	}
}
