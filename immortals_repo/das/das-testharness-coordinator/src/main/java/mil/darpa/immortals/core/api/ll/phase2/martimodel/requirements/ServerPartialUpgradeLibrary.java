package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@Description("A server library upgrade that will cause a partial upgrade")
@P2CP3
public enum ServerPartialUpgradeLibrary {
    Dom4jCot_2("Newer cot processing library");

    public final String description;

    ServerPartialUpgradeLibrary(String description) {
        this.description = description;
    }
}
