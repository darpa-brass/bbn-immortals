package mil.darpa.immortals.das.context;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.TestAdapterConfiguration;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetailsList;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetailsList;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by awellman@bbn.com on 1/5/18.
 */
public class TestAdapterSubmitter {

    private static final AtomicInteger sequenceGenerator = new AtomicInteger(0);

    public interface TestAdapterSubmissionInterface {
        @POST("/dasListener/updateAdaptationStatus")
        @Headers("Content-Type: application/json")
        Call<Void> updateAdaptationStatus(@Body AdaptationDetailsList adaptationState);


        @POST("/dasListener/updateValidationStatus")
        @Headers("Content-Type: application/json")
        Call<Void> updateValidationStatus(@Body TestDetailsList validatorState);
    }

    private static TestAdapterSubmitter instance;

    //    private static final Logger logger = (Logger) LoggerFactory.getLogger(TestAdapterSubmitter.class);
    private static final ImmortalsUtils.NetworkLogger networkLogger =
            ImmortalsUtils.getNetworkLogger("DAS", "TA");

    private TestAdapterSubmissionInterface submissionInterface;

    private TestAdapterSubmitter() {
        if (ImmortalsConfig.getInstance().debug.isUseMockTestAdapter()) {
            submissionInterface = new MockServices.MockTestAdapter();
        } else {
            TestAdapterConfiguration tac = ImmortalsConfig.getInstance().testAdapter;

            OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
            
            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(tac.getProtocol() + "://" + tac.getUrl() + ":" + tac.getPort() + "/")
                    .addConverterFactory(GsonConverterFactory.create(ImmortalsUtils.gson))
                    .build();

            submissionInterface = retrofit.create(TestAdapterSubmissionInterface.class);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized static TestAdapterSubmitter getInstance() {
        if (instance == null) {
            instance = new TestAdapterSubmitter();
        }
        return instance;
    }

    public static synchronized void updateAdaptationStatus(@Nonnull AdaptationDetailsList adaptationDetails) {
        updateAdaptationStatus(adaptationDetails, true);
    }

    public static synchronized void updateAdaptationStatus(@Nonnull AdaptationDetailsList adaptationDetails, boolean updateSequenceNumber) {
        if (updateSequenceNumber) {
            adaptationDetails.sequence = sequenceGenerator.getAndIncrement();
        }
        networkLogger.logPostSending("/dasListener/updateAdaptationStatus", adaptationDetails);
        getInstance().submissionInterface.updateAdaptationStatus(adaptationDetails).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@Nonnull Call<Void> call, @Nonnull Response<Void> response) {
                networkLogger.logPostSendingAckReceived("/dasListener/updateAdaptationStatus", response.code(), null);
            }

            @Override
            public void onFailure(@Nonnull Call<Void> call, @Nonnull Throwable t) {
                ImmortalsErrorHandler.reportFatalException(t);
            }
        });
    }

    public static synchronized void updateValidationStatus(@Nonnull TestDetailsList testDetails) {
        updateValidationStatus(testDetails, true);
    }

    public static synchronized void updateValidationStatus(@Nonnull TestDetailsList testDetails, boolean updateSequenceNumber) {
        if (updateSequenceNumber) {
            testDetails.sequence = sequenceGenerator.getAndIncrement();
        }
        networkLogger.logPostSending("/dasListener/updateValidationStatus", testDetails);
        getInstance().submissionInterface.updateValidationStatus(testDetails).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@Nonnull Call<Void> call, @Nonnull Response<Void> response) {
                networkLogger.logPostSendingAckReceived("/dasListener/updateValidationStatus", response.code(), null);
            }

            @Override
            public void onFailure(@Nonnull Call<Void> call, @Nonnull Throwable t) {
                ImmortalsErrorHandler.reportFatalException(t);
            }
        });
    }
}
