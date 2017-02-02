package mil.darpa.immortals.analytics.protocols.log4j;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import com.google.gson.Gson;
import mil.darpa.immortals.analytics.L;
import mil.darpa.immortals.analytics.validators.ValidatorManager;
import mil.darpa.immortals.analytics.validators.Validators;
import mil.darpa.immortals.analytics.validators.result.ValidationResults;
import mil.darpa.immortals.analytics.validators.result.ValidationResultsListener;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.core.analytics.AnalyticsVerbosity;
import org.apache.log4j.Appender;
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

    private final long startTime;
    private final long maxRunDurationMS;
    private final long minRunDurationMS;

    private final Log4jClientServer clientServer;

    public Log4jValidationServer(int port, Level logLevel, long maxDurationMS, long minDurationMS, String appenderAddress, Integer appenderPort) {

        validatorManager = new ValidatorManager(this);

        Appender[] appenders;

        if (appenderAddress != null && appenderPort != null) {
            appenders = new Appender[2];
            appenders[0] = new ValidatorManagerLog4jAppender(validatorManager);
            appenders[1] = new RestfulLoggingAppender(appenderAddress, appenderPort);
        } else {
            appenders = new Appender[1];
            appenders[0] = new ValidatorManagerLog4jAppender(validatorManager);
        }

        clientServer = new Log4jClientServer(port, appenders);

        this.maxRunDurationMS = (maxDurationMS > minDurationMS ? maxDurationMS : minDurationMS);
        this.minRunDurationMS = minDurationMS;

        this.startTime = System.currentTimeMillis();

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
            identifiers = new ArrayList(Validators.getValidatorIdentifierList());
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
                Thread.sleep(maxRunDurationMS);
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

        AnalyticsEventReporter aer = AnalyticsEventReporter.getInstance();
        if (aer != null) {
            AnalyticsEvent ae = new AnalyticsEvent(
                    AnalyticsEventType.Tooling_ValidationServerStopped,
                    "JavaAnalyticsServer",
                    "JavaAnalyticsServer",
                    results,
                    System.currentTimeMillis()
            );
            AnalyticsEventReporter.getInstance().report((new Gson()).toJson(ae));
        }

        long durationMS = System.currentTimeMillis() - this.startTime;
        long timeLeft = minRunDurationMS - durationMS;

        if (timeLeft <= 0) {
            interrupt();
        } else {
            try {
                Thread.sleep(timeLeft);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            interrupt();
        }

    }
}
