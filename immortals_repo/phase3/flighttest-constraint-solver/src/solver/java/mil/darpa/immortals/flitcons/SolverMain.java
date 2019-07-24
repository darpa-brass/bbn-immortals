package mil.darpa.immortals.flitcons;

import com.google.gson.JsonObject;
import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.mdl.FlighttestConstraintSolver;
import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.OrientVertexDataSource;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import mil.darpa.immortals.schemaevolution.TerminalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

public class SolverMain {

	private static final Logger logger = LoggerFactory.getLogger(SolverMain.class);

	private static String jsonifyException(@Nonnull Throwable t) {
		JsonObject jo = new JsonObject();

		if (t instanceof AdaptationnException) {
			AdaptationnException ae = (AdaptationnException) t;
			jo.addProperty("adaptationStatus", ae.result.name());
		}

		jo.addProperty("message", t.getMessage());

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		pw.close();
		jo.addProperty("stacktrace", sw.toString());
		return jo.toString();
	}

	public static void main(String[] args) {
		SolverConfiguration config = SolverConfiguration.getInstance();
		CommandLine.populateCommand(config, args);

		if (config.isHelpRequested()) {
			CommandLine.usage(config, System.out);
		} else if (config.isValidateOrientdb()) {
			validate();
		} else {
			execute();
		}
	}

	static void execute() {
		String evaluationInstanceIdentifier = null;
		ChallengeProblemBridge cpb = new ChallengeProblemBridge();

		Set<String> previousEvaluationIdentifiers = new HashSet<>();

		try {
			SolverConfiguration config = SolverConfiguration.getInstance();

			TerminalStatus state;

			while ((state = cpb.waitForReadyOrHalt()) != TerminalStatus.Halt) {
				logger.info("Starting Adaptation");
				if (state == TerminalStatus.ReadyForAdaptation) {
					evaluationInstanceIdentifier = config.getEvaluationIdentifier() == null ? ("I" + System.currentTimeMillis()) : config.getEvaluationIdentifier();
					if (previousEvaluationIdentifiers.contains(evaluationInstanceIdentifier)) {
						throw new RuntimeException("Refusing to reuse the evaluation identifier '" + evaluationInstanceIdentifier + "'!");
					}
					previousEvaluationIdentifiers.add(evaluationInstanceIdentifier);

					cpb = EnvironmentConfiguration.initializeChallengeProblemBridge(evaluationInstanceIdentifier);

					FlighttestConstraintSolver fcs = new FlighttestConstraintSolver();
					fcs.solve();
					fcs.shutdown();
					logger.info("Adaptation finished with result 'AdaptationSuccessful'. Submitting to OrientDB....");
					cpb.postResultsJson(evaluationInstanceIdentifier, TerminalStatus.AdaptationSuccessful, "");
					logger.info("Results submitted to OrientDB.");

					if (config.isStopOnFinish()) {
						System.exit(0);
					}
					evaluationInstanceIdentifier = "UNDEFINED";
				}
			}

		} catch (Exception e) {
			exceptionHandler(cpb, e, evaluationInstanceIdentifier == null ? "UNDEFINED" : evaluationInstanceIdentifier);
		}
	}

	private static void validate() {
		OrientVertexDataSource dataSource = new OrientVertexDataSource();
		MdlDataValidator validator = new MdlDataValidator(null, null, dataSource);
		boolean inputIsValid = validator.validateConfiguration(ValidationScenario.InputConfigurationRequirements).isValid();
		boolean inventoryIsValid = validator.validateConfiguration(ValidationScenario.DauInventory).isValid();

		String inputResult = inputIsValid ? "PASSED" : "FAILED";
		String inventoryResult = inventoryIsValid ? "PASSED" : "FAILED";

		boolean somethingFailed = inputResult.equals("FAILED") || inventoryResult.equals("FAILED");

		String result = "\n\tInput Configuration: " + inputResult + "\n\tDAU Inventory: " + inventoryResult;

		if (somethingFailed) {
			logger.error("VALIDATION FAILED:" + result);
			System.exit(101);
		} else {
			logger.info("Validation Succeeded:" + result);
		}
	}

	private static void exceptionHandler(@Nonnull ChallengeProblemBridge cpb, @Nonnull Throwable e, @Nonnull String evaluationInstanceIdentifier) {
		System.err.println(e.getMessage());
		e.printStackTrace(System.err);


		if (e instanceof AdaptationnException) {
			try {
				AdaptationnException ae = (AdaptationnException) e;

				switch (ae.result) {
					case ReadyForAdaptation:
					case AdaptationSuccessful:
						cpb.postError(evaluationInstanceIdentifier, "Unexpected exception state '" +
								ae.result.name() + "'!", jsonifyException(e));
						break;

					case PerturbationInputInvalid:
						cpb.postInvalidInputError(evaluationInstanceIdentifier,
								"The perturbation input was invalid.", jsonifyException(e));
						break;

					case AdaptationNotRequired:
						logger.info("Adaptation not required. " + ae.getMessage());
						cpb.postResultsJson(evaluationInstanceIdentifier, TerminalStatus.valueOf(ae.result.name()), e.getMessage(), "");
						break;

					case AdaptationPartiallySuccessful:
						logger.info("Adaptation was only partially successful. " + ae.getMessage());
						cpb.postResultsJson(evaluationInstanceIdentifier, TerminalStatus.valueOf(ae.result.name()), e.getMessage(), "");
						break;

					case AdaptationUnsuccessful:
						logger.info("Adaptation was unsuccessful. " + ae.getMessage());
						cpb.postResultsJson(evaluationInstanceIdentifier, TerminalStatus.valueOf(ae.result.name()), e.getMessage(), "");
						break;

					case AdaptationInternalError:
					case AdaptationUnexpectedError:
						cpb.postError(evaluationInstanceIdentifier, "Unexpected Internal Error!",
								jsonifyException(e));
						break;
				}
			} catch (Exception e2) {
				System.err.println(e2.getMessage());
				e2.printStackTrace(System.err);
				try {
					cpb.postError(evaluationInstanceIdentifier, e2.getMessage(), "{}");
				} catch (Exception e3) {
					System.err.println(e3.getMessage());
					throw new RuntimeException(e);
				}
			}

		} else {

			try {
				String msg = e.getMessage();

				if (msg == null) {
					System.err.println("EXCEPTION WITH NO MESSAGE!!");
					e.printStackTrace();
					cpb.postError(evaluationInstanceIdentifier, "UNDEFINED", jsonifyException(e));

				} else {
					System.err.println(e.getMessage());
					e.printStackTrace();
					cpb.postError(evaluationInstanceIdentifier, e.getMessage(), null);
				}
			} catch (Exception e2) {
				System.err.println(e.getMessage());
				logger.error(e.getMessage());
				e.printStackTrace();
				System.err.println();
				System.err.println();
				System.out.println();
				System.out.println();
				System.err.println(e2.getMessage());
				e2.printStackTrace();
			}
			System.exit(-1);
		}
	}
}
