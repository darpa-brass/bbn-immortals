package mil.darpa.immortals.schemaevolution;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import mil.darpa.immortals.EnvironmentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

	private final Logger logger = LoggerFactory.getLogger(ChallengeProblemBridge.class);

	// TODO: Check for uniqueness for identifiers

	private static final String BBNEvaluationDataLabel = "BBNEvaluationData";
	private static final String inputJsonDataLabel = "inputJsonData";
	private static final String outputJsonDataLabel = "outputJsonData";
	private static final String currentStateLabel = "currentState";
	private static final String currentStateInfoLabel = "currentStateInfo";

	private OrientGraphFactory _evaluationGraphFactory;

	private OrientGraphNoTx getEvaluationGraph() throws Exception {
		init();
		return new OrientGraphNoTx(EnvironmentConfiguration.getOdbTarget(), EnvironmentConfiguration.getOdbUser(), EnvironmentConfiguration.getOdbPassword());
	}

	public ChallengeProblemBridge() {
	}

	@Override
	public synchronized void init() throws Exception {
	}

	public TerminalStatus waitForReadyOrHalt() throws Exception {
		init();
		String state = null;

		logger.info("Waiting for OrientDB 'Halt' or 'ReadyForAdaptation' Ready state...");


		while (state == null || !(
				state.equals(TerminalStatus.ReadyForAdaptation.name()) ||
						state.equals(TerminalStatus.Halt.name()))) {
			OrientGraphNoTx graph = getEvaluationGraph();
			Vertex v = graph.getVerticesOfClass(BBNEvaluationDataLabel).iterator().next();
			state = v.getProperty(currentStateLabel);

			graph.shutdown();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		TerminalStatus rval = TerminalStatus.valueOf(state);

		if (rval == TerminalStatus.ReadyForAdaptation) {
			logger.info("OrientDB Now Ready.");
		} else if (rval == TerminalStatus.Halt) {
			logger.info("OrientDB Has set the Shutdown signal.");
		}
		return rval;
	}

	public String saveToFile(@Nonnull String evaluationInstanceIdentifier, @Nonnull byte[] data, @Nonnull String filename) throws IOException {
		File target = EnvironmentConfiguration.getArtifactDirectory().resolve(filename).toFile();
		FileOutputStream fos = new FileOutputStream(target);
		fos.write(data);
		fos.flush();
		fos.close();
		return target.toString();
	}

	@Nullable
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
			String result;
			boolean printDebug = ((result = valuesToSet.get(currentStateLabel)) != null);
			if (printDebug) {
				logger.trace("Setting currentState to '" + result + "'.");
			}
			evaluationGraph.commit();
			if (printDebug) {
				logger.trace("currentState set to '" + result + "'.");
			}
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
	@Deprecated
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

	@Override
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

	public synchronized void storeLargeBinaryDebugData(@Nonnull String evaluationInstanceIdentifier, @Nonnull String artifactIdentifier, @Nonnull byte[] binaryData) throws Exception {
		// TODO: Change to debug specific directory
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
