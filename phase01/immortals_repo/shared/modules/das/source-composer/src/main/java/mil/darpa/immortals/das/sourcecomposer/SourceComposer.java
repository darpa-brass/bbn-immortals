package mil.darpa.immortals.das.sourcecomposer;

import mil.darpa.immortals.das.buildbridge.BuildBridge;
import mil.darpa.immortals.das.configuration.*;
import mil.darpa.immortals.das.sourcecomposer.dfucomposers.ConsumingPipeComposer;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Created by awellman@bbn.com on 10/10/16.
 */
public class SourceComposer {

    public static class DfuInstance {
        private final String dfuClasspath;
        private final String dfuDependencyString;

        DfuInstance(String dfuClasspath, String dfuDependencyString) {
            this.dfuClasspath = dfuClasspath;
            this.dfuDependencyString = dfuDependencyString;
        }

        public String getClasspath() {
            return dfuClasspath;
        }

        public String getDependencyString() {
            return dfuDependencyString;
        }
    }

    public static class ApplicationInstance {

        private final EnvironmentConfiguration environmentConfiguration;
        private final EnvironmentConfiguration.CompositionTargetProfile compositionTargetProfile;
        private final Path targetApplicationPath;
        private final String sessionIdentifier;

        private final LinkedList<String> dependencyLines = new LinkedList<>();


        ApplicationInstance(EnvironmentConfiguration environmentConfiguration, EnvironmentConfiguration.CompositionTarget applicationIdentifier, String sessionIdentifier) throws IOException {
            this.sessionIdentifier = sessionIdentifier;
            this.environmentConfiguration = environmentConfiguration;
            this.compositionTargetProfile = environmentConfiguration.getApplicationProfile(applicationIdentifier);
            this.targetApplicationPath = ApplicationAugmenter.intializeApplicationInstance(compositionTargetProfile, sessionIdentifier).toAbsolutePath();

            dependencyLines.add("apply plugin: 'com.android.application'");
            dependencyLines.add("dependencies {");
            dependencyLines.add("}");
        }

        public Path getApplicationPath() {
            return targetApplicationPath;
        }

        public synchronized void addDependency(String dependencyString) throws IOException {
            String insertionString = "    compile '" + dependencyString + "'";

            if (!dependencyLines.contains(insertionString)) {
                dependencyLines.add(dependencyLines.size() - 1, insertionString);

                Path gradleFilepath = targetApplicationPath.resolve(compositionTargetProfile.getGradleTargetFile());
                StandardOpenOption fileOption = (Files.exists(gradleFilepath) ?
                        StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.CREATE_NEW);

                Files.write(gradleFilepath, dependencyLines, fileOption);
            }
        }

        public void replaceClassInTargetFiles(String originalClasspath, String newClasspath) throws IOException {
            ApplicationAugmenter.replaceClass(compositionTargetProfile, targetApplicationPath.toString(), originalClasspath, newClasspath);
        }

        public void build() {
            BuildBridge.getInstance().buildApplication(getApplicationPath());
        }
    }

    private final String sessionIdentifier;

    private final EnvironmentConfiguration environmentConfiguration;

    private final BuildBridge buildBridge;


    @Deprecated
    public SourceComposer() {
        this.sessionIdentifier = "S" + Long.toString(System.currentTimeMillis());
        this.environmentConfiguration = EnvironmentConfiguration.getInstance();
        this.buildBridge = BuildBridge.getInstance();
        initializeSession();
    }

    public SourceComposer(String sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
        this.environmentConfiguration = EnvironmentConfiguration.getInstance();
        this.buildBridge = BuildBridge.getInstance();
        initializeSession();
    }

    /**
     * Creates a new directory of the ATAKClient for manipulation
     *
     * @return The absolute path to the new client instance
     */
    public ApplicationInstance initializeApplicationInstance(EnvironmentConfiguration.CompositionTarget applicationIdentifier) throws IOException {
        return new ApplicationInstance(environmentConfiguration, applicationIdentifier, sessionIdentifier);
    }

    /**
     * Creates a new DFU using the provided configuration
     *
     * @return The absolute path to the new dfu instance
     */
    public DfuInstance constructAndPublishComposedDfu(DfuCompositionConfiguration dfuCompositionConfiguration) throws CompositionException {
        if (dfuCompositionConfiguration.originalDfu.consumingPipeSpecification != null) {
            EnvironmentConfiguration ec = EnvironmentConfiguration.getInstance();

            ConsumingPipeComposer cpc = new ConsumingPipeComposer(ec, dfuCompositionConfiguration);
            List<String> outputLines = cpc.createFileLines();
            Map<String, List<String>> fileMap = new HashMap<>();
            fileMap.put(dfuCompositionConfiguration.getProductClasspath(), outputLines);
            try {
                BuildEnvironmentHelper.publishSourceTree(dfuCompositionConfiguration, fileMap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            buildBridge.buildSynthesisRepository(dfuCompositionConfiguration.getSessionIdentifier());
        }

        DfuInstance dfuInstance = new DfuInstance(dfuCompositionConfiguration.getProductClasspath(),
                dfuCompositionConfiguration.getProductDependencyIdentifier());
        return dfuInstance;
    }

    public void executeAnalysis(ApplicationInstance applicationInstance, DfuCompositionAnalysisConfiguration configuration) throws CompositionException, IOException {
        Set<DfuCompositionConfiguration> configurations = configuration.produceCombinations();

        List<DfuInstance> dfuInstances = new ArrayList<>(configurations.size());

        boolean isProfiling = false;

        for (DfuCompositionConfiguration dfuCompositionConfiguration : configurations) {

            // Construct the DFU and return the dependency String to be inserted into the application
            SourceComposer.DfuInstance dfuInstance = constructAndPublishComposedDfu(dfuCompositionConfiguration);
            dfuInstances.add(dfuInstance);

            if (!isProfiling) {
                for (DfuCompositionConfiguration.DfuConfigurationContainer dfu : dfuCompositionConfiguration.dfuCompositionSequence) {
                    if (dfu.performAnalysis) {
                        isProfiling = true;
                        break;
                    }
                }
            }

            // Substitute the lines in the application with the synthesized DFU identifier
            applicationInstance.replaceClassInTargetFiles(dfuCompositionConfiguration.originalDfu.consumingPipeSpecification.classPackageIdentifier,
                    dfuInstance.getClasspath());

            // Add the DFU to the application's dependencies
            applicationInstance.addDependency(dfuInstance.getDependencyString());

            if (isProfiling) {
                if (dfuCompositionConfiguration.targetPlatform == DeploymentPlatform.Java) {
                    applicationInstance.addDependency("mil.darpa.immortals.components:product-analysis-bundle-android:+");

                } else if (dfuCompositionConfiguration.targetPlatform == DeploymentPlatform.Android) {
                    applicationInstance.addDependency("mil.darpa.immortals.components:product-analysis-bundle-base:+");
                }
            }
        }


    }

    public DfuCompositionConfiguration constructCP2ControlPointComposition(String originalDependencyString, String originalClassPackageIdentifier, String augmenterDependencyString, String augmenterClassPackageIdentifier, List<String> parameters) {
        return ControlPointAugmenter.constructCP2ControlPointComposition(originalDependencyString, originalClassPackageIdentifier, augmenterDependencyString, augmenterClassPackageIdentifier, parameters, sessionIdentifier);
    }

    public void executeCP2Composition(ApplicationInstance applicationInstance, DfuCompositionConfiguration dfuCompositionConfiguration) throws CompositionException, IOException {
        // Construct the DFU and return the dependency String to be inserted into the application
        SourceComposer.DfuInstance dfuInstance = constructAndPublishComposedDfu(dfuCompositionConfiguration);

        // Substitute the lines in the application with the synthesized DFU identifier
        applicationInstance.replaceClassInTargetFiles(dfuCompositionConfiguration.originalDfu.consumingPipeSpecification.classPackageIdentifier,
                dfuInstance.getClasspath());

        // Add the DFU to the application's dependencies
        applicationInstance.addDependency(dfuInstance.getDependencyString());
    }

    // Calling everything using values instead of variables since "rm -rf" is dangerous and this is not mission-critical

//    String d = "${IMMORTALS_ROOT}/PRODUCTS/"
//
//    if (Files.exists(Paths.get("${d}/modules/"))) {
//        delete {
//            [
//                    "${d}/modules/dfus",
//                    "${d}/modules/settings.gradle",
//                    "${d}/modules/.gradle"
//            ]
//        }
//        Files.createFile(Paths.get("${d}/modules/settings.gradle"))
//        Files.createDirectory(Paths.get("${d}/modules/dfus"))
//    }
//
//    if (Files.exists(Paths.get("${d}/IMMORTALS_REPO"))) {
//        delete {
//            "${d}/IMMORTALS_REPO"
//        }
//        Files.createDirectory(Paths.get("${d}/IMMORTALS_REPO"))
//    }
//
//    if (Files.exists(Paths.get("${d}/applications"))) {
//        delete {
//            "${d}/applications"
//        }
//        Files.createDirectory(Paths.get("${d}/applications"))
//    }

    private void initializeSession() {
        try {
            EnvironmentConfiguration ec = EnvironmentConfiguration.getInstance();

            Path rootPath = ec.getSynthesisRootPath(sessionIdentifier);
            Files.createDirectories(ec.getSynthesisModulesPath(sessionIdentifier));
            Files.createDirectories(ec.getSynthesizedDfuProjectFilepath(sessionIdentifier));
            Files.createDirectories(ec.getSynthesisRepoPath(sessionIdentifier));
            Files.createDirectories(rootPath);

            Path sourcePath = ec.getImmortalsRepositoryProjectFilepath().resolve("buildSrc");
            Path targetPath = ec.getSynthesisModulesPath(sessionIdentifier).resolve("buildSrc");

//            Files.createDirectories(targetPath);
            FileUtils.copyDirectory(sourcePath.toFile(), targetPath.toFile());

            Files.createFile(ec.getSynthesisGradleSettingsFilepath(sessionIdentifier));
        } catch (IOException e) {
            // If this fails, it is an environment configuration issue that is unrelated to synthesis decisions
            throw new RuntimeException(e);
        }
    }
}
