package mil.darpa.immortals.core.das.adaptationtargets.deploying;

import com.google.gson.*;
import mil.darpa.immortals.analysis.adaptationtargets.DeploymentTarget;
import mil.darpa.immortals.config.ImmortalsConfig;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;


/**
 * Created by awellman@bbn.com on 1/25/18.
 */
public class ApplicationDeploymentInstance {
    
    public static class PathDeserializer implements JsonDeserializer<Path> {
        @Override
        public Path deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Paths.get(jsonElement.getAsString());
        }
    }

    private final String deploymentInstanceIdentifier;
    private final Path executablePath;
    public final HashMap<File, String> deploymentFileMap;
    private final DeploymentTarget deploymentTarget;
    private final String packageIdentifier;
    private final String mainMethod;
    private final int settleTimeMS;
    private Path deploymentPath;
    private final String[] validators;

    public ApplicationDeploymentInstance(
            @Nonnull String deploymentInstanceIdentifier, @Nonnull Path executablePath,
            @Nonnull HashMap<File, String> deploymentFileMap, @Nonnull DeploymentTarget deploymentTarget,
            @Nonnull String packageIdentifier, @Nonnull String mainMethod, @Nonnull int settleTimeMS,
            @Nonnull Path deploymentPath, @Nonnull String[] validators) {
        this.deploymentInstanceIdentifier = deploymentInstanceIdentifier;
        this.executablePath = executablePath;
        this.deploymentFileMap = deploymentFileMap;
        this.deploymentTarget = deploymentTarget;
        this.packageIdentifier = packageIdentifier;
        this.mainMethod = mainMethod;
        this.settleTimeMS = settleTimeMS;
        this.deploymentPath = deploymentPath;
        this.validators = validators;
    }

    public String getDeploymentInstanceIdentifier() {
        return deploymentInstanceIdentifier;
    }

    public Path getDeploymentPath() {
        return deploymentPath;
    }

    public Path getExecutablePath() {
        return executablePath;
    }

    public HashMap<File, String> getDeploymentFileMap() {
        return deploymentFileMap;
    }

    public DeploymentTarget getDeploymentTarget() {
        return deploymentTarget;
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

    public Path getExecutableFile() {
        Path executable = deploymentPath.resolve(executablePath.getFileName());

        if (!Files.exists(executable)) {
            try {
                Files.copy(executablePath, executable);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return executable;
    }

    public ApplicationDeploymentInstance deriveNewInstanceWithoutDeploymentFileMap(@Nonnull String emulatorIdentifier, @Nonnull Path deploymentRootDirectory) throws IOException {
        File targetFilepath = deploymentRootDirectory.resolve(emulatorIdentifier).toFile();
        if (!targetFilepath.exists() && deploymentPath != null) {
            FileUtils.copyDirectory(deploymentPath.toFile(), targetFilepath);
        }
        return new ApplicationDeploymentInstance(
                emulatorIdentifier, executablePath, new HashMap<>(),
                deploymentTarget, packageIdentifier, mainMethod, settleTimeMS, targetFilepath.toPath(), validators);
    }
}
