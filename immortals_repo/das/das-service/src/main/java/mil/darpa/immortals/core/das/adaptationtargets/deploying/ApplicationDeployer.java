package mil.darpa.immortals.core.das.adaptationtargets.deploying;

import mil.darpa.immortals.analysis.adaptationtargets.DeploymentTarget;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by awellman@bbn.com on 1/29/18.
 */
public class ApplicationDeployer {

    public enum ApplicationPhase {
        NOT_DEPLOYED,
        DEPLOYED,
        STARTED,
        HALTED
    }

    private final ApplicationDeploymentInstance deploymentInstance;

    private Process javaProcess;

    private final AndroidAdbHelper adbHelper;

    private ApplicationPhase applicationPhase = ApplicationPhase.NOT_DEPLOYED;

    public ApplicationDeployer(ApplicationDeploymentInstance deploymentInstance) {
        this.deploymentInstance = deploymentInstance;

        if (deploymentInstance.getDeploymentTarget() == DeploymentTarget.ANDROID) {
            adbHelper = AndroidAdbHelper.createInstance();
        } else {
            adbHelper = null;
        }
    }

    public synchronized void deploy() {
        if (applicationPhase != ApplicationPhase.NOT_DEPLOYED) {
            ImmortalsErrorHandler.reportFatalError(deploymentInstance.getDeploymentInstanceIdentifier() + ": Cannot deploy from the current phase '" + applicationPhase.name() + "'!");
            return;
        }

        switch (deploymentInstance.getDeploymentTarget()) {

            case JAVA:
                for (Map.Entry<File, String> entry : deploymentInstance.getDeploymentFileMap().entrySet()) {
                    try {
                        FileUtils.copyFile(entry.getKey(), new File(entry.getValue()));
                    } catch (IOException e) {
                        ImmortalsErrorHandler.reportFatalException(e);
                        throw new RuntimeException(e);
                    }
                }
                break;

            case ANDROID:
                for (Map.Entry<File, String> entry : deploymentInstance.getDeploymentFileMap().entrySet()) {
                    adbHelper.uploadFile(entry.getKey().toString(), entry.getValue());
                }

                adbHelper.deployApk(deploymentInstance.getDeploymentPath().resolve(deploymentInstance.getExecutableFile().getFileName()).toString());
                break;
        }
        applicationPhase = ApplicationPhase.DEPLOYED;
    }

    public synchronized void start() {
        if (applicationPhase != ApplicationPhase.DEPLOYED) {
            ImmortalsErrorHandler.reportFatalError(deploymentInstance.getDeploymentInstanceIdentifier() + ": Cannot start from the current phase '" + applicationPhase.name() + "'!");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (javaProcess != null) {
                    javaProcess.destroyForcibly();
                }
            }
        }));

        try {
            switch (deploymentInstance.getDeploymentTarget()) {

                case JAVA:
                    String[] cmd = {
                            ImmortalsConfig.getInstance().build.augmentations.getJavaExecutablePath(),
                            "-jar",
                            deploymentInstance.getExecutableFile().toString()
                    };

                    ProcessBuilder pb = new ProcessBuilder().directory(deploymentInstance.getDeploymentPath().toFile()).command(
                            cmd);

                    pb.inheritIO();

                    System.out.println("EXEC: `" + Arrays.stream(cmd).collect(Collectors.joining(" ")) + "`");

                    javaProcess = pb.start();
                    break;

                case ANDROID:
                    adbHelper.startProcess(deploymentInstance.getPackageIdentifier() + "/" + deploymentInstance.getMainMethod());
                    break;
            }

            Thread.sleep(deploymentInstance.getSettleTimeMS());
            applicationPhase = ApplicationPhase.STARTED;
        } catch (IOException | InterruptedException e) {
            ImmortalsErrorHandler.reportFatalException(e);
            throw new RuntimeException(e);
        }
    }

    public synchronized void halt() {
        if (applicationPhase != ApplicationPhase.STARTED) {
            ImmortalsErrorHandler.reportFatalError(deploymentInstance.getDeploymentInstanceIdentifier() + ": Cannot halt from the current phase '" + applicationPhase.name() + "'!");
            return;
        }

        switch (deploymentInstance.getDeploymentTarget()) {
            case JAVA:
                if (javaProcess != null) {
                    javaProcess.destroyForcibly();
                }
                break;

            case ANDROID:
                adbHelper.forceStopProcess(deploymentInstance.getPackageIdentifier());
        }
        applicationPhase = ApplicationPhase.HALTED;
    }

    public static void main(String[] args) {
        try {
            AndroidEmuHelper.killEmulator("emulator-5580");
            AndroidEmuHelper.killEmulator("emulator-5578");

            AndroidEmuHelper.destroyEmulator("BRASS-21-80");
            AndroidEmuHelper.destroyEmulator("BRASS-23-78");

            AndroidEmuHelper.createEmulator(21, "BRASS-21-80");
            AndroidEmuHelper.startEmulatorAsynchronous("BRASS-21-80", 5580);
            AndroidEmuHelper.createEmulator(23, "BRASS-23-78");
            AndroidEmuHelper.startEmulatorAsynchronous("BRASS-23-78", 5578);

            String time = Long.toString(System.currentTimeMillis());
            String adaptationIdentifier = "adaptation" + time.substring(0, time.length() - 4);

            AdaptationTargetBuildInstance martiBuild = GradleKnowledgeBuilder.getBuildInstance("Marti", adaptationIdentifier);
            AdaptationTargetBuildInstance atakBuild = GradleKnowledgeBuilder.getBuildInstance("ATAKLite", adaptationIdentifier);


            ApplicationDeploymentInstance martiDeployment = new ApplicationDeploymentInstance(martiBuild, "MartiServer");
            ApplicationDeploymentInstance atakDeployment0 = new ApplicationDeploymentInstance(atakBuild, "Atak0");
            ApplicationDeploymentInstance atakDeployment1 = new ApplicationDeploymentInstance(atakBuild, "Atak1");

            ApplicationDeployer martiDeployer = new ApplicationDeployer(martiDeployment);
            ApplicationDeployer atak0Deployer = new ApplicationDeployer(atakDeployment0);
            ApplicationDeployer atak1Deployer = new ApplicationDeployer(atakDeployment1);

            martiDeployer.deploy();
            martiDeployer.start();

            atak0Deployer.deploy();
            atak0Deployer.start();

            atak1Deployer.deploy();
            atak1Deployer.start();

            Thread.sleep(100000);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
