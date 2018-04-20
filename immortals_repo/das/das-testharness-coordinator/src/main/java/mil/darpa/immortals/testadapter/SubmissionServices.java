package mil.darpa.immortals.testadapter;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.DasServiceConfiguration;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.TestHarnessConfiguration;
import mil.darpa.immortals.core.api.ll.phase2.result.TestAdapterState;
import mil.darpa.immortals.core.das.Mock;
import mil.darpa.immortals.core.das.ll.TestHarnessSubmissionInterface;
import mil.darpa.immortals.testadapter.restendpoints.DasSubmissionInterface;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by awellman@bbn.com on 1/5/18.
 */
public class SubmissionServices {

    private static final String localIdentifier = "TA";

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


                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .build();

                this.dasSubmitter = retrofit.create(DasSubmissionInterface.class);
            }
            this.networkLogger = ImmortalsUtils.getNetworkLogger(localIdentifier, remoteIdentifier);
        }

        @Override
        public Call<String> submitAdaptationRequest(String rdf) {
            networkLogger.logPostSending("bbn/das/submitAdaptationRequest", rdf);
            Call<String> call = dasSubmitter.submitAdaptationRequest(rdf);
            return call;
        }

        @Override
        public Call<String> submitValidationRequest(String rdf) {
            networkLogger.logPostSending("/bbin/das/submitValidationRequest", rdf);
            Call<String> call = dasSubmitter.submitValidationRequest(rdf);
            return call;
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
            this.networkLogger = ImmortalsUtils.getNetworkLogger(localIdentifier, remoteIdentifier);
        }

        @Override
        public synchronized Call<Void> ready() {
            networkLogger.logPostSending("/ready", null);
            Call<Void> call = testHarnessSubmitter.ready();
            return call;
        }

        @Override
        public synchronized Call<Void> error(String value) {
            networkLogger.logPostSending("/error", value);
            Call<Void> call = testHarnessSubmitter.error(value);
            return call;
        }

        @Override
        public synchronized Call<Void> status(TestAdapterState testAdapterState) {
            testAdapterState.timestamp = System.currentTimeMillis();
            networkLogger.logPostSending("/status", testAdapterState);
            Call<Void> call = testHarnessSubmitter.status(testAdapterState);
            return call;
        }

        @Override
        public synchronized Call<Void> done(TestAdapterState testAdapterState) {
            testAdapterState.timestamp = System.currentTimeMillis();
            networkLogger.logPostSending("/done", testAdapterState);
            Call<Void> call = testHarnessSubmitter.done(testAdapterState);
            return call;
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
