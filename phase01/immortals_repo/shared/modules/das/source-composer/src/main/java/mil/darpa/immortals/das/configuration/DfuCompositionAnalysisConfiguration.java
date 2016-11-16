package mil.darpa.immortals.das.configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by awellman@bbn.com on 10/7/16.
 */
public class DfuCompositionAnalysisConfiguration {
    /**
     * The identifier for the newly-formed DFU
     */
    @Nullable
    public final String sessionIdentifier;

    public final DeploymentPlatform targetPlatform;

    /**
     * The DFU whom the newly created DFU must adhere to in terms of interface
     */
    @Nonnull
    public final DfuCompositionConfiguration.DfuConfigurationContainer originalDfu;
    /**
     * The sequence of DFUs to be used to construct the new DFU
     */
    @Nonnull
    public final ArrayList<DfuAnalysisConfigurationContainer> dfuCompositionSequence;

    public DfuCompositionAnalysisConfiguration(@Nonnull String sessionIdentifier, @Nonnull DeploymentPlatform targetPlatform, @Nonnull DfuCompositionConfiguration.DfuConfigurationContainer originalDfu, @Nonnull ArrayList<DfuAnalysisConfigurationContainer> dfuCompositionSequence) {
        this.sessionIdentifier = sessionIdentifier;
        this.targetPlatform = targetPlatform;
        this.originalDfu = originalDfu;
        this.dfuCompositionSequence = dfuCompositionSequence;
    }

    public Set<DfuCompositionConfiguration> produceCombinations() {
        Set<ArrayList<DfuCompositionConfiguration.DfuConfigurationContainer>> dfuListSet = new HashSet<>();


//            Set<ArrayList<DfuCompositionConfiguration.DfuParameter>> dfuListSet = new HashSet<>();
        for (DfuAnalysisConfigurationContainer configurationContainer : dfuCompositionSequence) {
            Set<DfuCompositionConfiguration.DfuConfigurationContainer> containerSet = configurationContainer.produceCombinations();

            if (dfuListSet.isEmpty()) {
                for (DfuCompositionConfiguration.DfuConfigurationContainer container : containerSet) {
                    ArrayList<DfuCompositionConfiguration.DfuConfigurationContainer> newList = new ArrayList(1);
                    newList.add(container);
                    dfuListSet.add(newList);
                }

            } else {
                Set<ArrayList<DfuCompositionConfiguration.DfuConfigurationContainer>> newContainerListSet = new HashSet<>(dfuListSet.size() * containerSet.size());

                for (ArrayList<DfuCompositionConfiguration.DfuConfigurationContainer> currentContainerList : dfuListSet) {
                    for (DfuCompositionConfiguration.DfuConfigurationContainer newContainer : containerSet) {
                        ArrayList<DfuCompositionConfiguration.DfuConfigurationContainer> newList = new ArrayList<>(dfuListSet.size() + 1);
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
        for (ArrayList<DfuCompositionConfiguration.DfuConfigurationContainer> compositionSequences : dfuListSet) {
            String configurationIdentifier = sessionIdentifier + "_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0,12);
            returnSet.add(new DfuCompositionConfiguration(configurationIdentifier, targetPlatform, originalDfu, compositionSequences));
        }

        return returnSet;
    }


    public static class DfuAnalysisConfigurationContainer {
        @Nonnull
        public String dependencyString;

        public boolean performAnalysis;

        @Nonnull
        public ConsumingPipeAnalysisSpecification consumingPipeSpecification;

        public Set<DfuCompositionConfiguration.DfuConfigurationContainer> produceCombinations() {
            Set<DfuCompositionConfiguration.DfuConfigurationContainer> configurations = new HashSet<>();

            Set<DfuCompositionConfiguration.ConsumingPipeSpecification> specifications = consumingPipeSpecification.produceCombinations();

            for (DfuCompositionConfiguration.ConsumingPipeSpecification specification : specifications) {
                configurations.add(new DfuCompositionConfiguration.DfuConfigurationContainer(dependencyString, specification, performAnalysis));
            }

            return configurations;
        }
    }

    public static class ConsumingPipeAnalysisSpecification {

        public String classPackageIdentifier;


        public ArrayList<DfuCompositionConfiguration.DfuPermutationParameter> constructorParameters;

        public Set<DfuCompositionConfiguration.ConsumingPipeSpecification> produceCombinations() {

            Set<ArrayList<DfuCompositionConfiguration.DfuParameter>> parameterListSet = new HashSet<>();

            if (constructorParameters == null || constructorParameters.isEmpty()) {
                parameterListSet.add(new ArrayList<>());

            } else {

                for (DfuCompositionConfiguration.DfuPermutationParameter parameter : constructorParameters) {
                    Set<DfuCompositionConfiguration.DfuParameter> parameterSet = parameter.generateParameters();

                    if (parameterSet != null) {
                        if (parameterListSet.isEmpty()) {
                            for (DfuCompositionConfiguration.DfuParameter param : parameterSet) {
                                ArrayList<DfuCompositionConfiguration.DfuParameter> newList = new ArrayList(1);
                                newList.add(param);
                                parameterListSet.add(newList);
                            }

                        } else {
                            Set<ArrayList<DfuCompositionConfiguration.DfuParameter>> newParameterListSet = new HashSet<>(parameterListSet.size() * parameterSet.size());

                            for (List<DfuCompositionConfiguration.DfuParameter> currentParamList : parameterListSet) {
                                for (DfuCompositionConfiguration.DfuParameter newParam : parameterSet) {
                                    ArrayList<DfuCompositionConfiguration.DfuParameter> newList = new ArrayList<DfuCompositionConfiguration.DfuParameter>(currentParamList.size() + 1);
                                    newList.addAll(currentParamList);
                                    newList.add(newParam);
                                    newParameterListSet.add(newList);
                                }
                            }
                            parameterListSet = newParameterListSet;
                        }
                    }
                }
            }

            Set<DfuCompositionConfiguration.ConsumingPipeSpecification> returnSet = new HashSet<>(parameterListSet.size());
            for (ArrayList<DfuCompositionConfiguration.DfuParameter> parameters : parameterListSet) {
                returnSet.add(new DfuCompositionConfiguration.ConsumingPipeSpecification(classPackageIdentifier, parameters));
            }
            return returnSet;
        }
    }

}
