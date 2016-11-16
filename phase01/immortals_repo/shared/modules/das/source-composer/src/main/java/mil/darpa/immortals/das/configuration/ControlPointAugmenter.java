package mil.darpa.immortals.das.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Created by awellman@bbn.com on 10/21/16.
 */
public class ControlPointAugmenter {

    public static DfuCompositionConfiguration constructCP2ControlPointComposition(String originalDependencyString, String originalClassPackageIdentifier, String augmenterDependencyString, String augmenterClassPackageIdentifier, List<String> parameters, String sessionIdentifier) {
        try {
            InputStream is = EnvironmentConfiguration.class.getResourceAsStream("/CP2CompositionTemplate.json");
            DfuCompositionConfiguration configuration = GsonHelper.getInstance().fromInputStream(is, DfuCompositionConfiguration.class);

            configuration.sessionIdentifier = sessionIdentifier;
            configuration.originalDfu.dependencyString = originalDependencyString;
            configuration.originalDfu.consumingPipeSpecification.classPackageIdentifier = originalClassPackageIdentifier;

            configuration.dfuCompositionSequence.get(0).dependencyString = originalDependencyString;
            configuration.dfuCompositionSequence.get(0).consumingPipeSpecification.classPackageIdentifier = originalClassPackageIdentifier;

            configuration.dfuCompositionSequence.get(1).dependencyString = augmenterDependencyString;
            configuration.dfuCompositionSequence.get(1).consumingPipeSpecification.classPackageIdentifier = augmenterClassPackageIdentifier;
            configuration.dfuCompositionSequence.get(1).consumingPipeSpecification.constructorParameters.get(0).value = parameters.get(0);

            return configuration;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static DfuCompositionConfiguration constructCP2ControlPointComposition(String originalDependencyString, String originalClassPackageIdentifier, String augmenterDependencyString, String augmenterClassPackageIdentifier, List<String> parameters) {
        return constructCP2ControlPointComposition(originalDependencyString, originalClassPackageIdentifier, augmenterDependencyString, augmenterClassPackageIdentifier, parameters, "I" + UUID.randomUUID().toString().replaceAll("-", "").substring(0,12));
    }
}
