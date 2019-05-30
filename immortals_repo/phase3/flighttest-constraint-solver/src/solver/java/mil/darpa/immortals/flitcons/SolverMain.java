package mil.darpa.immortals.flitcons;

import com.google.gson.JsonObject;
import mil.darpa.immortals.flitcons.mdl.FlighttestConstraintSolver;
import mil.darpa.immortals.flitcons.mdl.MdlDataValidator;
import mil.darpa.immortals.flitcons.mdl.OrientVertexDataSource;
import mil.darpa.immortals.flitcons.mdl.ValidationScenario;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import mil.darpa.immortals.schemaevolution.ProvidedData;
import mil.darpa.immortals.schemaevolution.TerminalStatus;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.io.StringWriter;

public class SolverMain {

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
		ChallengeProblemBridge cpb = new ChallengeProblemBridge();
		String evaluationInstanceIdentifier = "UNDEFINED";

		try {
			CommandLine.populateCommand(config, args);

			if (config.helpRequested) {
				CommandLine.usage(config, System.out);
			} else if (config.validateOrientdb) {
				validate(config);
			} else {
				TerminalStatus state;

				while ((state = cpb.waitForReadyOrHalt()) != TerminalStatus.Halt) {
					if (state == TerminalStatus.ReadyForAdaptation) {
						evaluationInstanceIdentifier = config.evaluationIdentifier == null ? ("I" + System.currentTimeMillis()) : config.evaluationIdentifier;
						cpb = ProvidedData.initializeChallengeProblemBridge(evaluationInstanceIdentifier);
						execute(cpb, evaluationInstanceIdentifier);
						if (config.stopOnFinish) {
							System.exit(0);
						}
						evaluationInstanceIdentifier = "UNDEFINED";
					}
				}
			}
		} catch (Exception e) {
			exceptionHandler(cpb, e, "UNDEFINED");
		}
	}

	static void execute(@Nonnull ChallengeProblemBridge cpb, @Nonnull String evaluationInstanceIdentifier) throws Exception {
		try {
			FlighttestConstraintSolver fcs = new FlighttestConstraintSolver();
			fcs.solve();
			fcs.shutdown();
			cpb.postResultsJson(evaluationInstanceIdentifier, TerminalStatus.AdaptationSuccessful, "");
		} catch (Exception e) {
			exceptionHandler(cpb, e, evaluationInstanceIdentifier);
		}
	}

	private static void validate(@Nonnull SolverConfiguration config) {
		OrientVertexDataSource dataSource = new OrientVertexDataSource();
		MdlDataValidator validator = new MdlDataValidator(null, null, dataSource);
		boolean inputIsValid = validator.validateConfiguration(ValidationScenario.InputConfigurationRequirements, !config.colorlessMode).isValid();
		boolean inventoryIsValid = validator.validateConfiguration(ValidationScenario.DauInventory, !config.colorlessMode).isValid();

		String inputResult = inputIsValid ? "PASSED" : "FAILED";
		String inventoryResult = inventoryIsValid ? "PASSED" : "FAILED";

		boolean somethingFailed = inputResult.equals("FAILED") || inventoryResult.equals("FAILED");

		String result = "\n\tInput Configuration: " + inputResult + "\n\tDAU Inventory: " + inventoryResult;

		if (somethingFailed) {
			System.err.println("VALIDATION FAILED:" + result);
			System.exit(101);
		} else {
			System.out.println("Validation Succeeded:" + result);
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
					case AdaptationPartiallySuccessful:
					case AdaptationUnsuccessful:
						cpb.postResultsJson(evaluationInstanceIdentifier, TerminalStatus.valueOf(ae.result.name()), e.getMessage(), null);
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
