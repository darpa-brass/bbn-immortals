package mil.darpa.immortals.core.api.validation;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 8/31/17.
 */
public class ValidationStartData {
    public final LinkedList<String> validatorIdentifiers;
    public final LinkedList<String> clientIdentifiers;
    public final String sessionIdentifier;
    public final int maxRuntimeMS;
    public final int minRuntimeMS;


    public ValidationStartData(LinkedList<String> validatorIdentifiers, LinkedList<String> clientIdentifiers,
                               String sessionIdentifier, int maxRuntimeMS, int minRuntimeMS) {
        this.validatorIdentifiers = validatorIdentifiers;
        this.clientIdentifiers = clientIdentifiers;
        this.sessionIdentifier = sessionIdentifier;
        this.maxRuntimeMS = maxRuntimeMS;
        this.minRuntimeMS = minRuntimeMS;
    }

    public String toString() {
        return "{" + "\n\t\"sessionIdentifier\": \"" + sessionIdentifier +
                "\"\n\t\"clientIdentifiers\": \"" + clientIdentifiers.toString() +
                "\"\n\t\"validatorIdentifiers\": \"" + validatorIdentifiers.toString() +
                "\"\n\t\"maxRuntimeMS\": \"" + maxRuntimeMS +
                "\"\n\t\"minRuntimeMS\": \"" + minRuntimeMS + "\"\n}";
    }
}
