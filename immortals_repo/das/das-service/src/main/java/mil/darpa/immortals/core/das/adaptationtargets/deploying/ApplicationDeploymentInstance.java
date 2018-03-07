package mil.darpa.immortals.core.das.adaptationtargets.deploying;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationtargets.ATAKLiteConfig;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.adaptationtargets.building.DeploymentTarget;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by awellman@bbn.com on 1/25/18.
 */
public class ApplicationDeploymentInstance {

    private final String deploymentInstanceIdentifier;

    private Path deploymentPath;

    private final Path executablePath;
    private final HashMap<File, String> deploymentFileMap;
    private final DeploymentTarget deploymentTarget;

    public String getDeploymentInstanceIdentifier() {
        return deploymentInstanceIdentifier;
    }

    public Path getDeploymentPath() {
        return deploymentPath;
    }

    public Path getExecutableFile() {
        Path executable = deploymentPath.resolve(executablePath.getFileName());
        
        if (!Files.exists(executable)) {
            try {
                Files.copy(executablePath, executable);
            } catch (IOException e) {
                ImmortalsErrorHandler.reportFatalException(e);
            }
        }
        return executable;
    }

    public String getPackageIdentifier() {
        return packageIdentifier;
    }

    public String getMainMethod() {
        return mainMethod;
    }

    public int getSettleTimeMS() {
        return settleTimeMS;
    }

    private final String packageIdentifier;
    private final String mainMethod;
    private final int settleTimeMS;


    public ApplicationDeploymentInstance(@Nonnull AdaptationTargetBuildInstance buildInstance, @Nonnull String deploymentInstanceIdentifier) {
        this.deploymentInstanceIdentifier = deploymentInstanceIdentifier;
        this.executablePath = buildInstance.getExecutablePath();
        this.deploymentFileMap = new HashMap<>(buildInstance.getDeploymentFileMap());
        this.deploymentTarget = buildInstance.getDeploymentTarget();
        this.packageIdentifier = buildInstance.getExecutionPackageIdentifier();
        this.mainMethod = buildInstance.getExecutionMainMethod();
        this.settleTimeMS = buildInstance.getSettleTimeMS();
        this.deploymentPath = ImmortalsConfig.getInstance().globals
                .getApplicationsDeploymentDirectory(buildInstance.getAdaptationIdentifier())
                .resolve(buildInstance.getTargetIdentifier() + "-" + deploymentInstanceIdentifier);

        try {
            Files.createDirectory(this.deploymentPath);
        } catch (IOException e) {
            ImmortalsErrorHandler.reportFatalException(e);
        }
    }

    public DeploymentTarget getDeploymentTarget() {
        return deploymentTarget;
    }

    public synchronized HashMap<File, String> getDeploymentFileMap() {
        HashMap<File, String> rval = new HashMap<>();

        try {
            switch (deploymentTarget) {
                case JAVA:
                    for (Map.Entry<File, String> entry : deploymentFileMap.entrySet()) {
                        File target;
                        if (entry.getValue().startsWith("/")) {
                            ImmortalsErrorHandler.reportFatalError("Absolute paths are not supported for the target location of JAVA applications!");
                        } else {
                            target = deploymentPath.resolve(entry.getValue()).toFile();
                            Files.createDirectories(target.toPath().getParent());
                            FileUtils.copyFile(entry.getKey(), target);
//                            rval.put(target, target.toString());
                        }
                    }
                    break;

                case ANDROID:
                    for (Map.Entry<File, String> entry : deploymentFileMap.entrySet()) {
                        Path target = deploymentPath.resolve(entry.getValue().substring(1, entry.getValue().length()));

                        if (!Files.exists(target.getParent())) {
                            Files.createDirectories(target.getParent());
                        }

                        FileUtils.copyFile(entry.getKey(), target.toFile());

                        // TODO: This should not be hard-coded!
                        if (target.endsWith("ATAKLite-Config.json")) {
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            ATAKLiteConfig alc = gson.fromJson(new FileReader(target.toString()), ATAKLiteConfig.class);
                            alc.analyticsConfig.target = ATAKLiteConfig.AnalyticsTarget.DEFAULT;
                            alc.configSource = ATAKLiteConfig.ConfigSource.Filesystem;
                            alc.callsign = deploymentInstanceIdentifier;
                            alc.serverConfig.url = ImmortalsConfig.getInstance().deploymentEnvironment.getMartiAddress();

                            FileWriter fw = new FileWriter(target.toString());
                            fw.write(gson.toJson(alc));
                            fw.close();
                        }

                        rval.put(target.toFile(), entry.getValue());
                    }
                    break;
            }
        } catch (IOException e) {
            ImmortalsErrorHandler.reportFatalException(e);
            throw new RuntimeException(e);
        }
        return rval;
    }
}
