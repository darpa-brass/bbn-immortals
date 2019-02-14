package mil.darpa.immortals.das.context;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetailsList;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetailsList;
import okhttp3.Request;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 1/5/18.
 */
public class MockServices {

    public static class MockRetrofitAction<T> implements Call<T> {

        private final ImmortalsUtils.NetworkLogger logger;
        private final String path;
        private final Object body;
        private boolean isCancelled = false;
        private boolean isExecuted = false;
        private T returnData;

        public MockRetrofitAction(@Nonnull String path, @Nonnull ImmortalsUtils.NetworkLogger logger, @Nullable Object body, T returnData) {
            this.path = path;
            this.logger = logger;
            this.body = body;
            this.returnData = returnData;
        }
//        
//        public synchronized void execute() {
//            logger.info(message);
//            isExecuted = true;
//        }

        @Override
        public synchronized Response<T> execute() throws IOException {
            logger.logPostReceived(path, body);
            isExecuted = true;
            return Response.success(returnData);
        }

        @Override
        public void enqueue(@Nonnull Callback<T> callback) {
            logger.logPostReceived(path, body);
            isExecuted = true;
            callback.onResponse(this, Response.success(returnData));
        }

        @Override
        public synchronized boolean isExecuted() {
            return false;
        }

        @Override
        public synchronized void cancel() {
            isCancelled = true;
        }

        @Override
        public synchronized boolean isCanceled() {
            return isCancelled;
        }

        @Override
        public Call<T> clone() {
            return new MockRetrofitAction<T>(path, logger, body, returnData);
        }

        @Override
        public synchronized Request request() {
            return null;
        }
    }

    public static Logger getLogger(Class clazz) {
        Logger logger = (Logger) LoggerFactory.getLogger(clazz);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder.setContext(logger.getLoggerContext());
        encoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setFile(ImmortalsConfig.getInstance().globals.getGlobalLogDirectory().resolve(
                clazz.getSimpleName() + ".log").toAbsolutePath().toString());
        fileAppender.setContext(logger.getLoggerContext());
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        logger.addAppender(fileAppender);
        return logger;
    }

    public static class MockTestAdapter implements TestAdapterSubmitter.TestAdapterSubmissionInterface {

        private ImmortalsUtils.NetworkLogger logger = new ImmortalsUtils.NetworkLogger("MOCKTA", null);

        public MockTestAdapter() {
        }

        @Override
        public Call<Void> updateAdaptationStatus(AdaptationDetailsList testAdapterState) {
            logger.logPostReceived("/dasListener/updateAdaptationStatus", testAdapterState);
            return new MockRetrofitAction<>("/dasListener/updateAdaptationStatus", logger, testAdapterState, null);
        }

        @Override
        public Call<Void> updateValidationStatus(TestDetailsList validatorState) {
            return new MockRetrofitAction<>("/dasListener/updateValidationStatus", logger, validatorState, null);
        }
    }


    public static class MockTestHarnessErrorSubmitter implements ImmortalsErrorHandler.TestHarnessErrorSubmissionInterface {

        private ImmortalsUtils.NetworkLogger logger = new ImmortalsUtils.NetworkLogger("MOCKTH", null);

        public MockTestHarnessErrorSubmitter() {
        }

        @Override
        public Call<Void> error(String value) {
            logger.logPostReceived("/error", null);
            return new mil.darpa.immortals.das.context.MockServices.MockRetrofitAction<>("/error", logger, value, null);
        }

    }
}
