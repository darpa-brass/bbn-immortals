package mil.darpa.immortals.das.sourcecomposer;

import mil.darpa.immortals.configuration.sourcecomposer.ApplicationProfile;
import mil.darpa.immortals.configuration.sourcecomposer.AugmentationType;
import mil.darpa.immortals.configuration.sourcecomposer.ControlPointProfile;
import mil.darpa.immortals.configuration.sourcecomposer.DeploymentPlatform;
import mil.darpa.immortals.das.buildbridge.BuildBridge;
import mil.darpa.immortals.das.configuration.DfuCompositionConfiguration;
import mil.darpa.immortals.das.configuration.DfuSubstitutionInstance;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import mil.darpa.immortals.das.configuration.SessionConfiguration;
import mil.darpa.immortals.das.sourcecomposer.dfucomposers.ConsumingPipeComposer;
import mil.darpa.immortals.das.sourcecomposer.dfucomposers.SubstitutionComposer;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
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

        public final ApplicationProfile profileConfiguration;
        public final Path targetApplicationPath;
        private final String sessionIdentifier;

        private final LinkedList<String> dependencyLines;


        private ApplicationInstance(EnvironmentConfiguration environmentConfiguration, EnvironmentConfiguration.CompositionTarget applicationIdentifier, String sessionIdentifier, boolean newInstance) throws CompositionException, IOException {
            this.sessionIdentifier = sessionIdentifier;
            this.profileConfiguration = MockKnowledgeRepository.getInstance().getApplication(applicationIdentifier);

            if (newInstance) {
                targetApplicationPath = profileConfiguration.generateTargetApplicationPathValue(sessionIdentifier);

                Files.createDirectories(targetApplicationPath);
                FileUtils.copyDirectory(profileConfiguration.getSourceApplicationFilepath().toFile(), targetApplicationPath.toFile());

                dependencyLines = new LinkedList<>();

                dependencyLines.add("apply plugin: 'com.android.application'");
                dependencyLines.add("dependencies {");
                dependencyLines.add("}");

            } else {
                this.targetApplicationPath = this.profileConfiguration.generateTargetApplicationPathValue(this.sessionIdentifier);

                Path gradleFilepath = targetApplicationPath.resolve(profileConfiguration.getGradleTargetFile());

                if (Files.exists(gradleFilepath)) {
                    dependencyLines = new LinkedList<>(Files.readAllLines(gradleFilepath));
                } else {
                    dependencyLines = new LinkedList<>();
                }
            }
        }

        // TODO: Remove
        public Path getApplicationPath() {
            return targetApplicationPath;
        }


        public Path getSessionProductPath() {
            return EnvironmentConfiguration.getInstance().getSynthesisRootPath(sessionIdentifier);
        }

        public synchronized void addDependency(String dependencyString) throws IOException {
            String insertionString = "    compile '" + dependencyString + "'";

            if (!dependencyLines.contains(insertionString)) {
                dependencyLines.add(dependencyLines.size() - 1, insertionString);

                Path gradleFilepath = targetApplicationPath.resolve(profileConfiguration.getGradleTargetFile());
                StandardOpenOption fileOption = (Files.exists(gradleFilepath) ?
                        StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.CREATE_NEW);

                Files.write(gradleFilepath, dependencyLines, fileOption);
            }
        }

        public void replaceClassForControlPoint(DfuCompositionConfiguration configuration) throws CompositionException, IOException {
            ControlPointProfile cpc = configuration.controlPoint;

            if (cpc.augmentationType == AugmentationType.CompositionAugmentation) {

                for (String filepath : cpc.synthesisTargetFiles) {
                    Path targetPath = this.targetApplicationPath.resolve(filepath);
                    if (!Files.exists(targetPath)) {
                        throw new CompositionException.InvalidFilepathSpecifiedException(filepath);
                    }


                    String originalClasspath = configuration.controlPoint.originalDfu.consumingPipeSpecification.classPackage;
                    String newClasspath = configuration.getProductClasspath();

                    List<String> inputLines = Files.readAllLines(targetPath);
                    ArrayList<String> outputLines = new ArrayList<>(inputLines.size());
                    String originalClassname = originalClasspath.substring(originalClasspath.lastIndexOf(".") + 1, originalClasspath.length());

                    for (String inputLine : inputLines) {
                        if (inputLine.contains(originalClasspath)) {
                            outputLines.add(inputLine.replaceAll(originalClasspath, newClasspath));
                        } else if (inputLine.contains("new " + originalClassname)) {
                            outputLines.add(inputLine.replaceAll("new " + originalClassname, "new " + newClasspath));
                        } else {
                            outputLines.add(inputLine);
                        }
                    }

                    Files.write(targetPath, outputLines, StandardOpenOption.TRUNCATE_EXISTING);
                }
            }
        }

        public void build() {
            BuildBridge.getInstance().buildApplication(getApplicationPath());
        }


        public void executeAugmentation(DfuSubstitutionInstance substitutionInstance)
                throws CompositionException, IOException {
            SubstitutionComposer sc = new SubstitutionComposer(substitutionInstance);
            sc.augmentApplication(this);

        }

        public void executeAugmentation(DfuCompositionConfiguration dfuCompositionConfiguration, DfuInstance replacementDfu)
                throws CompositionException, IOException {

            this.replaceClassForControlPoint(dfuCompositionConfiguration);

            // Add the DFU to the application's dependencies
            this.addDependency(replacementDfu.getDependencyString());

            if (dfuCompositionConfiguration.performAnalysis) {
                if (dfuCompositionConfiguration.applicationProfile.deploymentPlatform == DeploymentPlatform.Java) {
                    this.addDependency("mil.darpa.immortals.components:product-analysis-bundle-android:+");

                } else if (dfuCompositionConfiguration.applicationProfile.deploymentPlatform == DeploymentPlatform.Android) {
                    this.addDependency("mil.darpa.immortals.components:product-analysis-bundle-base:+");
                }
            }
        }

        public void executeAnalysisAugmentation(DfuCompositionConfiguration dfuCompositionConfiguration) {
            ConsumingPipeComposer cpc = new ConsumingPipeComposer(EnvironmentConfiguration.getInstance(), dfuCompositionConfiguration);
        }


    }

    private final String sessionIdentifier;

    private final EnvironmentConfiguration environmentConfiguration;

    private final BuildBridge buildBridge;

    private final Map<String, ApplicationInstance> applicationInstances = new HashMap<>();

    private final MockKnowledgeRepository knowledgeRepository;

    private final SessionConfiguration sessionConfiguration;


    @Deprecated
    public SourceComposer() {
        this.sessionIdentifier = "S" + Long.toString(System.currentTimeMillis());
        this.environmentConfiguration = EnvironmentConfiguration.getInstance();
        this.buildBridge = BuildBridge.getInstance();
        this.knowledgeRepository = MockKnowledgeRepository.getInstance();
        this.sessionConfiguration = initializeSessionConfiguration();
    }

    public SourceComposer(String sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
        this.environmentConfiguration = EnvironmentConfiguration.getInstance();
        this.buildBridge = BuildBridge.getInstance();
        this.knowledgeRepository = MockKnowledgeRepository.getInstance();
        this.sessionConfiguration = initializeSessionConfiguration();
    }

    /**
     * Creates a new directory of the ATAKClient for manipulation
     *
     * @return The absolute path to the new client instance
     */
    // TODO: Throw the Composition exception, don't swallow as a runtime exception!
    public ApplicationInstance initializeApplicationInstance(EnvironmentConfiguration.CompositionTarget applicationIdentifier) throws IOException {
        try {
            return new ApplicationInstance(environmentConfiguration, applicationIdentifier, sessionIdentifier, true);
        } catch (CompositionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new DFU using the provided configuration
     *
     * @return The absolute path to the new dfu instance
     */
    public DfuInstance constructAndPublishComposedDfu(DfuCompositionConfiguration dfuCompositionConfiguration) throws CompositionException {
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

        buildBridge.buildSynthesisRepository(dfuCompositionConfiguration.sessionIdentifier);

        DfuInstance dfuInstance = new DfuInstance(dfuCompositionConfiguration.getProductClasspath(),
                dfuCompositionConfiguration.getProductDependencyIdentifier());
        return dfuInstance;
    }

    public DfuCompositionConfiguration constructCP2ControlPointComposition(
            String originalDependencyString, String originalClassPackageIdentifier, String augmenterDependencyString,
            String augmenterClassPackageIdentifier, Map<String, String> semanticParameterMap
    ) throws CompositionException {
        return Phase1Hacks.constructCP2ControlPointComposition(originalDependencyString, originalClassPackageIdentifier,
                augmenterDependencyString, augmenterClassPackageIdentifier, semanticParameterMap,
                this.sessionIdentifier);
    }


    public void executeCP2Composition(ApplicationInstance applicationInstance, DfuCompositionConfiguration dfuCompositionConfiguration) throws CompositionException, IOException {
        // Construct the DFU and return the dependency String to be inserted into the application
        SourceComposer.DfuInstance dfuInstance = constructAndPublishComposedDfu(dfuCompositionConfiguration);


        // Update the Application
        applicationInstance.executeAugmentation(dfuCompositionConfiguration, dfuInstance);
    }

    public void executeCP1Substitution(ApplicationInstance applicationInstance,
                                       DfuSubstitutionInstance dfuSubstitutionConfiguration)
            throws CompositionException, IOException {
        applicationInstance.executeAugmentation(dfuSubstitutionConfiguration);
    }

    public Path getSessionProductPath() {
        return EnvironmentConfiguration.getInstance().getSynthesisRootPath(sessionIdentifier);
    }

    private SessionConfiguration initializeSessionConfiguration() {
        SessionConfiguration sec =
                new SessionConfiguration(this.environmentConfiguration, this.sessionIdentifier);

        sec.initializeFilesystem();

        return sec;
    }
}
