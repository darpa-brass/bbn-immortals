package mil.darpa.immortals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 1/11/18.
 */
public class ImmortalsUtils {
    // GSON is thread-safe and efficient enough for our purposes so I'm putting a globally usable one here
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static class NetworkLogger {
        
        private static final Logger logger = LoggerFactory.getLogger(NetworkLogger.class);

        private final String sendingTemplate;
        private final String receivedAckToSendTemplate;
        private final String receivedTemplate;
        private final String sendingAckToReceivedTemplate;


        public NetworkLogger(@Nonnull String localIdentifier, @Nullable String remoteIdentifier) {
            sendingTemplate = localIdentifier + " sending %s %s to " + remoteIdentifier + " with %s";
            receivedAckToSendTemplate = localIdentifier + " received ACK from sent %s %s to " + remoteIdentifier + " with %s";
            receivedTemplate = localIdentifier + " received %s %s with %s";
            sendingAckToReceivedTemplate = localIdentifier + " sending ACK to received %s %s with %s";

        }

        private String getBody(@Nullable Object body) {
            return (body == null ? "no BODY" : (logger.isTraceEnabled() ? ("BODY:\n" + gson.toJson(body)) : ("BODY")));
        }

        public void logPostSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingTemplate, "POST", path, getBody(body)));
        }

        public void logPostSendingAckReceived(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(receivedAckToSendTemplate, "POST", path, getBody(body)));

        }

        public void logPostReceived(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(receivedTemplate, "POST", path, getBody(body)));
        }

        public void logPostReceivedAckSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingAckToReceivedTemplate, "POST", path, getBody(body)));
        }

        public void logGetSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingTemplate, "GET", path, getBody(body)));
        }

        public void logGetSendingAckReceived(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(receivedAckToSendTemplate, "GET", path, getBody(body)));

        }

        public void logGetReceived(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(receivedTemplate, "GET", path, getBody(body)));
        }

        public void logGetReceivedAckSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingAckToReceivedTemplate, "GET", path, getBody(body)));
        }
    }


    public static Gson getGson() {
        return gson;
    }

}
