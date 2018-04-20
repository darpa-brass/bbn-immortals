package mil.darpa.immortals.das.testcoordinators;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.das.TestCoordinatorExecutionInterface;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import mil.darpa.immortals.testadapter.SubmissionServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 1/5/18.
 */
public class P2CP2TestCoordinator implements TestCoordinatorExecutionInterface {

    private Logger logger = LoggerFactory.getLogger(P2CP2TestCoordinator.class);

    @Override
    public Response execute(SubmissionModel submissionModel, boolean attemptAdaptation) {
        String adaptationIdentifier;
//        AdaptationDetails adaptationDetails = null;
        // Do prepwork and produce the initial AdaptationDetails...
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            ImmortalsErrorHandler.reportFatalException(e);
        }

        // Produce the initial RDF
        // TODO: Make real RDF
        String rdf = submissionModel.sessionIdentifier;

        // Submit TTL asynchronously
//        SubmissionServices.getDasSubmitter().submitAdaptationRequest(dac).enqueue(new Callback<AdaptationDetails>() {
//            @Override
//            public void onResponse(@Nonnull Call<AdaptationDetails> call, @Nonnull Response<AdaptationDetails> response) {
//                logger.trace("Received Successful callback from DAS");
//            }
//
//            @Override
//            public void onFailure(@Nonnull Call<AdaptationDetails> call, @Nonnull Throwable t) {
//                logger.trace("Received Failure callback from DAS");
//                CoordinatorMain.fatalExceptionHandler.uncaughtException(Thread.currentThread(), t);
//            }
//        });

//        // or Submit TTL synchronously
        logger.trace("TH Submitting DasAdaptationContext to DAS");
        try {
            adaptationIdentifier = SubmissionServices.getDasSubmitter().submitAdaptationRequest(rdf).execute().body();
            logger.trace("TH Submitting DasAdaptationContext to DAS: ACK Received");
            if (adaptationIdentifier == null) {
                ImmortalsErrorHandler.reportFatalError("DAS should not return empty adaptationIdentifier to TestAdapter!");
            }
        } catch (IOException e) {
            ImmortalsErrorHandler.reportFatalException(e);
        }

        // Get an initial result for the response and return it
        // Return the previously obtained initial response
        return Response.ok().build();
    }
}
