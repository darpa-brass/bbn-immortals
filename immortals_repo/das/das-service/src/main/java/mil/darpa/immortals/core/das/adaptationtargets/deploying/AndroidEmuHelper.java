package mil.darpa.immortals.core.das.adaptationtargets.deploying;

import mil.darpa.immortals.config.ImmortalsConfig;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by awellman@bbn.com on 1/22/18.
 */
public class AndroidEmuHelper {

    private static final Logger logger = LoggerFactory.getLogger(AndroidEmuHelper.class);

    private static final String AVDMANAGER_COMMAND =
            ImmortalsConfig.getInstance().build.augmentations.getAndroidAvdmanagerPath();
    private static final String EMULATOR_COMMAND =
            ImmortalsConfig.getInstance().build.augmentations.getAndroidEmulatorCommand();

    private static final String ADB_COMMAND = ImmortalsConfig.getInstance().build.augmentations.getAndroidAdbPath();

    public AndroidEmuHelper() {
    }


    public static boolean emulatorExists(@Nonnull String emulatorIdentifier) {
        List<String> cmd = new ArrayList<>(Arrays.asList(
                AVDMANAGER_COMMAND,
                "list",
                "avd"
        ));
        String out = execSynchronousGetOutput(cmd);
        return (out.contains("Name: " + emulatorIdentifier));
    }

    public static int createEmulator(int sdkLevel, @Nonnull String instanceIdentifier) {
        if (emulatorExists(instanceIdentifier)) {
            return 0;
        }
        List<String> cmd = Arrays.asList(
                AVDMANAGER_COMMAND,
                "create",
                "avd",
                "--device",
                "pixel",
                "--sdcard",
                "512M",
                "--name",
                instanceIdentifier,
                "--package",
                "system-images;android-" + sdkLevel + ";default;x86_64"
        );
        return execSynchronous(cmd);
    }

    private static List<String> startEmulatorCmd(@Nonnull String instanceIdentifier, int consolePort) {
        List<String> cmd = new ArrayList<>(Arrays.asList(
                EMULATOR_COMMAND,
                "-avd",
                instanceIdentifier,
                "-port",
                Integer.toString(consolePort)
        ));
        return cmd;
    }


    public static boolean emulatorStarted(@Nonnull String adbIdentifier) {
        List<String> knownDevices = listDevices();
        return knownDevices.contains(adbIdentifier);
    }
    
    public static void killEmulator(@Nonnull String adbIdentifier) {
        if (!emulatorStarted(adbIdentifier)) {
            return;
        }
        
        List<String> cmd = new ArrayList<>(Arrays.asList(
                ADB_COMMAND,
                "-s",
                adbIdentifier,
                "emu",
                "kill"
        ));
        execSynchronous(cmd);

        while (AndroidEmuHelper.emulatorStarted("emulator-5580")) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static void startEmulatorSynchronous(@Nonnull String instanceIdentifier, int consolePort) {
        List<String> cmd = startEmulatorCmd(instanceIdentifier, consolePort);
        execSynchronous(cmd);
    }

    public static void startEmulatorAsynchronous(@Nonnull String instanceIdentifier, int consolePort) {
        if (!emulatorStarted("emulator-" + Integer.toString(consolePort))) {
            List<String> cmd = startEmulatorCmd(instanceIdentifier, consolePort);
            execAsynchronous(cmd);
        }

        while (!isFullyBooted("emulator-" + Integer.toString(consolePort))) {
            try {
                Thread.sleep(400);
                System.out.print(".");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean isFullyBooted(@Nonnull String adbIdentifier) {
        List<String> cmd = new ArrayList<>(Arrays.asList(
                ADB_COMMAND,
                "-s",
                adbIdentifier,
                "shell",
                "getprop",
                "init.svc.bootanim"
        ));
        String value = execSynchronousGetOutput(cmd);
        return value != null && value.startsWith("stopped");
    }

    public static List<String> listDevices() {
        List<String> cmd = new ArrayList<>(Arrays.asList(
                ADB_COMMAND,
                "devices"
        ));

        String listDevicesResult = execSynchronousGetOutput(cmd);

        return Arrays.asList(listDevicesResult
                .replace("List of devices attached\n", "")
                .replace("\tdevice", "").split("\n"));
    }

    public static void destroyEmulator(@Nonnull String emulatorIdentifier) {
        if (emulatorExists(emulatorIdentifier)) {
            List<String> cmd = new ArrayList<>(Arrays.asList(
                    AVDMANAGER_COMMAND,
                    "delete",
                    "avd",
                    "--name",
                    emulatorIdentifier
            ));
            execSynchronous(cmd);
            
            while (emulatorExists(emulatorIdentifier)) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static int execSynchronous(@Nonnull List<String> cmd) {
        logger.debug(cmd.stream().collect(Collectors.joining(" ")));
        try {
            ProcessBuilder pb = new ProcessBuilder().command(cmd).inheritIO();
            Process p = pb.start();
            p.waitFor();
            return p.exitValue();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String execSynchronousGetOutput(@Nonnull List<String> cmd) {
        logger.debug(cmd.stream().collect(Collectors.joining(" ")));
        try {
            ProcessBuilder pb = new ProcessBuilder().command(cmd);
            Process p = pb.start();
            p.waitFor();

            InputStream is = p.getInputStream();
            String rval = IOUtils.toString(is);
            is.close();
            return rval;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private static void execAsynchronous(@Nonnull List<String> cmd) {
        logger.debug(cmd.stream().collect(Collectors.joining(" ")));
        try {
            ProcessBuilder pb = new ProcessBuilder().command(cmd).inheritIO();
            Process p = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
