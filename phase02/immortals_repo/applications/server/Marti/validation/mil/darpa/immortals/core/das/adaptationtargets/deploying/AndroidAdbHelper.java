package mil.darpa.immortals.core.das.adaptationtargets.deploying;

import mil.darpa.immortals.config.DeploymentEnvironmentConfiguration;
import mil.darpa.immortals.config.ImmortalsConfig;

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

    private synchronized void execSynchronous(boolean allowFailure, @Nonnull List<String> cmd) {
        String[] cmd2 = cmd.toArray(new String[0]);
        execSynchronous(allowFailure, cmd2);
    }

    private synchronized void execSynchronous(boolean allowFailure, @Nonnull String... cmd) {
        if (!hasConnected) {
            hasConnected = true;

            if (androidEnvironment.getAdbUrl() != null) {
                execSynchronous(
                        allowFailure,
                        ADB_COMMAND,
                        "connect",
                        androidEnvironment.getAdbUrl() + ":" + androidEnvironment.getAdbPort()
                );
            }
        }

        System.out.println("EXEC: `" + Arrays.stream(cmd).collect(Collectors.joining(" ")) + "`");
        try {
            ProcessBuilder pb = new ProcessBuilder().command(cmd).inheritIO();
            Process p = pb.start();
            p.waitFor();

            if (!allowFailure && p.exitValue() != 0) {
                throw new RuntimeException("Got a non-zero exit value when executing the command '" +
                        Arrays.stream(cmd).collect(Collectors.joining(" ")) + "`" + "'!");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void execDeviceSynchronous(boolean allowFailure, @Nonnull String... commands) {
        List<String> cmd = new ArrayList<>(Arrays.asList(
                ADB_COMMAND,
                "-s",
                androidEnvironment.getAdbIdentifier()));

        cmd.addAll(Arrays.asList(commands));
        execSynchronous(allowFailure, cmd);
    }

    private AndroidAdbHelper(DeploymentEnvironmentConfiguration.AndroidEnivronmentConfiguration androidEnvironment) {
        this.androidEnvironment = androidEnvironment;
    }

    public String getEmulatorIdentifier() {
        return androidEnvironment.getAdbIdentifier();
    }

    public void uploadFile(String source, String target) {
        execDeviceSynchronous(false, "shell", "mkdir -p " + Paths.get(target).getParent().toString());
        execDeviceSynchronous(false, "push", source, target);
    }

    public void startProcess(String packageActivity) {
        execDeviceSynchronous(false, "shell", "am", "start", "-n", packageActivity);
    }

    public void forceStopProcess(String packageActivity) {
        execDeviceSynchronous(false, "shell", "am", "force-stop", packageActivity);
    }
    
    public void clean() {
        execDeviceSynchronous(true, "uninstall", "com.bbn.ataklite");
        execDeviceSynchronous(true, "shell", "rm", "-r", "/sdcard/ataklite");
    }

    public void deployApk(String adbFilepath) {
        execDeviceSynchronous(false, "install", "-r", adbFilepath);
    }
}
