package mil.darpa.immortals.das;

import ch.qos.logback.classic.Level;
import mil.darpa.immortals.config.AppConfigInterface;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.RestfulAppConfigInterface;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Created by awellman@bbn.com on 12/19/17.
 */
public class DasLauncher {

    private enum Verbosity {
        DEFAULT(null, null, null),
        INFO(org.slf4j.event.Level.INFO, java.util.logging.Level.INFO, org.slf4j.event.Level.INFO),
        DEBUG(org.slf4j.event.Level.DEBUG, java.util.logging.Level.FINER, org.slf4j.event.Level.DEBUG),
        ALL(org.slf4j.event.Level.TRACE, java.util.logging.Level.ALL, org.slf4j.event.Level.TRACE);

        public final org.slf4j.event.Level slf4jLoggingLevel;
        public final java.util.logging.Level javaLoggingLevel;
        public final org.slf4j.event.Level log4jLoggingLevel;

        Verbosity(@Nullable org.slf4j.event.Level slf4jLoggingLevel,
                  @Nullable java.util.logging.Level javaLoggingLevel,
                  @Nullable org.slf4j.event.Level log4jLoggingLevel) {
            this.slf4jLoggingLevel = slf4jLoggingLevel;
            this.javaLoggingLevel = javaLoggingLevel;
            this.log4jLoggingLevel = log4jLoggingLevel;
        }

        public static final String parameterOptionsString = "[DEFAULT|INFO|DEBUG|ALL]";

        public static final String optionsLoggingMappingChart =
                "| PARAMETER | LOGBACK LEVEL | LOG4J LEVEL | JAVA LEVEL |\n" +
                        "| INFO      | INFO          | INFO        | INFO       |\n" +
                        "| DEBUG     | DEBUG         | DEBUG       | FINER      |\n" +
                        "| ALL       | TRACE         | TRACE       | ALL        |";
    }

    private static final String logo =
            "==================================================\n" +
                    "    __\n" +
                    "   / /                               \n" +
                    "  / /_  _    _  _   __  __ _|_  _  | __\n" +
                    " / // \\/ \\  / \\/ \\ |  ||    |   _| ||__\n" +
                    "/_// /\\/\\ \\/ /\\/\\ \\|__||    |  |_| | __|\n" +
                    "==================================================\n";

    private final Logger logger;

    private final Verbosity verbosity;

    private final String stdTargetIdentifier;

    private final boolean deleteExistingLogs;

    private static final Long initTimestamp = System.currentTimeMillis();

    private static final long shutdownWaitDurationMS = 10000;

    private static final TreeMap<String, Process> runningProcesses = new TreeMap<>();

    private static final Set<Process> monitoredProcesses = new HashSet<>();

    private static final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    private Thread stdListenThread = null;

    private static void mergeNetworkLogs() throws IOException {
        String regexPattern = "^\\[\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d,\\d\\d\\d\\].*$";

        File logdir = ImmortalsConfig.getInstance().globals.getGlobalLogDirectory().toFile();

        Map<String, List<String>> outputLines = new TreeMap<>();

        for (File file : FileUtils.listFiles(logdir, null, false)) {
            String fileString = file.getName();

            if (fileString.startsWith("network_")) {
                List<String> fileLines = FileUtils.readLines(file, Charset.defaultCharset());

                String keyLine = null;
                List<String> statementLines = new LinkedList<>();

                for (int i = 0; i < fileLines.size(); i++) {
                    String currentLine = fileLines.get(i);
                    boolean currentLineMatches = currentLine.matches(regexPattern);

                    String nextLineValue = (i + 1) == fileLines.size() ? null : fileLines.get(i + 1);

                    // The current data should be flushed if there is no next value or the next line does not match the new line regex
                    boolean flushCurrentLines = nextLineValue == null || nextLineValue.matches(regexPattern);

                    if (keyLine == null) {
                        // If the keyLine is unset, double check it is a properly formatted key line, set it, and add it
                        if (!currentLineMatches) {
                            throw new RuntimeException("Network Log Merge Error: The statement lines are empty but the new line does not start with a timestamp!");
                        }
                        keyLine = currentLine;
                        statementLines.add(currentLine);

                    } else {
                        // Otherwise, check that it is not a properly formatted key line and add it
                        if (currentLineMatches) {
                            throw new RuntimeException("Network Log Merge Error: A new statement line is being added to an existing statement line set!");
                        }
                        statementLines.add(currentLine);
                    }

                    // If it is time to flush a complete statement to the output list, flush it
                    if (flushCurrentLines) {
                        outputLines.put(keyLine, statementLines);
                        keyLine = null;
                        statementLines = new LinkedList<>();
                    }
                }
            }
        }

        List<String> outputList = new LinkedList<>();

        for (List<String> lines : outputLines.values()) {
            outputList.addAll(lines);
        }

        File outputFile = ImmortalsConfig.getInstance().globals.getGlobalLogDirectory().resolve("network_combined.log").toFile();
        FileUtils.writeLines(outputFile, outputList);
        System.out.println("Combined network logs written to the file '" + outputFile.toString() + "'.");
    }

    private static void shutdown() {
        shuttingDown.set(true);

        synchronized (shuttingDown) {
            System.out.println("Shutting down.");

            for (Process p : runningProcesses.values()) {
                p.destroy();
            }

            long startTime = System.currentTimeMillis();
            long timeLeft = startTime + shutdownWaitDurationMS - System.currentTimeMillis();

            try {
                while (timeLeft > 0 && runningProcesses.size() > 0) {
                    Thread.sleep(1000);
                    System.out.print(".");

                    String identifier = runningProcesses.lastKey();
                    while (identifier != null) {
                        if (!runningProcesses.get(identifier).isAlive()) {
                            runningProcesses.remove(identifier);
                            System.out.println("Successfully shut down '" + identifier + "'.");
                        }
                        identifier = runningProcesses.lowerKey(identifier);
                    }
                    timeLeft = startTime + shutdownWaitDurationMS - System.currentTimeMillis();
                }
            } catch (InterruptedException e) {
                System.err.println("Shutdown interrupted! Forcing shutdown!");
            }

            for (Map.Entry<String, Process> p : runningProcesses.entrySet()) {
                System.err.println("Forcing shutdown of '" + p.getKey() + "'!");
                p.getValue().destroyForcibly();
            }

            try {
                if (ImmortalsConfig.getInstance().debug.isLogNetworkActivityToSeparateFile()) {
                    mergeNetworkLogs();
                }
            } catch (Exception e) {
                System.err.println("Could not merge network logs into a single unified file!");
            }
        }
    }

    private DasLauncher(@Nonnull Verbosity verbosity, @Nullable ImmortalsServiceManifest stdTarget, boolean deleteExistingLogs) {
        this.logger = LoggerFactory.getLogger("mil.darpa.immortals.DasLauncher");
        this.verbosity = verbosity;
        this.stdTargetIdentifier = stdTarget == null ? null : stdTarget.getConfig().getIdentifier();
        this.deleteExistingLogs = deleteExistingLogs;
    }

    private void startDasComponents() throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Loaded the following ImmortalsConfig:\n" + ImmortalsConfig.getInstanceAsJsonString());
        }

        long startTime = System.currentTimeMillis();
        
        List<String> probableErrors = new LinkedList<>();

        for (ImmortalsServiceManifest service : ImmortalsServiceManifest.values()) {
            synchronized (shuttingDown) {
                if (!shuttingDown.get()) {
                    if (service.getConfig() instanceof AppConfigInterface) {
                        AppConfigInterface cfg = (AppConfigInterface) service.getConfig();

                        if (service.useMock()) {
                            System.out.println("Skipping startup of '" + service.name() + "' since it is flagged for mocking in the debug configuration.");


                        } else if (cfg.isUserManaged()) {
                            System.out.println("Please start '" + service.getConfig().getIdentifier() + "' and press any key to continue.");
                            System.in.read();

                        } else {
                            String probableErrorMessage = startService(cfg);
                            if (probableErrorMessage != null) {
                                probableErrors.add(probableErrorMessage);
                            }
                        }
                    }
                }
            }
        }

        if (!shuttingDown.get()) {
            String duration = Long.toString(System.currentTimeMillis() - startTime);
            if (probableErrors.isEmpty()) {
                System.out.println("The DAS has finished starting up (" + duration + "ms). Press Ctrl-C to terminate (this may take up to ten seconds)");
            } else {
                System.out.println("The DAS has finished starting up (" + duration + "ms) WITH SERVICE STARTUP TIMEOUTS and likely will not work properly! Press Ctrl-C to terminate (this may take up to ten seconds)");
            }
        }
    }

    private void archiveFile(@Nonnull Path filepath) throws IOException {
        // Try and get the file creation time, falling back on something slightly before this run's creation time
        long creationTime = -1;

        try {
            BasicFileAttributes attrs = Files.readAttributes(filepath, BasicFileAttributes.class);
            creationTime = attrs.creationTime().toMillis();

        } catch (Exception e) {
            // Do nothing
        } finally {
            if (creationTime <= 0) {
                creationTime = initTimestamp - 60000;
            }
        }

        Path targetPath;
        if (deleteExistingLogs) {
            Files.delete(filepath);
        } else {
            Path logArchivePath = filepath.toAbsolutePath().getParent().resolve("PREVIOUS");
            if (!Files.exists(logArchivePath)) {
                Files.createDirectory(logArchivePath);
            }
            targetPath = logArchivePath.resolve(filepath.getFileName().toString() + "-" + Long.toString(creationTime));
            Files.move(filepath, targetPath);
        }
    }

    private ProcessBuilder prepareProcess(AppConfigInterface appConfig) throws IOException {

        Path templateFolder = appConfig.getWorkingDirectoryTemplateFolder();
        if (templateFolder != null) {
            FileUtils.copyDirectory(templateFolder.toFile(), new File(appConfig.getWorkingDirectory()));
        }

        ProcessBuilder pb = new ProcessBuilder();

        pb.environment().putAll(appConfig.getEnvironmentVariables());
        pb.directory(new File(appConfig.getWorkingDirectory()));

        File stdoutFile = ImmortalsConfig.getInstance().globals.getGlobalLogDirectory().resolve(appConfig.getIdentifier() + "_stdout.log").toFile();

        // If the file exists from a previous run
        if (stdoutFile.exists()) {
            archiveFile(stdoutFile.toPath());
        }

        File stderrFile = ImmortalsConfig.getInstance().globals.getGlobalLogDirectory().resolve(appConfig.getIdentifier() + "_stderr.log").toFile();
        if (stderrFile.exists()) {
            archiveFile(stderrFile.toPath());
        }

//        if (service == stdTarget) {
//            pb.inheritIO();
//
//        } else {
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(stdoutFile));

        pb.redirectError(ProcessBuilder.Redirect.appendTo(stderrFile));
//        }

        String[] command;
        if (appConfig.getExePath().endsWith(".jar") || appConfig.getExePath().endsWith(".war")) {
            int len = appConfig.getInterpreterParameters().length + appConfig.getParameters().length + 2;
            ArrayList<String> commandList = new ArrayList<>(len);
            commandList.add(Paths.get(System.getenv("JAVA_HOME")).resolve("bin/java").toAbsolutePath().toString());

            if (verbosity != Verbosity.DEFAULT) {
                commandList.add("-Dlogback.configurationFile=" +
                        Paths.get(ImmortalsConfig.getInstance().debug.loggingConfigDirectory).resolve("logback-" + verbosity.slf4jLoggingLevel.name() + ".xml"));
                commandList.add("-Djava.util.logging.config.file=" +
                        Paths.get(ImmortalsConfig.getInstance().debug.loggingConfigDirectory).resolve("java-" + verbosity.javaLoggingLevel.getName() + ".properties"));
                commandList.add("-Dlog4j.configurationFile=" +
                        Paths.get(ImmortalsConfig.getInstance().debug.loggingConfigDirectory).resolve("log4j-" + verbosity.log4jLoggingLevel.name() + ".xml"));
            }

            commandList.addAll(Arrays.asList(appConfig.getInterpreterParameters()));
            commandList.add("-jar");
            commandList.add(appConfig.getExePath());
            commandList.addAll(Arrays.asList(appConfig.getParameters()));
            command = commandList.toArray(new String[len]);

        } else if (appConfig.getExePath().endsWith(".sh") || appConfig.getExePath().endsWith(".bash")) {
            int len = appConfig.getInterpreterParameters().length + appConfig.getParameters().length + 2;
            ArrayList<String> commandList = new ArrayList<>(len);
            commandList.add("bash");
            commandList.addAll(Arrays.asList(appConfig.getInterpreterParameters()));
            commandList.add(appConfig.getExePath());
            commandList.addAll(Arrays.asList(appConfig.getParameters()));
            command = commandList.toArray(new String[len]);

        } else if (appConfig.getExePath().endsWith("build.xml")) {
            int len = appConfig.getInterpreterParameters().length + appConfig.getParameters().length + 2;
            ArrayList<String> commandList = new ArrayList<>(len);
            commandList.add("ant");
            commandList.addAll(Arrays.asList(appConfig.getInterpreterParameters()));
            commandList.add(appConfig.getExePath());
            commandList.addAll(Arrays.asList(appConfig.getParameters()));
            command = commandList.toArray(new String[len]);


        } else {
            command = Stream.concat(
                    Arrays.stream(new String[]{appConfig.getExePath()}),
                    Arrays.stream(appConfig.getParameters())).toArray(String[]::new);
        }

        pb.command(command);

        return pb;
    }

    private String startService(@Nonnull AppConfigInterface appConfig) throws IOException {
        String probableErrorMessage = null;
        Process p;
        BufferedReader reader = null;

        try {
            // Create a process builder instance all set up
            ProcessBuilder pb = prepareProcess(appConfig);

            // Get the stdout file for monitoring
            File stdoutFile = pb.redirectOutput().file();

            if (logger.isDebugEnabled()) {
                logger.debug("EXEC: [" + String.join(" ", pb.command()) + "]");
            }

            // Start the process
            p = pb.start();

            // Print the most verbose compact start statement possible
            if (appConfig instanceof RestfulAppConfigInterface) {
                RestfulAppConfigInterface raci = (RestfulAppConfigInterface) appConfig;
                System.out.print("Starting '" + raci.getIdentifier() + "' at '" + raci.getFullUrl() + "'");
            } else {
                System.out.print("Starting '" + appConfig.getIdentifier() + "'");
            }

            long timeoutMS = appConfig.getStartupTimeMS();

            String regexMatch = appConfig.getReadyStdoutLineRegexPattern();

            // Wait until you see the ready signal in the log
            reader = new BufferedReader(new FileReader(stdoutFile));
            long durationMS = 0;
            boolean found = false;
            boolean isShuttingDown = false;
            while (!found && durationMS < timeoutMS && !isShuttingDown) {
                String line = reader.readLine();

                if (line == null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // Shouldn't be possible
                    }
                    durationMS += 200;
                    if (!appConfig.getIdentifier().equals(stdTargetIdentifier)) {
                        if (durationMS % 1600 == 0) {
                            System.out.print(".");
                        }
                    }
                } else {
                    if (appConfig.getIdentifier().equals(stdTargetIdentifier)) {
                        System.out.println(line);
                    }
                    if (line.matches(regexMatch)) {
                        found = true;
                    }
                }
                isShuttingDown = shuttingDown.get();
            }

            if (durationMS >= timeoutMS) {
                System.err.println("TIMEOUT!");
                probableErrorMessage = "PROBABLE ERROR! Startup of '" + appConfig.getIdentifier() + "' hit its timeout of "
                        + Long.toString(appConfig.getStartupTimeMS()) + " ms!";
                System.err.println(probableErrorMessage);

            } else if (isShuttingDown) {
                System.err.println("INTERRUPTED!");
                System.err.println("Start up of process '" + appConfig.getIdentifier() + "' Interrupted!");

            } else if (p.isAlive()) {
                runningProcesses.put(appConfig.getIdentifier(), p);
                if (appConfig.isShutdownEverythingOnTermination()) {
                    monitoredProcesses.add(p);
                }
                System.out.println(" Done (" + Long.toString(durationMS) + "ms)");

            } else {
                System.out.println();
                System.err.println("ERROR: Process '" + appConfig.getIdentifier() + "' terminated immediately after startup!");
                runningProcesses.put(appConfig.getIdentifier(), p);
                shutdown();
            }
        } finally {
            if (appConfig.getIdentifier().equals(stdTargetIdentifier)) {
                BufferedReader r = reader;
                stdListenThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String line;
                        try {
                            line = r.readLine();

                            if (line != null) {
                                System.out.println(line);
                            }

                        } catch (IOException e) {
                            // TODO: Something else!
                            throw new RuntimeException(e);
                        }
                    }
                });

                stdListenThread.setDaemon(true);
                stdListenThread.start();

            } else {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            }
        }
        return probableErrorMessage;
    }


    private static class CliParser implements Callable<Void> {
        @CommandLine.Option(names = {"-v", "--verbosity"}, type = Verbosity.class, paramLabel = Verbosity.parameterOptionsString,
                description = "Specifies the verbosity to pass through to children java processes through JVM parameters " +
                        "specifying logback.xml and logging.properties files to use. Verbosity Mapping:\n" + Verbosity.optionsLoggingMappingChart)
        Verbosity verbosity = Verbosity.INFO;

        @CommandLine.Option(names = {"-s", "--std-target"}, type = ImmortalsServiceManifest.class, paramLabel = ImmortalsServiceManifest.displayString,
                description = "Specifies to route a child service's stdin/stderr to the launcher instead of a file. After " +
                        "startup, the only time the launcher will print to stderr/stdin is when an unexpected error or shutdown occurs.")
        ImmortalsServiceManifest stdTarget = null;

        @CommandLine.Option(names = {"-d", "--delete-existing-logs"}, description = "If provided, logs will be sent to /dev/null instead of a sister 'PREVIOUS' directory.")
        private boolean deleteExistingLogs = false;

        @Override
        public Void call() {
            System.out.println(logo);
            System.out.println();

            if (verbosity != Verbosity.DEFAULT) {
                System.out.println("Using verbosity level '" + verbosity.name() +
                        "' (slf4j='" + verbosity.slf4jLoggingLevel.name() +
                        "', log4j='" + verbosity.log4jLoggingLevel.name() +
                        "', java.logging='" + verbosity.javaLoggingLevel.getName() + "').");
            }

            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            root.setLevel(Level.valueOf(verbosity.name()));
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    shutdown();
                }
            }));

            try {
                DasLauncher dl = new DasLauncher(verbosity, stdTarget, deleteExistingLogs);
                dl.startDasComponents();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                shutdown();
            }

            try {
                while (!shuttingDown.get()) {
                    for (Process p : monitoredProcesses) {
                        if (!p.isAlive()) {
                            shutdown();
                        }
                    }
                    Thread.sleep(shutdownWaitDurationMS + 1000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

    }

    public static void main(String[] argsv) throws Exception {
        CommandLine.call(new CliParser(), System.err, argsv);
    }

}
