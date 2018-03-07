package mil.darpa.immortals.das.sourcecomposer.configuration;

import mil.darpa.immortals.das.sourcecomposer.configuration.paradigms.SubstitutionConfiguration;

/**
 * Defines the abstract information needed to apply a substitution to an application
 * Created by awellman@bbn.com on 5/2/17.
 */
public class DfuSubstitutionInstance {

    public final String sessionIdentifier;
    public final String controlPointUuid;

    public final SubstitutionConfiguration substitutionConfiguration;

    public DfuSubstitutionInstance(String sessionIdentifier, String controlPointUuid,
                                   SubstitutionConfiguration substitutionConfiguration) {
        this.sessionIdentifier = sessionIdentifier;
        this.controlPointUuid = controlPointUuid;
        this.substitutionConfiguration = substitutionConfiguration;
    }
}
