package mil.darpa.immortals.configuration.sourcecomposer.paradigms;

import mil.darpa.immortals.das.sourcecomposer.CompositionException;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Specifies the details necessary to use a DFU
 */
public class ConfigurationContainer {

    /**
     * The specification of the DFU in the form of an {@link ConsumingPipeConfiguration}
     * <p>
     * In the future, a configuration in the form of this or another paradigm will be required, but for the moment,
     * this is the only type supported and is mandatory
     */
    @Nullable
    public final ConsumingPipeConfiguration consumingPipeSpecification;

    @Nullable
    public final SubstitutionConfiguration substitutionConfiguration;

    public ConfigurationContainer(@Nullable ConsumingPipeConfiguration consumingPipeSpecification, @Nullable SubstitutionConfiguration substitutionConfiguration) {
        this.consumingPipeSpecification = consumingPipeSpecification;
        this.substitutionConfiguration = substitutionConfiguration;
    }

    public Set<ConfigurationContainer> produceAnalysisConfigurations() throws CompositionException {
        HashSet<ConfigurationContainer> rval = new HashSet<>();

        Set<ConsumingPipeConfiguration> analysisCombinations =
                consumingPipeSpecification.produceAnalysisConfigurations();

        for (ConsumingPipeConfiguration config : analysisCombinations) {
            rval.add(new ConfigurationContainer(config, null));
        }

        return rval;
    }
}
