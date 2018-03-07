package mil.darpa.immortals.das.context;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import okhttp3.Request;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 1/5/18.
 */
public class MockServices {

    public static class MockRetrofitAction<T> implements Call<T> {

        private final org.slf4j.Logger logger;
        private final String message;
        private boolean isCancelled = false;
        private boolean isExecuted = false;
        private T returnData;

        public MockRetrofitAction(@Nonnull org.slf4j.Logger logger, @Nonnull String message, T returnData) {
            this.logger = logger;
            this.message = message;
            this.returnData = returnData;
        }
//        
//        public synchronized void execute() {
//            logger.info(message);
//            isExecuted = true;
//        }

        @Override
        public synchronized Response<T> execute() throws IOException {
            logger.info(message);
            isExecuted = true;
            return Response.success(returnData);
        }

        @Override
        public void enqueue(@Nonnull Callback<T> callback) {
            logger.info(message);
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
            return new MockRetrofitAction<T>(logger, message, returnData);
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

        private Logger logger;
        private Gson gson = new GsonBuilder().setPrettyPrinting().create();

        public MockTestAdapter() {
            logger = getLogger(MockTestAdapter.class);
        }


        @Override
        public Call<Void> updateAdaptationStatus(AdaptationDetails testAdapterState) {
            logger.info(gson.toJson(testAdapterState));
            return null;
        }
    }

}
