package mil.darpa.immortals.analytics.protocols.log4j;


import io.airlift.airline.*;
import mil.darpa.immortals.analytics.validators.ValidatorManager;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsStdoutEndpoint;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Log4j based logging server main method.
 * <p>
 * Created by awellman@bbn.com on 8/2/16.
 */
public class LoggingServerMain {

    public static void main(String[] args) {
        Cli.CliBuilder<Runnable> builder = Cli.<Runnable>builder("loggingServer")
                .withDescription("IMMoRTALS Logging and validation server")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class, Validate.class, Observe.class);

        Cli<Runnable> parser = builder.build();
        parser.parse(args).run();
    }

//    private static void printResults(ValidationResults results) {
//        Gson gson = new GsonBuilder().create();
//        L.validationEnded(results);
//    }

    private static abstract class LoggingServerCommand implements Runnable {

        @Option(type = OptionType.GLOBAL, name = "-c", description = "Log to the console (enabled by default)")
        boolean logToConsole = false;

        @Option(type = OptionType.GLOBAL, name = "-f", description = "Log to a file (disabled by default)")
        String logToFile;

        @Option(type = OptionType.GLOBAL, name = "-m", description = "Log minimally (does not log client activity (disabled by default)")
        boolean logMinimal = false;

        @Option(type = OptionType.GLOBAL, name = "-t", description = "How many seconds until the server stops the validation or observation (600 by default)")
        int validationTimeout = 600;

        @Option(name = {"-p", "--port"}, title = "PORT", description = "The port to run the server on (7707 by default)")
        int port = 7707;

    }

    @Command(name = "validate", description = "Validates the received data against the specified validators")
    public static class Validate extends LoggingServerCommand {

        @Option(type= OptionType.COMMAND, name = "-i", description = "The identifier of an expected client")
        private HashSet<String> clientIdentifier = new HashSet<>();

        @Arguments(description = "The validators to run")
        private LinkedList<String> validatorIdentifiers = new LinkedList<>();
        private Log4jValidationServer server;

        public void run() {
            Analytics.initializeEndpoint(new AnalyticsStdoutEndpoint());
            Analytics.setSourceIdentifier(LoggingServerMain.class.getName());

            try {
                if (validatorIdentifiers.size() == 0) {
                    String errorMessage = "\"No validators specified! Valid validators:";
                    for (String validatorIdentifier : ValidatorManager.VALIDATOR_IDENTIFIERS) {
                        errorMessage += ("\n\t" + validatorIdentifier);
                    }
                    errorMessage += ("\n\tall");
                    throw new RuntimeException(errorMessage);
                }

                server = new Log4jValidationServer(port, (logMinimal ? Level.INFO : Level.DEBUG), validationTimeout);

                if (logToConsole) {
                    server.initConsoleLogger();
                }

                if (logToFile != null) {
                    server.initFileLogger(logToFile);
                }

                server.addValidators(clientIdentifier, validatorIdentifiers.toArray(new String[0]));

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        server.interrupt();
                        server.shutdown();
                    }
                });

                server.start();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Command(name = "observe", description = "Prints the received data to the command line")
    public static class Observe extends LoggingServerCommand {

        @Override
        public void run() {
            try {
                Log4jValidationServer server = new Log4jValidationServer(port, (logMinimal ? Level.INFO : Level.DEBUG), validationTimeout);

                if (logToConsole) {
                    server.initConsoleLogger();
                }

                if (logToFile != null) {
                    server.initFileLogger(logToFile);
                }

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        server.interrupt();
                    }
                });

                server.run();

//                try {
//                    Thread.sleep(validationTimeout * 1000);
//                } catch (InterruptedException e) {
//                     Pass
//                } finally {
//                    server.stop();
//                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
