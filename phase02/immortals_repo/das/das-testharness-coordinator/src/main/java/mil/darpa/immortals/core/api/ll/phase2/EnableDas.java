package mil.darpa.immortals.core.api.ll.phase2;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Unstable;

/**
 * Created by awellman@bbn.com on 9/21/17.
 */
@Unstable
@Description("Used to enable disable das functionality, differentiating between Baseline and Challenge Scenarios")
public class EnableDas {
    @Description("Indicates if the DAS functionality should bet set to enabled or disabled")
    public boolean dasEnabled;

    public EnableDas() {
    }

    public EnableDas(boolean dasEnabled) {
        this.dasEnabled = dasEnabled;
    }
}
