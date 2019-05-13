package mil.darpa.immortals.schemaevolution;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is intended to provide simplified read/write access to OrientDB.
 * <p>
 * All configuration options will be read from environment variables, so make sure they are properly passed to the
 * Process instance running this process if it is not running directly within the root evaluation harness.
 */
public class ChallengeProblemBridge implements ChallengeProblemBridgeInterface {

	// TODO: Check for uniqueness for identifiers

	private static final String BBNEvaluationDataLabel = "BBNEvaluationData";
	private static final String inputJsonDataLabel = "inputJsonData";
	private static final String outputJsonDataLabel = "outputJsonData";
	private static final String currentStateLabel = "currentState";
	private static final String currentStateInfoLabel = "currentStateInfo";

	private OrientGraphFactory _evaluationGraphFactory;

	private final String odbUser;
	private final String odbPassword;
	private final String odbTarget;
	private final Path artifactDirectory;

	private OrientGraphNoTx getEvaluationGraph() throws Exception {
		init();
		return new OrientGraphNoTx(odbTarget, odbUser, odbPassword);
	}

	public ChallengeProblemBridge() {
		odbUser = getEvaluationUser();
		odbPassword = getEvaluationPassword();
		odbTarget = getEvaluationTarget();
		artifactDirectory = getArtifactDirectory();
	}

	public static String getEvaluationTarget() {
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

	public static String getEvaluationUser() {
		if (System.getenv().containsKey(ENV_VAR_EVAL_USER)) {
			return System.getenv(ENV_VAR_EVAL_USER);
		} else {
			return "admin";
		}
	}

	public static String getEvaluationPassword() {

		if (System.getenv().containsKey(ENV_VAR_EVAL_PASSWORD)) {
			return System.getenv(ENV_VAR_EVAL_PASSWORD);
		} else {
			return "admin";
		}
	}

	public static Path getArtifactDirectory() {
		Path artifactDirectory;
		if (System.getProperty(JARGS_ARTIFACT_DIRECTORY) != null) {
			artifactDirectory = Paths.get(System.getProperty(JARGS_ARTIFACT_DIRECTORY));

		} else if (System.getenv(ENV_VAR_ARTIFACT_DIRECTORY) != null) {
			artifactDirectory = Paths.get(System.getenv(ENV_VAR_ARTIFACT_DIRECTORY));
		} else {
			throw new RuntimeException(
					"No Persistence directory could be set! Please set the environment variable '" +
							ENV_VAR_ARTIFACT_DIRECTORY + "' or the JVM argument '" + JARGS_ARTIFACT_DIRECTORY + "' to an appropriate directory!!");
		}

		if (!Files.exists(artifactDirectory)) {
			throw new RuntimeException("THe specified artifact directory '" + artifactDirectory.toAbsolutePath().toString() + "' does not exist!");
		}
		return artifactDirectory;
	}

	@Override
	public synchronized void init() throws Exception {
	}

	public TerminalStatus waitForReadyOrHalt() throws Exception {
		init();
		String state = null;
		System.out.print("Waiting for OirnetDB Ready state...");
		while (state == null || !(
				state.equals(TerminalStatus.ReadyForAdaptation.name()) ||
						state.equals(TerminalStatus.Halt.name()))) {
			Vertex v = getEvaluationGraph().getVerticesOfClass(BBNEvaluationDataLabel).iterator().next();
			state = v.getProperty(currentStateLabel);

			System.out.print(".");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		System.out.println();
		TerminalStatus rval = TerminalStatus.valueOf(state);

		if (rval == TerminalStatus.ReadyForAdaptation) {
			System.out.println("OrientDB Now Ready.");
		} else if (rval == TerminalStatus.Halt) {
			System.out.println("OrientDB Has set the Shutdown signal.");
		}
		return rval;
	}

	public TerminalStatus getState() throws Exception {
		Vertex v = getEvaluationGraph().getVerticesOfClass(BBNEvaluationDataLabel).iterator().next();
		String state = v.getProperty(currentStateLabel);
		return TerminalStatus.valueOf(state);
	}

	private void saveToFile(@Nonnull String evaluationInstanceIdentifier, @Nonnull byte[] data, @Nonnull String filename) throws IOException {
		Path subpath = artifactDirectory.resolve(evaluationInstanceIdentifier);
		if (!Files.exists(subpath)) {
			Files.createDirectory(artifactDirectory.resolve(evaluationInstanceIdentifier));
		}
		FileOutputStream fos = new FileOutputStream(subpath.resolve(filename).toFile());
		fos.write(data);
		fos.flush();
		fos.close();
	}

	@Override
	public synchronized String getConfigurationJson(@Nonnull String evaluationInstanceIdentifier) throws Exception {
		BBNEvaluationData data = getCurrentEvaluationData(evaluationInstanceIdentifier);
		return data.getInputJsonData();
	}

	private synchronized Map<String, String> getAddEvaluationDataVertexValues(@Nullable Map<String, String> valuesToSet, @Nullable String... valuesToGet) throws Exception {
		Map<String, String> rval = new HashMap<>();

		OrientGraphNoTx evaluationGraph = getEvaluationGraph();
		Iterator<Vertex> vertices = evaluationGraph.getVerticesOfClass(BBNEvaluationDataLabel).iterator();

		if (!vertices.hasNext()) {
			throw new RuntimeException("Evaluation Graph must contain a single " + BBNEvaluationDataLabel + " Node but none were found!");
		}

		Vertex evaluationVertex = vertices.next();

		if (vertices.hasNext()) {
			throw new RuntimeException("Evaluation Graph must contain a single " + BBNEvaluationDataLabel + " Node but multiple instances were found!");
		}

		if (valuesToGet != null) {
			for (String property : valuesToGet) {
				String value = evaluationVertex.getProperty(property);
				rval.put(property, value);
			}
		}

		if (valuesToSet != null) {
			for (String property : valuesToSet.keySet()) {
				String newValue = valuesToSet.get(property);
				evaluationVertex.setProperty(property, newValue);
				rval.put(property, newValue);
			}
			evaluationGraph.commit();
		}

		evaluationGraph.shutdown();

		return rval;
	}

	public synchronized void postResultsJson(@Nonnull String evaluationInstanceIdentifier, @Nonnull TerminalStatus finishStatus, @Nonnull String description, @Nonnull String jsonResults) throws Exception {
		updateCurrentEvaluationData(evaluationInstanceIdentifier,
				null,
				jsonResults,
				finishStatus.name(),
				description);

	}

	@Override
	public synchronized void postResultsJson(@Nonnull String evaluationInstanceIdentifier, @Nonnull TerminalStatus finishStatus, @Nonnull String results) throws Exception {
		updateCurrentEvaluationData(evaluationInstanceIdentifier,
				null,
				results,
				finishStatus.name(),
				null);
	}

	@Override
	public synchronized void postError(@Nonnull String evaluationInstanceIdentifier, @Nonnull String errorDescription, @Nullable String errorData) throws Exception {
		updateCurrentEvaluationData(evaluationInstanceIdentifier,
				null,
				errorData,
				TerminalStatus.AdaptationUnexpectedError.name(),
				errorData);
	}

	public synchronized void postInvalidInputError(@Nonnull String evaluationInstanceIdentifier, @Nonnull String errorDescription, @Nullable String errorData) throws Exception {
		updateCurrentEvaluationData(evaluationInstanceIdentifier,
				null,
				errorData,
				TerminalStatus.PerturbationInputInvalid.name(),
				errorData);
	}

	@Override
	public synchronized void storeLargeBinaryData(@Nonnull String evaluationInstanceIdentifier, @Nonnull String artifactIdentifier, @Nonnull byte[] binaryData) throws Exception {
		if (artifactIdentifier.startsWith("_")) {
			throw new RuntimeException("Artifact identifiers starting with an underscore cannot be used!");
		}
		saveToFile(evaluationInstanceIdentifier, binaryData, artifactIdentifier);
	}

	public BBNEvaluationData getCurrentEvaluationData(@Nonnull String evaluationInstanceIdentifier) throws Exception {
		Map<String, String> currentValues = getAddEvaluationDataVertexValues(
				null, inputJsonDataLabel, outputJsonDataLabel, currentStateLabel, currentStateInfoLabel);

		BBNEvaluationData data = BBNEvaluationData.fromFieldMap(currentValues);
		saveToFile(evaluationInstanceIdentifier, data.toJsonString().getBytes(), "_bbnEvaluationData.json");
		return data;
	}

	private BBNEvaluationData updateCurrentEvaluationData(@Nonnull String evaluationInstanceIdentifier,
	                                                      @Nullable String inputJsonData,
	                                                      @Nullable String outputJsonData,
	                                                      @Nullable String currentState,
	                                                      @Nullable String currentStateInfo) throws Exception {
		Map<String, String> valuesToSet = new HashMap<>();
		if (inputJsonData != null) {
			valuesToSet.put(inputJsonDataLabel, inputJsonData);
		}
		if (outputJsonData != null) {
			valuesToSet.put(outputJsonDataLabel, outputJsonData);
		}
		if (currentState != null) {
			valuesToSet.put(currentStateLabel, currentState);
		}
		if (currentStateInfo != null) {
			valuesToSet.put(currentStateInfoLabel, currentStateInfo);
		}

		Map<String, String> outputValues = getAddEvaluationDataVertexValues(valuesToSet, inputJsonDataLabel,
				outputJsonDataLabel, currentStateLabel, currentStateInfoLabel);
		BBNEvaluationData data = BBNEvaluationData.fromFieldMap(outputValues);
		saveToFile(evaluationInstanceIdentifier, data.toJsonString().getBytes(), "_bbnEvaluationData.json");
		return data;
	}
}
