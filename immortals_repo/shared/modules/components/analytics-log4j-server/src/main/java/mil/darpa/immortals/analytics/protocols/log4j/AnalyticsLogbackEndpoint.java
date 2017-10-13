package mil.darpa.immortals.analytics.protocols.log4j;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;

import javax.annotation.Nonnull;
import java.io.IOException;


/**
 * Created by awellman@bbn.com on 7/31/17.
 */
public class AnalyticsLogbackEndpoint implements AnalyticsEndpointInterface {

    private static final Gson gson = new GsonBuilder().create();

    private Logger analyticsLogger = new LoggerContext().getLogger("ImmortalsAnalytics");

    AnalyticsLogbackEndpoint() {
    }

    @Override
    public void start() {
        // pass
    }

    @Override
    public void log(AnalyticsEvent event) {
        analyticsLogger.info(gson.toJson(event));
    }

    @Override
    public void shutdown() {
        analyticsLogger.getLoggerContext().stop();
    }

    void initFileLogger(@Nonnull String logFilepath) throws IOException {
        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(analyticsLogger.getLoggerContext());
        fileAppender.setFile(logFilepath);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(analyticsLogger.getLoggerContext());
        encoder.setPattern("%msg%n");
        fileAppender.setEncoder(encoder);
        encoder.start();
        fileAppender.start();

        analyticsLogger.addAppender(fileAppender);
    }

    void initConsoleLogger() {
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(analyticsLogger.getLoggerContext());

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(analyticsLogger.getLoggerContext());
        encoder.setPattern("%msg%n");
        consoleAppender.setEncoder(encoder);
        encoder.start();
        consoleAppender.start();

        analyticsLogger.addAppender(consoleAppender);
    }

    void initRestLogger(String address, int port) {
        LogbackAnalyticsRestfulAppender a = new LogbackAnalyticsRestfulAppender(address, port);
        a.setContext(analyticsLogger.getLoggerContext());
        a.start();
        analyticsLogger.addAppender(a);
    }
}
