package mil.darpa.immortals.das;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A helper class for starting subprocesses. Adds the following:
 * - Sets stdout/stderr redirection
 * - Sets working directory
 * - Gently (And after a delay forcefully) kills processes when a fatal error has occurred through {@link ImmortalsErrorHandler}
 * <p>
 * Created by awellman@bbn.com on 1/30/18.
 */
public class ImmortalsProcessBuilder {

    private final Logger logger = LoggerFactory.getLogger(ImmortalsProcessBuilder.class);

    private static List<Process> runningProcesses = new LinkedList<>();

    private static synchronized void addProcess(Process newProcess) {
        // Try to do some cleanup
        for (Process runningProcess : new ArrayList<>(runningProcesses)) {
            if (!runningProcess.isAlive()) {
                runningProcesses.remove(runningProcess);
            }
        }
        // Then add the new process
        runningProcesses.add(newProcess);
    }

    public static synchronized void shutdownAllProcesses() {
        // Try to kill them nicely
        for (Process p : runningProcesses) {
            p.destroy();
        }

        long startTime = System.currentTimeMillis();
        long graceTime = ImmortalsConfig.getInstance().debug.getShutdownGracePeriodMS();

        // Poll them to see if they are shut down until the grace period has ended
        while (runningProcesses.size() > 0 && (startTime + graceTime) < System.currentTimeMillis()) {
            for (Process p : new ArrayList<>(runningProcesses)) {
                if (!p.isAlive()) {
                    runningProcesses.remove(p);
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Then kill any stragglers forcefully
        for (Process p : runningProcesses) {
            p.destroyForcibly();
        }
    }

    private final ProcessBuilder pb;

    private final File workingDirectory;


    public ImmortalsProcessBuilder(@Nonnull String adaptationIdentifier,
                                   @Nonnull String extensionIdentifier) {
        this(adaptationIdentifier, extensionIdentifier, null);
    }

    public ImmortalsProcessBuilder(@Nonnull String adaptationIdentifier,
                                   @Nonnull String extensionIdentifier,
                                   @Nullable String processIdentifier) {
        pb = new ProcessBuilder();
        workingDirectory = ImmortalsConfig.getInstance().globals
                .getAdaptationComponentWorkingDirectory(adaptationIdentifier, extensionIdentifier).toAbsolutePath().toFile();

        Path basePath = ImmortalsConfig.getInstance().globals.getAdaptationLogDirectory(adaptationIdentifier);
        File stdout = basePath.resolve(extensionIdentifier + (processIdentifier == null ? "" : processIdentifier) + "_stdout.log").toFile();
        File stderr = basePath.resolve(extensionIdentifier + (processIdentifier == null ? "" : processIdentifier) + "_stderr.log").toFile();

        pb.directory(workingDirectory);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(stdout));
        pb.redirectError(ProcessBuilder.Redirect.appendTo(stderr));
    }

    public File directory() {
        return workingDirectory;
    }

    public synchronized Process start() throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("EXEC: `" + pb.command().stream().collect(Collectors.joining(" ")) + "`");
        }

        Process p = pb.start();
        addProcess(p);
        return p;
    }

    public synchronized ImmortalsProcessBuilder command(List<String> command) {
        pb.command(command);
        return this;
    }

    public synchronized ImmortalsProcessBuilder command(String... command) {
        pb.command(command);
        return this;
    }

    public synchronized Map<String, String> environment() {
        return pb.environment();
    }
}
