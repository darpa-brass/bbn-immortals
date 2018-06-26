package mil.darpa.immortals.config;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Global values.
 * <p>
 * The declaration of values as static and instance seems redundant, but there is logic to this madness:
 * <p>
 * By declaring them as static, they can be accessed by the instance initializers of any class, which helps when
 * you need to declare subdirectories of a defined directory path.
 * <p>
 * By declaring them instance, it ensures they are recorded to any produced JSON configuration files.
 */
public class GlobalsConfig {

    //////// Define the static properties that other configuration files can derive their own default values from
    public static final Path staticImmortalsRoot = StaticHelper.resolveExistingDirectoryValue(
            "globals.immortalsRoot",
            StaticHelper.readResourceValue("/IMMORTALS_ROOT_PATH"));

    static final Path staticImmortalsRepo = StaticHelper.resolveExistingDirectoryValue(
            "globals.immortalsRepo",
            StaticHelper.readResourceValue("/IMMORTALS_REPO_PATH"));

    static final Path staticGlobalWorkingDirectory = StaticHelper.resolveNewDirectoryValue(
            "globals.globalWorkingDirectory",
            staticImmortalsRoot.resolve("DAS_DEPLOYMENT").toAbsolutePath().toString());

    static final Path staticGlobalApplicationDeploymentDirectory = StaticHelper.resolveNewDirectoryValue(
            "globals.globalApplicationDeploymentDirectory",
            staticImmortalsRoot.resolve("APP_DEPLOYMENT").toAbsolutePath().toString());


    static final Path staticTtlIngestionDirectory = StaticHelper.resolveExistingDirectoryValue(
            "globals.ttlIngestionDirectory",
            staticImmortalsRoot.resolve("knowledge-repo/vocabulary/ontology-static/ontology/"));

    // Define the instance values so that they are properly exposed and serialized
    private String immortalsRoot = staticImmortalsRoot.toString();
    private String immortalsRepo = staticImmortalsRepo.toString();
    private String globalApplicationDeploymentDirectory = staticGlobalApplicationDeploymentDirectory.toString();
    private String globalWorkingDirectory = staticGlobalWorkingDirectory.toString();
    private String ttlIngestionDirectory = staticTtlIngestionDirectory.toString();

    private String globalLogDirectory = mkworkingdir("_logs");

    // You would think these should be paired together... But I want to keep it easy to configure logs and runtime
    // data to be put in two completely separate locations
    private String executionsDirectory = globalWorkingDirectory;
    private String executionsLogDirectory = globalLogDirectory;

    private String immortalsOntologyUriRoot = "http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#";
    private String immortalsOntologyUriPrefix = "IMMoRTALS_mil_darpa_immortals_ontology";

    public static URI toFullUrl(RestfulAppConfigInterface c) {
        try {
            return new URI(c.getProtocol(), null, c.getUrl(), c.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getImmortalsOntologyUriRoot() {
        return immortalsOntologyUriRoot;
    }

    public String getImmortalsOntologyUriPrefix() {
        return immortalsOntologyUriPrefix;
    }

    private boolean headless = true;

    public Path getImmortalsRoot() {
        return Paths.get(immortalsRoot);
    }

    public URI getImmortalsRepo() {
        return Paths.get(immortalsRepo).toAbsolutePath().toUri();
    }

    public Path getGlobalWorkingDirectory() {
        return Paths.get(globalWorkingDirectory);
    }

    public Path getGlobalLogDirectory() {
        return Paths.get(globalLogDirectory);
    }

    public Path getApplicationsDeploymentDirectory(String adaptationIdentifier) {
        return StaticHelper.mkdir(Paths.get(globalApplicationDeploymentDirectory).resolve(adaptationIdentifier));
    }

    public Path getAdaptationWorkingDirectory(String adaptationIdentifier) {
        return StaticHelper.mkdir(Paths.get(executionsDirectory).resolve(adaptationIdentifier));
    }

    public Path getAdaptationComponentWorkingDirectory(String adaptationIdentifier, String componentIdentifier) {
        return StaticHelper.mkdir(getAdaptationWorkingDirectory(adaptationIdentifier).resolve(componentIdentifier));
    }

    public Path getAdaptationLogDirectory(String adaptationIdentifier) {
        return StaticHelper.mkdir(Paths.get(executionsLogDirectory).resolve(adaptationIdentifier));
    }

    public Path getTtlIngestionDirectory() {
        return Paths.get(ttlIngestionDirectory);
    }

    public boolean isHeadless() {
        return headless;
    }

    public GlobalsConfig() {
    }

    public static String mkworkingdir(@Nonnull String subdir) {
        Path dirPath = staticGlobalWorkingDirectory.resolve(subdir).toAbsolutePath();
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dirPath.toString();
    }
    
    public static Path mkextensiondir(@Nonnull String subdir) {
        Path dirPath = staticImmortalsRoot.resolve("extensions") .resolve(subdir).toAbsolutePath();
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dirPath;
    }

    public static Path getExtensionsDownloadDir() {
        try {
            Path buildDir = staticImmortalsRoot.resolve("build");
            if (!Files.exists(buildDir)) {
                Files.createDirectory(buildDir);
            }

            Path downloadDir = buildDir.resolve("immortals_extensions");
            if (!Files.exists(downloadDir)) {
                Files.createDirectory(downloadDir);
            }
            return downloadDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
