package mil.darpa.immortals;

import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum EnvironmentConfiguration {
	IMMORTALS_ROOT("IMMORTALS_ROOT", "mil.darpa.immortals.root",
			tryResolveRelativeToImmortalsRoot(false, "")),
	ODB_TARGET("ORIENTDB_EVAL_TARGET", "mil.darpa.immortals.odbTarget", null),
	ODB_USER("ORIENTDB_EVAL_USER", "mil.darpa.immortals.odbUser", "admin"),
	ODB_PASSWORD("ORIENTDB_EVAL_PASSWORD", "mil.darpa.immortals.odbPassword", "admin"),
	ARTIFACT_DIRECTORY("IMMORTALS_ARTIFACT_DIRECTORY", "mil.darpa.immortals.artifactdirectory",
			tryResolveRelativeToImmortalsRoot(true, "phase3", "DEFAULT_ARTIFACT_DIRECTORY")),
	ARTIFACT_DIRECTORY_SUBDIRECTORY("IMMORTALS_ARTIFACT_DIRECTORY_SUBDIRECTORY", "mil.darpa.immortals.artifactsubdirectory", null),
	ARTIFACT_PREFIX("IMMORTALS_ARTIFACT_PREFIX", "mil.darpa.immortals.artifactprefix", ""),
	CHALLENGE_PROBLEMS_ROOT("IMMORTALS_CHALLENGE_PROBLEMS_ROOT", "mil.darpa.immortals.challengeProblemsRoot", null),
	ADAPTIVE_CONSTRAINT_SATISFACTION_ROOT("IMMORTALS_ADAPTIVE_CONSTRAINT_SATISFACTION_ROOT", "mil.darpa.immortals.adaptiveConstraintSatisfactionRoot", null),
	DSL_PATH("IMMORTALS_RESOURCE_DSL", "mil.darpa.immortals.resourceDslRoot",
			tryResolveRelativeToImmortalsRoot(false, "dsl", "resource-dsl")),
	BASIC_DISPLAY_MODE("IMMORTALS_BASIC_DISPLAY_MODE", "mil.darpa.immortals.basicDisplayMode", true),
	MAX_DSL_SOLVER_DAUS("IMMORTALS_MAX_DSL_SOLVER_DAUS", "mil.darpa.immortals.maxDausSelected", "2");

	private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfiguration.class.getName());

	public static String getOdbTarget() {
		return ODB_TARGET.getValue();
	}

	public static String getOdbUser() {
		return ODB_USER.getValue();
	}

	public static String getOdbPassword() {
		return ODB_PASSWORD.getValue();
	}

	public static void setArtifactDirectorySubdirectory(@Nonnull String subdirectory) {
		System.setProperty(ARTIFACT_DIRECTORY_SUBDIRECTORY.javaArg, subdirectory);
	}

	public static void setArtifactPrefix(@Nonnull String artifactPrefix) {
		System.setProperty(ARTIFACT_PREFIX.javaArg, artifactPrefix);
	}

	public static int getMaxDauSelectionCount() {
			return Integer.parseInt(MAX_DSL_SOLVER_DAUS.getValue());
	}

	public static String getArtifactPrefix() {
		return ARTIFACT_PREFIX.getValue();
	}

	public static Path getArtifactDirectory() {
		try {

			Path artifactDirectory = Paths.get(ARTIFACT_DIRECTORY.getValue()).toAbsolutePath();
			if (!Files.exists(artifactDirectory)) {
				throw new RuntimeException("The artifact directory '" + artifactDirectory.toString() + "' does not exist!");
			}
			if (ARTIFACT_DIRECTORY_SUBDIRECTORY.isPresent()) {
				artifactDirectory = artifactDirectory.resolve(ARTIFACT_DIRECTORY_SUBDIRECTORY.getValue());
				if (!Files.exists(artifactDirectory)) {
					Files.createDirectory(artifactDirectory);
				}
			}
			return artifactDirectory;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isDefaultArtifactDirectory() {
		String immortalsRoot = tryResolveRelativeToImmortalsRoot(true, "phase3", "DEFAULT_ARTIFACT_DIRECTORY");
		if (immortalsRoot == null) {
			return false;
		} else {
			return getArtifactDirectory().equals(Paths.get(immortalsRoot));
		}
	}

	public static Path getDslRoot() {
		return Paths.get(DSL_PATH.getValue()).toAbsolutePath();
	}

	public static Path getImmortalsRoot() {
		return Paths.get(IMMORTALS_ROOT.getValue()).toAbsolutePath();
	}

	public static Path getChallengeProblemsRoot() {
		return Paths.get(CHALLENGE_PROBLEMS_ROOT.getValue()).toAbsolutePath();
	}

	public static boolean isBasicDisplayMode() {
		return BASIC_DISPLAY_MODE.isPresent();
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
			if (filename.startsWith("_")) {
				filename = filename.substring(1);
				filename = "_" + getArtifactPrefix() + filename;
			}
			Path target = getArtifactDirectory().resolve(filename);
			Files.write(target, data);
			return target.toString();
		}
	}

	static {
		logger.debug("---------------------------------INIT VARIABLES---------------------------------");
		for (EnvironmentConfiguration var : EnvironmentConfiguration.values()) {
			try {
				if (var.isFlag) {
					logger.debug(var.name() + (var.isPresent() ? "='true'" : "='false'"));
				} else {
					logger.debug(var.name() + "='" + var.getValue() + "'");
				}
			} catch (Exception e) {
				logger.debug(var.name() + "=UNDEFINED");
			}
		}
		logger.debug("--------------------------------------------------------------------------------");
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
	private final boolean isFlag;

	EnvironmentConfiguration(@Nonnull String envVar, @Nonnull String javaArg, @Nullable String defaultValue) {
		this.envVar = envVar;
		this.javaArg = javaArg;
		this.defaultValue = defaultValue;
		this.isFlag = false;
	}

	EnvironmentConfiguration(@Nonnull String envVar, @Nonnull String javaArg, boolean isFlag) {
		this.envVar = envVar;
		this.javaArg = javaArg;
		this.defaultValue = null;
		this.isFlag = isFlag;
	}


	public boolean isPresent() {
		return (System.getProperties().containsKey(javaArg) || System.getenv().containsKey(envVar));
	}

	public String getValue() {
		String rval;

		if (isFlag) {
			logger.warn("Should be using \"isFlagPresent\" method instead of \"getValue\" for the environment variable \"" + name() + "\"!");
		}

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

	public String getDisplayableUsage() {
		return "Please set the environment variable '" + this.envVar + "' or the java arg '" + this.javaArg + "'!";
	}
}
