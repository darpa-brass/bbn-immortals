package mil.darpa.immortals.analytics.configuration;

import javax.annotation.Nonnull;
import java.util.HashSet;

/**
 * Created by awellman@bbn.com on 10/17/16.
 */
public class EvaluationConfiguration {

    public final HashSet<EvaluationClientConfiguration> clientConfigurationSet = new HashSet<>();

    public final String evaluationIdentifier;

    public EvaluationConfiguration(@Nonnull String evaluationIdentifier) {
        this.evaluationIdentifier = evaluationIdentifier;
    }

    public void addIdenticalClients(@Nonnull EvaluationClientConfiguration clientConfiguration, int clientCount) {
        for (int i = 0; i < clientCount; i++) {
            EvaluationClientConfiguration newConfiguration = new EvaluationClientConfiguration(
                    clientConfiguration.latestSABroadcastIntervalMS,
                    clientConfiguration.imageBroadcastIntervalMS);
            clientConfigurationSet.add(newConfiguration);
        }
    }
}
