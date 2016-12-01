package mil.darpa.immortals.das.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by awellman@bbn.com on 10/10/16.
 */
public class EnvironmentConfiguration {

    private static EnvironmentConfiguration mEnvironmentConfiguration;

    private static final Set<String> immortalsRootExpectedFiles = new HashSet<>();

    static {
        immortalsRootExpectedFiles.add("applications");
        immortalsRootExpectedFiles.add("das");
        immortalsRootExpectedFiles.add("dsl");
        immortalsRootExpectedFiles.add("knowledge-repo");
        immortalsRootExpectedFiles.add("shared");
        immortalsRootExpectedFiles.add("build.gradle");
        immortalsRootExpectedFiles.add("settings.gradle");
    }

    public enum CompositionTarget {
        Client_ATAKLite,
        Client_ConsumingPipeRunner
    }

    public static EnvironmentConfiguration initializeDefaultEnvironmentConfiguration() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        InputStreamReader isr = new InputStreamReader(EnvironmentConfiguration.class.getResourceAsStream("/DefaultEnvironmentConfiguration.json"));
        EnvironmentConfiguration environmentConfiguration = gson.fromJson(isr, EnvironmentConfiguration.class);
        mEnvironmentConfiguration = environmentConfiguration;
        return mEnvironmentConfiguration;
    }

    public static EnvironmentConfiguration initializeDefaultEnvironmentConfiguration(Path immortalsRoot) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        InputStreamReader isr = new InputStreamReader(EnvironmentConfiguration.class.getResourceAsStream("/DefaultEnvironmentConfiguration.json"));
        EnvironmentConfiguration environmentConfiguration = gson.fromJson(isr, EnvironmentConfiguration.class);
        mEnvironmentConfiguration = environmentConfiguration;
        mEnvironmentConfiguration.immortalsRoot = immortalsRoot.toAbsolutePath().toString();
        return mEnvironmentConfiguration;
    }

    public static EnvironmentConfiguration loadEnvironmentConfiguration(String path) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        FileReader fr = new FileReader(new File(path));
        EnvironmentConfiguration environmentConfiguration = gson.fromJson(fr, EnvironmentConfiguration.class);
        fr.close();

        mEnvironmentConfiguration = environmentConfiguration;
        return mEnvironmentConfiguration;
    }

    public static EnvironmentConfiguration getInstance() {
        if (mEnvironmentConfiguration == null) {
            throw new RuntimeException("The environment configuration path must be set before an instance can be obtained!");
        }
        return mEnvironmentConfiguration;
    }

    public Path getSynthesisGradleSettingsFilepath(String sessionIdentifier) {
        return getSynthesisModulesPath(sessionIdentifier).resolve(synthesisGradleSettingsFile);
    }

    public Path getImmortalsRepositoryProjectFilepath() {
        return getImmortalsRootPath().resolve(dfuProjectsSubdirectory);
    }

    public Path getSynthesizedDfuProjectFilepath(String sessionIdentifier) {
        Path p = getSynthesisRootPath(sessionIdentifier).resolve(synthesizedDfusSubdirectory);
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return p;
    }

    public Path getScenarioComposeerPath() {
        return getImmortalsRootPath().resolve(scenarioComposerSubdirectory);
    }

    public Path getImmortalsRootPath() {
        if (immortalsRoot != null) {
            return Paths.get(immortalsRoot).toAbsolutePath();

        } else {
            try {
                String executionPath = URLDecoder.decode(EnvironmentConfiguration.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");

                Path currentPath = Paths.get(executionPath);

                while (currentPath != null) {

                    boolean isRoot = true;

                    for (String subpath : immortalsRootExpectedFiles) {
                        if (!Files.exists(currentPath.resolve(subpath))) {
                            isRoot = false;
                            break;
                        }
                    }
                    if (isRoot) {
                        immortalsRoot = currentPath.toAbsolutePath().toString();
                        return currentPath.toAbsolutePath();
                    } else {
                        currentPath = currentPath.getParent();
                    }

                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        if (immortalsRoot == null) {
            throw new RuntimeException("Could not find the immortals root directory! please set it using the 'IMMORTALS_ROOT' environment variable!");
        }

        return Paths.get(immortalsRoot);
    }


    public static class CompositionTargetProfile {

        public final CompositionTarget compositionTarget;

        private final String applicationBasePath;

//        private final String augmentedApplicationPath;

        private final String gradleModificationFile;

        public final LinkedList<String> synthesisTargetFiles;


        public Path getSourceApplicationFilepath() {
            return EnvironmentConfiguration.getInstance().getImmortalsRootPath().resolve(applicationBasePath);
        }

        public Path generateTargetApplicationPathValue(String sessionIdentifier) {
            String basePath = EnvironmentConfiguration.getInstance().getSynthesisRootPath(sessionIdentifier).resolve(applicationBasePath).toString();

            return Paths.get((basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath) +
                    "-" + sessionIdentifier);
        }

        public String getGradleTargetFile() {
            return gradleModificationFile;
        }

        public CompositionTargetProfile(CompositionTarget compositionTarget, String applicationBasePath, String gradleModificationFile, LinkedList<String> synthesisTargetFiles) {
            this.compositionTarget = compositionTarget;
            this.applicationBasePath = applicationBasePath;
            this.gradleModificationFile = gradleModificationFile;
            this.synthesisTargetFiles = synthesisTargetFiles;
        }
    }

    public Path getSynthesisRootPath() {
        Path p = getImmortalsRootPath().resolve(synthesisRoot);
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return p;
    }

    public Path getSynthesisRootPath(String sessionIdentifier) {
        Path p = getSynthesisRootPath().resolve(sessionIdentifier);
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return p;
    }

    public Path getSynthesisModulesPath(String sessionIdentifier) {
        Path p = getSynthesisRootPath(sessionIdentifier).resolve(synthesisModulesSubdirectory);
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return p;
    }

    public Path getSynthesisRepoPath(String sessionIdentifier) {
        Path p = getSynthesisRootPath(sessionIdentifier).resolve("IMMORTALS_REPO");
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return p;
    }

    public String getSynthesizedDfuPackage() {
        return synthesizedDfuPackage;
    }

    String getSynthesizedDfuSourceSubdirectory() {
        return synthesizedDfuSourceSubdirectory;
    }

    private String immortalsRoot;
    private final String dfuProjectsSubdirectory;
    private final String synthesizedDfuPackage;
    private final String synthesisRoot;
    private final String synthesisModulesSubdirectory;
    private final String synthesizedDfusSubdirectory;
    private final String synthesizedDfuSourceSubdirectory;
    private final String synthesisGradleSettingsFile;
    private final String scenarioComposerSubdirectory;
    private final HashMap<String, Integer> ipcPorts;

    public int getIpcPort(String componentIdentifier) {
        return ipcPorts.get(componentIdentifier);
    }

    private final ArrayList<CompositionTargetProfile> compositionTargetProfiles;

    public CompositionTargetProfile getApplicationProfile(CompositionTarget compositionTarget) {
        for (CompositionTargetProfile profile : compositionTargetProfiles) {
            if (profile.compositionTarget == compositionTarget) {
                return profile;
            }
        }
        throw new RuntimeException("No application profile for '" + compositionTarget + "' has been defined!");
    }

    public EnvironmentConfiguration(String dfuProjectsSubdirectory, String synthesizedDfuPackage, String synthesisRoot, String synthesisModulesSubdirectory, String synthesizedDfusSubdirectory, String synthesizedDfuSourceSubdirectory, String synthesisGradleSettingsFile, String scenarioComposerSubdirectory, String immortalsRoot, ArrayList<CompositionTargetProfile> compositionTargetProfiles, Map<String, Integer> ipcPorts) {
        this.dfuProjectsSubdirectory = dfuProjectsSubdirectory;
        this.synthesizedDfuPackage = synthesizedDfuPackage;
        this.synthesisRoot = synthesisRoot;
        this.synthesisModulesSubdirectory = synthesisModulesSubdirectory;
        this.synthesizedDfusSubdirectory = synthesizedDfusSubdirectory;
        this.synthesizedDfuSourceSubdirectory = synthesizedDfuSourceSubdirectory;
        this.synthesisGradleSettingsFile = synthesisGradleSettingsFile;
        this.scenarioComposerSubdirectory = scenarioComposerSubdirectory;
        this.immortalsRoot = immortalsRoot;
        this.compositionTargetProfiles = compositionTargetProfiles;
        this.ipcPorts = new HashMap<>(ipcPorts);
    }
}
