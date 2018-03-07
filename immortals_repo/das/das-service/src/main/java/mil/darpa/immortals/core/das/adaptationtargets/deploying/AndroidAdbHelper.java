package mil.darpa.immortals.core.das.adaptationtargets.deploying;

import mil.darpa.immortals.config.DeploymentEnvironmentConfiguration;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by awellman@bbn.com on 1/22/18.
 */
public class AndroidAdbHelper {

    private static final List<DeploymentEnvironmentConfiguration.AndroidEnivronmentConfiguration> availableEnvironments
            = new LinkedList<>(ImmortalsConfig.getInstance().deploymentEnvironment.getAndroidEnvironments());

    private static final String ADB_COMMAND = ImmortalsConfig.getInstance().build.augmentations.getAndroidAdbPath();

    public static synchronized AndroidAdbHelper createInstance() {
        DeploymentEnvironmentConfiguration.AndroidEnivronmentConfiguration aec = availableEnvironments.remove(0);
        return new AndroidAdbHelper(aec);
    }

    private final DeploymentEnvironmentConfiguration.AndroidEnivronmentConfiguration androidEnvironment;

    private boolean hasConnected = false;

    private synchronized void execSynchronous(@Nonnull List<String> cmd) {
        String[] cmd2 = cmd.toArray(new String[0]);
        execSynchronous(cmd2);
    }

    private synchronized void execSynchronous(@Nonnull String... cmd) {
        if (!hasConnected) {
            hasConnected = true;

            execSynchronous(
                    ADB_COMMAND,
                    "connect",
                    androidEnvironment.getAdbUrl() + ":" + androidEnvironment.getAdbPort()
            );
        }

        System.out.println("EXEC: `" + Arrays.stream(cmd).collect(Collectors.joining(" ")) + "`");
        try {
            ProcessBuilder pb = new ProcessBuilder().command(cmd).inheritIO();
            Process p = pb.start();
            p.waitFor();

            if (p.exitValue() != 0) {
                ImmortalsErrorHandler.reportFatalError("Got a non-zero exit value when executing the command '" +
                        Arrays.stream(cmd).collect(Collectors.joining(" ")) + "`" + "'!");
            }
        } catch (IOException | InterruptedException e) {
            ImmortalsErrorHandler.reportFatalException(e);
        }
    }

    private synchronized void execDeviceSynchronous(@Nonnull String... commands) {
        List<String> cmd = new ArrayList<>(Arrays.asList(
                ADB_COMMAND,
                "-s",
                androidEnvironment.getAdbIdentifier()));

        cmd.addAll(Arrays.asList(commands));
        execSynchronous(cmd);
    }

    private AndroidAdbHelper(DeploymentEnvironmentConfiguration.AndroidEnivronmentConfiguration androidEnvironment) {
        this.androidEnvironment = androidEnvironment;
    }

    public void uploadFile(String source, String target) {
        execDeviceSynchronous("shell", "mkdir -p " + Paths.get(target).getParent().toString());
        execDeviceSynchronous("push", source, target);
    }

    public void startProcess(String packageActivity) {
        execDeviceSynchronous("shell", "am", "start", "-n", packageActivity);
    }

    public void forceStopProcess(String packageActivity) {
        execDeviceSynchronous("shell", "am", "force-stop", packageActivity);
    }

    public void deployApk(String adbFilepath) {
        execDeviceSynchronous("install", "-r", adbFilepath);
    }
}
