package mil.darpa.immortals.core.das.adaptationtargets.deploying;

import com.bbn.ataklite.ATAKLiteConfig;
import com.bbn.marti.Tests;
import com.bbn.marti.ValidationRunner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import mil.darpa.immortals.config.ImmortalsConfig;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 1/29/18.
 */
public class AndroidApplicationDeployer {

    private final Gson gson;

    public enum ApplicationPhase {
        NOT_DEPLOYED,
        DEPLOYED,
        STARTED,
        HALTED
    }

    private final ApplicationDeploymentInstance deploymentInstance;

    private Process javaProcess;

    private final AndroidAdbHelper adbHelper;

    private final Path deploymentRootFolder;

    private final String clientIdentifier;

    private ApplicationPhase applicationPhase = ApplicationPhase.NOT_DEPLOYED;

    public AndroidApplicationDeployer(@Nonnull String clientIdentifier,
                                      @Nonnull ApplicationDeploymentInstance deploymentTemplate,
                                      @Nonnull Path deploymentRootFolder) throws IOException {
        this.clientIdentifier = clientIdentifier;
        this.deploymentRootFolder = deploymentRootFolder;
        this.adbHelper = AndroidAdbHelper.createInstance();
        this.deploymentInstance = deploymentTemplate.deriveNewInstanceWithoutDeploymentFileMap(adbHelper.getEmulatorIdentifier(), deploymentRootFolder);
        gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Path.class, new ApplicationDeploymentInstance.PathDeserializer()).create();
    }

    public synchronized void deploy() {
        if (applicationPhase != ApplicationPhase.NOT_DEPLOYED) {
            throw new RuntimeException(deploymentInstance.getDeploymentInstanceIdentifier() + ": Cannot deploy from the current phase '" + applicationPhase.name() + "'!");
        }
        
        if (!"23".equals(System.getProperty("mil.darpa.immortals.fakeAndroidVersion"))) {
            adbHelper.clean();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        prepareClientConfig();
        for (Map.Entry<File, String> entry : deploymentInstance.getDeploymentFileMap().entrySet()) {
            adbHelper.uploadFile(entry.getKey().toString(), entry.getValue());
        }

        adbHelper.deployApk(deploymentInstance.getDeploymentPath().resolve(deploymentInstance.getExecutableFile().getFileName()).toString());
        applicationPhase = ApplicationPhase.DEPLOYED;
    }

    public synchronized void start() {
        if (applicationPhase != ApplicationPhase.DEPLOYED) {
            throw new RuntimeException(deploymentInstance.getDeploymentInstanceIdentifier() + ": Cannot start from the current phase '" + applicationPhase.name() + "'!");
        }

        final String packageActivity = deploymentInstance.getPackageIdentifier() + "/" + deploymentInstance.getMainMethod();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                adbHelper.forceStopProcess(packageActivity);
            }
        }));

        try {
            adbHelper.startProcess(deploymentInstance.getPackageIdentifier() + "/" + deploymentInstance.getMainMethod());
            Thread.sleep(deploymentInstance.getSettleTimeMS());
            applicationPhase = ApplicationPhase.STARTED;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void halt() {
        if (applicationPhase != ApplicationPhase.STARTED) {
            throw new RuntimeException(deploymentInstance.getDeploymentInstanceIdentifier() + ": Cannot halt from the current phase '" + applicationPhase.name() + "'!");
        }

        adbHelper.forceStopProcess(deploymentInstance.getPackageIdentifier());
        applicationPhase = ApplicationPhase.HALTED;
    }

    private void prepareClientConfig() {
        InputStream is = Tests.class.getClassLoader().getResourceAsStream("ATAKLite-Config.json");
        ATAKLiteConfig config = gson.fromJson(new InputStreamReader(is), ATAKLiteConfig.class);

        Path clientDir = deploymentInstance.getDeploymentPath();
        clientDir.toFile().mkdir();

        try {
            if (!clientDir.resolve("sample_image.jpg").toFile().exists()) {
                Path target = clientDir.resolve("sample_image.jpg");
                InputStream sampleImage = Tests.class.getClassLoader().getResourceAsStream("sample_image.jpg");
                Files.copy(sampleImage, target);
                deploymentInstance.getDeploymentFileMap().put(target.toFile(), "/sdcard/ataklite/sample_image.jpg");
            }


            Path envTarget = clientDir.resolve("env.json");
            if (!deploymentRootFolder.resolve("env.json").toFile().exists()) {
                Files.copy(Tests.class.getClassLoader().getResourceAsStream("env.json"), envTarget);
                deploymentInstance.getDeploymentFileMap().put(envTarget.toFile(), "/sdcard/ataklite/env.json");
            }

            JsonObject ec = gson.fromJson(new FileReader(envTarget.toFile()), JsonObject.class);

            String fakeAndroidVersionStr = System.getProperty("mil.darpa.immortals.fakeAndroidVersion");
            if (fakeAndroidVersionStr == null) {
                throw new RuntimeException("Android Version not propagated to client! Error!");
            }
            ec.addProperty("simulatedAndroidVersion", Integer.valueOf(fakeAndroidVersionStr));
            Files.write(envTarget, gson.toJson(ec).getBytes());

            config.callsign = clientIdentifier;
            config.analyticsConfig.target = ATAKLiteConfig.AnalyticsTarget.ZEROMQ;
            config.analyticsConfig.url = ImmortalsConfig.getInstance().deploymentEnvironment.getMartiAddress();
            config.analyticsConfig.port = 53265;
            config.latestSABroadcastIntervalMS = 1000;
            config.imageBroadcastIntervalMS = 1000;
            config.imageBroadcastDelayMS = 0;
            config.latestSABroadcastDelayMS = 0;
            config.serverConfig.url = ImmortalsConfig.getInstance().deploymentEnvironment.getMartiAddress();

            Path configTarget = clientDir.resolve("ATAKLite-Config.json");
            String configJson = gson.toJson(config);

            Files.write(configTarget, configJson.getBytes());

            deploymentInstance.getDeploymentFileMap().put(configTarget.toFile(), "/sdcard/ataklite/ATAKLite-Config.json");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
