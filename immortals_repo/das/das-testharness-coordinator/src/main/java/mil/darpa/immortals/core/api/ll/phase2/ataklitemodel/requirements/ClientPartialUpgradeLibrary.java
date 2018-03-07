package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP3
@Description("A client library upgrade that will cause a partial upgrade")
public enum ClientPartialUpgradeLibrary {
    @P2CP3
    Dropbox_X_X("Version of dropbox containing a resolution for a security flaw");

    public final String description;

    ClientPartialUpgradeLibrary(String description) {
        this.description = description;
    }
}
