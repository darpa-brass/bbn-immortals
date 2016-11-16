package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.analytics.validators.result.ValidatorResult;
import mil.darpa.immortals.analytics.validators.result.ValidatorState;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public class ClientLocationUpdateValidator extends AbstractClientEventValidator {

    public ClientLocationUpdateValidator(@Nonnull Set<String> clientIdentifiers) {
        super(clientIdentifiers);
    }

    @Override
    protected AnalyticsEventType getDesiredEventType() {
        return AnalyticsEventType.MyLocationProduced;
    }

    @Override
    public String getValidatorName() {
        return "client-location-produce";
    }
}
