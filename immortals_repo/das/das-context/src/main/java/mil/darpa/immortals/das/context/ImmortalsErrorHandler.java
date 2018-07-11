package mil.darpa.immortals.das.context;

import ch.qos.logback.classic.Logger;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.TestHarnessConfiguration;
import okhttp3.OkHttpClient;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 1/5/18.
 */
public class ImmortalsErrorHandler {

    public static final Thread.UncaughtExceptionHandler fatalExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            ImmortalsErrorHandler.reportFatalException(e);
        }
    };

    public interface TestHarnessErrorSubmissionInterface {
        @POST("/error")
        @Headers("Content-Type: text/plain")
        public Call<Void> error(@Body String value);
    }

    private static ImmortalsErrorHandler instance;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(ImmortalsErrorHandler.class);

    private TestHarnessErrorSubmissionInterface submissionInterface;

    private ImmortalsErrorHandler() {
        if (ImmortalsConfig.getInstance().debug.isUseMockTestHarness()) {
            submissionInterface = new MockServices.MockTestHarnessErrorSubmitter();
            
        } else {
            TestHarnessConfiguration thc = ImmortalsConfig.getInstance().testHarness;
            OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(thc.getProtocol() + "://" + thc.getUrl() + ":" + thc.getPort())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            submissionInterface = retrofit.create(TestHarnessErrorSubmissionInterface.class);
        }
    }

    private synchronized static ImmortalsErrorHandler getInstance() {
        if (instance == null) {
            instance = new ImmortalsErrorHandler();
        }
        return instance;
    }

    /**
     * This will result in complete shutdown of the DAS. Try to make it verbose to simplify debugging!
     *
     * @param error The error String to report
     */
    public static void reportFatalError(String error) {
        System.err.println("################################################################################");
        System.err.println("FATAL ERROR REPORTED: " + error);
        System.err.println("################################################################################");
        logger.trace("Submitting Fatal Error to TH");

        try {
            if (!ImmortalsConfig.getInstance().debug.isUseMockTestHarness()) {
                getInstance().submissionInterface.error(error).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@Nonnull Call<Void> call, @Nonnull Response<Void> response) {
                        // Ignore
                    }

                    @Override
                    public void onFailure(@Nonnull Call<Void> call, @Nonnull Throwable t) {
                        t.printStackTrace();
                        System.err.println(t.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            getInstance().submissionInterface.error(error);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This will result in complete shutdown of the DAS
     *
     * @param t The exception to report
     */
    public static void reportFatalException(Throwable t) {
        t.printStackTrace();
        System.err.println("################################################################################");
        System.err.println("FATAL EXCEPTION REPORTED: " + t.getMessage());
        t.printStackTrace();
        System.err.println("################################################################################");
        logger.trace("Submitting Fatal Exception to TH");

        getInstance().submissionInterface.error(t.getMessage()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@Nonnull Call<Void> call, @Nonnull Response<Void> response) {
                // Ignore
            }

            @Override
            public void onFailure(@Nonnull Call<Void> call, @Nonnull Throwable t) {
                t.printStackTrace();
                System.err.println(t.getMessage());
            }
        });
    }
}
