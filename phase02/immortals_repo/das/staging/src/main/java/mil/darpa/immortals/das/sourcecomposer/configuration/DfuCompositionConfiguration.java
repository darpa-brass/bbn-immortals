package mil.darpa.immortals.das.sourcecomposer.configuration;

import mil.darpa.immortals.das.hacks.configuration.applications.CompositionTarget;
import mil.darpa.immortals.das.hacks.configuration.applications.ApplicationProfile;
import mil.darpa.immortals.das.hacks.configuration.applications.ControlPointProfile;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import mil.darpa.immortals.das.sourcecomposer.CompositionException;
import mil.darpa.immortals.das.hacks.MockKnowledgeRepository;
import mil.darpa.immortals.das.sourcecomposer.configuration.paradigms.ConfigurationContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A configuration used to define the composition of DFUs
 * <p>
 * Created by awellman@bbn.com on 9/27/16.
 */
// TODO: Rename DfuCompositionInstance
public class DfuCompositionConfiguration {

    public class ShallowDfuCompositionConfiguration {
        public String sessionIdentifier;
        public String controlPointUuid;
        public CompositionTarget compositionTarget;
        public ArrayList<ConfigurationContainer> dfuCompositionSequence;
        public boolean performAnalysis;

        public DfuCompositionConfiguration toDfuCompositionConfiguration() throws CompositionException {
            MockKnowledgeRepository kr = MockKnowledgeRepository.getInstance();
            
            ApplicationProfile applicationProfile = kr.getApplication(compositionTarget);
            ControlPointProfile controlPointProfile = applicationProfile.getControlPointByUuid(controlPointUuid);

            return new DfuCompositionConfiguration(
                    sessionIdentifier,
                    applicationProfile,
                    controlPointProfile,
                    performAnalysis,
                    dfuCompositionSequence
            );
        }
    }

    public String getProductClasspath() {
        EnvironmentConfiguration ec = EnvironmentConfiguration.getInstance();
        return ec.synthesizedDfuPackage + (ec.synthesizedDfuPackage.endsWith(".") ? "" : ".") + compositionIdentifier;
    }

    public String getProductDependencyIdentifier() {
        EnvironmentConfiguration ec = EnvironmentConfiguration.getInstance();
        return ec.synthesizedDfuPackage + ":" + compositionIdentifier + ":+";
    }

    public Path getProductDirectory() {
        return EnvironmentConfiguration.getInstance().getSynthesizedDfuProjectFilepath(sessionIdentifier).resolve(compositionIdentifier);
    }

    public Path getProductSourceDirectory() {
        return getProductDirectory().resolve(EnvironmentConfiguration.getInstance().synthesizedDfuSourceSubdirectory);
    }


    public DfuCompositionConfiguration(
            @Nonnull String sessionIdentifier,
            @Nonnull ApplicationProfile applicationProfile,
            @Nonnull ControlPointProfile controlPoint,
            boolean performAnalysis,
            @Nonnull ArrayList<ConfigurationContainer> dfuCompositionSequence
    ) {
        this.sessionIdentifier = sessionIdentifier;
        this.applicationProfile = applicationProfile;
        this.controlPoint = controlPoint;

        this.performAnalysis = performAnalysis;
        this.dfuCompositionSequence = dfuCompositionSequence;

        this.compositionIdentifier = sessionIdentifier + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Nonnull
    public final ControlPointProfile controlPoint;

    /**
     * The identifier for the newly-formed DFU
     */
    @Nullable
    public final String compositionIdentifier;

    /**
     * The session identifier
     */
    @Nonnull
    public final String sessionIdentifier;


    /**
     * Whether or not to insert analysis reporting
     */
    public final boolean performAnalysis;

    /**
     * The sequence of DFUs to be used to construct the new DFU
     */
    @Nonnull
    public final ArrayList<ConfigurationContainer> dfuCompositionSequence;


    public final ApplicationProfile applicationProfile;

//    public DfuCompositionConfiguration(@Nonnull String controlPointUuid, @Nonnull DeploymentPlatform deploymentPlatform, @Nonnull String sessionIdentifier, @Nonnull ConfigurationContainer originalDfu, @Nonnull ArrayList<ConfigurationContainer> dfuCompositionSequence, boolean performAnalysis) {
//    }

    public Set<DfuCompositionConfiguration> produceAnalysisConfigurations() throws CompositionException {
        Set<ArrayList<ConfigurationContainer>> dfuListSet = new HashSet<>();

        for (ConfigurationContainer genericConfiguration : dfuCompositionSequence) {
            Set<ConfigurationContainer> containerSet = genericConfiguration.produceAnalysisConfigurations();

            if (dfuListSet.isEmpty()) {
                for (ConfigurationContainer container : containerSet) {
                    ArrayList<ConfigurationContainer> newList = new ArrayList(1);
                    newList.add(container);
                    dfuListSet.add(newList);
                }

            } else {
                Set<ArrayList<ConfigurationContainer>> newContainerListSet = new HashSet<>(dfuListSet.size() * containerSet.size());

                for (ArrayList<ConfigurationContainer> currentContainerList : dfuListSet) {
                    for (ConfigurationContainer newContainer : containerSet) {
                        ArrayList<ConfigurationContainer> newList = new ArrayList<>(dfuListSet.size() + 1);
                        newList.addAll(currentContainerList);
                        newList.add(newContainer);
                        newContainerListSet.add(newList);
                    }
                }
                if (!newContainerListSet.isEmpty()) {
                    dfuListSet = newContainerListSet;
                }

            }
        }

        Set<DfuCompositionConfiguration> returnSet = new HashSet<>(dfuListSet.size());
        for (ArrayList<ConfigurationContainer> compositionSequence : dfuListSet) {
            returnSet.add(new DfuCompositionConfiguration(
                    sessionIdentifier + UUID.randomUUID().toString().substring(0,8),
                    applicationProfile,
                    controlPoint,
                    performAnalysis,
                    compositionSequence
            ));
        }
        return returnSet;
    }
}

/* JSON Example
{
    "originalDfu": {
        "dependencyString": "mil.darpa.immortals.dfus:ImageUtilsAndroid:+",
        "consumingPipeSpecification": {
            "classPackage": "mil.darpa.immortals.dfus.images.BitmapReader",
            "constructorParameters": [
                {
                    "providedByApplication": true,
                    "classType": "mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe<android.graphics.Bitmap>"
                }
            ]
        }
    },
    "dfuCompositionSequence": [
        {
            "dependencyString": "mil.darpa.immortals.dfus:ImageUtilsAndroid:+",
            "consumingPipeSpecification": {
                "classPackage": "mil.darpa.immortals.dfus.images.BitmapReader",
                "constructorParameters": [
                    {
                        "providedByApplication": true,
                        "classType": "mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe<android.graphics.Bitmap>"
                    }
                ]
            }
        },
        {
            "dependencyString": "mil.darpa.immortals.dfus:ImageUtilsAndroid:+",
            "consumingPipeSpecification": {
                "classPackage": "mil.darpa.immortals.dfus.images.BitmapScaler",
                "constructorParameters": [
                    {
                        "providedByApplication": false,
                        "classType": "java.lang.Integer",
                        "value": "0.85"
                    },
                    {
                        "providedByApplication": true,
                        "classType": "mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe<android.graphics.Bitmap>"
                    }
                ]
            }
        }
    ]
}
 */



