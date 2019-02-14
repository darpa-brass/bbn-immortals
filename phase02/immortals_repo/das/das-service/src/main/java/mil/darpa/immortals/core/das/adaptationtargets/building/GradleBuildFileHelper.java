package mil.darpa.immortals.core.das.adaptationtargets.building;

import mil.darpa.immortals.analysis.adaptationtargets.BuildPlatform;
import mil.darpa.immortals.analysis.adaptationtargets.DeploymentTarget;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.DasGsonHelper;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;
import mil.darpa.immortals.das.context.DasAdaptationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Created by awellman@bbn.com on 2/13/18.
 */
public class GradleBuildFileHelper {

    private final Map<String, String> dependencyReplacementMap = new HashMap<>();
    private final Map<String, Path> dependencyReplacementFileMap = new HashMap<>();

    private final List<String> newDependencies = new LinkedList<>();

    private final AdaptationTargetBuildInstance buildInstance;

    private boolean hasBeenSaved = false;

    public GradleBuildFileHelper(@Nonnull AdaptationTargetBuildInstance buildInstance) {
        this.buildInstance = buildInstance;
    }

    public synchronized void save() throws IOException {
        if (hasBeenSaved) {
            return;
        }
        hasBeenSaved = true;
        Path buildFile = buildInstance.getBuildFilePath();
        Path originalFile = buildFile.getParent().resolve(buildFile.getFileName() + ".original");

        if (!Files.exists(originalFile)) {
            Files.copy(buildFile, originalFile);
        }

        List<String> originallines = Files.readAllLines(originalFile);
        
        List<String> lines = new LinkedList<>();
        
        // TODO: Replace this with proper substitution. It won't let me substitute with a file for some reason...
        if (dependencyReplacementFileMap.isEmpty()) {
            lines = originallines;
        } else {
            for (String line : originallines) {
                for (Map.Entry<String, Path> entry : dependencyReplacementFileMap.entrySet()) {
                    line = line.replaceAll("compile " + "'" + entry.getKey() + "'", "compile files('" + entry.getValue().toString() + "')");
                    lines.add(line);
                }
            }
        }

        // Add dependency substitutions
        if (dependencyReplacementMap.size() > 0) {
            lines.add("");
            lines.add("configurations.all {");
            lines.add("    resolutionStrategy {");
            lines.add("        dependencySubstitution {");
            for (Map.Entry<String, String> entry : dependencyReplacementMap.entrySet()) {
                lines.add("            substitute module('" + entry.getKey() + "') with module('" + entry.getValue() + "')");

            }
            
            lines.add("        }");
            lines.add("    }");
            lines.add("}");
        }

        // Add new dependencies
        lines.add("");
        if (newDependencies.size() > 0) {
            lines.add("dependencies {");
            for (String dependency : newDependencies) {
                lines.add("compile '" + dependency + "'");
            }
            lines.add("}");
        }

        if (buildInstance.getPublishDependencyCoordinates() != null) {
            // Rename the artifact
            lines.add("publishing.publications.getAsMap().values().stream().filter { x -> x instanceof MavenPublication }.forEach {");
            lines.add("    it.artifactId = it.artifactId + '-" + buildInstance.getAdaptationIdentifier() + "'");
            lines.add("}");
        }

        Files.write(buildFile, lines, StandardOpenOption.TRUNCATE_EXISTING);
        hasBeenSaved = true;
    }

    public synchronized void replaceDependency(String originalDependency, String newDependency) throws IOException {
        dependencyReplacementMap.put(originalDependency, newDependency);
        hasBeenSaved = false;
    }
    
    public synchronized void replaceDependency(String originalDependencyCoordinates, Path newDependencyFilepath) {
        dependencyReplacementFileMap.put(originalDependencyCoordinates, newDependencyFilepath);
        hasBeenSaved = false;
    }

    public synchronized void addDependency(String dependency) {
        newDependencies.add(dependency);
        hasBeenSaved = false;
    }

    private synchronized boolean executeCommand(String[] cmd) throws IOException, InterruptedException {
        save();

        Path settingsFile = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("settings.gradle");

        Path originalFile = settingsFile.getParent().resolve(settingsFile.getFileName() + ".original");


        if (!Files.exists(buildInstance.getBuildRoot().resolve("settings.gradle"))) {
            if (!Files.exists(originalFile)) {
                Files.copy(settingsFile, originalFile);
            }

            List<String> lines = Files.readAllLines(originalFile);


            String includePath = ImmortalsConfig.getInstance().globals.getImmortalsRoot().relativize(buildInstance.getBuildRoot()).toString().replaceAll("/", ":");
            lines.add("include ':" + includePath + "'");
            Files.write(settingsFile, lines, StandardOpenOption.TRUNCATE_EXISTING);
        }

        ImmortalsProcessBuilder pb = new ImmortalsProcessBuilder(buildInstance.getAdaptationIdentifier(), "gradle");
        Process p = pb.command(cmd).start();
        p.waitFor();
//        if (!Files.exists(buildInstance.getBuildRoot().resolve("settings.gradle"))) {
//            Files.delete(settingsFile);
//            Files.copy(originalFile, settingsFile);
//        }

        return p.exitValue() == 0;
    }

    /**
     * Executes `gradle clean build`
     *
     * @return Whether or not it was built successfully
     */
    public synchronized boolean executeCleanAndBuild() throws IOException, InterruptedException {
        LinkedList<String> cmdList = new LinkedList<>();
        cmdList.add(buildInstance.getBuildToolPath().toString());
        cmdList.add("--build-file");
        cmdList.add(buildInstance.getBuildFilePath().toString());
        cmdList.addAll(Arrays.asList(buildInstance.getBuildToolBuildParameters()));
        return executeCommand(cmdList.toArray(new String[0]));
    }

    /**
     * Executes `gradle clean build publish` DO NOT Run this on something that does not publish an artifact!
     *
     * @return Whether or not it was successful.
     */
    public synchronized Boolean executeCleanBuildAndPublish() throws IOException, InterruptedException {
        if (!buildInstance.canPublish()) {
            return null;
        }
        LinkedList<String> cmdList = new LinkedList<>();
        cmdList.add(buildInstance.getBuildToolPath().toString());
        cmdList.add("--build-file");
        cmdList.add(buildInstance.getBuildFilePath().toString());
        cmdList.addAll(Arrays.asList(buildInstance.getPublishBuildToolParameters()));
        return executeCommand(cmdList.toArray(new String[0]));
    }

    public synchronized Boolean executeCleanAndTest(@Nullable Collection<String> testIdentifiers, @Nullable AdaptationTargetBuildInstance appToTestIntegrationWith, boolean fakeAndroid23) throws IOException, InterruptedException {
        if (!buildInstance.canTest()) {
            return null;
        }
        LinkedList<String> cmdList = new LinkedList<>();
        cmdList.add(buildInstance.getBuildToolPath().toString());
        cmdList.add("--build-file");
        cmdList.add(buildInstance.getBuildFilePath().toString());
        cmdList.addAll(Arrays.asList(buildInstance.getTestBuildToolParameters()));

        if (appToTestIntegrationWith != null) {
            if (buildInstance.getBuildPlatform() == BuildPlatform.GRADLE) {

                // TODO: Contain all gradle build/validation logic internally. All you need from the analysis should be task identifiers and task parameters.

                Path clientDeploymentJsonPath = buildInstance.getBuildRoot().resolve("clientDeploymentInstance.json");
                String clientConfigString = DasGsonHelper.getGson().toJson(appToTestIntegrationWith);
                Files.write(clientDeploymentJsonPath, clientConfigString.getBytes());
                // TODO: The filepath here should be quoted.... But it breaks running it from within the DAS, so skipping for now
                cmdList.add(cmdList.size() - 2, "-Dmil.darpa.immortals.clientDeploymentInstance.json.path=" + clientDeploymentJsonPath.toString());
                if (fakeAndroid23) {
                    cmdList.add(cmdList.size() - 2, "-Dmil.darpa.immortals.fakeAndroidVersion=23");
                    // It seems to need this....
                    cmdList.add(cmdList.size() - 2, "--no-daemon");
                    for (String cmd : new ArrayList<>(cmdList)) {
                        if (cmd.equals("--daemon")) {
                            cmdList.remove("--daemon");
                        }
                    }
                } else {
                    cmdList.add(cmdList.size() - 2, "-Dmil.darpa.immortals.fakeAndroidVersion=21");
                }
            }
        } else {
            cmdList.add(cmdList.size() - 2, "-Dmil.darpa.immortals.mock=true");
        }

        if (testIdentifiers != null) {
            if (buildInstance.getDeploymentTarget() == DeploymentTarget.JAVA) {
                for (String testIdentifier : testIdentifiers) {
                    cmdList.add("--tests");
                    cmdList.add(testIdentifier);
                }
            } else if (buildInstance.getDeploymentTarget() == DeploymentTarget.ANDROID) {
                List<String> androidTestIdentifiers = new ArrayList<>(testIdentifiers.size());
                for (String ti : testIdentifiers) {
                    int lastDot = ti.lastIndexOf(".");
                    androidTestIdentifiers.add(ti.substring(0, lastDot) + "#" + ti.substring(lastDot + 1));
                }
                if (!androidTestIdentifiers.isEmpty()) {
                    cmdList.add(cmdList.size() - 2, "-Pandroid.testInstrumentationRunnerArguments.class=" + String.join(",", androidTestIdentifiers));
                }

            } else {
                throw new RuntimeException("Unexpected deployment target '" + buildInstance.getDeploymentTarget() + "'!");
            }
        }
        return executeCommand(cmdList.toArray(new String[0]));
    }
    
    public synchronized Boolean executeCleanTestAndGetCoverage(@Nullable Collection<String> testIdentifiers) throws IOException, InterruptedException {
        if (!buildInstance.canTest()) {
            return null;
        }
        LinkedList<String> cmdList = new LinkedList<>();
        cmdList.add(buildInstance.getBuildToolPath().toString());
        cmdList.add("--build-file");
        cmdList.add(buildInstance.getBuildFilePath().toString());
        cmdList.addAll(Arrays.asList(buildInstance.getTestBuildToolParameters()));

        if (testIdentifiers != null) {
            for (String testIdentifier : testIdentifiers) {
                cmdList.add("--tests");
                cmdList.add(testIdentifier);
            }
        }
        
        cmdList.add("jacocoTestReport");

        return executeCommand(cmdList.toArray(new String[0]));
    }

}
