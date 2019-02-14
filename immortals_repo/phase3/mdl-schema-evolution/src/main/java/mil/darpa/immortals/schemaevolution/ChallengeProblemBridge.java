package mil.darpa.immortals.schemaevolution;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * This class is intended to provide simplified read/write access to OrientDB.
 * <p>
 * All configuration options will be read from environment variables, so make sure they are properly passed to the
 * Process instance running this process if it is not running directly within the root evaluation harness.
 */
public class ChallengeProblemBridge implements ChallengeProblemBridgeInterface {

	// TODO: Check for uniqueness for identifiers

	private OrientGraphFactory _persistentGraphFactory;
	private OrientGraphFactory _evaluationGraphFactory;

	private OrientGraphNoTx getPersistentGraph() throws Exception {
		init();
		return _persistentGraphFactory.getNoTx();
	}

	private OrientGraphNoTx getEvaluationGraph() throws Exception {
		init();
		return _evaluationGraphFactory.getNoTx();
	}

	public ChallengeProblemBridge() {
	}

	@Override
	public synchronized void init() throws Exception {
		String evaluationTarget;
		String persistenceTarget;
		String evaluationUser;
		String evaluationPassword;

		if (_evaluationGraphFactory == null) {
			if (System.getProperty(JARGS_EVAL_ODB) != null) {
				evaluationTarget = System.getProperty(JARGS_EVAL_ODB);

			} else if (System.getenv().containsKey(ENV_VAR_EVAL_ODB)) {
				evaluationTarget = System.getenv(ENV_VAR_EVAL_ODB);
			} else {
				throw new RuntimeException(
						"No Evaluation OrientDB server could be set! Please set the environment variable '" +
								ENV_VAR_EVAL_ODB + "' or the JVM argument '" + JARGS_EVAL_ODB + "' to a server url!");
			}

			if (System.getProperty(JARGS_PERS_ODB) != null) {
				persistenceTarget = System.getProperty(JARGS_PERS_ODB);

			} else if (System.getenv().containsKey(ENV_VAR_PERS_ODB)) {
				persistenceTarget = System.getenv(ENV_VAR_PERS_ODB);

			} else {
				throw new RuntimeException(
						"No Persistence OrientDB server could be set! Please set the environment variable '" +
								ENV_VAR_PERS_ODB + "' or the JVM argument '" + JARGS_PERS_ODB + "' to a server url!");
			}
			


			if (System.getenv().containsKey(ENV_VAR_EVAL_USER)) {
				evaluationUser = System.getenv(ENV_VAR_EVAL_USER);
			} else {
				evaluationUser = "admin";
			}

			if (System.getenv().containsKey(ENV_VAR_EVAL_PASSWORD)) {
				evaluationPassword = System.getenv(ENV_VAR_EVAL_PASSWORD);
			} else {
				evaluationPassword = "admin";
			}

			_persistentGraphFactory = new OrientGraphFactory(persistenceTarget, "admin", "admin");
			_evaluationGraphFactory = new OrientGraphFactory(evaluationTarget, evaluationUser, evaluationPassword);
		}
	}

	@Override
	public synchronized String getConfigurationJson(@Nonnull String evaluationInstanceIdentifier) throws Exception {
		OrientGraphNoTx evaluationGraph = getEvaluationGraph();

		Iterator<Vertex> vertices = evaluationGraph.getVerticesOfClass("BBNEvaluationInput").iterator();

		Vertex configuration = vertices.next();

		if (configuration == null) {
			throw new Exception("No Config found!");
		} else if (vertices.hasNext()) {
			throw new Exception("Multiple Configs found!");
		}

		String rval = configuration.getProperty("jsonData");

		if (rval == null || "".equals(rval)) {
			throw new RuntimeException("No 'jsonData' attached to 'BBNEvaluationInput' node!");
		}

		OrientGraphNoTx persistenceGraph = getPersistentGraph();
		try {
			persistenceGraph.begin();
			OrientVertex ov = persistenceGraph.addVertex("class:BBNEvaluationInput");
			ov.setProperty("evaluationInstanceIdentifier", evaluationInstanceIdentifier);
			ov.setProperty("jsonData", rval);
			persistenceGraph.commit();

		} catch (Exception e) {
			persistenceGraph.rollback();
			throw e;
		} finally {
			persistenceGraph.shutdown();
		}

		evaluationGraph.shutdown();
		return rval;
	}


	private synchronized Vertex getEvaluationResultsVertex() throws Exception {
		OrientGraphNoTx evaluationGraph = getEvaluationGraph();
		Iterator<Vertex> vertices = evaluationGraph.getVerticesOfClass("BBNEvaluationOutput").iterator();

		if (!vertices.hasNext()) {
			throw new RuntimeException("Evaluation Graph must contain a single BBNEvaluationOutput Node but none were found!");
		}

		Vertex resultsVertex = vertices.next();

		if (vertices.hasNext()) {
			throw new RuntimeException("Evaluation Graph must contain a single BBNEvaluationOutput Node but multiple instances were found!");
		}

		evaluationGraph.shutdown();
		return resultsVertex;
	}

	private synchronized Vertex getPutPersistentResultsVertex(@Nonnull String evaluationInstanceIdentifier) throws Exception {
		OrientGraphNoTx persistenceGraph = getPersistentGraph();
		Vertex resultsVertex;

		Iterable<Vertex> o = persistenceGraph.command(new OCommandSQL(
				"SELECT FROM BBNEvaluationOutput WHERE evaluationInstanceIdentifier = '" + evaluationInstanceIdentifier + "'"
		)).execute();

		Iterator<Vertex> oi = o.iterator();

		if (oi.hasNext()) {
			return oi.next();
		}

		try {
			persistenceGraph.begin();
			resultsVertex = persistenceGraph.command(new OCommandSQL(
					"CREATE Vertex BBNEvaluationOutput SET evaluationInstanceIdentifier='" + evaluationInstanceIdentifier + "'"
			)).execute();

			persistenceGraph.commit();
		} catch (Exception e) {
			persistenceGraph.rollback();
			throw e;
		} finally {
			persistenceGraph.shutdown();
		}
		return resultsVertex;
	}

	@Override
	public synchronized void postResultsJson(@Nonnull String evaluationInstanceIdentifier, @Nonnull TerminalStatus finishStatus, @Nonnull String results) throws Exception {
		OrientGraphNoTx evaluationGraph = getEvaluationGraph();
		try {
			evaluationGraph.begin();
			Vertex evaluationResultsVertex = getEvaluationResultsVertex();
			evaluationResultsVertex.setProperty("finalState", finishStatus.name());
			evaluationResultsVertex.setProperty("resultsJson", results);
			evaluationGraph.commit();
		} catch (Exception e) {
			evaluationGraph.rollback();
			throw e;
		} finally {
			evaluationGraph.shutdown();
		}

		OrientGraphNoTx persistenceGraph = getPersistentGraph();
		try {
			persistenceGraph.begin();
			Vertex persistentResultsVertex = getPutPersistentResultsVertex(evaluationInstanceIdentifier);
			persistentResultsVertex.setProperty("finalState", finishStatus.name());
			persistentResultsVertex.setProperty("resultsJson", results);
			persistenceGraph.commit();
		} catch (Exception e) {
			persistenceGraph.rollback();
			throw e;
		} finally {
			persistenceGraph.shutdown();
		}
	}

	private synchronized void innerPostError(OrientGraphNoTx graph, Vertex resultsVertex, @Nonnull String errorDescription, @Nullable String errorData) throws Exception {
		try {
			graph.begin();

			resultsVertex.setProperty("finalState", TerminalStatus.AdaptationUnexpectedError);
			resultsVertex.setProperty("finalStateInfo", errorDescription);
			if (errorData != null) {
				resultsVertex.setProperty("resultsJson", errorData);
			}
			graph.commit();
		} catch (Exception e) {
			graph.rollback();
			throw e;
		} finally {
			graph.shutdown();
		}
	}

	@Override
	public synchronized void postError(@Nonnull String evaluationInstanceIdentifier, @Nonnull String errorDescription, @Nullable String errorData) throws Exception {
		OrientGraphNoTx evaluationGraph = getEvaluationGraph();
		Exception evaluationGraphException = null;
		try {
			Vertex evaluationResultsVertex = getEvaluationResultsVertex();
			innerPostError(evaluationGraph, evaluationResultsVertex, errorDescription, errorData);
		} catch (Exception e) {
			evaluationGraphException = e;
		}

		OrientGraphNoTx persistenceGraph = getPersistentGraph();
		Vertex persistenceVertex = getPutPersistentResultsVertex(evaluationInstanceIdentifier);
		innerPostError(persistenceGraph, persistenceVertex, errorDescription, errorData);

		if (evaluationGraphException != null) {
			throw evaluationGraphException;
		}
	}

	@Override
	public synchronized void storeLargeBinaryData(@Nonnull String evaluationInstanceIdentifier, @Nonnull String artifactIdentifier, @Nonnull byte[] binaryData) throws Exception {
		init();
		// TODO: Implement this. There is a direct interface that doesn't need any encoding.
	}
}
