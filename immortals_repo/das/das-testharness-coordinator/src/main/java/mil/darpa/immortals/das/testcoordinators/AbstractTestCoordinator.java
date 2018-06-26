package mil.darpa.immortals.das.testcoordinators;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.das.TestCoordinatorExecutionInterface;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import mil.darpa.immortals.das.deploymentmodel.DeploymentModelBuilder;
import mil.darpa.immortals.testadapter.SubmissionServices;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Created by awellman@bbn.com on 3/28/18.
 */
public abstract class AbstractTestCoordinator implements TestCoordinatorExecutionInterface {


    protected final Logger logger;

    /**
     * Validates that the input submission model is valid. If the submission model does not contain any information
     * related to this challenge problem it is still considered valid and should be treated as a baseline scenario.
     *
     * @param submissionModel The submission model to validate
     * @return A list of errors if the model is invalid
     */
    @Nonnull
    abstract List<String> validateSubmissionModel(@Nonnull SubmissionModel submissionModel) throws Exception;

    abstract void setupChallengeProblem(@Nonnull SubmissionModel submissionModel) throws Exception;

    @Nonnull
    protected String getDeploymentModel(@Nonnull SubmissionModel submissionModel) throws Exception {
        Model deploymentModel = new DeploymentModelBuilder(submissionModel).build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        deploymentModel.write(out, "TURTLE");

        String rval = new String(out.toByteArray());
        System.out.println(rval);
        return rval;
    }

    AbstractTestCoordinator() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected Model loadDeploymentModelFromResource(@Nonnull String resourceName, @Nonnull String adaptationIdentifier) throws Exception {
        //Load the default deployment model
        InputStream is = this.getClass().getResourceAsStream(resourceName);
        Model deploymentModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(deploymentModel, is, Lang.TTL);

        //Get the deployment model statement
        Selector selector = new SimpleSelector(null, RDF.type,
                deploymentModel.getResource("http://darpa.mil/immortals/ontology/r2.0.0/gmei#DeploymentModel"));
        StmtIterator iter = deploymentModel.listStatements(selector);

        if (!iter.hasNext()) {
            logger.error("DeploymentModel triple not found in deployment model template. Check " + resourceName + ".");
            throw new Exception("Unable to create perturbed deployment model for DAS.");
        }

        org.apache.jena.rdf.model.Statement deploymentModelStatement = iter.next();

        deploymentModelStatement.getSubject().addLiteral(
                deploymentModel.getProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasSessionIdentifier"),
                adaptationIdentifier);

        return deploymentModel;
    }

    @Override
    public final Response execute(SubmissionModel submissionModel, boolean attemptAdaptation) {
        logger.info("TestCoordinator executed.");

        try {
            String adaptationIdentifier = submissionModel.sessionIdentifier;

            // Check if what this CP cares about is set
            boolean perturbEnvironment = submissionModel.martiServerModel != null &&
                    submissionModel.martiServerModel.requirements != null &&
                    submissionModel.martiServerModel.requirements.postgresqlPerturbation != null;

            logger.info("Adaptation identifier: " + adaptationIdentifier);


            // Validate the input submission model
            if (perturbEnvironment) {
                List<String> errors = validateSubmissionModel(submissionModel);
                String error = String.join("\n", errors);

                // If there are errors, log and return BAD_REQUEST
                if (!errors.isEmpty()) {
                    logger.error("Validation error in submission model");
                    errors.forEach(e -> logger.error(e + System.lineSeparator()));
                    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
                }
            }

            Thread t = new Thread(() -> {
                try {

                    if (perturbEnvironment) {
                        logger.info("Perturbing Environment");
                        setupChallengeProblem(submissionModel);
                    }

                    logger.info("Creating deployment model");
                    String deploymentModel = getDeploymentModel(submissionModel);
                    logger.trace("Deployment Model: " + deploymentModel);

                    if (attemptAdaptation) {
                        logger.info("Submitting deployment model to DAS for adaptation");
                        SubmissionServices.getDasSubmitter().submitAdaptationRequest(deploymentModel).execute();
                    } else {
                        logger.info("Submitting deployment model to DAS for validation");
                        SubmissionServices.getDasSubmitter().submitValidationRequest(deploymentModel).execute();
                    }

                } catch (Exception e) {
                    logger.error("Unexpected exception during adaptation request handling.", e);
                    ImmortalsErrorHandler.reportFatalException(e);
                }
            });

            t.setUncaughtExceptionHandler(ImmortalsErrorHandler.fatalExceptionHandler);
            t.start();
            if (attemptAdaptation) {
                logger.info("Adaptation entered running state.");
            } else {
                logger.info("Validation entered running state.");
            }
            return Response.ok().build();

        } catch (Exception e) {
            logger.error("Exception during initial submission to DAS.");

            return Response.serverError().build();
        }

    }
}
