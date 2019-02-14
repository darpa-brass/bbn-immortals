package mil.darpa.immortals.core.api.ll.phase2.functionality;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP2;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP2
@Description("The requirements for all data that is transmitted over the wire")
public class DataInTransit {
    @P2CP2
    @Description("The required security standard to adhere to")
    public SecurityStandard securityStandard;

    public DataInTransit() {
    }

    public DataInTransit(SecurityStandard securityStandard) {
        this.securityStandard = securityStandard;
    }
}
