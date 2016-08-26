package mil.darpa.immortals.analysis;


import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;
import mil.darpa.immortals.analysis.analytics.Log4jAnalyticsServer;

import javax.inject.Inject;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
@Command(name = "LoggingServer", description = "Starts up a log4j logging server")
public class LoggingServerMain {

    @Inject
    public HelpOption helpOption;

    @Option(name = {"-p", "--port"}, title = "PORT", description = "The port to run the server on (default=7707)")
    public Integer port = 7707;

    @Option(name = {"-t", "--target-file"}, title = "TARGET_FILE", description = "The target file to log the data to. If not specified, data will be logged to stdout.")
    public String logFile;

    public static void main(String[] args) {
        LoggingServerMain loggingServerMain = SingleCommand.singleCommand(LoggingServerMain.class).parse(args);

        if (loggingServerMain.helpOption.showHelpIfRequested()) {
            return;
        }

        loggingServerMain.run();
    }

    public void run() {
        Log4jAnalyticsServer server = new Log4jAnalyticsServer(port, logFile);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                server.stop();
            }
        });

        server.start();
    }
}
