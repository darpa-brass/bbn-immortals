package mil.darpa.immortals.core.das.adaptationtargets.building;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;

import javax.annotation.Nonnull;
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

    private final List<String> newDependencies = new LinkedList<>();

    private final AdaptationTargetBuildInstance buildInstance;

    private boolean hasBeenSaved = false;

    public GradleBuildFileHelper(@Nonnull AdaptationTargetBuildInstance buildInstance) {
        this.buildInstance = buildInstance;
    }

    public synchronized void save() throws IOException {
        Path buildFile = buildInstance.getBuildFilePath();
        Path originalFile = buildFile.getParent().resolve(buildFile.getFileName() + ".original");

        if (!Files.exists(originalFile)) {
            Files.copy(buildFile, originalFile);
        }

        List<String> lines = Files.readAllLines(originalFile);

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

        if (buildInstance.getOwnDependencyCoordinates() != null) {
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
    }

    public synchronized void addDependency(String dependency) {
        newDependencies.add(dependency);
    }

    private synchronized boolean executeCommand(String[] cmd) throws IOException, InterruptedException {

        if (!hasBeenSaved) {
            save();
        }

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
        return executeCommand( cmdList.toArray(new String[0]));
    }

    /**
     * Executes `gradle clean build publish` DO NOT Run this on something that does not publish an artifact!
     *
     * @return Whether or not it was successful.
     */
    public synchronized boolean executeCleanBuildAndPublish() throws IOException, InterruptedException {
        LinkedList<String> cmdList = new LinkedList<>();
        cmdList.add(buildInstance.getBuildToolPath().toString());
        cmdList.add("--build-file");
        cmdList.add(buildInstance.getBuildFilePath().toString());
        cmdList.addAll(Arrays.asList(buildInstance.getBuildToolPublishParameters()));
        return executeCommand( cmdList.toArray(new String[0]));
    }

    public synchronized boolean executeCleanAndTest() throws IOException, InterruptedException {
        LinkedList<String> cmdList = new LinkedList<>();
        cmdList.add(buildInstance.getBuildToolPath().toString());
        cmdList.add("--build-file");
        cmdList.add(buildInstance.getBuildFilePath().toString());
        cmdList.addAll(Arrays.asList(buildInstance.getBuildToolValidationParameters()));
        return executeCommand( cmdList.toArray(new String[0]));
    }

}
