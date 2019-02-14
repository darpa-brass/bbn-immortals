package mil.darpa.immortals.das.sourcecomposer;

import mil.darpa.immortals.das.buildtools.AdaptationGradleHelper;
import mil.darpa.immortals.das.buildtools.GradleHelper;
import mil.darpa.immortals.das.hacks.configuration.applications.CompositionTarget;
import mil.darpa.immortals.das.hacks.MockKnowledgeRepository;
import mil.darpa.immortals.das.hacks.Phase1Hacks;
import mil.darpa.immortals.das.sourcecomposer.configuration.DfuCompositionConfiguration;
import mil.darpa.immortals.das.sourcecomposer.configuration.DfuSubstitutionInstance;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import mil.darpa.immortals.das.configuration.SessionConfiguration;
import mil.darpa.immortals.das.sourcecomposer.dfucomposers.ConsumingPipeComposer;

import java.io.IOException;
import java.nio.file.Path;
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

    private final String sessionIdentifier;

    private final EnvironmentConfiguration environmentConfiguration;

    private final GradleHelper buildBridge;

    private final Map<String, ApplicationInstance> applicationInstances = new HashMap<>();

    private final mil.darpa.immortals.das.hacks.MockKnowledgeRepository knowledgeRepository;

    private final SessionConfiguration sessionConfiguration;


    @Deprecated
    public SourceComposer() {
        this.sessionIdentifier = "S" + Long.toString(System.currentTimeMillis());
        this.environmentConfiguration = EnvironmentConfiguration.getInstance();
        this.buildBridge = GradleHelper.getInstance();
        this.knowledgeRepository = mil.darpa.immortals.das.hacks.MockKnowledgeRepository.getInstance();
        this.sessionConfiguration = initializeSessionConfiguration();
    }

    public SourceComposer(String sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
        this.environmentConfiguration = EnvironmentConfiguration.getInstance();
        this.buildBridge = GradleHelper.getInstance();
        this.knowledgeRepository = MockKnowledgeRepository.getInstance();
        this.sessionConfiguration = initializeSessionConfiguration();
    }

    /**
     * Creates a new directory of the ATAKClient for manipulation
     *
     * @return The absolute path to the new client instance
     */
    // TODO: Throw the Composition exception, don't swallow as a runtime exception!
    public ApplicationInstance initializeApplicationInstance(CompositionTarget applicationIdentifier) throws IOException {
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
            AdaptationGradleHelper.publishSourceTree(dfuCompositionConfiguration, fileMap);
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
