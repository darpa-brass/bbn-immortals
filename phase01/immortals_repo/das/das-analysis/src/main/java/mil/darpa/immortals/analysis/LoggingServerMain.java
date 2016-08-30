package mil.darpa.immortals.analysis;


import io.airlift.airline.*;
import mil.darpa.immortals.analysis.analytics.Log4jAnalyticsServer;
import mil.darpa.immortals.analysis.analytics.ValidatorAppender;
import mil.darpa.immortals.analysis.validators.ClientImageProduceValidator;
import mil.darpa.immortals.analysis.validators.ClientImageShareValidator;
import mil.darpa.immortals.analysis.validators.ClientLocationShareValidator;
import mil.darpa.immortals.analysis.validators.ClientLocationUpdateValidator;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public class LoggingServerMain {

    public static Set<ValidatorAppender> validatorAppenderSet = new HashSet();

    public static boolean wait = false;

    public static void main(String[] args) {
        Cli.CliBuilder<Runnable> builder = Cli.<Runnable>builder("loggingServer")
                .withDescription("IMMoRTALS Logging and validation server")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class, Validate.class, Observe.class);

        Cli<Runnable> parser = builder.build();


        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                for (ValidatorAppender validatorAppender : validatorAppenderSet) {
                    List<String> errors = validatorAppender.getValidationErrors();

                    if (errors == null || errors.isEmpty()) {
                        System.out.println(validatorAppender.getValidatorName() + ": Pass");
                    } else {
                        System.out.println(validatorAppender.getValidatorName() + ": Fail");
                        for (String error : errors) {
                            System.out.println("\t" + error);
                        }
                    }
                }
            }
        });


        parser.parse(args).run();


        if (wait == true) {
            try {
                Thread.sleep(10000000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static abstract class LoggingServerCommand implements Runnable {

        @Option(type = OptionType.GLOBAL, name = "-c", description = "Log to the console (default=on for observe, off for validate")
        public Boolean logToConsole;

        @Option(type = OptionType.GLOBAL, name = "-f", description = "Log to a file")
        public String logToFile;

        @Option(name = {"-p", "--port"}, title = "PORT", description = "The port to run the server on (default=7707)")
        public Integer port = 7707;

    }

    @Command(name = "validate", description = "Validates the received data against the specified validators")
    public static class Validate extends LoggingServerCommand {
        @Arguments(description = "The validators to run")
        public List<String> validatorIdentifiers;

        public void run() {
            try {
                if (validatorIdentifiers == null || validatorIdentifiers.size() == 0) {
                    System.err.println("No validators specified! Valid validators:\n\tclient-location-share\n\tclient-location-produce\n\tclient-image-share\n\tclient-image-produce");
                    System.exit(0);
                }

                System.out.println("Starting validation on port " + port);

                Log4jAnalyticsServer server = new Log4jAnalyticsServer(port);

                if (logToConsole != null && logToConsole) {
                    server.initConsoleLogger();
                }

                if (logToFile != null) {
                    server.initFileLogger(logToFile);
                }


                for (String identifier : validatorIdentifiers) {
                    if (identifier.equals("client-location-share")) {
                        ClientLocationShareValidator validator = new ClientLocationShareValidator(new HashSet<>());
                        validatorAppenderSet.add(validator);
                        server.addValidator(validator);

                    } else if (identifier.equals("client-location-produce")) {
                        ClientLocationUpdateValidator validator = new ClientLocationUpdateValidator(new HashSet<>());
                        validatorAppenderSet.add(validator);
                        server.addValidator(validator);

                    } else if (identifier.equals("client-image-share")) {
                        ClientImageShareValidator validator = new ClientImageShareValidator(new HashSet<>());
                        validatorAppenderSet.add(validator);
                        server.addValidator(validator);

                    } else if (identifier.equals("client-image-produce")) {
                        ClientImageProduceValidator validator = new ClientImageProduceValidator(new HashSet<>());
                        validatorAppenderSet.add(validator);
                        server.addValidator(validator);

                    } else {
                        System.err.print("Unexpected validator identifier '" + identifier + "'! Valid validators:\n\tclient-location-share\n\tclient-location-produce\n\tclient-image-share\n\tclient-image-produce");
                    }
                }

                server.start();
                System.out.println("Validation has been started. Once the generation of data has ceased, please press Ctrl-C to validate the data received.");
                wait = true;

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
                System.out.println("Starting observation on port " + port);
                Log4jAnalyticsServer server = new Log4jAnalyticsServer(port);

                if (logToConsole == null || logToConsole == true) {
                    server.initConsoleLogger();
                }

                if (logToFile != null) {
                    server.initFileLogger(logToFile);
                }

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        server.stop();
                    }
                });

                server.start();

                wait = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
