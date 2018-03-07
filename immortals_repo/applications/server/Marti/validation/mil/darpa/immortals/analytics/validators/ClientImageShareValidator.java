package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.core.analytics.AnalyticsEventType;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 7/27/16.
 */
public class ClientImageShareValidator extends AbstractClientShareValidator {
    public ClientImageShareValidator(@Nonnull Set<String> clientIdentifiers, boolean haltUponSuccessfulValidation) {
        super(clientIdentifiers, haltUponSuccessfulValidation);
    }

    @Override
    protected AnalyticsEventType getDesiredEventType() {
        return AnalyticsEventType.FieldImageReceived;
    }
}
