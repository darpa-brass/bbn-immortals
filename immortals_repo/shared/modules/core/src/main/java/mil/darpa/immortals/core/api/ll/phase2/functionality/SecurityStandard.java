package mil.darpa.immortals.core.api.ll.phase2.functionality;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP2;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP2
@Description("Common security standards")
public enum SecurityStandard {
    Nothing("No security required"),
    FIPS140Dash1("Obsolete less secure NIST government security standard"),
    FIPS140Dash2("Current secure NIST government security standard"),
    NIST800Dash171("Recent governemnt contractor security standard"),
    Secret("Classified information lower security standard"),
    TopSceret("Classified information highest security standard");
    
    public final String description;
    
    SecurityStandard(String description) {
        this.description = description;
    }
}
