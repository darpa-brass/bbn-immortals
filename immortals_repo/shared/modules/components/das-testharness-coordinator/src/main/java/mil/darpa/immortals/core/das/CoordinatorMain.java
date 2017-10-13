package mil.darpa.immortals.core.das;

import mil.darpa.immortals.core.Configuration;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class CoordinatorMain {

    public static final Logger logger = LoggerFactory.getLogger(CoordinatorMain.class);

    public static final Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("Exception!", e);
        }
    };

    private static HttpServer server;
    private boolean isRunning = false;

    private DasInterface dumbDas;

    public CoordinatorMain() {
    }

    public boolean signalReady = true;

    public synchronized void start() throws IOException {

        dumbDas = new DasInterface();

        // Start the rest server
        if (!isRunning) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    server = startServer();
                    isRunning = true;
                    try {
                        System.in.read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.shutdown();
                }
            });
            t.start();
        }

        try {
            Thread.sleep(4000);
            if (signalReady) {
                dumbDas.getMediator().signalReady();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void shutdown() {
        if (isRunning) {
            if (server != null && server.isStarted()) {
                server.shutdown();
            }
        }
    }

    private HttpServer startServer() {
        Configuration.TestAdapterConfiguration tac = Configuration.getInstance().testAdapter;
//        Configuration.DasServiceConfiguration dsc = Configuration.getInstance().dasService;

        final ResourceConfig rc = new ResourceConfig().packages("mil.darpa.immortals.core.das");

        rc.register(JacksonFeature.class);

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(tac.protocol + tac.url + ":" + tac.port), rc);
    }

    public static void main(String[] args) {
        try {
            CoordinatorMain cm = new CoordinatorMain();
            
            if (args != null && args.length == 1) {
                if (args[0].equals("--no-signal")) {
                    cm.signalReady = false;
                }
            }

            cm.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
