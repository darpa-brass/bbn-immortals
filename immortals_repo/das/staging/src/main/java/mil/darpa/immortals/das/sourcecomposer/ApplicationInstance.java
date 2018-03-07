package mil.darpa.immortals.das.sourcecomposer;

import mil.darpa.immortals.das.buildtools.GradleHelper;
import mil.darpa.immortals.das.hacks.configuration.applications.ApplicationOrigin;
import mil.darpa.immortals.das.hacks.configuration.applications.CompositionTarget;
import mil.darpa.immortals.das.hacks.configuration.applications.ApplicationProfile;
import mil.darpa.immortals.das.hacks.configuration.applications.ControlPointProfile;
import mil.darpa.immortals.das.hacks.MockKnowledgeRepository;
import mil.darpa.immortals.das.sourcecomposer.configuration.DfuCompositionConfiguration;
import mil.darpa.immortals.das.sourcecomposer.configuration.DfuSubstitutionInstance;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import mil.darpa.immortals.das.sourcecomposer.configuration.AugmentationType;
import mil.darpa.immortals.das.sourcecomposer.configuration.DeploymentPlatform;
import mil.darpa.immortals.das.sourcecomposer.dfucomposers.ConsumingPipeComposer;
import mil.darpa.immortals.das.sourcecomposer.dfucomposers.SubstitutionComposer;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 10/25/17.
 */
public class ApplicationInstance {

    public final ApplicationProfile profileConfiguration;
    public final Path targetApplicationPath;
    private final String sessionIdentifier;

    private final LinkedList<String> dependencyLines;


    ApplicationInstance(EnvironmentConfiguration environmentConfiguration, CompositionTarget applicationIdentifier, String sessionIdentifier, boolean newInstance) throws CompositionException, IOException {
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
        GradleHelper.getInstance().buildApplication(getApplicationPath());
    }


    public void executeAugmentation(DfuSubstitutionInstance substitutionInstance)
            throws CompositionException, IOException {
        SubstitutionComposer sc = new SubstitutionComposer(substitutionInstance);
        sc.augmentApplication(this);

    }

    public void executeAugmentation(DfuCompositionConfiguration dfuCompositionConfiguration, SourceComposer.DfuInstance replacementDfu)
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
