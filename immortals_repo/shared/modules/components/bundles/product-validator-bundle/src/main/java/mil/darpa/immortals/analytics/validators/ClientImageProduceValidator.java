package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.core.analytics.AnalyticsEventType;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public class ClientImageProduceValidator extends AbstractClientEventValidator {

    public ClientImageProduceValidator(@Nonnull Set<String> clientIdentifiers, boolean haltOnSuccessfulValidation) {
        super(clientIdentifiers, haltOnSuccessfulValidation);
    }

    @Override
    protected AnalyticsEventType getDesiredEventType() {
        return AnalyticsEventType.MyImageSent;
    }
}
