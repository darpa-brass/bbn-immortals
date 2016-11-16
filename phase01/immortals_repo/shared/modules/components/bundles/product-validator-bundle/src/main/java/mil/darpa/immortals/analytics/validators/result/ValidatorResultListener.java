package mil.darpa.immortals.analytics.validators.result;

import mil.darpa.immortals.analytics.validators.ValidatorInterface;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
public interface ValidatorResultListener {
    void validationComplete(ValidatorInterface validator, ValidatorResult result);
}
