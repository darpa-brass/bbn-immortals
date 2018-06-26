package mil.darpa.immortals.core.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.zeromq.ZMQ;

import javax.annotation.Nonnull;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public class AnalyticsMQEndpoint implements AnalyticsEndpointInterface {

    private static final Gson gson = new GsonBuilder().create();

    private ZMQ.Context context;
    private ZMQ.Socket requester;

    private final String host;
    private final int port;
    private boolean connected = false;
    private final LinkedList<String> messageQueue = new LinkedList<>();

    public AnalyticsMQEndpoint(@Nonnull String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void log(final AnalyticsEvent analyticsEvent) {
        synchronized (messageQueue) {
            String sendVal = gson.toJson(analyticsEvent);
            if (connected) {
                send(sendVal);
            } else {
                messageQueue.add(sendVal);
            }
        }
    }

    private void send(final String event) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("IMMORTALS-SENDING: " + event);
                requester.send(event.getBytes(), 0);
                requester.recv(0);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void start() {
        synchronized (messageQueue) {
            context = ZMQ.context(1);
            requester = context.socket(ZMQ.REQ);
            String target = "tcp://" + host + ":" + Integer.toString(port);
            System.out.println("Connecting to ZERMQ endpoint '" + target + "'.");
            requester.connect(target);
            System.out.println("Connected to ZERMQ endpoint '" + target + "'.");
            connected = true;

            if (messageQueue.size() > 0) {
                for (String msg : messageQueue) {
                    send(msg);
                }
                messageQueue.clear();
            }
        }
    }

    @Override
    public void shutdown() {
        requester.close();
        context.term();
    }
}
