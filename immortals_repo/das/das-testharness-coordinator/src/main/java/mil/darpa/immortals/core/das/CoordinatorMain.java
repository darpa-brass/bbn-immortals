package mil.darpa.immortals.core.das;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.TestAdapterConfiguration;
import mil.darpa.immortals.core.das.ll.TestHarnessAdapterMediator;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import mil.darpa.immortals.testadapter.restendpoints.DasRequestListener;
import mil.darpa.immortals.testadapter.restendpoints.TestHarnessRequestListener;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.net.URI;

public class CoordinatorMain {

    public static class GenericExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Throwable> {
        @Override
        public Response toResponse(Throwable exception) {
            ImmortalsErrorHandler.reportFatalException(exception);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getMessage()).build();

        }
    }


    private static final Logger logger = LoggerFactory.getLogger(CoordinatorMain.class);

    private static HttpServer server;
    private boolean isRunning = false;

    private CoordinatorMain() {
    }

    private synchronized void start() {
        TestAdapterConfiguration tac = ImmortalsConfig.getInstance().testAdapter;
        String base_url = tac.getProtocol() + "://" + tac.getUrl() + ":" + tac.getPort() + "/";

        // Start the rest server
        if (!isRunning) {
            final ResourceConfig rc = new ResourceConfig();
            rc.registerClasses(
                    TestHarnessRequestListener.class,
                    DasRequestListener.class,
                    GsonJacksonHelper.class,
                    GenericExceptionMapper.class
            );

            server = GrizzlyHttpServerFactory.createHttpServer(URI.create(base_url), rc);
            isRunning = true;

            System.out.println("Started TestAdapter at URL '" + base_url + "'.");

            try {
                Thread.sleep(4000);
                TestHarnessAdapterMediator.getInstance().signalReady();

            } catch (InterruptedException e) {
                ImmortalsErrorHandler.reportFatalException(e);
            }
        }

    }

    private synchronized void shutdown() {
        if (isRunning) {
            if (server != null && server.isStarted()) {
                System.out.println("Shutting down TestAdapter...");
                server.shutdown();
                isRunning = false;
            }
        }
    }

    public static void main(String[] args) {
        final CoordinatorMain cm = new CoordinatorMain();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cm.shutdown();
            }
        });
        cm.start();
    }

}
