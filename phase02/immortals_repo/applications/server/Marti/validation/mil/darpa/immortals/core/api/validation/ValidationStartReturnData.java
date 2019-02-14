package mil.darpa.immortals.core.api.validation;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 9/5/17.
 */
public class ValidationStartReturnData {

    public final int expectedDurationSeconds;
    public final LinkedList<String> validatorIdentifiers;

    public ValidationStartReturnData(int expectedDurationSeconds, LinkedList<String> validatorIdentifiers) {
        this.expectedDurationSeconds = expectedDurationSeconds;
        this.validatorIdentifiers = validatorIdentifiers;
    }
}
