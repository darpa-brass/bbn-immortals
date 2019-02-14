package mil.darpa.immortals.das.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 10/10/16.
 */
@Deprecated
public class EnvironmentConfiguration {
    
    class RawEnvironmentConfiguration {
        public String dfuProjectsSubdirectory;
        public String synthesizedDfuPackage;
        public String synthesisRoot;
        public String synthesisModulesSubdirectory;
        public String synthesizedDfusSubdirectory;
        public String synthesizedDfuSourceSubdirectory;
        public String synthesisGradleSettingsFile;
        public String harnessRestUrl;
        
        public EnvironmentConfiguration toEnvironmentConfiguration() {
            return new EnvironmentConfiguration(
                    dfuProjectsSubdirectory,
                    synthesizedDfuPackage,
                    synthesisRoot,
                    synthesisModulesSubdirectory,
                    synthesizedDfusSubdirectory,
                    synthesizedDfuSourceSubdirectory,
                    synthesisGradleSettingsFile,
                    null,
                    harnessRestUrl);
        }
    }

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

    public static EnvironmentConfiguration initializeDefaultEnvironmentConfiguration() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        InputStreamReader isr = new InputStreamReader(EnvironmentConfiguration.class.getResourceAsStream("/DefaultEnvironmentConfiguration.json"));
        mEnvironmentConfiguration = gson.fromJson(isr, RawEnvironmentConfiguration.class).toEnvironmentConfiguration();
        return mEnvironmentConfiguration;
    }

    public static EnvironmentConfiguration initializeDefaultEnvironmentConfiguration(Path immortalsRoot) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        InputStreamReader isr = new InputStreamReader(EnvironmentConfiguration.class.getResourceAsStream("/DefaultEnvironmentConfiguration.json"));

        JsonObject jo = gson.fromJson(isr, JsonObject.class);
        jo.addProperty("immortalsRoot", immortalsRoot.toString());

        mEnvironmentConfiguration = gson.fromJson(jo, EnvironmentConfiguration.class);
       
        return mEnvironmentConfiguration;
    }

    public static EnvironmentConfiguration loadEnvironmentConfiguration(String path) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        FileReader fr = new FileReader(new File(path));
        JsonObject jo = gson.fromJson(fr, JsonObject.class);
        jo.addProperty("immortalsRoot", path.toString());
        EnvironmentConfiguration environmentConfiguration = gson.fromJson(jo, EnvironmentConfiguration.class);
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
        return dfuProjectsDirectory;
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

    private static final Path getImmortalsRootPath(String immortalsRoot) {
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



    private Path getSynthesisRootPath() {
        if (!Files.exists(synthesisRoot)) {
            try {
                Files.createDirectories(synthesisRoot);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return synthesisRoot;
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

    public final Path immortalsRoot;
    public final String synthesizedDfuPackage;
    protected final Path synthesisRoot;
    final String synthesisModulesSubdirectory;
    final String synthesizedDfusSubdirectory;
    public final String synthesizedDfuSourceSubdirectory;
    protected final String synthesisGradleSettingsFile;
    private final String harnessRestUrl;

    public final Path defaultDfusPath;

    public final Path dfuProjectsDirectory;


    public String getHarnessRestUrl() {
        return harnessRestUrl;
    }


    public EnvironmentConfiguration(String dfuProjectsSubdirectory, String synthesizedDfuPackage, String synthesisRoot, String synthesisModulesSubdirectory, String synthesizedDfusSubdirectory, String synthesizedDfuSourceSubdirectory, String synthesisGradleSettingsFile, String immortalsRoot, String harnessRestUrl) {
        this.immortalsRoot = getImmortalsRootPath(immortalsRoot);

        this.synthesisRoot = this.immortalsRoot.resolve(synthesisRoot);
        mkdir(this.synthesisRoot);
        this.dfuProjectsDirectory = this.immortalsRoot.resolve(dfuProjectsSubdirectory);
        this.synthesizedDfuPackage = synthesizedDfuPackage;

        this.defaultDfusPath = this.immortalsRoot.resolve(dfuProjectsSubdirectory);

        this.synthesisModulesSubdirectory = synthesisModulesSubdirectory;
        this.synthesizedDfusSubdirectory = synthesizedDfusSubdirectory;
        this.synthesizedDfuSourceSubdirectory = synthesizedDfuSourceSubdirectory;
        this.synthesisGradleSettingsFile = synthesisGradleSettingsFile;
        this.harnessRestUrl = harnessRestUrl;
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

}
