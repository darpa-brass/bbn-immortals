package mil.darpa.immortals.analytics.protocols.log4j;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import mil.darpa.immortals.analytics.L;
import mil.darpa.immortals.analytics.validators.ValidatorManager;
import mil.darpa.immortals.analytics.validators.result.ValidationResults;
import mil.darpa.immortals.analytics.validators.result.ValidationResultsListener;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsVerbosity;
import org.slf4j.event.Level;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 7/20/16.
 */
class Log4jValidationServer extends Thread implements ValidationResultsListener {

    private volatile boolean isRunning = false;

    private final ValidatorManager validatorManager;

    private Logger rootLogger = new LoggerContext().getLogger("ImmortalsAnalytics");

    private final int maxRunDurationSeconds;

    private final Log4jClientServer clientServer;

    public Log4jValidationServer(int port, Level logLevel, int maxDurationSeconds) {

        validatorManager = new ValidatorManager(this);

        clientServer = new Log4jClientServer(port, new ValidatorManagerLog4jAppender(validatorManager));

        this.maxRunDurationSeconds = maxDurationSeconds;

        L.initialize(rootLogger);
        Analytics.initializeEndpoint(L.getAnalyticsEndpointInstance());
        Analytics.setSourceIdentifier("Log4jAnalyticsServer");
        Analytics.setVerbosity(AnalyticsVerbosity.Data);
        rootLogger.setLevel(ch.qos.logback.classic.Level.valueOf(logLevel.name()));
    }

    public void initFileLogger(@Nonnull String logFilepath) throws IOException {
        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(rootLogger.getLoggerContext());
        fileAppender.setFile(logFilepath);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(rootLogger.getLoggerContext());
        encoder.setPattern("%msg%n");
        fileAppender.setEncoder(encoder);
        encoder.start();
        fileAppender.start();

        rootLogger.addAppender(fileAppender);
    }

    public void initConsoleLogger() {
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(rootLogger.getLoggerContext());

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(rootLogger.getLoggerContext());
        encoder.setPattern("%msg%n");
        consoleAppender.setEncoder(encoder);
        encoder.start();
        consoleAppender.start();

        rootLogger.addAppender(consoleAppender);
    }

    public synchronized void addValidators(@Nonnull Set<String> clientIdentifiers, @Nonnull String... validatorIdentifiers) {

        List<String> identifiers = Arrays.asList(validatorIdentifiers);

        if (identifiers.contains("all")) {
            identifiers = new ArrayList(ValidatorManager.VALIDATOR_IDENTIFIERS);
        }

        for (String validatorIdentifier : identifiers) {
            validatorManager.addValidator(clientIdentifiers, validatorIdentifier);
        }
    }

    public void run() {
        if (!isRunning) {
            isRunning = true;
            rootLogger.getLoggerContext().start();

            clientServer.start();

            try {
                Thread.sleep(maxRunDurationSeconds * 1000);
            } catch (InterruptedException e) {
                // Do nothing
            } finally {
                shutdown();
            }
        }
    }

    public synchronized void shutdown() {
        if (isRunning) {
            isRunning = false;
            clientServer.stop();
            validatorManager.forceEndValidation();
            rootLogger.getLoggerContext().stop();
        }
    }

    @Override
    public synchronized void finished(ValidationResults results) {
        interrupt();
    }
}
