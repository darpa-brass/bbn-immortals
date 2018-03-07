package mil.darpa.immortals.das.configuration;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by awellman@bbn.com on 5/15/17.
 */
public class SessionConfiguration {

    private final EnvironmentConfiguration environmentConfiguration;
    public final Path synthesisRootPath;
    public final Path synthesisSessionPath;
    public final Path synthesizedModulesPath;
    public final Path synthesizedDfusPath;
    public final Path synthesizedDfusGradleSettingsFilepath;

    public SessionConfiguration(EnvironmentConfiguration ec, String sessionIdentifier) {
        this.environmentConfiguration = ec;
        this.synthesisRootPath = ec.synthesisRoot;
        this.synthesisSessionPath = ec.synthesisRoot.resolve(sessionIdentifier);
        this.synthesizedModulesPath = this.synthesisSessionPath.resolve(ec.synthesisModulesSubdirectory);
        this.synthesizedDfusGradleSettingsFilepath = this.synthesizedModulesPath.resolve(ec.synthesisGradleSettingsFile);
        this.synthesizedDfusPath = this.synthesisSessionPath.resolve(ec.synthesizedDfusSubdirectory);
    }

    public void initializeFilesystem() {
        mkdir(synthesisRootPath);
        mkdir(synthesisSessionPath);
        mkdir(synthesizedModulesPath);
        mkdir(synthesizedDfusPath);
        touch(synthesizedDfusGradleSettingsFilepath);

        Path sourcePath = this.environmentConfiguration.defaultDfusPath.resolve("buildSrc");
        Path targetPath = synthesizedModulesPath.resolve("buildSrc");
        copyDirectory(sourcePath, targetPath);
    }

    private static void mkdir(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void touch(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void copyDirectory(Path sourcePath, Path targetPath) {
        try {
            FileUtils.copyDirectory(sourcePath.toFile(), targetPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
