package mil.darpa.immortals;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.config.ImmortalsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 1/11/18.
 */
public class ImmortalsUtils {
    // GSON is thread-safe and efficient enough for our purposes so I'm putting a globally usable one here
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final HashMap<String, NetworkLogger> loggerMap = new HashMap<>();

    public static synchronized NetworkLogger getNetworkLogger(@Nonnull String localIdentifier, @Nullable String remoteIdentifier) {
        String identifier = localIdentifier + (remoteIdentifier == null ? "" : "-" + remoteIdentifier);
        NetworkLogger logger = loggerMap.get(identifier);
        if (logger == null) {
            logger = new NetworkLogger(localIdentifier, remoteIdentifier);
            loggerMap.put(identifier, logger);
        }
        return logger;
    }

    public static class NetworkLogger {

        private final Logger logger;
        private final String sendingTemplate;
        private final String receivedAckToSendTemplate;
        private final String receivedTemplate;
        private final String sendingAckToReceivedTemplate;


        public NetworkLogger(@Nonnull String localIdentifier, @Nullable String remoteIdentifier) {
            sendingTemplate = localIdentifier + " sending %s %s to " + remoteIdentifier + " with %s";
            receivedAckToSendTemplate = localIdentifier + " received ACK %s from sent %s %s to " + remoteIdentifier + " with %s";
            receivedTemplate = localIdentifier + " received %s %s with %s";
            sendingAckToReceivedTemplate = localIdentifier + " sending ACK to received %s %s with %s";

            if (ImmortalsConfig.getInstance().debug.isLogNetworkActivityToSeparateFile()) {
                String loggerIdentifier =
                        "network_" + localIdentifier + (remoteIdentifier == null ? "" : "-" + remoteIdentifier);

                LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
                PatternLayoutEncoder ple = new PatternLayoutEncoder();
                ple.setPattern("[%date] %msg%n");
                ple.setContext(lc);
                ple.start();
                FileAppender<ILoggingEvent> fa = new FileAppender<>();
                fa.setFile(ImmortalsConfig.getInstance().globals.getGlobalLogDirectory().resolve(
                        loggerIdentifier + ".log").toAbsolutePath().toString());
                fa.setEncoder(ple);
                fa.setContext(lc);
                fa.start();

                ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerIdentifier);
                logger.addAppender(fa);
                logger.setLevel(Level.ALL);
                logger.setAdditive(false);
                this.logger = logger;

            } else {
                logger = LoggerFactory.getLogger(NetworkLogger.class);
            }
        }

        private String getBody(@Nullable Object body) {
            return (body == null ? "no BODY" : (logger.isTraceEnabled() ? ("BODY:\n" + gson.toJson(body, body.getClass())) : ("BODY")));
        }

        public synchronized void logPostSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingTemplate, "POST", path, getBody(body)));
        }

        public synchronized void logPostSendingAckReceived(@Nonnull String path, int status, @Nullable Object body) {
            logger.debug(String.format(receivedAckToSendTemplate, Integer.toString(status), "POST", path, getBody(body)));

        }

        public synchronized void logPostReceived(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(receivedTemplate, "POST", path, getBody(body)));
        }

        public synchronized void logPostReceivedAckSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingAckToReceivedTemplate, "POST", path, getBody(body)));
        }

        public synchronized void logGetSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingTemplate, "GET", path, getBody(body)));
        }

        public synchronized void logGetSendingAckReceived(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(receivedAckToSendTemplate, "GET", path, getBody(body)));

        }

        public synchronized void logGetReceived(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(receivedTemplate, "GET", path, getBody(body)));
        }

        public synchronized void logGetReceivedAckSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingAckToReceivedTemplate, "GET", path, getBody(body)));
        }
    }

    public static Gson getGson() {
        return gson;
    }
}
