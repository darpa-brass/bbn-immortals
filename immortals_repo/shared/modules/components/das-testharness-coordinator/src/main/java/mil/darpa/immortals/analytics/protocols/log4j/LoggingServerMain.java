package mil.darpa.immortals.analytics.protocols.log4j;


import io.airlift.airline.Cli;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import mil.darpa.immortals.analytics.AnalyticsHost;
import mil.darpa.immortals.analytics.ScenarioConductorHost;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsVerbosity;
import mil.darpa.immortals.core.das.CoordinatorMain;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Handler;
import java.util.logging.LogManager;

/**
 * Log4j based logging server main method.
 * <p>
 * Created by awellman@bbn.com on 8/2/16.
 */
public class LoggingServerMain {

    public static void main(String[] args) {
        Cli.CliBuilder<Runnable> builder = Cli.<Runnable>builder("loggingServer")
                .withDescription("IMMoRTALS Logging and validation server")
                .withDefaultCommand(Validate.class);

        Cli<Runnable> parser = builder.build();
        parser.parse(args).run();
    }

    private static abstract class LoggingServerCommand implements Runnable {

        @Option(type = OptionType.GLOBAL, name = "-f", description = "Log to a file (disabled by default)")
        String logToFile;

        @Option(type = OptionType.GLOBAL, name= {"-l", "--log4j-port"}, title="LOG4J_PORT", description = "The port to run the log4j server on (7707 by default")
        int log4j_port = 7707;

        @Option(type = OptionType.GLOBAL, name = {"-p", "--websocket-port"}, title = "PORT", description = "The port to run the websocket server on (9696 by default)")
        int ws_port = 9696;

        @Option(type = OptionType.GLOBAL, name = "--auxillary-logging-port", description = "If set, logs will also be POSTed to the specified port on the localhost (disabled by default)")
        Integer auxillaryLoggingPort = null;

        @Option(type = OptionType.GLOBAL, name = "--auxillary-logging-address", description = "If set along with the auxillary logging port, will log to the specified address at the specified port ((127.0.0.1 by default)")
        String auxillaryLoggingAddress  = "127.0.0.1";
    }

    @Command(name = "validate", description = "Validates the received data against the specified validators")
    public static class Validate extends LoggingServerCommand {

        private Log4jClientServer clientServer;

        public void run() {
            try {
                java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
                rootLogger.setLevel(java.util.logging.Level.ALL);
                for (Handler h : rootLogger.getHandlers()) {
                    h.setLevel(java.util.logging.Level.ALL);
                }

                // Initialize the logging endpoint
                final AnalyticsLogbackEndpoint endpoint = new AnalyticsLogbackEndpoint();

                // Enable console logging on the endpoint
                endpoint.initConsoleLogger();

                // Enable file logging on the endpoint if a file location was provided
                if (logToFile != null) {
                    endpoint.initFileLogger(logToFile);
                }

                // Enable remote logging on the endpoint if remote details were provided to communicate with the test adapter
                if (auxillaryLoggingPort != null) {
                    endpoint.initRestLogger(auxillaryLoggingAddress, auxillaryLoggingPort);
                }


                // Initialize the local Analytics instance to communicate with the logging endpoint
                Analytics.initializeEndpoint(endpoint);
                Analytics.setSourceIdentifier("Log4jAnalyticsServer");
                Analytics.setVerbosity(AnalyticsVerbosity.Data);

                // Start the websocket interface
                AnalyticsHost wah = new AnalyticsHost(ws_port);
                ScenarioConductorHost sch = new ScenarioConductorHost(ws_port);

                // Initialize the network client event collector
                clientServer = new Log4jClientServer(log4j_port, wah, endpoint);

                // Declare the current thread
                final Thread current = Thread.currentThread();

                // Add a shutdown hook so if shutdown is triggered early logs are collected
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        current.interrupt();
                    }
                });

                // Start the client server
                clientServer.start();

                // Start the websocket endpoints
                wah.start();
                sch.start();

                // Start the REST server
                CoordinatorMain dl = new CoordinatorMain();
                dl.start();

                System.out.println("LoggingServer has finished starting and is ready to take websocket connections at ws://localhost:" + ws_port);

            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
