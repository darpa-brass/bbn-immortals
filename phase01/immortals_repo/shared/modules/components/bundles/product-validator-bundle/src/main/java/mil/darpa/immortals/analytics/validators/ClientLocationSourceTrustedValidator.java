package mil.darpa.immortals.analytics.validators;

import com.google.gson.Gson;
import mil.darpa.immortals.analytics.validators.result.ValidatorResult;
import mil.darpa.immortals.analytics.validators.result.ValidatorState;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 11/10/16.
 */
public class ClientLocationSourceTrustedValidator extends AbstractClientLocationSourceValidator {

    private List<String> tags = null;

    @Override
    public String getValidatorName() {
        return "client-location-source-trusted";
    }

    @Override
    List<String> getValidLocationTags() {
        if (tags == null) {
            tags = new LinkedList<>();
            tags.add("m-r-p");
            tags.add("m-r-e");
            tags.add("m-r-t");
        }
        return tags;
    }

    public ClientLocationSourceTrustedValidator(@Nonnull Set<String> clientIdentifiers) {
        super(clientIdentifiers);
    }
}
