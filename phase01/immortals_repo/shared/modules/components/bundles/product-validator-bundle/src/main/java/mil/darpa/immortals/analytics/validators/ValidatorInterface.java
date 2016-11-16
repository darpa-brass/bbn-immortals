package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.analytics.validators.result.ValidatorResult;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;

/**
 * Created by awellman@bbn.com on 10/19/16.
 */
public interface ValidatorInterface {

    void processEvent(AnalyticsEvent event);

    String getValidatorName();

    ValidatorResult attemptValidation();

}
