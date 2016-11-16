package mil.darpa.immortals.das.sourcecomposer;

import mil.darpa.immortals.das.configuration.DfuCompositionConfiguration;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 10/11/16.
 */
public class BuildEnvironmentHelper {

    public static void publishSourceTree(DfuCompositionConfiguration compositionConfiguration, Map<String, List<String>> classpathContentsMap) throws IOException {
        createTopLevelBuildFileIfNecessary(compositionConfiguration);

        updateSynthesisSettingsGradle(compositionConfiguration);

        Path dfuModulePath = compositionConfiguration.getProductDirectory();
        Path dfuModuleSourcePath = compositionConfiguration.getProductSourceDirectory();

        List<String> buildFileLines = createBuildFile(compositionConfiguration);
        Path buildFilePath = dfuModulePath.resolve("build.gradle");
        Files.createDirectories(buildFilePath.getParent());
        Files.write(buildFilePath, buildFileLines);

        for (String classpath : classpathContentsMap.keySet()) {
            Path filepath = dfuModuleSourcePath.resolve(classpath.replaceAll("\\.", "/") + ".java");
            Files.createDirectories(filepath.getParent());
            Files.write(filepath, classpathContentsMap.get(classpath));
        }
    }

    private static void updateSynthesisSettingsGradle(DfuCompositionConfiguration compositionConfiguration) throws IOException {
        String dfuProjectIdentifier = "dfus:" +compositionConfiguration.getProductBaseIdentifier();
        List outList = new ArrayList(1);
        outList.add("include '" + dfuProjectIdentifier + "'");

        Path gradleSettingsFilepath = EnvironmentConfiguration.getInstance().getSynthesisGradleSettingsFilepath(compositionConfiguration.getSessionIdentifier());
        if (!Files.exists(gradleSettingsFilepath)) {
            Files.createFile(gradleSettingsFilepath);
        }

        Files.write(gradleSettingsFilepath, outList, StandardOpenOption.APPEND);
    }

    private static void createTopLevelBuildFileIfNecessary(DfuCompositionConfiguration compositionConfiguration) throws IOException {
        Path buildFile = EnvironmentConfiguration.getInstance().getSynthesisModulesPath(compositionConfiguration.getSessionIdentifier()).resolve("build.gradle");

        if (!Files.exists(buildFile)) {
            List<String> outputLines = new LinkedList<>();
            outputLines.add("subprojects {");
            outputLines.add("    apply plugin: immortals.CoreDefinitions");
            outputLines.add("    apply plugin: immortals.synthesis.JavaDfu");
            outputLines.add("    apply plugin: immortals.synthesis.PublishJavaDfu");
            outputLines.add("}");

            Files.write(buildFile, outputLines, StandardOpenOption.CREATE_NEW);
        }
    }

    private static List<String> createBuildFile(DfuCompositionConfiguration configuration) {
        List<String> outputLines = new LinkedList<>();

        outputLines.add("apply plugin: immortals.CoreDefinitions");
        outputLines.add("apply plugin: immortals.synthesis.JavaDfu");
        outputLines.add("apply plugin: immortals.synthesis.DfuBase");
        outputLines.add("apply plugin: immortals.synthesis.PublishJavaDfu");

        outputLines.add("group project.rootGroup + '.dfus'");

        List<String> dependencies = new LinkedList<>();
        for (DfuCompositionConfiguration.DfuConfigurationContainer dfuConfig :
                configuration.dfuCompositionSequence) {
            dependencies.add(dfuConfig.dependencyString);

            if (dfuConfig.performAnalysis) {
                // TODO: Make this flexible once java work also needs to be done
                dependencies.add("mil.darpa.immortals.components:product-analysis-bundle-android:+");
            }

        }

        if (!dependencies.isEmpty()) {
            outputLines.add("dependencies {");
            for (String dependencyString : dependencies) {
                outputLines.add("    compile '" + dependencyString + "'");
            }
            outputLines.add("}");
        }

        return outputLines;
    }
}
