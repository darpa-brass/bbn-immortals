package mil.darpa.immortals.testadapter;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.DasServiceConfiguration;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.TestHarnessConfiguration;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.TestAdapterState;
import mil.darpa.immortals.core.das.Mock;
import mil.darpa.immortals.core.das.ll.TestHarnessSubmissionInterface;
import mil.darpa.immortals.testadapter.restendpoints.DasSubmissionInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by awellman@bbn.com on 1/5/18.
 */
public class SubmissionServices {

    private static final String localIdentifier = "TH";

    public static class DasSubmitter implements DasSubmissionInterface {

        private final String remoteIdentifier = "DAS";
        private final DasSubmissionInterface dasSubmitter;
        private final ImmortalsUtils.NetworkLogger networkLogger;

        DasSubmitter() {
            // Start the rest client 
            DasServiceConfiguration dsc = ImmortalsConfig.getInstance().dasService;

            if (ImmortalsConfig.getInstance().debug.isUseMockDas()) {
                logger.debug("Using Mock DAS Submitter.");
                dasSubmitter = new Mock.MockDas();
            } else {
                String baseUrl = dsc.getProtocol() + "://" + dsc.getUrl() + ":" + dsc.getPort();
                logger.debug("Constructing DAS Submitter with URL '" + baseUrl + "'.");

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                this.dasSubmitter = retrofit.create(DasSubmissionInterface.class);
            }
            this.networkLogger = new ImmortalsUtils.NetworkLogger(localIdentifier, remoteIdentifier);
        }

        @Override
        public Call<AdaptationDetails> submitAdaptationRequest(String rdf) {
            networkLogger.logPostSending("bbn/das/submitAdaptationRequest", rdf);
            return dasSubmitter.submitAdaptationRequest(rdf);
        }
    }


    public static class TestHarnessSubmitter implements TestHarnessSubmissionInterface {

        private final String remoteIdentifier = "TH";
        private final TestHarnessSubmissionInterface testHarnessSubmitter;
        private final ImmortalsUtils.NetworkLogger networkLogger;

        TestHarnessSubmitter() {
            // Start the rest client 
            TestHarnessConfiguration thc = ImmortalsConfig.getInstance().testHarness;

            if (ImmortalsConfig.getInstance().debug.isUseMockTestHarness()) {
                logger.debug("Using Mock TA Submitter.");
                testHarnessSubmitter = new Mock.MockTestHarness();
            } else {

                String baseUrl = thc.getProtocol() + "://" + thc.getUrl() + ":" + thc.getPort();

                logger.debug("Constructing TH Submitter with URL '" + baseUrl + "'.");

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                testHarnessSubmitter = retrofit.create(TestHarnessSubmissionInterface.class);
            }
            this.networkLogger = new ImmortalsUtils.NetworkLogger(localIdentifier, remoteIdentifier);
        }

        @Override
        public Call<Void> ready() {
            networkLogger.logPostSending("/ready", null);
            return testHarnessSubmitter.ready();
        }

        @Override
        public Call<Void> error(String value) {
            networkLogger.logPostSending("/error", value);
            return testHarnessSubmitter.error(value);
        }

        @Override
        public Call<Void> status(TestAdapterState testAdapterState) {
            networkLogger.logPostSending("/status", testAdapterState);
            return testHarnessSubmitter.status(testAdapterState);
        }

        @Override
        public Call<Void> done(TestAdapterState testAdapterState) {
            networkLogger.logPostSending("/done", testAdapterState);
            return testHarnessSubmitter.done(testAdapterState);
        }
    }

    private static DasSubmissionInterface dasSubmitter;
    private static TestHarnessSubmissionInterface testHarnessSubmitter;

    private static Logger logger = LoggerFactory.getLogger(SubmissionServices.class);

    public static synchronized DasSubmissionInterface getDasSubmitter() {
        if (dasSubmitter == null) {
            dasSubmitter = new DasSubmitter();
        }
        return dasSubmitter;
    }

    public static synchronized TestHarnessSubmissionInterface getTestHarnessSubmitter() {
        if (testHarnessSubmitter == null) {
            testHarnessSubmitter = new TestHarnessSubmitter();
        }
        return testHarnessSubmitter;
    }
}
