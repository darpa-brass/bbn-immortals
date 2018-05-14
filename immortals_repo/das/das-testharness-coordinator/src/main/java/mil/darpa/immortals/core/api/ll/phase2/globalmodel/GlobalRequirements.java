package mil.darpa.immortals.core.api.ll.phase2.globalmodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP2;
import mil.darpa.immortals.core.api.ll.phase2.functionality.DataInTransit;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP2
@Description("Requirements applicable to the entire System under Test")
public class GlobalRequirements {

    @P2CP2
    @Description("The transmission of data")
    public DataInTransit dataInTransit;
    
    public GlobalRequirements() {}

    public GlobalRequirements(DataInTransit dataInTransit) {
        this.dataInTransit = dataInTransit;
    }
}
